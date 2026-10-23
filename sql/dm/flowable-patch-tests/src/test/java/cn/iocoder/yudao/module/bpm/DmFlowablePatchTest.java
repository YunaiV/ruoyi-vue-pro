package cn.iocoder.yudao.module.bpm;

import cn.iocoder.yudao.module.bpm.framework.flowable.config.DmFlowableConfiguration;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.eventregistry.impl.EventRegistryEngine;
import org.flowable.eventregistry.api.EventDeployment;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 达梦补丁的版本兼容与真实数据库回归测试
 *
 * @author 芋道源码
 */
class DmFlowablePatchTest {

    @Test
    void testOtherDatabaseUnchanged() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:dm_patch_regression", "sa", "");
        SpringProcessEngineConfiguration configuration = configuration(dataSource);
        configureDm(configuration);
        assertNull(configuration.getDatabaseType());
        assertTrue(configuration.isBulkInsertEnabled());
    }

    @Test
    void testDmSchemaAndProcess() throws Exception {
        DataSource dataSource = dmDataSource();
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("DM DBMS", connection.getMetaData().getDatabaseProductName());
        }
        String instanceId;
        SpringProcessEngineConfiguration first = configuration(dataSource);
        first.setDatabaseSchemaUpdate("true");
        configureDm(first);
        assertEquals("oracle", first.getDatabaseType());
        ProcessEngine engine = first.buildProcessEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("approval.bpmn20.xml").deploy();
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("dmApproval", "dm-test",
                    Collections.<String, Object>singletonMap("amount", 100));
            instanceId = instance.getId();
            Task task = engine.getTaskService().createTaskQuery().processInstanceId(instanceId).singleResult();
            assertEquals("审批一", task.getName());
            engine.getTaskService().setAssignee(task.getId(), "1");
            engine.getTaskService().complete(task.getId());
            assertEquals("审批二", engine.getTaskService().createTaskQuery().processInstanceId(instanceId).singleResult().getName());
        } finally {
            engine.close();
        }
        // 再次开启 schema-update，验证已存在的工作流表不会重复创建
        SpringProcessEngineConfiguration second = configuration(dataSource);
        second.setDatabaseSchemaUpdate("true");
        configureDm(second);
        engine = second.buildProcessEngine();
        try {
            Task task = engine.getTaskService().createTaskQuery().processInstanceId(instanceId).singleResult();
            engine.getTaskService().complete(task.getId());
            assertNull(engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instanceId).singleResult());
            HistoricProcessInstance history = engine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            assertNotNull(history.getEndTime());
            assertEquals(2, engine.getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(instanceId).finished().count());
            assertEquals(1, engine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("dmApproval").listPage(0, 1).size());
        } finally {
            engine.close();
        }
    }

    @Test
    void testDmReturnAndReject() {
        ProcessEngine engine = dmEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("approval.bpmn20.xml").deploy();
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("dmApproval");
            Task task = engine.getTaskService().createTaskQuery().processInstanceId(instance.getId()).singleResult();
            engine.getTaskService().complete(task.getId());
            engine.getRuntimeService().createChangeActivityStateBuilder().processInstanceId(instance.getId())
                    .moveActivityIdTo("second", "first").changeState();
            assertEquals("审批一", engine.getTaskService().createTaskQuery().processInstanceId(instance.getId()).singleResult().getName());
            engine.getRuntimeService().deleteProcessInstance(instance.getId(), "审批拒绝");
            assertEquals("审批拒绝", engine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(instance.getId()).singleResult().getDeleteReason());
        } finally {
            engine.close();
        }
    }

    @Test
    void testDmCancelAndVariables() {
        ProcessEngine engine = dmEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("approval.bpmn20.xml").deploy();
            String longText = String.join("", Collections.nCopies(3000, "达梦🙂"));
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("dmApproval", "cancel-test",
                    Collections.<String, Object>singletonMap("longText", longText));
            engine.getRuntimeService().setVariable(instance.getId(), "approved", true);
            assertEquals(longText, engine.getRuntimeService().getVariable(instance.getId(), "longText"));
            assertEquals(true, engine.getRuntimeService().getVariable(instance.getId(), "approved"));
            engine.getRuntimeService().deleteProcessInstance(instance.getId(), "发起人取消");
            assertEquals("发起人取消", engine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey("cancel-test").orderByProcessInstanceStartTime().desc().listPage(0, 1).get(0).getDeleteReason());
            assertEquals(longText, engine.getHistoryService().createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getId()).variableName("longText").singleResult().getValue());
        } finally {
            engine.close();
        }
    }

    @Test
    void testDmParallelGateway() {
        ProcessEngine engine = dmEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("parallel.bpmn20.xml").deploy();
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("dmParallel");
            List<Task> tasks = engine.getTaskService().createTaskQuery().processInstanceId(instance.getId()).list();
            assertEquals(2, tasks.size());
            engine.getTaskService().complete(tasks.get(0).getId());
            assertEquals(1, engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
            engine.getTaskService().complete(tasks.get(1).getId());
            assertEquals(0, engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
        } finally {
            engine.close();
        }
    }

    @Test
    void testDmTimer() {
        ProcessEngine engine = dmEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("timer.bpmn20.xml").deploy();
            ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("dmTimer");
            String timerId = engine.getManagementService().createTimerJobQuery().processInstanceId(instance.getId()).singleResult().getId();
            String jobId = engine.getManagementService().moveTimerToExecutableJob(timerId).getId();
            engine.getManagementService().executeJob(jobId);
            assertEquals(0, engine.getManagementService().createTimerJobQuery().processInstanceId(instance.getId()).count());
            assertEquals("定时后审批", engine.getTaskService().createTaskQuery().processInstanceId(instance.getId()).singleResult().getName());
            engine.getRuntimeService().deleteProcessInstance(instance.getId(), "测试结束");
        } finally {
            engine.close();
        }
    }

    @Test
    void testDmDmnSchemaAndDecision() {
        DataSource dataSource = dmDataSource();
        SpringDmnEngineConfiguration configuration = new SpringDmnEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setTransactionManager(new DataSourceTransactionManager(dataSource));
        configureDm(configuration);
        configuration.setDatabaseSchemaUpdate("true");
        DmnEngine engine = configuration.buildDmnEngine();
        try {
            engine.getDmnRepositoryService().createDeployment().addClasspathResource("amount.dmn").deploy();
            Map<String, Object> result = engine.getDmnDecisionService().createExecuteDecisionBuilder()
                    .decisionKey("dmAmount").variable("amount", 100).executeWithSingleResult();
            assertEquals("通过", result.get("result"));
        } finally {
            engine.close();
        }
        // DMN 在 6.8.0 使用 Liquibase，重复启动必须能识别已存在的 changelog 表
        configuration = new SpringDmnEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setTransactionManager(new DataSourceTransactionManager(dataSource));
        configureDm(configuration);
        configuration.setDatabaseSchemaUpdate("true");
        engine = configuration.buildDmnEngine();
        engine.close();
    }

    @Test
    void testBootAutoConfiguration() {
        dmDataSource();
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.NONE)
                .properties("flowable.database-schema-update=true", "flowable.async-executor-activate=false",
                        "logging.level.root=WARN", "spring.main.banner-mode=off")
                .run()) {
            ProcessEngine engine = context.getBean(ProcessEngine.class);
            assertEquals("oracle", engine.getProcessEngineConfiguration().getDatabaseType());
            assertFalse(((SpringProcessEngineConfiguration) engine.getProcessEngineConfiguration()).isBulkInsertEnabled());
            assertNotNull(engine.getRepositoryService().createDeploymentQuery().listPage(0, 1));
            DmnEngine dmn = context.getBean(DmnEngine.class);
            assertEquals("oracle", dmn.getDmnEngineConfiguration().getDatabaseType());
            assertFalse(dmn.getDmnEngineConfiguration().isBulkInsertEnabled());
            assertEquals("通过", dmn.getDmnDecisionService().createExecuteDecisionBuilder()
                    .decisionKey("dmAutoTwo").variable("amount", 100).executeWithSingleResult().get("result"));
            String model = resourceText("/amount.dmn");
            dmn.getDmnRepositoryService().createDeployment().addString("one.dmn", model)
                    .addString("two.dmn", model.replace("dmAmount", "dmAmountTwo")).deploy();
            assertEquals("通过", dmn.getDmnDecisionService().createExecuteDecisionBuilder()
                    .decisionKey("dmAmountTwo").variable("amount", 100).executeWithSingleResult().get("result"));
            EventRegistryEngine event = context.getBean(EventRegistryEngine.class);
            assertEquals("oracle", event.getEventRegistryEngineConfiguration().getDatabaseType());
            assertFalse(event.getEventRegistryEngineConfiguration().isBulkInsertEnabled());
            assertTrue(event.getEventRepositoryService().createEventDefinitionQuery().eventDefinitionKey("dmAutoEventOne").count() > 0);
            assertTrue(event.getEventRepositoryService().createEventDefinitionQuery().eventDefinitionKey("dmAutoEventTwo").count() > 0);
            EventDeployment deployment = event.getEventRepositoryService().createDeployment()
                    .addString("one.event", "{\"key\":\"dmEventOne\",\"name\":\"One\",\"payload\":[]}")
                    .addString("two.event", "{\"key\":\"dmEventTwo\",\"name\":\"Two\",\"payload\":[]}").deploy();
            assertEquals(2, event.getEventRepositoryService().createEventDefinitionQuery().deploymentId(deployment.getId()).count());
        }
    }

    private ProcessEngine dmEngine() {
        SpringProcessEngineConfiguration configuration = configuration(dmDataSource());
        configuration.setDatabaseSchemaUpdate("true");
        configureDm(configuration);
        return configuration.buildProcessEngine();
    }

    private static DataSource dmDataSource() {
        assumeTrue(System.getenv("DM_JDBC_URL") != null, "设置 DM_JDBC_URL 后执行真实达梦测试");
        return new DriverManagerDataSource(System.getenv("DM_JDBC_URL"),
                System.getenv("DM_JDBC_USER"), System.getenv("DM_JDBC_PASSWORD"));
    }

    @Test
    void testDmTransactionRollback() {
        ProcessEngine engine = dmEngine();
        try {
            engine.getRepositoryService().createDeployment().addClasspathResource("approval.bpmn20.xml").deploy();
            String businessKey = UUID.randomUUID().toString();
            SpringProcessEngineConfiguration configuration = (SpringProcessEngineConfiguration) engine.getProcessEngineConfiguration();
            TransactionTemplate transaction = new TransactionTemplate(configuration.getTransactionManager());
            assertThrows(IllegalStateException.class, () -> transaction.execute(status -> {
                engine.getRuntimeService().startProcessInstanceByKey("dmApproval", businessKey);
                throw new IllegalStateException("回滚测试");
            }));
            assertEquals(0, engine.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey(businessKey).count());
            assertEquals(0, engine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).count());
        } finally {
            engine.close();
        }
    }

    private void configureDm(Object configuration) {
        DmFlowableConfiguration.dmEngineConfigurationBeanPostProcessor().postProcessBeforeInitialization(configuration, "testEngineConfiguration");
    }

    private String resourceText(String name) {
        try (InputStream input = getClass().getResourceAsStream(name);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private SpringProcessEngineConfiguration configuration(DataSource dataSource) {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setTransactionManager(new DataSourceTransactionManager(dataSource));
        configuration.setAsyncExecutorActivate(false);
        return configuration;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(DmFlowableConfiguration.class)
    static class TestApplication {

        @Bean
        public DataSource dataSource() {
            return dmDataSource();
        }

    }

}

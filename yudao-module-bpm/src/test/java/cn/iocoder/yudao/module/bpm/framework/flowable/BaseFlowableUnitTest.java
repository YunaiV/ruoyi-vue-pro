package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.redis.BpmProcessIdRedisDAO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.config.BpmFlowableConfiguration;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmProcessInstanceEventListener;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmTaskEventListener;
import cn.iocoder.yudao.module.bpm.service.comment.BpmCommentService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmFormService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceCopyService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.listener.BpmCallActivityListener;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.flowable.spring.boot.ProcessEngineServicesAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 依赖 Flowable 引擎的单元测试
 *
 * <p>使用 H2 数据源，Flowable 表由测试环境的 Flowable 自动配置创建；需要业务表的测试自行导入对应 SQL。</p>
 * <p>加载真实的 {@link BpmTaskServiceImpl}、{@link BpmProcessInstanceServiceImpl} 及 Flowable 事件监听器，
 * 保证事务代理、getSelf() 自调用、任务/流程实例状态回调与生产一致；其它模块、DB 相关的 Service 统一 Mock。</p>
 * <p>注意：Mock 统一声明在基类，保证子类共享同一个 Spring 上下文（同一个 Flowable 引擎），子类不要再额外声明 {@link MockitoBean}。</p>
 *
 * @author HUIHUI
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = BaseFlowableUnitTest.Application.class)
@ActiveProfiles("unit-test")
@TestPropertySource(properties = {
        // Flowable 8 的 H2 脚本使用旧式 identity 语法，LEGACY 模式兼容该语法；业务表脚本在需要时由测试显式导入。
        "spring.datasource.url=jdbc:h2:mem:bpm-flowable-testdb;MODE=LEGACY;DATABASE_TO_UPPER=false;NON_KEYWORDS=value",
        "spring.sql.init.mode=never",
        // 关闭异步执行器，定时器由测试显式触发，避免后台线程与测试交错执行
        "flowable.async-executor-activate=false"
})
public abstract class BaseFlowableUnitTest {

    @MockitoBean
    protected BpmProcessDefinitionService processDefinitionService;
    @MockitoBean
    protected BpmModelService modelService;
    @MockitoBean
    protected BpmCommentService commentService;
    @MockitoBean
    protected BpmMessageService messageService;
    @MockitoBean
    protected BpmFormService formService;
    @MockitoBean
    protected BpmProcessInstanceCopyService processInstanceCopyService;
    @MockitoBean
    protected BpmProcessIdRedisDAO processIdRedisDAO;
    @MockitoBean
    protected AdminUserApi adminUserApi;
    @MockitoBean
    protected DeptApi deptApi;

    @Import({BaseDbUnitTest.Application.class, BpmFlowableConfiguration.class,
            BpmTaskServiceImpl.class, BpmProcessInstanceServiceImpl.class,
            BpmTaskEventListener.class, BpmProcessInstanceEventListener.class})
    @ImportAutoConfiguration({ProcessEngineAutoConfiguration.class, ProcessEngineServicesAutoConfiguration.class,
            TransactionAutoConfiguration.class})
    public static class Application {

        @Bean("testBpmTaskCandidateInvoker")
        @Primary
        public BpmTaskCandidateInvoker bpmTaskCandidateInvoker() {
            BpmTaskCandidateInvoker invoker = mock(BpmTaskCandidateInvoker.class);
            when(invoker.calculateUsersByTask(any())).thenReturn(Set.of(1L));
            return invoker;
        }

        /**
         * 子流程发起人监听器：BPMN 通过 ${bpmCallActivityListener} 引用，需与组件扫描的 Bean 名称一致
         */
        @Bean("bpmCallActivityListener")
        public BpmCallActivityListener bpmCallActivityListener() {
            return new BpmCallActivityListener();
        }
    }

    /**
     * 审批人计算的 Mock，属于上下文单例（非 {@link MockitoBean}），每个测试前重置，避免测试间相互影响
     */
    @Resource(name = "testBpmTaskCandidateInvoker")
    protected BpmTaskCandidateInvoker taskCandidateInvoker;

    @BeforeEach
    public void resetTaskCandidateInvoker() {
        // 默认：任务的审批人为用户 1
        reset(taskCandidateInvoker);
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of(1L));
    }

    /**
     * 将流程定义、模型、用户相关的 Mock 委托给真实 Flowable 引擎，模拟业务表中已有对应数据
     *
     * <p>流程定义信息默认为允许撤回的 BPMN 流程；用户编号为 id 时，昵称为「用户 + id」。</p>
     */
    @SuppressWarnings("unchecked")
    protected void mockFlowableBackedServices(RepositoryService repositoryService) {
        when(processDefinitionService.getActiveProcessDefinition(anyString())).thenAnswer(invocation ->
                repositoryService.createProcessDefinitionQuery().processDefinitionKey(invocation.getArgument(0))
                        .latestVersion().singleResult());
        when(processDefinitionService.getProcessDefinition(anyString())).thenAnswer(invocation ->
                repositoryService.createProcessDefinitionQuery().processDefinitionId(invocation.getArgument(0))
                        .singleResult());
        when(processDefinitionService.getProcessDefinitionBpmnModel(anyString())).thenAnswer(invocation ->
                repositoryService.getBpmnModel(invocation.getArgument(0)));
        when(processDefinitionService.getProcessDefinitionInfo(anyString())).thenAnswer(invocation ->
                new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                        .setModelType(BpmModelTypeEnum.BPMN.getType()).setAllowWithdrawTask(true));
        when(processDefinitionService.canUserStartProcessDefinition(any(), any())).thenReturn(true);
        when(modelService.getBpmnModelByDefinitionId(anyString())).thenAnswer(invocation ->
                repositoryService.getBpmnModel(invocation.getArgument(0)));
        when(adminUserApi.getUser(any())).thenAnswer(invocation -> buildUser(invocation.getArgument(0)));
        when(adminUserApi.getUserList(anyCollection())).thenAnswer(invocation ->
                ((Collection<Long>) invocation.getArgument(0)).stream().map(BaseFlowableUnitTest::buildUser).toList());
        when(adminUserApi.getUserMap(anyCollection())).thenAnswer(invocation ->
                ((Collection<Long>) invocation.getArgument(0)).stream()
                        .collect(Collectors.toMap(Function.identity(), BaseFlowableUnitTest::buildUser)));
    }

    private static AdminUserRespDTO buildUser(Long id) {
        return new AdminUserRespDTO().setId(id).setNickname("用户" + id);
    }

}

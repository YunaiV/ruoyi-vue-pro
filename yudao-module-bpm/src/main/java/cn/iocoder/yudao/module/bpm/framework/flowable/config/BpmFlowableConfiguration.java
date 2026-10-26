package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.FlowableFunctionDelegate;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class BpmFlowableConfiguration {

    /**
     * 参考 {@link org.flowable.spring.boot.FlowableJobConfiguration} 类，创建对应的 AsyncListenableTaskExecutor Bean
     * <p>
     * 如果不创建，会导致项目启动时，Flowable 报错的问题
     */
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("flowable-task-Executor-");
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }

    /**
     * BPM 模块的 ProcessEngineConfigurationConfigurer 实现类：
     *
     * 1. 设置各种监听器
     * 2. 设置自定义的 ActivityBehaviorFactory 实现
     * 3. 设置华为 GaussDB、openGauss（含 MogDB）、神通的数据库类型
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> bpmProcessEngineConfigurationConfigurer(
            ObjectProvider<FlowableEventListener> listeners,
            ObjectProvider<FlowableFunctionDelegate> customFlowableFunctionDelegates,
            BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return configuration -> {
            // 注册监听器，例如说 BpmActivityEventListener
            configuration.setEventListeners(ListUtil.toList(listeners.iterator()));
            // 设置 ActivityBehaviorFactory 实现类，用于流程任务的审核人的自定义
            configuration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
            // 设置自定义的函数
            configuration.setCustomFlowableFunctionDelegates(ListUtil.toList(customFlowableFunctionDelegates.stream().iterator()));
            // 设置华为 GaussDB、openGauss（含 MogDB）、神通的数据库类型
            initDatabaseType(configuration);
        };
    }

    /**
     * 以下原生驱动返回的 ProductName，Flowable 无法识别，启动时会报错：
     * 1. 华为 GaussDB 的原生驱动（jdbc:gaussdb:），返回 GaussDB
     * 2. org.opengauss 的 openGauss JDBC 驱动（jdbc:opengauss:），返回 openGauss，连接 MogDB 时也是如此
     * 3. 神通的 JDBC 驱动（jdbc:oscar:），返回 OSCAR
     *
     * 因此，在未显式设置 databaseType 时：GaussDB、openGauss 按 PostgreSQL 处理（需要使用 PG 兼容模式创建的数据库）；
     * 神通按 Oracle 处理，使用 BLOB、CLOB 字段及大写的表名。
     * 只读取 Flowable 自身数据源的元数据，其它数据库不做处理，仍由 Flowable 自动识别
     *
     * @param configuration Flowable 配置
     */
    private static void initDatabaseType(SpringProcessEngineConfiguration configuration) {
        if (configuration.getDatabaseType() != null || configuration.getDataSource() == null) {
            return;
        }
        try (Connection connection = configuration.getDataSource().getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            if (ObjectUtils.equalsAny(productName,
                    DbTypeEnum.GAUSS_DB.getProductName(), DbTypeEnum.OPENGAUSS.getProductName())) {
                configuration.setDatabaseType(AbstractEngineConfiguration.DATABASE_TYPE_POSTGRES);
            } else if (DbTypeEnum.OSCAR.getProductName().equals(productName)) {
                configuration.setDatabaseType(AbstractEngineConfiguration.DATABASE_TYPE_ORACLE);
            }
        } catch (SQLException ex) {
            // 获取失败时，不做处理，由 Flowable 自动识别时报错
            log.warn("[initDatabaseType][获取数据库类型失败]", ex);
        }
    }

    // =========== 审批人相关的 Bean ==========

    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskCandidateInvoker bpmTaskCandidateInvoker) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setTaskCandidateInvoker(bpmTaskCandidateInvoker);
        return bpmActivityBehaviorFactory;
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // adminUserApi 可以注入成功
    public BpmTaskCandidateInvoker bpmTaskCandidateInvoker(List<BpmTaskCandidateStrategy> strategyList,
                                                           AdminUserApi adminUserApi) {
        return new BpmTaskCandidateInvoker(strategyList, adminUserApi);
    }

    // =========== 自己拓展的 Bean ==========

    @Bean
    public BpmProcessInstanceEventPublisher processInstanceEventPublisher(ApplicationEventPublisher publisher) {
        return new BpmProcessInstanceEventPublisher(publisher);
    }

}
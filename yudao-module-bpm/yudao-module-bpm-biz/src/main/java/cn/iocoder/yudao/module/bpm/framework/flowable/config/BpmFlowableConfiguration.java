package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration
public class BpmFlowableConfiguration {

    /**
     * BPM 模块的 ProcessEngineConfigurationConfigurer 实现类：
     *
     * 1. 设置各种监听器
     * 2. 设置自定义的 ActivityBehaviorFactory 实现
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> bpmProcessEngineConfigurationConfigurer(
            ObjectProvider<FlowableEventListener> listeners,
            BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return configuration -> {
            // 注册监听器，例如说 BpmActivityEventListener
            configuration.setEventListeners(ListUtil.toList(listeners.iterator()));
            // 设置 ActivityBehaviorFactory 实现类，用于流程任务的审核人的自定义
            configuration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
        };
    }

    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskAssignRuleService taskRuleService) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setBpmTaskRuleService(taskRuleService);
        return bpmActivityBehaviorFactory;
    }

}

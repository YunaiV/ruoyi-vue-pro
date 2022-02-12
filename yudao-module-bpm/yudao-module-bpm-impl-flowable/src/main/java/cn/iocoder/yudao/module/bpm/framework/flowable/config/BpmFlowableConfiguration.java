package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmProcessInstanceEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration
public class BpmFlowableConfiguration {

    /**
     *将自定义 listener 加入全局listener
     * @param processInstanceListener 自定义监听 {@link ProcessInstance}
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> addCustomerListenerConfigurer (BpmProcessInstanceEventListener processInstanceListener) {
        return engineConfiguration -> {
            List<FlowableEventListener> eventListeners = new ArrayList<>();
            eventListeners.add(processInstanceListener);
            engineConfiguration.setEventListeners(eventListeners);
        };
    }
}

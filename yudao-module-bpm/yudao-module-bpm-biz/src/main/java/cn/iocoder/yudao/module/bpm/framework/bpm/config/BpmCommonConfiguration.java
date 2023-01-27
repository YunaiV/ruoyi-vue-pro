package cn.iocoder.yudao.module.bpm.framework.bpm.config;

import cn.iocoder.yudao.module.bpm.framework.bpm.core.event.BpmProcessInstanceResultEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BPM 通用的 Configuration 配置类，提供给 Activiti 和 Flowable
 */
@Configuration(proxyBeanMethods = false)
public class BpmCommonConfiguration {

    @Bean
    public BpmProcessInstanceResultEventPublisher processInstanceResultEventPublisher(ApplicationEventPublisher publisher) {
        return new BpmProcessInstanceResultEventPublisher(publisher);
    }

}

package cn.iocoder.yudao.module.iot.messagebus.config;

import cn.iocoder.yudao.module.iot.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.messagebus.core.local.LocalIotMessageBus;
import cn.iocoder.yudao.module.iot.messagebus.core.rocketmq.RocketMQIotMessageBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 消息总线自动配置
 *
 * @author 芋道源码
 */
@AutoConfiguration
@EnableConfigurationProperties(IotMessageBusProperties.class)
@Slf4j
public class IotMessageBusAutoConfiguration {

    // ==================== Local 实现 ====================

    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.message-bus", name = "type", havingValue = "local", matchIfMissing = true)
    public static class LocalIotMessageBusConfiguration {

        @Bean
        public IotMessageBus localIotMessageBus(ApplicationContext applicationContext) {
            log.info("[localIotMessageBus][创建 Local IoT 消息总线]");
            return new LocalIotMessageBus(applicationContext);
        }

    }

    // ==================== RocketMQ 实现 ====================

    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.message-bus", name = "type", havingValue = "rocketmq")
    @ConditionalOnClass(RocketMQTemplate.class)
    public static class RocketMQIotMessageBusConfiguration {

        @Bean
        public IotMessageBus rocketMQIotMessageBus(RocketMQProperties rocketMQProperties, RocketMQTemplate rocketMQTemplate) {
            log.info("[rocketMQIotMessageBus][创建 RocketMQ IoT 消息总线]");
            return new RocketMQIotMessageBus(rocketMQProperties, rocketMQTemplate);
        }

    }

}
package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IotGatewayProperties.class)
@Slf4j
public class IotGatewayConfiguration {

    /**
     * IoT 网关 HTTP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.http", name = "enabled", havingValue = "true")
    @Slf4j
    public static class HttpProtocolConfiguration {

        @Bean
        public IotHttpUpstreamProtocol iotHttpUpstreamProtocol(IotGatewayProperties gatewayProperties) {
            return new IotHttpUpstreamProtocol(gatewayProperties.getProtocol().getHttp());
        }

        @Bean
        public IotHttpDownstreamSubscriber iotHttpDownstreamSubscriber(IotHttpUpstreamProtocol httpUpstreamProtocol,
                                                                       IotMessageBus messageBus) {
            return new IotHttpDownstreamSubscriber(httpUpstreamProtocol, messageBus);
        }
    }

    /**
     * IoT 网关 MQTT 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.emqx", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttProtocolConfiguration {

        // TODO @haohao：临时注释，避免报错
//        @Bean
//        public IotMqttUpstreamProtocol iotMqttUpstreamProtocol(IotGatewayProperties gatewayProperties) {
//            return new IotMqttUpstreamProtocol(gatewayProperties.getProtocol().getEmqx());
//        }
//
//        @Bean
//        public IotMqttDownstreamSubscriber iotMqttDownstreamSubscriber(IotMqttUpstreamProtocol mqttUpstreamProtocol,
//                                                                       IotMessageBus messageBus) {
//            return new IotMqttDownstreamSubscriber(mqttUpstreamProtocol, messageBus);
//        }
    }

}

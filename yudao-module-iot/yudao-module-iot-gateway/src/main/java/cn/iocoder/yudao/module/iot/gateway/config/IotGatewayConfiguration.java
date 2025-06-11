package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttHttpAuthProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
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

        @Bean
        public IotMqttUpstreamProtocol iotMqttUpstreamProtocol(IotGatewayProperties gatewayProperties) {
            return new IotMqttUpstreamProtocol(gatewayProperties.getProtocol().getEmqx());
        }

        @Bean
        public IotMqttDownstreamSubscriber iotMqttDownstreamSubscriber(IotMqttUpstreamProtocol mqttUpstreamProtocol,
                                                                       IotMessageBus messageBus) {
            return new IotMqttDownstreamSubscriber(mqttUpstreamProtocol, messageBus);
        }

        /**
         * MQTT HTTP 认证协议：提供 HTTP 认证接口供 EMQX 调用
         */
        @Bean
        public IotMqttHttpAuthProtocol mqttHttpAuthProtocol(IotGatewayProperties gatewayProperties) {
            return new IotMqttHttpAuthProtocol(gatewayProperties.getProtocol().getEmqx());
        }
    }

}

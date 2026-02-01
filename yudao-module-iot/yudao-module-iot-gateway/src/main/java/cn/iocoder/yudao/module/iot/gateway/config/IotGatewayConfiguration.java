package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxAuthEventProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.router.IotWebSocketDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(IotGatewayProperties.class)
@Slf4j
public class IotGatewayConfiguration {

    @Bean
    public IotMessageSerializerManager iotMessageSerializerManager() {
        return new IotMessageSerializerManager();
    }

    @Bean
    public IotProtocolManager iotProtocolManager(IotGatewayProperties gatewayProperties) {
        return new IotProtocolManager(gatewayProperties);
    }

    /**
     * IoT 网关 EMQX 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.emqx", name = "enabled", havingValue = "true")
    @Slf4j
    public static class EmqxProtocolConfiguration {

        @Bean(name = "emqxVertx", destroyMethod = "close")
        public Vertx emqxVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotEmqxAuthEventProtocol iotEmqxAuthEventProtocol(IotGatewayProperties gatewayProperties,
                                                                 @Qualifier("emqxVertx") Vertx emqxVertx) {
            return new IotEmqxAuthEventProtocol(gatewayProperties.getProtocol().getEmqx(), emqxVertx);
        }

        @Bean
        public IotEmqxUpstreamProtocol iotEmqxUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                               @Qualifier("emqxVertx") Vertx emqxVertx) {
            return new IotEmqxUpstreamProtocol(gatewayProperties.getProtocol().getEmqx(), emqxVertx);
        }

        @Bean
        public IotEmqxDownstreamSubscriber iotEmqxDownstreamSubscriber(IotEmqxUpstreamProtocol mqttUpstreamProtocol,
                IotMessageBus messageBus) {
            return new IotEmqxDownstreamSubscriber(mqttUpstreamProtocol, messageBus);
        }
    }

    /**
     * IoT 网关 MQTT 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.mqtt", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttProtocolConfiguration {

        @Bean(name = "mqttVertx", destroyMethod = "close")
        public Vertx mqttVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotMqttUpstreamProtocol iotMqttUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                               IotDeviceMessageService messageService,
                                                               IotMqttConnectionManager connectionManager,
                                                               @Qualifier("mqttVertx") Vertx mqttVertx) {
            return new IotMqttUpstreamProtocol(gatewayProperties.getProtocol().getMqtt(), messageService,
                    connectionManager, mqttVertx);
        }

        @Bean
        public IotMqttDownstreamHandler iotMqttDownstreamHandler(IotDeviceMessageService messageService,
                                                                 IotMqttConnectionManager connectionManager) {
            return new IotMqttDownstreamHandler(messageService, connectionManager);
        }

        @Bean
        public IotMqttDownstreamSubscriber iotMqttDownstreamSubscriber(IotMqttUpstreamProtocol mqttUpstreamProtocol,
                                                                       IotMqttDownstreamHandler downstreamHandler,
                                                                       IotMessageBus messageBus) {
            return new IotMqttDownstreamSubscriber(mqttUpstreamProtocol, downstreamHandler, messageBus);
        }

    }

    /**
     * IoT 网关 CoAP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.coap", name = "enabled", havingValue = "true")
    @Slf4j
    public static class CoapProtocolConfiguration {

        @Bean
        public IotCoapUpstreamProtocol iotCoapUpstreamProtocol(IotGatewayProperties gatewayProperties) {
            return new IotCoapUpstreamProtocol(gatewayProperties.getProtocol().getCoap());
        }

        @Bean
        public IotCoapDownstreamSubscriber iotCoapDownstreamSubscriber(IotCoapUpstreamProtocol coapUpstreamProtocol,
                                                                       IotMessageBus messageBus) {
            return new IotCoapDownstreamSubscriber(coapUpstreamProtocol, messageBus);
        }

    }

    /**
     * IoT 网关 WebSocket 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.websocket", name = "enabled", havingValue = "true")
    @Slf4j
    public static class WebSocketProtocolConfiguration {

        @Bean(name = "websocketVertx", destroyMethod = "close")
        public Vertx websocketVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotWebSocketUpstreamProtocol iotWebSocketUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                                         IotDeviceService deviceService,
                                                                         IotDeviceMessageService messageService,
                                                                         IotWebSocketConnectionManager connectionManager,
                                                                         @Qualifier("websocketVertx") Vertx websocketVertx) {
            return new IotWebSocketUpstreamProtocol(gatewayProperties.getProtocol().getWebsocket(),
                    deviceService, messageService, connectionManager, websocketVertx);
        }

        @Bean
        public IotWebSocketDownstreamHandler iotWebSocketDownstreamHandler(IotDeviceMessageService messageService,
                                                                           IotWebSocketConnectionManager connectionManager) {
            return new IotWebSocketDownstreamHandler(messageService, connectionManager);
        }

        @Bean
        public IotWebSocketDownstreamSubscriber iotWebSocketDownstreamSubscriber(IotWebSocketUpstreamProtocol protocolHandler,
                                                                                 IotWebSocketDownstreamHandler downstreamHandler,
                                                                                 IotMessageBus messageBus) {
            return new IotWebSocketDownstreamSubscriber(protocolHandler, downstreamHandler, messageBus);
        }

    }

}

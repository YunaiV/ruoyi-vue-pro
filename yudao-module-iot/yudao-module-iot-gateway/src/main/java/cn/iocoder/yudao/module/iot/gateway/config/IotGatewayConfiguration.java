package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxAuthEventProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.router.IotUdpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.router.IotWebSocketDownstreamHandler;
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
    public IotProtocolManager iotProtocolManager(IotGatewayProperties gatewayProperties,
                                                 IotMessageSerializerManager serializerManager,
                                                 IotMessageBus messageBus) {
        return new IotProtocolManager(gatewayProperties, serializerManager, messageBus);
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
     * IoT 网关 TCP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.tcp", name = "enabled", havingValue = "true")
    @Slf4j
    public static class TcpProtocolConfiguration {

        @Bean(name = "tcpVertx", destroyMethod = "close")
        public Vertx tcpVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotTcpUpstreamProtocol iotTcpUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                             IotDeviceService deviceService,
                                                             IotDeviceMessageService messageService,
                                                             IotTcpConnectionManager connectionManager,
                                                             @Qualifier("tcpVertx") Vertx tcpVertx) {
            return new IotTcpUpstreamProtocol(gatewayProperties.getProtocol().getTcp(),
                    deviceService, messageService, connectionManager, tcpVertx);
        }

        @Bean
        public IotTcpDownstreamHandler iotTcpDownstreamHandler(IotDeviceMessageService messageService,
                                                               IotTcpConnectionManager connectionManager) {
            return new IotTcpDownstreamHandler(messageService, connectionManager);
        }

        @Bean
        public IotTcpDownstreamSubscriber iotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocolHandler,
                                                                     IotTcpDownstreamHandler downstreamHandler,
                                                                     IotMessageBus messageBus) {
            return new IotTcpDownstreamSubscriber(protocolHandler, downstreamHandler, messageBus);
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
     * IoT 网关 UDP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.udp", name = "enabled", havingValue = "true")
    @Slf4j
    public static class UdpProtocolConfiguration {

        @Bean(name = "udpVertx", destroyMethod = "close")
        public Vertx udpVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotUdpUpstreamProtocol iotUdpUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                             IotDeviceService deviceService,
                                                             IotDeviceMessageService messageService,
                                                             IotUdpSessionManager sessionManager,
                                                             @Qualifier("udpVertx") Vertx udpVertx) {
            return new IotUdpUpstreamProtocol(gatewayProperties.getProtocol().getUdp(),
                    deviceService, messageService, sessionManager, udpVertx);
        }

        @Bean
        public IotUdpDownstreamHandler iotUdpDownstreamHandler(IotDeviceMessageService messageService,
                                                               IotUdpSessionManager sessionManager,
                                                               IotUdpUpstreamProtocol protocol) {
            return new IotUdpDownstreamHandler(messageService, sessionManager, protocol);
        }

        @Bean
        public IotUdpDownstreamSubscriber iotUdpDownstreamSubscriber(IotUdpUpstreamProtocol protocolHandler,
                                                                     IotUdpDownstreamHandler downstreamHandler,
                                                                     IotMessageBus messageBus) {
            return new IotUdpDownstreamSubscriber(protocolHandler, downstreamHandler, messageBus);
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

package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxAuthEventProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.IotMqttWsDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.IotMqttWsUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.manager.IotMqttWsConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.router.IotMqttWsDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
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
        public IotTcpDownstreamSubscriber iotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocolHandler,
                                                                     IotDeviceMessageService messageService,
                                                                     IotDeviceService deviceService,
                                                                     IotTcpConnectionManager connectionManager,
                                                                     IotMessageBus messageBus) {
            return new IotTcpDownstreamSubscriber(protocolHandler, messageService, deviceService, connectionManager,
                    messageBus);
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
     * IoT 网关 MQTT WebSocket 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.mqtt-ws", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttWsProtocolConfiguration {

        @Bean(name = "mqttWsVertx", destroyMethod = "close")
        public Vertx mqttWsVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotMqttWsUpstreamProtocol iotMqttWsUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                                   IotDeviceMessageService messageService,
                                                                   IotMqttWsConnectionManager connectionManager,
                                                                   @Qualifier("mqttWsVertx") Vertx mqttWsVertx) {
            return new IotMqttWsUpstreamProtocol(gatewayProperties.getProtocol().getMqttWs(),
                    messageService, connectionManager, mqttWsVertx);
        }

        @Bean
        public IotMqttWsDownstreamHandler iotMqttWsDownstreamHandler(IotDeviceMessageService messageService,
                                                                     IotDeviceService deviceService,
                                                                     IotMqttWsConnectionManager connectionManager) {
            return new IotMqttWsDownstreamHandler(messageService, deviceService, connectionManager);
        }

        @Bean
        public IotMqttWsDownstreamSubscriber iotMqttWsDownstreamSubscriber(IotMqttWsUpstreamProtocol mqttWsUpstreamProtocol,
                                                                           IotMqttWsDownstreamHandler downstreamHandler,
                                                                           IotMessageBus messageBus) {
            return new IotMqttWsDownstreamSubscriber(mqttWsUpstreamProtocol, downstreamHandler, messageBus);
        }

    }

}

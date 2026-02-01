package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxAuthEventProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.*;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.client.IotModbusTcpClient;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.codec.IotModbusDataConverter;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.manager.IotModbusTcpConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.manager.IotModbusTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.manager.IotModbusTcpPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.router.IotModbusTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.router.IotModbusTcpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
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
     * IoT 网关 Modbus TCP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yudao.iot.gateway.protocol.modbus-tcp", name = "enabled", havingValue = "true")
    @Slf4j
    public static class ModbusTcpProtocolConfiguration {

        @Bean(name = "modbusTcpVertx", destroyMethod = "close")
        public Vertx modbusTcpVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotModbusDataConverter iotModbusDataConverter() {
            return new IotModbusDataConverter();
        }

        @Bean
        public IotModbusTcpClient iotModbusTcpClient() {
            return new IotModbusTcpClient();
        }

        @Bean
        public IotModbusTcpConnectionManager iotModbusTcpConnectionManager(
                RedissonClient redissonClient,
                @Qualifier("modbusTcpVertx") Vertx modbusTcpVertx) {
            return new IotModbusTcpConnectionManager(redissonClient, modbusTcpVertx);
        }

        @Bean
        public IotModbusTcpConfigCacheService iotModbusTcpConfigCacheService(
                cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi deviceApi) {
            return new IotModbusTcpConfigCacheService(deviceApi);
        }

        @Bean
        public IotModbusTcpUpstreamHandler iotModbusTcpUpstreamHandler(
                IotDeviceMessageService messageService,
                IotModbusDataConverter dataConverter) {
            return new IotModbusTcpUpstreamHandler(messageService, dataConverter);
        }

        @Bean
        public IotModbusTcpPollScheduler iotModbusTcpPollScheduler(
                @Qualifier("modbusTcpVertx") Vertx modbusTcpVertx,
                IotModbusTcpConnectionManager connectionManager,
                IotModbusTcpClient modbusClient,
                IotModbusTcpUpstreamHandler upstreamHandler) {
            return new IotModbusTcpPollScheduler(modbusTcpVertx, connectionManager, modbusClient, upstreamHandler);
        }

        @Bean
        public IotModbusTcpDownstreamHandler iotModbusTcpDownstreamHandler(
                IotModbusTcpConnectionManager connectionManager,
                IotModbusTcpClient modbusClient,
                IotModbusDataConverter dataConverter,
                IotModbusTcpConfigCacheService configCacheService) {
            return new IotModbusTcpDownstreamHandler(connectionManager, modbusClient, dataConverter, configCacheService);
        }

        @Bean
        public IotModbusTcpUpstreamProtocol iotModbusTcpUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                                         IotDeviceMessageService messageService,
                                                                         IotModbusTcpConnectionManager connectionManager,
                                                                         IotModbusTcpPollScheduler pollScheduler,
                                                                         IotModbusTcpConfigCacheService configCacheService,
                                                                         IotModbusTcpUpstreamHandler upstreamHandler,
                                                                         @Qualifier("modbusTcpVertx") Vertx modbusTcpVertx) {
            return new IotModbusTcpUpstreamProtocol(gatewayProperties.getProtocol().getModbusTcp(),
                    messageService, connectionManager, pollScheduler, configCacheService, upstreamHandler, modbusTcpVertx);
        }

        @Bean
        public IotModbusTcpDownstreamSubscriber iotModbusTcpDownstreamSubscriber(IotModbusTcpUpstreamProtocol upstreamProtocol,
                                                                                  IotModbusTcpDownstreamHandler downstreamHandler,
                                                                                  IotMessageBus messageBus) {
            return new IotModbusTcpDownstreamSubscriber(upstreamProtocol, downstreamHandler, messageBus);
        }

    }

}

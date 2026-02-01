package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolInstanceProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttRegisterHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IoT 网关 MQTT 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttProtocol implements IotProtocol {

    /**
     * 协议配置
     */
    private final ProtocolInstanceProperties properties;
    /**
     * 服务器 ID（用于消息追踪，全局唯一）
     */
    @Getter
    private final String serverId;

    /**
     * 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * Vert.x 实例
     */
    private Vertx vertx;
    /**
     * MQTT 服务器
     */
    private MqttServer mqttServer;

    /**
     * 连接管理器
     */
    private final IotMqttConnectionManager connectionManager;
    /**
     * 下行消息订阅者
     */
    private final IotMqttDownstreamSubscriber downstreamSubscriber;

    private final IotDeviceMessageService deviceMessageService;

    private final IotMqttAuthHandler authHandler;
    private final IotMqttRegisterHandler registerHandler;
    private final IotMqttUpstreamHandler upstreamHandler;

    public IotMqttProtocol(ProtocolInstanceProperties properties) {
        IotMqttConfig mqttConfig = properties.getMqtt();
        Assert.notNull(mqttConfig, "MQTT 协议配置（mqtt）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化连接管理器
        this.connectionManager = new IotMqttConnectionManager();

        // 初始化 Handler
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.authHandler = new IotMqttAuthHandler(connectionManager, deviceMessageService, deviceApi, serverId);
        this.registerHandler = new IotMqttRegisterHandler(connectionManager, deviceMessageService, deviceApi);
        this.upstreamHandler = new IotMqttUpstreamHandler(connectionManager, deviceMessageService, serverId);

        // 初始化下行消息订阅者
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        IotMqttDownstreamHandler downstreamHandler = new IotMqttDownstreamHandler(deviceMessageService, connectionManager);
        this.downstreamSubscriber = new IotMqttDownstreamSubscriber(this, downstreamHandler, messageBus);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MQTT;
    }

    // done @AI：这个方法的整体注释风格，参考 IotTcpProtocol 的 start 方法。
    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT MQTT 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 1.2 创建服务器选项
        IotMqttConfig mqttConfig = properties.getMqtt();
        MqttServerOptions options = new MqttServerOptions()
                .setPort(properties.getPort())
                .setMaxMessageSize(mqttConfig.getMaxMessageSize())
                .setTimeoutOnConnect(mqttConfig.getConnectTimeoutSeconds());
        if (Boolean.TRUE.equals(mqttConfig.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(mqttConfig.getSslKeyPath())
                    .setCertPath(mqttConfig.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 1.3 创建服务器并设置连接处理器
        mqttServer = MqttServer.create(vertx, options);
        mqttServer.endpointHandler(this::handleEndpoint);

        // 1.4 启动 MQTT 服务器
        try {
            mqttServer.listen().result();
            running = true;
            log.info("[start][IoT MQTT 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 2. 启动下行消息订阅者
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT MQTT 协议 {} 启动失败]", getId(), e);
            // 启动失败时关闭资源
            if (mqttServer != null) {
                mqttServer.close();
                mqttServer = null;
            }
            if (vertx != null) {
                vertx.close();
                vertx = null;
            }
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        // 1. 停止下行消息订阅者
        try {
            downstreamSubscriber.stop();
            log.info("[stop][IoT MQTT 协议 {} 下行消息订阅者已停止]", getId());
        } catch (Exception e) {
            log.error("[stop][IoT MQTT 协议 {} 下行消息订阅者停止失败]", getId(), e);
        }

        // 2.1 关闭 MQTT 服务器
        if (mqttServer != null) {
            try {
                mqttServer.close().result();
                log.info("[stop][IoT MQTT 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT MQTT 协议 {} 服务器停止失败]", getId(), e);
            }
            mqttServer = null;
        }
        // 2.2 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT MQTT 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT MQTT 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }
        running = false;
        log.info("[stop][IoT MQTT 协议 {} 已停止]", getId());
    }

    // ======================================= MQTT 连接处理 ======================================

    /**
     * 处理 MQTT 连接端点
     *
     * @param endpoint MQTT 连接端点
     */
    private void handleEndpoint(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();

        // 1. 委托 authHandler 处理连接认证
        // done @AI：register topic 不需要注册，需要判断下；当前逻辑已支持（设备可在未认证状态发送 register 消息，registerHandler 会处理）
        if (!authHandler.handleAuthenticationRequest(endpoint)) {
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return;
        }

        // 2.1 设置异常和关闭处理器
        endpoint.exceptionHandler(ex -> {
            log.warn("[handleEndpoint][连接异常，客户端 ID: {}，地址: {}，异常: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint), ex.getMessage());
            endpoint.close();
        });
        // done @AI：closeHandler 处理底层连接关闭（网络中断、异常等），disconnectHandler 处理 MQTT DISCONNECT 报文
        endpoint.closeHandler(v -> cleanupConnection(endpoint));
        endpoint.disconnectHandler(v -> {
            log.debug("[handleEndpoint][设备断开连接，客户端 ID: {}]", clientId);
            cleanupConnection(endpoint);
        });
        // 2.2 设置心跳处理器
        endpoint.pingHandler(v -> log.debug("[handleEndpoint][收到客户端心跳，客户端 ID: {}]", clientId));

        // 3.1 设置消息处理器
        endpoint.publishHandler(message -> processMessage(endpoint, message));
        // 3.2 设置 QoS 2 消息的 PUBREL 处理器
        endpoint.publishReleaseHandler(endpoint::publishComplete);

        // 4.1 设置订阅处理器
        // done @AI：使用 CollectionUtils.convertList 简化
        endpoint.subscribeHandler(subscribe -> {
            List<String> topicNames = convertList(subscribe.topicSubscriptions(), MqttTopicSubscription::topicName);
            log.debug("[handleEndpoint][设备订阅，客户端 ID: {}，主题: {}]", clientId, topicNames);
            List<MqttQoS> grantedQoSLevels = convertList(subscribe.topicSubscriptions(), MqttTopicSubscription::qualityOfService);
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQoSLevels);
        });
        // 4.2 设置取消订阅处理器
        endpoint.unsubscribeHandler(unsubscribe -> {
            log.debug("[handleEndpoint][设备取消订阅，客户端 ID: {}，主题: {}]", clientId, unsubscribe.topics());
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });

        // 5. 接受连接
        endpoint.accept(false);
    }

    /**
     * 处理消息（发布）
     *
     * @param endpoint MQTT 连接端点
     * @param message  发布消息
     */
    private void processMessage(MqttEndpoint endpoint, MqttPublishMessage message) {
        String clientId = endpoint.clientIdentifier();
        try {
            // 根据 topic 分发到不同 handler
            String topic = message.topicName();
            byte[] payload = message.payload().getBytes();
            if (registerHandler.isRegisterMessage(topic)) {
                registerHandler.handleRegister(endpoint, topic, payload);
            } else {
                upstreamHandler.handleBusinessRequest(endpoint, topic, payload);
            }

            // 根据 QoS 级别发送相应的确认消息
            handleQoSAck(endpoint, message);
        } catch (Exception e) {
            log.error("[processMessage][消息处理失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint), e.getMessage());
            cleanupConnection(endpoint);
            endpoint.close();
        }
    }

    /**
     * 处理 QoS 确认
     *
     * @param endpoint MQTT 连接端点
     * @param message  发布消息
     */
    private void handleQoSAck(MqttEndpoint endpoint, MqttPublishMessage message) {
        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            // QoS 1: 发送 PUBACK 确认
            endpoint.publishAcknowledge(message.messageId());
        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            // QoS 2: 发送 PUBREC 确认
            endpoint.publishReceived(message.messageId());
        }
        // QoS 0 无需确认
    }

    /**
     * 清理连接
     *
     * @param endpoint MQTT 连接端点
     */
    private void cleanupConnection(MqttEndpoint endpoint) {
        try {
            // 1. 发送设备离线消息
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(endpoint);
            if (connectionInfo != null) {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                deviceMessageService.sendDeviceMessage(offlineMessage, connectionInfo.getProductKey(),
                        connectionInfo.getDeviceName(), serverId);
            }

            // 2. 注销连接
            connectionManager.unregisterConnection(endpoint);
        } catch (Exception e) {
            log.error("[cleanupConnection][清理连接失败，客户端 ID: {}，错误: {}]",
                    endpoint.clientIdentifier(), e.getMessage());
        }
    }

}

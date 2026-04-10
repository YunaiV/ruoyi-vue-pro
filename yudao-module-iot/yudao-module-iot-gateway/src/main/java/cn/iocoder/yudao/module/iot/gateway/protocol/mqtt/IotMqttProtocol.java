package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttRegisterHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
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

import java.util.ArrayList;
import java.util.List;

/**
 * IoT 网关 MQTT 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttProtocol implements IotProtocol {

    /**
     * 注册连接的 clientId 标识
     *
     * @see #handleEndpoint(MqttEndpoint)
     */
    private static final String AUTH_TYPE_REGISTER = "|authType=register|";

    /**
     * 协议配置
     */
    private final ProtocolProperties properties;
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
    private IotMqttDownstreamSubscriber downstreamSubscriber;

    private final IotDeviceMessageService deviceMessageService;

    private final IotMqttAuthHandler authHandler;
    private final IotMqttRegisterHandler registerHandler;
    private final IotMqttUpstreamHandler upstreamHandler;

    public IotMqttProtocol(ProtocolProperties properties) {
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
        this.registerHandler = new IotMqttRegisterHandler(connectionManager, deviceMessageService);
        this.upstreamHandler = new IotMqttUpstreamHandler(connectionManager, deviceMessageService, serverId);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MQTT;
    }

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
        IotGatewayProperties.SslConfig sslConfig = properties.getSsl();
        if (sslConfig != null && Boolean.TRUE.equals(sslConfig.getSsl())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(sslConfig.getSslKeyPath())
                    .setCertPath(sslConfig.getSslCertPath());
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
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            IotMqttDownstreamHandler downstreamHandler = new IotMqttDownstreamHandler(deviceMessageService, connectionManager);
            this.downstreamSubscriber = new IotMqttDownstreamSubscriber(this, downstreamHandler, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT MQTT 协议 {} 启动失败]", getId(), e);
            stop0();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        // 1. 停止下行消息订阅者
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
                log.info("[stop][IoT MQTT 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT MQTT 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 关闭所有连接
        connectionManager.closeAll();
        // 2.2 关闭 MQTT 服务器
        if (mqttServer != null) {
            try {
                mqttServer.close().result();
                log.info("[stop][IoT MQTT 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT MQTT 协议 {} 服务器停止失败]", getId(), e);
            }
            mqttServer = null;
        }
        // 2.3 关闭 Vertx 实例
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
        // 1. 如果是注册请求，注册待认证连接；否则走正常认证流程
        String clientId = endpoint.clientIdentifier();
        if (StrUtil.endWith(clientId, AUTH_TYPE_REGISTER)) {
            // 情况一：设备注册请求
            registerHandler.handleRegister(endpoint);
            return;
        } else {
            // 情况二：普通认证请求
            if (!authHandler.handleAuthenticationRequest(endpoint)) {
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
                return;
            }
        }

        // 2.1 设置异常和关闭处理器
        endpoint.exceptionHandler(ex -> {
            log.warn("[handleEndpoint][连接异常，客户端 ID: {}，地址: {}，异常: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint), ex.getMessage());
            endpoint.close();
        });
        endpoint.closeHandler(v -> cleanupConnection(endpoint)); // 处理底层连接关闭（网络中断、异常等）
        endpoint.disconnectHandler(v -> { // 处理 MQTT DISCONNECT 报文
            log.debug("[handleEndpoint][设备断开连接，客户端 ID: {}]", clientId);
            cleanupConnection(endpoint);
        });
        // 2.2 设置心跳处理器
        endpoint.pingHandler(v -> log.debug("[handleEndpoint][收到客户端心跳，客户端 ID: {}]", clientId));

        // 3.1 设置消息处理器
        endpoint.publishHandler(message -> processMessage(endpoint, message));
        // 3.2 设置 QoS 2 消息的 PUBREL 处理器
        endpoint.publishReleaseHandler(endpoint::publishComplete);

        // 4.1 设置订阅处理器（带 ACL 校验）
        endpoint.subscribeHandler(subscribe -> {
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(endpoint);
            List<MqttQoS> grantedQoSLevels = new ArrayList<>();
            for (MqttTopicSubscription sub : subscribe.topicSubscriptions()) {
                String topicName = sub.topicName();
                // 校验主题是否属于当前设备
                if (connectionInfo != null && IotMqttTopicUtils.isTopicSubscribeAllowed(
                        topicName, connectionInfo.getProductKey(), connectionInfo.getDeviceName())) {
                    grantedQoSLevels.add(sub.qualityOfService());
                    log.debug("[handleEndpoint][订阅成功，客户端 ID: {}，主题: {}]", clientId, topicName);
                } else {
                    log.warn("[handleEndpoint][订阅被拒绝，客户端 ID: {}，主题: {}]", clientId, topicName);
                    grantedQoSLevels.add(MqttQoS.FAILURE);
                }
            }
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
            // 1. 处理业务消息
            String topic = message.topicName();
            byte[] payload = message.payload().getBytes();
            upstreamHandler.handleBusinessRequest(endpoint, topic, payload);

            // 2. 根据 QoS 级别发送相应的确认消息
            handleQoSAck(endpoint, message);
        } catch (Exception e) {
            log.error("[processMessage][消息处理失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint), e.getMessage());
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

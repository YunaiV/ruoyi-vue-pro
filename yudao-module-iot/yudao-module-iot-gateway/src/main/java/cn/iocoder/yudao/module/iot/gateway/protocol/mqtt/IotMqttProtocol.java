package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolInstanceProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream.IotMqttConnectionHandler;
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

import java.util.List;
import java.util.stream.Collectors;

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
    private IotMqttConnectionManager connectionManager;

    /**
     * 下行消息订阅者
     */
    private IotMqttDownstreamSubscriber downstreamSubscriber;

    // TODO @AI：这个是不是提前创建下？因为是无状态的。
    private IotMqttConnectionHandler connectionHandler;
    private IotMqttRegisterHandler registerHandler;
    private IotMqttUpstreamHandler upstreamHandler;

    public IotMqttProtocol(ProtocolInstanceProperties properties) {
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // TODO @AI：初始化连接器，参考 IotTcpProtocol

        // TODO @AI：初始化下行消息订阅者，参考 IotTcpProtocol
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MQTT;
    }

    // TODO @AI：这个方法的整体注释风格，参考 IotTcpProtocol 的 start 方法。
    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT MQTT 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例（每个 Protocol 独立管理）
        this.vertx = Vertx.vertx();

        // 1.2 创建连接管理器
        this.connectionManager = new IotMqttConnectionManager();

        // 1.3 初始化 Handler
        initHandlers();

        // 2. 创建服务器选项
        IotMqttConfig mqttConfig = properties.getMqtt();
        // TODO @AI：default 值，在 IotMqttConfig 处理；
        MqttServerOptions options = new MqttServerOptions()
                .setPort(properties.getPort())
                .setMaxMessageSize(mqttConfig != null ? mqttConfig.getMaxMessageSize() : 8192)
                .setTimeoutOnConnect(mqttConfig != null ? mqttConfig.getConnectTimeoutSeconds() : 60);

        // 3. 配置 SSL（如果启用）
        if (mqttConfig != null && Boolean.TRUE.equals(mqttConfig.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(mqttConfig.getSslKeyPath())
                    .setCertPath(mqttConfig.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 4. 创建服务器并设置连接处理器
        mqttServer = MqttServer.create(vertx, options);
        mqttServer.endpointHandler(this::handleEndpoint);

        // 5. 启动服务器
        try {
            mqttServer.listen().result();
            running = true;
            log.info("[start][IoT MQTT 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 6. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            IotMqttDownstreamHandler downstreamHandler = new IotMqttDownstreamHandler(
                    SpringUtil.getBean(IotDeviceMessageService.class), connectionManager);
            this.downstreamSubscriber = new IotMqttDownstreamSubscriber(this, downstreamHandler, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT MQTT 协议 {} 启动失败]", getId(), e);
            // 启动失败时关闭 Vertx
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
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
                log.info("[stop][IoT MQTT 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT MQTT 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
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

    /**
     * 初始化 Handler
     */
    private void initHandlers() {
        IotDeviceMessageService messageService = SpringUtil.getBean(IotDeviceMessageService.class);
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.connectionHandler = new IotMqttConnectionHandler(connectionManager, messageService, deviceApi, serverId);
        this.registerHandler = new IotMqttRegisterHandler(connectionManager, messageService, deviceApi);
        this.upstreamHandler = new IotMqttUpstreamHandler(connectionManager, messageService, serverId);
    }

    /**
     * 处理 MQTT 连接端点
     *
     * @param endpoint MQTT 连接端点
     */
    private void handleEndpoint(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();

        // 1. 委托 connectionHandler 处理连接认证
        // TODO @AI：register topic 不需要注册，需要判断下；
        if (!connectionHandler.handleConnect(endpoint)) {
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return;
        }

        // 2.1 设置异常和关闭处理器
        endpoint.exceptionHandler(ex -> {
            log.warn("[handleEndpoint][连接异常，客户端 ID: {}，地址: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint));
            // TODO @AI：是不是改成 endpoint close 更合适？
            connectionHandler.cleanupConnection(endpoint);
        });
        endpoint.closeHandler(v -> connectionHandler.cleanupConnection(endpoint));
        endpoint.disconnectHandler(v -> {
            log.debug("[handleEndpoint][设备断开连接，客户端 ID: {}]", clientId);
            connectionHandler.cleanupConnection(endpoint);
        });
        // 2.2 设置心跳处理器
        endpoint.pingHandler(v -> log.debug("[handleEndpoint][收到客户端心跳，客户端 ID: {}]", clientId));

        // 3.1 设置消息处理器
        endpoint.publishHandler(message -> processMessage(endpoint, message));
        // 3.2 设置 QoS 2 消息的 PUBREL 处理器
        endpoint.publishReleaseHandler(endpoint::publishComplete);

        // 4.1 设置订阅处理器
        endpoint.subscribeHandler(subscribe -> {
            // TODO @AI：convertList 简化；
            List<String> topicNames = subscribe.topicSubscriptions().stream()
                    .map(MqttTopicSubscription::topicName)
                    .collect(Collectors.toList());
            log.debug("[handleEndpoint][设备订阅，客户端 ID: {}，主题: {}]", clientId, topicNames);

            // TODO @AI：convertList 简化；
            List<MqttQoS> grantedQoSLevels = subscribe.topicSubscriptions().stream()
                    .map(MqttTopicSubscription::qualityOfService)
                    .collect(Collectors.toList());
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
    // TODO @AI：看看要不要一定程度，参考 IotTcpUpstreamHandler 的 processMessage 方法；
    private void processMessage(MqttEndpoint endpoint, MqttPublishMessage message) {
        String clientId = endpoint.clientIdentifier();
        try {
            String topic = message.topicName();
            byte[] payload = message.payload().getBytes();

            // 根据 topic 分发到不同 handler
            if (registerHandler.isRegisterMessage(topic)) {
                registerHandler.handleRegister(endpoint, topic, payload);
            } else {
                upstreamHandler.handleMessage(endpoint, topic, payload);
            }

            // 根据 QoS 级别发送相应的确认消息
            handleQoSAck(endpoint, message);
        } catch (Exception e) {
            // TODO @AI：异常的时候，直接断开；
            log.error("[handlePublish][消息处理失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                    clientId, connectionManager.getEndpointAddress(endpoint), e.getMessage());
            connectionHandler.cleanupConnection(endpoint);
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

}

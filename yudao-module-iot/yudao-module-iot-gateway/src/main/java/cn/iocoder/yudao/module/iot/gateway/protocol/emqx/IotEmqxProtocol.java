package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.downstream.IotEmqxDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream.IotEmqxAuthEventHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream.IotEmqxUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IoT 网关 EMQX 协议实现：
 * <p>
 * 1. 提供 HTTP Hook 服务（/mqtt/auth、/mqtt/acl、/mqtt/event）给 EMQX 调用
 * 2. 通过 MQTT Client 订阅设备上行消息，并发布下行消息到 Broker
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxProtocol implements IotProtocol {

    /**
     * 协议配置
     */
    private final ProtocolProperties properties;
    /**
     * EMQX 配置
     */
    private final IotEmqxConfig emqxConfig;
    /**
     * 服务器 ID
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
     * HTTP Hook 服务器
     */
    private HttpServer httpServer;

    /**
     * MQTT Client
     */
    private volatile MqttClient mqttClient;
    /**
     * MQTT 重连定时器 ID
     */
    private volatile Long reconnectTimerId;

    /**
     * 上行消息处理器
     */
    private final IotEmqxUpstreamHandler upstreamHandler;

    /**
     * 下行消息订阅者
     */
    private IotEmqxDownstreamSubscriber downstreamSubscriber;

    public IotEmqxProtocol(ProtocolProperties properties) {
        Assert.notNull(properties, "协议实例配置不能为空");
        Assert.notNull(properties.getEmqx(), "EMQX 协议配置（emqx）不能为空");
        this.properties = properties;
        this.emqxConfig = properties.getEmqx();
        Assert.notNull(emqxConfig.getConnectTimeoutSeconds(),
                "MQTT 连接超时时间(emqx.connect-timeout-seconds)不能为空");
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
        this.upstreamHandler = new IotEmqxUpstreamHandler(serverId);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.EMQX;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT EMQX 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例 和 下行消息订阅者
        this.vertx = Vertx.vertx();

        try {
            // 1.2 启动 HTTP Hook 服务
            startHttpServer();

            // 1.3 启动 MQTT Client
            startMqttClient();
            running = true;
            log.info("[start][IoT EMQX 协议 {} 启动成功，hookPort：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 2. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            this.downstreamSubscriber = new IotEmqxDownstreamSubscriber(this, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT EMQX 协议 {} 启动失败]", getId(), e);
            // 启动失败时，关闭资源
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
                log.info("[stop][IoT EMQX 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT EMQX 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 先置为 false：避免 closeHandler 触发重连
        running = false;
        stopMqttClientReconnectChecker();
        // 2.2 停止 MQTT Client
        stopMqttClient();

        // 2.3 停止 HTTP Hook 服务
        stopHttpServer();

        // 2.4 关闭 Vertx
        if (vertx != null) {
            try {
                vertx.close().toCompletionStage().toCompletableFuture()
                        .get(10, TimeUnit.SECONDS);
                log.info("[stop][IoT EMQX 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT EMQX 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }

        log.info("[stop][IoT EMQX 协议 {} 已停止]", getId());
    }

    // ======================================= HTTP Hook Server =======================================

    /**
     * 启动 HTTP Hook 服务（/mqtt/auth、/mqtt/acl、/mqtt/event）
     */
    private void startHttpServer() {
        // 1. 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setBodyLimit(1024 * 1024)); // 限制 body 大小为 1MB，防止大包攻击

        // 2. 创建处理器
        IotEmqxAuthEventHandler handler = new IotEmqxAuthEventHandler(serverId, this);
        router.post(IotMqttTopicUtils.MQTT_AUTH_PATH).handler(handler::handleAuth);
        router.post(IotMqttTopicUtils.MQTT_ACL_PATH).handler(handler::handleAcl);
        router.post(IotMqttTopicUtils.MQTT_EVENT_PATH).handler(handler::handleEvent);

        // 3. 启动 HTTP Server（支持 HTTPS）
        IotEmqxConfig.Http httpConfig = emqxConfig.getHttp();
        HttpServerOptions options = new HttpServerOptions().setPort(properties.getPort());
        if (httpConfig != null && Boolean.TRUE.equals(httpConfig.getSslEnabled())) {
            Assert.notBlank(httpConfig.getSslCertPath(), "EMQX HTTP SSL 证书路径(emqx.http.ssl-cert-path)不能为空");
            Assert.notBlank(httpConfig.getSslKeyPath(), "EMQX HTTP SSL 私钥路径(emqx.http.ssl-key-path)不能为空");
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(httpConfig.getSslKeyPath())
                    .setCertPath(httpConfig.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }
        try {
            httpServer = vertx.createHttpServer(options)
                    .requestHandler(router)
                    .listen()
                    .toCompletionStage().toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
            log.info("[startHttpServer][IoT EMQX 协议 {} HTTP Hook 服务启动成功, port: {}, ssl: {}]",
                    getId(), properties.getPort(), httpConfig != null && Boolean.TRUE.equals(httpConfig.getSslEnabled()));
        } catch (Exception e) {
            log.error("[startHttpServer][IoT EMQX 协议 {} HTTP Hook 服务启动失败, port: {}]", getId(), properties.getPort(), e);
            throw new RuntimeException("HTTP Hook 服务启动失败", e);
        }
    }

    private void stopHttpServer() {
        if (httpServer == null) {
            return;
        }
        try {
            httpServer.close().toCompletionStage().toCompletableFuture()
                    .get(5, TimeUnit.SECONDS);
            log.info("[stopHttpServer][IoT EMQX 协议 {} HTTP Hook 服务已停止]", getId());
        } catch (Exception e) {
            log.error("[stopHttpServer][IoT EMQX 协议 {} HTTP Hook 服务停止失败]", getId(), e);
        } finally {
            httpServer = null;
        }
    }

    // ======================================= MQTT Client ======================================

    private void startMqttClient() {
        // 1.1 创建 MQTT Client
        MqttClient client = createMqttClient();
        this.mqttClient = client;
        // 1.2 连接 MQTT Broker
        if (!connectMqttClient(client)) {
            throw new RuntimeException("MQTT Client 启动失败: 连接 Broker 失败");
        }

        // 2. 启动定时重连检查
        startMqttClientReconnectChecker();
    }

    private void stopMqttClient() {
        MqttClient client = this.mqttClient;
        this.mqttClient = null;  // 先清理引用
        if (client == null) {
            return;
        }

        // 1. 批量取消订阅（仅在连接时）
        if (client.isConnected()) {
            List<String> topicList = emqxConfig.getMqttTopics();
            if (CollUtil.isNotEmpty(topicList)) {
                try {
                    client.unsubscribe(topicList).toCompletionStage().toCompletableFuture()
                            .get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("[stopMqttClient][IoT EMQX 协议 {} 取消订阅异常]", getId(), e);
                }
            }
        }

        // 2. 断开 MQTT 连接
        try {
            client.disconnect().toCompletionStage().toCompletableFuture()
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[stopMqttClient][IoT EMQX 协议 {} 断开连接异常]", getId(), e);
        }
    }

    // ======================================= MQTT 基础方法 ======================================

    /**
     * 创建 MQTT 客户端
     *
     * @return 新创建的 MqttClient
     */
    private MqttClient createMqttClient() {
        // 1.1 基础配置
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(emqxConfig.getMqttClientId())
                .setUsername(emqxConfig.getMqttUsername())
                .setPassword(emqxConfig.getMqttPassword())
                .setSsl(Boolean.TRUE.equals(emqxConfig.getMqttSsl()))
                .setCleanSession(Boolean.TRUE.equals(emqxConfig.getCleanSession()))
                .setKeepAliveInterval(emqxConfig.getKeepAliveIntervalSeconds())
                .setMaxInflightQueue(emqxConfig.getMaxInflightQueue());
        options.setConnectTimeout(emqxConfig.getConnectTimeoutSeconds() * 1000); // Vert.x 需要毫秒
        options.setTrustAll(Boolean.TRUE.equals(emqxConfig.getTrustAll()));
        // 1.2 配置遗嘱消息
        IotEmqxConfig.Will will = emqxConfig.getWill();
        if (will != null && will.isEnabled()) {
            Assert.notBlank(will.getTopic(), "遗嘱消息主题(emqx.will.topic)不能为空");
            Assert.notNull(will.getPayload(), "遗嘱消息内容(emqx.will.payload)不能为空");
            options.setWillFlag(true)
                    .setWillTopic(will.getTopic())
                    .setWillMessageBytes(Buffer.buffer(will.getPayload()))
                    .setWillQoS(will.getQos())
                    .setWillRetain(will.isRetain());
        }
        // 1.3 配置高级 SSL/TLS（仅在启用 SSL 且不信任所有证书时生效，且需要 sslOptions 非空）
        IotEmqxConfig.Ssl sslOptions = emqxConfig.getSslOptions();
        if (Boolean.TRUE.equals(emqxConfig.getMqttSsl())
                && Boolean.FALSE.equals(emqxConfig.getTrustAll())
                && sslOptions != null) {
            if (StrUtil.isNotBlank(sslOptions.getTrustStorePath())) {
                options.setTrustStoreOptions(new JksOptions()
                        .setPath(sslOptions.getTrustStorePath())
                        .setPassword(sslOptions.getTrustStorePassword()));
            }
            if (StrUtil.isNotBlank(sslOptions.getKeyStorePath())) {
                options.setKeyStoreOptions(new JksOptions()
                        .setPath(sslOptions.getKeyStorePath())
                        .setPassword(sslOptions.getKeyStorePassword()));
            }
        }

        // 2. 创建客户端
        return MqttClient.create(vertx, options);
    }

    /**
     * 连接 MQTT Broker（同步等待）
     *
     * @param client MQTT 客户端
     * @return 连接成功返回 true，失败返回 false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private synchronized boolean connectMqttClient(MqttClient client) {
        String host = emqxConfig.getMqttHost();
        int port = emqxConfig.getMqttPort();
        int timeoutSeconds = emqxConfig.getConnectTimeoutSeconds();
        try {
            // 1. 连接 Broker
            client.connect(port, host).toCompletionStage().toCompletableFuture()
                    .get(timeoutSeconds, TimeUnit.SECONDS);
            log.info("[connectMqttClient][IoT EMQX 协议 {} 连接成功, host: {}, port: {}]",
                    getId(), host, port);

            // 2. 设置处理器
            setupMqttClientHandlers(client);
            subscribeMqttClientTopics(client);
            return true;
        } catch (Exception e) {
            log.error("[connectMqttClient][IoT EMQX 协议 {} 连接发生异常]", getId(), e);
            return false;
        }
    }

    /**
     * 关闭 MQTT 客户端
     */
    private void closeMqttClient() {
        MqttClient oldClient = this.mqttClient;
        this.mqttClient = null;  // 先清理引用
        if (oldClient == null) {
            return;
        }
        // 尽力释放（无论是否连接都尝试 disconnect）
        try {
            oldClient.disconnect().toCompletionStage().toCompletableFuture()
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    // ======================================= MQTT 重连机制 ======================================

    /**
     * 启动 MQTT Client 周期性重连检查器
     */
    private void startMqttClientReconnectChecker() {
        long interval = emqxConfig.getReconnectDelayMs();
        this.reconnectTimerId = vertx.setPeriodic(interval, timerId -> {
            if (!running) {
                return;
            }
            if (mqttClient != null && mqttClient.isConnected()) {
                return;
            }
            log.info("[startMqttClientReconnectChecker][IoT EMQX 协议 {} 检测到断开，尝试重连]", getId());
            // 用 executeBlocking 避免阻塞 event-loop（tryReconnectMqttClient 内部有同步等待）
            vertx.executeBlocking(() -> {
                tryReconnectMqttClient();
                return null;
            });
        });
    }

    /**
     * 停止 MQTT Client 重连检查器
     */
    private void stopMqttClientReconnectChecker() {
        if (reconnectTimerId != null && vertx != null) {
            try {
                vertx.cancelTimer(reconnectTimerId);
            } catch (Exception ignored) {
            }
            reconnectTimerId = null;
        }
    }

    /**
     * 尝试重连 MQTT Client
     */
    private synchronized void tryReconnectMqttClient() {
        // 1. 前置检查
        if (!running) {
            return;
        }
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }

        log.info("[tryReconnectMqttClient][IoT EMQX 协议 {} 开始重连]", getId());
        try {
            // 2. 关闭旧客户端
            closeMqttClient();

            // 3.1 创建新客户端
            MqttClient client = createMqttClient();
            this.mqttClient = client;
            // 3.2 连接（失败只打印日志，等下次定时）
            if (!connectMqttClient(client)) {
                log.warn("[tryReconnectMqttClient][IoT EMQX 协议 {} 重连失败，等待下次重试]", getId());
            }
        } catch (Exception e) {
            log.error("[tryReconnectMqttClient][IoT EMQX 协议 {} 重连异常]", getId(), e);
        }
    }

    // ======================================= MQTT Handler ======================================

    /**
     * 设置 MQTT Client 事件处理器
     */
    private void setupMqttClientHandlers(MqttClient client) {
        // 1. 断开重连监听
        client.closeHandler(closeEvent -> {
            if (!running) {
                return;
            }
            log.warn("[setupMqttClientHandlers][IoT EMQX 协议 {} 连接断开，立即尝试重连]", getId());
            // 用 executeBlocking 避免阻塞 event-loop（tryReconnectMqttClient 内部有同步等待）
            vertx.executeBlocking(() -> {
                tryReconnectMqttClient();
                return null;
            });
        });

        // 2. 异常处理
        client.exceptionHandler(exception ->
                log.error("[setupMqttClientHandlers][IoT EMQX 协议 {} MQTT Client 异常]", getId(), exception));

        // 3. 上行消息处理
        client.publishHandler(upstreamHandler::handle);
    }

    /**
     * 订阅 MQTT Client 主题（同步等待）
     */
    private void subscribeMqttClientTopics(MqttClient client) {
        List<String> topicList = emqxConfig.getMqttTopics();
        if (!client.isConnected()) {
            log.warn("[subscribeMqttClientTopics][IoT EMQX 协议 {} MQTT Client 未连接, 跳过订阅]", getId());
            return;
        }
        if (CollUtil.isEmpty(topicList)) {
            log.warn("[subscribeMqttClientTopics][IoT EMQX 协议 {} 未配置订阅主题, 跳过订阅]", getId());
            return;
        }
        // 执行订阅
        Map<String, Integer> topics = convertMap(emqxConfig.getMqttTopics(), topic -> topic,
                topic -> emqxConfig.getMqttQos());
        try {
            client.subscribe(topics).toCompletionStage().toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
            log.info("[subscribeMqttClientTopics][IoT EMQX 协议 {} 订阅成功, 共 {} 个主题]", getId(), topicList.size());
        } catch (Exception e) {
            log.error("[subscribeMqttClientTopics][IoT EMQX 协议 {} 订阅失败]", getId(), e);
        }
    }

    /**
     * 发布消息到 MQTT Broker
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void publishMessage(String topic, byte[] payload) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[publishMessage][IoT EMQX 协议 {} MQTT Client 未连接, 无法发布消息]", getId());
            return;
        }
        MqttQoS qos = MqttQoS.valueOf(emqxConfig.getMqttQos());
        mqttClient.publish(topic, Buffer.buffer(payload), qos, false, false)
                .onFailure(e -> log.error("[publishMessage][IoT EMQX 协议 {} 发布失败, topic: {}]", getId(), topic, e));
    }

    /**
     * 延迟发布消息到 MQTT Broker
     *
     * @param topic   主题
     * @param payload 消息内容
     * @param delayMs 延迟时间（毫秒）
     */
    public void publishDelayMessage(String topic, byte[] payload, long delayMs) {
        vertx.setTimer(delayMs, id -> publishMessage(topic, payload));
    }

}

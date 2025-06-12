package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttHttpAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttUpstreamHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 MQTT 统一协议
 * <p>
 * 集成了 MQTT 上行协议和 HTTP 认证协议的功能：
 * 1. MQTT 客户端：连接 EMQX，处理设备上行和下行消息
 * 2. HTTP 认证服务：为 EMQX 提供设备认证接口
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamProtocol {

    /**
     * 默认 QoS 级别
     */
    private static final MqttQoS DEFAULT_QOS = MqttQoS.AT_LEAST_ONCE;

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    // 共享资源
    private Vertx vertx;

    // MQTT 客户端相关
    private MqttClient mqttClient;
    private IotMqttUpstreamHandler upstreamHandler;

    // HTTP 认证服务相关
    private HttpServer httpAuthServer;
    private IotMqttHttpAuthHandler authHandler;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    /**
     * 构造函数
     */
    public IotMqttUpstreamProtocol(IotGatewayProperties.EmqxProperties emqxProperties) {
        this.emqxProperties = emqxProperties;
    }

    @PostConstruct
    public void start() {
        if (isRunning) {
            log.warn("[start][MQTT 统一协议服务已经在运行中，请勿重复启动]");
            return;
        }
        log.info("[start][开始启动 MQTT 统一协议服务]");

        try {
            // 1. 创建共享的 Vertx 实例
            this.vertx = Vertx.vertx();
            log.info("[start][共享 Vertx 实例创建成功]");

            // 2. 启动 HTTP 认证服务
            startHttpAuthServer();

            // 3. 启动 MQTT 客户端
            startMqttClient();

            isRunning = true;
            log.info("[start][MQTT 统一协议服务启动完成]");
        } catch (Exception e) {
            log.error("[start][MQTT 统一协议服务启动失败]", e);
            // 启动失败时清理资源
            stop();
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (!isRunning) {
            log.warn("[stop][MQTT 统一协议服务已经停止，无需再次停止]");
            return;
        }
        log.info("[stop][开始停止 MQTT 统一协议服务]");

        // 1. 停止 MQTT 客户端
        stopMqttClient();

        // 2. 停止 HTTP 认证服务
        stopHttpAuthServer();

        // 3. 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close();
                log.info("[stop][Vertx 实例已关闭]");
            } catch (Exception e) {
                log.warn("[stop][关闭 Vertx 实例失败]", e);
            }
        }

        isRunning = false;
        log.info("[stop][MQTT 统一协议服务已停止]");
    }

    /**
     * 启动 HTTP 认证服务
     */
    private void startHttpAuthServer() {
        log.info("[startHttpAuthServer][开始启动 HTTP 认证服务]");

        // 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建认证处理器
        this.authHandler = new IotMqttHttpAuthHandler();

        // 添加认证路由
        router.post("/mqtt/auth/authenticate").handler(authHandler::authenticate);
        router.post("/mqtt/auth/connected").handler(authHandler::connected);
        router.post("/mqtt/auth/disconnected").handler(authHandler::disconnected);

        // 启动 HTTP 服务器
        int authPort = emqxProperties.getHttpAuthPort();
        try {
            httpAuthServer = vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(authPort)
                    .result();
            log.info("[startHttpAuthServer][HTTP 认证服务启动成功，端口：{}]", authPort);
        } catch (Exception e) {
            log.error("[startHttpAuthServer][HTTP 认证服务启动失败]", e);
            throw e;
        }
    }

    /**
     * 停止 HTTP 认证服务
     */
    private void stopHttpAuthServer() {
        if (httpAuthServer != null) {
            try {
                httpAuthServer.close().result();
                log.info("[stopHttpAuthServer][HTTP 认证服务已停止]");
            } catch (Exception e) {
                log.error("[stopHttpAuthServer][HTTP 认证服务停止失败]", e);
            }
        }
    }

    /**
     * 启动 MQTT 客户端
     */
    private void startMqttClient() {
        log.info("[startMqttClient][开始启动 MQTT 客户端]");

        // 初始化消息处理器
        this.upstreamHandler = new IotMqttUpstreamHandler();

        // 创建 MQTT 客户端
        MqttClientOptions options = new MqttClientOptions()
                .setClientId("yudao-iot-gateway-" + IdUtil.fastSimpleUUID())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword())
                .setSsl(ObjUtil.defaultIfNull(emqxProperties.getMqttSsl(), false));

        this.mqttClient = MqttClient.create(vertx, options);

        // 连接 MQTT Broker
        connectMqtt();
    }

    /**
     * 停止 MQTT 客户端
     */
    private void stopMqttClient() {
        // 1. 取消 MQTT 主题订阅
        if (mqttClient != null && mqttClient.isConnected()) {
            List<String> topicList = emqxProperties.getMqttTopics();
            if (CollUtil.isNotEmpty(topicList)) {
                for (String topic : topicList) {
                    try {
                        mqttClient.unsubscribe(topic);
                        log.debug("[stopMqttClient][取消订阅主题: {}]", topic);
                    } catch (Exception e) {
                        log.warn("[stopMqttClient][取消订阅主题异常: {}]", topic, e);
                    }
                }
            }
        }

        // 2. 关闭 MQTT 客户端
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("[stopMqttClient][MQTT 客户端已断开]");
            }
        } catch (Exception e) {
            log.warn("[stopMqttClient][关闭 MQTT 客户端异常]", e);
        }
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     */
    private void connectMqtt() {
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();

        if (StrUtil.isBlank(host)) {
            String msg = "[connectMqtt][MQTT Host 为空，无法连接]";
            log.error(msg);
            return;
        }
        if (port == null) {
            log.warn("[connectMqtt][MQTT Port 为 null，使用默认端口 1883]");
            port = 1883;
        }

        final Integer finalPort = port;
        CompletableFuture<Void> connectFuture = mqttClient.connect(finalPort, host)
                .toCompletionStage()
                .toCompletableFuture()
                .thenAccept(connAck -> {
                    log.info("[connectMqtt][MQTT 客户端连接成功]");
                    // 设置断开重连监听器
                    mqttClient.closeHandler(closeEvent -> {
                        log.warn("[closeHandler][MQTT 连接已断开，准备重连]");
                        reconnectWithDelay();
                    });
                    // 设置消息处理器
                    setupMessageHandler();
                    // 订阅主题
                    subscribeToTopics();
                })
                .exceptionally(error -> {
                    log.error("[connectMqtt][连接 MQTT Broker 失败]", error);
                    reconnectWithDelay();
                    return null;
                });

        // 等待连接完成
        try {
            connectFuture.get(10, TimeUnit.SECONDS);
            log.info("[connectMqtt][MQTT 客户端启动完成]");
        } catch (Exception e) {
            log.error("[connectMqtt][MQTT 客户端启动失败]", e);
        }
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        mqttClient.publishHandler(upstreamHandler::handle);
        log.debug("[setupMessageHandler][MQTT 消息处理器设置完成]");
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        List<String> topicList = emqxProperties.getMqttTopics();
        if (CollUtil.isEmpty(topicList)) {
            log.warn("[subscribeToTopics][没有配置要订阅的主题]");
            return;
        }

        for (String topic : topicList) {
            mqttClient.subscribe(topic, DEFAULT_QOS.value(), subscribeResult -> {
                if (subscribeResult.succeeded()) {
                    log.info("[subscribeToTopics][订阅主题成功: {}]", topic);
                } else {
                    log.error("[subscribeToTopics][订阅主题失败: {}]", topic, subscribeResult.cause());
                }
            });
        }
    }

    /**
     * 延迟重连
     */
    private void reconnectWithDelay() {
        vertx.setTimer(5000, timerId -> {
            if (isRunning && (mqttClient == null || !mqttClient.isConnected())) {
                log.info("[reconnectWithDelay][开始重连 MQTT Broker]");
                connectMqtt();
            }
        });
    }

    /**
     * 发布消息到 MQTT Broker
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void publishMessage(String topic, String payload) {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.publish(topic, Buffer.buffer(payload), DEFAULT_QOS, false, false);
            log.debug("[publishMessage][发布消息成功][topic: {}]", topic);
        } else {
            log.warn("[publishMessage][MQTT 客户端未连接，无法发布消息][topic: {}]", topic);
        }
    }

    /**
     * 获取服务器 ID
     */
    public String getServerId() {
        return IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
    }

}
package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttHttpAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 MQTT 协议：接收设备上行消息
 * <p>
 * 1. MQTT 客户端：连接 EMQX，消费处理设备上行和下行消息
 * 2. HTTP 认证服务：为 EMQX 提供设备认证、连接、断开接口
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamProtocol {

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    private Vertx vertx;

    @Getter
    private final String serverId;

    // MQTT 客户端相关
    private MqttClient mqttClient;
    private IotMqttUpstreamHandler upstreamHandler;

    // HTTP 认证服务相关
    private HttpServer httpAuthServer;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    public IotMqttUpstreamProtocol(IotGatewayProperties.EmqxProperties emqxProperties) {
        this.emqxProperties = emqxProperties;
        this.serverId = IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
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
        IotMqttHttpAuthHandler authHandler = new IotMqttHttpAuthHandler();
        router.post(IotMqttTopicUtils.MQTT_AUTH_AUTHENTICATE_PATH).handler(authHandler::authenticate);
        router.post(IotMqttTopicUtils.MQTT_AUTH_CONNECTED_PATH).handler(authHandler::connected);
        router.post(IotMqttTopicUtils.MQTT_AUTH_DISCONNECTED_PATH).handler(authHandler::disconnected);

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
        if (httpAuthServer == null) {
            return;
        }
        try {
            httpAuthServer.close().result();
            log.info("[stopHttpAuthServer][HTTP 认证服务已停止]");
        } catch (Exception e) {
            log.error("[stopHttpAuthServer][HTTP 认证服务停止失败]", e);
        }
    }

    /**
     * 启动 MQTT 客户端
     */
    private void startMqttClient() {
        log.info("[startMqttClient][开始启动 MQTT 客户端]");

        // 1. 初始化消息处理器
        this.upstreamHandler = new IotMqttUpstreamHandler(this);

        // 2. 创建 MQTT 客户端
        log.info("[startMqttClient][使用 MQTT 客户端 ID: {}]", emqxProperties.getMqttClientId());

        MqttClientOptions options = new MqttClientOptions()
                .setClientId(emqxProperties.getMqttClientId())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword())
                .setSsl(emqxProperties.getMqttSsl());
        this.mqttClient = MqttClient.create(vertx, options);

        // 3. 连接 MQTT Broker
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
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                log.info("[stopMqttClient][MQTT 客户端已断开]");
            } catch (Exception e) {
                log.warn("[stopMqttClient][关闭 MQTT 客户端异常]", e);
            }
        }
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     */
    private void connectMqtt() {
        // 1. 参数校验
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();
        if (StrUtil.isBlank(host)) {
            log.error("[connectMqtt][MQTT Host 为空，无法连接]");
            throw new IllegalArgumentException("MQTT Host 不能为空");
        }
        if (port == null || port <= 0) {
            log.error("[connectMqtt][MQTT Port 无效：{}]", port);
            throw new IllegalArgumentException("MQTT Port 必须为正整数");
        }
        log.info("[connectMqtt][开始连接 MQTT Broker][host: {}][port: {}]", host, port);

        // 2. 连接
        try {
            mqttClient.connect(port, host)
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get(emqxProperties.getConnectTimeoutSeconds(), TimeUnit.SECONDS);
            log.info("[connectMqtt][MQTT 客户端连接成功][host: {}][port: {}]", host, port);

            // 3. 设置处理器
            // 3.1 设置断开重连监听器
            mqttClient.closeHandler(closeEvent -> {
                log.warn("[closeHandler][MQTT 连接已断开，准备重连]");
                reconnectWithDelay();
            });
            // 3.2 设置消息处理器
            mqttClient.publishHandler(upstreamHandler::handle);
            log.debug("[setupMessageHandler][MQTT 消息处理器设置完成]");

            // 4. 订阅主题
            subscribeToTopics();
        } catch (Exception e) {
            log.error("[connectMqtt][连接 MQTT Broker 失败][host: {}][port: {}]", host, port, e);
            reconnectWithDelay(); // 连接失败时，也要尝试重连
            throw new RuntimeException("MQTT 客户端启动失败", e);
        }
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        List<String> topicList = emqxProperties.getMqttTopics();
        int qos = emqxProperties.getMqttQos();

        log.info("[subscribeToTopics][开始订阅主题，共 {} 个]", topicList.size());
        for (String topic : topicList) {
            mqttClient.subscribe(topic, qos, subscribeResult -> {
                if (subscribeResult.succeeded()) {
                    log.info("[subscribeToTopics][订阅主题成功: {}][QoS: {}]", topic, qos);
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
        long delay = emqxProperties.getReconnectDelayMs();
        vertx.setTimer(delay, timerId -> {
            if (!isRunning || (mqttClient != null && mqttClient.isConnected())) {
                return;
            }
            log.info("[reconnectWithDelay][开始重连 MQTT Broker，延迟 {} 毫秒]", delay);
            try {
                connectMqtt();
            } catch (Exception e) {
                log.error("[reconnectWithDelay][重连失败，将继续尝试重连]", e);
                reconnectWithDelay(); // 失败后，继续尝试
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
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[publishMessage][MQTT 客户端未连接，无法发布消息][topic: {}]", topic);
            return;
        }
        MqttQoS qos = MqttQoS.valueOf(emqxProperties.getMqttQos());
        mqttClient.publish(topic, Buffer.buffer(payload), qos, false, false);
    }

}
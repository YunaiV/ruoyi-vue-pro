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

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    private Vertx vertx;

    @Getter
    private final String serverId;

    // MQTT 客户端相关
    private MqttClient mqttClient;
    private IotMqttUpstreamHandler upstreamHandler;

    // HTTP 认证服务相关
    private HttpServer httpAuthServer;

    public IotMqttUpstreamProtocol(IotGatewayProperties.EmqxProperties emqxProperties) {
        this.emqxProperties = emqxProperties;
        this.serverId = IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
    }

    @PostConstruct
    public void start() {
        if (isRunning) {
            log.warn("[start][MQTT 协议服务已经在运行中，请勿重复启动]");
            return;
        }
        log.info("[start][启动 MQTT 协议服务]");

        try {
            this.vertx = Vertx.vertx();

            // 1. 启动 HTTP 认证服务
            startHttpAuthServer();

            // 2. 启动 MQTT 客户端
            startMqttClient();

            isRunning = true;
            log.info("[start][MQTT 协议服务启动完成]");
        } catch (Exception e) {
            log.error("[start][MQTT 协议服务启动失败]", e);
            // 启动失败时清理资源
            stop();
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (!isRunning) {
            log.warn("[stop][MQTT 协议服务已经停止，无需再次停止]");
            return;
        }
        log.info("[stop][停止 MQTT 协议服务]");

        // 1. 停止 MQTT 客户端
        stopMqttClient();

        // 2. 停止 HTTP 认证服务
        stopHttpAuthServer();

        // 3. 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close();
                log.debug("[stop][Vertx 实例已关闭]");
            } catch (Exception e) {
                log.warn("[stop][关闭 Vertx 实例失败]", e);
            }
        }

        isRunning = false;
        log.info("[stop][MQTT 协议服务已停止]");
    }

    /**
     * 启动 HTTP 认证服务
     */
    private void startHttpAuthServer() {
        log.info("[startHttpAuthServer][启动 HTTP 认证服务]");

        // 1.1 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // 1.2 创建认证处理器
        IotMqttHttpAuthHandler authHandler = new IotMqttHttpAuthHandler(this);
        router.post(IotMqttTopicUtils.MQTT_AUTH_PATH).handler(authHandler::handleAuth);
        router.post(IotMqttTopicUtils.MQTT_EVENT_PATH).handler(authHandler::handleEvent);

        // 2. 启动 HTTP 服务器
        int authPort = emqxProperties.getHttpAuthPort();
        try {
            httpAuthServer = vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(authPort)
                    .result();
            log.info("[startHttpAuthServer][HTTP 认证服务启动成功, 端口: {}]", authPort);
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
        log.info("[startMqttClient][启动 MQTT 客户端]");

        try {
            // 1. 初始化消息处理器
            this.upstreamHandler = new IotMqttUpstreamHandler(this);

            // 2. 创建 MQTT 客户端
            log.info("[startMqttClient][使用 MQTT 客户端 ID: {}]", emqxProperties.getMqttClientId());
            createMqttClient();

            // 3. 连接 MQTT Broker（异步连接，不会抛出异常）
            connectMqtt(false);

            log.info("[startMqttClient][MQTT 客户端启动完成，正在异步连接中...]");
        } catch (Exception e) {
            log.error("[startMqttClient][MQTT 客户端启动失败]", e);
            throw new RuntimeException("MQTT 客户端启动失败", e);
        }
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
                        log.warn("[stopMqttClient][取消订阅主题({})异常]", topic, e);
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
     *
     * @param isReconnect 是否为重连
     */
    private void connectMqtt(boolean isReconnect) {
        // 1. 参数校验
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();
        // TODO @haohao：这些参数校验，交给 validator；
        if (StrUtil.isBlank(host)) {
            log.error("[connectMqtt][MQTT Host 为空, 无法连接]");
            throw new IllegalArgumentException("MQTT Host 不能为空");
        }
        if (port == null || port <= 0) {
            log.error("[connectMqtt][MQTT Port({}) 无效]", port);
            throw new IllegalArgumentException("MQTT Port 必须为正整数");
        }

        if (isReconnect) {
            log.info("[connectMqtt][开始重连 MQTT Broker, host: {}, port: {}]", host, port);
            // 重连时重新创建客户端实例
            createMqttClient();
        } else {
            log.info("[connectMqtt][开始连接 MQTT Broker, host: {}, port: {}]", host, port);
        }

        // 2. 异步连接
        mqttClient.connect(port, host, connectResult -> {
            // TODO @haohao：if return，减少括号哈；
            if (connectResult.succeeded()) {
                if (isReconnect) {
                    log.info("[connectMqtt][MQTT 客户端重连成功, host: {}, port: {}]", host, port);
                } else {
                    log.info("[connectMqtt][MQTT 客户端连接成功, host: {}, port: {}]", host, port);
                }

                // 设置处理器
                setupMqttHandlers();
                // 订阅主题
                subscribeToTopics();
            } else {
                log.error("[connectMqtt][连接 MQTT Broker 失败, host: {}, port: {}, isReconnect: {}]",
                        host, port, isReconnect, connectResult.cause());

                // TODO @haohao：体感上，是不是首次必须连接成功？类似 mysql；首次要连接上，然后后续可以重连；
                if (!isReconnect) {
                    // 首次连接失败时，也要尝试重连
                    log.warn("[connectMqtt][首次连接失败，将开始重连机制]");
                    reconnectWithDelay();
                } else {
                    // 重连失败时，继续尝试重连
                    reconnectWithDelay();
                }
            }
        });
    }

    /**
     * 创建 MQTT 客户端
     */
    private void createMqttClient() {
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(emqxProperties.getMqttClientId())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword())
                .setSsl(emqxProperties.getMqttSsl());
        this.mqttClient = MqttClient.create(vertx, options);
    }

    /**
     * 设置 MQTT 处理器
     */
    private void setupMqttHandlers() {
        // TODO @haohao：mqttClient 一定非空；
        if (mqttClient == null) {
            log.warn("[setupMqttHandlers][MQTT 客户端为空，跳过处理器设置]");
            return;
        }

        // 设置断开重连监听器
        mqttClient.closeHandler(closeEvent -> {
            log.warn("[closeHandler][MQTT 连接已断开, 准备重连]");
            reconnectWithDelay();
        });

        // 设置异常处理器
        mqttClient.exceptionHandler(exception -> {
            log.error("[exceptionHandler][MQTT 客户端异常]", exception);
        });

        // 设置消息处理器
        // TODO @haohao：upstreamHandler 一定非空；
        if (upstreamHandler != null) {
            mqttClient.publishHandler(upstreamHandler::handle);
            log.debug("[setupMqttHandlers][MQTT 消息处理器设置完成]");
        } else {
            log.warn("[setupMqttHandlers][上行消息处理器为空，跳过设置]");
        }
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        List<String> topicList = emqxProperties.getMqttTopics();
        if (CollUtil.isEmpty(topicList)) {
            log.warn("[subscribeToTopics][订阅主题列表为空, 跳过订阅]");
            return;
        }
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[subscribeToTopics][MQTT 客户端未连接, 跳过订阅]");
            return;
        }

        int qos = emqxProperties.getMqttQos();
        log.info("[subscribeToTopics][开始订阅主题, 共 {} 个, QoS: {}]", topicList.size(), qos);

        // TODO @haohao：使用 atomicinteger 会更合适；
        int[] successCount = { 0 }; // 使用数组以便在 lambda 中修改
        int[] failCount = { 0 };

        for (String topic : topicList) {
            // TODO @haohao：MqttClient subscribe(Map<String, Integer> topics, 是不是更简洁哈；
            mqttClient.subscribe(topic, qos, subscribeResult -> {
                if (subscribeResult.succeeded()) {
                    successCount[0]++;
                    log.debug("[subscribeToTopics][订阅主题成功, topic: {}, qos: {}]", topic, qos);

                    // 当所有主题都处理完成时，记录汇总日志
                    if (successCount[0] + failCount[0] == topicList.size()) {
                        log.info("[subscribeToTopics][主题订阅完成, 成功: {}, 失败: {}, 总计: {}]",
                                successCount[0], failCount[0], topicList.size());
                    }
                } else {
                    failCount[0]++;
                    log.error("[subscribeToTopics][订阅主题失败, topic: {}, qos: {}, 原因: {}]",
                            topic, qos, subscribeResult.cause().getMessage(), subscribeResult.cause());

                    // 当所有主题都处理完成时，记录汇总日志
                    if (successCount[0] + failCount[0] == topicList.size()) {
                        log.info("[subscribeToTopics][主题订阅完成, 成功: {}, 失败: {}, 总计: {}]",
                                successCount[0], failCount[0], topicList.size());
                    }
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
            if (!isRunning) {
                log.debug("[reconnectWithDelay][服务已停止, 取消重连]");
                return;
            }
            // 检查连接状态，如果已连接则无需重连
            if (mqttClient != null && mqttClient.isConnected()) {
                log.debug("[reconnectWithDelay][MQTT 客户端已连接, 无需重连]");
                return;
            }
            log.info("[reconnectWithDelay][开始重连 MQTT Broker, 延迟: {} ms]", delay);
            try {
                connectMqtt(true); // 标记为重连
            } catch (Exception e) {
                log.error("[reconnectWithDelay][重连失败, 将继续尝试]", e);
                // 重连失败时，不需要重复调用，因为 connectMqtt(true) 内部已经处理了重连逻辑
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
            log.warn("[publishMessage][MQTT 客户端未连接, 无法发布消息到 topic({})]", topic);
            return;
        }
        MqttQoS qos = MqttQoS.valueOf(emqxProperties.getMqttQos());
        mqttClient.publish(topic, Buffer.buffer(payload), qos, false, false);
    }

}
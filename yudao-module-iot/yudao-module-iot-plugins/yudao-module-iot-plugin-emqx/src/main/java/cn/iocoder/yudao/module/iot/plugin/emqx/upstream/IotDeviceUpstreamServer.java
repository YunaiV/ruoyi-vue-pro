package cn.iocoder.yudao.module.iot.plugin.emqx.upstream;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.emqx.config.IotPluginEmqxProperties;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceAuthVertxHandler;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceMqttMessageHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    private static final int RECONNECT_DELAY_MS = 5000; // 重连延迟时间(毫秒)
    private static final int CONNECTION_TIMEOUT_MS = 10000; // 连接超时时间(毫秒)
    private static final String TOPIC_SEPARATOR = ","; // 主题分隔符
    private static final MqttQoS DEFAULT_QOS = MqttQoS.AT_LEAST_ONCE; // 默认QoS级别

    private final Vertx vertx;
    private final HttpServer server;
    private final MqttClient client;
    private final IotPluginEmqxProperties emqxProperties;
    private final IotDeviceMqttMessageHandler mqttMessageHandler;
    private volatile boolean isRunning = false; // 服务运行状态标志

    public IotDeviceUpstreamServer(IotPluginEmqxProperties emqxProperties,
                                   IotDeviceUpstreamApi deviceUpstreamApi,
                                   Vertx vertx,
                                   MqttClient client) {
        this.vertx = vertx;
        this.emqxProperties = emqxProperties;
        this.client = client;

        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body
        router.post(IotDeviceAuthVertxHandler.PATH)
                .handler(new IotDeviceAuthVertxHandler(deviceUpstreamApi));
        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
        this.mqttMessageHandler = new IotDeviceMqttMessageHandler(deviceUpstreamApi, client);
    }

    /**
     * 启动 HTTP 服务器、MQTT 客户端
     */
    public void start() {
        if (isRunning) {
            log.warn("服务已经在运行中，请勿重复启动");
            return;
        }

        log.info("[start] 开始启动服务");

        // 1. 启动 HTTP 服务器
        CompletableFuture<Void> httpFuture = server.listen(emqxProperties.getAuthPort())
                .toCompletionStage()
                .toCompletableFuture()
                .thenAccept(v -> log.info("[start] HTTP服务器启动完成，端口: {}", server.actualPort()));

        // 2. 连接 MQTT Broker
        CompletableFuture<Void> mqttFuture = connectMqtt()
                .toCompletionStage()
                .toCompletableFuture()
                .thenAccept(v -> {
                    // 3. 添加 MQTT 断开重连监听器
                    client.closeHandler(closeEvent -> {
                        log.warn("[closeHandler] MQTT连接已断开，准备重连");
                        reconnectWithDelay();
                    });

                    // 4. 设置 MQTT 消息处理器
                    setupMessageHandler();
                });

        // 等待所有服务启动完成
        CompletableFuture.allOf(httpFuture, mqttFuture)
                .orTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[start] 服务启动失败", error);
                    } else {
                        isRunning = true;
                        log.info("[start] 所有服务启动完成");
                    }
                });
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        client.publishHandler(mqttMessageHandler::handle);
        log.debug("[setupMessageHandler] MQTT消息处理器设置完成");
    }

    /**
     * 重连 MQTT 客户端
     */
    private void reconnectWithDelay() {
        if (!isRunning) {
            log.info("[reconnectWithDelay] 服务已停止，不再尝试重连");
            return;
        }

        vertx.setTimer(RECONNECT_DELAY_MS, id -> {
            log.info("[reconnectWithDelay] 开始重新连接MQTT");
            connectMqtt();
        });
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     *
     * @return 连接结果的Future
     */
    private Future<Void> connectMqtt() {
        return client.connect(emqxProperties.getMqttPort(), emqxProperties.getMqttHost())
                .compose(connAck -> {
                    log.info("[connectMqtt] MQTT客户端连接成功");
                    return subscribeToTopics();
                })
                .recover(err -> {
                    log.error("[connectMqtt] 连接MQTT Broker失败: {}", err.getMessage());
                    reconnectWithDelay();
                    return Future.failedFuture(err);
                });
    }

    /**
     * 订阅设备上行消息主题
     *
     * @return 订阅结果的Future
     */
    private Future<Void> subscribeToTopics() {
        String topicsStr = emqxProperties.getMqttTopics();
        if (topicsStr == null || topicsStr.trim().isEmpty()) {
            log.warn("[subscribeToTopics] 未配置MQTT主题，跳过订阅");
            return Future.succeededFuture();
        }

        log.info("[subscribeToTopics] 开始订阅设备上行消息主题");

        String[] topics = topicsStr.split(TOPIC_SEPARATOR);
        Future<Void> compositeFuture = Future.succeededFuture();

        for (String topic : topics) {
            String trimmedTopic = topic.trim();
            if (trimmedTopic.isEmpty()) {
                continue;
            }

            compositeFuture = compositeFuture.compose(v -> client.subscribe(trimmedTopic, DEFAULT_QOS.value())
                    .<Void>map(ack -> {
                        log.info("[subscribeToTopics] 成功订阅主题: {}", trimmedTopic);
                        return null;
                    })
                    .recover(err -> {
                        log.error("[subscribeToTopics] 订阅主题失败: {}, 原因: {}",
                                trimmedTopic, err.getMessage());
                        return Future.<Void>succeededFuture(); // 继续订阅其他主题
                    }));
        }

        return compositeFuture;
    }

    /**
     * 停止所有服务
     */
    public void stop() {
        if (!isRunning) {
            log.warn("[stop] 服务未运行，无需停止");
            return;
        }

        log.info("[stop] 开始关闭服务");
        isRunning = false;

        try {
            CompletableFuture<Void> serverFuture = server != null
                    ? server.close().toCompletionStage().toCompletableFuture()
                    : CompletableFuture.completedFuture(null);

            CompletableFuture<Void> clientFuture = client != null
                    ? client.disconnect().toCompletionStage().toCompletableFuture()
                    : CompletableFuture.completedFuture(null);

            CompletableFuture<Void> vertxFuture = vertx != null
                    ? vertx.close().toCompletionStage().toCompletableFuture()
                    : CompletableFuture.completedFuture(null);

            // 等待所有资源关闭
            CompletableFuture.allOf(serverFuture, clientFuture, vertxFuture)
                    .orTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("[stop] 服务关闭过程中发生异常", error);
                        } else {
                            log.info("[stop] 所有服务关闭完成");
                        }
                    });
        } catch (Exception e) {
            log.error("[stop] 关闭服务异常", e);
            throw new RuntimeException("关闭IoT设备上行服务失败", e);
        }
    }
}
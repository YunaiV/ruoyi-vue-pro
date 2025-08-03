package cn.iocoder.yudao.module.iot.plugin.emqx.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.emqx.config.IotPluginEmqxProperties;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceAuthVertxHandler;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceMqttMessageHandler;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceWebhookVertxHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    /**
     * 重连延迟时间(毫秒)
     */
    private static final int RECONNECT_DELAY_MS = 5000;
    /**
     * 连接超时时间(毫秒)
     */
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    /**
     * 默认 QoS 级别
     */
    private static final MqttQoS DEFAULT_QOS = MqttQoS.AT_LEAST_ONCE;

    private final Vertx vertx;
    private final HttpServer server;
    private final MqttClient client;
    private final IotPluginEmqxProperties emqxProperties;
    private final IotDeviceMqttMessageHandler mqttMessageHandler;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

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
                // TODO @haohao：疑问，mqtt 的认证，需要通过 http 呀？
                // 回复：MQTT 认证不必须通过 HTTP 进行，但 HTTP 认证是 EMQX 等 MQTT 服务器支持的一种灵活的认证方式
                .handler(new IotDeviceAuthVertxHandler(deviceUpstreamApi));
        // 添加 Webhook 处理器，用于处理设备连接和断开连接事件
        router.post(IotDeviceWebhookVertxHandler.PATH)
                .handler(new IotDeviceWebhookVertxHandler(deviceUpstreamApi));
        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
        this.mqttMessageHandler = new IotDeviceMqttMessageHandler(deviceUpstreamApi, client);
    }

    /**
     * 启动 HTTP 服务器、MQTT 客户端
     */
    public void start() {
        if (isRunning) {
            log.warn("[start][服务已经在运行中，请勿重复启动]");
            return;
        }
        log.info("[start][开始启动服务]");

        // TODO @haohao：建议先启动 MQTT Broker，再启动 HTTP Server。类似 jdbc 先连接了，在启动 tomcat 的味道
        // 1. 启动 HTTP 服务器
        CompletableFuture<Void> httpFuture = server.listen(emqxProperties.getAuthPort())
                .toCompletionStage()
                .toCompletableFuture()
                .thenAccept(v -> log.info("[start][HTTP 服务器启动完成，端口: {}]", server.actualPort()));

        // 2. 连接 MQTT Broker
        CompletableFuture<Void> mqttFuture = connectMqtt()
                .toCompletionStage()
                .toCompletableFuture()
                .thenAccept(v -> {
                    // 2.1 添加 MQTT 断开重连监听器
                    client.closeHandler(closeEvent -> {
                        log.warn("[closeHandler][MQTT 连接已断开，准备重连]");
                        reconnectWithDelay();
                    });
                    // 2.2 设置 MQTT 消息处理器
                    setupMessageHandler();
                });

        // 3. 等待所有服务启动完成
        CompletableFuture.allOf(httpFuture, mqttFuture)
//                .orTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS) // TODO @芋艿：JDK8 不兼容
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[start][服务启动失败]", error);
                    } else {
                        isRunning = true;
                        log.info("[start][所有服务启动完成]");
                    }
                });
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        client.publishHandler(mqttMessageHandler::handle);
        log.debug("[setupMessageHandler][MQTT 消息处理器设置完成]");
    }

    /**
     * 重连 MQTT 客户端
     */
    private void reconnectWithDelay() {
        if (!isRunning) {
            log.info("[reconnectWithDelay][服务已停止，不再尝试重连]");
            return;
        }

        vertx.setTimer(RECONNECT_DELAY_MS, id -> {
            log.info("[reconnectWithDelay][开始重新连接 MQTT]");
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
                    log.info("[connectMqtt][MQTT客户端连接成功]");
                    return subscribeToTopics();
                })
                .recover(error -> {
                    log.error("[connectMqtt][连接MQTT Broker失败:]", error);
                    reconnectWithDelay();
                    return Future.failedFuture(error);
                });
    }

    /**
     * 订阅设备上行消息主题
     *
     * @return 订阅结果的 Future
     */
    private Future<Void> subscribeToTopics() {
        String[] topics = emqxProperties.getMqttTopics();
        if (ArrayUtil.isEmpty(topics)) {
            log.warn("[subscribeToTopics][未配置MQTT主题，跳过订阅]");
            return Future.succeededFuture();
        }
        log.info("[subscribeToTopics][开始订阅设备上行消息主题]");

        Future<Void> compositeFuture = Future.succeededFuture();
        for (String topic : topics) {
            String trimmedTopic = topic.trim();
            if (trimmedTopic.isEmpty()) {
                continue;
            }
            compositeFuture = compositeFuture.compose(v -> client.subscribe(trimmedTopic, DEFAULT_QOS.value())
                    .<Void>map(ack -> {
                        log.info("[subscribeToTopics][成功订阅主题: {}]", trimmedTopic);
                        return null;
                    })
                    .recover(error -> {
                        log.error("[subscribeToTopics][订阅主题失败: {}]", trimmedTopic, error);
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
            log.warn("[stop][服务未运行，无需停止]");
            return;
        }
        log.info("[stop][开始关闭服务]");
        isRunning = false;

        try {
            // 关闭 HTTP 服务器
            if (server != null) {
                server.close()
                        .toCompletionStage()
                        .toCompletableFuture()
                        .join();
            }

            // 关闭 MQTT 客户端
            if (client != null) {
                client.disconnect()
                       .toCompletionStage()
                       .toCompletableFuture()
                       .join();
            }

            // 关闭 Vertx 实例
            if (vertx!= null) {
                vertx.close()
                      .toCompletionStage()
                      .toCompletableFuture()
                      .join();
            }
            log.info("[stop][关闭完成]");
        } catch (Exception e) {
            log.error("[stop][关闭服务异常]", e);
            throw new RuntimeException("关闭 IoT 设备上行服务失败", e);
        }
    }
}
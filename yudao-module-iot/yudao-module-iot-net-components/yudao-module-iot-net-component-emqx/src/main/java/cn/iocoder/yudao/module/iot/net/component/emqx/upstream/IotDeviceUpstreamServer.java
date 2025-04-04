package cn.iocoder.yudao.module.iot.net.component.emqx.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.core.heartbeat.IotNetComponentRegistry;
import cn.iocoder.yudao.module.iot.net.component.emqx.config.IotNetComponentEmqxProperties;
import cn.iocoder.yudao.module.iot.net.component.emqx.upstream.router.IotDeviceAuthVertxHandler;
import cn.iocoder.yudao.module.iot.net.component.emqx.upstream.router.IotDeviceMqttMessageHandler;
import cn.iocoder.yudao.module.iot.net.component.emqx.upstream.router.IotDeviceWebhookVertxHandler;
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
 * IoT 设备上行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    /**
     * 默认 QoS 级别
     */
    private static final MqttQoS DEFAULT_QOS = MqttQoS.AT_LEAST_ONCE;

    private final Vertx vertx;
    private final HttpServer server;
    private final MqttClient client;
    private final IotNetComponentEmqxProperties emqxProperties;
    private final IotDeviceMqttMessageHandler mqttMessageHandler;
    private final IotNetComponentRegistry componentRegistry;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    public IotDeviceUpstreamServer(IotNetComponentEmqxProperties emqxProperties,
                                   IotDeviceUpstreamApi deviceUpstreamApi,
                                   Vertx vertx,
                                   MqttClient client,
                                   IotNetComponentRegistry componentRegistry) {
        this.vertx = vertx;
        this.emqxProperties = emqxProperties;
        this.client = client;
        this.componentRegistry = componentRegistry;

        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body
        router.post(IotDeviceAuthVertxHandler.PATH)
                // MQTT 认证不必须通过 HTTP 进行，但 HTTP 认证是 EMQX 等 MQTT 服务器支持的一种灵活的认证方式
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

        // 检查 authPort 是否为 null
        Integer authPort = emqxProperties.getAuthPort();
        if (authPort == null) {
            log.warn("[start][authPort 为 null，使用默认端口 8080]");
            authPort = 8080; // 默认端口
        }

        // 获取连接超时时间
        int connectionTimeoutMs = emqxProperties.getConnectionTimeoutMs() != null
                ? emqxProperties.getConnectionTimeoutMs()
                : 10000;

        // 1. 启动 HTTP 服务器
        final Integer finalAuthPort = authPort; // 为 lambda 表达式创建 final 变量
        CompletableFuture<Void> httpFuture = server.listen(finalAuthPort)
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
                .orTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS)
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

        // 获取重连延迟时间
        int reconnectDelayMs = emqxProperties.getReconnectDelayMs() != null
                ? emqxProperties.getReconnectDelayMs()
                : 5000;

        vertx.setTimer(reconnectDelayMs, id -> {
            log.info("[reconnectWithDelay][开始重新连接 MQTT]");
            connectMqtt();
        });
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     *
     * @return 连接结果的 Future
     */
    private Future<Void> connectMqtt() {
        // 检查必要的 MQTT 配置
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();
        if (StrUtil.isBlank(host)) {
            String msg = "[connectMqtt][MQTT Host 为空，无法连接]";
            log.error(msg);
            return Future.failedFuture(new IllegalStateException(msg));
        }
        if (port == null) {
            log.warn("[connectMqtt][MQTT Port 为 null，使用默认端口 1883]");
            port = 1883; // 默认 MQTT 端口
        }

        final Integer finalPort = port;
        return client.connect(finalPort, host)
                .compose(connAck -> {
                    log.info("[connectMqtt][MQTT 客户端连接成功]");
                    return subscribeToTopics();
                })
                .recover(error -> {
                    log.error("[connectMqtt][连接 MQTT Broker 失败:]", error);
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
            log.warn("[subscribeToTopics][未配置 MQTT 主题或为 null，使用默认主题]");
            topics = new String[]{"/device/#"}; // 默认订阅所有设备上下行主题
        }

        // 使用协调器追踪多个 Future 的完成状态
        Future<Void> result = Future.succeededFuture();
        for (String topic : topics) {
            if (StrUtil.isBlank(topic)) {
                log.warn("[subscribeToTopics][跳过空主题]");
                continue;
            }

            result = result.compose(v -> client.subscribe(topic, DEFAULT_QOS.value())
                    .<Void>map(ack -> {
                        log.info("[subscribeToTopics][订阅主题成功: {}]", topic);
                        return null;
                    })
                    .recover(err -> {
                        log.error("[subscribeToTopics][订阅主题失败: {}]", topic, err);
                        return Future.failedFuture(err);
                    }));
        }
        return result;
    }

    /**
     * 停止服务
     */
    public void stop() {
        if (!isRunning) {
            log.warn("[stop][服务已经停止，无需再次停止]");
            return;
        }
        log.info("[stop][开始停止服务]");

        // 1. 取消 MQTT 主题订阅
        if (client.isConnected()) {
            for (String topic : emqxProperties.getMqttTopics()) {
                try {
                    client.unsubscribe(topic);
                } catch (Exception e) {
                    log.warn("[stop][取消订阅主题异常: {}]", topic, e);
                }
            }
        }

        // 2. 关闭 MQTT 客户端
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception e) {
            log.warn("[stop][关闭 MQTT 客户端异常]", e);
        }

        // 3. 关闭 HTTP 服务器
        try {
            server.close();
        } catch (Exception e) {
            log.warn("[stop][关闭 HTTP 服务器异常]", e);
        }

        // 4. 更新状态
        isRunning = false;
        log.info("[stop][服务已停止]");
    }
}
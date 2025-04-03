package cn.iocoder.yudao.module.iot.component.emqx.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentRegistry;
import cn.iocoder.yudao.module.iot.component.emqx.config.IotComponentEmqxProperties;
import cn.iocoder.yudao.module.iot.component.emqx.upstream.router.IotDeviceAuthVertxHandler;
import cn.iocoder.yudao.module.iot.component.emqx.upstream.router.IotDeviceMqttMessageHandler;
import cn.iocoder.yudao.module.iot.component.emqx.upstream.router.IotDeviceWebhookVertxHandler;
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

    // TODO @haohao：抽到 IotComponentEmqxProperties 里？
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
    private final IotComponentEmqxProperties emqxProperties;
    private final IotDeviceMqttMessageHandler mqttMessageHandler;
    private final IotComponentRegistry componentRegistry;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    public IotDeviceUpstreamServer(IotComponentEmqxProperties emqxProperties,
                                   IotDeviceUpstreamApi deviceUpstreamApi,
                                   Vertx vertx,
                                   MqttClient client,
                                   IotComponentRegistry componentRegistry) {
        this.vertx = vertx;
        this.emqxProperties = emqxProperties;
        this.client = client;
        this.componentRegistry = componentRegistry;

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

        // 检查authPort是否为null
        Integer authPort = emqxProperties.getAuthPort();
        if (authPort == null) {
            log.warn("[start][authPort为null，使用默认端口8080]");
            authPort = 8080; // 默认端口
        }

        // 1. 启动 HTTP 服务器
        final Integer finalAuthPort = authPort; // 为lambda表达式创建final变量
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
                    // 2. 设置 MQTT 消息处理器
                    setupMessageHandler();
                });

        // 3. 等待所有服务启动完成
        CompletableFuture.allOf(httpFuture, mqttFuture)
                .orTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
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
        // 检查必要的 MQTT 配置
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();
        if (host == null) {
            String msg = "[connectMqtt][MQTT Host 为 null，无法连接]";
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
            log.warn("[subscribeToTopics][未配置 MQTT 主题或为 null，使用默认主题]");
            topics = new String[]{"/device/#"}; // 默认订阅所有设备上下行主题
        }
        log.info("[subscribeToTopics][开始订阅设备上行消息主题]");

        Future<Void> compositeFuture = Future.succeededFuture();
        for (String topic : topics) {
            String trimmedTopic = StrUtil.trim(topic);
            if (StrUtil.isBlank(trimmedTopic)) {
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
                            log.error("[stop][服务关闭过程中发生异常]", error);
                        } else {
                            log.info("[stop][所有服务关闭完成]");
                        }
                    });
        } catch (Exception e) {
            log.error("[stop][关闭服务异常]", e);
            throw new RuntimeException("关闭 IoT 设备上行服务失败", e);
        }
    }
}
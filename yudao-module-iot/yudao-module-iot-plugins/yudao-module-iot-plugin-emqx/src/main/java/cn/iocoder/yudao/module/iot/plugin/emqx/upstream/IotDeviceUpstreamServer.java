package cn.iocoder.yudao.module.iot.plugin.emqx.upstream;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.emqx.config.IotPluginEmqxProperties;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceAuthVertxHandler;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceMqttMessageHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    private static final int RECONNECT_DELAY = 5000; // 重连延迟时间(毫秒)

    private final Vertx vertx;
    private final HttpServer server;
    private final MqttClient client;
    private final IotPluginEmqxProperties emqxProperties;
    private final IotDeviceMqttMessageHandler mqttMessageHandler;

    public IotDeviceUpstreamServer(IotPluginEmqxProperties emqxProperties,
                                   IotDeviceUpstreamApi deviceUpstreamApi) {
        this.emqxProperties = emqxProperties;

        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();
        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body
        router.post(IotDeviceAuthVertxHandler.PATH)
                .handler(new IotDeviceAuthVertxHandler(deviceUpstreamApi));
        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);

        // 创建 MQTT 客户端
        MqttClientOptions options = new MqttClientOptions()
                .setClientId("yudao-iot-server-" + IdUtil.fastSimpleUUID())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword())
                .setSsl(emqxProperties.isMqttSsl());
        client = MqttClient.create(vertx, options);
        this.mqttMessageHandler = new IotDeviceMqttMessageHandler(deviceUpstreamApi, client);
    }

    /**
     * 启动 HTTP 服务器、MQTT 客户端
     */
    public void start() {
        // 1. 启动 HTTP 服务器
        log.info("[start][开始启动]");
        server.listen(emqxProperties.getAuthPort())
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        log.info("[start][HTTP服务器启动完成，端口({})]", this.server.actualPort());

        // 2. 连接 MQTT Broker
        connectMqtt();

        // 3. 添加 MQTT 断开重连监听器
        client.closeHandler(v -> {
            log.warn("[closeHandler][MQTT 连接已断开，准备重连]");
            reconnectWithDelay();
        });

        // 4. 设置 MQTT 消息处理器
        setupMessageHandler();
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        client.publishHandler(mqttMessageHandler::handle);
    }

    /**
     * 重连 MQTT 客户端
     */
    private void reconnectWithDelay() {
        vertx.setTimer(RECONNECT_DELAY, id -> {
            log.info("[reconnectWithDelay][开始重新连接 MQTT]");
            connectMqtt();
        });
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     */
    private void connectMqtt() {
        client.connect(emqxProperties.getMqttPort(), emqxProperties.getMqttHost())
                .onSuccess(connAck -> {
                    log.info("[connectMqtt][MQTT客户端连接成功]");
                    subscribeToTopics();
                })
                .onFailure(err -> {
                    log.error("[connectMqtt][连接 MQTT Broker 失败]", err);
                    reconnectWithDelay();
                });
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        String[] topics = emqxProperties.getMqttTopics().split(",");
        for (String topic : topics) {
            client.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value())
                    .onSuccess(v -> log.info("[subscribeToTopics][成功订阅主题: {}]", topic))
                    .onFailure(err -> log.error("[subscribeToTopics][订阅主题失败: {}]", topic, err));
        }
        log.info("[subscribeToTopics][开始订阅设备上行消息主题]");
    }

    /**
     * 停止所有
     */
    public void stop() {
        log.info("[stop][开始关闭]");
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
            if (vertx != null) {
                vertx.close()
                        .toCompletionStage()
                        .toCompletableFuture()
                        .join();
            }
            log.info("[stop][关闭完成]");
        } catch (Exception e) {
            log.error("[stop][关闭异常]", e);
            throw new RuntimeException(e);
        }
    }
}
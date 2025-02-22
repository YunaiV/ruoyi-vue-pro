package cn.iocoder.yudao.module.iot.plugin.emqx.upstream;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.config.IotPluginCommonProperties;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import cn.iocoder.yudao.module.iot.plugin.emqx.config.IotPluginEmqxProperties;
import cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router.IotDeviceAuthVertxHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    private final Vertx vertx;
    private final HttpServer server;
    private final MqttClient client;
    private final IotPluginEmqxProperties emqxProperties;
    private final IotDeviceUpstreamApi deviceUpstreamApi;

    public IotDeviceUpstreamServer(IotPluginCommonProperties commonProperties,
                                   IotPluginEmqxProperties emqxProperties,
                                   IotDeviceUpstreamApi deviceUpstreamApi,
                                   IotDeviceDownstreamServer deviceDownstreamServer) {
        this.emqxProperties = emqxProperties;
        this.deviceUpstreamApi = deviceUpstreamApi;
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
                .setClientId("yudao-iot-server-" + UUID.randomUUID())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword())
                .setSsl(emqxProperties.isMqttSsl());
        client = MqttClient.create(vertx, options);
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
            // 等待 5 秒后重连,避免频繁重连
            vertx.setTimer(5000, id -> {
                log.info("[closeHandler][开始重新连接 MQTT]");
                connectMqtt();
            });
        });

        // 4. 设置 MQTT 消息处理器
        client.publishHandler(message -> {
            String topic = message.topicName();
            String payload = message.payload().toString();
            log.info("[messageHandler][接收到消息][topic: {}][payload: {}]", topic, payload);

            try {
                // 4.1 处理设备属性上报消息: /{productKey}/{deviceName}/event/property/post
                if (topic.contains("/event/property/post")) {
                    // 4.2 解析消息内容
                    JSONObject jsonObject = JSONUtil.parseObj(payload);
                    String requestId = jsonObject.getStr("id");
                    Long timestamp = jsonObject.getLong("timestamp");

                    // 4.3 从 topic 中解析设备标识
                    String[] topicParts = topic.split("/");
                    String productKey = topicParts[1];
                    String deviceName = topicParts[2];

                    // 4.4 构建设备属性上报请求对象
                    IotDevicePropertyReportReqDTO devicePropertyReportReqDTO = ((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO()
                            .setRequestId(requestId)
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(productKey).setDeviceName(deviceName))
                            .setProperties(jsonObject.getJSONObject("params"));

                    // 4.5 调用上游 API 处理设备上报数据
                    deviceUpstreamApi.reportDeviceProperty(devicePropertyReportReqDTO);
                    log.info("[messageHandler][处理设备上行消息成功][topic: {}][devicePropertyReportReqDTO: {}]",
                            topic, JSONUtil.toJsonStr(devicePropertyReportReqDTO));
                }
            } catch (Exception e) {
                log.error("[messageHandler][处理消息失败][topic: {}][payload: {}]", topic, payload, e);
            }
        });
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     */
    private void connectMqtt() {
        // 连接 MQTT Broker
        client.connect(emqxProperties.getMqttPort(), emqxProperties.getMqttHost())
                .onSuccess(connAck -> {
                    log.info("[connectMqtt][MQTT客户端连接成功]");
                    // 连接成功后订阅主题
                    String mqttTopics = emqxProperties.getMqttTopics();
                    String[] topics = mqttTopics.split(",");
                    for (String topic : topics) {
                        client.subscribe(topic, 1)
                                .onSuccess(v -> log.info("[connectMqtt][成功订阅主题: {}]", topic))
                                .onFailure(err -> log.error("[connectMqtt][订阅主题失败: {}]", topic, err));
                    }
                    log.info("[connectMqtt][开始订阅设备上行消息主题]");
                })
                .onFailure(err -> {
                    log.error("[connectMqtt][连接 MQTT Broker 失败]", err);
                    // 连接失败后，等待 5 秒重试
                    vertx.setTimer(5000, id -> {
                        log.info("[connectMqtt][准备重新连接 MQTT]");
                        connectMqtt();
                    });
                });
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

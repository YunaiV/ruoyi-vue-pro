package cn.iocoder.yudao.module.iot.plugin.emqx.upstream;

import cn.hutool.core.util.IdUtil;
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

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 * <p>
 * 协议：HTTP、MQTT
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    private static final String PROPERTY_POST_TOPIC = "/event/property/post";
    private static final int RECONNECT_DELAY = 5000; // 重连延迟时间(毫秒)
    private static final int QOS_LEVEL = 1;

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
                .setClientId("yudao-iot-server-" + IdUtil.fastSimpleUUID())
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
            reconnectWithDelay();
        });

        // 4. 设置 MQTT 消息处理器
        setupMessageHandler();
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        client.publishHandler(message -> {
            String topic = message.topicName();
            String payload = message.payload().toString();
            log.info("[messageHandler][接收到消息][topic: {}][payload: {}]", topic, payload);

            try {
                handleMessage(topic, payload);
            } catch (Exception e) {
                log.error("[messageHandler][处理消息失败][topic: {}][payload: {}]", topic, payload, e);
            }
        });
    }

    /**
     * 处理 MQTT 消息
     */
    private void handleMessage(String topic, String payload) {
        // 处理设备属性上报消息
        if (topic.contains(PROPERTY_POST_TOPIC)) {
            handlePropertyPost(topic, payload);
        }
    }

    /**
     * 处理设备属性上报
     */
    private void handlePropertyPost(String topic, String payload) {
        // 解析消息内容
        JSONObject jsonObject = JSONUtil.parseObj(payload);
        String[] topicParts = topic.split("/");

        // 构建设备属性上报请求对象
        IotDevicePropertyReportReqDTO reportReqDTO = buildPropertyReportDTO(
                jsonObject,
                topicParts[1], // productKey
                topicParts[2] // deviceName
        );

        // 调用上游 API 处理设备上报数据
        deviceUpstreamApi.reportDeviceProperty(reportReqDTO);
        log.info("[handlePropertyPost][处理设备上行消息成功][topic: {}][reportReqDTO: {}]",
                topic, JSONUtil.toJsonStr(reportReqDTO));
    }

    /**
     * 构建设备属性上报请求对象
     */
    private IotDevicePropertyReportReqDTO buildPropertyReportDTO(JSONObject jsonObject,
                                                                 String productKey,
                                                                 String deviceName) {
        return ((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO()
                .setRequestId(jsonObject.getStr("id"))
                .setProcessId(IotPluginCommonUtils.getProcessId())
                .setReportTime(LocalDateTime.now())
                .setProductKey(productKey)
                .setDeviceName(deviceName))
                .setProperties(jsonObject.getJSONObject("params"));
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
            client.subscribe(topic, QOS_LEVEL)
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
package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttAuthRouter;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttUpstreamRouter;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 MQTT 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttUpstreamProtocol {

    /**
     * 默认 QoS 级别
     */
    private static final MqttQoS DEFAULT_QOS = MqttQoS.AT_LEAST_ONCE;

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    private Vertx vertx;
    private MqttClient mqttClient;
    private IotMqttUpstreamRouter messageRouter;
    private IotMqttAuthRouter authRouter;
        private IotDeviceMessageProducer deviceMessageProducer;

    /**
     * 服务运行状态标志
     */
    private volatile boolean isRunning = false;

    @PostConstruct
    public void start() {
        if (isRunning) {
            log.warn("[start][MQTT 协议服务已经在运行中，请勿重复启动]");
            return;
        }
        log.info("[start][开始启动 MQTT 协议服务]");

        // 初始化组件
        this.vertx = Vertx.vertx();
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
        this.messageRouter = new IotMqttUpstreamRouter(this);
        this.authRouter = new IotMqttAuthRouter(this);

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

    @PreDestroy
    public void stop() {
        if (!isRunning) {
            log.warn("[stop][MQTT 协议服务已经停止，无需再次停止]");
            return;
        }
        log.info("[stop][开始停止 MQTT 协议服务]");

        // 1. 取消 MQTT 主题订阅
        if (mqttClient != null && mqttClient.isConnected()) {
            List<String> topicList = emqxProperties.getMqttTopics();
            if (CollUtil.isNotEmpty(topicList)) {
                for (String topic : topicList) {
                    try {
                        mqttClient.unsubscribe(topic);
                        log.debug("[stop][取消订阅主题: {}]", topic);
                    } catch (Exception e) {
                        log.warn("[stop][取消订阅主题异常: {}]", topic, e);
                    }
                }
            }
        }

        // 2. 关闭 MQTT 客户端
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (Exception e) {
            log.warn("[stop][关闭 MQTT 客户端异常]", e);
        }

        // 3. 关闭 Vertx
        try {
            if (vertx != null) {
                vertx.close();
            }
        } catch (Exception e) {
            log.warn("[stop][关闭 Vertx 异常]", e);
        }

        // 4. 更新状态
        isRunning = false;
        log.info("[stop][MQTT 协议服务已停止]");
    }

    /**
     * 连接 MQTT Broker 并订阅主题
     */
    private void connectMqtt() {
        // 检查必要的 MQTT 配置
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();
        if (StrUtil.isBlank(host)) {
            String msg = "[connectMqtt][MQTT Host 为空，无法连接]";
            log.error(msg);
            return;
        }
        if (port == null) {
            log.warn("[connectMqtt][MQTT Port 为 null，使用默认端口 1883]");
            port = 1883; // 默认 MQTT 端口
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
            isRunning = true;
            log.info("[connectMqtt][MQTT 协议服务启动完成]");
        } catch (Exception e) {
            log.error("[connectMqtt][MQTT 协议服务启动失败]", e);
        }
    }

    /**
     * 设置 MQTT 消息处理器
     */
    private void setupMessageHandler() {
        mqttClient.publishHandler(messageRouter::route);
        log.debug("[setupMessageHandler][MQTT 消息处理器设置完成]");
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        List<String> topicList = emqxProperties.getMqttTopics();
        if (CollUtil.isEmpty(topicList)) {
            log.warn("[subscribeToTopics][未配置 MQTT 主题，使用默认主题]");
            topicList = List.of("/sys/#"); // 默认订阅所有系统主题
        }

        for (String topic : topicList) {
            if (StrUtil.isBlank(topic)) {
                log.warn("[subscribeToTopics][跳过空主题]");
                continue;
            }

            mqttClient.subscribe(topic, DEFAULT_QOS.value())
                    .onSuccess(ack -> log.info("[subscribeToTopics][订阅主题成功: {}]", topic))
                    .onFailure(err -> log.error("[subscribeToTopics][订阅主题失败: {}]", topic, err));
        }
    }

    /**
     * 重连 MQTT 客户端
     */
    private void reconnectWithDelay() {
        if (!isRunning) {
            log.info("[reconnectWithDelay][服务已停止，不再尝试重连]");
            return;
        }

        // 默认重连延迟 5 秒
        int reconnectDelayMs = 5000;
        vertx.setTimer(reconnectDelayMs, id -> {
            log.info("[reconnectWithDelay][开始重新连接 MQTT]");
            connectMqtt();
        });
    }

    /**
     * 发布消息到 MQTT
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void publishMessage(String topic, String payload) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[publishMessage][MQTT 客户端未连接，无法发送消息][topic: {}]", topic);
            return;
        }

        mqttClient.publish(topic, io.vertx.core.buffer.Buffer.buffer(payload), DEFAULT_QOS, false, false)
                .onSuccess(v -> log.debug("[publishMessage][发送消息成功][topic: {}]", topic))
                .onFailure(err -> log.error("[publishMessage][发送消息失败][topic: {}]", topic, err));
    }

    /**
     * 获取服务器 ID
     *
     * @return 服务器 ID
     */
    public String getServerId() {
        return IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
    }

    /**
     * 获取 MQTT 客户端
     *
     * @return MQTT 客户端
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }

    /**
     * 获取认证路由器
     *
     * @return 认证路由器
     */
    public IotMqttAuthRouter getAuthRouter() {
        return authRouter;
    }

}
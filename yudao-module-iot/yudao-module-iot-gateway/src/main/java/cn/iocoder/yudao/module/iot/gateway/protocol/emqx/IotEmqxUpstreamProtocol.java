package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router.IotEmqxUpstreamHandler;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IoT 网关 EMQX 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxUpstreamProtocol {

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    private volatile boolean isRunning = false;

    private Vertx vertx;

    @Getter
    private final String serverId;

    private MqttClient mqttClient;

    private IotEmqxUpstreamHandler upstreamHandler;

    public IotEmqxUpstreamProtocol(IotGatewayProperties.EmqxProperties emqxProperties) {
        this.emqxProperties = emqxProperties;
        this.serverId = IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
    }

    @PostConstruct
    public void start() {
        if (isRunning) {
            return;
        }

        try {
            // 1. 初始化 Vertx 实例
            this.vertx = Vertx.vertx();

            // 2. 启动 MQTT 客户端
            startMqttClient();

            // 3. 标记服务为运行状态
            isRunning = true;
            log.info("[start][IoT 网关 EMQX 协议启动成功]");
        } catch (Exception e) {
            log.error("[start][IoT 网关 EMQX 协议服务启动失败，应用将关闭]", e);
            stop();

            // 异步关闭应用，避免阻塞当前线程
            // TODO @haohao：是不是阻塞，也没关系哈？
            new Thread(() -> {
                try {
                    // TODO @haohao：可以考虑用 ThreadUtil.sleep 更简洁
                    Thread.sleep(1000); // 等待 1 秒让日志输出完成
                    log.error("[start][由于 MQTT 连接失败，正在关闭应用]");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.exit(1); // 直接关闭 JVM
                }
            }).start();

            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (!isRunning) {
            return;
        }

        // 1. 停止 MQTT 客户端
        stopMqttClient();

        // 2. 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close();
            } catch (Exception e) {
                log.warn("[stop][关闭 Vertx 实例失败]", e);
            }
        }

        // 3. 标记服务为停止状态
        isRunning = false;
        log.info("[stop][IoT 网关 MQTT 协议服务已停止]");
    }

    /**
     * 启动 MQTT 客户端
     */
    private void startMqttClient() {
        try {
            // 1. 初始化消息处理器
            this.upstreamHandler = new IotEmqxUpstreamHandler(this);

            // 2. 创建 MQTT 客户端，连接 MQTT Broker
            boolean connected = connectMqttSync();
            if (!connected) {
                throw new RuntimeException("首次连接 MQTT Broker 失败");
            }
        } catch (Exception e) {
            log.error("[startMqttClient][MQTT 客户端启动失败]", e);
            throw new RuntimeException("MQTT 客户端启动失败", e);
        }
    }

    /**
     * 连接 MQTT Broker
     *
     * @param isReconnect 是否为重连
     * @param isSync      是否同步等待连接结果
     * @return 当 isSync 为 true 时返回连接是否成功，否则返回 null
     */
    // TODO @haohao：是不是不用结果；结束后，直接判断 this.mqttClient.isConnected()；
    private Boolean connectMqtt(boolean isReconnect, boolean isSync) {
        // TODO @haohao：这块代码，是不是放到
        String host = emqxProperties.getMqttHost();
        Integer port = emqxProperties.getMqttPort();

        // 2.3.1. 如果是重连，则需要重新创建 MQTT 客户端
        // TODO @hoahao：疑惑，为啥这里要重新创建对象呀？另外，创建是不是拿到 reconnectWithDelay 会更合适？这样和 startMqttClient 一样呢；
        if (isReconnect) {
            createMqttClient();
        }

        // 2.3.2. 连接 MQTT Broker
        CountDownLatch latch = isSync ? new CountDownLatch(1) : null;
        AtomicBoolean success = isSync
                ? new AtomicBoolean(false)
                : null;
        mqttClient.connect(port, host, connectResult -> {
            if (connectResult.succeeded()) {
                if (isReconnect) {
                    log.info("[connectMqtt][MQTT 客户端重连成功]");
                } else {
                    log.info("[connectMqtt][MQTT 客户端连接成功, host: {}, port: {}]", host, port);
                }
                // 设置处理器和订阅主题
                setupMqttHandlers();
                subscribeToTopics();
                if (success != null) {
                    success.set(true);
                }
            } else {
                log.error("[connectMqtt][连接 MQTT Broker 失败, host: {}, port: {}]", host, port, connectResult.cause());
                if (isReconnect) {
                    reconnectWithDelay();
                } else {
                    log.error("[connectMqtt][首次连接失败，连接终止]");
                }
            }

            if (latch != null) {
                latch.countDown();
            }
        });

        // 2.3.3. 如果需要同步等待连接结果，则等待
        if (isSync) {
            try {
                latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[connectMqtt][等待连接结果被中断]", e);
            }
            return success.get();
        }

        return null;
    }

    /**
     * 同步连接 MQTT Broker
     *
     * @return 是否连接成功
     */
    private boolean connectMqttSync() {
        Boolean result = connectMqtt(false, true);
        return result != null ? result : false;
    }

    /**
     * 停止 MQTT 客户端
     */
    private void stopMqttClient() {
        // 1. 取消订阅所有主题
        if (mqttClient != null && mqttClient.isConnected()) {
            List<String> topicList = emqxProperties.getMqttTopics();
            for (String topic : topicList) {
                try {
                    mqttClient.unsubscribe(topic);
                } catch (Exception e) {
                    log.warn("[stopMqttClient][取消订阅主题({})异常]", topic, e);
                }
            }
        }
        // 2. 断开 MQTT 客户端连接
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (Exception e) {
                log.warn("[stopMqttClient][关闭 MQTT 客户端异常]", e);
            }
        }
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
        // 1.1 设置断开重连监听器
        mqttClient.closeHandler(closeEvent -> {
            if (!isRunning) {
                return;
            }
            log.warn("[closeHandler][MQTT 连接已断开, 准备重连]");
            reconnectWithDelay();
        });
        // 1.2 设置异常处理器
        mqttClient.exceptionHandler(exception -> log.error("[exceptionHandler][MQTT 客户端异常]", exception));

        // 2. 设置消息处理器
        mqttClient.publishHandler(upstreamHandler::handle);
    }

    /**
     * 订阅设备上行消息主题
     */
    private void subscribeToTopics() {
        // 1. 校验 MQTT 客户端是否连接
        List<String> topicList = emqxProperties.getMqttTopics();
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[subscribeToTopics][MQTT 客户端未连接, 跳过订阅]");
            return;
        }

        // 2. 批量订阅所有主题
        Map<String, Integer> topics = new HashMap<>();
        int qos = emqxProperties.getMqttQos();
        for (String topic : topicList) {
            topics.put(topic, qos);
        }
        mqttClient.subscribe(topics, subscribeResult -> {
            if (subscribeResult.succeeded()) {
                log.info("[subscribeToTopics][订阅主题成功, 共 {} 个主题]", topicList.size());
            } else {
                log.error("[subscribeToTopics][订阅主题失败, 共 {} 个主题, 原因: {}]",
                        topicList.size(), subscribeResult.cause().getMessage(), subscribeResult.cause());
            }
        });
    }

    /**
     * 延迟重连
     */
    private void reconnectWithDelay() {
        long delay = emqxProperties.getReconnectDelayMs();
        vertx.setTimer(delay, timerId -> {
            if (!isRunning) {
                return;
            }
            if (mqttClient != null && mqttClient.isConnected()) {
                return;
            }
            log.info("[reconnectWithDelay][开始重连 MQTT Broker]");
            try {
                connectMqtt(true, false);
            } catch (Exception e) {
                log.error("[reconnectWithDelay][重连失败, 将继续尝试]", e);
            }
            // TODO @haohao：是不是把如果连接失败，放到这里处理？继续发起。。。这样，connect 逻辑更简单纯粹；1）首次连接，失败就退出；2）重新连接，如果失败，继续重试！
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
            log.warn("[publishMessage][MQTT 客户端未连接, 无法发布消息]");
            return;
        }
        MqttQoS qos = MqttQoS.valueOf(emqxProperties.getMqttQos());
        mqttClient.publish(topic, Buffer.buffer(payload), qos, false, false);
    }

}
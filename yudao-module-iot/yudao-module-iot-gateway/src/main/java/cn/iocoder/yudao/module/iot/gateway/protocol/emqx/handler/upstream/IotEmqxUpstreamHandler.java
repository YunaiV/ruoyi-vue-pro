package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxUpstreamHandler {

    /**
     * 标准消息、透传上报最多排队的消息数：超过时丢弃
     */
    private static final int MAX_PENDING = 1024;

    private final IotDeviceMessageService deviceMessageService;

    private final String serverId;

    /**
     * 标准消息的 worker 队列（有序、有界）：所有设备共用一个 MQTT Client，与原来在 event loop 上一样按收到的顺序逐条处理
     */
    private IotWorkerTaskQueue queue;
    /**
     * 透传上报的 worker 队列（无序、有界）：执行脚本耗时较长，消息之间并行
     */
    private IotWorkerTaskQueue rawQueue;

    public IotEmqxUpstreamHandler(String serverId) {
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.serverId = serverId;
    }

    /**
     * 处理 MQTT 发布消息
     */
    public void handle(MqttPublishMessage mqttMessage) {
        log.debug("[handle][收到 MQTT 消息, topic: {}, payload: {}]", mqttMessage.topicName(), mqttMessage.payload());
        String topic = mqttMessage.topicName();
        byte[] payload = mqttMessage.payload().getBytes();
        // 0. 网关自己发布的透传下发（raw/push）被自己订阅到：直接忽略（回环）
        if (IotMqttTopicUtils.isRawPushTopic(topic)) {
            return;
        }
        try {
            // 1.1 解析主题，一次性获取所有信息
            String[] topicParts = topic.split("/");
            String productKey = ArrayUtil.get(topicParts, 2);
            String deviceName = ArrayUtil.get(topicParts, 3);
            if (topicParts.length < 4 || StrUtil.hasBlank(productKey, deviceName)) {
                log.warn("[handle][topic({}) 格式不正确，无法解析有效的 productKey 和 deviceName]", topic);
                return;
            }
            // 1.2 透传上报：身份来自 Topic（由 EMQX ACL 保证设备只能发布自己的 Topic），跳过反序列化
            if (IotMqttTopicUtils.isRawPostTopic(topic)) {
                handleRawRequest(topic, productKey, deviceName, payload);
                return;
            }

            // 2. 标准消息：反序列化（获取设备）、发送总线会阻塞，交给 worker 按收到的顺序执行
            getQueue().submit(() -> {
                handleStandardRequest(topic, productKey, deviceName, payload);
                return null;
            }).onFailure(e -> log.error("[handle][topic({}) payload({}) 处理异常]", topic, new String(payload), e));
        } catch (Exception e) {
            log.error("[handle][topic({}) payload({}) 处理异常]", topic, new String(payload), e);
        }
    }

    /**
     * 处理标准消息（worker 线程）：反序列化后发送
     *
     * @param topic      主题
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param payload    消息内容
     */
    private void handleStandardRequest(String topic, String productKey, String deviceName, byte[] payload) {
        // 1.1 反序列化消息
        IotDeviceMessage message = deviceMessageService.deserializeDeviceMessage(payload, productKey, deviceName);
        if (message == null) {
            log.warn("[handleStandardRequest][topic({}) payload({}) 消息解码失败]", topic, new String(payload));
            return;
        }
        // 1.2 标准化回复消息的 method（MQTT 协议中，设备回复消息的 method 会携带 _reply 后缀）
        IotMqttTopicUtils.normalizeReplyMethod(message);

        // 2. 发送消息到队列
        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
    }

    /**
     * 获取标准消息的 worker 队列：按当前（MQTT Client 所在的）context 创建；协议重启后 context 变化时重建
     */
    private IotWorkerTaskQueue getQueue() {
        Context context = Vertx.currentContext();
        if (queue == null || queue.getContext() != context) {
            queue = new IotWorkerTaskQueue(context, true, MAX_PENDING);
        }
        return queue;
    }

    /**
     * 处理透传上报：原始字节按产品的数据解析脚本 decode 后发送；会阻塞，交给 worker 执行（不同消息之间并行）
     *
     * @param topic      主题
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param payload    原始字节
     */
    private void handleRawRequest(String topic, String productKey, String deviceName, byte[] payload) {
        Context context = Vertx.currentContext();
        if (rawQueue == null || rawQueue.getContext() != context) { // 协议重启后 MQTT Client 换了 Vertx：按新的 context 重建
            rawQueue = new IotWorkerTaskQueue(context, false, MAX_PENDING);
        }
        // 截止时间从收到（入队之前）开始计算
        long receivedNanos = System.nanoTime();
        rawQueue.submit(() -> deviceMessageService.sendRawMessage(payload, productKey, deviceName, serverId,
                IotProtocolTypeEnum.EMQX, receivedNanos)).onFailure(e ->
                log.warn("[handleRawRequest][topic({}) 透传上报处理失败，数据长度: {} 字节，错误: {}]",
                        topic, payload.length, e.getMessage()));
    }

}

package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * IoT 网关 MQTT 协议的【上行】处理器
 * <p>
 * 处理设备上行消息，包括事件上报、属性上报、服务调用响应等
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamHandler {

    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;

    public IotMqttUpstreamHandler() {
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    /**
     * 处理 MQTT 发布消息
     */
    public void handle(MqttPublishMessage message) {
        String topic = message.topicName();
        String payload = message.payload().toString(StandardCharsets.UTF_8);

        if (StrUtil.isBlank(topic)) {
            log.warn("[handle][主题为空，忽略消息]");
            return;
        }

        if (StrUtil.isBlank(payload)) {
            log.warn("[handle][消息内容为空][topic: {}]", topic);
            return;
        }

        log.debug("[handle][收到 MQTT 消息][topic: {}]", topic);
        handle(topic, payload);
    }

    /**
     * 处理 MQTT 消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void handle(String topic, String payload) {
        try {
            // 1. 识别并验证消息类型
            String messageType = getMessageType(topic);
            if (messageType == null) {
                log.warn("[handle][未知的消息类型][topic: {}]", topic);
                return;
            }

            // 2. 处理消息
            processMessage(topic, payload, messageType);

        } catch (Exception e) {
            log.error("[handle][处理消息失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 处理消息的统一逻辑
     */
    private void processMessage(String topic, String payload, String messageType) {
        log.info("[processMessage][接收到{}][topic: {}]", messageType, topic);

        // 解析主题获取设备信息
        String[] topicParts = parseTopic(topic);
        if (topicParts == null) {
            return;
        }

        String productKey = topicParts[2];
        String deviceName = topicParts[3];

        // 解码消息
        byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
        IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                messageBytes, productKey, deviceName);

        // 发送消息到队列
        deviceMessageProducer.sendDeviceMessage(message);

        // 记录成功日志
        log.info("[processMessage][处理{}成功][topic: {}]", messageType, topic);
    }

    /**
     * 识别消息类型
     *
     * @param topic 主题
     * @return 消息类型描述，如果不支持返回 null
     */
    private String getMessageType(String topic) {
        // 设备事件上报: /sys/{productKey}/{deviceName}/thing/event/{eventIdentifier}/post
        if (topic.contains("/thing/event/") && topic.endsWith("/post")) {
            return "设备事件上报";
        }

        // 设备属性操作: /sys/{productKey}/{deviceName}/thing/property/post
        // 或属性响应: /sys/{productKey}/{deviceName}/thing/service/property/set_reply
        if (topic.endsWith("/thing/property/post") ||
                topic.contains("/thing/service/property/set") ||
                topic.contains("/thing/service/property/get")) {
            return "设备属性操作";
        }

        // 设备服务调用: /sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}
        if (topic.contains("/thing/service/") && !topic.contains("/property/")) {
            return "设备服务调用";
        }

        // 不支持的消息类型
        return null;
    }

    /**
     * 解析主题，获取主题各部分
     *
     * @param topic 主题
     * @return 主题各部分数组，如果解析失败返回 null
     */
    private String[] parseTopic(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length < 7) {
            log.warn("[parseTopic][主题格式不正确][topic: {}]", topic);
            return null;
        }
        return topicParts;
    }
}
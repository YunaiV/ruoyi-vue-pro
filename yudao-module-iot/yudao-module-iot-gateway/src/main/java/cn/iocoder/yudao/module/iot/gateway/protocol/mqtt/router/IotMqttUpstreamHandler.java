package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * IoT 网关 MQTT 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamHandler {

    private final IotDeviceMessageService deviceMessageService;
    private final String serverId;

    public IotMqttUpstreamHandler(IotMqttUpstreamProtocol protocol) {
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.serverId = protocol.getServerId();
    }

    /**
     * 处理 MQTT 发布消息
     */
    public void handle(MqttPublishMessage message) {
        String topic = message.topicName();
        byte[] payload = message.payload().getBytes();

        log.debug("[handle][收到 MQTT 消息, topic: {}]", topic);

        try {
            // 1. 前置校验
            if (StrUtil.isBlank(topic)) {
                log.warn("[handle][主题为空, 忽略消息]");
                return;
            }
            // 注意：payload 可以为空

            // 2. 识别并验证消息类型
            String messageType = getMessageType(topic);
            Assert.notNull(messageType, String.format("未知的消息类型, topic(%s)", topic));
            log.debug("[handle][接收到上行消息({}), topic: {}]", messageType, topic);

            // 3. 解析主题，获取 productKey 和 deviceName
            String[] topicParts = topic.split("/");
            if (topicParts.length < 4) {
                log.warn("[handle][主题格式不正确，无法解析 productKey 和 deviceName][topic: {}]", topic);
                return;
            }
            String productKey = topicParts[2];
            String deviceName = topicParts[3];
            if (StrUtil.isAllBlank(productKey, deviceName)) {
                log.warn("[handle][主题中 productKey 或 deviceName 为空][topic: {}]", topic);
                return;
            }

            // 4. 解码消息
            IotDeviceMessage deviceMessage = deviceMessageService.decodeDeviceMessage(
                    payload, productKey, deviceName);
            if (deviceMessage == null) {
                log.warn("[handle][消息解码失败][topic: {}]", topic);
                return;
            }

            // 5. 发送消息到队列
            deviceMessageService.sendDeviceMessage(deviceMessage, productKey, deviceName, serverId);

            // 6. 记录成功日志
            log.debug("[handle][处理上行消息({})成功, topic: {}]", messageType, topic);
        } catch (Exception e) {
            log.error("[handle][处理 MQTT 消息失败][topic: {}][payload: {}]", topic, new String(payload), e);
        }
    }

    /**
     * 从主题中，获得消息类型
     *
     * @param topic 主题
     * @return 消息类型
     */
    private String getMessageType(String topic) {
        String[] topicParts = topic.split("/");
        // 约定：topic 第 4 个部分开始为消息类型
        // 例如：/sys/{productKey}/{deviceName}/thing/property/post ->
        // thing/property/post
        if (topicParts.length > 4) {
            return String.join("/", Arrays.copyOfRange(topicParts, 4, topicParts.length));
        }
        return topicParts[topicParts.length - 1];
    }

}
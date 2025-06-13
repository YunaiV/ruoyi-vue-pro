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
    public void handle(MqttPublishMessage mqttMessage) {
        String topic = mqttMessage.topicName();
        byte[] payload = mqttMessage.payload().getBytes();
        try {
            // 1. 前置校验
            if (StrUtil.isBlank(topic)) {
                log.warn("[handle][主题为空, 忽略消息]");
                return;
            }

            // 2.1 识别并验证消息类型
            String messageType = getMessageType(topic);
            // TODO @haohao：可以使用 hutool 的，它的字符串拼接更简单；
            Assert.notNull(messageType, String.format("未知的消息类型, topic(%s)", topic));
            // 2.2 解析主题，获取 productKey 和 deviceName
            // TODO @haohao：体感 getMessageType 和下面，都 split；是不是一次就 ok 拉；1）split 掉；2）2、3 位置是 productKey、deviceName；3）4 开始还是 method
            String[] topicParts = topic.split("/");
            if (topicParts.length < 4) {
                log.warn("[handle][topic({}) 格式不正确，无法解析 productKey 和 deviceName]", topic);
                return;
            }
            String productKey = topicParts[2];
            String deviceName = topicParts[3];
            // TODO @haohao：是不是要判断，部分为空，就不行呀；
            if (StrUtil.isAllBlank(productKey, deviceName)) {
                log.warn("[handle][topic({}) 格式不正确，productKey 和 deviceName 部分为空]", topic);
                return;
            }

            // 3. 解码消息
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[handle][topic({}) payload({}) 消息解码失败", topic, new String(payload));
                return;
            }

            // 4. 发送消息到队列
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        } catch (Exception e) {
            log.error("[handle][topic({}) payload({}) 处理异常]", topic, new String(payload), e);
        }
    }

    // TODO @haohao：是不是 getMethodFromTopic？
    /**
     * 从主题中，获得消息类型
     *
     * @param topic 主题
     * @return 消息类型
     */
    private String getMessageType(String topic) {
        String[] topicParts = topic.split("/");
        // 约定：topic 第 4 个部分开始为消息类型
        // 例如：/sys/{productKey}/{deviceName}/thing/property/post -> thing/property/post
        if (topicParts.length > 4) {
            // TODO @haohao：是不是 subString 前 3 个，性能更好；
            return String.join("/", Arrays.copyOfRange(topicParts, 4, topicParts.length));
        }
        return topicParts[topicParts.length - 1];
    }

}
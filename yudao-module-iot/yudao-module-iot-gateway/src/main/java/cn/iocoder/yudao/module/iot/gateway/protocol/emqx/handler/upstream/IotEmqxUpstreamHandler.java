package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxUpstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final String serverId;

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
        try {
            // 1. 解析主题，一次性获取所有信息
            String[] topicParts = topic.split("/");
            String productKey = ArrayUtil.get(topicParts, 2);
            String deviceName = ArrayUtil.get(topicParts, 3);
            if (topicParts.length < 4 || StrUtil.hasBlank(productKey, deviceName)) {
                log.warn("[handle][topic({}) 格式不正确，无法解析有效的 productKey 和 deviceName]", topic);
                return;
            }

            // 2.1 反序列化消息
            IotDeviceMessage message = deviceMessageService.deserializeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[handle][topic({}) payload({}) 消息解码失败]", topic, new String(payload));
                return;
            }
            // 2.2 标准化回复消息的 method（MQTT 协议中，设备回复消息的 method 会携带 _reply 后缀）
            IotMqttTopicUtils.normalizeReplyMethod(message);

            // 3. 发送消息到队列
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        } catch (Exception e) {
            log.error("[handle][topic({}) payload({}) 处理异常]", topic, new String(payload), e);
        }
    }

}

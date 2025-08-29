package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
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

    public IotEmqxUpstreamHandler(IotEmqxUpstreamProtocol protocol) {
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.serverId = protocol.getServerId();
    }

    /**
     * 处理 MQTT 发布消息
     */
    public void handle(MqttPublishMessage mqttMessage) {
        log.info("[handle][收到 MQTT 消息, topic: {}, payload: {}]", mqttMessage.topicName(), mqttMessage.payload());
        String topic = mqttMessage.topicName();
        byte[] payload = mqttMessage.payload().getBytes();
        try {
            // 1. 解析主题，一次性获取所有信息
            String[] topicParts = topic.split("/");
            if (topicParts.length < 4 || StrUtil.hasBlank(topicParts[2], topicParts[3])) {
                log.warn("[handle][topic({}) 格式不正确，无法解析有效的 productKey 和 deviceName]", topic);
                return;
            }

            String productKey = topicParts[2];
            String deviceName = topicParts[3];

            // 3. 解码消息
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[handle][topic({}) payload({}) 消息解码失败]", topic, new String(payload));
                return;
            }

            // 4. 发送消息到队列
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        } catch (Exception e) {
            log.error("[handle][topic({}) payload({}) 处理异常]", topic, new String(payload), e);
        }
    }

}
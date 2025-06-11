package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * IoT 网关 MQTT 事件处理器
 * <p>
 * 处理设备事件相关的 MQTT 消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttEventHandler extends IotMqttAbstractHandler {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;

    @Override
    public void handle(String topic, String payload) {
        try {
            log.info("[handle][接收到设备事件上报][topic: {}]", topic);

            // 解析消息内容
            String[] topicParts = parseTopic(topic);
            if (topicParts == null) {
                return;
            }

            // 构建设备消息
            String productKey = topicParts[2];
            String deviceName = topicParts[3];
            String eventIdentifier = getEventIdentifier(topicParts, topic);
            if (eventIdentifier == null) {
                return;
            }

            // 使用 IotDeviceMessageService 解码消息
            byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                    messageBytes, productKey, deviceName);

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handle][处理设备事件上报成功][topic: {}]", topic);

            // 发送响应消息
            String method = "thing.event." + eventIdentifier + ".post";
            sendResponse(topic, JSONUtil.parseObj(payload), method);
        } catch (Exception e) {
            log.error("[handle][处理设备事件上报失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 从主题部分中获取事件标识符
     *
     * @param topicParts 主题各部分
     * @param topic      原始主题，用于日志
     * @return 事件标识符，如果获取失败返回 null
     */
    private String getEventIdentifier(String[] topicParts, String topic) {
        try {
            return topicParts[6];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warn("[getEventIdentifier][无法从主题中获取事件标识符][topic: {}]", topic);
            return null;
        }
    }

    /**
     * 发送响应消息
     *
     * @param topic      原始主题
     * @param jsonObject 原始消息 JSON 对象
     * @param method     响应方法
     */
    private void sendResponse(String topic, JSONObject jsonObject, String method) {
        try {
            String replyTopic = IotDeviceTopicEnum.getReplyTopic(topic);

            // 构建响应消息
            JSONObject response = new JSONObject();
            response.set("id", jsonObject.getStr("id"));
            response.set("code", 200);
            response.set("method", method);
            response.set("data", new JSONObject());

            // 发送响应
            protocol.publishMessage(replyTopic, response.toString());
            log.debug("[sendResponse][发送响应消息成功][topic: {}]", replyTopic);
        } catch (Exception e) {
            log.error("[sendResponse][发送响应消息失败][topic: {}]", topic, e);
        }
    }

}
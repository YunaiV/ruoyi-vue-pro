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
 * IoT 网关 MQTT 属性处理器
 * <p>
 * 处理设备属性相关的 MQTT 消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttPropertyHandler extends IotMqttAbstractHandler {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;

    @Override
    public void handle(String topic, String payload) {
        if (topic.endsWith(IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic())) {
            // 属性上报
            handlePropertyPost(topic, payload);
        } else if (topic.contains(IotDeviceTopicEnum.PROPERTY_SET_TOPIC.getTopic())) {
            // 属性设置响应
            handlePropertySetReply(topic, payload);
        } else if (topic.contains(IotDeviceTopicEnum.PROPERTY_GET_TOPIC.getTopic())) {
            // 属性获取响应
            handlePropertyGetReply(topic, payload);
        } else {
            log.warn("[handle][未知的属性主题][topic: {}]", topic);
        }
    }

    /**
     * 处理设备属性上报消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    private void handlePropertyPost(String topic, String payload) {
        try {
            log.info("[handlePropertyPost][接收到设备属性上报][topic: {}]", topic);

            // 解析主题获取设备信息
            String[] topicParts = parseTopic(topic);
            if (topicParts == null) {
                return;
            }

            String productKey = topicParts[2];
            String deviceName = topicParts[3];

            // 使用 IotDeviceMessageService 解码消息
            byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                    messageBytes, productKey, deviceName);

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handlePropertyPost][处理设备属性上报成功][topic: {}]", topic);

            // 发送响应消息
            sendResponse(topic, JSONUtil.parseObj(payload), "thing.event.property.post");
        } catch (Exception e) {
            log.error("[handlePropertyPost][处理设备属性上报失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 处理属性设置响应消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    private void handlePropertySetReply(String topic, String payload) {
        try {
            log.info("[handlePropertySetReply][接收到属性设置响应][topic: {}]", topic);

            // 解析主题获取设备信息
            String[] topicParts = parseTopic(topic);
            if (topicParts == null) {
                return;
            }

            String productKey = topicParts[2];
            String deviceName = topicParts[3];

            // 使用 IotDeviceMessageService 解码消息
            byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                    messageBytes, productKey, deviceName);

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handlePropertySetReply][处理属性设置响应成功][topic: {}]", topic);
        } catch (Exception e) {
            log.error("[handlePropertySetReply][处理属性设置响应失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 处理属性获取响应消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    private void handlePropertyGetReply(String topic, String payload) {
        try {
            log.info("[handlePropertyGetReply][接收到属性获取响应][topic: {}]", topic);

            // 解析主题获取设备信息
            String[] topicParts = parseTopic(topic);
            if (topicParts == null) {
                return;
            }

            String productKey = topicParts[2];
            String deviceName = topicParts[3];

            // 使用 IotDeviceMessageService 解码消息
            byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                    messageBytes, productKey, deviceName);

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handlePropertyGetReply][处理属性获取响应成功][topic: {}]", topic);
        } catch (Exception e) {
            log.error("[handlePropertyGetReply][处理属性获取响应失败][topic: {}][payload: {}]", topic, payload, e);
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
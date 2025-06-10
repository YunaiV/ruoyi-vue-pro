package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.message.IotDeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * IoT 网关 MQTT 服务处理器
 * <p>
 * 处理设备服务调用相关的 MQTT 消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttServiceHandler extends IotMqttAbstractHandler {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;

    @Override
    public void handle(String topic, String payload) {
        try {
            log.info("[handle][接收到设备服务调用响应][topic: {}]", topic);

            // 解析消息内容
            String[] topicParts = parseTopic(topic);
            if (topicParts == null) {
                return;
            }

            // 构建设备消息
            String productKey = topicParts[2];
            String deviceName = topicParts[3];
            String serviceIdentifier = getServiceIdentifier(topicParts, topic);
            if (serviceIdentifier == null) {
                return;
            }

            // 使用 IotDeviceMessageService 解码消息
            byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                    messageBytes, productKey, deviceName, protocol.getServerId());

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handle][处理设备服务调用响应成功][topic: {}]", topic);

            // 发送响应消息
            String method = "thing.service." + serviceIdentifier;
            sendResponse(topic, JSONUtil.parseObj(payload), method);
        } catch (Exception e) {
            log.error("[handle][处理设备服务调用响应失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 从主题部分中获取服务标识符
     *
     * @param topicParts 主题各部分
     * @param topic      原始主题，用于日志
     * @return 服务标识符，如果获取失败返回 null
     */
    private String getServiceIdentifier(String[] topicParts, String topic) {
        try {
            // 服务主题格式：/sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}
            return topicParts[6];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warn("[getServiceIdentifier][无法从主题中获取服务标识符][topic: {}]", topic);
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
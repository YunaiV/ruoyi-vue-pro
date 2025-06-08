package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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

    @Override
    public void handle(String topic, String payload) {
        try {
            log.info("[handle][接收到设备服务调用响应][topic: {}]", topic);

            // 解析消息内容
            JSONObject jsonObject = JSONUtil.parseObj(payload);
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

            Map<String, Object> serviceData = parseServiceDataFromPayload(jsonObject);
            IotDeviceMessage message = IotDeviceMessage.of(productKey, deviceName, protocol.getServerId());
            // 设置服务消息类型和标识符
            message.setType("service");
            message.setIdentifier(serviceIdentifier);
            message.setData(serviceData);

            // 发送消息
            deviceMessageProducer.sendDeviceMessage(message);
            log.info("[handle][处理设备服务调用响应成功][topic: {}]", topic);

            // 发送响应消息
            String method = "thing.service." + serviceIdentifier;
            sendResponse(topic, jsonObject, method);
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
     * 从消息载荷解析服务数据
     *
     * @param jsonObject 消息 JSON 对象
     * @return 服务数据映射
     */
    private Map<String, Object> parseServiceDataFromPayload(JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        if (params == null) {
            log.warn("[parseServiceDataFromPayload][消息格式不正确，缺少 params 字段][jsonObject: {}]", jsonObject);
            return Map.of();
        }
        return params;
    }

    /**
     * 发送响应消息
     *
     * @param topic      原始主题
     * @param jsonObject 原始消息 JSON 对象
     * @param method     响应方法
     */
    private void sendResponse(String topic, JSONObject jsonObject, String method) {
        String replyTopic = IotDeviceTopicEnum.getReplyTopic(topic);

        // 构建响应消息
        JSONObject response = new JSONObject();
        response.set("id", jsonObject.getStr("id"));
        response.set("code", 200);
        response.set("method", method);
        response.set("data", new JSONObject());

        // 发送响应
        protocol.publishMessage(replyTopic, response.toString());
        log.debug("[sendResponse][发送响应消息][topic: {}]", replyTopic);
    }

}
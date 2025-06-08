package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotMqttUpstreamProtocol protocol;
    private final IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.info("[onMessage][接收到下行消息：{}]", message);

        try {
            // 根据消息类型处理不同的下行消息
            String messageType = message.getType();
            switch (messageType) {
                case "property":
                    handlePropertyMessage(message);
                    break;
                case "service":
                    handleServiceMessage(message);
                    break;
                case "config":
                    handleConfigMessage(message);
                    break;
                case "ota":
                    handleOtaMessage(message);
                    break;
                default:
                    log.warn("[onMessage][未知的消息类型：{}]", messageType);
                    break;
            }
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败：{}]", message, e);
        }
    }

    /**
     * 处理属性相关消息
     *
     * @param message 设备消息
     */
    private void handlePropertyMessage(IotDeviceMessage message) {
        String identifier = message.getIdentifier();
        String productKey = message.getProductKey();
        String deviceName = message.getDeviceName();

        if ("set".equals(identifier)) {
            // 属性设置
            String topic = IotDeviceTopicEnum.buildPropertySetTopic(productKey, deviceName);
            JSONObject payload = buildDownstreamPayload(message, "thing.service.property.set");
            protocol.publishMessage(topic, payload.toString());
            log.info("[handlePropertyMessage][发送属性设置消息][topic: {}]", topic);
        } else if ("get".equals(identifier)) {
            // 属性获取
            String topic = IotDeviceTopicEnum.buildPropertyGetTopic(productKey, deviceName);
            JSONObject payload = buildDownstreamPayload(message, "thing.service.property.get");
            protocol.publishMessage(topic, payload.toString());
            log.info("[handlePropertyMessage][发送属性获取消息][topic: {}]", topic);
        } else {
            log.warn("[handlePropertyMessage][未知的属性操作：{}]", identifier);
        }
    }

    /**
     * 处理服务调用消息
     *
     * @param message 设备消息
     */
    private void handleServiceMessage(IotDeviceMessage message) {
        String identifier = message.getIdentifier();
        String productKey = message.getProductKey();
        String deviceName = message.getDeviceName();

        String topic = IotDeviceTopicEnum.buildServiceTopic(productKey, deviceName, identifier);
        JSONObject payload = buildDownstreamPayload(message, "thing.service." + identifier);
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleServiceMessage][发送服务调用消息][topic: {}]", topic);
    }

    /**
     * 处理配置设置消息
     *
     * @param message 设备消息
     */
    private void handleConfigMessage(IotDeviceMessage message) {
        String productKey = message.getProductKey();
        String deviceName = message.getDeviceName();

        String topic = IotDeviceTopicEnum.buildConfigSetTopic(productKey, deviceName);
        JSONObject payload = buildDownstreamPayload(message, "thing.service.config.set");
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleConfigMessage][发送配置设置消息][topic: {}]", topic);
    }

    /**
     * 处理 OTA 升级消息
     *
     * @param message 设备消息
     */
    private void handleOtaMessage(IotDeviceMessage message) {
        String productKey = message.getProductKey();
        String deviceName = message.getDeviceName();

        String topic = IotDeviceTopicEnum.buildOtaUpgradeTopic(productKey, deviceName);
        JSONObject payload = buildDownstreamPayload(message, "thing.service.ota.upgrade");
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleOtaMessage][发送 OTA 升级消息][topic: {}]", topic);
    }

    /**
     * 构建下行消息载荷
     *
     * @param message 设备消息
     * @param method  方法名
     * @return JSON 载荷
     */
    private JSONObject buildDownstreamPayload(IotDeviceMessage message, String method) {
        JSONObject payload = new JSONObject();
        payload.set("id", message.getMessageId());
        payload.set("version", "1.0");
        payload.set("method", method);
        payload.set("params", message.getData());
        return payload;
    }

}
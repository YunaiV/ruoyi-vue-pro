package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceCacheService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
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

    @Resource
    private IotDeviceCacheService deviceCacheService;

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
            // 根据消息方法处理不同的下行消息
            String method = message.getMethod();
            if (method == null) {
                log.warn("[onMessage][消息方法为空]");
                return;
            }

            if (method.startsWith("thing.service.property.")) {
                handlePropertyMessage(message);
            } else if (method.startsWith("thing.service.") && !method.contains("property") && !method.contains("config")
                    && !method.contains("ota")) {
                handleServiceMessage(message);
            } else if (method.startsWith("thing.service.config.")) {
                handleConfigMessage(message);
            } else if (method.startsWith("thing.service.ota.")) {
                handleOtaMessage(message);
            } else {
                log.warn("[onMessage][未知的消息方法：{}]", method);
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
        String method = message.getMethod();

        // 通过 deviceId 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfoById(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handlePropertyMessage][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        String productKey = deviceInfo.getProductKey();
        String deviceName = deviceInfo.getDeviceName();

        if ("thing.service.property.set".equals(method)) {
            // 属性设置
            String topic = IotDeviceTopicEnum.buildPropertySetTopic(productKey, deviceName);
            JSONObject payload = buildDownstreamPayload(message, method);
            protocol.publishMessage(topic, payload.toString());
            log.info("[handlePropertyMessage][发送属性设置消息][topic: {}]", topic);
        } else if ("thing.service.property.get".equals(method)) {
            // 属性获取
            String topic = IotDeviceTopicEnum.buildPropertyGetTopic(productKey, deviceName);
            JSONObject payload = buildDownstreamPayload(message, method);
            protocol.publishMessage(topic, payload.toString());
            log.info("[handlePropertyMessage][发送属性获取消息][topic: {}]", topic);
        } else {
            log.warn("[handlePropertyMessage][未知的属性操作：{}]", method);
        }
    }

    /**
     * 处理服务调用消息
     *
     * @param message 设备消息
     */
    private void handleServiceMessage(IotDeviceMessage message) {
        String method = message.getMethod();

        // 通过 deviceId 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfoById(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handleServiceMessage][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        String productKey = deviceInfo.getProductKey();
        String deviceName = deviceInfo.getDeviceName();

        // 从方法中提取服务标识符
        String serviceIdentifier = method.substring("thing.service.".length());

        String topic = IotDeviceTopicEnum.buildServiceTopic(productKey, deviceName, serviceIdentifier);
        JSONObject payload = buildDownstreamPayload(message, method);
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleServiceMessage][发送服务调用消息][topic: {}]", topic);
    }

    /**
     * 处理配置设置消息
     *
     * @param message 设备消息
     */
    private void handleConfigMessage(IotDeviceMessage message) {
        // 通过 deviceId 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfoById(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handleConfigMessage][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        String productKey = deviceInfo.getProductKey();
        String deviceName = deviceInfo.getDeviceName();

        String topic = IotDeviceTopicEnum.buildConfigSetTopic(productKey, deviceName);
        JSONObject payload = buildDownstreamPayload(message, message.getMethod());
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleConfigMessage][发送配置设置消息][topic: {}]", topic);
    }

    /**
     * 处理 OTA 升级消息
     *
     * @param message 设备消息
     */
    private void handleOtaMessage(IotDeviceMessage message) {
        // 通过 deviceId 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfoById(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handleOtaMessage][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        String productKey = deviceInfo.getProductKey();
        String deviceName = deviceInfo.getDeviceName();

        String topic = IotDeviceTopicEnum.buildOtaUpgradeTopic(productKey, deviceName);
        JSONObject payload = buildDownstreamPayload(message, message.getMethod());
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
        payload.set("id", message.getId()); // 使用正确的消息ID字段
        payload.set("version", "1.0");
        payload.set("method", method);
        payload.set("params", message.getData());
        return payload;
    }

}
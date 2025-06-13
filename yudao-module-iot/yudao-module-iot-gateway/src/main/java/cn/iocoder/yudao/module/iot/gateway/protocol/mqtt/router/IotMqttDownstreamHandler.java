package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 下行消息处理器
 * <p>
 * 从消息总线接收到下行消息，然后发布到 MQTT Broker
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttDownstreamHandler {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceService deviceService;
    private final IotDeviceMessageService deviceMessageService;

    public IotMqttDownstreamHandler(IotMqttUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    public void handle(IotDeviceMessage message) {
        // 1. 获取设备信息（使用缓存）
        IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handle][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        // 2. 根据方法构建主题
        String topic = buildTopicByMethod(message.getMethod(), deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        if (StrUtil.isBlank(topic)) {
            log.warn("[handle][未知的消息方法：{}]", message.getMethod());
            return;
        }

        // 3. 构建载荷
        JSONObject payload = buildDownstreamPayload(message);

        // 4. 发布消息
        protocol.publishMessage(topic, payload.toString());
        log.info("[handle][发布下行消息成功][method: {}][topic: {}]", message.getMethod(), topic);
    }

    /**
     * 根据方法构建主题
     *
     * @param method     消息方法
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 构建的主题，如果方法不支持返回 null
     */
    private String buildTopicByMethod(String method, String productKey, String deviceName) {
        IotDeviceMessageMethodEnum methodEnum = IotDeviceMessageMethodEnum.of(method);
        if (methodEnum == null) {
            return null;
        }
        return switch (methodEnum) {
            case PROPERTY_POST -> IotMqttTopicUtils.buildPropertyPostReplyTopic(productKey, deviceName);
            case PROPERTY_SET -> IotMqttTopicUtils.buildPropertySetTopic(productKey, deviceName);
            default -> null;
        };

    }

    /**
     * 构建下行消息载荷
     *
     * @param message 设备消息
     * @return JSON 载荷
     */
    private JSONObject buildDownstreamPayload(IotDeviceMessage message) {
        // 使用 IotDeviceMessageService 进行消息编码
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(message.getDeviceId());
        byte[] encodedBytes = deviceMessageService.encodeDeviceMessage(message, device.getProductKey(),
                device.getDeviceName());
        return new JSONObject(new String(encodedBytes));
    }

}
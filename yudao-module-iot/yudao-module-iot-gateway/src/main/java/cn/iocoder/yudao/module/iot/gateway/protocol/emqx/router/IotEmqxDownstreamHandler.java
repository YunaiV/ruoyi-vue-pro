package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 下行消息处理器
 * <p>
 * 从消息总线接收到下行消息，然后发布到 MQTT Broker，从而被设备所接收
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxDownstreamHandler {

    private final IotEmqxUpstreamProtocol protocol;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService deviceMessageService;

    public IotEmqxDownstreamHandler(IotEmqxUpstreamProtocol protocol) {
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
        // 1. 获取设备信息
        IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(message.getDeviceId());
        if (deviceInfo == null) {
            log.error("[handle][设备信息({})不存在]", message.getDeviceId());
            return;
        }

        // 2.1 根据方法构建主题
        String topic = buildTopicByMethod(message, deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        if (StrUtil.isBlank(topic)) {
            log.warn("[handle][未知的消息方法: {}]", message.getMethod());
            return;
        }
        // 2.2 构建载荷
        byte[] payload = deviceMessageService.encodeDeviceMessage(message, deviceInfo.getProductKey(),
                deviceInfo.getDeviceName());
        // 2.3 发布消息
        protocol.publishMessage(topic, payload);
    }

    /**
     * 根据消息方法和回复状态构建主题
     *
     * @param message    设备消息
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 构建的主题，如果方法不支持返回 null
     */
    private String buildTopicByMethod(IotDeviceMessage message, String productKey, String deviceName) {
        // 1. 判断是否为回复消息
        boolean isReply = IotDeviceMessageUtils.isReplyMessage(message);
        // 2. 根据消息方法类型构建对应的主题
        return IotMqttTopicUtils.buildTopicByMethod(message.getMethod(), productKey, deviceName, isReply);
    }

}
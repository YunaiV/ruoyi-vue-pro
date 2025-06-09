package cn.iocoder.yudao.module.iot.core.mq.producer;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息生产者
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class IotDeviceMessageProducer {

    private final IotMessageBus messageBus;

    /**
     * 发送设备消息
     *
     * @param message 设备消息
     */
    public void sendDeviceMessage(IotDeviceMessage message) {
        messageBus.post(IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC, message);
    }

    /**
     * 发送网关设备消息
     *
     * @param serverId 网关的 serverId 标识
     * @param message 设备消息
     */
    public void sendDeviceMessageToGateway(String serverId, IotDeviceMessage message) {
        messageBus.post(IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(serverId), message);
    }

}

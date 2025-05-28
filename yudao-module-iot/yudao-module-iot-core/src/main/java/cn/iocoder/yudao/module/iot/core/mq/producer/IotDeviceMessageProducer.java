package cn.iocoder.yudao.module.iot.core.mq.producer;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息生产者
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class IotDeviceMessageProducer {

    /**
     * 【消息总线】应用的设备消息 Topic，由 iot-gateway 发给 iot-biz 进行消费
     */
    private static final String MESSAGE_BUS_DEVICE_MESSAGE_TOPIC = "iot_device_message";

    /**
     * 【消息总线】设备消息 Topic，由 iot-biz 发送给 iot-gateway 的某个 “server”(protocol) 进行消费
     *
     * 其中，%s 就是该“server”(protocol) 的标识
     */
    private static final String MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC = MESSAGE_BUS_DEVICE_MESSAGE_TOPIC + "/%s";

    private final IotMessageBus messageBus;

    /**
     * 发送设备消息
     *
     * @param message 设备消息
     */
    public void sendDeviceMessage(IotDeviceMessage message) {
        messageBus.post(MESSAGE_BUS_DEVICE_MESSAGE_TOPIC, message);
    }

    /**
     * 发送网关设备消息
     *
     * @param server 网关的 server 标识
     * @param message 设备消息
     */
    public void sendGatewayDeviceMessage(String server, Object message) {
        messageBus.post(String.format(MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC, server), message);
    }

}

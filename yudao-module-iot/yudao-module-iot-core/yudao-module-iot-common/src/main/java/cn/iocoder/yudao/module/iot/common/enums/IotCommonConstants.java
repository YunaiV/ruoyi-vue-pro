package cn.iocoder.yudao.module.iot.common.enums;

/**
 * IoT 通用的枚举
 *
 * @author 芋道源码
 */
public interface IotCommonConstants {

    /**
     * 【消息总线】应用的设备消息 Topic，由 iot-gateway 发给 iot-biz 进行消费
     */
    String MESSAGE_BUS_DEVICE_MESSAGE_TOPIC = "iot_device_message";

    /**
     * 【消息总线】设备消息 Topic，由 iot-biz 发送给 iot-gateway 的某个 “server”(protocol) 进行消费
     *
     * 其中，%s 就是该“server”(protocol) 的标识
     */
    String MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC = MESSAGE_BUS_DEVICE_MESSAGE_TOPIC + "/%s";

}

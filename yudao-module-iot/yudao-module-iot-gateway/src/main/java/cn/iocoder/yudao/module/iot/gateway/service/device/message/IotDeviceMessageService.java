package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * IoT 设备消息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageService {

    /**
     * 序列化消息
     *
     * @param message    消息
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 序列化后的消息内容
     */
    byte[] serializeDeviceMessage(IotDeviceMessage message,
                                  String productKey, String deviceName);

    /**
     * 序列化消息
     *
     * @param message       消息
     * @param serializeType 序列化类型
     * @return 序列化后的消息内容
     */
    byte[] serializeDeviceMessage(IotDeviceMessage message,
                                  IotSerializeTypeEnum serializeType);

    /**
     * 反序列化消息
     *
     * @param bytes      消息内容
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 反序列化后的消息内容
     */
    IotDeviceMessage deserializeDeviceMessage(byte[] bytes,
                                              String productKey, String deviceName);

    /**
     * 反序列化消息
     *
     * @param bytes         消息内容
     * @param serializeType 序列化类型
     * @return 反序列化后的消息内容
     */
    IotDeviceMessage deserializeDeviceMessage(byte[] bytes, IotSerializeTypeEnum serializeType);

    /**
     * 发送消息
     *
     * @param message    消息
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param serverId   设备连接的 serverId
     */
    void sendDeviceMessage(IotDeviceMessage message,
                           String productKey, String deviceName, String serverId);

}

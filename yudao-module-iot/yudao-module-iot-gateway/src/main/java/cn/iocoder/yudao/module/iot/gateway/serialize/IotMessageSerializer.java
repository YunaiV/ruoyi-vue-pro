package cn.iocoder.yudao.module.iot.gateway.serialize;

import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * IoT 设备消息序列化器接口
 *
 * 用于序列化和反序列化设备消息
 *
 * @author 芋道源码
 */
public interface IotMessageSerializer {

    /**
     * 序列化消息
     *
     * @param message 消息
     * @return 编码后的消息内容
     */
    byte[] serialize(IotDeviceMessage message);

    /**
     * 反序列化消息
     *
     * @param bytes 消息内容
     * @return 解码后的消息内容
     */
    IotDeviceMessage deserialize(byte[] bytes);

    /**
     * 获取序列化类型
     *
     * @return 序列化类型枚举
     */
    IotSerializeTypeEnum getType();

}

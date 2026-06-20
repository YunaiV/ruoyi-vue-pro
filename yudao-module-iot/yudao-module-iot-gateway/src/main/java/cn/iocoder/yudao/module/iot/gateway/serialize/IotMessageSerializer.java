package cn.iocoder.yudao.module.iot.gateway.serialize;

import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

import java.util.Map;

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

    /**
     * 透传原始 body 编码（影子等不走 {@link IotDeviceMessage} 封套的场景）
     *
     * @param value 待编码对象（通常为 Map 或 DTO）
     * @return 线格式字节
     */
    default byte[] encodeRaw(Object value) {
        throw new UnsupportedOperationException("该序列化器不支持影子透传编码");
    }

    /**
     * 透传原始 body 解码
     *
     * @param bytes 线格式字节
     * @return 透传 params
     */
    default Map<String, Object> decodeRaw(byte[] bytes) {
        throw new UnsupportedOperationException("该序列化器不支持影子透传解码");
    }

}

package cn.iocoder.yudao.module.iot.gateway.serialize.json;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;

/**
 * JSON 格式的消息序列化器
 *
 * 直接使用 JsonUtils 序列化/反序列化 {@link IotDeviceMessage}，不包装额外字段
 *
 * @author 芋道源码
 */
public class IotJsonSerializer implements IotMessageSerializer {

    @Override
    public IotSerializeTypeEnum getType() {
        return IotSerializeTypeEnum.JSON;
    }

    @Override
    public byte[] serialize(IotDeviceMessage message) {
        Assert.notNull(message, "消息不能为空");
        return JsonUtils.toJsonByte(message);
    }

    @Override
    public IotDeviceMessage deserialize(byte[] bytes) {
        Assert.notNull(bytes, "待解码数据不能为空");
        IotDeviceMessage message = JsonUtils.parseObject(bytes, IotDeviceMessage.class);
        Assert.notNull(message, "消息解码失败");
        return message;
    }

}

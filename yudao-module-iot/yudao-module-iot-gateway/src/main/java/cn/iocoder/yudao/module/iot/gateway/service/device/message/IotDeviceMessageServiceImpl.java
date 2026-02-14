package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

/**
 * IoT 设备消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Resource
    private IotMessageSerializerManager messageSerializerManager;

    @Override
    public byte[] serializeDeviceMessage(IotDeviceMessage message,
                                         String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(device.getSerializeType());
        Assert.notNull(serializeType, "设备序列化类型不能为空");

        // 2. 序列化消息
        return serializeDeviceMessage(message, serializeType);
    }

    @Override
    public byte[] serializeDeviceMessage(IotDeviceMessage message,
                                         IotSerializeTypeEnum serializeType) {
        // 1. 获取序列化器
        IotMessageSerializer serializer = messageSerializerManager.get(serializeType);
        if (serializer == null) {
            throw new IllegalArgumentException(StrUtil.format("序列化器({}) 不存在", serializeType));
        }

        // 2. 序列化消息
        return serializer.serialize(message);
    }

    @Override
    public IotDeviceMessage deserializeDeviceMessage(byte[] bytes,
                                                     String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(device.getSerializeType());
        Assert.notNull(serializeType, "设备序列化类型不能为空");

        // 2. 反序列化消息
        return deserializeDeviceMessage(bytes, serializeType);
    }

    @Override
    public IotDeviceMessage deserializeDeviceMessage(byte[] bytes, IotSerializeTypeEnum serializeType) {
        // 1. 获取序列化器
        IotMessageSerializer serializer = messageSerializerManager.get(serializeType);
        if (serializer == null) {
            throw new IllegalArgumentException(StrUtil.format("序列化器({}) 不存在", serializeType));
        }

        // 2. 反序列化消息
        return serializer.deserialize(bytes);
    }

    @Override
    public void sendDeviceMessage(IotDeviceMessage message,
                                  String productKey, String deviceName, String serverId) {
        // 1. 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }

        // 2. 发送消息
        appendDeviceMessage(message, device, serverId);
        deviceMessageProducer.sendDeviceMessage(message);
    }

    /**
     * 补充消息的后端字段
     *
     * @param message  消息
     * @param device   设备信息
     * @param serverId 设备连接的 serverId
     */
    private void appendDeviceMessage(IotDeviceMessage message,
                                     IotDeviceRespDTO device, String serverId) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
                .setDeviceId(device.getId()).setTenantId(device.getTenantId()).setServerId(serverId);
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
    }

}

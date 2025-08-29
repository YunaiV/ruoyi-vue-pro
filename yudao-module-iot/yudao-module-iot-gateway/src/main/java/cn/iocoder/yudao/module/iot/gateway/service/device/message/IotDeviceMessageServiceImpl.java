package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 编解码器
     */
    private final Map<String, IotDeviceMessageCodec> codes;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    public IotDeviceMessageServiceImpl(List<IotDeviceMessageCodec> codes) {
        this.codes = CollectionUtils.convertMap(codes, IotDeviceMessageCodec::type);
    }

    @Override
    public byte[] encodeDeviceMessage(IotDeviceMessage message,
                                      String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取编解码器
        IotDeviceMessageCodec codec = codes.get(device.getCodecType());
        if (codec == null) {
            throw new IllegalArgumentException(StrUtil.format("编解码器({}) 不存在", device.getCodecType()));
        }

        // 2. 编码消息
        return codec.encode(message);
    }

    @Override
    public byte[] encodeDeviceMessage(IotDeviceMessage message,
                                      String codecType) {
        // 1. 获取编解码器
        IotDeviceMessageCodec codec = codes.get(codecType);
        if (codec == null) {
            throw new IllegalArgumentException(StrUtil.format("编解码器({}) 不存在", codecType));
        }

        // 2. 编码消息
        return codec.encode(message);
    }

    @Override
    public IotDeviceMessage decodeDeviceMessage(byte[] bytes,
                                                String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取编解码器
        IotDeviceMessageCodec codec = codes.get(device.getCodecType());
        if (codec == null) {
            throw new IllegalArgumentException(StrUtil.format("编解码器({}) 不存在", device.getCodecType()));
        }

        // 2. 解码消息
        return codec.decode(bytes);
    }

    @Override
    public IotDeviceMessage decodeDeviceMessage(byte[] bytes, String codecType) {
        // 1. 获取编解码器
        IotDeviceMessageCodec codec = codes.get(codecType);
        if (codec == null) {
            throw new IllegalArgumentException(StrUtil.format("编解码器({}) 不存在", codecType));
        }

        // 2. 解码消息
        return codec.decode(bytes);
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

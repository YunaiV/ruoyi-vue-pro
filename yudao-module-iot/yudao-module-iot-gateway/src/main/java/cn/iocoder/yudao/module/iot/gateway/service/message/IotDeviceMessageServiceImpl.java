package cn.iocoder.yudao.module.iot.gateway.service.message;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.alink.IotAlinkDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceCacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private final Map<String, IotAlinkDeviceMessageCodec> codes;

    @Resource
    private IotDeviceCacheService deviceCacheService;

    public IotDeviceMessageServiceImpl(List<IotAlinkDeviceMessageCodec> codes) {
        this.codes = CollectionUtils.convertMap(codes, IotAlinkDeviceMessageCodec::type);
    }

    @Override
    public byte[] encodeDeviceMessage(IotDeviceMessage message,
                                      String productKey, String deviceName) {
        // 获取设备信息以确定编解码类型
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfo(productKey, deviceName);
        if (deviceInfo == null) {
            log.warn("[encodeDeviceMessage][设备信息不存在][productKey: {}][deviceName: {}]",
                    productKey, deviceName);
            return null;
        }

        String codecType = "alink"; // 默认使用 alink 编解码器
        IotAlinkDeviceMessageCodec codec = codes.get(codecType);
        if (codec == null) {
            log.error("[encodeDeviceMessage][编解码器不存在][codecType: {}]", codecType);
            return null;
        }

        return codec.encode(message);
    }

    @Override
    public IotDeviceMessage decodeDeviceMessage(byte[] bytes,
                                                String productKey, String deviceName, String serverId) {
        // 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfo(productKey, deviceName);
        if (deviceInfo == null) {
            log.warn("[decodeDeviceMessage][设备信息不存在][productKey: {}][deviceName: {}]",
                    productKey, deviceName);
            return null;
        }

        String codecType = "alink"; // 默认使用 alink 编解码器
        IotAlinkDeviceMessageCodec codec = codes.get(codecType);
        if (codec == null) {
            log.error("[decodeDeviceMessage][编解码器不存在][codecType: {}]", codecType);
            return null;
        }

        IotDeviceMessage message = codec.decode(bytes);
        if (message == null) {
            log.warn("[decodeDeviceMessage][消息解码失败][productKey: {}][deviceName: {}]",
                    productKey, deviceName);
            return null;
        }

        // 补充后端字段
        return appendDeviceMessage(message, deviceInfo, serverId);
    }

    @Override
    public IotDeviceMessage buildDeviceMessageOfStateOnline(String productKey, String deviceName, String serverId) {
        // 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfo(productKey, deviceName);
        if (deviceInfo == null) {
            log.warn("[buildDeviceMessageOfStateOnline][设备信息不存在][productKey: {}][deviceName: {}]",
                    productKey, deviceName);
            return null;
        }

        IotDeviceMessage message = IotDeviceMessage.requestOf(null,
                IotDeviceMessageMethodEnum.STATE_ONLINE.getMethod(), null);

        return appendDeviceMessage(message, deviceInfo, serverId);
    }

    @Override
    public IotDeviceMessage buildDeviceMessageOfStateOffline(String productKey, String deviceName, String serverId) {
        // 获取设备信息
        IotDeviceCacheService.DeviceInfo deviceInfo = deviceCacheService.getDeviceInfo(productKey, deviceName);
        if (deviceInfo == null) {
            log.warn("[buildDeviceMessageOfStateOffline][设备信息不存在][productKey: {}][deviceName: {}]",
                    productKey, deviceName);
            return null;
        }

        IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.STATE_OFFLINE.getMethod(),
                null);
        return appendDeviceMessage(message, deviceInfo, serverId);
    }

    /**
     * 补充消息的后端字段
     *
     * @param message    消息
     * @param device 设备信息
     * @param serverId   设备连接的 serverId
     * @return 消息
     */
    private IotDeviceMessage appendDeviceMessage(IotDeviceMessage message,
                                                 IotDeviceCacheService.DeviceInfo device, String serverId) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
                .setDeviceId(device.getDeviceId()).setTenantId(device.getTenantId()).setServerId(serverId);
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
        return message;
    }

}

package cn.iocoder.yudao.module.iot.gateway.service.message;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.alink.IotAlinkDeviceMessageCodec;
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
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    /**
     * 编解码器
     */
    private final Map<String, IotAlinkDeviceMessageCodec> codes;

    public IotDeviceMessageServiceImpl(List<IotAlinkDeviceMessageCodec> codes) {
        this.codes = CollectionUtils.convertMap(codes, IotAlinkDeviceMessageCodec::type);
    }

    @Override
    public byte[] encodeDeviceMessage(IotDeviceMessage message,
                                      String productKey, String deviceName) {
        // TODO @芋艿：获取设备信息
        String codecType = "alink";
        return codes.get(codecType).encode(message);
    }

    @Override
    public IotDeviceMessage decodeDeviceMessage(byte[] bytes,
                                                String productKey, String deviceName, String serverId) {
        // TODO @芋艿：获取设备信息
        String codecType = "alink";
        IotDeviceMessage message = codes.get(codecType).decode(bytes);
        // 补充后端字段
        Long deviceId = 25L;
        Long tenantId = 1L;
        appendDeviceMessage(message, deviceId, tenantId, serverId);
        return message;
    }

    @Override
    public IotDeviceMessage buildDeviceMessageOfStateOnline(String productKey, String deviceName, String serverId) {
        IotDeviceMessage message = IotDeviceMessage.of(null,
                IotDeviceMessageMethodEnum.STATE_ONLINE.getMethod(), null);
        // 补充后端字段
        Long deviceId = 25L;
        Long tenantId = 1L;
        return appendDeviceMessage(message, deviceId, tenantId, serverId);
    }

    /**
     * 补充消息的后端字段
     *
     * @param message  消息
     * @param deviceId 设备编号
     * @param tenantId 租户编号
     * @param serverId 设备连接的 serverId
     * @return 消息
     */
    private IotDeviceMessage appendDeviceMessage(IotDeviceMessage message,
                                                 Long deviceId, Long tenantId, String serverId) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
                .setDeviceId(deviceId).setTenantId(tenantId).setServerId(serverId);
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
        return message;
    }

}

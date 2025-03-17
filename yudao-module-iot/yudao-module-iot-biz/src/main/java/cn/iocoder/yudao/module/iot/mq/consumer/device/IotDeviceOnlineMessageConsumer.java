package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 针对 {@link IotDeviceMessage} 的消费者，将离线的设备，自动标记为上线
 *
 * 注意：只有设备上行消息，才会触发该逻辑
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotDeviceOnlineMessageConsumer {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;

    @EventListener
    @Async
    public void onMessage(IotDeviceMessage message) {
        // 1.1 只处理上行消息。因为，只有设备上行的消息，才会触发设备上线的逻辑
        if (!isUpstreamMessage(message)) {
            return;
        }
        // 1.2 如果设备已在线，则不做处理
        log.info("[onMessage][消息内容({})]", message);
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                message.getProductKey(), message.getDeviceName());
        if (device == null) {
            log.error("[onMessage][消息({}) 对应的设备部存在]", message);
            return;
        }
        if (IotDeviceStateEnum.isOnline(device.getState())) {
            return;
        }

        // 2. 标记设备为在线
        // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志等等
        deviceUpstreamService.updateDeviceState(((IotDeviceStateUpdateReqDTO)
                new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID()).setReportTime(LocalDateTime.now())
                        .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName()))
                .setState((IotDeviceStateEnum.ONLINE.getState())));
    }

    private boolean isUpstreamMessage(IotDeviceMessage message) {
        // 设备属性
        if (Objects.equals(message.getType(), IotDeviceMessageTypeEnum.PROPERTY.getType())
            && Objects.equals(message.getIdentifier(), IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier())) {
            return true;
        }
        // 设备事件
        if (Objects.equals(message.getType(), IotDeviceMessageTypeEnum.EVENT.getType())) {
            return true;
        }
        // 设备服务
        // noinspection RedundantIfStatement
        if (Objects.equals(message.getType(), IotDeviceMessageTypeEnum.SERVICE.getType())
            && !StrUtil.endWith(message.getIdentifier(), IotDeviceMessageIdentifierEnum.SERVICE_REPLY_SUFFIX.getIdentifier())) {
            return true;
        }
        return false;
    }

}
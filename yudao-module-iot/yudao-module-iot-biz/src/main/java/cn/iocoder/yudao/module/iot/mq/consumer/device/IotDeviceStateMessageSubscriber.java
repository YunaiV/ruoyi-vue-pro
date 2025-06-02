package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 针对 {@link IotDeviceMessage} 的消费者：记录设备状态
 *
 * 特殊：如果是离线的设备，将自动上线
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotDeviceStateMessageSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;

    @Resource
    private IotMessageBus messageBus;
    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC;
    }

    @Override
    public String getGroup() {
        return "iot_device_state_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        // 1.1 只处理上行消息，或者是 STATE 相关的消息
        if (!IotDeviceMessageUtils.isUpstreamMessage(message)
            && ObjectUtil.notEqual(message.getType(), IotDeviceMessageTypeEnum.STATE.getType())) {
            return;
        }
        // 1.2 校验设备是否存在
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(
                message.getProductKey(), message.getDeviceName());
        if (device == null) {
            log.error("[onMessage][消息({}) 对应的设备部存在]", message);
            return;
        }

        // 2. 处理消息
        TenantUtils.execute(device.getTenantId(), () -> onMessage(message, device));
    }

    private void onMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 更新设备的最后时间
        devicePropertyService.updateDeviceReportTime(device.getProductKey(), device.getDeviceName(), LocalDateTime.now());

        // 情况一：STATE 相关的消息
        if (Objects.equals(message.getType(), IotDeviceMessageTypeEnum.STATE.getType())) {
            if (Objects.equals(message.getIdentifier(), IotDeviceMessageIdentifierEnum.STATE_ONLINE.getIdentifier())) {
                deviceService.updateDeviceState(device.getId(), IotDeviceStateEnum.ONLINE.getState());
                devicePropertyService.updateDeviceServerId(device.getProductKey(), device.getDeviceName(), message.getServerId());
            } else {
                deviceService.updateDeviceState(device.getId(), IotDeviceStateEnum.OFFLINE.getState());
                devicePropertyService.deleteDeviceServerId(device.getProductKey(), device.getDeviceName());
            }
            // TODO 芋艿：子设备的关联
            return;
        }

        // 情况二：非 STATE 相关的消息
        devicePropertyService.updateDeviceServerId(device.getProductKey(), device.getDeviceName(), message.getServerId());
        // 特殊：设备非在线时，主动标记设备为在线
        // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志等等
        if (ObjectUtil.notEqual(device.getState(), IotDeviceStateEnum.ONLINE.getState())) {
            deviceMessageProducer.sendDeviceMessage(IotDeviceMessage.of(message.getProductKey(), message.getDeviceName())
                    .ofStateOnline());
        }
    }

}
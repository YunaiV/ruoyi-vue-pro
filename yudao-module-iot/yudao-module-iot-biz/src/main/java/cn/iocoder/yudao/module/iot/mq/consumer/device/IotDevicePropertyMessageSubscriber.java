package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageIdentifierEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceMessageTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import com.google.common.base.Objects;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 针对 {@link IotDeviceMessage} 的消费者：记录设备属性
 *
 * @author alwayssuper
 */
@Component
@Slf4j
public class IotDevicePropertyMessageSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotDevicePropertyService deviceDataService;

    @Resource
    private IotMessageBus messageBus;

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
        return "iot_device_property_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        if (Objects.equal(message.getType(), IotDeviceMessageTypeEnum.PROPERTY.getType())
                && Objects.equal(message.getIdentifier(), IotDeviceMessageIdentifierEnum.PROPERTY_REPORT.getIdentifier())) {
            // 保存设备属性
            deviceDataService.saveDeviceProperty(message);
        }
    }

}

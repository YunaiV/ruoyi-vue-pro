package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.device.data.IotDeviceLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 针对 {@link IotDeviceMessage} 的消费者：记录设备日志
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotDeviceLogMessageSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotMessageBus messageBus;

    @Resource
    private IotDeviceLogService deviceLogService;

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
        return "iot_device_log_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        deviceLogService.createDeviceLog(message);
    }

}

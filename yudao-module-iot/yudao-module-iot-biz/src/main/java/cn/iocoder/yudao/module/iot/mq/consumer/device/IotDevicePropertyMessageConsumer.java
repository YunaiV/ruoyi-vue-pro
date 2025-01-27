package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.device.data.IotDevicePropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link IotDeviceMessage} 的消费者，记录设备属性
 *
 * @author alwayssuper
 */
@Component
@Slf4j
public class IotDevicePropertyMessageConsumer {

    @Resource
    private IotDevicePropertyService deviceDataService;

    @EventListener
    @Async
    public void onMessage(IotDeviceMessage message) {
        log.info("[onMessage][消息内容({})]", message);

        // 设备日志记录
        // TODO @芋艿：重新写下
//        deviceLogDataService.createDeviceLog(message);
    }

}

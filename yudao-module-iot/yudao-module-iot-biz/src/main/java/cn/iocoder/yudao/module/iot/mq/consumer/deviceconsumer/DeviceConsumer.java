package cn.iocoder.yudao.module.iot.mq.consumer.deviceconsumer;


import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceLogDataService;
import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link ThingModelMessage} 的消费者
 *
 * @author alwayssuper
 */
@Component
@Slf4j
public class DeviceConsumer {
    @Resource
    private IotDeviceLogDataService iotDeviceLogDataService;
    @Resource
    private IotDevicePropertyDataService deviceDataService;

    // TODO @芋艿：这块先用ThingModelMessage，后续看看用啥替代
    @EventListener
    @Async
    public void onMessage(ThingModelMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        // 设备数据记录
//        deviceDataService.saveDeviceDataTest(message);
        // 设备日志记录
        iotDeviceLogDataService.saveDeviceLog(message);
    }

}

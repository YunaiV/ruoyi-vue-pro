package cn.iocoder.yudao.module.iot.mq.consumer.deviceconsumer;


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
    private IotDeviceLogDataService deviceLogDataService;
    @Resource
    private IotDevicePropertyDataService deviceDataService;

    // TODO @芋艿：这块先用ThingModelMessage，后续看看用啥替代
    @EventListener
    @Async
    public void onMessage(ThingModelMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        //TODO:数据插入这块整体写的比较混乱，整体借鉴了浩浩哥之前写的逻辑，目前是通过模拟设备科插入数据了，但之前的逻辑有大量弃用的部分，后续看看怎么完善

        // 设备数据记录
        deviceDataService.saveDeviceDataTest(message);
        // 设备日志记录
        deviceLogDataService.saveDeviceLog(message);
    }

}

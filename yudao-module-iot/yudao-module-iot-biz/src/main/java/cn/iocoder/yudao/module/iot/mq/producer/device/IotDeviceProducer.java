package cn.iocoder.yudao.module.iot.mq.producer.device;

import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * IoT 设备相关消息的 Producer
 *
 * @author alwayssuper
 * @since 2024/12/17 16:35
 */
@Slf4j
@Component
public class IotDeviceProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link IotDeviceMessage} 消息
     *
     * @param thingModelMessage 物模型消息
     */
    public void sendDeviceMessage(IotDeviceMessage thingModelMessage) {
        applicationContext.publishEvent(thingModelMessage);
    }

}

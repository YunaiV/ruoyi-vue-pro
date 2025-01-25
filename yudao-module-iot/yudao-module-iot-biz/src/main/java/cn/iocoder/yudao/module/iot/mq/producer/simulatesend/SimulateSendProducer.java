package cn.iocoder.yudao.module.iot.mq.producer.simulatesend;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// TODO @芋艿：@alwayssuper：是不是还没用起来哈？Producer 最好属于某个模块；
/**
 * SimulateSend 模拟设备上报的 Producer
 *
 * @author alwayssuper
 * @since 2024/12/17 16:35
 */
@Slf4j
@Component
public class SimulateSendProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link ThingModelMessage} 消息
     *
     * @param thingModelMessage 物模型消息
     */
    public void sendSimulateMessage(ThingModelMessage thingModelMessage) {
        applicationContext.publishEvent(thingModelMessage);
    }

}

package cn.iocoder.yudao.module.iot.messagebus.core.local;

import cn.iocoder.yudao.module.iot.messagebus.core.AbstractIotMessageBus;
import cn.iocoder.yudao.module.iot.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.messagebus.core.IotMessageBusSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

/**
 * 本地的 {@link IotMessageBus} 实现类
 *
 * 注意：仅适用于单机场景！！！
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class LocalIotMessageBus extends AbstractIotMessageBus {

    private final ApplicationContext applicationContext;

    @Override
    public void post(String topic, Object message) {
        applicationContext.publishEvent(new LocalIotMessage(topic, message));
    }

    @Override
    protected void doRegister(String topic, IotMessageBusSubscriber<?> subscriber) {
        // 无需实现，交给 Spring @EventListener 监听
    }

    @EventListener
    public void onMessage(LocalIotMessage message) {
        notifySubscribers(message.getTopic(), message.getMessage());
    }

}
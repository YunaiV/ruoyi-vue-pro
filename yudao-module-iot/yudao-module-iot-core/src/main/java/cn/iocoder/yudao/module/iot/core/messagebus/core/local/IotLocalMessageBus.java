package cn.iocoder.yudao.module.iot.core.messagebus.core.local;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地的 {@link IotMessageBus} 实现类
 *
 * 注意：仅适用于单机场景！！！
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotLocalMessageBus implements IotMessageBus {

    private final ApplicationContext applicationContext;

    /**
     * 订阅者映射表
     * Key: topic
     */
    private final Map<String, List<IotMessageSubscriber<?>>> subscribers = new HashMap<>();

    @Override
    public void post(String topic, Object message) {
        applicationContext.publishEvent(new IotLocalMessage(topic, message));
    }

    @Override
    public void register(IotMessageSubscriber<?> subscriber) {
        String topic = subscriber.getTopic();
        List<IotMessageSubscriber<?>> topicSubscribers = subscribers.computeIfAbsent(topic, k -> new ArrayList<>());
        topicSubscribers.add(subscriber);
        log.info("[register][topic({}/{}) 注册消费者({})成功]",
                topic, subscriber.getGroup(), subscriber.getClass().getName());
    }

    @EventListener
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onMessage(IotLocalMessage message) {
        String topic = message.getTopic();
        List<IotMessageSubscriber<?>> topicSubscribers = subscribers.get(topic);
        if (CollUtil.isEmpty(topicSubscribers)) {
            return;
        }
        for (IotMessageSubscriber subscriber : topicSubscribers) {
            try {
                subscriber.onMessage(message.getMessage());
            } catch (Exception ex) {
                log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                        subscriber.getTopic(), subscriber.getGroup(), message.getMessage(), subscriber.getClass().getName(), ex);
            }
        }
    }

}
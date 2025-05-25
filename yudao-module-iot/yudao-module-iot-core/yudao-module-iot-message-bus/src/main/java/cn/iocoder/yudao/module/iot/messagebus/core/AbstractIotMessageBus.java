package cn.iocoder.yudao.module.iot.messagebus.core;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * IoT 消息总线抽象基类
 *
 * 提供通用的订阅者管理功能
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractIotMessageBus implements IotMessageBus {

    /**
     * 订阅者映射表
     * Key: topic
     */
    private final Map<String, List<IotMessageBusSubscriber<?>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void register(String topic, IotMessageBusSubscriber<?> subscriber) {
        // 执行注册
        doRegister(topic, subscriber);

        // 添加订阅者映射
        List<IotMessageBusSubscriber<?>> topicSubscribers = subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
        topicSubscribers.add(subscriber);
        topicSubscribers.sort(Comparator.comparingInt(IotMessageBusSubscriber::order));
        log.info("[register][topic({}) 注册订阅者({})成功]", topic, subscriber.getClass().getName());
    }

    /**
     * 注册订阅者
     *
     * @param topic 主题
     * @param subscriber 订阅者
     */
    protected abstract void doRegister(String topic, IotMessageBusSubscriber<?> subscriber);

    /**
     * 通知订阅者
     *
     * @param message 消息
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void notifySubscribers(String topic, Object message) {
        List<IotMessageBusSubscriber<?>> topicSubscribers = subscribers.get(topic);
        if (CollUtil.isEmpty(topicSubscribers)) {
            return;
        }
        for (IotMessageBusSubscriber subscriber : topicSubscribers) {
            try {
                subscriber.onMessage(topic, message);
            } catch (Exception e) {
                log.error("[notifySubscribers][topic({}) message({}) 通知订阅者({})失败]",
                        topic, e, subscriber.getClass().getName());
            }
        }
    }

}
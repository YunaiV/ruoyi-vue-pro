package cn.iocoder.yudao.module.iot.core.messagebus.core.rocketmq;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 RocketMQ 的 {@link IotMessageBus} 实现类
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotRocketMQMessageBus implements IotMessageBus {

    private final RocketMQProperties rocketMQProperties;

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 主题对应的消费者映射
     */
    private final List<DefaultMQPushConsumer> topicConsumers = new ArrayList<>();

    /**
     * 销毁时关闭所有消费者
     */
    @PreDestroy
    public void destroy() {
        for (DefaultMQPushConsumer consumer : topicConsumers) {
            try {
                consumer.shutdown();
                log.info("[destroy][关闭 group({}) 的消费者成功]", consumer.getConsumerGroup());
            } catch (Exception e) {
                log.error("[destroy]关闭 group({}) 的消费者异常]", consumer.getConsumerGroup(), e);
            }
        }
    }

    @Override
    public void post(String topic, Object message) {
        // TODO @芋艿：需要 orderly！
        SendResult result = rocketMQTemplate.syncSend(topic, JsonUtils.toJsonString(message));
        log.info("[post][topic({}) 发送消息({}) result({})]", topic, message, result);
    }

    @Override
    @SneakyThrows
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 1.1 创建 DefaultMQPushConsumer
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
        consumer.setConsumerGroup(subscriber.getGroup());
        // 1.2 订阅主题
        consumer.subscribe(subscriber.getTopic(), "*");
        // 1.3 设置消息监听器
        consumer.setMessageListener((MessageListenerConcurrently) (messages, context) -> {
            for (MessageExt messageExt : messages) {
                try {
                    byte[] body = messageExt.getBody();
                    subscriber.onMessage(JsonUtils.parseObject(body, type));
                } catch (Exception ex) {
                    log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                            subscriber.getTopic(), subscriber.getGroup(), messageExt, subscriber.getClass().getName(), ex);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        // 1.4 启动消费者
        consumer.start();

        // 2. 保存消费者引用
        topicConsumers.add(consumer);
    }

}
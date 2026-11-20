package cn.iocoder.yudao.module.iot.core.messagebus.core.rocketmq;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import javax.annotation.PreDestroy;
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

    /**
     * 确认发送：syncSend 失败时抛出异常；返回非 SEND_OK（刷盘、同步从节点超时等）时，也视为未确认
     * <p>
     * 失败等待时间由生产者的 send-message-timeout、retry-times-when-send-failed 限定（沿用用户配置）
     * <p>
     * 消息不合法（MESSAGE_ILLEGAL）时，重试也不会成功，抛出 {@link IotMessageRejectedException}：
     * 1. 客户端发送前校验（MQClientException）：消息体超过 max-message-size 等
     * 2. broker 返回（MQBrokerException）：消息体超过 broker 的 maxMessageSize、属性超限（PROPERTIES_SIZE_EXCEEDED）等
     * <p>
     * 其它失败（系统繁忙、超时、无路由、broker 不可用等）按未确认抛出
     */
    @Override
    public void postConfirmed(String topic, Object message) {
        SendResult result;
        try {
            result = rocketMQTemplate.syncSend(topic, JsonUtils.toJsonString(message));
        } catch (RuntimeException e) {
            if (isMessageIllegal(e)) {
                throw new IotMessageRejectedException(String.format("RocketMQ 消息不合法，topic(%s)", topic), e);
            }
            throw e;
        }
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new IllegalStateException(String.format("RocketMQ 发送未确认，topic(%s) result(%s)", topic, result));
        }
    }

    /**
     * 是否消息不合法（MESSAGE_ILLEGAL）：RocketMQTemplate 把客户端、broker 的异常包装在原因链中
     */
    private static boolean isMessageIllegal(Throwable e) {
        Throwable clientCause = ExceptionUtil.getCausedBy(e, MQClientException.class);
        if (clientCause != null && ((MQClientException) clientCause).getResponseCode() == ResponseCode.MESSAGE_ILLEGAL) {
            return true;
        }
        Throwable brokerCause = ExceptionUtil.getCausedBy(e, MQBrokerException.class);
        return brokerCause != null && ((MQBrokerException) brokerCause).getResponseCode() == ResponseCode.MESSAGE_ILLEGAL;
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
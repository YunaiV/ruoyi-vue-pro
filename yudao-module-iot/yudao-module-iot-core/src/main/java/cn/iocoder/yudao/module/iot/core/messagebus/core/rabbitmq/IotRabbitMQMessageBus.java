package cn.iocoder.yudao.module.iot.core.messagebus.core.rabbitmq;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于 RabbitMQ 的 {@link IotMessageBus} 实现类
 *
 * @author ywc
 */
@Slf4j
public class IotRabbitMQMessageBus implements IotMessageBus {

    private static final String ROUTING_KEY = "#";

    private final RabbitTemplate rabbitTemplate;

    private final RabbitAdmin rabbitAdmin;

    /**
     * 确认发送使用的 RabbitTemplate：开启 mandatory，无法路由的消息由 broker 退回；不修改共享的 {@link #rabbitTemplate}
     */
    private final RabbitTemplate confirmRabbitTemplate;
    /**
     * 确认发送时，等待 publisher confirm 的超时时间
     */
    private final Duration confirmTimeout;

    @Getter
    private final List<IotMessageSubscriber<?>> subscribers = new ArrayList<>();

    private final List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    public IotRabbitMQMessageBus(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin, Duration confirmTimeout) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.confirmTimeout = confirmTimeout;
        this.confirmRabbitTemplate = new RabbitTemplate(rabbitTemplate.getConnectionFactory());
        this.confirmRabbitTemplate.setMandatory(true);
        // 退回的消息由 CorrelationData#getReturned 判断，这里只记日志（同时避免“没有回调”的告警）
        this.confirmRabbitTemplate.setReturnsCallback(returned -> log.warn(
                "[postConfirmed][exchange({}) 消息无法路由，被退回：{} {}]",
                returned.getExchange(), returned.getReplyCode(), returned.getReplyText()));
    }

    @Override
    public void post(String topic, Object message) {
        rabbitTemplate.send(topic, ROUTING_KEY, MessageBuilder.withBody(JsonUtils.toJsonByte(message)).build());
        log.info("[post][topic({}) 发送消息({})]", topic, message);
    }

    /**
     * 确认发送：publisher confirm 正向 ACK，且没有因为无法路由被退回，才算 broker 已接收
     * <p>
     * 1. NACK：broker 拒绝；交换机不存在时 broker 关闭 channel，框架生成 NACK
     * 2. 退回：交换机存在但没有绑定队列（例如说平台还没有启动、注册订阅者），mandatory 消息被退回；
     *    Spring AMQP 保证退回先于 ACK 设置到 CorrelationData
     * 3. 超时：{@link #confirmTimeout} 内没有收到确认。broker 可能已经接收，按未确认处理（调用方保留，可能重复，不会丢失）
     * <p>
     * 前提：连接工厂开启 spring.rabbitmq.publisher-confirm-type=correlated、spring.rabbitmq.publisher-returns=true
     */
    @Override
    public void postConfirmed(String topic, Object message) {
        // 1. 校验连接工厂已开启 publisher confirm、return：否则拿不到确认，不能当作已接收
        validateConfirmable();

        // 2. 发送，等待确认
        CorrelationData correlationData = new CorrelationData(IdUtil.fastSimpleUUID());
        try {
            confirmRabbitTemplate.send(topic, ROUTING_KEY,
                    MessageBuilder.withBody(JsonUtils.toJsonByte(message)).build(), correlationData);
        } catch (AmqpException e) {
            throw new ConfirmException(String.format("发送 RabbitMQ 消息失败，topic(%s)", topic), e);
        }
        CorrelationData.Confirm confirm;
        try {
            confirm = correlationData.getFuture().get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfirmException(String.format("等待 RabbitMQ 确认被中断，topic(%s)", topic), e);
        } catch (ExecutionException e) {
            throw new ConfirmException(String.format("等待 RabbitMQ 确认失败，topic(%s)", topic), e);
        } catch (TimeoutException e) {
            throw new ConfirmException(String.format("等待 RabbitMQ 确认超时，topic(%s)", topic), e);
        }

        // 3.1 NACK
        if (!confirm.isAck()) {
            throw new ConfirmException(String.format("RabbitMQ 拒绝消息（nack），topic(%s) reason(%s)",
                    topic, confirm.getReason()), null);
        }
        // 3.2 ACK 但被退回：没有队列接收
        ReturnedMessage returned = correlationData.getReturned();
        if (returned != null) {
            throw new ConfirmException(String.format("RabbitMQ 消息无法路由（unroutable），topic(%s) reply(%s %s)",
                    topic, returned.getReplyCode(), returned.getReplyText()), null);
        }
    }

    /**
     * 校验连接工厂开启了 publisher confirm（CORRELATED，SIMPLE 不支持）、return，且确认超时时间大于 0
     */
    @Override
    public void validateConfirmable() {
        ConnectionFactory connectionFactory = confirmRabbitTemplate.getConnectionFactory();
        if (!connectionFactory.isPublisherConfirms() || !connectionFactory.isPublisherReturns()) {
            throw new IllegalStateException("RabbitMQ 确认发送需要配置 spring.rabbitmq.publisher-confirm-type=correlated、"
                    + "spring.rabbitmq.publisher-returns=true");
        }
        // 不大于 0 时，等待确认会立即超时，消息永远无法确认
        if (confirmTimeout.toMillis() <= 0) {
            throw new IllegalStateException(String.format("RabbitMQ 确认发送的超时时间(%s) 必须大于 0，"
                    + "请配置 yudao.iot.message-bus.confirm-timeout", confirmTimeout));
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 1.1 声明交换机、队列和绑定关系
        Queue queue = new Queue(subscriber.getGroup(), true, false, false);
        rabbitAdmin.declareQueue(queue);
        TopicExchange exchange = new TopicExchange(subscriber.getTopic());
        rabbitAdmin.declareExchange(exchange);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
        rabbitAdmin.declareBinding(binding);

        // 1.2 创建消费容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitTemplate.getConnectionFactory());
        container.setQueues(queue);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            try {
                subscriber.onMessage(JsonUtils.parseObject(message.getBody(), type));
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception ex) {
                // 特殊：确认发送失败（例如说补传的 pack 拆出的消息）时，首次投递重新入队，再投递一次；其它失败与原来一致，直接丢弃
                boolean requeue = !message.getMessageProperties().isRedelivered()
                        && ExceptionUtil.isCausedBy(ex, ConfirmException.class);
                if (requeue) {
                    log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 确认发送失败，重新入队]",
                            subscriber.getTopic(), subscriber.getGroup(), message, subscriber.getClass().getName(), ex);
                } else {
                    log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                            subscriber.getTopic(), subscriber.getGroup(), message, subscriber.getClass().getName(), ex);
                }
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, requeue);
            }
        });
        container.start();

        // 2. 保存消费者引用
        containers.add(container);
        subscribers.add(subscriber);
    }

    @PreDestroy
    public void destroy() {
        for (SimpleMessageListenerContainer container : containers) {
            try {
                container.stop();
                container.destroy();
                log.info("[destroy][关闭 RabbitMQ 消费者容器成功]");
            } catch (Exception e) {
                log.error("[destroy][关闭 RabbitMQ 消费者容器异常]", e);
            }
        }
    }

    /**
     * 确认发送失败：发送异常、NACK、无法路由、超时。消费者因它失败时重新入队一次，见 {@link #register}
     */
    private static class ConfirmException extends IllegalStateException {

        ConfirmException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}

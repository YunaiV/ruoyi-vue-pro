package cn.iocoder.yudao.module.iot.core.messagebus.core.rabbitmq;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 RabbitMQ 的 {@link IotMessageBus} 实现类
 *
 * @author ywc
 */
@RequiredArgsConstructor
@Slf4j
public class IotRabbitMQMessageBus implements IotMessageBus {

    private static final String ROUTING_KEY = "#";

    private final RabbitTemplate rabbitTemplate;

    private final RabbitAdmin rabbitAdmin;

    @Getter
    private final List<IotMessageSubscriber<?>> subscribers = new ArrayList<>();

    private final List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    @Override
    public void post(String topic, Object message) {
        rabbitTemplate.send(topic, ROUTING_KEY, MessageBuilder.withBody(JsonUtils.toJsonByte(message)).build());
        log.info("[post][topic({}) 发送消息({})]", topic, message);
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
                log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                        subscriber.getTopic(), subscriber.getGroup(), message, subscriber.getClass().getName(), ex);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
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

}

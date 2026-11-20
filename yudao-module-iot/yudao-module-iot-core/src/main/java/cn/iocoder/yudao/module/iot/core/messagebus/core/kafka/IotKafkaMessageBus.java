package cn.iocoder.yudao.module.iot.core.messagebus.core.kafka;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 基于 Kafka 的 {@link IotMessageBus} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class IotKafkaMessageBus implements IotMessageBus {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final KafkaProperties kafkaProperties;

    @Getter
    private final List<IotMessageSubscriber<?>> subscribers = new ArrayList<>();

    private final List<ConcurrentMessageListenerContainer<String, String>> containers = new ArrayList<>();

    /**
     * 生产者的 acks 是否需要 broker 确认：acks=0 时不等待 broker，{@link #postConfirmed} 无法确认
     */
    private final boolean brokerAcks;

    public IotKafkaMessageBus(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        Map<String, Object> producerProperties = buildProducerProperties(kafkaProperties);
        this.kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProperties));
        // 未配置时使用 kafka-clients 的默认值（3.x 为 all）
        Object acks = producerProperties.get(ProducerConfig.ACKS_CONFIG);
        this.brokerAcks = acks == null || ObjUtil.notEqual("0", StrUtil.trim(acks.toString()));
    }

    @Override
    public void post(String topic, Object message) {
        String messageJson = JsonUtils.toJsonString(message);
        try {
            kafkaTemplate.send(topic, messageJson).get();
            log.info("[post][topic({}) 发送消息({})]", topic, message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(String.format("发送 Kafka 消息失败，topic(%s) message(%s)", topic, message), e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(String.format("发送 Kafka 消息失败，topic(%s) message(%s)", topic, message), e);
        }
    }

    /**
     * 确认发送：{@link #post} 等待 send 的结果，acks 不为 0 时即 broker 已确认；
     * 失败等待时间由生产者的 max.block.ms、delivery.timeout.ms 限定（沿用用户配置）
     * <p>
     * 消息超过大小限制（RecordTooLargeException：客户端的 max.request.size，或 broker 的 message.max.bytes）时，
     * 重试也不会成功，抛出 {@link IotMessageRejectedException}；其它失败（含超时）按未确认抛出
     */
    @Override
    public void postConfirmed(String topic, Object message) {
        validateConfirmable();
        try {
            post(topic, message);
        } catch (RuntimeException e) {
            if (ExceptionUtil.isCausedBy(e, RecordTooLargeException.class)) {
                throw new IotMessageRejectedException(String.format("Kafka 消息超过大小限制，topic(%s)", topic), e);
            }
            throw e;
        }
    }

    @Override
    public void validateConfirmable() {
        if (!brokerAcks) {
            throw new IllegalStateException("Kafka 生产者的 acks=0，broker 不会确认，无法确认发送；请配置 spring.kafka.producer.acks 为 1 或 all");
        }
    }

    @Override
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 1. 创建消费容器
        ContainerProperties containerProperties = new ContainerProperties(subscriber.getTopic());
        containerProperties.setGroupId(subscriber.getGroup());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProperties.setMissingTopicsFatal(false);
        containerProperties.setMessageListener((AcknowledgingMessageListener<String, String>) (message, acknowledgment) -> {
            try {
                subscriber.onMessage(JsonUtils.parseObject(message.value(), type));
                acknowledgment.acknowledge();
            } catch (Exception ex) {
                log.error("[onMessage][topic({}/{}) message({}) 消费者({}) 处理异常]",
                        subscriber.getTopic(), subscriber.getGroup(), message, subscriber.getClass().getName(), ex);
                throw ex;
            }
        });
        ConcurrentMessageListenerContainer<String, String> container = new ConcurrentMessageListenerContainer<>(
                new DefaultKafkaConsumerFactory<>(buildConsumerProperties(kafkaProperties, subscriber.getGroup())),
                containerProperties);
        container.start();

        // 2. 保存消费者引用
        containers.add(container);
        subscribers.add(subscriber);
    }

    @PreDestroy
    public void destroy() {
        for (ConcurrentMessageListenerContainer<String, String> container : containers) {
            try {
                container.stop();
                log.info("[destroy][关闭 Kafka 消费者容器成功]");
            } catch (Exception e) {
                log.error("[destroy][关闭 Kafka 消费者容器异常]", e);
            }
        }
        kafkaTemplate.destroy();
    }

    private static Map<String, Object> buildProducerProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return properties;
    }

    private static Map<String, Object> buildConsumerProperties(KafkaProperties kafkaProperties, String group) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

}

package cn.iocoder.yudao.module.iot.core.messagebus.core.kafka;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
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

    public IotKafkaMessageBus(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(buildProducerProperties(kafkaProperties)));
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

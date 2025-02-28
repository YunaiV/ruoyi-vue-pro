package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotKafkaMQDataBridgeExecute extends AbstractCacheableDataBridgeExecute {

    @Override
    public void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge) {
        // 1.1 校验数据桥梁的类型 == KAFKA
        if (!IotDataBridgTypeEnum.KAFKA.getType().equals(dataBridge.getType())) {
            return;
        }
        // 1.2 执行 Kafka 发送消息
        executeKafka(message, (IotDataBridgeDO.KafkaMQConfig) dataBridge.getConfig());
    }

    private void executeKafka(IotDeviceMessage message, IotDataBridgeDO.KafkaMQConfig config) {
        try {
            // 1. 获取或创建 KafkaTemplate
            KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) getProducer(config);

            // 2. 发送消息并等待结果
            kafkaTemplate.send(config.getTopic(), message.toString())
                    .get(10, TimeUnit.SECONDS); // 添加超时等待
            log.info("[executeKafka][message({}) 发送成功]", message);
        } catch (TimeoutException e) {
            log.error("[executeKafka][message({}) config({}) 发送超时]", message, config, e);
        } catch (Exception e) {
            log.error("[executeKafka][message({}) config({}) 发送异常]", message, config, e);
        }
    }

    @Override
    protected Object initProducer(Object config) {
        IotDataBridgeDO.KafkaMQConfig kafkaConfig = (IotDataBridgeDO.KafkaMQConfig) config;

        // 1.1 构建生产者配置
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 1.2 如果配置了认证信息
        if (kafkaConfig.getUsername() != null && kafkaConfig.getPassword() != null) {
            props.put("security.protocol", "SASL_PLAINTEXT");
            props.put("sasl.mechanism", "PLAIN");
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                            + kafkaConfig.getUsername() + "\" password=\"" + kafkaConfig.getPassword() + "\";");
        }

        // 1.3 如果启用 SSL
        if (Boolean.TRUE.equals(kafkaConfig.getSsl())) {
            props.put("security.protocol", "SSL");
        }

        // 2. 创建 KafkaTemplate
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Override
    protected void closeProducer(Object producer) {
        if (producer instanceof KafkaTemplate) {
            ((KafkaTemplate<?, ?>) producer).destroy();
        }
    }

    // TODO @芋艿：测试代码，后续清理
    public static void main(String[] args) {
        // 1. 创建一个共享的实例
        IotKafkaMQDataBridgeExecute action = new IotKafkaMQDataBridgeExecute();

        // 2. 创建共享的配置
        IotDataBridgeDO.KafkaMQConfig config = new IotDataBridgeDO.KafkaMQConfig();
        config.setBootstrapServers("127.0.0.1:9092");
        config.setTopic("test-topic");
        config.setSsl(false);
        config.setUsername(null);
        config.setPassword(null);

        // 3. 创建共享的消息
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("TEST-001")
                .productKey("testProduct")
                .deviceName("testDevice")
                .deviceKey("testDeviceKey")
                .type("property")
                .identifier("temperature")
                .data("{\"value\": 60}")
                .reportTime(LocalDateTime.now())
                .tenantId(1L)
                .build();

        // 4. 执行两次测试，验证缓存
        log.info("[main][第一次执行，应该会创建新的 producer]");
        action.executeKafka(message, config);

        log.info("[main][第二次执行，应该会复用缓存的 producer]");
        action.executeKafka(message, config);
    }

}

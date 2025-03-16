package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeKafkaMQConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
@Component
@Slf4j
public class IotKafkaMQDataBridgeExecute extends
        AbstractCacheableDataBridgeExecute<IotDataBridgeKafkaMQConfig, KafkaTemplate<String, String>> {

    private static final Duration SEND_TIMEOUT = Duration.ofMillis(10000); // 10 秒超时时间

    @Override
    public Integer getType() {
        return IotDataBridgeTypeEnum.KAFKA.getType();
    }

    @Override
    public void execute0(IotDeviceMessage message, IotDataBridgeKafkaMQConfig config) throws Exception {
        // 1. 获取或创建 KafkaTemplate
        KafkaTemplate<String, String> kafkaTemplate = getProducer(config);

        // 2. 发送消息并等待结果
        kafkaTemplate.send(config.getTopic(), message.toString())
                .get(SEND_TIMEOUT.getSeconds(), TimeUnit.SECONDS); // 添加超时等待
        log.info("[execute0][message({}) 发送成功]", message);
    }

    @Override
    protected KafkaTemplate<String, String> initProducer(IotDataBridgeKafkaMQConfig config) {
        // 1.1 构建生产者配置
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 1.2 如果配置了认证信息
        if (config.getUsername() != null && config.getPassword() != null) {
            props.put("security.protocol", "SASL_PLAINTEXT");
            props.put("sasl.mechanism", "PLAIN");
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                            + config.getUsername() + "\" password=\"" + config.getPassword() + "\";");
        }
        // 1.3 如果启用 SSL
        if (Boolean.TRUE.equals(config.getSsl())) {
            props.put("security.protocol", "SSL");
        }

        // 2. 创建 KafkaTemplate
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Override
    protected void closeProducer(KafkaTemplate<String, String> producer) {
        producer.destroy();
    }

}

package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeKafkaMQConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
//@Component
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

    // TODO @芋艿：测试代码，后续清理
    public static void main(String[] args) {
        // 1. 创建一个共享的实例
        IotKafkaMQDataBridgeExecute action = new IotKafkaMQDataBridgeExecute();

        // 2. 创建共享的配置
        IotDataBridgeKafkaMQConfig config = new IotDataBridgeKafkaMQConfig();
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
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[main][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

}

package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkKafkaConfig;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 的 {@link IotDataRuleAction} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
@Component
@Slf4j
public class IotKafkaDataRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkKafkaConfig, KafkaTemplate<String, String>> {

    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.KAFKA.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkKafkaConfig config) throws Exception {
        try {
            // 1. 获取或创建 KafkaTemplate
            KafkaTemplate<String, String> kafkaTemplate = getProducer(config);

            // 2. 发送消息并等待结果
            SendResult<String, String> sendResult = kafkaTemplate.send(config.getTopic(), JsonUtils.toJsonString(message))
                    .get(SEND_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            // 3. 处理发送结果
            if (sendResult != null && sendResult.getRecordMetadata() != null) {
                log.info("[execute][message({}) config({}) 发送成功，结果: partition={}, offset={}, timestamp={}]",
                        message, config,
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset(),
                        sendResult.getRecordMetadata().timestamp());
            } else {
                log.warn("[execute][message({}) config({}) 发送结果为空]", message, config);
            }
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 发送失败]", message, config, e);
            throw e;
        }
    }

    @Override
    protected KafkaTemplate<String, String> initProducer(IotDataSinkKafkaConfig config) {
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

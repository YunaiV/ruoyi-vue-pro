package cn.iocoder.yudao.framework.sse.core.sender.kafka;

import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutionException;

/**
 * 基于 Kafka 的 {@link SseMessageSender} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class KafkaSseMessageSender extends SseMessageSender {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    private final String topic;

    public KafkaSseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures,
                                 KafkaTemplate<Object, Object> kafkaTemplate, String topic) {
        super(sessionManager, sendTimeoutMillis, maxSendFailures);
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    protected void doPublish(SseMessage message) {
        try {
            kafkaTemplate.send(topic, message).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[doPublish][等待 Kafka 发送结果时被中断]", e);
        } catch (ExecutionException e) {
            log.error("[doPublish][发送消息({}) 到 Kafka 失败]", message, e);
        }
    }

}

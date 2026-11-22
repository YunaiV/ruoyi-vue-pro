package cn.iocoder.yudao.framework.sse.core.sender.kafka;

import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * {@link SseMessage} 的 Kafka 广播消息的消费者，投递到本节点的 SSE 连接
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class KafkaSseMessageConsumer {

    private final KafkaSseMessageSender kafkaSseMessageSender;

    @KafkaListener(
            topics = "${yudao.sse.sender-kafka.topic}",
            // 在 Group 上，使用 UUID 生成其后缀。这样，启动的 Consumer 的 Group 不同，以达到广播消费的目的
            groupId = "${yudao.sse.sender-kafka.consumer-group}" + "-" + "#{T(java.util.UUID).randomUUID()}")
    public void onMessage(SseMessage message) {
        kafkaSseMessageSender.deliver(message);
    }

}

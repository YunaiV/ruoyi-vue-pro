package cn.iocoder.yudao.framework.sse.core.sender.rabbitmq;

import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;

/**
 * {@link SseMessage} 的 RabbitMQ 广播消息的消费者，投递到本节点的 SSE 连接
 *
 * @author 芋道源码
 */
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(
                        // 在 Queue 的名字上，使用 UUID 生成其后缀。这样，启动的 Consumer 的 Queue 不同，以达到广播消费的目的
                        name = "${yudao.sse.sender-rabbitmq.queue}" + "-" + "#{T(java.util.UUID).randomUUID()}",
                        // Consumer 关闭时，该队列就可以被自动删除了
                        autoDelete = "true"
                ),
                exchange = @Exchange(
                        name = "${yudao.sse.sender-rabbitmq.exchange}",
                        type = ExchangeTypes.TOPIC,
                        declare = "false"
                )
        )
)
@RequiredArgsConstructor
public class RabbitMQSseMessageConsumer {

    private final RabbitMQSseMessageSender rabbitMQSseMessageSender;

    @RabbitHandler
    public void onMessage(SseMessage message) {
        rabbitMQSseMessageSender.deliver(message);
    }

}

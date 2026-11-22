package cn.iocoder.yudao.framework.sse.core.sender.rocketmq;

import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * {@link SseMessage} 的 RocketMQ 广播消息的消费者，投递到本节点的 SSE 连接
 *
 * @author 芋道源码
 */
@RocketMQMessageListener( // 重点：添加 @RocketMQMessageListener 注解，声明消费的 topic
        topic = "${yudao.sse.sender-rocketmq.topic}",
        consumerGroup = "${yudao.sse.sender-rocketmq.consumer-group}",
        messageModel = MessageModel.BROADCASTING // 设置为广播模式，保证每个实例都能收到消息
)
@RequiredArgsConstructor
public class RocketMQSseMessageConsumer implements RocketMQListener<SseMessage> {

    private final RocketMQSseMessageSender rocketMQSseMessageSender;

    @Override
    public void onMessage(SseMessage message) {
        rocketMQSseMessageSender.deliver(message);
    }

}

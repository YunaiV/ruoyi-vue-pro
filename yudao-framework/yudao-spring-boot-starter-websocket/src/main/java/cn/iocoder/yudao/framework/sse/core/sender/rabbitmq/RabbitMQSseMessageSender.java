package cn.iocoder.yudao.framework.sse.core.sender.rabbitmq;

import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 基于 RabbitMQ 的 {@link SseMessageSender} 实现类
 *
 * @author 芋道源码
 */
public class RabbitMQSseMessageSender extends SseMessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final TopicExchange topicExchange;

    public RabbitMQSseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures,
                                    RabbitTemplate rabbitTemplate, TopicExchange topicExchange) {
        super(sessionManager, sendTimeoutMillis, maxSendFailures);
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    @Override
    protected void doPublish(SseMessage message) {
        rabbitTemplate.convertAndSend(topicExchange.getName(), null, message);
    }

}

package cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketPushMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 基于 RabbitMQ 的 {@link WebSocketMessageSender} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class RabbitMQWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final TopicExchange topicExchange;

    public RabbitMQWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                          RabbitTemplate rabbitTemplate,
                                          TopicExchange topicExchange) {
        super(sessionManager);
        this.rabbitTemplate = rabbitTemplate;
        this.topicExchange = topicExchange;
    }

    /**
     * 通过 RabbitMQ 广播消息
     *
     * @param message 消息
     */
    @Override
    protected void doPublish(WebSocketPushMessage message) {
        RabbitMQWebSocketMessage mqMessage = BeanUtils.toBean(message, RabbitMQWebSocketMessage.class);
        rabbitTemplate.convertAndSend(topicExchange.getName(), null, mqMessage);
    }

}

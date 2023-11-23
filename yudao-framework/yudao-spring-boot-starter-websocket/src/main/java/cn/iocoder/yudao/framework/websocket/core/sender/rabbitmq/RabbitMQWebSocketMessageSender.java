package cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq;

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

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        sendRabbitMQMessage(null, userId, userType, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        sendRabbitMQMessage(null, null, userType, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        sendRabbitMQMessage(sessionId, null, null, messageType, messageContent);
    }

    /**
     * 通过 RabbitMQ 广播消息
     *
     * @param sessionId Session 编号
     * @param userId 用户编号
     * @param userType 用户类型
     * @param messageType 消息类型
     * @param messageContent 消息内容
     */
    private void sendRabbitMQMessage(String sessionId, Long userId, Integer userType,
                                     String messageType, String messageContent) {
        RabbitMQWebSocketMessage mqMessage = new RabbitMQWebSocketMessage()
                .setSessionId(sessionId).setUserId(userId).setUserType(userType)
                .setMessageType(messageType).setMessageContent(messageContent);
        rabbitTemplate.convertAndSend(topicExchange.getName(), null, mqMessage);
    }

}

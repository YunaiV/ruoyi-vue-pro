package cn.iocoder.yudao.framework.websocket.core.sender.rocketmq;

import cn.iocoder.yudao.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

/**
 * 基于 RocketMQ 的 {@link WebSocketMessageSender} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class RocketMQWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RocketMQTemplate rocketMQTemplate;

    private final String topic;

    public RocketMQWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                          RocketMQTemplate rocketMQTemplate,
                                          String topic) {
        super(sessionManager);
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        sendRocketMQMessage(null, userId, userType, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        sendRocketMQMessage(null, null, userType, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        sendRocketMQMessage(sessionId, null, null, messageType, messageContent);
    }

    /**
     * 通过 RocketMQ 广播消息
     *
     * @param sessionId Session 编号
     * @param userId 用户编号
     * @param userType 用户类型
     * @param messageType 消息类型
     * @param messageContent 消息内容
     */
    private void sendRocketMQMessage(String sessionId, Long userId, Integer userType,
                                     String messageType, String messageContent) {
        RocketMQWebSocketMessage mqMessage = new RocketMQWebSocketMessage()
                .setSessionId(sessionId).setUserId(userId).setUserType(userType)
                .setMessageType(messageType).setMessageContent(messageContent);
        rocketMQTemplate.syncSend(topic, mqMessage);
    }

}

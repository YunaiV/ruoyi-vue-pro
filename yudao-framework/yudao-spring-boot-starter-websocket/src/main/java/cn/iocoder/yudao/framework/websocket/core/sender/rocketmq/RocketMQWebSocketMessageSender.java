package cn.iocoder.yudao.framework.websocket.core.sender.rocketmq;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketPushMessage;
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

    /**
     * 通过 RocketMQ 广播消息
     *
     * @param message 消息
     */
    @Override
    protected void doPublish(WebSocketPushMessage message) {
        RocketMQWebSocketMessage mqMessage = BeanUtils.toBean(message, RocketMQWebSocketMessage.class);
        rocketMQTemplate.syncSend(topic, mqMessage);
    }

}

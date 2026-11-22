package cn.iocoder.yudao.framework.websocket.core.sender.redis;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketPushMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Redis 的 {@link WebSocketMessageSender} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class RedisWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RedisMQTemplate redisMQTemplate;

    public RedisWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                       RedisMQTemplate redisMQTemplate) {
        super(sessionManager);
        this.redisMQTemplate = redisMQTemplate;
    }

    /**
     * 通过 Redis 广播消息
     *
     * @param message 消息
     */
    @Override
    protected void doPublish(WebSocketPushMessage message) {
        RedisWebSocketMessage mqMessage = BeanUtils.toBean(message, RedisWebSocketMessage.class);
        redisMQTemplate.send(mqMessage);
    }

}

package cn.iocoder.yudao.framework.sse.core.sender.redis;

import cn.iocoder.yudao.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import lombok.Data;

/**
 * Redis 广播 SSE 的消息
 *
 * Channel 默认使用类名，与 WebSocket 的 RedisWebSocketMessage 天然隔离
 *
 * @author 芋道源码
 */
@Data
public class RedisSseMessage extends AbstractRedisChannelMessage {

    /**
     * SSE 的消息
     */
    private SseMessage message;

}

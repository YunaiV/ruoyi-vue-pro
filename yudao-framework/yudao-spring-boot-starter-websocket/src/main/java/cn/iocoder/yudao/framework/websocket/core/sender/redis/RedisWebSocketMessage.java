package cn.iocoder.yudao.framework.websocket.core.sender.redis;

import cn.iocoder.yudao.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import lombok.Data;

/**
 * Redis 广播 WebSocket 的消息
 */
@Data
public class RedisWebSocketMessage extends AbstractRedisChannelMessage {

    /**
     * Session 编号
     */
    private String sessionId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 事件编号
     */
    private String eventId;
    /**
     * 租户编号
     */
    private Long tenantId;
    /**
     * 发送时间，单位：毫秒
     */
    private Long timestamp;

}

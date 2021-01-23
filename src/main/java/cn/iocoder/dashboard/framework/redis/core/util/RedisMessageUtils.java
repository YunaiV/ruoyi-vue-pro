package cn.iocoder.dashboard.framework.redis.core.util;

import cn.iocoder.dashboard.framework.redis.core.pubsub.ChannelMessage;
import cn.iocoder.dashboard.util.json.JSONUtils;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 消息工具类
 *
 * @author 芋道源码
 */
public class RedisMessageUtils {

    /**
     * 发送 Redis 消息，基于 Redis pub/sub 实现
     *
     * @param redisTemplate Redis 操作模板
     * @param message 消息
     */
    public static <T extends ChannelMessage>  void sendChannelMessage(RedisTemplate<?, ?> redisTemplate, T message) {
        redisTemplate.convertAndSend(message.getChannel(), JSONUtils.toJSONString(message));
    }

}

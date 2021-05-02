package cn.iocoder.yudao.framework.redis.core.util;

import cn.iocoder.yudao.framework.redis.core.pubsub.ChannelMessage;
import cn.iocoder.yudao.framework.redis.core.stream.StreamMessage;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
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
    public static <T extends ChannelMessage> void sendChannelMessage(RedisTemplate<?, ?> redisTemplate, T message) {
        redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
    }

    /**
     * 发送 Redis 消息，基于 Redis Stream 实现
     *
     * @param redisTemplate Redis 操作模板
     * @param message 消息
     * @return 消息记录的编号对象
     */
    public static <T extends StreamMessage> RecordId sendStreamMessage(RedisTemplate<String, ?> redisTemplate, T message) {
        return redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(message)) // 设置内容
                .withStreamKey(message.getStreamKey())); // 设置 stream key
    }

}

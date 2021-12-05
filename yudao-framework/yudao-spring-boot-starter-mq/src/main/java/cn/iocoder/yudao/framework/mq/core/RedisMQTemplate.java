package cn.iocoder.yudao.framework.mq.core;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.core.interceptor.RedisMessageInterceptor;
import cn.iocoder.yudao.framework.mq.core.message.AbstractRedisMessage;
import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessage;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis MQ 操作模板类
 *
 * @author 芋道源码
 */
@AllArgsConstructor
public class RedisMQTemplate {

    @Getter
    private final RedisTemplate<String, ?> redisTemplate;
    /**
     * 拦截器数组
     */
    @Getter
    private final List<RedisMessageInterceptor> interceptors = new ArrayList<>();

    /**
     * 发送 Redis 消息，基于 Redis pub/sub 实现
     *
     * @param message 消息
     */
    public <T extends AbstractChannelMessage> void send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 发送 Redis 消息，基于 Redis Stream 实现
     *
     * @param message 消息
     * @return 消息记录的编号对象
     */
    public <T extends AbstractStreamMessage> RecordId send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            return redisTemplate.opsForStream().add(StreamRecords.newRecord()
                    .ofObject(JsonUtils.toJsonString(message)) // 设置内容
                    .withStreamKey(message.getStreamKey())); // 设置 stream key
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(RedisMessageInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    private void sendMessageBefore(AbstractRedisMessage message) {
        // 正序
        interceptors.forEach(interceptor -> interceptor.sendMessageBefore(message));
    }

    private void sendMessageAfter(AbstractRedisMessage message) {
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).sendMessageAfter(message);
        }
    }

}

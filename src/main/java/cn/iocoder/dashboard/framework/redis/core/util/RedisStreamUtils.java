package cn.iocoder.dashboard.framework.redis.core.util;

import cn.iocoder.dashboard.modules.system.redis.stream.sms.SmsSendMessage;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 消息工具类
 *
 * @author 芋道源码
 */
public class RedisStreamUtils {

    public static final String KEY_SMS_SEND = "stream_sms_send";

    public static final String GROUP_SMS_SEND = "group_sms_send";

    /**
     * 发送 Redis 消息，基于 Redis pub/sub 实现
     *
     * @param redisTemplate Redis 操作模板
     * @param message       消息
     */
    public static void sendChannelMessage(RedisTemplate<String, ?> redisTemplate, SmsSendMessage message) {

        redisTemplate.opsForStream().add(StreamRecords.newRecord().ofObject(message).withStreamKey(KEY_SMS_SEND));
    }

}

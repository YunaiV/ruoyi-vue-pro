package cn.iocoder.yudao.framework.mq.redis.core.job;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Redis Stream 消息清理任务
 * 用于定期清理已消费的消息，防止内存占用过大
 *
 * @see <a href="https://www.cnblogs.com/nanxiang/p/16179519.html">记一次 redis stream 数据类型内存不释放问题</a>
 *
 * @author 芋道源码
 */
@Slf4j
@AllArgsConstructor
public class RedisStreamMessageCleanupJob {

    /**
     * 业务 MQ（Spring 容器内 AbstractRedisStreamMessageListener）清理任务使用的分布式锁
     */
    public static final String DEFAULT_CLEANUP_LOCK_KEY = "redis:stream:message-cleanup:lock";

    /**
     * IoT Redis 总线清理任务使用的分布式锁（须与 {@link #DEFAULT_CLEANUP_LOCK_KEY} 区分，否则会共抢一把锁，
     * 同一时刻只有一侧能执行 XTRIM，另一侧 Stream 可能无限积压）
     */
    public static final String IOT_CLEANUP_LOCK_KEY = "redis:stream:message-cleanup:lock:iot";

    /**
     * 保留的消息数量，默认保留最近 10000 条消息
     */
    private static final long MAX_COUNT = 10000;

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisTemplate;
    private final RedissonClient redissonClient;
    /**
     * Redisson 锁键（多 Bean 注册清理任务时必须各不相同）
     */
    private final String cleanupLockKey;

    /**
     * 每小时执行一次清理任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanup() {
        RLock lock = redissonClient.getLock(cleanupLockKey);
        if (lock.tryLock()) {
            try {
                execute();
            } catch (Exception ex) {
                log.error("[cleanup][执行异常][lockKey={}]", cleanupLockKey, ex);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            log.debug("[cleanup][未获取到锁，跳过本轮][lockKey={}]", cleanupLockKey);
        }
    }

    /**
     * 执行清理逻辑
     */
    private void execute() {
        StreamOperations<String, Object, Object> ops = redisTemplate.getRedisTemplate().opsForStream();
        listeners.forEach(listener -> {
            try {
                // 使用 XTRIM MAXLEN 精确裁剪（approximate=false），避免 ~ 模式下长期明显高于上限
                Long trimCount = ops.trim(listener.getStreamKey(), MAX_COUNT, false);
                if (trimCount != null && trimCount > 0) {
                    log.info("[execute][Stream({}) 清理消息数量({})]", listener.getStreamKey(), trimCount);
                }
            } catch (Exception ex) {
                log.error("[execute][Stream({}) 清理异常]", listener.getStreamKey(), ex);
            }
        });
    }
}
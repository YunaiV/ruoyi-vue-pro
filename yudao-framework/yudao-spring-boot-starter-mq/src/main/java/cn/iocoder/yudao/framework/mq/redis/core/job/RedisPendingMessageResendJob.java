package cn.iocoder.yudao.framework.mq.redis.core.job;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 这个任务用于处理，crash 之后的消费者未消费完的消息
 */
@Slf4j
@AllArgsConstructor
public class RedisPendingMessageResendJob {

    private static final String LOCK_KEY = "redis:pending:msg:lock";

    /**
     * 消息超时时间，默认 5 分钟
     *
     * 1. 超时的消息才会被重新投递
     * 2. 由于定时任务 1 分钟一次，消息超时后不会被立即重投，极端情况下消息5分钟过期后，再等 1 分钟才会被扫瞄到
     */
    private static final int EXPIRE_TIME = 5 * 60;

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisTemplate;
    private final String groupName;
    private final RedissonClient redissonClient;

    /**
     * 一分钟执行一次,这里选择每分钟的35秒执行，是为了避免整点任务过多的问题
     */
    @Scheduled(cron = "35 * * * * ?")
    public void messageResend() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        // 尝试加锁
        if (lock.tryLock()) {
            try {
                execute();
            } catch (Exception ex) {
                log.error("[messageResend][执行异常]", ex);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 执行清理逻辑
     *
     * @see <a href="https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/480/files">讨论</a>
     */
    private void execute() {
        StreamOperations<String, Object, Object> ops = redisTemplate.getRedisTemplate().opsForStream();
        listeners.forEach(listener -> {
            PendingMessagesSummary pendingMessagesSummary = Objects.requireNonNull(ops.pending(listener.getStreamKey(), groupName));
            // 每个消费者的 pending 队列消息数量
            Map<String, Long> pendingMessagesPerConsumer = pendingMessagesSummary.getPendingMessagesPerConsumer();
            pendingMessagesPerConsumer.forEach((consumerName, pendingMessageCount) -> {
                log.info("[processPendingMessage][消费者({}) 消息数量({})]", consumerName, pendingMessageCount);
                // 每个消费者的 pending消息的详情信息
                PendingMessages pendingMessages = ops.pending(listener.getStreamKey(), Consumer.from(groupName, consumerName), Range.unbounded(), pendingMessageCount);
                if (pendingMessages.isEmpty()) {
                    return;
                }
                pendingMessages.forEach(pendingMessage -> {
                    // 获取消息上一次传递到 consumer 的时间,
                    long lastDelivery = pendingMessage.getElapsedTimeSinceLastDelivery().getSeconds();
                    if (lastDelivery < EXPIRE_TIME){
                        return;
                    }
                    // 获取指定 id 的消息体
                    List<MapRecord<String, Object, Object>> records = ops.range(listener.getStreamKey(),
                            Range.of(Range.Bound.inclusive(pendingMessage.getIdAsString()), Range.Bound.inclusive(pendingMessage.getIdAsString())));
                    if (CollUtil.isEmpty(records)) {
                        return;
                    }
                    // 重新投递消息
                    redisTemplate.getRedisTemplate().opsForStream().add(StreamRecords.newRecord()
                            .ofObject(records.get(0).getValue()) // 设置内容
                            .withStreamKey(listener.getStreamKey()));
                    // ack 消息消费完成
                    redisTemplate.getRedisTemplate().opsForStream().acknowledge(groupName, records.get(0));
                    log.info("[processPendingMessage][消息({})重新投递成功]", records.get(0).getId());
                });
            });
        });
    }
}

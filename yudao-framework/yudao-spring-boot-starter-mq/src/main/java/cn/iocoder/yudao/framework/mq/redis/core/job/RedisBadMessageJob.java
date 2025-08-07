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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 这个任务用于处理消费者 crash 之后长时间未 ACK 的消息
 */
@Slf4j
@AllArgsConstructor
public class RedisBadMessageJob {

    private static final String LOCK_KEY = "redis:stream:pending-message-resend:lock";

    /**
     * 消息超时时间，默认 5 分钟
     *
     * ACK 超时的消息会被记录为坏消息（死信）
     */
    private static final int BAD_MESSAGE_SECONDS = 5 * 60;
    private static final Duration BAD_MESSAGE_DURATION = Duration.ofSeconds(BAD_MESSAGE_SECONDS);
    /**
     * 消息重试上限（不准确，因为是靠定时任务限制，而 {@link AbstractRedisStreamMessageListener#onMessage(ObjectRecord)}并不限制消费次数）
     */
    private static final long MAX_DELIVERY_COUNT = 3;
    /**
     * 用于存放坏消息的消费者
     */
    private static final String BAD_MESSAGE_CONSUMER = "__BAD_MESSAGE_CONSUMER__";

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisTemplate;
    private final String groupName;
    private final RedissonClient redissonClient;

    /**
     * 一分钟执行一次,这里选择每分钟的 35 秒执行，是为了避免整点任务过多的问题
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
     * 执行坏消息逻辑
     *
     * @see <a href="https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/480/files">讨论</a>
     */
    private void execute() {
        StreamOperations<String, Object, Object> ops = redisTemplate.getRedisTemplate().opsForStream();
        listeners.forEach(listener -> {
            String streamKey = listener.getStreamKey();
            PendingMessagesSummary pendingMessagesSummary = Objects.requireNonNull(ops.pending(streamKey, groupName));
            // 每个消费者的 pending 队列消息数量
            Map<String, Long> pendingMessagesPerConsumer = pendingMessagesSummary.getPendingMessagesPerConsumer();
            pendingMessagesPerConsumer.forEach((consumerName, pendingMessageCount) -> {
                if (BAD_MESSAGE_CONSUMER.equals(consumerName)) {
                    return;
                }
                log.info("[processPendingMessage][消费者({}) 消息数量({})]", consumerName, pendingMessageCount);
                // 每个消费者的 pending 消息的详情信息
                PendingMessages pendingMessages = ops.pending(streamKey, Consumer.from(groupName, consumerName), Range.unbounded(), pendingMessageCount);
                if (pendingMessages.isEmpty()) {
                    return;
                }
                for (PendingMessage pendingMessage : pendingMessages) {
                    boolean lastDeliveryTimeout = pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(BAD_MESSAGE_DURATION) > 0;
                    if (!lastDeliveryTimeout && pendingMessage.getTotalDeliveryCount() < MAX_DELIVERY_COUNT) {
                        continue;
                    }
                    // 获取指定 id 的消息体
                    List<MapRecord<String, Object, Object>> records = ops.range(streamKey, rangeEquals(pendingMessage.getIdAsString()));
                    if (CollUtil.isEmpty(records)) {
                        // 消息已被删除
                        continue;
                    }
                    // 转移到坏消息消费者
                    ops.claim(streamKey, groupName, BAD_MESSAGE_CONSUMER, Duration.ZERO, pendingMessage.getId());
                    // 打日志记录消息体，方便人工重投
                    long cancelAt = System.currentTimeMillis();
                    long lastDeliveryAt = cancelAt - pendingMessage.getElapsedTimeSinceLastDelivery().toMillis();
                    log.error("[Redis Stream 消息队列 发现坏消息] msgId={} msgVal={} lastDeliveryAt={} cancelAt={}",
                            pendingMessage.getId(), records.get(0).getValue(), lastDeliveryAt, cancelAt);
                }
            });
        });
    }

    private static Range<String> rangeEquals(String messageId) {
        return Range.of(Range.Bound.inclusive(messageId), Range.Bound.inclusive(messageId));
    }

}

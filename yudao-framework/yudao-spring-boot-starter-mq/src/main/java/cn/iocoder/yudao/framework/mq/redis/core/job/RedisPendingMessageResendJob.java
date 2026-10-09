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

    public static final String DEFAULT_RESEND_LOCK_KEY = "redis:stream:pending-message-resend:lock";

    public static final String IOT_RESEND_LOCK_KEY = "redis:stream:pending-message-resend:lock:iot";

    /**
     * 消息超时时间，默认 5 分钟
     *
     * 1. 超时的消息才会被重新投递
     * 2. 由于定时任务 1 分钟一次，消息超时后不会被立即重投，极端情况下消息 5 分钟过期后，再等 1 分钟才会被扫瞄到
     */
    private static final int EXPIRE_TIME = 5 * 60;

    /**
     * 每轮每个消费者最多扫描的 pending 条目数
     *
     * 1. 限制单次 XPENDING 的返回规模：pending 大量积压时，全量扫描的单次调用可达数百毫秒，
     *    叠加每分钟一轮会持续占死 Redis；
     * 2. 未扫到的条目由后续轮次继续处理（每分钟一轮），配合下方“消息体已被裁剪直接 ack”
     *    的逻辑，pending 会持续排空而不是只增不减。
     */
    private static final int PENDING_BATCH_SIZE = 1000;

    /**
     * 每个消费分组 pending 条目数的上限（自我保护）
     *
     * 1. 基础设施持续故障（例如数据库/Redis 故障）时，所有消费失败的消息都会滞留 pending，
     *    此时重试反而会放大故障；
     * 2. 超过上限后，任务直接 ack（丢弃）最老的超出条目，保证 pending 不会无界增长占死 Redis；
     * 3. 丢弃时打印 ERROR 日志便于运维发现。默认 20 万，可通过配置项 yudao.mq.pending-message-max-count 调整
     */
    public static final long DEFAULT_PENDING_MAX_COUNT = 200_000L;

    /**
     * 每轮最多丢弃的超出条目数（XPENDING/XACK 是轻量命令，限制批量以控制单轮耗时）
     */
    private static final int PENDING_DISCARD_BATCH = 10000;

    private final List<AbstractRedisStreamMessageListener<?>> listeners;
    private final RedisMQTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final String resendLockKey;
    private final long pendingMaxCount;

    /**
     * 一分钟执行一次,这里选择每分钟的 35 秒执行，是为了避免整点任务过多的问题
     */
    @Scheduled(cron = "35 * * * * ?")
    public void messageResend() {
        RLock lock = redissonClient.getLock(resendLockKey);
        if (lock.tryLock()) {
            try {
                execute();
            } catch (Exception ex) {
                log.error("[messageResend][执行异常][lockKey={}]", resendLockKey, ex);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            log.debug("[messageResend][未获取到锁，跳过本轮][lockKey={}]", resendLockKey);
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
            PendingMessagesSummary pendingMessagesSummary = Objects.requireNonNull(ops.pending(listener.getStreamKey(), listener.getGroup()));
            // 自我保护：pending 总数超过上限时，直接 ack（丢弃）最老的超出条目。
            // 没有这一步，基础设施持续故障时 pending 会无界增长，每轮扫描的成本最终拖垮 Redis
            long totalPending = pendingMessagesSummary.getTotalPendingMessages();
            if (totalPending > pendingMaxCount) {
                int discardCount = (int) Math.min(totalPending - pendingMaxCount, PENDING_DISCARD_BATCH);
                PendingMessages oldest = ops.pending(listener.getStreamKey(), listener.getGroup(),
                        Range.unbounded(), discardCount);
                oldest.forEach(pendingMessage -> redisTemplate.getRedisTemplate().opsForStream()
                        .acknowledge(listener.getStreamKey(), listener.getGroup(), pendingMessage.getIdAsString()));
                log.error("[processPendingMessage][消费分组({}) pending 总数({}) 超过上限({})，已丢弃（确认）最老的 {} 条未处理消息，请尽快排查消费失败原因]",
                        listener.getGroup(), totalPending, pendingMaxCount, oldest.size());
            }
            // 每个消费者的 pending 队列消息数量
            Map<String, Long> pendingMessagesPerConsumer = pendingMessagesSummary.getPendingMessagesPerConsumer();
            pendingMessagesPerConsumer.forEach((consumerName, pendingMessageCount) -> {
                log.info("[processPendingMessage][消费者({}) 消息数量({})]", consumerName, pendingMessageCount);
                // 每个消费者的 pending消息的详情信息
                PendingMessages pendingMessages = ops.pending(listener.getStreamKey(), Consumer.from(listener.getGroup(), consumerName), Range.unbounded(), PENDING_BATCH_SIZE);
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
                        // 消息体已被 RedisStreamMessageCleanupJob 裁剪：无法重新投递，直接 ack 清除该
                        // pending 条目；否则条目永久滞留，XPENDING 的扫描成本随 pending 数量线性增长
                        redisTemplate.getRedisTemplate().opsForStream().acknowledge(listener.getStreamKey(),
                                listener.getGroup(), pendingMessage.getIdAsString());
                        return;
                    }
                    // 重新投递消息
                    redisTemplate.getRedisTemplate().opsForStream().add(StreamRecords.newRecord()
                            .ofObject(records.get(0).getValue()) // 设置内容
                            .withStreamKey(listener.getStreamKey()));
                    // ack 消息消费完成
                    redisTemplate.getRedisTemplate().opsForStream().acknowledge(listener.getGroup(), records.get(0));
                    log.info("[processPendingMessage][消息({})重新投递成功]", records.get(0).getId());
                });
            });
        });
    }
}

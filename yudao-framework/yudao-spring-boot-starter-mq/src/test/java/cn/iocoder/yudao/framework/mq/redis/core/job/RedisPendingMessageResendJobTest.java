package cn.iocoder.yudao.framework.mq.redis.core.job;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessage;
import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link RedisPendingMessageResendJob} 的单元测试类
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class RedisPendingMessageResendJobTest {

    private static final String STREAM_KEY = "test_stream";
    private static final String GROUP = "test_group";
    private static final String CONSUMER_NAME = "test_consumer";

    private RedisPendingMessageResendJob resendJob;

    @Mock
    private RedisMQTemplate redisMQTemplate;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private StreamOperations<String, Object, Object> streamOperations;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock lock;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setUp() {
        AbstractRedisStreamMessageListener<?> listener = new AbstractRedisStreamMessageListener<>(STREAM_KEY, GROUP) {

            @Override
            public void onMessage(AbstractRedisStreamMessage message) {
            }

        };
        resendJob = new RedisPendingMessageResendJob(singletonList(listener), redisMQTemplate, redissonClient,
                RedisPendingMessageResendJob.DEFAULT_RESEND_LOCK_KEY);
        // mock 分布式锁
        when(redissonClient.getLock(eq(RedisPendingMessageResendJob.DEFAULT_RESEND_LOCK_KEY))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        // mock Redis Stream 操作
        when(redisMQTemplate.getRedisTemplate()).thenReturn((RedisTemplate) redisTemplate);
        when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOperations);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMessageResend_success() {
        // mock 数据：一条超时消息，一条未超时消息
        RecordId expiredId = RecordId.of("1-0");
        RecordId freshId = RecordId.of("2-0");
        mockPending(2L, List.of(buildPendingMessage(expiredId, Duration.ofMinutes(6)),
                buildPendingMessage(freshId, Duration.ofMinutes(1))));
        Map<Object, Object> body = singletonMap("payload", "{\"id\":1}");
        MapRecord<String, Object, Object> record = StreamRecords.newRecord().in(STREAM_KEY).withId(expiredId).ofMap(body);
        when(streamOperations.range(eq(STREAM_KEY), eq(buildIdRange(expiredId)))).thenReturn(singletonList(record));

        // 调用
        resendJob.messageResend();
        // 断言：超时消息重新投递完整消息体，并 ack 原 pending 消息
        ArgumentCaptor<Record<String, ?>> recordCaptor = ArgumentCaptor.forClass(Record.class);
        verify(streamOperations).add(recordCaptor.capture());
        assertEquals(STREAM_KEY, recordCaptor.getValue().getStream());
        assertEquals(body, recordCaptor.getValue().getValue());
        verify(streamOperations).acknowledge(eq(GROUP), eq(record));
        // 断言：未超时消息不处理
        verify(streamOperations, never()).range(eq(STREAM_KEY), eq(buildIdRange(freshId)));
        verify(lock).unlock();
    }

    @Test
    public void testMessageResend_messageBodyNotExists() {
        // mock 数据：消息体已被裁剪
        RecordId recordId = RecordId.of("1-0");
        mockPending(1L, singletonList(buildPendingMessage(recordId, Duration.ofMinutes(6))));
        when(streamOperations.range(eq(STREAM_KEY), eq(buildIdRange(recordId)))).thenReturn(Collections.emptyList());

        // 调用
        resendJob.messageResend();
        // 断言：无法重新投递，直接 ack 清理 pending
        verify(streamOperations).acknowledge(eq(STREAM_KEY), eq(GROUP), eq(recordId));
        verify(streamOperations, never()).add(any(Record.class));
    }

    @Test
    public void testMessageResend_pendingBatchSize() {
        // mock 数据：pending 数量超过单批上限
        mockPending(5000L, Collections.emptyList());

        // 调用
        resendJob.messageResend();
        // 断言：单次 XPENDING 最多扫描 1000 条
        verify(streamOperations).pending(eq(STREAM_KEY), eq(Consumer.from(GROUP, CONSUMER_NAME)),
                eq(Range.unbounded()), eq(1000L));
    }

    @Test
    public void testMessageResend_pendingLessThanBatchSize() {
        // mock 数据：pending 数量未超过单批上限
        mockPending(3L, Collections.emptyList());

        // 调用
        resendJob.messageResend();
        // 断言：按实际数量扫描
        verify(streamOperations).pending(eq(STREAM_KEY), eq(Consumer.from(GROUP, CONSUMER_NAME)),
                eq(Range.unbounded()), eq(3L));
    }

    @Test
    public void testMessageResend_redisException() {
        // mock 数据：读取消息体时 Redis 超时
        RecordId recordId = RecordId.of("1-0");
        mockPending(1L, singletonList(buildPendingMessage(recordId, Duration.ofMinutes(6))));
        when(streamOperations.range(eq(STREAM_KEY), eq(buildIdRange(recordId))))
                .thenThrow(new QueryTimeoutException("Redis 超时"));

        // 调用
        resendJob.messageResend();
        // 断言：保留 pending，不 ack 也不重新投递，等待下一轮重试
        verify(streamOperations, never()).acknowledge(anyString(), anyString(), any(RecordId[].class));
        verify(streamOperations, never()).acknowledge(anyString(), any(Record.class));
        verify(streamOperations, never()).add(any(Record.class));
        verify(lock).unlock();
    }

    // ========== 随机对象 ==========

    private void mockPending(Long pendingCount, List<PendingMessage> pendingMessages) {
        when(streamOperations.pending(eq(STREAM_KEY), eq(GROUP))).thenReturn(new PendingMessagesSummary(GROUP,
                pendingCount, Range.unbounded(), singletonMap(CONSUMER_NAME, pendingCount)));
        when(streamOperations.pending(eq(STREAM_KEY), eq(Consumer.from(GROUP, CONSUMER_NAME)), eq(Range.unbounded()),
                anyLong())).thenReturn(new PendingMessages(GROUP, pendingMessages));
    }

    private static PendingMessage buildPendingMessage(RecordId recordId, Duration elapsedTime) {
        return new PendingMessage(recordId, Consumer.from(GROUP, CONSUMER_NAME), elapsedTime, 1L);
    }

    private static Range<String> buildIdRange(RecordId recordId) {
        return Range.of(Range.Bound.inclusive(recordId.getValue()), Range.Bound.inclusive(recordId.getValue()));
    }

}

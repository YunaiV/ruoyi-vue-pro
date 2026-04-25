package cn.iocoder.yudao.module.deepay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiUsageMapper;

/**
 * {@link AiRateLimitService} 单元测试。
 *
 * <p>测试：每分钟频控逻辑、每日限额、匿名用户限制。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiRateLimitServiceTest {

    @Mock private StringRedisTemplate  stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private DeepayAiUsageMapper  aiUsageMapper;
    /** 使用同步执行器以便在单元测试中同步执行异步落库 */
    @Mock private TaskExecutor asyncExecutor;

    @InjectMocks
    private AiRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ====================================================================
    // 正常通过
    // ====================================================================

    @Test
    void testCheckAndConsume_normalUser_withinLimit_shouldPass() {
        // Given: 每分钟第 1 次请求
        when(valueOps.increment(anyString())).thenReturn(1L); // minute counter
        // dailyKey returns 1 (first request today)
        // 需要分别 stub 两次 increment 调用
        when(valueOps.increment(contains(":min:"))).thenReturn(1L);
        when(valueOps.increment(contains(":day:"))).thenReturn(1L);

        // When
        AiRateLimitService.RateLimitResult result =
                rateLimitService.checkAndConsume(0L, "user123", "selection");

        // Then: 应该通过（null = 允许）
        assertNull(result, "正常请求应该通过限流检查");
    }

    @Test
    void testCheckAndConsume_anonymousUser_withinLimit_shouldPass() {
        when(valueOps.increment(contains(":min:"))).thenReturn(1L);
        when(valueOps.increment(contains(":day:"))).thenReturn(1L);

        AiRateLimitService.RateLimitResult result =
                rateLimitService.checkAndConsume(0L, null, "selection");

        assertNull(result, "匿名用户第一次请求应该通过");
    }

    // ====================================================================
    // 每分钟超限
    // ====================================================================

    @Test
    void testCheckAndConsume_minuteLimitExceeded_shouldBlock() {
        // Given: 第 11 次请求（超过 DEFAULT_MINUTE_LIMIT=10）
        when(valueOps.increment(contains(":min:"))).thenReturn(11L);
        when(valueOps.increment(contains(":day:"))).thenReturn(11L);

        // When
        AiRateLimitService.RateLimitResult result =
                rateLimitService.checkAndConsume(0L, "user123", "selection");

        // Then: 应该被限流
        assertNotNull(result, "超过每分钟限制时应该返回限流结果");
        assertTrue(result.exceeded);
        assertEquals("minute", result.limitType);
        assertEquals(429, result.httpStatus);
        assertNotNull(result.message);
        assertTrue(result.message.contains("频繁"));
    }

    @Test
    void testCheckAndConsume_anonymousUser_minuteLimitExceeded_shouldBlock() {
        // Anonymous limit is 3
        when(valueOps.increment(contains(":min:"))).thenReturn(4L);
        when(valueOps.increment(contains(":day:"))).thenReturn(4L);

        AiRateLimitService.RateLimitResult result =
                rateLimitService.checkAndConsume(0L, null, "selection");

        assertNotNull(result, "匿名用户超过每分钟限制时应该被限流");
        assertTrue(result.exceeded);
        assertEquals("minute", result.limitType);
    }

    // ====================================================================
    // 每日超限
    // ====================================================================

    @Test
    void testCheckAndConsume_dailyLimitExceeded_shouldBlock() {
        // Given: 每分钟通过，但每日第 201 次
        when(valueOps.increment(contains(":min:"))).thenReturn(1L);
        when(valueOps.increment(contains(":day:"))).thenReturn(201L);
        when(valueOps.decrement(anyString())).thenReturn(200L);

        // When
        AiRateLimitService.RateLimitResult result =
                rateLimitService.checkAndConsume(0L, "user123", "selection");

        // Then: 应该被限流（每日上限 200）
        assertNotNull(result, "超过每日上限时应该返回限流结果");
        assertTrue(result.exceeded);
        assertEquals("daily", result.limitType);
        assertEquals(429, result.httpStatus);
        assertTrue(result.message.contains("上限"));
        // 应该回滚 Redis 计数
        verify(valueOps).decrement(contains(":day:"));
    }

    // ====================================================================
    // RateLimitResult.toMap()
    // ====================================================================

    @Test
    void testRateLimitResult_minuteExceeded_toMap() {
        AiRateLimitService.RateLimitResult result =
                AiRateLimitService.RateLimitResult.minuteLimitExceeded(10, "超出每分钟限制");

        var map = result.toMap();
        assertEquals(true,    map.get("exceeded"));
        assertEquals("minute", map.get("limitType"));
        assertEquals(10,      map.get("limit"));
        assertEquals("超出每分钟限制", map.get("message"));
        assertEquals(429,     map.get("code"));
    }

    @Test
    void testRateLimitResult_dailyExceeded_toMap() {
        AiRateLimitService.RateLimitResult result =
                AiRateLimitService.RateLimitResult.dailyLimitExceeded(200, "超出每日上限");

        var map = result.toMap();
        assertEquals(true,   map.get("exceeded"));
        assertEquals("daily", map.get("limitType"));
        assertEquals(200,    map.get("limit"));
        assertEquals("超出每日上限", map.get("message"));
    }

}

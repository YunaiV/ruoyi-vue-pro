package cn.iocoder.yudao.module.deepay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link DeepayRateLimitService} 单元测试。
 *
 * <p>验证限流逻辑：
 * <ul>
 *   <li>首次请求（setIfAbsent 返回 true）→ 放行</li>
 *   <li>后续请求次数 ≤ MAX → 放行</li>
 *   <li>后续请求次数 > MAX → 拒绝</li>
 *   <li>Redis 异常 → 放行（fail-open）</li>
 *   <li>匿名用户（null userId）→ 使用 "anonymous" key</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DeepayRateLimitServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private DeepayRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ── 1. 首次请求 → 放行 ──────────────────────────────────

    @Test
    void allow_firstRequest_returnsTrue() {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        boolean result = rateLimitService.allow("user123");

        assertTrue(result, "首次请求（setIfAbsent 成功）应放行");
        verify(valueOps, never()).increment(anyString());
    }

    // ── 2. 后续请求未超限 → 放行 ────────────────────────────

    @Test
    void allow_withinLimit_returnsTrue() {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenReturn(Boolean.FALSE);
        when(valueOps.increment(anyString()))
                .thenReturn((long) DeepayRateLimitService.MAX_PER_MINUTE); // 刚好到上限

        boolean result = rateLimitService.allow("user123");

        assertTrue(result, "请求次数等于上限时应放行");
    }

    // ── 3. 超过限流上限 → 拒绝 ─────────────────────────────

    @Test
    void allow_exceedLimit_returnsFalse() {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenReturn(Boolean.FALSE);
        when(valueOps.increment(anyString()))
                .thenReturn((long) DeepayRateLimitService.MAX_PER_MINUTE + 1); // 超限

        boolean result = rateLimitService.allow("user123");

        assertFalse(result, "超过限流上限时应拒绝");
    }

    // ── 4. Redis 异常 → fail-open（放行）───────────────────

    @Test
    void allow_redisException_returnsTrueFailOpen() {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenThrow(new RuntimeException("Redis 连接超时"));

        boolean result = rateLimitService.allow("user123");

        assertTrue(result, "Redis 异常时应 fail-open（放行）");
    }

    // ── 5. 匿名用户（null userId）→ 使用 "anonymous" key ──

    @Test
    void allow_nullUserId_usesAnonymousKey() {
        when(valueOps.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        rateLimitService.allow(null);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).setIfAbsent(keyCaptor.capture(), eq("1"), any(Duration.class));
        assertTrue(keyCaptor.getValue().contains("anonymous"),
                "null userId 应使用包含 'anonymous' 的 key");
    }

    // ── 6. MAX_PER_MINUTE 常量可见且合理 ───────────────────

    @Test
    void maxPerMinuteConstant_isPositive() {
        assertTrue(DeepayRateLimitService.MAX_PER_MINUTE > 0,
                "MAX_PER_MINUTE 应为正整数");
    }
}

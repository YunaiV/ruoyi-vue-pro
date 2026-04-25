package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiUsageMapper;

/**
 * AI 请求限流服务（防滥用）。
 *
 * <h3>两层限流</h3>
 * <ol>
 *   <li>每分钟频控（Redis 计数器，60 秒 TTL）：防止短时间内爆发请求</li>
 *   <li>每日用量上限（Redis + MySQL 双写）：限制每日最多 N 次</li>
 * </ol>
 *
 * <h3>默认限制</h3>
 * <ul>
 *   <li>每分钟：{@value #DEFAULT_MINUTE_LIMIT} 次</li>
 *   <li>每日：{@value #DEFAULT_DAILY_LIMIT} 次</li>
 * </ul>
 *
 * <p>匿名用户（userId=null/blank）使用 IP 或 "anonymous" 键，给 1 次体验机会。</p>
 */
@Slf4j
@Service
public class AiRateLimitService {

    /** 每分钟最大请求数（可通过 feature config 覆盖） */
    public static final int DEFAULT_MINUTE_LIMIT = 10;

    /** 每日最大请求数（可通过 feature config 覆盖） */
    public static final int DEFAULT_DAILY_LIMIT = 200;

    /** 匿名用户每分钟限制 */
    private static final int ANONYMOUS_MINUTE_LIMIT = 3;

    /** 匿名用户每日限制 */
    private static final int ANONYMOUS_DAILY_LIMIT = 5;

    private static final String MINUTE_KEY_PREFIX = "ai:rl:min:";
    private static final String DAILY_KEY_PREFIX  = "ai:rl:day:";

    @Resource private StringRedisTemplate stringRedisTemplate;
    @Resource private DeepayAiUsageMapper  aiUsageMapper;
    /** 复用 deepay 异步执行器，避免直接创建无托管线程 */
    @Resource(name = "deepayAsyncExecutor")
    private TaskExecutor asyncExecutor;

    // ====================================================================
    // 核心：检查并消费一次配额
    // ====================================================================

    /**
     * 检查并原子消费一次调用配额。
     *
     * @param tenantId 租户 ID（0=默认）
     * @param userId   用户标识（null/blank=匿名）
     * @param module   板块
     * @return null=允许通过；非null=限流，包含友好消息和剩余配额信息
     */
    public RateLimitResult checkAndConsume(Long tenantId, String userId, String module) {
        boolean anonymous = !StringUtils.hasText(userId);
        String effectiveId = anonymous ? "anonymous" : userId;
        int minuteLimit = anonymous ? ANONYMOUS_MINUTE_LIMIT : DEFAULT_MINUTE_LIMIT;
        int dailyLimit  = anonymous ? ANONYMOUS_DAILY_LIMIT  : DEFAULT_DAILY_LIMIT;

        // 1. 每分钟频控
        String minuteKey = buildMinuteKey(tenantId, effectiveId);
        Long minuteCount = incrementAndExpire(minuteKey, 60, TimeUnit.SECONDS);
        if (minuteCount > minuteLimit) {
            log.warn("[AiRateLimit] 每分钟超限 userId={} count={}", effectiveId, minuteCount);
            return RateLimitResult.minuteLimitExceeded(minuteLimit,
                    "请求过于频繁，请稍等片刻再试（每分钟最多 " + minuteLimit + " 次）");
        }

        // 2. 每日用量上限（Redis 快速检查）
        String dailyKey = buildDailyKey(tenantId, effectiveId);
        Long dailyCount = incrementAndExpire(dailyKey, 25, TimeUnit.HOURS); // 25h 保证跨日零点
        if (dailyCount > dailyLimit) {
            // 回滚 Redis 计数（超限时不应该增加）
            stringRedisTemplate.opsForValue().decrement(dailyKey);
            log.warn("[AiRateLimit] 每日超限 userId={} count={}", effectiveId, dailyCount - 1);
            return RateLimitResult.dailyLimitExceeded(dailyLimit,
                    "今日 AI 调用次数已达上限（" + dailyLimit + " 次/天），明日 0 点重置");
        }

        // 3. 异步落库（不阻塞主流程）
        asyncRecordUsage(tenantId, effectiveId, module);

        return null; // 允许通过
    }

    // ====================================================================
    // 查询剩余配额
    // ====================================================================

    /**
     * 查询用户剩余每日配额（前端展示用）。
     */
    public Map<String, Object> getRemainingQuota(Long tenantId, String userId) {
        boolean anonymous = !StringUtils.hasText(userId);
        String effectiveId = anonymous ? "anonymous" : userId;
        int dailyLimit = anonymous ? ANONYMOUS_DAILY_LIMIT : DEFAULT_DAILY_LIMIT;
        int minuteLimit = anonymous ? ANONYMOUS_MINUTE_LIMIT : DEFAULT_MINUTE_LIMIT;

        String dailyKey = buildDailyKey(tenantId, effectiveId);
        String dailyCountStr = stringRedisTemplate.opsForValue().get(dailyKey);
        int used = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dailyLimit",  dailyLimit);
        result.put("dailyUsed",   Math.max(0, used));
        result.put("dailyRemain", Math.max(0, dailyLimit - used));
        result.put("minuteLimit", minuteLimit);
        return result;
    }

    // ====================================================================
    // Private helpers
    // ====================================================================

    private String buildMinuteKey(Long tenantId, String userId) {
        long minute = System.currentTimeMillis() / 60_000;
        return MINUTE_KEY_PREFIX + tenantId + ":" + userId + ":" + minute;
    }

    private String buildDailyKey(Long tenantId, String userId) {
        return DAILY_KEY_PREFIX + tenantId + ":" + userId + ":" + LocalDate.now();
    }

    private Long incrementAndExpire(String key, long timeout, TimeUnit unit) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // 首次创建时设置 TTL（防止未设置 TTL 导致永久保留）
            stringRedisTemplate.expire(key, timeout, unit);
        }
        return count != null ? count : 1L;
    }

    private void asyncRecordUsage(Long tenantId, String userId, String module) {
        // 使用 Spring 托管的异步执行器，不直接创建线程，以便线程池管理和优雅关闭
        asyncExecutor.execute(() -> {
            try {
                aiUsageMapper.incrementDailyCount(
                        tenantId != null ? tenantId : 0L,
                        userId,
                        LocalDate.now(),
                        module);
            } catch (Exception e) {
                log.warn("[AiRateLimit] 使用量落库失败 userId={}", userId, e);
            }
        });
    }

    // ====================================================================
    // Result VO
    // ====================================================================

    public static class RateLimitResult {
        public final boolean exceeded;
        public final String  limitType; // "minute" or "daily"
        public final int     limit;
        public final String  message;
        public final int     httpStatus; // 429

        private RateLimitResult(boolean exceeded, String limitType, int limit,
                                String message, int httpStatus) {
            this.exceeded   = exceeded;
            this.limitType  = limitType;
            this.limit      = limit;
            this.message    = message;
            this.httpStatus = httpStatus;
        }

        public static RateLimitResult minuteLimitExceeded(int limit, String message) {
            return new RateLimitResult(true, "minute", limit, message, 429);
        }

        public static RateLimitResult dailyLimitExceeded(int limit, String message) {
            return new RateLimitResult(true, "daily", limit, message, 429);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("exceeded",   exceeded);
            m.put("limitType",  limitType);
            m.put("limit",      limit);
            m.put("message",    message);
            m.put("code",       httpStatus);
            return m;
        }
    }

}

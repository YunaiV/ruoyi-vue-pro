package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * 限流服务（STEP 23 硬化版）。
 *
 * <p>使用 {@code SET NX EX}（setIfAbsent）+ INCR 双步骤，防止并发竞态穿透：
 * <ol>
 *   <li>首次请求：{@code SET key "1" NX EX 60} —— 原子创建窗口并直接放行</li>
 *   <li>后续请求：INCR，检查是否超过 {@value #MAX_PER_MINUTE}</li>
 * </ol>
 * 纯 INCR 方案在并发时可能多个线程同时拿到 count==1 但都跳过 expire，
 * 导致 key 永不过期；setIfAbsent 消除了这个竞态窗口。
 * </p>
 *
 * <p>key 格式：{@code rate:design:{userId}}</p>
 */
@Slf4j
@Service
public class DeepayRateLimitService {

    private static final int    MAX_PER_MINUTE = 3;
    private static final String KEY_PREFIX     = "rate:design:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断本次请求是否被允许。
     *
     * @param userId 用户 ID（null 时用 "anonymous"）
     * @return true 允许；false 超出限流
     */
    public boolean allow(String userId) {
        String key = KEY_PREFIX + (userId != null ? userId : "anonymous");
        try {
            // ① 首次请求：原子 SET key "1" NX EX 60s — 成功则是窗口第一次，直接放行
            Boolean isFirst = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofMinutes(1));
            if (Boolean.TRUE.equals(isFirst)) {
                return true;
            }
            // ② 非首次：INCR 计数（key 必然已有过期时间，不存在竞态）
            Long count = stringRedisTemplate.opsForValue().increment(key);
            boolean allowed = count != null && count <= MAX_PER_MINUTE;
            if (!allowed) {
                log.warn("[RateLimit] 超出限流 userId={} count={} max={}", userId, count, MAX_PER_MINUTE);
            }
            return allowed;
        } catch (Exception e) {
            // Redis 异常时放行，避免误伤正常用户
            log.warn("[RateLimit] Redis 异常，放行请求 userId={}", userId, e);
            return true;
        }
    }

}

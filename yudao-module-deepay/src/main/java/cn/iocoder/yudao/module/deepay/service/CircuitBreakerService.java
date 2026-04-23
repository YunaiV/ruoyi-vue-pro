package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 轻量级内存熔断器（无外部依赖）。
 *
 * <p>状态机：CLOSED → OPEN → HALF_OPEN → CLOSED</p>
 * <ul>
 *   <li><b>CLOSED</b>：正常通行；失败率超过阈值 → 转 OPEN</li>
 *   <li><b>OPEN</b>：拒绝所有请求；超过重置窗口后 → 转 HALF_OPEN</li>
 *   <li><b>HALF_OPEN</b>：放行一次探测请求；成功 → CLOSED，失败 → 回 OPEN</li>
 * </ul>
 *
 * <p>默认参数：
 * <ul>
 *   <li>失败率阈值：50%（10 次采样窗口）</li>
 *   <li>OPEN 持续时间：30 秒</li>
 * </ul>
 * 参数可通过构造函数定制。
 * </p>
 *
 * <p><b>线程安全</b>：使用 {@link AtomicInteger} / {@link AtomicLong}，适合单 JVM 部署；
 * 多实例部署请改用 Redis 计数器。</p>
 */
@Slf4j
@Component
public class CircuitBreakerService {

    private static final int    DEFAULT_WINDOW_SIZE      = 10;
    private static final double DEFAULT_FAILURE_RATE      = 0.5;
    private static final long   DEFAULT_OPEN_DURATION_MS  = 30_000L;

    private final int    windowSize;
    private final double failureRateThreshold;
    private final long   openDurationMs;

    private final AtomicInteger totalCalls   = new AtomicInteger(0);
    private final AtomicInteger failedCalls  = new AtomicInteger(0);
    private final AtomicLong    openSince    = new AtomicLong(0L);

    /** CLOSED=0 / OPEN=1 / HALF_OPEN=2 */
    private volatile int state = 0;

    public CircuitBreakerService() {
        this(DEFAULT_WINDOW_SIZE, DEFAULT_FAILURE_RATE, DEFAULT_OPEN_DURATION_MS);
    }

    public CircuitBreakerService(int windowSize, double failureRateThreshold, long openDurationMs) {
        this.windowSize           = windowSize;
        this.failureRateThreshold = failureRateThreshold;
        this.openDurationMs       = openDurationMs;
    }

    /**
     * 通过熔断器执行 {@code action}。
     *
     * @param name     熔断器名称（仅用于日志）
     * @param action   待保护的调用
     * @param fallback 熔断时的降级逻辑
     * @param <T>      返回类型
     * @return action 或 fallback 的执行结果
     */
    public <T> T execute(String name, Supplier<T> action, Supplier<T> fallback) {
        if (isOpen(name)) {
            log.warn("[CircuitBreaker] {} 熔断中，执行降级", name);
            return fallback.get();
        }
        try {
            T result = action.get();
            onSuccess(name);
            return result;
        } catch (Exception e) {
            onFailure(name, e);
            return fallback.get();
        }
    }

    // ---------------------------------------------------------------- internals

    private boolean isOpen(String name) {
        if (state == 1) {
            // 检查是否到了 HALF_OPEN 窗口
            if (System.currentTimeMillis() - openSince.get() >= openDurationMs) {
                state = 2; // HALF_OPEN
                log.info("[CircuitBreaker] {} 进入 HALF_OPEN，尝试探测", name);
                return false;
            }
            return true;
        }
        return false;
    }

    private void onSuccess(String name) {
        if (state == 2) {
            // HALF_OPEN 探测成功 → 恢复 CLOSED
            reset(name);
            return;
        }
        totalCalls.incrementAndGet();
        // 超过采样窗口，重置计数
        if (totalCalls.get() > windowSize) {
            reset(name);
        }
    }

    private void onFailure(String name, Exception e) {
        log.warn("[CircuitBreaker] {} 调用失败", name, e);
        if (state == 2) {
            // HALF_OPEN 探测失败 → 重新 OPEN
            openSince.set(System.currentTimeMillis());
            state = 1;
            log.warn("[CircuitBreaker] {} HALF_OPEN 探测失败，重新 OPEN", name);
            return;
        }
        int total  = totalCalls.incrementAndGet();
        int failed = failedCalls.incrementAndGet();
        if (total >= windowSize && (double) failed / total >= failureRateThreshold) {
            state = 1;
            openSince.set(System.currentTimeMillis());
            String failRate = String.format("%.0f%%", (double) failed / total * 100);
            log.error("[CircuitBreaker] {} 失败率={}，触发熔断，持续 {}s",
                    name, failRate, openDurationMs / 1000);
        }
    }

    private void reset(String name) {
        totalCalls.set(0);
        failedCalls.set(0);
        state = 0;
        log.info("[CircuitBreaker] {} 恢复 CLOSED", name);
    }

}

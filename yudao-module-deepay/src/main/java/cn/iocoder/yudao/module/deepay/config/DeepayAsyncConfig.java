package cn.iocoder.yudao.module.deepay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置（STEP 31）。
 *
 * <pre>
 * 参数设计：
 *   corePoolSize   = 10   — 常驻线程；每个 AI 生成任务约 5~15 s，10 并发足以应对早期流量
 *   maxPoolSize    = 50   — 瞬时峰值；避免 AI API 被过度并发打垮
 *   queueCapacity  = 100  — 排队缓冲；超过 maxPoolSize 时排队，不直接拒绝
 *   keepAliveSeconds = 60 — 空闲线程存活时间
 *   rejectedPolicy = CallerRunsPolicy — 队列满时由调用线程执行，降级而非抛错
 * </pre>
 */
@Configuration
@EnableAsync
public class DeepayAsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("deepay-ai-");

        // 队列满时由调用线程执行（降级），防止请求被直接丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

}

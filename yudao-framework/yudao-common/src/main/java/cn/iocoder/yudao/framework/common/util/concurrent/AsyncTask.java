package cn.iocoder.yudao.framework.common.util.concurrent;


import cn.iocoder.yudao.framework.common.util.date.DateFormatConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异步任务执行器
 */
@Slf4j
public class AsyncTask {

    final static List<DecoratorItem<?>> DECORATORS = new ArrayList<>();

    public interface DecoratorClear {
        void clear();
    }
    public static AsyncTaskExecutor DEFAULT = new AsyncTaskExecutor(8, 256, 60, 1024, -1, new ContextTaskDecorator());

    public static <T> void addDecoratorItem(Supplier<T> getter, Consumer<T> setter) {
        addDecoratorItem(getter, setter, null);
    }

    public static <T> void addDecoratorItem(Supplier<T> getter, Consumer<T> setter, DecoratorClear clear) {
        DECORATORS.add(new DecoratorItem<T>(getter, setter, clear));
    }

    /**
     * 异步执行且无结果
     */
    public static void run(Runnable runnable) {
        DEFAULT.run(runnable);
    }

    /**
     * 延迟执行
     */
    public static void delay(int ms, Runnable runnable) {
        DEFAULT.delay(Long.valueOf(ms), runnable);
    }

    public static void delay(long ms, Runnable runnable) {
        DEFAULT.delay(ms, runnable);
    }


    /**
     * 异步执行且返回结果
     */
    public static <T> T run(Callable<T> callable) {
        return DEFAULT.run(callable);
    }

    /**
     * 重试
     */
    public static <T> T retry(Callable<T> callable, int maxRetry, long interval, Function<Throwable, T> failureHandler) {
        return DEFAULT.retry(callable, maxRetry, interval, failureHandler);
    }

    /**
     * 重试
     */
    public static <T> T retry(Callable<T> callable, int maxRetry, long interval) {
        return DEFAULT.retry(callable, maxRetry, interval, null);
    }

    /**
     * 异步执行,将输入分解成多个部分，且返回结果
     */
    public static <IN, OUT> List<OUT> fork(List<IN> list, int batchSize, Function<IN, OUT> function) {
        return DEFAULT.fork(list, batchSize, function);
    }


    public static class AsyncTaskExecutor {

        public static final String THREAD_NAME_PREFIX = "somle-task-";
        private ThreadPoolTaskScheduler scheduler = null;
        @Getter
        private ThreadPoolTaskExecutor executor = null;

        private int parallelism = -1;

        public AsyncTaskExecutor() {
            this(4, 64, 60, 1024, -1, null);
        }

        /**
         * 异步任务执行器构造函数
         *
         * @param coreThreadSize     核心线程数：线程池中会维护的最小线程数
         * @param maxThreadSize      最大线程数：线程池中允许的最大线程数
         * @param threadAliveSeconds 线程存活时间：当线程数大于核心线程数时，多余的空闲线程的存活时间
         * @param queueCapacity      队列容量：用于存储等待执行的任务的阻塞队列大小
         * @param parallelism        并行度：用于控制并行流的并行度，-1表示使用默认值
         * @param taskDecorator      任务装饰器：用于在任务执行前后进行一些操作，比如传递ThreadLocal等上下文信息
         */
        public AsyncTaskExecutor(int coreThreadSize, int maxThreadSize, int threadAliveSeconds, int queueCapacity, int parallelism, TaskDecorator taskDecorator) {
            // 设置并行度
            this.parallelism = parallelism;

            // 创建拒绝策略处理器：当线程池和队列都满了时，由调用线程自己执行任务
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

            // 创建线程池执行器
            this.executor = new ThreadPoolTaskExecutor();
            // 设置核心线程数
            this.executor.setCorePoolSize(coreThreadSize);
            // 设置最大线程数
            this.executor.setMaxPoolSize(maxThreadSize);
            // 设置线程存活时间（秒）：超过核心线程数的线程，在空闲这么长时间后会被回收
            this.executor.setKeepAliveSeconds(threadAliveSeconds);
            // 设置队列容量：用于存储等待执行的任务，超过此容量会触发拒绝策略
            this.executor.setQueueCapacity(queueCapacity);

            // 设置任务装饰器：用于解决父子线程间的数据传递问题
            // 比如：在异步任务中获取到父线程的ThreadLocal值
            if (taskDecorator != null) {
                this.executor.setTaskDecorator(taskDecorator);
            }

            // 设置拒绝策略：当线程池和队列都满了时，由调用线程自己执行任务
            this.executor.setRejectedExecutionHandler(handler);
            // 设置等待所有任务结束后再关闭线程池：优雅关闭
            this.executor.setWaitForTasksToCompleteOnShutdown(true);

            // 初始化线程池
            this.executor.initialize();

            // 创建定时任务调度器
            this.scheduler = new ThreadPoolTaskScheduler();
            // 设置调度器线程池大小
            this.scheduler.setPoolSize(20);
            // 设置线程名前缀：方便在日志中识别线程来源
            this.scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
            // 设置等待时间：关闭时等待任务完成的最长时间
            this.scheduler.setAwaitTerminationSeconds(60);
            // 设置等待所有任务结束后再关闭调度器：优雅关闭
            this.scheduler.setWaitForTasksToCompleteOnShutdown(true);
            // 初始化调度器
            this.scheduler.initialize();
        }

        /**
         * 异步执行且无结果
         */
        public void run(Runnable runnable) {
            this.executor.execute(runnable);
        }


        public void delay(long ms, Runnable runnable) {
            scheduler.schedule(() -> {
                AsyncTaskExecutor.this.run(runnable);
            }, Instant.now().atZone(ZoneId.of(DateFormatConstants.TIME_ZONE_ASIA_SHANGHAI)).toInstant().plusMillis(ms));
        }

        /**
         * 异步执行且返回结果
         */
        public <T> T run(Callable<T> callable) {
            Future<T> future = this.executor.submit(callable);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 异步重试
         *
         * @param callable       任务
         * @param maxRetry       最大重试次数
         * @param interval       重试间隔时间 ms
         * @param failureHandler 最后一次失败是否抛出异常 true or false, 可传null 默认 false
         * @return 重试结果
         */
        public <T> T retry(Callable<T> callable, int maxRetry, long interval, Function<Throwable, T> failureHandler) {

            T t = this.run(() -> {

                RetryTemplate retryTemplate = createSimpleRetryTemplate(maxRetry, interval, failureHandler != null);
                T it = null;
                try {
                    it = retryTemplate.execute(new RetryCallback<T, Throwable>() {
                        @Override
                        public T doWithRetry(RetryContext context) throws Throwable {
                            return callable.call();
                        }
                    });
                } catch (Throwable e) {
                    if (failureHandler == null) {
                        log.error("执行异常", e);
                    } else {
                        it = failureHandler.apply(e);
                    }
                }
                return it;

            });


            return t;

        }

        private RetryTemplate createSimpleRetryTemplate(int maxRetry, long interval, Boolean lastRetryFailedThrowException) {
            RetryTemplate retryTemplate = new RetryTemplate();
            SimpleRetryPolicy policy = new SimpleRetryPolicy();
            policy.setMaxAttempts(maxRetry);
            retryTemplate.setRetryPolicy(policy);
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(interval);
            // backOffPolicy.setMultiplier(2);
            // backOffPolicy.setMaxInterval(15000);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            retryTemplate.setThrowLastExceptionOnExhausted(lastRetryFailedThrowException);
            return retryTemplate;
        }

        /**
         * 异步执行,将输入分解成多个部分，且返回结果
         */
        public <IN, OUT> List<OUT> fork(List<IN> list, int batchSize, Function<IN, OUT> function) {
            SimpleJoinForkTask<IN, OUT> task = new SimpleJoinForkTask<>(list, batchSize, this.parallelism);
            List<OUT> outs = task.execute(els -> {
                List<OUT> result = new ArrayList<>();
                for (IN el : els) {
                    OUT out = function.apply(el);
                    result.add(out);
                }
                return result;
            });
            return outs;
        }


        /**
         * 待任务执行完毕后关闭
         */
        public void shutdown() {
            this.executor.shutdown();
        }

//        /**
//         * 停止任务并关闭
//         * */
//        public void shutdownNow() {
//            this.executor.sh
//        }

    }


}

class DecoratorItem<T> {


    private final Consumer<T> setter;
    private final Supplier<T> getter;
    @Getter
    private final AsyncTask.DecoratorClear clear;

    public DecoratorItem(Supplier<T> getter, Consumer<T> setter, AsyncTask.DecoratorClear clear) {
        this.getter = getter;
        this.setter = setter;
        this.clear = clear;
    }

    public Consumer getSetter() {
        return setter;
    }

    public Supplier getGetter() {
        return getter;
    }

}

class ContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

        // 获取父线程的上下文数据
        Map<Integer, Object> data = new HashMap<>();
        for (int i = 0; i < AsyncTask.DECORATORS.size(); i++) {
            DecoratorItem item = AsyncTask.DECORATORS.get(i);
            if (item != null && item.getGetter() != null) {
                data.put(i, item.getGetter().get());
            } else {
                data.put(i, null);
            }
        }


//        if(securitySupport==null) {
//            securitySupport = SpringUtil.getBean(SecuritySupport.class);
//        }
        //获取父线程的loginVal
//        final LoginUser threadUser =  securitySupport.getLoginUser();
//        final HttpServletRequest request =securitySupport.getRequest();
//        final LoginUser requestUser =securitySupport.getRequestShareUser();
        return () -> {
            try {
//                // 将主线程的请求信息，设置到子线程中
//                securitySupport.setThreadShareUser(threadUser);
//                securitySupport.setRequestShareUser(requestUser);
//                securitySupport.setRequest(request);
                for (int i = 0; i < AsyncTask.DECORATORS.size(); i++) {
                    DecoratorItem item = AsyncTask.DECORATORS.get(i);
                    item.getSetter().accept(data.get(i));
                }
                runnable.run();
            } finally {
//                // 线程结束，清空这些信息，否则可能造成内存泄漏
//                securitySupport.clearThreadShareUser();
//                securitySupport.clearRequestShareUser();
//                securitySupport.clearRequest();
                for (int i = 0; i < AsyncTask.DECORATORS.size(); i++) {
                    DecoratorItem item = AsyncTask.DECORATORS.get(i);
                    if (item.getClear() != null) {
                        item.getClear().clear();
                    }
                }
            }
        };
    }
}



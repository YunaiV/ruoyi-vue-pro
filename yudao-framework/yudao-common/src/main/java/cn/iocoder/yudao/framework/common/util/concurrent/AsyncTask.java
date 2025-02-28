package cn.iocoder.yudao.framework.common.util.concurrent;


import cn.iocoder.yudao.framework.common.util.date.DateFormatConstants;
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
 *  异步任务执行器
 * */
@Slf4j
public class AsyncTask {

    final static List<DecoratorItem<?>>  DECORATORS=new ArrayList<>();

    public static interface DecoratorClear {
        void clear();
    }

    public static <T> void addDecoratorItem(Supplier<T> getter,Consumer<T> setter) {
        addDecoratorItem(getter,setter,null);
    }
    public static <T> void addDecoratorItem(Supplier<T> getter,Consumer<T> setter,DecoratorClear clear) {
        DECORATORS.add(new DecoratorItem<T>(getter,setter,clear));
    }

    public static AsyncTaskExecutor DEFAULT=new AsyncTaskExecutor(8,256,60,1024,-1,new ContextTaskDecorator());

    /**
     * 异步执行且无结果
     * */
    public static void run(Runnable runnable) {
        DEFAULT.run(runnable);
    }

    /**
     * 延迟执行
     * */
    public static void delay(int ms,Runnable runnable) {
        DEFAULT.delay(Long.valueOf(ms),runnable);
    }

    public static void delay(long ms,Runnable runnable) {
        DEFAULT.delay(ms,runnable);
    }


    /**
     * 异步执行且返回结果
     * */
    public static <T> T run(Callable<T> callable) {
        return DEFAULT.run(callable);
    }

    /**
     * 重试
     * */
    public static <T> T retry(Callable<T> callable, int maxRetry, long interval, Function<Throwable,T> failureHandler) {
        return DEFAULT.retry(callable,maxRetry,interval,failureHandler);
    }

    /**
     * 重试
     * */
    public static <T> T retry(Callable<T> callable, int maxRetry, long interval) {
        return DEFAULT.retry(callable,maxRetry,interval,null);
    }

    /**
     * 异步执行,将输入分解成多个部分，且返回结果
     * */
    public static <IN,OUT> List<OUT> fork(List<IN> list, int batchSize, Function<IN,OUT> function) {
        return DEFAULT.fork(list,batchSize,function);
    }




    public static class AsyncTaskExecutor {

        public static final String THREAD_NAME_PREFIX = "task-";
        private ThreadPoolTaskScheduler scheduler = null;
        private ThreadPoolTaskExecutor executor = null;

        private int parallelism = -1 ;

        public AsyncTaskExecutor() {
            this(4,64,60,1024,-1,null);
        }

        public AsyncTaskExecutor(int coreThreadSize, int maxThreadSize, int threadAliveSeconds,int queueCapacity, int parallelism,TaskDecorator taskDecorator) {
            this.parallelism = parallelism;
            RejectedExecutionHandler handler=new ThreadPoolExecutor.CallerRunsPolicy();
            this.executor = new ThreadPoolTaskExecutor();
            this.executor.setCorePoolSize(coreThreadSize);
            this.executor.setMaxPoolSize(maxThreadSize);
            // 设置线程活跃时间（秒）
            this.executor.setKeepAliveSeconds(threadAliveSeconds);
            // 设置队列容量
            this.executor.setQueueCapacity(queueCapacity);
            //设置TaskDecorator，用于解决父子线程间的数据复用
            if(taskDecorator!=null) {
                this.executor.setTaskDecorator(taskDecorator);
            }

            this.executor.setRejectedExecutionHandler(handler);
            // 等待所有任务结束后再关闭线程池
            this.executor.setWaitForTasksToCompleteOnShutdown(true);

            this.executor.initialize();


            this.scheduler = new ThreadPoolTaskScheduler();
            this.scheduler.setPoolSize(20);
            this.scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
            this.scheduler.setAwaitTerminationSeconds(60);
            this.scheduler.setWaitForTasksToCompleteOnShutdown(true);
            this.scheduler.initialize();


        }

        /**
         * 异步执行且无结果
         * */
        public void run(Runnable runnable) {
            this.executor.execute(runnable);
        }



        public void delay(long ms,Runnable runnable) {
            scheduler.schedule(()->{
                AsyncTaskExecutor.this.run(runnable);
            }, Instant.now().atZone(ZoneId.of(DateFormatConstants.TIME_ZONE_ASIA_SHANGHAI)).toInstant().plusMillis(ms));
        }

        /**
         * 异步执行且返回结果
         * */
        public <T> T run(Callable<T> callable) {
            Future<T> future=this.executor.submit(callable);
            try {
                return future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 异步重试
         * @param callable                        任务
         * @param maxRetry                 最大重试次数
         * @param interval                    重试间隔时间 ms
         * @param failureHandler     最后一次失败是否抛出异常 true or false, 可传null 默认 false
         * @return 重试结果
         */
        public <T> T retry(Callable<T> callable, int maxRetry, long interval,Function<Throwable,T> failureHandler)  {

            T t=this.run(()->{

                RetryTemplate retryTemplate = createSimpleRetryTemplate(maxRetry,interval,failureHandler!=null);
                T it = null;
                try {
                    it=retryTemplate.execute(new RetryCallback<T, Throwable>() {
                        @Override
                        public T doWithRetry(RetryContext context) throws Throwable {
                            return callable.call();
                        }
                    });
                } catch (Throwable e){
                    if(failureHandler==null) {
                        log.error("执行异常",e);
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
            SimpleRetryPolicy policy=new SimpleRetryPolicy();
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
         * */
        public <IN,OUT> List<OUT> fork(List<IN> list, int batchSize, Function<IN,OUT> function) {
            SimpleJoinForkTask<IN,OUT> task=new SimpleJoinForkTask<>(list,batchSize,this.parallelism);
            List<OUT> outs= task.execute(els->{
                List<OUT> result = new ArrayList<>();
                for (IN el : els) {
                    OUT out=function.apply(el);
                    result.add(out);
                }
                return result;
            });
            return outs;
        }



        /**
         * 待任务执行完毕后关闭
         * */
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


    private Consumer<T> setter;
    private Supplier<T> getter;
    private AsyncTask.DecoratorClear clear;
    public DecoratorItem(Supplier<T> getter, Consumer<T> setter, AsyncTask.DecoratorClear clear) {
        this.getter=getter;
        this.setter=setter;
        this.clear=clear;
    }
    public Consumer getSetter() {
        return setter;
    }

    public Supplier getGetter() {
        return getter;
    }

    public AsyncTask.DecoratorClear getClear() {
        return clear;
    }
}

class ContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

        // 获取父线程的上下文数据
        Map<Integer,Object> data=new HashMap<>();
        for (int i = 0; i < AsyncTask.DECORATORS.size(); i++) {
            DecoratorItem item=AsyncTask.DECORATORS.get(i);
            if(item!=null && item.getGetter()!=null) {
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
                    DecoratorItem item=AsyncTask.DECORATORS.get(i);
                    item.getSetter().accept(data.get(i));
                }
                runnable.run();
            } finally {
//                // 线程结束，清空这些信息，否则可能造成内存泄漏
//                securitySupport.clearThreadShareUser();
//                securitySupport.clearRequestShareUser();
//                securitySupport.clearRequest();
                for (int i = 0; i < AsyncTask.DECORATORS.size(); i++) {
                    DecoratorItem item=AsyncTask.DECORATORS.get(i);
                    if(item.getClear()!=null) {
                        item.getClear().clear();
                    }
                }
            }
        };
    }
}



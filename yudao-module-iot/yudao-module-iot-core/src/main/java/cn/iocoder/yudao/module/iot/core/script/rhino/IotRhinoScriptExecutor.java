package cn.iocoder.yudao.module.iot.core.script.rhino;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.script.IotScriptException;
import cn.iocoder.yudao.module.iot.core.script.IotScriptExecutor;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

// DONE @AI：字段注释；
/**
 * IoT 数据解析脚本执行器：同一个 JVM 内执行，不是沙箱
 * <p>
 * Rhino 下，经过指令检查点的超限会真正中断执行、释放线程；不经过检查点的原生长操作只能被标记为超时，线程计入卡住检测。
 * 本节点的熔断（按 productId:hash，或卡住线程过多时全局）只在内存中生效，不写库，重启后复位
 *
 * @author 芋道源码
 */
@Slf4j
public class IotRhinoScriptExecutor implements IotScriptExecutor {

    /**
     * 熔断：1 分钟内超时达到该次数
     */
    private static final int BREAKER_TIMEOUT_COUNT = 3;
    // DONE @AI：字段注释；
    /**
     * 熔断：统计超时次数的时间窗口（1 分钟）
     */
    private static final long BREAKER_TIMEOUT_WINDOW_NANOS = TimeUnit.MINUTES.toNanos(1);

    // DONE @AI：字段注释；
    /**
     * IoT 专用的 Rhino ContextFactory：所有 JS 线程共用，不使用全局 ContextFactory
     */
    private final IotScriptContextFactory contextFactory = new IotScriptContextFactory();
    // DONE @AI：字段注释；
    /**
     * 每个 JS 线程独占的 Rhino 运行时：预热或首次调用时创建，不跨线程共享
     */
    private final ThreadLocal<IotScriptRuntime> runtimes = new ThreadLocal<>();

    // DONE @AI：字段注释；
    /**
     * 执行器名称：线程名前缀，也用于日志
     */
    private final String name;
    /**
     * JS 线程池：核心数 = 最大数，有界队列，AbortPolicy
     */
    private final ThreadPoolExecutor jsPool;
    /**
     * 单线程计时器：执行预算兜底、卡住扫描；不执行业务回调。由本执行器创建，{@link #shutdown()} 时关闭
     */
    private final ScheduledThreadPoolExecutor timer;
    /**
     * 单次执行的时间预算（纳秒）：实际预算取它与截止时间中较早的一个
     */
    private final long execBudgetNanos;
    /**
     * 单次执行的指令上限
     */
    private final long maxInstructions;
    /**
     * 运行超过该时长视为卡住（纳秒）
     */
    private final long stuckNanos;
    /**
     * 卡住的线程达到该数量时全局熔断
     */
    private final int maxStuckThreads;
    /**
     * 卡住扫描的计时任务：每秒一次，{@link #shutdown()} 时取消
     */
    private final ScheduledFuture<?> stuckScanner;

    /**
     * 正在执行的任务 → 开始时间
     */
    private final Map<IotScriptTask, Long> running = new ConcurrentHashMap<>();
    /**
     * 已熔断的脚本 key（productId:hash）
     */
    private final Set<String> openKeys = ConcurrentHashMap.newKeySet();
    /**
     * 最近的执行超时时间：key → 时间（nanoTime）
     */
    private final Map<String, Deque<Long>> recentTimeouts = new ConcurrentHashMap<>();
    /**
     * 当前卡住的线程数：每秒扫描一次
     */
    private volatile int stuckThreads;
    /**
     * 预热失败：全局熔断，需要重启
     */
    private volatile boolean warmUpFailed;

    public IotRhinoScriptExecutor(Options options) {
        this.name = options.getName();
        this.jsPool = new ThreadPoolExecutor(options.getPoolSize(), options.getPoolSize(), 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(options.getQueueCapacity()), new NamedThreadFactory(options.getName() + "-", true),
                new ThreadPoolExecutor.AbortPolicy());
        this.timer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(options.getName() + "-timer-", true));
        this.timer.setRemoveOnCancelPolicy(true); // 被取消的计时任务立即移出队列，不留到原截止时间
        this.execBudgetNanos = TimeUnit.MILLISECONDS.toNanos(options.getExecBudgetMillis());
        this.maxInstructions = options.getMaxInstructions();
        this.stuckNanos = TimeUnit.MILLISECONDS.toNanos(options.getStuckMillis());
        this.maxStuckThreads = Math.max(1, options.getMaxStuckThreads());
        this.stuckScanner = timer.scheduleWithFixedDelay(this::scanStuck, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 执行一次脚本调用：调用方线程（网关为 Vert.x worker、biz 调试为请求线程）阻塞等待，最长到截止时间
     * <p>
     * 脚本在 JS 线程池执行，受执行预算（时间、指令）约束；等待到期时，还在排队的任务被终结，之后不会再启动
     *
     * @param source        脚本
     * @param function      "decode" 或 "encode"
     * @param ctxJson       平台构造的入参 JSON
     * @param deadlineNanos 截止时间（System.nanoTime）：覆盖排队与执行
     * @return 结果：SUCCESS / ERROR / TIMEOUT / REJECTED
     */
    @Override
    public IotScriptResult execute(IotScriptSource source, String function, String ctxJson, long deadlineNanos) {
        if (isOpen(source)) {
            return IotScriptResult.rejected("脚本已熔断");
        }
        long remain = deadlineNanos - System.nanoTime();
        if (remain <= 0) {
            return IotScriptResult.timeout("进入 JS 池前已超过截止时间");
        }
        IotScriptTask task = new IotScriptTask(source);
        try {
            jsPool.execute(() -> run(task, function, ctxJson, deadlineNanos));
        } catch (RejectedExecutionException e) {
            return IotScriptResult.rejected("脚本线程池已满");
        }
        try {
            return task.getResult().get(remain, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            // 到期：排队中 PENDING → DONE（结果为超时），之后 markRunning 失败，不会启动；执行中的由预算中断或计入卡住检测
            finish(task, IotScriptResult.timeout("超过截止时间"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finish(task, IotScriptResult.error("等待脚本结果被中断", 0));
        } catch (ExecutionException e) {
            finish(task, IotScriptResult.error(StrUtil.maxLength(String.valueOf(e.getCause()), 200), 0));
        }
        return task.getResult().join(); // finish 只会成功一次：这里拿到的是最终终结的那个结果
    }

    /**
     * 只做语法检查（不执行任何用户代码）：在 JS 线程上编译，调用方线程有界等待
     *
     * @param content 脚本内容
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return 结果：SUCCESS / ERROR（带行号）/ REJECTED / TIMEOUT
     */
    @Override
    public IotScriptResult compileOnly(String content, long timeout, TimeUnit unit) {
        if (isGlobalOpen()) {
            return IotScriptResult.rejected("脚本已熔断");
        }
        Future<IotScriptResult> future;
        try {
            future = jsPool.submit(() -> {
                try {
                    runtime().compileOnly(content);
                    return IotScriptResult.success(null, 0);
                } catch (IotScriptException e) {
                    return IotScriptResult.of(e.getStatus(), e.getMessage(), e.getLineNumber(), 0);
                }
            });
        } catch (RejectedExecutionException e) {
            return IotScriptResult.rejected("脚本线程池已满");
        }
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(false);
            return IotScriptResult.timeout("编译超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return IotScriptResult.error("编译被中断", 0);
        } catch (ExecutionException e) {
            return IotScriptResult.error(StrUtil.maxLength(String.valueOf(e.getCause()), 200), 0);
        }
    }

    private void run(IotScriptTask task, String function, String ctxJson, long deadlineNanos) {
        if (System.nanoTime() >= deadlineNanos) { // 出队时已过期（等待方通常已先终结；finish 只会成功一次）
            finish(task, IotScriptResult.timeout("排队期间超过截止时间"));
            return;
        }
        if (!task.markRunning()) { // 已被超时或拒绝终结：不启动
            return;
        }
        long start = System.nanoTime();
        long budgetDeadline = Math.min(start + execBudgetNanos, deadlineNanos);
        // 兜底：观察器到不了的原生长操作
        try {
            task.setBudgetTimer(timer.schedule(() -> finishTimeoutRunning(task, "执行超过预算"),
                    Math.max(0L, budgetDeadline - start), TimeUnit.NANOSECONDS));
        } catch (RejectedExecutionException e) { // 停机时 timer 已关闭：不执行，直接终结
            finish(task, IotScriptResult.rejected("脚本执行器已关闭"));
            return;
        }
        running.put(task, start);
        try {
            IotScriptRuntime runtime = runtime(); // 正常已在启动时预热；线程被替换后为冷启动，耗时计入本次预算
            if (System.nanoTime() >= budgetDeadline || task.isFinished()) { // 实际调用前再校验一次
                finish(task, IotScriptResult.timeout("调用前已超过截止时间"));
                return;
            }
            // 顶层代码、入参原生化、调用、序列化共用这一个预算
            String json = runtime.invoke(task.getSource(), function, ctxJson,
                    new IotScriptBudget(budgetDeadline, maxInstructions));
            finish(task, IotScriptResult.success(json, elapsedMillis(start)));
        } catch (IotScriptBudget.BudgetExceededError e) {
            finishTimeoutRunning(task, e.getMessage()); // 观察器中断：线程已经回到这里，可以继续使用
        } catch (IotScriptException e) {
            finish(task, IotScriptResult.of(e.getStatus(), e.getMessage(), e.getLineNumber(), elapsedMillis(start)));
        } catch (StackOverflowError e) {
            finish(task, IotScriptResult.error("调用层级过深", elapsedMillis(start))); // 原生递归可能绕过解释器栈深限制
        } catch (RuntimeException e) {
            finish(task, IotScriptResult.error(StrUtil.maxLength(e.getMessage(), 200), elapsedMillis(start)));
        } finally {
            running.remove(task); // OutOfMemoryError 等其它 Error 不捕获，交给 JVM 现有的处理
        }
    }

    /**
     * 执行中的超时：计入熔断统计
     */
    private void finishTimeoutRunning(IotScriptTask task, String message) {
        if (finish(task, IotScriptResult.timeout(message))) {
            recordTimeout(task.getSource());
        }
    }

    /**
     * 只终结一次：终结的那个结果交给等待中的调用方
     *
     * @return 是否由本次调用终结
     */
    private boolean finish(IotScriptTask task, IotScriptResult result) {
        if (!task.finish()) {
            return false; // 已终结：迟到的结果直接丢弃
        }
        task.getResult().complete(result);
        return true;
    }

    private IotScriptRuntime runtime() {
        IotScriptRuntime runtime = runtimes.get();
        if (runtime == null) { // 只有预热，或线程被替换后的首次调用才会进入：创建封存的标准对象
            runtime = new IotScriptRuntime(contextFactory);
            runtimes.set(runtime);
        }
        return runtime;
    }

    /**
     * 启动时调用一次，在接收流量之前：N 个任务通过屏障同时占住 N 个不同的 worker，保证每个 worker 都实际创建了 runtime
     * <p>
     * 失败 → 全局熔断（记 ERROR），之后的调用都按「已熔断」拒绝
     */
    @Override
    public void warmUp() {
        int n = jsPool.getCorePoolSize();
        try {
            jsPool.prestartAllCoreThreads();
            CyclicBarrier barrier = new CyclicBarrier(n);
            List<Future<?>> futures = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                futures.add(jsPool.submit(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return runtime();
                }));
            }
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            warmUpFailed = true;
            log.error("[warmUp][{} 预热失败，本节点脚本执行全局熔断]", name, e);
        }
    }

    /**
     * 停止：不再接收新任务，正在执行的脚本由预算中断后退出；同时关闭本执行器的计时器（停机后提交的预算计时被拒绝，任务直接终结）
     */
    @Override
    public void shutdown() {
        stuckScanner.cancel(false);
        jsPool.shutdown();
        timer.shutdownNow();
    }

    // ========== 熔断（只在本节点生效，不写库） ==========

    /**
     * 是否已熔断：预热失败、卡住的线程过多（全局），或该脚本已被熔断
     */
    public boolean isOpen(IotScriptSource source) {
        return isGlobalOpen() || openKeys.contains(source.getKey());
    }

    private boolean isGlobalOpen() {
        return warmUpFailed || stuckThreads >= maxStuckThreads;
    }

    private void recordTimeout(IotScriptSource source) {
        long now = System.nanoTime();
        Deque<Long> times = recentTimeouts.computeIfAbsent(source.getKey(), key -> new ArrayDeque<>());
        synchronized (times) {
            times.addLast(now);
            while (!times.isEmpty() && now - times.peekFirst() > BREAKER_TIMEOUT_WINDOW_NANOS) {
                times.pollFirst();
            }
            if (times.size() < BREAKER_TIMEOUT_COUNT) {
                return;
            }
        }
        if (openKeys.add(source.getKey())) {
            log.error("[recordTimeout][{} 脚本 1 分钟内超时 {} 次，本节点熔断 product({}) hash({})]", name,
                    BREAKER_TIMEOUT_COUNT, source.getProductId(), source.getShortHash());
        }
    }

    /**
     * 每秒执行一次：运行超过 stuckNanos 的任务记为卡住 → 本节点熔断这个 (productId, hash)，记 ERROR；不调用 RPC、不改数据库开关
     */
    private void scanStuck() {
        try {
            long now = System.nanoTime();
            int stuck = 0;
            for (Map.Entry<IotScriptTask, Long> entry : running.entrySet()) {
                if (now - entry.getValue() <= stuckNanos) {
                    continue;
                }
                stuck++;
                IotScriptSource source = entry.getKey().getSource();
                if (openKeys.add(source.getKey())) {
                    log.error("[scanStuck][{} 脚本卡住一个线程，本节点熔断 product({}) hash({})]", name,
                            source.getProductId(), source.getShortHash());
                }
            }
            if (stuck >= maxStuckThreads && stuckThreads < maxStuckThreads) {
                log.error("[scanStuck][{} 卡住的线程数({}) 达到上限，本节点全局熔断；线程恢复后自动解除]", name, stuck);
            }
            stuckThreads = stuck;
        } catch (RuntimeException e) {
            log.error("[scanStuck][{} 卡住扫描异常]", name, e);
        }
    }

    private static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    /**
     * 执行器参数
     */
    @Data
    @Accessors(chain = true)
    public static class Options {

        /**
         * 线程名前缀，如 iot-script-js
         */
        private String name = "iot-script-js";
        /**
         * 线程数
         */
        private int poolSize = 4;
        /**
         * 队列容量
         */
        private int queueCapacity = 200;
        /**
         * 单次执行的时间预算（毫秒）
         */
        private long execBudgetMillis = 500;
        /**
         * 单次执行的指令上限
         */
        private long maxInstructions = 1_000_000;
        /**
         * 运行超过该时长视为卡住（毫秒）
         */
        private long stuckMillis = 5000;
        /**
         * 卡住的线程达到该数量时全局熔断
         */
        private int maxStuckThreads = 3;

    }

    /**
     * JS 任务：一个 AtomicInteger 状态机；JS 线程的「开始」与超时、拒绝、完成的「终结」竞争同一个状态
     * <p>
     * 只代表「这一次脚本调用」的终态。终结时取消的是本任务自己的预算计时器，不涉及调用方（协议）的响应计时器
     */
    static final class IotScriptTask {

        /**
         * 状态：排队中，还没开始
         */
        static final int PENDING = 0;
        /**
         * 状态：JS 线程执行中
         */
        static final int RUNNING = 1;
        /**
         * 状态：已终结（成功、错误、拒绝或超时；具体原因见终结的结果）
         */
        static final int DONE = 2;

        /**
         * 当前状态：PENDING → RUNNING（markRunning）；PENDING 或 RUNNING → DONE（finish），只能终结一次
         */
        private final AtomicInteger state = new AtomicInteger(PENDING);
        /**
         * 本次调用的脚本
         */
        @Getter
        private final IotScriptSource source;
        /**
         * 终结的结果：调用方在 {@link #execute} 中等待
         */
        @Getter
        private final CompletableFuture<IotScriptResult> result = new CompletableFuture<>();
        /**
         * 执行中的时间兜底（原生长操作不经过观察器时）
         */
        @Setter
        private volatile ScheduledFuture<?> budgetTimer;

        IotScriptTask(IotScriptSource source) {
            this.source = source;
        }

        /**
         * JS 线程：只有 PENDING 才能开始；已被超时或拒绝终结的任务返回 false，不再启动
         */
        boolean markRunning() {
            return state.compareAndSet(PENDING, RUNNING);
        }

        /**
         * 任意线程：PENDING 或 RUNNING → DONE；并发调用时只有一个返回 true
         */
        boolean finish() {
            for (;;) {
                int s = state.get();
                if (s == DONE) {
                    return false;
                }
                if (state.compareAndSet(s, DONE)) {
                    ScheduledFuture<?> timer = budgetTimer;
                    if (timer != null) {
                        timer.cancel(false);
                    }
                    return true;
                }
            }
        }

        boolean isFinished() {
            return state.get() == DONE;
        }

    }

}

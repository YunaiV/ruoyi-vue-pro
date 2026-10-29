package cn.iocoder.yudao.module.iot.core.script.rhino;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.script.IotScriptUtils;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotRhinoScriptExecutor} 的单元测试：真实的 Rhino 解释执行
 *
 * @author 芋道源码
 */
public class IotRhinoScriptExecutorTest {

    private static final String CTX = "{\"bytes\":[1,2,3],\"hex\":\"010203\"}";

    private IotRhinoScriptExecutor executor;

    @BeforeEach
    public void setUp() {
        executor = new IotRhinoScriptExecutor(new IotRhinoScriptExecutor.Options().setName("test-script-js").setPoolSize(1)
                .setQueueCapacity(4).setExecBudgetMillis(500).setMaxInstructions(1_000_000));
    }

    @AfterEach
    public void tearDown() {
        executor.shutdown();
    }

    private IotScriptResult invoke(String content, String function) {
        return executor.execute(IotScriptSource.of(1L, content), function, CTX,
                System.nanoTime() + TimeUnit.SECONDS.toNanos(3));
    }

    @Test
    public void testExecute_decodeSuccess() {
        // 准备参数：宿主侧 JSON.parse 入参，函数返回原生对象，宿主侧 JSON.stringify
        String content = "function decode(ctx) { return {method: 'thing.property.post',"
                + " params: {sum: ctx.bytes[0] + ctx.bytes[1] + ctx.bytes[2], hex: ctx.hex}}; }";

        // 调用
        IotScriptResult result = invoke(content, IotScriptUtils.FUNCTION_DECODE);
        // 断言
        assertEquals(IotScriptResult.Status.SUCCESS, result.getStatus());
        JsonNode root = JsonUtils.parseTree(result.getJson());
        assertEquals(6, root.get("params").get("sum").intValue());
        assertEquals("010203", root.get("params").get("hex").textValue());
    }

    @Test
    public void testExecute_functionMissing() {
        // 调用：没有定义 encode
        IotScriptResult result = invoke("function decode(ctx) { return null; }", IotScriptUtils.FUNCTION_ENCODE);
        // 断言：成功但没有输出，由调用方处理
        assertEquals(IotScriptResult.Status.SUCCESS, result.getStatus());
        assertNull(result.getJson());
    }

    @Test
    public void testExecute_javaAccessDenied() {
        // 调用：java、Packages、getClass 都不可用
        String content = "function decode(ctx) { return {method: 'thing.property.post', params: {"
                + "java: typeof java === 'undefined' ? 1 : 0, packages: typeof Packages === 'undefined' ? 1 : 0}}; }";
        IotScriptResult result = invoke(content, IotScriptUtils.FUNCTION_DECODE);
        // 断言
        assertEquals(IotScriptResult.Status.SUCCESS, result.getStatus());
        assertEquals("{\"java\":1,\"packages\":1}", JsonUtils.parseTree(result.getJson()).get("params").toString());

        // 调用：显式访问 Java 类
        IotScriptResult result2 = invoke("function decode(ctx) { return {method: 'thing.property.post',"
                + " params: {home: java.lang.System.getProperty('user.home').length}}; }", IotScriptUtils.FUNCTION_DECODE);
        // 断言：脚本错误，不是成功
        assertEquals(IotScriptResult.Status.ERROR, result2.getStatus());
    }

    @Test
    public void testExecute_infiniteLoopTimeoutAndReuse() {
        // 调用：死循环被预算中断
        IotScriptResult result = invoke("function decode(ctx) { while (true) {} }", IotScriptUtils.FUNCTION_DECODE);
        // 断言
        assertEquals(IotScriptResult.Status.TIMEOUT, result.getStatus());

        // 调用：同一个线程继续可用（线程池只有 1 个线程）
        IotScriptResult result2 = invoke("function decode(ctx) { return null; }", IotScriptUtils.FUNCTION_DECODE);
        // 断言
        assertEquals(IotScriptResult.Status.SUCCESS, result2.getStatus());
    }

    @Test
    public void testExecute_breakerOpenAfterTimeouts() {
        // 准备参数：同一个脚本 1 分钟内超时 3 次
        String content = "function decode(ctx) { for (;;) {} }";
        for (int i = 0; i < 3; i++) {
            assertEquals(IotScriptResult.Status.TIMEOUT, invoke(content, IotScriptUtils.FUNCTION_DECODE).getStatus());
        }

        // 调用 + 断言：之后直接拒绝；其它脚本不受影响
        assertTrue(executor.isOpen(IotScriptSource.of(1L, content)));
        assertEquals(IotScriptResult.Status.REJECTED, invoke(content, IotScriptUtils.FUNCTION_DECODE).getStatus());
        assertEquals(IotScriptResult.Status.SUCCESS, invoke("function decode(ctx) { return null; }",
                IotScriptUtils.FUNCTION_DECODE).getStatus());
    }

    @Test
    public void testExecute_deadlinePassed() {
        // 调用：截止时间已过
        IotScriptResult result = executor.execute(IotScriptSource.of(1L, "function decode(ctx) { return null; }"),
                IotScriptUtils.FUNCTION_DECODE, CTX, System.nanoTime() - 1);
        // 断言：不进入 JS 池
        assertEquals(IotScriptResult.Status.TIMEOUT, result.getStatus());
    }

    @Test
    public void testExecute_pendingTimeoutNeverStarts() throws Exception {
        // 准备参数：用一个 Java 任务确定地占住唯一的 JS 线程（不依赖脚本预算与睡眠）；
        // 该线程还没有执行过脚本（没有预热），所以它的 runtime 为空：任何脚本一旦在它上面启动，都会创建 runtime
        ThreadPoolExecutor jsPool = (ThreadPoolExecutor) ReflectUtil.getFieldValue(executor, "jsPool");
        ThreadLocal<?> runtimes = (ThreadLocal<?>) ReflectUtil.getFieldValue(executor, "runtimes");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        jsPool.execute(() -> {
            entered.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(entered.await(5, TimeUnit.SECONDS));

        // 调用：排队中的任务 100ms 后到期
        IotScriptSource pending = IotScriptSource.of(2L, "function decode(ctx) { return {method: 'thing.property.post'}; }");
        IotScriptResult result = executor.execute(pending, IotScriptUtils.FUNCTION_DECODE, CTX,
                System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(100));
        // 断言：调用方按截止时间返回超时；任务仍在 JS 池的队列里，没有启动
        assertEquals(IotScriptResult.Status.TIMEOUT, result.getStatus());
        assertEquals(1, jsPool.getQueue().size());

        // 调用：放开 JS 线程，再提交一个探测任务；单线程 FIFO，探测任务执行时，排队超时的任务已经出队
        release.countDown();
        Future<Boolean> runtimeCreated = jsPool.submit(() -> runtimes.get() != null);
        // 断言：排队超时的任务出队后没有启动脚本（JS 线程上仍没有创建 runtime）
        assertFalse(runtimeCreated.get(5, TimeUnit.SECONDS));
        // 断言：排队超时的任务不计入熔断；JS 线程可以继续使用，之后真正执行了脚本
        assertFalse(executor.isOpen(pending));
        assertEquals(IotScriptResult.Status.SUCCESS, invoke("function decode(ctx) { return null; }",
                IotScriptUtils.FUNCTION_DECODE).getStatus());
        assertTrue(jsPool.submit(() -> runtimes.get() != null).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testScriptTask_finishOnce() {
        // 准备参数：执行中的任务，预算计时器 1 小时后才会触发
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        try {
            IotScriptSource source = IotScriptSource.of(1L, "function decode(ctx) { return null; }");
            IotRhinoScriptExecutor.IotScriptTask task = new IotRhinoScriptExecutor.IotScriptTask(source);
            assertTrue(task.markRunning());
            ScheduledFuture<?> budgetTimer = timer.schedule(() -> { }, 1, TimeUnit.HOURS);
            task.setBudgetTimer(budgetTimer);

            // 调用 + 断言：第一次终结成功，并取消预算计时器
            assertTrue(task.finish());
            assertTrue(task.isFinished());
            assertTrue(budgetTimer.isCancelled());
            // 调用 + 断言：重复终结失败（迟到的结果直接丢弃）
            assertFalse(task.finish());

            // 调用 + 断言：排队中已被终结的任务，之后不能再开始
            IotRhinoScriptExecutor.IotScriptTask pending = new IotRhinoScriptExecutor.IotScriptTask(source);
            assertTrue(pending.finish());
            assertFalse(pending.markRunning());
        } finally {
            timer.shutdownNow();
        }
    }

    @Test
    public void testCompileOnly() {
        // 调用 + 断言：语法正确，不执行顶层代码（死循环也不会卡住）
        assertTrue(executor.compileOnly("while (true) {} function decode(ctx) {}", 2, TimeUnit.SECONDS).isSuccess());
        // 调用 + 断言：语法错误带行号（第 2 行只有一个不能作为语句开头的 ")"，解析器在该 token 上立即报错，行号无歧义）
        IotScriptResult result = executor.compileOnly("var a = 1;\n)\nfunction decode(ctx) {}", 2, TimeUnit.SECONDS);
        assertEquals(IotScriptResult.Status.ERROR, result.getStatus());
        assertEquals(2, result.getLineNumber());
    }

}

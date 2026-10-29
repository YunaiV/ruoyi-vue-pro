package cn.iocoder.yudao.module.iot.gateway.util;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotWorkerTaskQueue} 的单元测试
 *
 * @author 芋道源码
 */
public class IotWorkerTaskQueueTest {

    private static final long TIMEOUT_SECONDS = 10;

    private Vertx vertx;
    private Context context;
    /**
     * context 所在的 event loop 线程
     */
    private Thread contextThread;

    @BeforeEach
    public void setUp() throws Exception {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        contextThread = callOnContext(Thread::currentThread);
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    public void testSubmit_orderedSequential() throws Exception {
        // 准备参数：越早提交的任务执行越久；第 3 个任务失败
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        int count = 5;
        List<Integer> started = new CopyOnWriteArrayList<>();
        List<Integer> completed = new CopyOnWriteArrayList<>();
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxRunning = new AtomicInteger();
        AtomicBoolean completedOnContext = new AtomicBoolean(true);
        CountDownLatch done = new CountDownLatch(count);

        // 调用：在 context 上依次提交
        List<Future<Integer>> futures = callOnContext(() -> {
            List<Future<Integer>> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int index = i;
                Future<Integer> future = queue.submit(() -> {
                    maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
                    started.add(index);
                    try {
                        Thread.sleep((count - index) * 30L);
                        if (index == 2) {
                            throw new IllegalStateException("task-2 failed");
                        }
                        return index;
                    } finally {
                        running.decrementAndGet();
                    }
                });
                future.onComplete(ar -> {
                    if (Thread.currentThread() != contextThread) {
                        completedOnContext.set(false);
                    }
                    completed.add(index);
                    done.countDown();
                });
                list.add(future);
            }
            return list;
        });
        assertTrue(done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 断言：按提交顺序逐个执行（同一时刻只有一个在执行）、逐个完成，结果回到 context 线程
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), started);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), completed);
        assertEquals(1, maxRunning.get());
        assertTrue(completedOnContext.get());
        // 断言：第 3 个任务失败，之后的任务照常执行
        for (int i = 0; i < count; i++) {
            Future<Integer> future = futures.get(i);
            if (i == 2) {
                assertTrue(future.failed());
                assertInstanceOf(IllegalStateException.class, future.cause());
                assertEquals("task-2 failed", future.cause().getMessage());
            } else {
                assertTrue(future.succeeded());
                assertEquals(Integer.valueOf(i), future.result());
            }
        }
    }

    @Test
    public void testSubmit_queuesParallel() throws Exception {
        // 准备参数：同一个 context 上的两个队列（两个连接）
        IotWorkerTaskQueue queueA = new IotWorkerTaskQueue(context);
        IotWorkerTaskQueue queueB = new IotWorkerTaskQueue(context);
        CountDownLatch releaseA = new CountDownLatch(1);
        CountDownLatch startedA = new CountDownLatch(1);

        // 调用：A 的第一个任务阻塞，A 的第二个任务排在它之后
        List<Future<String>> futuresA = callOnContext(() -> Arrays.asList(
                queueA.submit(() -> {
                    startedA.countDown();
                    return block(releaseA, "A1");
                }),
                queueA.submit(() -> "A2")));
        assertTrue(startedA.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        // 调用：B 提交一个任务
        Future<String> futureB = callOnContext(() -> queueB.submit(() -> "B"));

        try {
            // 断言：A 阻塞时 B 照常完成；A 的第二个任务仍在等待第一个
            assertEquals("B", await(futureB));
            assertFalse(futuresA.get(0).isComplete());
            assertFalse(futuresA.get(1).isComplete());
        } finally {
            releaseA.countDown();
        }
        // 断言：释放后 A 按顺序完成
        assertEquals("A1", await(futuresA.get(0)));
        assertEquals("A2", await(futuresA.get(1)));
    }

    @Test
    public void testSubmit_maxPending() throws Exception {
        // 准备参数：最多 2 个待处理任务
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context, true, 2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean rejectedImmediately = new AtomicBoolean();

        // 调用：前两个任务阻塞，第三个超过上限
        List<Future<String>> futures = callOnContext(() -> {
            List<Future<String>> list = Arrays.asList(
                    queue.submit(() -> block(release, "1")),
                    queue.submit(() -> block(release, "2")),
                    queue.submit(() -> "3"));
            rejectedImmediately.set(list.get(2).failed());
            return list;
        });

        try {
            // 断言：第三个立即失败为 RejectedExecutionException
            assertTrue(rejectedImmediately.get());
            assertInstanceOf(RejectedExecutionException.class, futures.get(2).cause());
            assertFalse(futures.get(0).isComplete());
            assertFalse(futures.get(1).isComplete());
        } finally {
            release.countDown();
        }
        // 断言：前两个完成后名额释放，可以再次提交
        assertEquals("1", await(futures.get(0)));
        assertEquals("2", await(futures.get(1)));
        assertEquals("4", await(callOnContext(() -> queue.submit(() -> "4"))));
    }

    @Test
    public void testSubmitLast() throws Exception {
        // 准备参数：最多 1 个待处理任务
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context, true, 1);
        CountDownLatch release = new CountDownLatch(1);
        List<String> executed = new CopyOnWriteArrayList<>();
        AtomicBoolean lastRejectedImmediately = new AtomicBoolean(true);

        // 调用：第一个任务阻塞；队列已满时 submitLast 不受上限；之后 submit / submitLast 都失败
        List<Future<String>> futures = callOnContext(() -> {
            List<Future<String>> list = Arrays.asList(
                    queue.submit(() -> {
                        block(release, "1");
                        executed.add("1");
                        return "1";
                    }),
                    queue.submit(() -> "full"),
                    queue.submitLast(() -> {
                        executed.add("last");
                        return "last";
                    }),
                    queue.submit(() -> "closed"),
                    queue.submitLast(() -> "last-again"));
            lastRejectedImmediately.set(list.get(2).failed());
            return list;
        });

        try {
            // 断言：已满的 submit 被拒绝；submitLast 被接受，排在阻塞任务之后
            assertRejected(futures.get(1));
            assertFalse(lastRejectedImmediately.get());
            assertFalse(futures.get(2).isComplete());
            // 断言：关闭之后 submit、submitLast 都被拒绝
            assertRejected(futures.get(3));
            assertRejected(futures.get(4));
        } finally {
            release.countDown();
        }
        // 断言：最后一个任务在之前的任务之后执行
        assertEquals("1", await(futures.get(0)));
        assertEquals("last", await(futures.get(2)));
        assertEquals(Arrays.asList("1", "last"), executed);
        // 断言：已关闭，之后的任务都被拒绝
        assertRejected(callOnContext(() -> queue.submit(() -> "after")));
    }

    @Test
    public void testSubmit_unordered() throws Exception {
        // 准备参数：无序，最多 2 个待处理任务
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context, false, 2);
        CountDownLatch release = new CountDownLatch(1);

        try {
            // 调用：第一个任务阻塞
            Future<String> blocked = callOnContext(() -> queue.submit(() -> block(release, "A")));
            // 断言：之后的任务不排在它之后，直接完成
            assertEquals("B", await(callOnContext(() -> queue.submit(() -> "B"))));
            assertFalse(blocked.isComplete());

            // 调用：A 仍在执行，再提交 C 后达到上限，D 被拒绝
            List<Future<String>> futures = callOnContext(() -> Arrays.asList(
                    queue.submit(() -> block(release, "C")),
                    queue.submit(() -> "D")));
            // 断言：只限制数量
            assertRejected(futures.get(1));
            assertFalse(futures.get(0).isComplete());

            // 调用：释放
            release.countDown();
            // 断言
            assertEquals("A", await(blocked));
            assertEquals("C", await(futures.get(0)));
        } finally {
            release.countDown();
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 在 context 线程上执行（与 Vert.x 处理器一致），等待并返回结果
     */
    private <T> T callOnContext(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        context.runOnContext(v -> {
            try {
                result.complete(action.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * worker 线程上阻塞，直到释放
     */
    private static String block(CountDownLatch release, String value) throws InterruptedException {
        if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("等待释放超时");
        }
        return value;
    }

    private static void assertRejected(Future<?> future) {
        assertTrue(future.failed());
        assertInstanceOf(RejectedExecutionException.class, future.cause());
    }

}

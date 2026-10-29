package cn.iocoder.yudao.framework.datapermission.core.aop;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import com.alibaba.ttl.TtlRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * {@link DataPermissionContextHolder} 的单元测试
 *
 * @author 芋道源码
 */
class DataPermissionContextHolderTest {

    private static final long TIMEOUT_SECONDS = 5;

    private static final DataPermission ENABLE = EnableClass.class.getAnnotation(DataPermission.class);
    private static final DataPermission DISABLE = DisableClass.class.getAnnotation(DataPermission.class);

    @BeforeEach
    public void setUp() {
        DataPermissionContextHolder.clear();
    }

    @Test
    public void testGet() {
        // mock 方法
        DataPermission dataPermission01 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission01);
        DataPermission dataPermission02 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission02);

        // 调用
        DataPermission result = DataPermissionContextHolder.get();
        // 断言
        assertSame(result, dataPermission02);
    }

    @Test
    public void testPush() {
        // 调用
        DataPermission dataPermission01 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission01);
        DataPermission dataPermission02 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission02);
        // 断言
        DataPermission first = DataPermissionContextHolder.getAll().get(0);
        DataPermission second = DataPermissionContextHolder.getAll().get(1);
        assertSame(dataPermission01, first);
        assertSame(dataPermission02, second);
    }

    @Test
    public void testRemove() {
        // mock 方法
        DataPermission dataPermission01 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission01);
        DataPermission dataPermission02 = mock(DataPermission.class);
        DataPermissionContextHolder.add(dataPermission02);

        // 调用
        DataPermission result = DataPermissionContextHolder.remove();
        // 断言
        assertSame(result, dataPermission02);
        assertEquals(1, DataPermissionContextHolder.getAll().size());
    }

    @Test
    public void testTtlRunnable_preStartedExecutor() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            // 准备参数：预启动线程池的线程，此时父线程还没有 DataPermission，不会通过 InheritableThreadLocal 继承
            executor.submit(() -> {}).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            DataPermissionContextHolder.add(ENABLE);
            AtomicReference<List<DataPermission>> childStack = new AtomicReference<>();

            // 调用：子任务入栈 enable = false 后暂停
            Future<?> future = executor.submit(TtlRunnable.get(() -> {
                DataPermissionContextHolder.add(DISABLE);
                childStack.set(DataPermissionContextHolder.getAll());
                entered.countDown();
                try {
                    await(release);
                } finally {
                    DataPermissionContextHolder.remove();
                }
            }));
            await(entered);
            // 断言：子任务继承父线程的 DataPermission，但父线程不受子任务入栈影响
            assertNotSame(DataPermissionContextHolder.getAll(), childStack.get());
            assertEquals(Arrays.asList(ENABLE, DISABLE), childStack.get());
            assertEquals(singletonList(ENABLE), DataPermissionContextHolder.getAll());
            assertTrue(DataPermissionContextHolder.get().enable());

            // 调用：子任务出栈并结束
            release.countDown();
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // 断言：父线程不受子任务出栈影响
            assertEquals(singletonList(ENABLE), DataPermissionContextHolder.getAll());
            // 断言：线程池的线程，执行完后恢复为原上下文
            assertNull(executor.submit((Callable<DataPermission>) DataPermissionContextHolder::get)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void testTtlRunnable_captureSnapshot() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // 准备参数：预启动线程池的线程
            executor.submit(() -> {}).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            DataPermissionContextHolder.add(ENABLE);
            AtomicReference<List<DataPermission>> childValues = new AtomicReference<>();
            Runnable task = TtlRunnable.get(() -> childValues.set(new ArrayList<>(DataPermissionContextHolder.getAll())));

            // 调用：捕获后，父线程嵌套入栈，再执行子任务
            DataPermissionContextHolder.add(DISABLE);
            executor.submit(task).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // 断言：子任务使用捕获时的 DataPermission，不受父线程后续入栈影响
            assertEquals(singletonList(ENABLE), childValues.get());

            // 调用 + 断言：父线程嵌套出栈，出栈至空后清理上下文
            assertSame(DISABLE, DataPermissionContextHolder.remove());
            assertSame(ENABLE, DataPermissionContextHolder.remove());
            assertTrue(DataPermissionContextHolder.getAll().isEmpty());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testChildThread() throws Exception {
        // 准备参数
        DataPermissionContextHolder.add(ENABLE);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<List<DataPermission>> childStack = new AtomicReference<>();
        FutureTask<Void> task = new FutureTask<>(() -> {
            // 断言：直接创建的子线程，不继承父线程的 DataPermission，即默认开启数据权限
            assertNull(DataPermissionContextHolder.get());
            DataPermissionContextHolder.add(DISABLE);
            childStack.set(DataPermissionContextHolder.getAll());
            entered.countDown();
            await(release);
            // 出栈至空，清理子线程的上下文
            assertSame(DISABLE, DataPermissionContextHolder.remove());
            assertNull(DataPermissionContextHolder.get());
            return null;
        });

        // 调用：直接创建子线程，子线程入栈 enable = false 后暂停
        new Thread(task).start();
        try {
            await(entered);
            // 断言：父线程不受子线程入栈影响
            assertNotSame(DataPermissionContextHolder.getAll(), childStack.get());
            assertEquals(singletonList(ENABLE), DataPermissionContextHolder.getAll());
            assertTrue(DataPermissionContextHolder.get().enable());
        } finally {
            release.countDown();
        }
        task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        // 断言：父线程不受子线程出栈、清理影响
        assertEquals(singletonList(ENABLE), DataPermissionContextHolder.getAll());
        assertTrue(DataPermissionContextHolder.get().enable());
    }

    @Test
    public void testUnmanagedWorker_notRetainParent() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // 准备参数：在 enable = false 的作用域内，懒创建线程池的线程，且任务没有使用 TtlRunnable 包装
            DataPermissionContextHolder.add(DISABLE);
            assertNull(executor.submit((Callable<DataPermission>) DataPermissionContextHolder::get)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            // 调用：父线程退出作用域
            assertSame(DISABLE, DataPermissionContextHolder.remove());

            // 断言：线程池的线程，没有永久保留创建时的 enable = false，仍为默认开启数据权限
            assertNull(executor.submit((Callable<DataPermission>) DataPermissionContextHolder::get)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @DataPermission
    private static class EnableClass {
    }

    @DataPermission(enable = false)
    private static class DisableClass {
    }

}

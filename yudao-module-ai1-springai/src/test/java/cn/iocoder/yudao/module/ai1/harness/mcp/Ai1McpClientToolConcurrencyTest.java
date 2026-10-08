package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1McpClientTool} 的并发单元测试
 *
 * @author 芋道源码
 */
public class Ai1McpClientToolConcurrencyTest {

    @Test
    public void testGetOrCreateClient_differentMcpConnectsIndependently() throws Exception {
        // mock 数据
        Ai1McpClientTool tool = new Ai1McpClientTool();
        McpSyncClient slowClient = mock(McpSyncClient.class);
        McpSyncClient readyClient = mock(McpSyncClient.class);
        CountDownLatch initializing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        // mock slowClient 的方法
        when(slowClient.initialize()).thenAnswer(invocation -> {
            initializing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            throw new IllegalStateException("连接失败");
        });
        // 准备参数
        Ai1McpDO slowMcp = new Ai1McpDO().setId(1L).setName("不可达服务").setTransport("http")
                .setUrl("http://127.0.0.1:8081/mcp");
        Ai1McpDO readyMcp = new Ai1McpDO().setId(2L).setName("可用服务").setTransport("http")
                .setUrl("http://127.0.0.1:8082/mcp");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 调用
            Future<McpSyncClient> slow = executor.submit(() -> getOrCreateClient(tool, slowMcp, slowClient));
            assertTrue(initializing.await(5, TimeUnit.SECONDS));
            Future<McpSyncClient> ready = executor.submit(() -> getOrCreateClient(tool, readyMcp, readyClient));
            // 断言
            assertSame(readyClient, ready.get(2, TimeUnit.SECONDS));
            assertFalse(slow.isDone());
            verify(readyClient).initialize();
            release.countDown();
            ExecutionException exception = assertThrows(ExecutionException.class, () -> slow.get(5, TimeUnit.SECONDS));
            assertEquals("连接失败", exception.getCause().getMessage());
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            tool.destroy();
        }
    }

    @Test
    public void testGetOrCreateClient_sameMcpInitializesOnce() throws Exception {
        // mock 数据
        Ai1McpClientTool tool = new Ai1McpClientTool();
        McpSyncClient firstClient = mock(McpSyncClient.class);
        McpSyncClient secondClient = mock(McpSyncClient.class);
        CountDownLatch initializing = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        AtomicReference<Thread> secondThread = new AtomicReference<>();
        CountDownLatch release = new CountDownLatch(1);
        // mock firstClient 的方法
        when(firstClient.initialize()).thenAnswer(invocation -> {
            initializing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return null;
        });
        // 准备参数
        Ai1McpDO mcp = new Ai1McpDO().setId(1L).setName("测试服务").setTransport("http")
                .setUrl("http://127.0.0.1:8081/mcp");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 调用
            Future<McpSyncClient> first = executor.submit(() -> getOrCreateClient(tool, mcp, firstClient));
            assertTrue(initializing.await(5, TimeUnit.SECONDS));
            Future<McpSyncClient> second = executor.submit(() -> {
                secondThread.set(Thread.currentThread());
                secondStarted.countDown();
                return getOrCreateClient(tool, mcp, secondClient);
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                while (secondThread.get().getState() != Thread.State.BLOCKED
                        || Arrays.stream(secondThread.get().getStackTrace()).noneMatch(frame ->
                                frame.getClassName().equals(Ai1McpClientTool.class.getName())
                                        && frame.getMethodName().equals("getOrCreateClient"))) {
                    Thread.sleep(5);
                }
            });
            release.countDown();
            // 断言
            assertSame(firstClient, first.get(5, TimeUnit.SECONDS));
            assertSame(firstClient, second.get(5, TimeUnit.SECONDS));
            verify(firstClient).initialize();
            verifyNoInteractions(secondClient);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            tool.destroy();
        }
    }

    private static McpSyncClient getOrCreateClient(Ai1McpClientTool tool, Ai1McpDO mcp, McpSyncClient client) {
        AtomicReference<McpClientTransport> transport = new AtomicReference<>();
        McpClient.SyncSpec spec = mock(McpClient.SyncSpec.class, RETURNS_SELF);
        when(spec.build()).thenReturn(client);
        try (MockedStatic<McpClient> clientFactory = mockStatic(McpClient.class)) {
            clientFactory.when(() -> McpClient.sync(any(McpClientTransport.class))).thenAnswer(invocation -> {
                transport.set(invocation.getArgument(0));
                return spec;
            });
            return ReflectionTestUtils.invokeMethod(tool, "getOrCreateClient", mcp);
        } finally {
            if (transport.get() != null) {
                transport.get().closeGracefully().block(Duration.ofSeconds(5));
            }
        }
    }

}

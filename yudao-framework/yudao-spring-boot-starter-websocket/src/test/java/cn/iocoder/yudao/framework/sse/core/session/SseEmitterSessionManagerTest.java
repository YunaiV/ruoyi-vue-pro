package cn.iocoder.yudao.framework.sse.core.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SseEmitterSessionManager} 的单元测试
 *
 * @author 芋道源码
 */
public class SseEmitterSessionManagerTest {

    private SseEmitterSessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager = new SseEmitterSessionManager(1000L);
    }

    public static SseConnection newConnection(Long tenantId, Integer userType, Long userId, String clientId) {
        return new SseConnection(new RecordingSseEmitter(), tenantId, userType, userId, clientId, null);
    }

    @Test
    public void testGetConnectionList() {
        // 准备参数
        SseConnection c1 = newConnection(1L, 2, 100L, null);
        SseConnection c2 = newConnection(1L, 2, 100L, null);
        SseConnection c3 = newConnection(2L, 2, 100L, null); // 其它租户的同编号用户
        SseConnection c4 = newConnection(1L, 1, 200L, null); // 其它用户类型

        // 调用
        Arrays.asList(c1, c2, c3, c4).forEach(c -> assertTrue(sessionManager.register(c)));
        // 断言
        assertTrue(c1.getConnectionId().startsWith(SseConnection.CONNECTION_ID_PREFIX));
        assertSame(c1, sessionManager.getConnection(c1.getConnectionId()));
        assertEquals(2, sessionManager.getConnectionList(1L, 2, 100L).size());
        assertEquals(1, sessionManager.getConnectionList(2L, 2, 100L).size());
        assertEquals(3, sessionManager.getConnectionList(null, 2, 100L).size()); // 租户为空，匹配所有租户
        assertEquals(2, sessionManager.getConnectionList(1L, 2).size());
        assertEquals(3, sessionManager.getConnectionList(null, 2).size());
        assertEquals(1, sessionManager.getConnectionList(null, 1).size());
        assertEquals(4, sessionManager.getConnectionCount());
    }

    @Test
    public void testRemove_idempotent() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用
        sessionManager.remove(connection.getConnectionId());
        sessionManager.remove(connection.getConnectionId());
        sessionManager.remove((String) null);
        // 断言
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
        assertTrue(sessionManager.getConnectionList(1L, 2, 100L).isEmpty());
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testRegister_sameClientId() {
        // 准备参数
        SseConnection old = newConnection(1L, 2, 100L, "tab-1");
        SseConnection newer = newConnection(1L, 2, 100L, "tab-1");

        // 调用
        sessionManager.register(old);
        sessionManager.register(newer);
        // 断言：旧连接收到 REPLACED 关闭事件后被移除
        RecordingSseEmitter oldEmitter = (RecordingSseEmitter) old.getEmitter();
        assertTrue(oldEmitter.completed);
        assertTrue(oldEmitter.frames.get(0).contains("\"reason\":\"REPLACED\""));
        assertNull(sessionManager.getConnection(old.getConnectionId()));
        assertSame(newer, sessionManager.getConnection(newer.getConnectionId()));
        assertEquals(1, sessionManager.getConnectionList(1L, 2, 100L).size());
    }

    @Test
    public void testRegister_closed() {
        // 准备参数：客户端立即断开，onCompletion 回调先于 register 执行
        SseConnection connection = newConnection(1L, 2, 100L, "tab-1");
        connection.markClosed();
        sessionManager.remove(connection.getConnectionId());

        // 调用
        assertFalse(sessionManager.register(connection));
        // 断言：注册被回滚，两个索引都没有残留
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
        assertEquals(0, sessionManager.getConnectionCount());
        assertTrue(((Map<?, ?>) ReflectionTestUtils.getField(sessionManager, "userConnectionIds")).isEmpty());
    }

    @Test
    public void testRegister_concurrent() throws InterruptedException {
        // 准备参数
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(400);

        // 调用
        for (int i = 0; i < 400; i++) {
            long userId = i % 5;
            executor.execute(() -> {
                try {
                    SseConnection connection = newConnection(1L, 2, userId, null);
                    sessionManager.register(connection);
                    sessionManager.getConnectionList(1L, 2);
                    sessionManager.remove(connection.getConnectionId());
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        // 断言
        assertEquals(0, sessionManager.getConnectionCount());
        for (long userId = 0; userId < 5; userId++) {
            assertTrue(sessionManager.getConnectionList(1L, 2, userId).isEmpty());
        }
    }

    @Test
    public void testRegister_concurrentSameUser() throws InterruptedException {
        // 准备参数
        int count = 2000;
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch latch = new CountDownLatch(count);
        List<SseConnection> kept = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger registerFailures = new AtomicInteger();

        // 调用：同一用户并发注册、移除，覆盖 register 与 removeIndex 对同一用户索引的竞争
        for (int i = 0; i < count; i++) {
            boolean keep = i % 2 == 0;
            executor.execute(() -> {
                try {
                    SseConnection connection = newConnection(1L, 2, 100L, null);
                    if (!sessionManager.register(connection)) {
                        registerFailures.incrementAndGet();
                        return;
                    }
                    if (keep) {
                        kept.add(connection);
                    } else {
                        sessionManager.remove(connection.getConnectionId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        // 断言：保留的连接全部可以通过用户索引查到
        assertEquals(0, registerFailures.get());
        assertEquals(kept.size(), sessionManager.getConnectionCount());
        assertEquals(kept.size(), sessionManager.getConnectionList(1L, 2, 100L).size());
    }

    @Test
    public void testClose_success() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用：重复关闭
        sessionManager.close(connection, SseCloseReasonEnum.CLIENT_CLOSE);
        sessionManager.close(connection, SseCloseReasonEnum.CLIENT_CLOSE);
        // 断言：只发送一次 close 事件
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        assertEquals(1, emitter.countFrames("event:close"));
        assertTrue(emitter.frames.get(0).contains("CLIENT_CLOSE"));
        assertTrue(emitter.completed);
        assertTrue(connection.isClosed());
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testClose_waitSending() throws Exception {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        CountDownLatch blocker = new CountDownLatch(1);
        emitter.sendBlocker = blocker;
        AtomicReference<SseConnection.SendResult> waitingResult = new AtomicReference<>();
        // 帧 A 已持有发送锁，正在发送
        Thread sending = new Thread(() -> connection.send(SseEmitter.event().name("demo").data("A"), 5000L));
        sending.start();
        waitUntil(() -> emitter.concurrentSends.get() == 1);
        // 帧 B 已通过关闭检查，正在等待发送锁
        Thread waiting = new Thread(() -> waitingResult.set(connection.send(SseEmitter.event().name("demo").data("B"), 5000L)));
        waiting.start();
        waitUntil(() -> waiting.getState() == Thread.State.TIMED_WAITING);

        // 调用：关闭连接，等待发送锁；随后放行帧 A
        Thread closing = new Thread(() -> sessionManager.close(connection, SseCloseReasonEnum.CLIENT_CLOSE));
        closing.start();
        waitUntil(() -> connection.isClosed() && closing.getState() == Thread.State.TIMED_WAITING);
        blocker.countDown();
        sending.join(5000L);
        waiting.join(5000L);
        closing.join(5000L);
        // 断言：帧 B 不会发出，close 事件是最后一帧
        assertEquals(SseConnection.SendResult.FAILED, waitingResult.get());
        assertEquals(2, emitter.frames.size());
        assertTrue(emitter.frames.get(0).contains("A"));
        assertTrue(emitter.frames.get(1).contains("event:close"));
        assertTrue(emitter.completed);
        assertEquals(SseConnection.SendResult.FAILED, connection.send(SseEmitter.event().data("C"), 100L));
        assertEquals(2, emitter.frames.size());
    }

    @Test
    public void testClose_sendFailed() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        ((RecordingSseEmitter) connection.getEmitter()).failure = new IOException("broken pipe");
        sessionManager.register(connection);

        // 调用
        sessionManager.close(connection, SseCloseReasonEnum.TOKEN_INVALID);
        // 断言
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
        assertEquals(0, sessionManager.getConnectionCount());
    }

    @Test
    public void testAbort_success() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);
        IllegalStateException ex = new IllegalStateException("test");

        // 调用
        sessionManager.abort(connection, ex);
        sessionManager.abort(connection, ex);
        // 断言：异常结束，不发送 close 事件
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        assertSame(ex, emitter.error);
        assertTrue(emitter.frames.isEmpty());
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testShutdown_success() {
        // 准备参数
        SseConnection c1 = newConnection(1L, 2, 100L, null);
        SseConnection c2 = newConnection(2L, 2, 200L, null);
        sessionManager.register(c1);
        sessionManager.register(c2);

        // 调用
        sessionManager.shutdown();
        // 断言：所有连接收到 SHUTDOWN 事件，停机后拒绝新连接
        for (SseConnection connection : Arrays.asList(c1, c2)) {
            RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
            assertTrue(emitter.frames.get(0).contains("\"reason\":\"SHUTDOWN\""));
            assertTrue(emitter.frames.get(0).contains("\"reconnect\":true"));
            assertTrue(emitter.completed);
        }
        assertEquals(0, sessionManager.getConnectionCount());
        assertTrue(sessionManager.isShutdown());
        assertFalse(sessionManager.register(newConnection(1L, 2, 300L, null)));
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L;
        while (!condition.getAsBoolean()) {
            assertTrue(System.currentTimeMillis() < deadline, "等待条件超时");
            Thread.sleep(5L);
        }
    }

}

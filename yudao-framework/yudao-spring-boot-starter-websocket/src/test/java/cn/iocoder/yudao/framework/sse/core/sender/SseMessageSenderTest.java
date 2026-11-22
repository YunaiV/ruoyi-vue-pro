package cn.iocoder.yudao.framework.sse.core.sender;

import cn.iocoder.yudao.framework.sse.core.codec.SseEventEncoder;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.local.LocalSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.session.RecordingSseEmitter;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManagerTest.newConnection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SseMessageSender} 的单元测试
 *
 * @author 芋道源码
 */
public class SseMessageSenderTest {

    private SseEmitterSessionManager sessionManager;
    private SseMessageSender messageSender;

    @BeforeEach
    public void setUp() {
        sessionManager = new SseEmitterSessionManager(50L);
        messageSender = new LocalSseMessageSender(sessionManager, 50L, 2);
    }

    @Test
    public void testSend_success() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用
        messageSender.send(2, 100L, "notice-push", "{}");
        String eventId = messageSender.send(null, 2, null, "demo", "{}");
        messageSender.send(connection.getConnectionId(), "demo", "{}");
        // 断言
        assertNotNull(eventId);
        assertEquals(3, emitter(connection).frames.size());
        assertTrue(emitter(connection).frames.get(1).contains("id:" + eventId));
    }

    @Test
    public void testDeliver_tenant() {
        // 准备参数
        SseConnection tenant1 = newConnection(1L, 2, 100L, null);
        SseConnection tenant2 = newConnection(2L, 2, 100L, null);
        SseConnection otherType = newConnection(1L, 1, 100L, null);
        sessionManager.register(tenant1);
        sessionManager.register(tenant2);
        sessionManager.register(otherType);

        // 调用：指定用户，只投递给同租户、同用户类型
        messageSender.deliver(message("E1").setTenantId(1L).setUserType(2).setUserId(100L));
        // 断言
        assertEquals(1, emitter(tenant1).frames.size());
        assertEquals(0, emitter(tenant2).frames.size());
        assertEquals(0, emitter(otherType).frames.size());

        // 调用：用户类型广播，按租户过滤
        messageSender.deliver(message("E2").setTenantId(2L).setUserType(2));
        // 断言
        assertEquals(1, emitter(tenant1).frames.size());
        assertEquals(1, emitter(tenant2).frames.size());
    }

    @Test
    public void testDeliver_nullTenant() {
        // 准备参数
        SseConnection tenant1 = newConnection(1L, 2, 100L, null);
        SseConnection tenant2 = newConnection(2L, 2, 200L, null);
        sessionManager.register(tenant1);
        sessionManager.register(tenant2);

        // 调用：租户为空，系统级广播
        messageSender.deliver(message("E1").setUserType(2));
        // 断言
        assertEquals(1, emitter(tenant1).frames.size());
        assertEquals(1, emitter(tenant2).frames.size());
    }

    @Test
    public void testDeliver_connectionIdOtherTenant() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用：其它租户的消息
        messageSender.deliver(message("E1").setTenantId(2L).setConnectionId(connection.getConnectionId()));
        // 断言
        assertTrue(emitter(connection).frames.isEmpty());

        // 调用：同租户的消息
        messageSender.deliver(message("E2").setTenantId(1L).setConnectionId(connection.getConnectionId()));
        // 断言
        assertEquals(1, emitter(connection).frames.size());
        assertTrue(emitter(connection).frames.get(0).contains("id:E2"));
    }

    @Test
    public void testDeliver_connectionIdNotExists() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用
        messageSender.deliver(message("E1").setTenantId(1L).setConnectionId("ws-session-1"));
        // 断言
        assertTrue(emitter(connection).frames.isEmpty());
    }

    @Test
    public void testDeliver_sendFailed() {
        // 准备参数
        SseConnection broken = newConnection(1L, 2, 100L, null);
        SseConnection healthy = newConnection(1L, 2, 100L, null);
        emitter(broken).failure = new IOException("broken pipe");
        sessionManager.register(broken);
        sessionManager.register(healthy);

        // 调用
        messageSender.deliver(message("E1").setTenantId(1L).setUserType(2).setUserId(100L));
        // 断言：失败的连接被移除，不影响其它连接
        assertNull(sessionManager.getConnection(broken.getConnectionId()));
        assertTrue(emitter(broken).completed);
        assertEquals(1, emitter(healthy).frames.size());
    }

    @Test
    public void testDeliver_lockTimeout() throws Exception {
        // 准备参数
        SseConnection slow = newConnection(1L, 2, 100L, null);
        emitter(slow).sendDelayMillis = 500;
        sessionManager.register(slow);
        // mock 慢客户端：另一个线程长时间持有发送锁
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            slow.send(SseEventEncoder.encodeHeartbeat(), 1000L);
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        // 调用：第一次等待锁超时，跳过本条
        messageSender.deliver(message("E1").setTenantId(1L).setUserType(2).setUserId(100L));
        // 断言
        assertNotNull(sessionManager.getConnection(slow.getConnectionId()));
        assertEquals(1, slow.getSendFailures());

        // 调用：第二次连续失败达到上限，断开
        messageSender.deliver(message("E2").setTenantId(1L).setUserType(2).setUserId(100L));
        // 断言
        assertNull(sessionManager.getConnection(slow.getConnectionId()));
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
    }

    @Test
    public void testDeliver_concurrent() throws InterruptedException {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        emitter(connection).sendDelayMillis = 1;
        sessionManager.register(connection);
        messageSender = new LocalSseMessageSender(sessionManager, 5000L, 3);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(200);

        // 调用
        for (int i = 0; i < 200; i++) {
            String eventId = "E" + i;
            executor.execute(() -> {
                try {
                    messageSender.deliver(message(eventId).setTenantId(1L).setUserType(2).setUserId(100L));
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();
        // 断言：发送锁保证串行，不出现并发写
        assertFalse(emitter(connection).overlapped);
        assertEquals(200, emitter(connection).frames.size());
    }

    private static SseMessage message(String eventId) {
        return new SseMessage().setEventId(eventId).setMessageType("demo").setMessageContent("{}")
                .setTimestamp(System.currentTimeMillis());
    }

    private static RecordingSseEmitter emitter(SseConnection connection) {
        return (RecordingSseEmitter) connection.getEmitter();
    }

}

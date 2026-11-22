package cn.iocoder.yudao.framework.sse.core.scheduler;

import cn.iocoder.yudao.framework.sse.core.session.RecordingSseEmitter;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManagerTest.newConnection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SseHeartbeatScheduler} 的单元测试
 *
 * @author 芋道源码
 */
public class SseHeartbeatSchedulerTest {

    private SseEmitterSessionManager sessionManager;
    private SseHeartbeatScheduler scheduler;

    @BeforeEach
    public void setUp() {
        sessionManager = new SseEmitterSessionManager(100L);
        scheduler = new SseHeartbeatScheduler(sessionManager, 1000L, 100L);
    }

    @Test
    public void testHeartbeat_success() {
        // 准备参数
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用
        scheduler.heartbeat();
        // 断言
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        assertEquals(1, emitter.frames.size());
        assertTrue(emitter.frames.get(0).startsWith(":ping "));
        assertNotNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testHeartbeat_sendFailed() {
        // 准备参数
        SseConnection broken = newConnection(1L, 2, 100L, null);
        ((RecordingSseEmitter) broken.getEmitter()).failure = new IOException("client gone");
        SseConnection healthy = newConnection(1L, 2, 200L, null);
        sessionManager.register(broken);
        sessionManager.register(healthy);

        // 调用
        scheduler.heartbeat();
        // 断言：失败的连接被移除，不影响其它连接
        assertNull(sessionManager.getConnection(broken.getConnectionId()));
        assertEquals(1, ((RecordingSseEmitter) healthy.getEmitter()).frames.size());
    }

    @Test
    public void testHeartbeat_tokenExpired() {
        // 准备参数
        SseConnection connection = new SseConnection(new RecordingSseEmitter(), 1L, 2, 100L, null,
                LocalDateTime.now().minusSeconds(1));
        sessionManager.register(connection);

        // 调用
        scheduler.heartbeat();
        // 断言
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        assertTrue(emitter.frames.get(0).contains("\"reason\":\"TOKEN_INVALID\""));
        assertTrue(emitter.completed);
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testHeartbeat_tokenNotExpired() {
        // 准备参数
        SseConnection connection = new SseConnection(new RecordingSseEmitter(), 1L, 2, 100L, null,
                LocalDateTime.now().plusMinutes(30));
        sessionManager.register(connection);

        // 调用
        scheduler.heartbeat();
        // 断言
        assertNotNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testHeartbeat_zombie() {
        // 准备参数：超过 3 个心跳周期没有发送成功
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);
        ReflectionTestUtils.setField(connection, "lastActiveTime", System.currentTimeMillis() - 3001L);

        // 调用
        scheduler.heartbeat();
        // 断言
        assertTrue(((RecordingSseEmitter) connection.getEmitter()).frames.isEmpty());
        assertNull(sessionManager.getConnection(connection.getConnectionId()));
    }

    @Test
    public void testStop_success() throws InterruptedException {
        // 准备参数
        scheduler = new SseHeartbeatScheduler(sessionManager, 50L, 100L);
        SseConnection connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);

        // 调用
        scheduler.start();
        scheduler.start(); // 重复启动
        assertTrue(scheduler.isRunning());
        Thread.sleep(200);
        scheduler.stop();
        // 断言：停止前发送过心跳，停止时关闭所有连接
        RecordingSseEmitter emitter = (RecordingSseEmitter) connection.getEmitter();
        assertFalse(scheduler.isRunning());
        assertTrue(emitter.countFrames(":ping") >= 1);
        assertTrue(emitter.frames.get(emitter.frames.size() - 1).contains("\"reason\":\"SHUTDOWN\""));
        assertTrue(emitter.completed);
        assertEquals(0, sessionManager.getConnectionCount());
        assertEquals(Integer.MAX_VALUE, scheduler.getPhase()); // 早于 Web 服务器的优雅停机执行
    }

}

package cn.iocoder.yudao.framework.websocket.core.sender;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.websocket.core.sender.local.LocalWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManagerImpl;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link LocalWebSocketMessageSender} 的单元测试
 *
 * @author 芋道源码
 */
public class LocalWebSocketMessageSenderTest {

    private WebSocketSessionManagerImpl sessionManager;
    private LocalWebSocketMessageSender sender;

    @BeforeEach
    public void setUp() {
        sessionManager = new WebSocketSessionManagerImpl();
        sender = new LocalWebSocketMessageSender(sessionManager);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSend_legacySignatures() throws IOException {
        // 准备参数
        WebSocketSession session = mockSession("s1", 1L, 1, 100L);
        sessionManager.addSession(session);
        TenantContextHolder.setTenantId(1L);

        // 调用：原有方法签名
        sender.send(1, 100L, "t1", "c1");
        sender.send(1, "t2", "c2");
        sender.send("s1", "t3", "c3");
        sender.sendObject(1, 100L, "t4", Collections.singletonMap("k", "v"));
        // 断言
        verify(session, times(4)).sendMessage(any());
    }

    @Test
    public void testSend_legacyMessage() throws IOException {
        // 准备参数：旧版本节点发出的消息，没有 eventId、tenantId
        WebSocketSession session = mockSession("s1", 1L, 1, 100L);
        sessionManager.addSession(session);
        TenantContextHolder.setTenantId(1L);
        WebSocketPushMessage legacy = new WebSocketPushMessage().setUserType(1).setUserId(100L).setMessageType("t");

        // 调用
        sender.send(legacy);
        // 断言：补本地 eventId，租户取自 MQ 租户拦截器恢复的上下文
        verify(session).sendMessage(any());
        assertEquals(1L, legacy.getTenantId());
        assertNotNull(legacy.getEventId());
    }

    @Test
    public void testSend_bySessionId() throws IOException {
        // 准备参数
        WebSocketSession session = mockSession("s1", 1L, 1, 100L);
        sessionManager.addSession(session);

        // 调用
        sender.send(message("E1").setSessionId("s1"));
        // 断言：下发的帧格式与原有 {type, content} 一致
        verify(session).sendMessage(argThat(m -> ((TextMessage) m).getPayload()
                .equals("{\"type\":\"demo-message-receive\",\"content\":\"{\\\"text\\\":\\\"hi\\\"}\"}")));
    }

    @Test
    public void testSend_bySessionIdOtherTenant() throws IOException {
        // 准备参数
        WebSocketSession session = mockSession("s1", 1L, 1, 100L);
        sessionManager.addSession(session);

        // 调用
        sender.send(message("E1").setTenantId(2L).setSessionId("s1"));
        // 断言
        verify(session, never()).sendMessage(any());
    }

    @Test
    public void testSend_byUserId() throws IOException {
        // 准备参数
        WebSocketSession s1 = mockSession("s1", 1L, 1, 100L);
        WebSocketSession s2 = mockSession("s2", 1L, 1, 100L);
        WebSocketSession other = mockSession("s3", 1L, 1, 200L);
        sessionManager.addSession(s1);
        sessionManager.addSession(s2);
        sessionManager.addSession(other);

        // 调用
        sender.send(message("E1").setTenantId(1L).setUserType(1).setUserId(100L));
        // 断言
        verify(s1).sendMessage(any());
        verify(s2).sendMessage(any());
        verify(other, never()).sendMessage(any());
    }

    @Test
    public void testSend_byUserType() throws IOException {
        // 准备参数
        WebSocketSession tenant1 = mockSession("s1", 1L, 1, 100L);
        WebSocketSession tenant2 = mockSession("s2", 2L, 1, 200L);
        sessionManager.addSession(tenant1);
        sessionManager.addSession(tenant2);
        TenantContextHolder.setTenantId(2L); // 上下文租户与消息租户不同时，以消息租户为准

        // 调用
        sender.send(message("E1").setTenantId(1L).setUserType(1));
        // 断言
        verify(tenant1).sendMessage(any());
        verify(tenant2, never()).sendMessage(any());
        assertEquals(2L, TenantContextHolder.getTenantId()); // 上下文被恢复
    }

    @Test
    public void testSend_byUserTypeNullTenant() throws IOException {
        // 准备参数
        WebSocketSession tenant1 = mockSession("s1", 1L, 1, 100L);
        WebSocketSession tenant2 = mockSession("s2", 2L, 1, 200L);
        sessionManager.addSession(tenant1);
        sessionManager.addSession(tenant2);

        // 调用：租户为空，系统级广播
        sender.send(message("E1").setUserType(1));
        // 断言
        verify(tenant1).sendMessage(any());
        verify(tenant2).sendMessage(any());
    }

    @Test
    public void testSend_sessionClosedOrFailed() throws IOException {
        // 准备参数
        WebSocketSession closed = mockSession("s1", 1L, 1, 100L);
        when(closed.isOpen()).thenReturn(false);
        WebSocketSession broken = mockSession("s2", 1L, 1, 100L);
        doThrow(new IOException("broken")).when(broken).sendMessage(any());
        WebSocketSession healthy = mockSession("s3", 1L, 1, 100L);
        sessionManager.addSession(closed);
        sessionManager.addSession(broken);
        sessionManager.addSession(healthy);

        // 调用
        sender.send(message("E1").setTenantId(1L).setUserType(1).setUserId(100L));
        // 断言：跳过已关闭的连接，发送异常不影响其它连接
        verify(closed, never()).sendMessage(any());
        verify(healthy).sendMessage(any());
    }

    static WebSocketSession mockSession(String id, Long tenantId, Integer userType, Long userId) {
        WebSocketSession session = mock(WebSocketSession.class);
        ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();
        WebSocketFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setUserType(userType).setTenantId(tenantId), attributes);
        when(session.getId()).thenReturn(id);
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private static WebSocketPushMessage message(String eventId) {
        return new WebSocketPushMessage().setEventId(eventId).setMessageType("demo-message-receive")
                .setMessageContent("{\"text\":\"hi\"}");
    }

}

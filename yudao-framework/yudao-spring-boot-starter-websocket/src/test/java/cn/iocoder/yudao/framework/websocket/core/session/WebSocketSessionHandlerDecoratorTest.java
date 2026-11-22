package cn.iocoder.yudao.framework.websocket.core.session;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link WebSocketSessionHandlerDecorator} 的单元测试
 *
 * @author 芋道源码
 */
public class WebSocketSessionHandlerDecoratorTest {

    private WebSocketSessionManagerImpl sessionManager;
    private WebSocketSessionHandlerDecorator decorator;

    @BeforeEach
    public void setUp() {
        sessionManager = new WebSocketSessionManagerImpl();
        decorator = new WebSocketSessionHandlerDecorator(mock(WebSocketHandler.class), sessionManager);
    }

    private static WebSocketSession mockSession(String id, Long userId, String clientId) {
        return mockSession(id, 1L, userId, clientId);
    }

    private static WebSocketSession mockSession(String id, Long tenantId, Long userId, String clientId) {
        WebSocketSession session = mock(WebSocketSession.class);
        ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();
        WebSocketFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setUserType(1).setTenantId(tenantId), attributes);
        WebSocketFrameworkUtils.setClientId(clientId, attributes);
        when(session.getId()).thenReturn(id);
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    public void testAfterConnectionEstablished_success() {
        // 准备参数
        WebSocketSession session = mockSession("s1", 100L, null);

        // 调用
        decorator.afterConnectionEstablished(session);
        // 断言
        assertNotNull(sessionManager.getSession("s1"));
        assertEquals(1, sessionManager.getSessionList(1, 100L).size());
    }

    @Test
    public void testAfterConnectionEstablished_sameClientId() throws Exception {
        // 准备参数
        WebSocketSession old = mockSession("s1", 100L, "tab-1");
        WebSocketSession newer = mockSession("s2", 100L, "tab-1");

        // 调用
        decorator.afterConnectionEstablished(old);
        decorator.afterConnectionEstablished(newer);
        // 断言：旧连接以 REPLACED 关闭
        verify(old).close(argThat((CloseStatus status) -> status.getCode() == 4001 && "REPLACED".equals(status.getReason())));
        assertNull(sessionManager.getSession("s1"));
        assertNotNull(sessionManager.getSession("s2"));
        assertEquals(1, sessionManager.getSessionList(1, 100L).size());
    }

    @Test
    public void testAfterConnectionEstablished_differentClientId() throws Exception {
        // 准备参数
        WebSocketSession tab1 = mockSession("s1", 100L, "tab-1");
        WebSocketSession tab2 = mockSession("s2", 100L, "tab-2");
        WebSocketSession otherTenant = mockSession("s3", 2L, 100L, "tab-1");
        WebSocketSession noClientId = mockSession("s4", 100L, null);

        // 调用
        decorator.afterConnectionEstablished(tab1);
        decorator.afterConnectionEstablished(tab2);
        decorator.afterConnectionEstablished(otherTenant);
        decorator.afterConnectionEstablished(noClientId);
        // 断言：只替换同租户、同用户、同 clientId 的连接
        verify(tab1, never()).close(any());
        verify(tab2, never()).close(any());
        assertEquals(4, sessionManager.getSessionList(1, 100L).size());
    }

    @Test
    public void testAfterConnectionClosed_idempotent() {
        // 准备参数
        WebSocketSession session = mockSession("s1", 100L, null);
        decorator.afterConnectionEstablished(session);

        // 调用
        decorator.afterConnectionClosed(session, CloseStatus.NORMAL);
        assertDoesNotThrow(() -> decorator.afterConnectionClosed(session, CloseStatus.NORMAL));
        // 断言
        assertNull(sessionManager.getSession("s1"));
        assertTrue(sessionManager.getSessionList(1, 100L).isEmpty());
    }

}

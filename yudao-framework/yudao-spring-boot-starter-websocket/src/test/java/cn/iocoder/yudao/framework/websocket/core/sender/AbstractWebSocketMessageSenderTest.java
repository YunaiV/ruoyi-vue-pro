package cn.iocoder.yudao.framework.websocket.core.sender;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.websocket.core.sender.local.LocalWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link AbstractWebSocketMessageSender} 的单元测试
 *
 * @author 芋道源码
 */
public class AbstractWebSocketMessageSenderTest {

    private WebSocketSession session;
    private LocalWebSocketMessageSender sender;

    @BeforeEach
    public void setUp() {
        WebSocketSessionManagerImpl sessionManager = new WebSocketSessionManagerImpl();
        session = LocalWebSocketMessageSenderTest.mockSession("s1", 1L, 1, 100L);
        sessionManager.addSession(session);
        sender = new LocalWebSocketMessageSender(sessionManager);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testPublish_success() throws Exception {
        // 准备参数
        TenantContextHolder.setTenantId(1L);
        WebSocketPushMessage message = new WebSocketPushMessage().setUserType(1).setUserId(100L).setMessageType("t");

        // 调用
        sender.publish(message);
        // 断言：补全 eventId、tenantId、timestamp
        verify(session).sendMessage(any());
        assertNotNull(message.getEventId());
        assertEquals(1L, message.getTenantId());
        assertNotNull(message.getTimestamp());
    }

    @Test
    public void testPublish_withEventId() {
        // 准备参数
        WebSocketPushMessage message = new WebSocketPushMessage().setEventId("E1").setUserType(1);

        // 调用
        sender.publish(message);
        // 断言
        assertEquals("E1", message.getEventId());
    }

    @Test
    public void testSend_success() throws Exception {
        // 准备参数
        TenantContextHolder.setTenantId(1L);

        // 调用
        String eventId1 = sender.send(null, 1, 100L, "t", "c");
        String eventId2 = sender.send(null, 1, 100L, "t", "c");
        // 断言：每条消息生成新的 eventId
        assertNotNull(eventId1);
        assertNotEquals(eventId1, eventId2);
        verify(session, times(2)).sendMessage(any());
    }

}

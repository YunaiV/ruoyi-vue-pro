package cn.iocoder.yudao.module.infra.api.websocket;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * {@link WebSocketSenderApiImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class WebSocketSenderApiImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private WebSocketSenderApiImpl webSocketSenderApi;

    @Mock
    private WebSocketMessageSender webSocketMessageSender;

    @Test
    public void testSend_senderAbsent_skipsAllOverloads() {
        // 准备参数
        Integer userType = 1;
        Long userId = 2L;
        String sessionId = "session";
        String messageType = "notice";
        String messageContent = "content";
        // mock 关闭 WebSocket 后，可选的发送器未注入
        ReflectUtil.setFieldValue(webSocketSenderApi, "webSocketMessageSender", null);

        // 调用、断言
        assertDoesNotThrow(() -> {
            webSocketSenderApi.send(userType, userId, messageType, messageContent);
            webSocketSenderApi.send(userType, messageType, messageContent);
            webSocketSenderApi.send(sessionId, messageType, messageContent);
        });
        verifyNoInteractions(webSocketMessageSender);
    }

    @Test
    public void testSend_senderPresent_delegatesAllOverloads() {
        // 准备参数
        Integer userType = 1;
        Long userId = 2L;
        String sessionId = "session";
        String messageType = "notice";
        String messageContent = "content";

        // 调用
        webSocketSenderApi.send(userType, userId, messageType, messageContent);
        webSocketSenderApi.send(userType, messageType, messageContent);
        webSocketSenderApi.send(sessionId, messageType, messageContent);
        // 断言
        verify(webSocketMessageSender).send(userType, userId, messageType, messageContent);
        verify(webSocketMessageSender).send(userType, messageType, messageContent);
        verify(webSocketMessageSender).send(sessionId, messageType, messageContent);
        verifyNoMoreInteractions(webSocketMessageSender);
    }

}

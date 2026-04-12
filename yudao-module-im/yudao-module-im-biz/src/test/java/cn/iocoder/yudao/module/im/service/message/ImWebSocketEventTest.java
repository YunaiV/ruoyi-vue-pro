package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImWebSocketTypeConstants;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM WebSocket 事件单元测试
 * <p>
 * 验证各事件的 payload 字段是否正确
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class ImWebSocketEventTest {

    @InjectMocks
    private ImPrivateMessageServiceImpl privateMessageService;

    @Mock
    private ImPrivateMessageMapper imPrivateMessageMapper;
    @Mock
    private ImFriendService imFriendService;
    @Mock
    private ImSensitiveWordService imSensitiveWordService;
    @Mock
    private WebSocketSenderApi webSocketSenderApi;

    @Test
    public void testSendMessage_websocketEvent() {
        // 准备
        ImPrivateMessageSendReqVO reqVO = new ImPrivateMessageSendReqVO();
        reqVO.setClientMessageId("evt-uuid");
        reqVO.setReceiverId(2L);
        reqVO.setType(0);
        reqVO.setContent("{\"content\":\"事件测试\"}");

        when(imPrivateMessageMapper.selectBySenderIdAndClientMessageId(1L, "evt-uuid")).thenReturn(null);
        when(imPrivateMessageMapper.insert(any())).thenReturn(1);

        // 调用
        privateMessageService.sendMessage(1L, reqVO);

        // 捕获 WebSocket 调用
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> contentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(webSocketSenderApi, times(2)).sendObject(anyInt(), anyLong(),
                typeCaptor.capture(), contentCaptor.capture());

        // 验证消息事件类型
        assertEquals(ImWebSocketTypeConstants.PRIVATE_MESSAGE, typeCaptor.getAllValues().get(0));
        assertEquals(ImWebSocketTypeConstants.PRIVATE_MESSAGE, typeCaptor.getAllValues().get(1));

        // 验证 payload 中 id 字段按 String 返回
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) contentCaptor.getAllValues().get(0);
        assertTrue(payload.get("id") instanceof String, "id 应为 String 类型");
        assertTrue(payload.get("senderId") instanceof String, "senderId 应为 String 类型");
        assertTrue(payload.get("receiverId") instanceof String, "receiverId 应为 String 类型");
        assertEquals("private", payload.get("messageScene"));
    }

    @Test
    public void testReadMessage_readAndReceiptEvents() {
        // 准备
        when(imPrivateMessageMapper.updateStatusToRead(1L, 2L)).thenReturn(1);

        // 调用
        privateMessageService.readMessages(1L, 2L);

        // 捕获
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
        verify(webSocketSenderApi, times(2)).sendObject(anyInt(), userCaptor.capture(),
                typeCaptor.capture(), any());

        // 第一次：发给自己的 READ 事件
        assertEquals(ImWebSocketTypeConstants.READ, typeCaptor.getAllValues().get(0));
        assertEquals(1L, userCaptor.getAllValues().get(0));
        // 第二次：发给对方的 RECEIPT 事件
        assertEquals(ImWebSocketTypeConstants.RECEIPT, typeCaptor.getAllValues().get(1));
        assertEquals(2L, userCaptor.getAllValues().get(1));
    }

    @Test
    public void testRecallMessage_recallEvent() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(1L).receiverId(2L).status(0).build();
        when(imPrivateMessageMapper.selectById(10L)).thenReturn(message);
        when(imPrivateMessageMapper.updateById(any())).thenReturn(1);

        // 调用
        privateMessageService.recallMessage(1L, 10L);

        // 捕获
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> contentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(webSocketSenderApi, times(2)).sendObject(anyInt(), anyLong(),
                typeCaptor.capture(), contentCaptor.capture());

        // 验证 RECALL 事件类型
        assertEquals(ImWebSocketTypeConstants.RECALL, typeCaptor.getAllValues().get(0));

        // 验证 payload
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) contentCaptor.getAllValues().get(0);
        assertEquals("10", payload.get("messageId"));
        assertEquals("private", payload.get("messageScene"));
    }

}

package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.websocket.ImPrivateMessageDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM WebSocket 事件单元测试
 * <p>
 * 验证各事件的 payload 字段是否正确
 *
 * @author 芋道源码
 */
public class ImWebSocketEventTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImPrivateMessageServiceImpl privateMessageService;

    @Mock
    private ImPrivateMessageMapper imPrivateMessageMapper;
    @Mock
    private ImFriendService imFriendService;
    @Mock
    private ImSensitiveWordService imSensitiveWordService;
    @Mock
    private WebSocketMessageSender webSocketMessageSender;

    @Test
    public void testSendMessage_websocketEvent() {
        try (var springUtilMockedStatic = mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtilMockedStatic.when(() -> cn.hutool.extra.spring.SpringUtil.getBean(eq(ImPrivateMessageServiceImpl.class)))
                    .thenReturn(privateMessageService);

            // 准备
            ImPrivateMessageSendReqVO reqVO = new ImPrivateMessageSendReqVO();
            reqVO.setClientMessageId("evt-uuid");
            reqVO.setReceiverId(2L);
            reqVO.setType(0);
            reqVO.setContent("{\"content\":\"事件测试\"}");

            when(imPrivateMessageMapper.selectBySenderIdAndClientMessageId(1L, "evt-uuid")).thenReturn(null);
            when(imPrivateMessageMapper.insert(any(ImPrivateMessageDO.class))).thenAnswer(invocation -> {
                ImPrivateMessageDO msg = invocation.getArgument(0);
                msg.setId(99L);
                return 1;
            });

            // 调用
            privateMessageService.sendPrivateMessage(1L, reqVO);

            // 捕获 WebSocket 调用
            ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object> contentCaptor = ArgumentCaptor.forClass(Object.class);
            verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(),
                    typeCaptor.capture(), contentCaptor.capture());

            // 验证消息事件类型：统一为 im-private-message
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(0));
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(1));

            // 验证 payload 是 DTO 类型
            ImPrivateMessageDTO payload = (ImPrivateMessageDTO) contentCaptor.getAllValues().get(0);
            assertNotNull(payload.getId(), "id 不应为空");
            assertEquals(1L, payload.getSenderId());
            assertEquals(2L, payload.getReceiverId());
        }
    }

    @Test
    public void testReadMessage_readAndReceiptEvents() {
        try (var springUtilMockedStatic = mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtilMockedStatic.when(() -> cn.hutool.extra.spring.SpringUtil.getBean(eq(ImPrivateMessageServiceImpl.class)))
                    .thenReturn(privateMessageService);

            // 准备：mock 未读消息（id=1 和 id=5，maxReadId 应为 5）
            List<ImPrivateMessageDO> unreadMessages = List.of(
                    ImPrivateMessageDO.builder().id(1L).senderId(2L).receiverId(1L)
                            .status(ImMessageStatusEnum.UNREAD.getStatus()).build(),
                    ImPrivateMessageDO.builder().id(5L).senderId(2L).receiverId(1L)
                            .status(ImMessageStatusEnum.UNREAD.getStatus()).build()
            );
            when(imPrivateMessageMapper.selectListBySenderIdAndReceiverIdAndStatus(2L, 1L,
                    ImMessageStatusEnum.UNREAD.getStatus())).thenReturn(unreadMessages);

            // 调用
            privateMessageService.readPrivateMessages(1L, 2L);

            // 捕获
            ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Object> contentCaptor = ArgumentCaptor.forClass(Object.class);
            verify(webSocketMessageSender, times(2)).sendObject(anyInt(), userCaptor.capture(),
                    typeCaptor.capture(), contentCaptor.capture());

            // 第一次：发给自己的 READ 事件（统一 type = im-private-message）
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(0));
            assertEquals(1L, userCaptor.getAllValues().get(0));
            ImPrivateMessageDTO readPayload = (ImPrivateMessageDTO) contentCaptor.getAllValues().get(0);
            assertEquals(ImMessageTypeEnum.READ.getType(), readPayload.getType());
            assertEquals(1L, readPayload.getSenderId());
            assertEquals(2L, readPayload.getReceiverId());
            assertEquals(5L, readPayload.getId(), "READ id 应为 maxReadId");

            // 第二次：发给对方的 RECEIPT 事件（统一 type = im-private-message）
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(1));
            assertEquals(2L, userCaptor.getAllValues().get(1));
            ImPrivateMessageDTO receiptPayload = (ImPrivateMessageDTO) contentCaptor.getAllValues().get(1);
            assertEquals(ImMessageTypeEnum.RECEIPT.getType(), receiptPayload.getType());
            assertEquals(1L, receiptPayload.getSenderId());
            assertEquals(2L, receiptPayload.getReceiverId());
            assertEquals(5L, receiptPayload.getId(), "RECEIPT id 应为 maxReadId");
        }
    }

    @Test
    public void testRecallMessage_tipTextEvent() {
        try (var springUtilMockedStatic = mockStatic(cn.hutool.extra.spring.SpringUtil.class)) {
            springUtilMockedStatic.when(() -> cn.hutool.extra.spring.SpringUtil.getBean(eq(ImPrivateMessageServiceImpl.class)))
                    .thenReturn(privateMessageService);

            // 准备
            ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                    .id(10L).senderId(1L).receiverId(2L).status(0)
                    .sendTime(java.time.LocalDateTime.now()).build();
            when(imPrivateMessageMapper.selectById(10L)).thenReturn(message);
            when(imPrivateMessageMapper.updateById(any(ImPrivateMessageDO.class))).thenReturn(1);
            when(imPrivateMessageMapper.insert(any(ImPrivateMessageDO.class))).thenReturn(1);

            // 调用
            privateMessageService.recallPrivateMessage(1L, 10L);

            // 捕获
            ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object> contentCaptor = ArgumentCaptor.forClass(Object.class);
            verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(),
                    typeCaptor.capture(), contentCaptor.capture());

            // 验证：推送的是 TIP_TEXT 消息（统一 type = im-private-message）
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(0));
            assertEquals(ImPrivateMessageDTO.TYPE, typeCaptor.getAllValues().get(1));
        }
    }

}

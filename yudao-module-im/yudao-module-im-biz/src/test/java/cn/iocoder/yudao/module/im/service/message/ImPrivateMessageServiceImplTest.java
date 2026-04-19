package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM 私聊消息 Service 单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class ImPrivateMessageServiceImplTest {

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

    private ImPrivateMessageSendReqVO buildSendReqVO() {
        ImPrivateMessageSendReqVO reqVO = new ImPrivateMessageSendReqVO();
        reqVO.setClientMessageId("test-uuid-001");
        reqVO.setReceiverId(2L);
        reqVO.setType(0); // TEXT
        reqVO.setContent("{\"content\":\"你好\"}");
        return reqVO;
    }

    // ========== 发送测试 ==========

    @Test
    public void testSendMessage_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImPrivateMessageServiceImpl.class)))
                    .thenReturn(privateMessageService);

            // 准备
            ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
            when(imPrivateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                    .thenReturn(null);
            when(imPrivateMessageMapper.insert(any(ImPrivateMessageDO.class))).thenAnswer(invocation -> {
                ImPrivateMessageDO msg = invocation.getArgument(0);
                msg.setId(99L);
                return 1;
            });

            // 调用
            ImPrivateMessageDO result = privateMessageService.sendPrivateMessage(1L, reqVO);

            // 断言
            assertNotNull(result);
            assertEquals(1L, result.getSenderId());
            assertEquals(2L, result.getReceiverId());
            assertEquals(0, result.getType());
            assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), result.getStatus());
            assertNotNull(result.getSendTime());

            // 验证调用
            verify(imFriendService).validateFriendExists(1L, 2L);
            verify(imSensitiveWordService).validateText(reqVO.getContent());
            verify(imPrivateMessageMapper).insert(any(ImPrivateMessageDO.class));
            verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(), anyString(), any());
        }
    }

    @Test
    public void testSendMessage_clientMessageIdIdempotent() {
        // 准备：模拟已存在消息
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        ImPrivateMessageDO existingMessage = ImPrivateMessageDO.builder()
                .id(100L).clientMessageId("test-uuid-001").senderId(1L).receiverId(2L)
                .type(0).content("{\"content\":\"你好\"}").status(0)
                .sendTime(LocalDateTime.now()).build();
        when(imPrivateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(existingMessage);

        // 调用
        ImPrivateMessageDO result = privateMessageService.sendPrivateMessage(1L, reqVO);

        // 断言：返回已存在的消息
        assertEquals(100L, result.getId());
        // 验证不会重复插入
        verify(imPrivateMessageMapper, never()).insert(any(ImPrivateMessageDO.class));
    }

    @Test
    public void testSendMessage_notFriend() {
        // 准备
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        when(imPrivateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(null);
        doThrow(new ServiceException(FRIEND_NOT_FRIEND))
                .when(imFriendService).validateFriendExists(1L, 2L);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.sendPrivateMessage(1L, reqVO));
        assertEquals(FRIEND_NOT_FRIEND.getCode(), exception.getCode());
    }

    // ========== pull 测试 ==========

    @Test
    public void testPullMessages_success() {
        // 准备
        List<ImPrivateMessageDO> mockMessages = List.of(
                ImPrivateMessageDO.builder().id(1L).senderId(1L).receiverId(2L).build(),
                ImPrivateMessageDO.builder().id(2L).senderId(2L).receiverId(1L).build()
        );
        when(imPrivateMessageMapper.selectListByMinId(1L, 0L, 100)).thenReturn(mockMessages);

        // 调用
        List<ImPrivateMessageDO> result = privateMessageService.pullPrivateMessages(1L, 0L, 100);

        // 断言
        assertEquals(2, result.size());
    }

    @Test
    public void testPullMessages_sizeExceeded() {
        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.pullPrivateMessages(1L, 0L, 1001));
        assertEquals(MESSAGE_PULL_SIZE_EXCEEDED.getCode(), exception.getCode());
    }

    // ========== 已读测试 ==========

    @Test
    public void testReadMessages_success() {
        // 准备
        when(imPrivateMessageMapper.updateStatusToRead(1L, 2L)).thenReturn(3);

        // 调用
        privateMessageService.readPrivateMessages(1L, 2L);

        // 断言：发送了 READ 和 RECEIPT 事件
        verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(), anyString(), any());
    }

    // ========== 撤回测试 ==========

    @Test
    public void testRecallMessage_success() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(1L).receiverId(2L)
                .status(ImMessageStatusEnum.UNREAD.getStatus()).build();
        when(imPrivateMessageMapper.selectById(10L)).thenReturn(message);
        when(imPrivateMessageMapper.updateById(any(ImPrivateMessageDO.class))).thenReturn(1);

        // 调用
        privateMessageService.recallPrivateMessage(1L, 10L);

        // 断言
        verify(imPrivateMessageMapper).updateById(any(ImPrivateMessageDO.class));
        // 验证推送了 RECALL 事件（给接收方和发送方）
        verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(), anyString(), any());
    }

    @Test
    public void testRecallMessage_notOwn() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(2L).receiverId(1L)
                .status(ImMessageStatusEnum.UNREAD.getStatus()).build();
        when(imPrivateMessageMapper.selectById(10L)).thenReturn(message);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.recallPrivateMessage(1L, 10L));
        assertEquals(MESSAGE_RECALL_DENIED.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_alreadyRecalled() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(1L).receiverId(2L)
                .status(ImMessageStatusEnum.RECALL.getStatus()).build();
        when(imPrivateMessageMapper.selectById(10L)).thenReturn(message);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.recallPrivateMessage(1L, 10L));
        assertEquals(MESSAGE_ALREADY_RECALLED.getCode(), exception.getCode());
    }

}

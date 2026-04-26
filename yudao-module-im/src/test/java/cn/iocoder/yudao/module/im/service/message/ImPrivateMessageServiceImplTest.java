package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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
public class ImPrivateMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImPrivateMessageServiceImpl privateMessageService;

    @Mock
    private ImPrivateMessageMapper privateMessageMapper;
    @Mock
    private ImFriendService friendService;
    @Mock
    private ImSensitiveWordService sensitiveWordService;
    @Mock
    private ImWebSocketService imWebSocketService;

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
        // 准备
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        when(privateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(null);
        when(friendService.isFriend(1L, 2L)).thenReturn(true);
        when(privateMessageMapper.insert(any(ImPrivateMessageDO.class))).thenAnswer(invocation -> {
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
        verify(friendService).isFriend(1L, 2L);
        verify(sensitiveWordService).validateText(reqVO.getContent());
        verify(privateMessageMapper).insert(any(ImPrivateMessageDO.class));
        // 验证推送给接收方和发送方
        verify(imWebSocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
        verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testSendMessage_clientMessageIdIdempotent() {
        // 准备：模拟已存在消息
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        ImPrivateMessageDO existingMessage = ImPrivateMessageDO.builder()
                .id(100L).clientMessageId("test-uuid-001").senderId(1L).receiverId(2L)
                .type(0).content("{\"content\":\"你好\"}").status(0)
                .sendTime(LocalDateTime.now()).build();
        when(privateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(existingMessage);

        // 调用
        ImPrivateMessageDO result = privateMessageService.sendPrivateMessage(1L, reqVO);

        // 断言：返回已存在的消息
        assertEquals(100L, result.getId());
        // 验证不会重复插入
        verify(privateMessageMapper, never()).insert(any(ImPrivateMessageDO.class));
    }

    @Test
    public void testSendMessage_notFriend() {
        // 准备
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        when(privateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(null);
        when(friendService.isFriend(1L, 2L)).thenReturn(false);

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
        when(privateMessageMapper.selectListByMinId(eq(1L), eq(0L), any(LocalDateTime.class), eq(100)))
                .thenReturn(mockMessages);

        // 调用
        List<ImPrivateMessageDO> result = privateMessageService.pullPrivateMessageList(1L, 0L, 100);

        // 断言
        assertEquals(2, result.size());
    }

    @Test
    public void testPullMessages_sizeExceeded() {
        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.pullPrivateMessageList(1L, 0L, 1001));
        assertEquals(MESSAGE_PULL_SIZE_EXCEEDED.getCode(), exception.getCode());
    }

    // ========== 已读测试 ==========

    @Test
    public void testReadMessages_success() {
        // 准备：前端上报已读到 messageId=5；mapper 返回更新行数 2 表示有未读被翻转
        when(privateMessageMapper.updateBySenderIdAndReceiverIdAndIdLeAndStatus(
                eq(2L), eq(1L), eq(5L),
                eq(ImMessageStatusEnum.UNREAD.getStatus()), any(ImPrivateMessageDO.class)))
                .thenReturn(2);

        // 调用
        privateMessageService.readPrivateMessages(1L, 2L, 5L);

        // 断言：更新了消息状态
        verify(privateMessageMapper).updateBySenderIdAndReceiverIdAndIdLeAndStatus(
                eq(2L), eq(1L), eq(5L),
                eq(ImMessageStatusEnum.UNREAD.getStatus()), any(ImPrivateMessageDO.class));

        // 断言：发送了 READ + RECEIPT 事件，payload 字段正确
        ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ImPrivateMessageDTO> contentCaptor = ArgumentCaptor.forClass(ImPrivateMessageDTO.class);
        verify(imWebSocketService, times(2)).sendPrivateMessageAsync(
                userCaptor.capture(), contentCaptor.capture());

        // 第一次：发给自己的 READ 事件
        assertEquals(1L, userCaptor.getAllValues().get(0));
        ImPrivateMessageDTO readPayload = contentCaptor.getAllValues().get(0);
        assertEquals(ImMessageTypeEnum.READ.getType(), readPayload.getType());
        assertEquals(1L, readPayload.getSenderId());
        assertEquals(2L, readPayload.getReceiverId());
        assertEquals(5L, readPayload.getId(), "READ id 应为前端上报的 messageId");

        // 第二次：发给对方的 RECEIPT 事件
        assertEquals(2L, userCaptor.getAllValues().get(1));
        ImPrivateMessageDTO receiptPayload = contentCaptor.getAllValues().get(1);
        assertEquals(ImMessageTypeEnum.RECEIPT.getType(), receiptPayload.getType());
        assertEquals(5L, receiptPayload.getId(), "RECEIPT id 应为前端上报的 messageId");
    }

    // ========== 撤回测试 ==========

    @Test
    public void testRecallMessage_success() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(1L).receiverId(2L)
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now()).build(); // 刚发送，5 分钟内
        when(privateMessageMapper.selectById(10L)).thenReturn(message);
        when(privateMessageMapper.updateById(any(ImPrivateMessageDO.class))).thenReturn(1);
        when(privateMessageMapper.insert(any(ImPrivateMessageDO.class))).thenReturn(1);

        // 调用
        ImPrivateMessageDO result = privateMessageService.recallPrivateMessage(1L, 10L);

        // 断言：返回 TIP_TEXT 消息
        assertNotNull(result);
        // 验证：更新原消息状态 + 插入 tipMessage
        verify(privateMessageMapper).updateById(any(ImPrivateMessageDO.class));
        verify(privateMessageMapper).insert(any(ImPrivateMessageDO.class));
        // 验证推送了消息（给接收方和发送方）
        verify(imWebSocketService, times(2)).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testRecallMessage_notOwn() {
        // 准备
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(2L).receiverId(1L)
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(privateMessageMapper.selectById(10L)).thenReturn(message);

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
                .status(ImMessageStatusEnum.RECALL.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(privateMessageMapper.selectById(10L)).thenReturn(message);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.recallPrivateMessage(1L, 10L));
        assertEquals(MESSAGE_ALREADY_RECALLED.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_notExists() {
        // 准备
        when(privateMessageMapper.selectById(10L)).thenReturn(null);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.recallPrivateMessage(1L, 10L));
        assertEquals(MESSAGE_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_timeout() {
        // 准备：消息发送于 10 分钟前（超过 5 分钟窗口）
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .id(10L).senderId(1L).receiverId(2L)
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now().minusMinutes(10)).build();
        when(privateMessageMapper.selectById(10L)).thenReturn(message);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.recallPrivateMessage(1L, 10L));
        assertEquals(MESSAGE_RECALL_TIMEOUT.getCode(), exception.getCode());
        // 断言：不推送、不插 tipMessage
        verify(privateMessageMapper, never()).insert(any(ImPrivateMessageDO.class));
    }

    @Test
    public void testSendMessage_sensitiveWordBlocked() {
        // 准备：文本消息命中敏感词
        ImPrivateMessageSendReqVO reqVO = buildSendReqVO();
        when(privateMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-001"))
                .thenReturn(null);
        when(friendService.isFriend(1L, 2L)).thenReturn(true);
        doThrow(new ServiceException(MESSAGE_SENSITIVE_WORD_BLOCKED))
                .when(sensitiveWordService).validateText(reqVO.getContent());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> privateMessageService.sendPrivateMessage(1L, reqVO));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());
        // 断言：不入库、不推送
        verify(privateMessageMapper, never()).insert(any(ImPrivateMessageDO.class));
        verify(imWebSocketService, never()).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testReadMessages_noUnread() {
        // 准备：mapper 返回 0 表示 id <= messageId 范围内没有未读消息
        when(privateMessageMapper.updateBySenderIdAndReceiverIdAndIdLeAndStatus(
                eq(2L), eq(1L), eq(5L),
                eq(ImMessageStatusEnum.UNREAD.getStatus()), any(ImPrivateMessageDO.class)))
                .thenReturn(0);

        // 调用
        privateMessageService.readPrivateMessages(1L, 2L, 5L);

        // 断言：不推送 READ / RECEIPT
        verify(imWebSocketService, never()).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    // ========== sendTipPrivateMessage ==========

    @Test
    public void testSendTipPrivateMessage_success() {
        // 调用
        privateMessageService.sendTipPrivateMessage(1L, 2L, "好友添加成功");

        // 断言：插入一条 TIP_TEXT 提示消息
        ArgumentCaptor<ImPrivateMessageDO> captor = ArgumentCaptor.forClass(ImPrivateMessageDO.class);
        verify(privateMessageMapper).insert(captor.capture());
        ImPrivateMessageDO tip = captor.getValue();
        assertEquals(1L, tip.getSenderId());
        assertEquals(2L, tip.getReceiverId());
        assertEquals(ImMessageTypeEnum.TIP_TEXT.getType(), tip.getType());
        assertEquals("好友添加成功", tip.getContent());
        assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), tip.getStatus());
        assertNotNull(tip.getClientMessageId());
        assertNotNull(tip.getSendTime());
        // 断言：给双方各推送一次
        verify(imWebSocketService).sendPrivateMessageAsync(eq(1L), any(ImPrivateMessageDTO.class));
        verify(imWebSocketService).sendPrivateMessageAsync(eq(2L), any(ImPrivateMessageDTO.class));
    }

    // ========== getPrivateMessageList ==========

    @Test
    public void testGetPrivateMessageList_delegatesToMapper() {
        // 准备
        ImPrivateMessageListReqVO reqVO = new ImPrivateMessageListReqVO();
        reqVO.setReceiverId(2L);
        reqVO.setMaxId(100L);
        reqVO.setLimit(20);
        List<ImPrivateMessageDO> mockList = List.of(
                ImPrivateMessageDO.builder().id(99L).senderId(1L).receiverId(2L).build(),
                ImPrivateMessageDO.builder().id(98L).senderId(2L).receiverId(1L).build()
        );
        when(privateMessageMapper.selectHistoryList(1L, 2L, 100L, 20)).thenReturn(mockList);

        // 调用
        List<ImPrivateMessageDO> result = privateMessageService.getPrivateMessageList(1L, reqVO);

        // 断言：透传到 mapper，参数一致
        assertEquals(2, result.size());
        verify(privateMessageMapper).selectHistoryList(1L, 2L, 100L, 20);
    }

}

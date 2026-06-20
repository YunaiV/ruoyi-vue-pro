package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.conversation.ImConversationReadService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImGroupMessageNotification;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.RecallMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link ImGroupMessageServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class ImGroupMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImGroupMessageServiceImpl groupMessageService;

    @Mock
    private ImGroupMessageMapper groupMessageMapper;
    @Mock
    private ImGroupService groupService;
    @Mock
    private ImGroupMemberService groupMemberService;
    @Mock
    private ImSensitiveWordService sensitiveWordService;
    @Mock
    private ImConversationReadService conversationReadService;
    @Mock
    private ImWebSocketService imWebSocketService;

    @Spy
    private ImProperties imProperties = new ImProperties();

    private ImGroupMessageSendReqVO buildSendReqVO() {
        ImGroupMessageSendReqVO reqVO = new ImGroupMessageSendReqVO();
        reqVO.setClientMessageId("test-uuid-group-001");
        reqVO.setGroupId(10L);
        reqVO.setType(ImContentTypeEnum.TEXT.getType());
        reqVO.setContent("{\"content\":\"群聊你好\"}");
        return reqVO;
    }

    // ========== 发送测试 ==========

    @Test
    public void testSendMessage_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备
            ImGroupMessageSendReqVO reqVO = buildSendReqVO();
            reqVO.setReceipt(true);
            when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                    .thenReturn(null);
            ImGroupDO group = new ImGroupDO();
            group.setId(10L);
            group.setName("测试群");
            when(groupService.validateGroupExists(10L)).thenReturn(group);

            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);

            when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                    .thenReturn(List.of(1L, 2L, 3L));
            when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenAnswer(invocation -> {
                ImGroupMessageDO msg = invocation.getArgument(0);
                msg.setId(99L);
                return 1;
            });

            // 调用
            ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);

            // 断言
            assertNotNull(result);
            assertEquals(1L, result.getSenderId());
            assertEquals(10L, result.getGroupId());
            assertEquals(ImMessageStatusEnum.NORMAL.getStatus(), result.getStatus());
            assertEquals(ImMessageReceiptStatusEnum.PENDING.getStatus(), result.getReceiptStatus());

            // 验证推送给 3 个群成员（含发送者自己，用于多端同步）
            ArgumentCaptor<ImGroupMessageNotification> payloadCaptor =
                    ArgumentCaptor.forClass(ImGroupMessageNotification.class);
            verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                    ids.size() == 3 && ids.contains(1L) && ids.contains(2L) && ids.contains(3L)),
                    eq(ImConversationTypeEnum.GROUP.getType()), eq(ImContentTypeEnum.TEXT.getType()),
                    payloadCaptor.capture());
            ImGroupMessageNotification payload = payloadCaptor.getValue();
            assertEquals(ImMessageReceiptStatusEnum.PENDING.getStatus(), payload.getReceiptStatus());
            assertEquals(0, payload.getReadCount());
            assertEquals(List.of(1L, 2L, 3L), payload.getReceiverUserIds());
        }
    }

    @Test
    public void testSendMessage_senderAlwaysVisible() {
        // 边界：成员缓存漏掉发送者（理论上 validateMemberInGroup 已挡，这里纯防御）；发送者仍必须固化进 receiver_user_ids
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            ImGroupMessageSendReqVO reqVO = buildSendReqVO();
            when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                    .thenReturn(null);
            ImGroupDO group = new ImGroupDO();
            group.setId(10L);
            group.setName("测试群");
            when(groupService.validateGroupExists(10L)).thenReturn(group);
            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
            // 成员缓存只返回 {2,3}，漏掉发送者 1
            when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                    .thenReturn(List.of(2L, 3L));
            when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenAnswer(invocation -> {
                ImGroupMessageDO msg = invocation.getArgument(0);
                msg.setId(99L);
                return 1;
            });

            ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);

            // 断言：固化快照必含发送者，推送目标补回发送者 = {1,2,3}
            assertTrue(result.getReceiverUserIds().contains(1L), "发送者必须在 receiver_user_ids 快照内");
            verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                    ids.size() == 3 && ids.contains(1L) && ids.contains(2L) && ids.contains(3L)),
                    anyInt(), anyInt(), any());
        }
    }

    @Test
    public void testSendMessage_clientMessageIdIdempotent() {
        // 准备
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        ImGroupMessageDO existing = ImGroupMessageDO.builder()
                .id(100L).clientMessageId("test-uuid-group-001").senderId(1L).groupId(10L)
                .type(0).content("{\"content\":\"群聊你好\"}").status(0)
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(existing);

        // 调用
        ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);

        // 断言
        assertEquals(100L, result.getId());
        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
    }

    @Test
    public void testSendMessage_notInGroup() {
        // 准备
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        when(groupMemberService.validateMemberInGroup(10L, 1L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.sendGroupMessage(1L, reqVO));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    @Test
    public void testSendMessage_quoteDirectedMessageRejected() {
        // 准备：发送人 1 在群 10，当前有效成员（广播受众）= {1,2,3}
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        reqVO.setContent("{\"content\":\"引用\",\"quote\":{\"messageId\":500}}");
        when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001")).thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        ImGroupMemberDO member = ImGroupMemberDO.builder().groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L, 3L));
        // 被引用原消息：定向给 {1,2}（对发送人 1 可见，但不覆盖广播受众 {1,2,3}）
        ImGroupMessageDO original = ImGroupMessageDO.builder().id(500L).groupId(10L).senderId(2L)
                .status(ImMessageStatusEnum.NORMAL.getStatus()).receiverUserIds(List.of(1L, 2L))
                .type(ImContentTypeEnum.TEXT.getType()).content("{\"content\":\"密\"}")
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(500L)).thenReturn(original);

        // 调用并断言：引用定向消息被拒绝，避免原文随广播泄漏给全群
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.sendGroupMessage(1L, reqVO));
        assertEquals(MESSAGE_QUOTE_INVALID.getCode(), exception.getCode());
        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
    }

    @Test
    public void testSendMessage_quoteBroadcastAccepted() {
        // 准备：广播受众 {1,2,3}，被引用原消息快照覆盖全部受众 → 允许引用
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        reqVO.setContent("{\"content\":\"引用\",\"quote\":{\"messageId\":500}}");
        when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001")).thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        ImGroupMemberDO member = ImGroupMemberDO.builder().groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L, 3L));
        ImGroupMessageDO original = ImGroupMessageDO.builder().id(500L).groupId(10L).senderId(2L)
                .status(ImMessageStatusEnum.NORMAL.getStatus()).receiverUserIds(List.of(1L, 2L, 3L))
                .type(ImContentTypeEnum.TEXT.getType()).content("{\"content\":\"公开\"}")
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(500L)).thenReturn(original);
        when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenAnswer(invocation -> {
            invocation.<ImGroupMessageDO>getArgument(0).setId(99L);
            return 1;
        });

        // 调用并断言：正常入库
        ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);
        assertEquals(99L, result.getId());
        verify(groupMessageMapper).insert(any(ImGroupMessageDO.class));
    }

    // ========== pull 测试 ==========

    @Test
    public void testPullMessages_doesNotOverwriteStatus() {
        // 准备：拉到两条消息；Phase 2 起 pull 不再按读位置覆盖 status，已读由前端按读位置判断
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO low = ImGroupMessageDO.builder()
                .id(5L).groupId(10L).senderId(2L).receiverUserIds(List.of(1L))
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(now.minusHours(2)).build();
        ImGroupMessageDO high = ImGroupMessageDO.builder()
                .id(10L).groupId(10L).senderId(2L).receiverUserIds(List.of(1L))
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(1L), anyCollection(), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(low, high));

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：status 保持 DB 原值（都 NORMAL），pull 不覆盖
        assertEquals(2, result.size());
        assertEquals(ImMessageStatusEnum.NORMAL.getStatus(), result.get(0).getStatus());
        assertEquals(ImMessageStatusEnum.NORMAL.getStatus(), result.get(1).getStatus());
    }

    @Test
    public void testPullMessages_receiptReadCountForSender() {
        // 准备：本人发送的回执消息，可见成员 {1,2,3}，分母排除发送者后为 {2,3}
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO receiptMsg = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(1L)
                .receiverUserIds(List.of(1L, 2L, 3L))
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .receiptStatus(ImMessageReceiptStatusEnum.PENDING.getStatus())
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(1L), anyCollection(), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(receiptMsg));

        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 100L);
        positions.put(3L, 50L);
        when(conversationReadService.getUserReadMessageIdMap(eq(ImConversationTypeEnum.GROUP.getType()), eq(10L)))
                .thenReturn(positions);

        List<ImGroupMemberDO> allMembers = List.of(
                member,
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(now.minusDays(20)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(now.minusDays(20)).build()
        );
        when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(allMembers);

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：分母 {2,3}，仅用户 2 读到 >=100 → readCount==1
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReadCount());
    }

    @Test
    public void testPullMessages_noReceiptMessageSkipsReadCount() {
        // 准备：本人发送但不需要回执（NO_RECEIPT）的消息——不补 readCount，status 也不被覆盖
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO noReceiptMsg = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(1L)
                .receiverUserIds(List.of(1L, 2L, 3L))
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .receiptStatus(ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus())
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(1L), anyCollection(), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(noReceiptMsg));

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：status 保持 NORMAL（pull 不覆盖），NO_RECEIPT 不补 readCount，也不查 readCount 相关的读位置映射
        assertEquals(1, result.size());
        assertEquals(ImMessageStatusEnum.NORMAL.getStatus(), result.get(0).getStatus());
        assertNull(result.get(0).getReadCount());
        verify(conversationReadService, never()).getUserReadMessageIdMap(anyInt(), anyLong());
    }

    // ========== 撤回测试 ==========

    @Test
    public void testRecallMessage_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备：消息由用户 1 发送，刚发送 5 分钟内
            ImGroupMessageDO message = ImGroupMessageDO.builder()
                    .id(50L).senderId(1L).groupId(10L)
                    .status(ImMessageStatusEnum.NORMAL.getStatus())
                    .sendTime(LocalDateTime.now()).build();
            when(groupMessageMapper.selectById(50L)).thenReturn(message);
            // 撤回前需要校验用户仍在群中
            ImGroupMemberDO selfMember = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(selfMember);
            when(groupMessageMapper.updateById(any(ImGroupMessageDO.class))).thenReturn(1);
            when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenReturn(1);

            when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                    .thenReturn(List.of(1L, 2L));

            // 调用
            ImGroupMessageDO result = groupMessageService.recallGroupMessage(1L, 50L);

            // 断言：返回撤回消息
            assertNotNull(result);
            // 验证：更新原消息状态 + 插入 RecallMessage
            verify(groupMessageMapper).updateById(any(ImGroupMessageDO.class));
            verify(groupMessageMapper).insert(any(ImGroupMessageDO.class));
            // 验证：给 2 个活跃成员推送撤回提示
            verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                    ids.size() == 2 && ids.contains(1L) && ids.contains(2L)),
                    anyInt(), anyInt(), any());
        }
    }

    @Test
    public void testRecallMessage_notOwn() {
        // 准备
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(2L).groupId(10L)
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(MESSAGE_RECALL_DENIED.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_alreadyRecalled() {
        // 准备
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(1L).groupId(10L)
                .status(ImMessageStatusEnum.RECALL.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(MESSAGE_ALREADY_RECALLED.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_timeout() {
        // 准备：消息发送于 10 分钟前（超过 5 分钟窗口）
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(1L).groupId(10L)
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(LocalDateTime.now().minusMinutes(10)).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(MESSAGE_RECALL_TIMEOUT.getCode(), exception.getCode());
    }

    // ========== 群已读测试 ==========

    @Test
    public void testReadMessages_messageInvisible() {
        // 准备：消息不在用户接收快照内
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(2L).receiverUserIds(List.of(2L))
                .sendTime(LocalDateTime.now()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(MESSAGE_NOT_IN_GROUP.getCode(), exception.getCode());
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testGetReadUserIds_withVisibleScope() {
        // 准备：发送者用户 5 是群成员
        ImGroupMemberDO currentMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(5L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(groupMemberService.validateMemberInGroup(10L, 5L)).thenReturn(currentMember);

        // 准备：消息由用户 5 发，快照可见范围为 {1,2,5}（用户 3 不在快照内 → 不可见）
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(80L).groupId(10L).senderId(5L)
                .receiverUserIds(List.of(1L, 2L, 5L))
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        when(groupMessageMapper.selectById(80L)).thenReturn(message);

        // 准备：群成员一览
        // 用户 1: 在快照内
        // 用户 2: 在快照内
        // 用户 3: 不在快照内 → 不可见
        // 用户 5: 发送者，不计入回执
        List<ImGroupMemberDO> allMembers = List.of(
                currentMember,
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(5L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build()  // 发送者
        );
        when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(allMembers);

        // 准备：已读位置 — 用户 1 读到 100, 用户 2 读到 50, 用户 3 读到 200
        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 50L);
        positions.put(3L, 200L);
        when(conversationReadService.getUserReadMessageIdMap(eq(ImConversationTypeEnum.GROUP.getType()), eq(10L)))
                .thenReturn(positions);

        // 调用：查询 messageId=80 的已读用户
        List<Long> readUsers = groupMessageService.getGroupReadUserIds(5L, 10L, 80L);

        // 断言：
        // 用户 1: 可见 + readMaxId=100>=80 → 已读 ✓
        // 用户 2: 可见 + readMaxId=50<80 → 未读 ✗
        // 用户 3: 不在快照内 → 不可见 → 不算
        // 用户 5: 发送者 → 排除
        assertEquals(1, readUsers.size());
        assertTrue(readUsers.contains(1L));
        assertFalse(readUsers.contains(2L));
        assertFalse(readUsers.contains(3L));
        assertFalse(readUsers.contains(5L));
    }

    @Test
    public void testGetReadUserIds_notInGroup() {
        // 准备：当前用户不在群中
        when(groupMemberService.validateMemberInGroup(10L, 99L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupReadUserIds(99L, 10L, 80L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    // ========== 发送边界 ==========

    @Test
    public void testSendMessage_receiptPending() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备：reqVO.receipt=true
            ImGroupMessageSendReqVO reqVO = buildSendReqVO();
            reqVO.setReceipt(true);
            when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                    .thenReturn(null);
            ImGroupDO group = new ImGroupDO();
            group.setId(10L);
            when(groupService.validateGroupExists(10L)).thenReturn(group);
            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
            when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L));
            when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenReturn(1);

            // 调用
            ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);

            // 断言：receipt=true → 回执状态为 PENDING
            assertEquals(ImMessageReceiptStatusEnum.PENDING.getStatus(), result.getReceiptStatus());
        }
    }

    @Test
    public void testSendMessage_sensitiveWordBlocked() {
        // 准备：文本消息命中敏感词
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        doThrow(new ServiceException(MESSAGE_SENSITIVE_WORD_BLOCKED))
                .when(sensitiveWordService).validateText(reqVO.getContent());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.sendGroupMessage(1L, reqVO));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());
        // 断言：不入库、不推送
        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
        verify(imWebSocketService, never()).sendNotificationAsync(anyCollection(), anyInt(), anyInt(), any());
    }

    // ========== 撤回边界 ==========

    @Test
    public void testRecallMessage_notExists() {
        when(groupMessageMapper.selectById(50L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(MESSAGE_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testRecallMessage_senderNotInGroup() {
        // 准备：消息合法、可撤回时间内，但发送人已退群
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(1L).groupId(10L)
                .status(ImMessageStatusEnum.NORMAL.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);
        when(groupMemberService.validateMemberInGroup(10L, 1L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
        // 断言：不执行更新、不插 tip
        verify(groupMessageMapper, never()).updateById(any(ImGroupMessageDO.class));
        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
    }

    // ========== 已读事件 ==========

    @Test
    public void testReadGroupMessages_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备：消息对用户可见；已读位置从 5 前进到 100
            when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                    .id(100L).groupId(10L).senderId(2L).receiverUserIds(List.of(1L))
                    .sendTime(LocalDateTime.now()).build());
            when(conversationReadService.getConversationReadMessageId(1L, ImConversationTypeEnum.GROUP.getType(), 10L))
                    .thenReturn(5L);
            when(conversationReadService.updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L))
                    .thenReturn(true);
            // readGroupMessageEvent 内部会调 selectListByGroupIdAndPendingReceipt → 返回空简化流程
            when(groupMessageMapper.selectListByGroupIdAndPendingReceipt(10L, 5L, 100L)).thenReturn(List.of());

            // 调用
            groupMessageService.readGroupMessages(1L, 10L, 100L);

            // 断言：已读位置更新 + READ 事件
            verify(conversationReadService).updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L);
            verify(imWebSocketService).sendNotificationAsync(eq(1L), anyInt(), anyInt(), any());
            verify(groupMemberService, never()).validateMemberInGroup(10L, 1L);
        }
    }

    @Test
    public void testReadGroupMessages_disabled() {
        // 准备：关闭群聊已读
        imProperties.getMessage().setGroupReadEnabled(false);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(MESSAGE_GROUP_READ_DISABLED.getCode(), exception.getCode());
        // 断言：已读位置不写、不推送
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
        verify(imWebSocketService, never()).sendNotificationAsync(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    public void testGetGroupReadUserIds_disabled() {
        // 准备：关闭群聊已读
        imProperties.getMessage().setGroupReadEnabled(false);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupReadUserIds(1L, 10L, 100L));
        assertEquals(MESSAGE_GROUP_READ_DISABLED.getCode(), exception.getCode());
    }

    @Test
    public void testSendGroupMessage_groupReadDisabled_forcesNoReceipt() {
        // 关闭群已读：发送方传 receipt=true 也强制落 NO_RECEIPT
        imProperties.getMessage().setGroupReadEnabled(false);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            ImGroupMessageSendReqVO reqVO = buildSendReqVO();
            reqVO.setReceipt(true); // 发送方明确要求回执
            when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                    .thenReturn(null);
            ImGroupDO group = new ImGroupDO();
            group.setId(10L);
            when(groupService.validateGroupExists(10L)).thenReturn(group);
            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
            when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L));

            ImGroupMessageDO result = groupMessageService.sendGroupMessage(1L, reqVO);

            assertEquals(ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), result.getReceiptStatus(),
                    "群已读关闭时即使发送方传 receipt=true 也强制落 NO_RECEIPT");
        }
    }

    @Test
    public void testReadGroupMessages_cursorAlreadyAhead() {
        // 准备：已读游标已 >= 目标，直接返回
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(2L).receiverUserIds(List.of(1L))
                .sendTime(LocalDateTime.now()).build());
        when(conversationReadService.getConversationReadMessageId(1L, ImConversationTypeEnum.GROUP.getType(), 10L))
                .thenReturn(200L);
        // CAS 命中旧位置 200 > 100，单调更新不前进，返回 false
        when(conversationReadService.updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L))
                .thenReturn(false);

        // 调用
        groupMessageService.readGroupMessages(1L, 10L, 100L);

        // 断言：调用了 CAS 更新但读位置未前进 → 不推送
        verify(conversationReadService).updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L);
        verify(imWebSocketService, never()).sendNotificationAsync(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    public void testReadGroupMessages_messageNotInGroup() {
        // 准备：messageId 属于其它群
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(20L).senderId(2L).sendTime(LocalDateTime.now()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(MESSAGE_NOT_IN_GROUP.getCode(), exception.getCode());
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testReadGroupMessages_quitMemberVisibleMessage_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备：历史消息的接收快照包含用户
            when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                    .id(100L).groupId(10L).senderId(2L).receiverUserIds(List.of(1L, 2L))
                    .sendTime(LocalDateTime.now().minusMinutes(1)).build());
            when(conversationReadService.getConversationReadMessageId(1L, ImConversationTypeEnum.GROUP.getType(), 10L))
                    .thenReturn(0L);
            when(conversationReadService.updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L))
                    .thenReturn(true);
            when(groupMessageMapper.selectListByGroupIdAndPendingReceipt(10L, 0L, 100L)).thenReturn(List.of());

            // 调用
            groupMessageService.readGroupMessages(1L, 10L, 100L);

            // 断言：不校验当前在群，直接推进已读位置
            verify(groupMemberService, never()).validateMemberInGroup(10L, 1L);
            verify(conversationReadService).updateConversationReadPosition(1L, ImConversationTypeEnum.GROUP.getType(), 10L, 100L);
        }
    }

    @Test
    public void testReadGroupMessages_quitMemberInvisibleMessage() {
        // 准备：消息不在历史接收快照内
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(2L).receiverUserIds(List.of(2L))
                .sendTime(LocalDateTime.now()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(MESSAGE_NOT_IN_GROUP.getCode(), exception.getCode());
        verify(conversationReadService, never()).updateConversationReadPosition(anyLong(), anyInt(), anyLong(), anyLong());
    }

    // ========== 回执状态迁移 ==========

    @Test
    public void testReadGroupMessageEvent_receiptDoneTransition() {
        // 准备：消息 100 发送者 5，群 10 活跃成员 {5,1,2}，可见用户中除发送者外 {1,2}
        //       已读位置 1→100、2→100 → 全部已读 → 迁移为 DONE
        ImGroupMessageDO pending = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(5L)
                .receiverUserIds(List.of(5L, 1L, 2L))
                .sendTime(LocalDateTime.now().minusMinutes(1))
                .receiptStatus(ImMessageReceiptStatusEnum.PENDING.getStatus())
                .status(ImMessageStatusEnum.NORMAL.getStatus()).build();
        when(groupMessageMapper.selectListByGroupIdAndPendingReceipt(10L, 0L, 100L))
                .thenReturn(List.of(pending));
        List<ImGroupMemberDO> activeMembers = List.of(
                ImGroupMemberDO.builder().groupId(10L).userId(5L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build()
        );
        when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(activeMembers);
        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 100L);
        when(conversationReadService.getUserReadMessageIdMap(ImConversationTypeEnum.GROUP.getType(), 10L))
                .thenReturn(positions);

        // 调用
        groupMessageService.readGroupMessageEvent(1L, 10L, 0L, 100L);

        // 断言：消息回执状态更新为 DONE
        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).updateById(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertEquals(ImMessageReceiptStatusEnum.DONE.getStatus(), captor.getValue().getReceiptStatus());
        // 断言：给消息发送方（5）推送 RECEIPT 事件
        verify(imWebSocketService).sendNotificationAsync(eq(5L), anyInt(), anyInt(), any());
    }

    @Test
    public void testReadGroupMessageEvent_receiptStaysPendingWhenPartialRead() {
        // 准备：用户 2 还没读到 100 → 仍为 PENDING，不更新 DB
        ImGroupMessageDO pending = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(5L)
                .receiverUserIds(List.of(5L, 1L, 2L))
                .sendTime(LocalDateTime.now().minusMinutes(1))
                .receiptStatus(ImMessageReceiptStatusEnum.PENDING.getStatus())
                .status(ImMessageStatusEnum.NORMAL.getStatus()).build();
        when(groupMessageMapper.selectListByGroupIdAndPendingReceipt(10L, 0L, 100L))
                .thenReturn(List.of(pending));
        List<ImGroupMemberDO> activeMembers = List.of(
                ImGroupMemberDO.builder().groupId(10L).userId(5L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.now().minusDays(10)).build()
        );
        when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(activeMembers);
        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 50L); // 还没读到 100
        when(conversationReadService.getUserReadMessageIdMap(ImConversationTypeEnum.GROUP.getType(), 10L))
                .thenReturn(positions);

        groupMessageService.readGroupMessageEvent(1L, 10L, 0L, 100L);

        // 断言：回执状态保持 PENDING，不更新 DB
        verify(groupMessageMapper, never()).updateById(any(ImGroupMessageDO.class));
        // 仍然推送 RECEIPT 事件给发送方（携带已读人数）
        verify(imWebSocketService).sendNotificationAsync(eq(5L), anyInt(), anyInt(), any());
    }

    // ========== 定向群消息 ==========

    @Test
    public void testSendGroupMessage_noReceiverUserIds_broadcastsToAllMembers() {
        // 准备：无定向 → 应发给所有 ENABLE 成员（含发送者自己，多端同步）
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}");
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 4 && ids.contains(1L) && ids.contains(2L)
                                && ids.contains(3L) && ids.contains(4L)),
                anyInt(), anyInt(), any());
    }

    @Test
    public void testSendGroupMessage_withReceiverUserIds_onlyTargetedPlusSender() {
        // 准备：定向给 {2,3}，发送者 1；群成员 {1,2,3,4}
        // 预期推送目标：{1,2,3}（发送者自己多端同步 + 定向列表）；4 被过滤
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}")
                .setReceiverUserIds(List.of(2L, 3L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 3 && ids.contains(1L) && ids.contains(2L)
                                && ids.contains(3L) && !ids.contains(4L)),
                anyInt(), anyInt(), any());
    }

    @Test
    public void testSendGroupMessage_withReceiverUserIds_senderAlwaysIncluded() {
        // 边界：发送者不在群成员缓存里（理论上应先被 validateMemberInGroup 挡住，这里纯防御）
        // 预期：定向用户 + 发送者自己（始终纳入快照保证多端同步）；非定向成员 4 被过滤
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}")
                .setReceiverUserIds(List.of(2L, 3L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(99L, dto);

        verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 3 && ids.contains(2L) && ids.contains(3L)
                                && ids.contains(99L) && !ids.contains(4L)),
                anyInt(), anyInt(), any());
    }

    // ========== DTO 群消息 ==========

    @Test
    public void testSendGroupMessage_dto_persistsAndSerializesPojoContent() {
        // 准备：persistent=true 类型 + POJO content
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECALL.getType())
                .setContent(new RecallMessage().setMessageId(50L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L));

        groupMessageService.sendGroupMessage(1L, dto);

        // 断言：入库 + 系统字段兜底 + content 序列化为 JSON + receiptStatus=NO_RECEIPT
        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).insert(captor.capture());
        ImGroupMessageDO message = captor.getValue();
        assertEquals(1L, message.getSenderId());
        assertEquals(10L, message.getGroupId());
        assertEquals(ImContentTypeEnum.RECALL.getType(), message.getType());
        assertEquals("{\"messageId\":50}", message.getContent());
        assertEquals(ImMessageStatusEnum.NORMAL.getStatus(), message.getStatus());
        assertEquals(ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), message.getReceiptStatus());
        assertNotNull(message.getClientMessageId());
        assertNotNull(message.getSendTime());
    }

    @Test
    public void testSendGroupMessage_dto_receiptPending() {
        // 准备：dto.receipt=true → receiptStatus=PENDING
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":50}").setReceipt(true);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L));

        groupMessageService.sendGroupMessage(1L, dto);

        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).insert(captor.capture());
        assertEquals(ImMessageReceiptStatusEnum.PENDING.getStatus(), captor.getValue().getReceiptStatus());
    }

    @Test
    public void testSendGroupMessage_dto_nonPersistentTypeNotInserted() {
        // 准备：persistent=false 类型（RECEIPT 回执）→ 不入库，仅推送
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.RECEIPT.getType());
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
        verify(imWebSocketService).sendNotificationAsync(anyCollection(), anyInt(), anyInt(), any());
    }

    @Test
    public void testSendGroupMessage_dto_groupMemberNicknameUpdateNotInserted() {
        // 准备：成员昵称变更只做在线同步，不入库
        ImGroupMessageSendDTO dto = ImGroupMessageSendDTO.ofGroupMemberNicknameUpdate(10L, 1L, "群昵称");
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
        verify(imWebSocketService).sendNotificationAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 2 && ids.contains(1L) && ids.contains(2L)),
                anyInt(), eq(ImContentTypeEnum.GROUP_MEMBER_NICKNAME_UPDATE.getType()), any());
    }

    @Test
    public void testSendGroupMessage_threeArg_explicitTargetsBypassActiveMembers() {
        // 准备：调用方传入显式 targets（解散场景成员已被批量 DISABLE，必须按移除前快照推送）
        Set<Long> targets = Set.of(1L, 2L, 3L);
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImContentTypeEnum.GROUP_DISSOLVE.getType()).setContent("{}");

        groupMessageService.sendGroupMessage(1L, targets, dto);

        // 断言：不读取 active members，按调用方 targets 推送
        verify(groupMemberService, never()).getActiveGroupMemberUserIdsByGroupId(anyLong());
        verify(imWebSocketService).sendNotificationAsync(eq(targets), anyInt(), anyInt(), any());
    }

    // ========== getGroupMessageList ==========

    @Test
    public void testGetGroupMessageList_delegatesToMapper() {
        // 准备：可见性由 SQL 按 receiver_user_ids 快照过滤，Service 直接透传 mapper 结果
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.now().minusDays(5)).build();
        when(groupMemberService.getGroupMember(10L, 1L)).thenReturn(member);

        ImGroupMessageListReqVO reqVO = new ImGroupMessageListReqVO();
        reqVO.setGroupId(10L);
        reqVO.setMaxId(100L);
        reqVO.setLimit(20);

        List<ImGroupMessageDO> mockList = List.of(
                ImGroupMessageDO.builder().id(99L).groupId(10L).senderId(2L)
                        .receiverUserIds(List.of(1L)).sendTime(LocalDateTime.now()).build()
        );
        when(groupMessageMapper.selectHistoryListByUser(1L, 10L, 100L, 20)).thenReturn(mockList);

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.getGroupMessageList(1L, reqVO);

        // 断言：透传到 mapper，参数一致
        assertEquals(1, result.size());
        assertEquals(99L, result.get(0).getId());
        verify(groupMessageMapper).selectHistoryListByUser(1L, 10L, 100L, 20);
    }

    @Test
    public void testGetGroupMessageList_notInGroup() {
        // 准备：用户从未在群中（getGroupMember 返回 null）
        ImGroupMessageListReqVO reqVO = new ImGroupMessageListReqVO();
        reqVO.setGroupId(10L);
        reqVO.setLimit(20);
        when(groupMemberService.getGroupMember(10L, 1L)).thenReturn(null);

        // 调用并断言：鉴权失败
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupMessageList(1L, reqVO));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
        // 断言：不查询 DB
        verify(groupMessageMapper, never()).selectHistoryListByUser(anyLong(), anyLong(), anyLong(), anyInt());
    }

}

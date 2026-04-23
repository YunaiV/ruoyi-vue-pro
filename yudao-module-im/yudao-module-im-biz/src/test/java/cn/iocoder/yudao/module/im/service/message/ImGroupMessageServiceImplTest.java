package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.dal.redis.message.GroupMessageReadRedisDAO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM 群聊消息 Service 单元测试
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
    private GroupMessageReadRedisDAO groupMessageReadRedisDAO;
    @Mock
    private ImWebSocketService imWebSocketService;

    private ImGroupMessageSendReqVO buildSendReqVO() {
        ImGroupMessageSendReqVO reqVO = new ImGroupMessageSendReqVO();
        reqVO.setClientMessageId("test-uuid-group-001");
        reqVO.setGroupId(10L);
        reqVO.setType(0); // TEXT
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
            when(groupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                    .thenReturn(null);
            ImGroupDO group = new ImGroupDO();
            group.setId(10L);
            group.setName("测试群");
            when(groupService.validateGroupExists(10L)).thenReturn(group);

            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);

            List<ImGroupMemberDO> allMembers = List.of(
                    member,
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(3L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(allMembers);
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
            assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), result.getStatus());
            assertEquals(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), result.getReceiptStatus());

            // 验证推送给 3 个群成员（含发送者自己，用于多端同步）
            verify(imWebSocketService).sendGroupMessageAsync(argThat((Collection<Long> ids) ->
                    ids.size() == 3 && ids.contains(1L) && ids.contains(2L) && ids.contains(3L)),
                    any(ImGroupMessageDTO.class));
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

    // ========== pull 测试 ==========

    @Test
    public void testPullMessages_joinTimeFilter() {
        // 准备：消息时间用"最近 1 天内"避免被 30 天窗口过滤
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinTime = now.minusHours(2);
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(joinTime).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        // 入群前 1 条 + 入群后 1 条
        ImGroupMessageDO beforeJoin = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .sendTime(now.minusHours(3)).build();
        ImGroupMessageDO afterJoin = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(2L)
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(beforeJoin, afterJoin));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：入群前消息不可见
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void testPullMessages_activeQueryRetriesWhenFirstBatchInvisible() {
        // 准备：首批 2 条（入群前 + 定向给别人）全部不可见，第二批命中可见消息
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinTime = now.minusHours(1);
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(joinTime).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO beforeJoin = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .sendTime(now.minusHours(3)).build();
        ImGroupMessageDO directedMsg = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(2L)
                .receiverUserIds(List.of(2L, 3L))
                .sendTime(now.minusHours(2)).build();
        ImGroupMessageDO visibleMsg = ImGroupMessageDO.builder()
                .id(3L).groupId(10L).senderId(2L)
                .sendTime(now.minusMinutes(30)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(beforeJoin, directedMsg));
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(2L),
                any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(visibleMsg));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 2);

        // 断言：仅返回第二批的可见消息；两批 selectListByMinId 各被调用一次
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        verify(groupMessageMapper).selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(2));
        verify(groupMessageMapper).selectListByMinId(eq(List.of(10L)), eq(2L),
                any(LocalDateTime.class), eq(2));
    }

    @Test
    public void testPullMessages_quitMemberLoadsPreQuitMessages() {
        // 准备：当前群（在群）+ 一个最近退群
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO activeMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(activeMember));

        ImGroupMessageDO activeMsg = ImGroupMessageDO.builder()
                .id(5L).groupId(10L).senderId(2L)
                .sendTime(now.minusHours(2)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(activeMsg));

        // 已退群：退群时间在窗口内（minId=0，minQuitTime 直接用 minSendTime）
        LocalDateTime quitTime = now.minusDays(3);
        ImGroupMemberDO quitMember = ImGroupMemberDO.builder()
                .groupId(20L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus())
                .joinTime(now.minusDays(20))
                .quitTime(quitTime).build();
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(quitMember));

        ImGroupMessageDO quitGroupMsg = ImGroupMessageDO.builder()
                .id(3L).groupId(20L).senderId(99L)
                .sendTime(now.minusDays(5)).build();
        when(groupMessageMapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(eq(20L), eq(0L),
                any(LocalDateTime.class), eq(quitTime), eq(100)))
                .thenReturn(List.of(quitGroupMsg));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：两条消息均返回，并按 id 升序
        assertEquals(2, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals(5L, result.get(1).getId());
    }

    @Test
    public void testPullMessages_quitMemberFilteredByBoundarySendTime() {
        // 准备：minId > 0，边界消息 sendTime 晚于窗口起点，应当被用作 minQuitTime
        LocalDateTime now = LocalDateTime.now();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of());

        LocalDateTime boundarySendTime = now.minusDays(2);
        ImGroupMessageDO boundary = ImGroupMessageDO.builder()
                .id(8L).groupId(10L).senderId(2L).sendTime(boundarySendTime).build();
        when(groupMessageMapper.selectById(8L)).thenReturn(boundary);
        when(groupMemberService.getQuitGroupMemberListByUserId(1L, boundarySendTime))
                .thenReturn(List.of());

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 8L, 100);

        // 断言：使用边界消息 sendTime 抬升 minQuitTime
        assertEquals(0, result.size());
        verify(groupMemberService).getQuitGroupMemberListByUserId(1L, boundarySendTime);
    }

    @Test
    public void testPullMessages_quitQueryKeepsOriginalMinIdWhenActiveQueryRetries() {
        // 准备：主查询首批不可见触发重试（游标从 8→101），退群补齐仍使用原始 minId=8
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinTime = now.minusHours(1);
        ImGroupMemberDO activeMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(joinTime).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(activeMember));

        ImGroupMessageDO beforeJoin = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(2L)
                .sendTime(now.minusHours(3)).build();
        ImGroupMessageDO directedMsg = ImGroupMessageDO.builder()
                .id(101L).groupId(10L).senderId(2L)
                .receiverUserIds(List.of(2L, 3L))
                .sendTime(now.minusMinutes(50)).build();
        ImGroupMessageDO visibleActiveMsg = ImGroupMessageDO.builder()
                .id(102L).groupId(10L).senderId(2L)
                .sendTime(now.minusMinutes(30)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(8L),
                any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(beforeJoin, directedMsg));
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(101L),
                any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(visibleActiveMsg));

        LocalDateTime boundarySendTime = now.minusDays(2);
        ImGroupMessageDO boundary = ImGroupMessageDO.builder()
                .id(8L).groupId(99L).senderId(2L).sendTime(boundarySendTime).build();
        when(groupMessageMapper.selectById(8L)).thenReturn(boundary);

        LocalDateTime quitTime = now.minusHours(12);
        ImGroupMemberDO quitMember = ImGroupMemberDO.builder()
                .groupId(20L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus())
                .joinTime(now.minusDays(10))
                .quitTime(quitTime).build();
        when(groupMemberService.getQuitGroupMemberListByUserId(1L, boundarySendTime))
                .thenReturn(List.of(quitMember));

        ImGroupMessageDO quitGroupMsg = ImGroupMessageDO.builder()
                .id(50L).groupId(20L).senderId(99L)
                .sendTime(now.minusDays(1)).build();
        when(groupMessageMapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(eq(20L), eq(8L),
                any(LocalDateTime.class), eq(quitTime), eq(2)))
                .thenReturn(List.of(quitGroupMsg));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 8L, 2);

        // 断言：退群补齐使用原始 minId=8 而非主查询重试后的 101
        assertEquals(2, result.size());
        assertEquals(50L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
        verify(groupMessageMapper).selectListByMinId(eq(List.of(10L)), eq(8L),
                any(LocalDateTime.class), eq(2));
        verify(groupMessageMapper).selectListByMinId(eq(List.of(10L)), eq(101L),
                any(LocalDateTime.class), eq(2));
        verify(groupMemberService).getQuitGroupMemberListByUserId(1L, boundarySendTime);
        verify(groupMessageMapper).selectListByGroupIdAndMinIdAndQuitTimeBefore(eq(20L), eq(8L),
                any(LocalDateTime.class), eq(quitTime), eq(2));
        verify(groupMessageMapper, never()).selectListByGroupIdAndMinIdAndQuitTimeBefore(eq(20L), eq(101L),
                any(LocalDateTime.class), eq(quitTime), eq(2));
    }

    @Test
    public void testPullMessages_receiverUserIdsFilter() {
        // 准备
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        // 定向接收消息：只给用户 2 和 3
        ImGroupMessageDO directedMsg = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(5L)
                .receiverUserIds(List.of(2L, 3L))
                .sendTime(now.minusHours(2)).build();
        // 全员消息
        ImGroupMessageDO allMsg = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(5L)
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(directedMsg, allMsg));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：定向接收的消息用户 1 看不到，只能看到全员消息
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void testPullMessages_atFieldCorrectReturn() {
        // 准备
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO atMsg = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .atUserIds(List.of(1L, 3L))
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(atMsg));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        // 断言：@ 字段正确返回
        assertEquals(1, result.size());
        assertEquals(List.of(1L, 3L), result.get(0).getAtUserIds());
    }

    @Test
    public void testPullMessages_pageTrimmedToSize() {
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO activeMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(activeMember));

        ImGroupMessageDO activeMsg1 = ImGroupMessageDO.builder()
                .id(1000L).groupId(10L).senderId(2L).sendTime(now.minusHours(2)).build();
        ImGroupMessageDO activeMsg2 = ImGroupMessageDO.builder()
                .id(2000L).groupId(10L).senderId(2L).sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(activeMsg1, activeMsg2));

        LocalDateTime quitTime = now.minusDays(1);
        ImGroupMemberDO quitMember = ImGroupMemberDO.builder()
                .groupId(20L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus())
                .joinTime(now.minusDays(20))
                .quitTime(quitTime).build();
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(quitMember));

        ImGroupMessageDO quit1 = ImGroupMessageDO.builder()
                .id(150L).groupId(20L).senderId(99L).sendTime(now.minusDays(5)).build();
        ImGroupMessageDO quit2 = ImGroupMessageDO.builder()
                .id(160L).groupId(20L).senderId(99L).sendTime(now.minusDays(4)).build();
        when(groupMessageMapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(eq(20L), eq(0L),
                any(LocalDateTime.class), eq(quitTime), eq(2)))
                .thenReturn(List.of(quit1, quit2));

        when(groupMessageReadRedisDAO.getReadMaxMessageId(anyLong(), eq(1L))).thenReturn(null);

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 2);

        assertEquals(2, result.size());
        assertEquals(150L, result.get(0).getId());
        assertEquals(160L, result.get(1).getId());
    }

    @Test
    public void testPullMessages_pullSetsReadFromRedisCursor() {
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO low = ImGroupMessageDO.builder()
                .id(5L).groupId(10L).senderId(2L).status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(now.minusHours(2)).build();
        ImGroupMessageDO high = ImGroupMessageDO.builder()
                .id(10L).groupId(10L).senderId(2L).status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(low, high));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(groupMessageReadRedisDAO.getReadMaxMessageId(10L, 1L)).thenReturn(7L);

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        assertEquals(2, result.size());
        assertEquals(ImMessageStatusEnum.READ.getStatus(), result.get(0).getStatus());
        assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), result.get(1).getStatus());
    }

    @Test
    public void testPullMessages_receiptReadCountForSender() {
        LocalDateTime now = LocalDateTime.now();
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(now.minusDays(10)).build();
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO receiptMsg = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(1L)
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .receiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus())
                .sendTime(now.minusHours(1)).build();
        when(groupMessageMapper.selectListByMinId(eq(List.of(10L)), eq(0L),
                any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(receiptMsg));
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(groupMessageReadRedisDAO.getReadMaxMessageId(10L, 1L)).thenReturn(100L);
        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 100L);
        positions.put(3L, 50L);
        when(groupMessageReadRedisDAO.getReadMaxMessageIdMap(10L)).thenReturn(positions);

        List<ImGroupMemberDO> allMembers = List.of(
                member,
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(now.minusDays(20)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(now.minusDays(20)).build()
        );
        when(groupMemberService.getGroupMemberListByGroupId(10L)).thenReturn(allMembers);

        List<ImGroupMessageDO> result = groupMessageService.pullGroupMessageList(1L, 0L, 100);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReadCount());
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
                    .status(ImMessageStatusEnum.UNREAD.getStatus())
                    .sendTime(LocalDateTime.now()).build();
            when(groupMessageMapper.selectById(50L)).thenReturn(message);
            // 撤回前需要校验用户仍在群中
            ImGroupMemberDO selfMember = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(selfMember);
            when(groupMessageMapper.updateById(any(ImGroupMessageDO.class))).thenReturn(1);
            when(groupMessageMapper.insert(any(ImGroupMessageDO.class))).thenReturn(1);

            List<ImGroupMemberDO> members = List.of(
                    selfMember,
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(members);

            // 调用
            ImGroupMessageDO result = groupMessageService.recallGroupMessage(1L, 50L);

            // 断言：返回 TIP_TEXT 消息
            assertNotNull(result);
            // 验证：更新原消息状态 + 插入 tipMessage
            verify(groupMessageMapper).updateById(any(ImGroupMessageDO.class));
            verify(groupMessageMapper).insert(any(ImGroupMessageDO.class));
            // 验证：给 2 个活跃成员推送撤回提示
            verify(imWebSocketService).sendGroupMessageAsync(argThat((Collection<Long> ids) ->
                    ids.size() == 2 && ids.contains(1L) && ids.contains(2L)),
                    any(ImGroupMessageDTO.class));
        }
    }

    @Test
    public void testRecallMessage_notOwn() {
        // 准备
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(2L).groupId(10L)
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now()).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);

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
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now().minusMinutes(10)).build();
        when(groupMessageMapper.selectById(50L)).thenReturn(message);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.recallGroupMessage(1L, 50L));
        assertEquals(MESSAGE_RECALL_TIMEOUT.getCode(), exception.getCode());
    }

    // ========== 群已读测试 ==========

    @Test
    public void testReadMessages_notInGroup() {
        // 准备：当前用户不在群中
        when(groupMemberService.validateMemberInGroup(10L, 1L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    @Test
    public void testGetReadUsers_withVisibleScope() {
        // 准备：用户 1 是群成员
        ImGroupMemberDO currentMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(currentMember);

        // 准备：消息由用户 5 发，发送时间在 2026-04-12 10:00
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(80L).groupId(10L).senderId(5L)
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        when(groupMessageMapper.selectById(80L)).thenReturn(message);

        // 准备：群成员一览
        // 用户 1: 正常，入群在消息之前
        // 用户 2: 正常，入群在消息之前
        // 用户 3: 正常，但入群在消息之后 → 不可见
        // 用户 5: 发送者，不计入回执
        List<ImGroupMemberDO> allMembers = List.of(
                currentMember,
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 4, 13, 0, 0, 0)).build(), // 消息之后才入群
                ImGroupMemberDO.builder().groupId(10L).userId(5L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build()  // 发送者
        );
        when(groupMemberService.getGroupMemberListByGroupId(10L)).thenReturn(allMembers);

        // 准备：Redis 已读位置 — 用户 1 读到 100, 用户 2 读到 50, 用户 3 读到 200
        Map<Long, Long> positions = new HashMap<>();
        positions.put(1L, 100L);
        positions.put(2L, 50L);
        positions.put(3L, 200L);
        when(groupMessageReadRedisDAO.getReadMaxMessageIdMap(10L)).thenReturn(positions);

        // 调用：查询 messageId=80 的已读用户
        List<Long> readUsers = groupMessageService.getGroupReadUsers(1L, 10L, 80L);

        // 断言：
        // 用户 1: 可见 + readMaxId=100>=80 → 已读 ✓
        // 用户 2: 可见 + readMaxId=50<80 → 未读 ✗
        // 用户 3: 入群在消息之后 → 不可见 → 不算
        // 用户 5: 发送者 → 排除
        assertEquals(1, readUsers.size());
        assertTrue(readUsers.contains(1L));
        assertFalse(readUsers.contains(2L));
        assertFalse(readUsers.contains(3L));
        assertFalse(readUsers.contains(5L));
    }

    @Test
    public void testGetReadUsers_notInGroup() {
        // 准备：当前用户不在群中
        when(groupMemberService.validateMemberInGroup(10L, 99L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupReadUsers(99L, 10L, 80L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

}

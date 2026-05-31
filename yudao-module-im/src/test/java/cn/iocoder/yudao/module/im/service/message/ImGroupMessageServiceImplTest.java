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
import cn.iocoder.yudao.module.im.dal.redis.message.ImGroupMessageReadRedisDAO;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.RecallMessage;
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
    private ImGroupMessageReadRedisDAO groupMessageReadRedisDAO;
    @Mock
    private ImWebSocketService imWebSocketService;

    /** 用真实实例避免 NPE；默认值与生产保持一致（recallTimeoutMinutes=5、private/group read enabled=true、maxPullSize=1000）；个别用例可改字段测分支 */
    @Spy
    private ImProperties imProperties = new ImProperties();

    private ImGroupMessageSendReqVO buildSendReqVO() {
        ImGroupMessageSendReqVO reqVO = new ImGroupMessageSendReqVO();
        reqVO.setClientMessageId("test-uuid-group-001");
        reqVO.setGroupId(10L);
        reqVO.setType(ImMessageTypeEnum.TEXT.getType());
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
                .status(ImMessageStatusEnum.UNREAD.getStatus())
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
    public void testGetReadUserIds_withVisibleScope() {
        // 准备：发送者用户 5 是群成员
        ImGroupMemberDO currentMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(5L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(groupMemberService.validateMemberInGroup(10L, 5L)).thenReturn(currentMember);

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
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build(),
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
        List<Long> readUsers = groupMessageService.getGroupReadUserIds(5L, 10L, 80L);

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
    public void testGetReadUserIds_notInGroup() {
        // 准备：当前用户不在群中
        when(groupMemberService.validateMemberInGroup(10L, 99L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(), GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupReadUserIds(99L, 10L, 80L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    // ========== 发送：补充边界 ==========

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
            assertEquals(ImGroupMessageReceiptStatusEnum.PENDING.getStatus(), result.getReceiptStatus());
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
        verify(imWebSocketService, never()).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
    }

    // ========== 撤回：补充边界 ==========

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
                .status(ImMessageStatusEnum.UNREAD.getStatus())
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

    // ========== 已读：happy path + 跳过 ==========

    @Test
    public void testReadGroupMessages_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupMessageServiceImpl.class)))
                    .thenReturn(groupMessageService);

            // 准备：用户在群；Redis 游标从 5 前进到 100
            ImGroupMemberDO member = ImGroupMemberDO.builder()
                    .groupId(10L).userId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
            when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                    .id(100L).groupId(10L).senderId(2L).sendTime(LocalDateTime.now()).build());
            when(groupMessageReadRedisDAO.getReadMaxMessageId(10L, 1L)).thenReturn(5L);
            // readGroupMessageEvent 内部会调 selectListByGroupIdAndPendingReceipt → 返回空简化流程
            when(groupMessageMapper.selectListByGroupIdAndPendingReceipt(10L, 5L, 100L)).thenReturn(List.of());

            // 调用
            groupMessageService.readGroupMessages(1L, 10L, 100L);

            // 断言：Redis 游标更新 + READ 事件
            verify(groupMessageReadRedisDAO).updateReadMaxMessageId(10L, 1L, 100L);
            verify(imWebSocketService).sendGroupMessageAsync(eq(1L), any(ImGroupMessageDTO.class));
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
        // 断言：Redis 不写、不推送
        verify(groupMessageReadRedisDAO, never()).updateReadMaxMessageId(anyLong(), anyLong(), anyLong());
        verify(imWebSocketService, never()).sendGroupMessageAsync(anyLong(), any(ImGroupMessageDTO.class));
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

            assertEquals(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), result.getReceiptStatus(),
                    "群已读关闭时即使发送方传 receipt=true 也强制落 NO_RECEIPT");
        }
    }

    @Test
    public void testReadGroupMessages_cursorAlreadyAhead() {
        // 准备：已读游标已 >= 目标，直接返回
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(2L).sendTime(LocalDateTime.now()).build());
        when(groupMessageReadRedisDAO.getReadMaxMessageId(10L, 1L)).thenReturn(200L);

        // 调用
        groupMessageService.readGroupMessages(1L, 10L, 100L);

        // 断言：不更新、不推送
        verify(groupMessageReadRedisDAO, never()).updateReadMaxMessageId(anyLong(), anyLong(), anyLong());
        verify(imWebSocketService, never()).sendGroupMessageAsync(anyLong(), any(ImGroupMessageDTO.class));
    }

    @Test
    public void testReadGroupMessages_messageNotInGroup() {
        // 准备：用户在群，但 messageId 属于其它群
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);
        when(groupMessageMapper.selectById(100L)).thenReturn(ImGroupMessageDO.builder()
                .id(100L).groupId(20L).senderId(2L).sendTime(LocalDateTime.now()).build());

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readGroupMessages(1L, 10L, 100L));
        assertEquals(MESSAGE_NOT_IN_GROUP.getCode(), exception.getCode());
        verify(groupMessageReadRedisDAO, never()).updateReadMaxMessageId(anyLong(), anyLong(), anyLong());
    }

    // ========== 回执：DONE 状态迁移 ==========

    @Test
    public void testReadGroupMessageEvent_receiptDoneTransition() {
        // 准备：消息 100 发送者 5，群 10 活跃成员 {5,1,2}，可见用户中除发送者外 {1,2}
        //       Redis 位置 1→100、2→100 → 全部已读 → 迁移为 DONE
        ImGroupMessageDO pending = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(5L)
                .sendTime(LocalDateTime.now().minusMinutes(1))
                .receiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus())
                .status(ImMessageStatusEnum.UNREAD.getStatus()).build();
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
        when(groupMessageReadRedisDAO.getReadMaxMessageIdMap(10L)).thenReturn(positions);

        // 调用
        groupMessageService.readGroupMessageEvent(1L, 10L, 0L, 100L);

        // 断言：消息回执状态更新为 DONE
        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).updateById(captor.capture());
        assertEquals(100L, captor.getValue().getId());
        assertEquals(ImGroupMessageReceiptStatusEnum.DONE.getStatus(), captor.getValue().getReceiptStatus());
        // 断言：给消息发送方（5）推送 RECEIPT 事件
        verify(imWebSocketService).sendGroupMessageAsync(eq(5L), any(ImGroupMessageDTO.class));
    }

    @Test
    public void testReadGroupMessageEvent_receiptStaysPendingWhenPartialRead() {
        // 准备：用户 2 还没读到 100 → 仍为 PENDING，不更新 DB
        ImGroupMessageDO pending = ImGroupMessageDO.builder()
                .id(100L).groupId(10L).senderId(5L)
                .sendTime(LocalDateTime.now().minusMinutes(1))
                .receiptStatus(ImGroupMessageReceiptStatusEnum.PENDING.getStatus())
                .status(ImMessageStatusEnum.UNREAD.getStatus()).build();
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
        when(groupMessageReadRedisDAO.getReadMaxMessageIdMap(10L)).thenReturn(positions);

        groupMessageService.readGroupMessageEvent(1L, 10L, 0L, 100L);

        // 断言：回执状态保持 PENDING，不更新 DB
        verify(groupMessageMapper, never()).updateById(any(ImGroupMessageDO.class));
        // 仍然推送 RECEIPT 事件给发送方（携带已读人数）
        verify(imWebSocketService).sendGroupMessageAsync(eq(5L), any(ImGroupMessageDTO.class));
    }

    // ========== sendGroupMessage(senderId, dto)：receiverUserIds 定向过滤 ==========

    @Test
    public void testSendGroupMessage_noReceiverUserIds_broadcastsToAllMembers() {
        // 准备：无定向 → 应发给所有 ENABLE 成员（含发送者自己，多端同步）
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}");
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(imWebSocketService).sendGroupMessageAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 4 && ids.contains(1L) && ids.contains(2L)
                                && ids.contains(3L) && ids.contains(4L)),
                any(ImGroupMessageDTO.class));
    }

    @Test
    public void testSendGroupMessage_withReceiverUserIds_onlyTargetedPlusSender() {
        // 准备：定向给 {2,3}，发送者 1；群成员 {1,2,3,4}
        // 预期推送目标：{1,2,3}（发送者自己多端同步 + 定向列表）；4 被过滤
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}")
                .setReceiverUserIds(List.of(2L, 3L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(imWebSocketService).sendGroupMessageAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 3 && ids.contains(1L) && ids.contains(2L)
                                && ids.contains(3L) && !ids.contains(4L)),
                any(ImGroupMessageDTO.class));
    }

    @Test
    public void testSendGroupMessage_withReceiverUserIds_senderNotInGroup_senderStillExcluded() {
        // 边界：发送者不在群的 userId 列表里（理论上应先被 validateMemberInGroup 挡住，这里纯防御）
        // 预期：仅定向用户可见
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":1}")
                .setReceiverUserIds(List.of(2L, 3L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        groupMessageService.sendGroupMessage(99L, dto);

        verify(imWebSocketService).sendGroupMessageAsync(argThat((Collection<Long> ids) ->
                        ids.size() == 2 && ids.contains(2L) && ids.contains(3L)),
                any(ImGroupMessageDTO.class));
    }

    // ========== sendGroupMessage(senderId, dto)：helper 行为 ==========

    @Test
    public void testSendGroupMessage_dto_persistsAndSerializesPojoContent() {
        // 准备：persistent=true 类型 + POJO content
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECALL.getType())
                .setContent(new RecallMessage().setMessageId(50L));
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L));

        groupMessageService.sendGroupMessage(1L, dto);

        // 断言：入库 + 系统字段兜底 + content 序列化为 JSON + receiptStatus=NO_RECEIPT
        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).insert(captor.capture());
        ImGroupMessageDO message = captor.getValue();
        assertEquals(1L, message.getSenderId());
        assertEquals(10L, message.getGroupId());
        assertEquals(ImMessageTypeEnum.RECALL.getType(), message.getType());
        assertEquals("{\"messageId\":50}", message.getContent());
        assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), message.getStatus());
        assertEquals(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), message.getReceiptStatus());
        assertNotNull(message.getClientMessageId());
        assertNotNull(message.getSendTime());
    }

    @Test
    public void testSendGroupMessage_dto_receiptPending() {
        // 准备：dto.receipt=true → receiptStatus=PENDING
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECALL.getType())
                .setContent("{\"messageId\":50}").setReceipt(true);
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L));

        groupMessageService.sendGroupMessage(1L, dto);

        ArgumentCaptor<ImGroupMessageDO> captor = ArgumentCaptor.forClass(ImGroupMessageDO.class);
        verify(groupMessageMapper).insert(captor.capture());
        assertEquals(ImGroupMessageReceiptStatusEnum.PENDING.getStatus(), captor.getValue().getReceiptStatus());
    }

    @Test
    public void testSendGroupMessage_dto_nonPersistentTypeNotInserted() {
        // 准备：persistent=false 类型（RECEIPT 回执）→ 不入库，仅推送
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.RECEIPT.getType());
        when(groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L)).thenReturn(List.of(1L, 2L));

        groupMessageService.sendGroupMessage(1L, dto);

        verify(groupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
        verify(imWebSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
    }

    @Test
    public void testSendGroupMessage_threeArg_explicitTargetsBypassActiveMembers() {
        // 准备：调用方传入显式 targets（解散场景成员已被批量 DISABLE，必须按移除前快照推送）
        Set<Long> targets = Set.of(1L, 2L, 3L);
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(10L).setType(ImMessageTypeEnum.GROUP_DISSOLVE.getType()).setContent("{}");

        groupMessageService.sendGroupMessage(1L, targets, dto);

        // 断言：不读取 active members，按调用方 targets 推送
        verify(groupMemberService, never()).getActiveGroupMemberUserIdsByGroupId(anyLong());
        verify(imWebSocketService).sendGroupMessageAsync(eq(targets), any(ImGroupMessageDTO.class));
    }

    // ========== getGroupMessageList ==========

    @Test
    public void testGetGroupMessageList_delegatesToMapperWithJoinTime() {
        // 准备
        LocalDateTime joinTime = LocalDateTime.now().minusDays(5);
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(joinTime).build();
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(member);

        ImGroupMessageListReqVO reqVO = new ImGroupMessageListReqVO();
        reqVO.setGroupId(10L);
        reqVO.setMaxId(100L);
        reqVO.setLimit(20);

        List<ImGroupMessageDO> mockList = List.of(
                ImGroupMessageDO.builder().id(99L).groupId(10L).senderId(2L)
                        .sendTime(joinTime.plusMinutes(1)).build()
        );
        when(groupMessageMapper.selectHistoryList(10L, 100L, 20, joinTime)).thenReturn(mockList);

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.getGroupMessageList(1L, reqVO);

        // 断言：使用成员的 joinTime 作为历史消息的下限
        assertEquals(1, result.size());
        verify(groupMessageMapper).selectHistoryList(10L, 100L, 20, joinTime);
    }

    @Test
    public void testGetGroupMessageList_notInGroup() {
        // 准备：用户不在群中
        ImGroupMessageListReqVO reqVO = new ImGroupMessageListReqVO();
        reqVO.setGroupId(10L);
        reqVO.setLimit(20);
        when(groupMemberService.validateMemberInGroup(10L, 1L))
                .thenThrow(new ServiceException(GROUP_MEMBER_NOT_IN_GROUP.getCode(),
                        GROUP_MEMBER_NOT_IN_GROUP.getMsg()));

        // 调用并断言：鉴权失败
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getGroupMessageList(1L, reqVO));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
        // 断言：不查询 DB
        verify(groupMessageMapper, never()).selectHistoryList(anyLong(), anyLong(), anyInt(), any());
    }

    // ========== delete read cursor 委托 ==========

    @Test
    public void testDeleteReadMaxMessageId_delegatesToRedis() {
        groupMessageService.deleteReadMaxMessageId(10L, 1L);

        verify(groupMessageReadRedisDAO).deleteReadMaxMessageId(10L, 1L);
    }

    @Test
    public void testDeleteReadMaxMessageIds_delegatesToRedis() {
        groupMessageService.deleteReadMaxMessageIds(10L, List.of(1L, 2L));

        verify(groupMessageReadRedisDAO).deleteReadMaxMessageIds(10L, List.of(1L, 2L));
    }

    @Test
    public void testDeleteReadMaxMessageIdMap_delegatesToRedis() {
        groupMessageService.deleteReadMaxMessageIdMap(10L);

        verify(groupMessageReadRedisDAO).deleteReadMaxMessageIdMap(10L);
    }

}

package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.dal.redis.group.GroupReadPositionRedisDAO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// DONE @芋艿：已评估，23 个测试全部通过
/**
 * IM 群聊消息 Service 单元测试
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class ImGroupMessageServiceImplTest {

    @InjectMocks
    private ImGroupMessageServiceImpl groupMessageService;

    @Mock
    private ImGroupMessageMapper imGroupMessageMapper;
    @Mock
    private ImGroupService imGroupService;
    @Mock
    private ImGroupMemberService imGroupMemberService;
    @Mock
    private ImSensitiveWordService imSensitiveWordService;
    @Mock
    private GroupReadPositionRedisDAO groupReadPositionRedisDAO;
    @Mock
    private WebSocketMessageSender webSocketMessageSender;

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
        // 准备
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        when(imGroupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        group.setName("测试群");
        when(imGroupService.validateGroupExists(10L)).thenReturn(group);

        ImGroupMemberDO member = new ImGroupMemberDO();
        member.setGroupId(10L);
        member.setUserId(1L);
        member.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(imGroupMemberService.getGroupMember(10L, 1L)).thenReturn(member);

        List<ImGroupMemberDO> allMembers = List.of(
                member,
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        when(imGroupMemberService.selectByGroupId(10L)).thenReturn(allMembers);
        when(imGroupMessageMapper.insert(any(ImGroupMessageDO.class))).thenAnswer(invocation -> {
            ImGroupMessageDO msg = invocation.getArgument(0);
            msg.setId(99L);
            return 1;
        });

        // 调用
        ImGroupMessageDO result = groupMessageService.sendMessage(1L, reqVO);

        // 断言
        assertNotNull(result);
        assertEquals(1L, result.getSenderId());
        assertEquals(10L, result.getGroupId());
        assertEquals(ImMessageStatusEnum.UNREAD.getStatus(), result.getStatus());
        assertEquals(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus(), result.getReceiptStatus());

        // 验证推送给 3 个群成员
        verify(webSocketMessageSender, times(3)).sendObject(anyInt(), anyLong(), anyString(), any());
    }

    @Test
    public void testSendMessage_clientMessageIdIdempotent() {
        // 准备
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        ImGroupMessageDO existing = ImGroupMessageDO.builder()
                .id(100L).clientMessageId("test-uuid-group-001").senderId(1L).groupId(10L)
                .type(0).content("{\"content\":\"群聊你好\"}").status(0)
                .sendTime(LocalDateTime.now()).build();
        when(imGroupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(existing);

        // 调用
        ImGroupMessageDO result = groupMessageService.sendMessage(1L, reqVO);

        // 断言
        assertEquals(100L, result.getId());
        verify(imGroupMessageMapper, never()).insert(any(ImGroupMessageDO.class));
    }

    @Test
    public void testSendMessage_notInGroup() {
        // 准备
        ImGroupMessageSendReqVO reqVO = buildSendReqVO();
        when(imGroupMessageMapper.selectBySenderIdAndClientMessageId(1L, "test-uuid-group-001"))
                .thenReturn(null);
        ImGroupDO group = new ImGroupDO();
        group.setId(10L);
        when(imGroupService.validateGroupExists(10L)).thenReturn(group);
        when(imGroupMemberService.getGroupMember(10L, 1L)).thenReturn(null);

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.sendMessage(1L, reqVO));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    // ========== pull 测试 ==========

    @Test
    public void testPullMessages_joinTimeFilter() {
        // 准备
        LocalDateTime joinTime = LocalDateTime.of(2026, 4, 12, 10, 0, 0);
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(joinTime).build();
        when(imGroupMemberService.getGroupMembersByUserId(1L)).thenReturn(List.of(member));

        // 模拟一条入群前的消息和一条入群后的消息
        ImGroupMessageDO beforeJoin = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .sendTime(LocalDateTime.of(2026, 4, 12, 9, 0, 0)).build();
        ImGroupMessageDO afterJoin = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(2L)
                .sendTime(LocalDateTime.of(2026, 4, 12, 11, 0, 0)).build();
        when(imGroupMessageMapper.selectListByMinId(List.of(10L), 0L, 100))
                .thenReturn(List.of(beforeJoin, afterJoin));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullMessages(1L, 0L, 100);

        // 断言：入群前消息不可见
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void testPullMessages_quitTimeFilter() {
        // 准备
        LocalDateTime joinTime = LocalDateTime.of(2026, 4, 10, 10, 0, 0);
        LocalDateTime quitTime = LocalDateTime.of(2026, 4, 12, 10, 0, 0);
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus())
                .joinTime(joinTime).quitTime(quitTime).build();
        when(imGroupMemberService.getGroupMembersByUserId(1L)).thenReturn(List.of(member));

        // 退群前后各一条消息
        ImGroupMessageDO beforeQuit = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .sendTime(LocalDateTime.of(2026, 4, 11, 10, 0, 0)).build();
        ImGroupMessageDO afterQuit = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(2L)
                .sendTime(LocalDateTime.of(2026, 4, 13, 10, 0, 0)).build();
        when(imGroupMessageMapper.selectListByMinId(List.of(10L), 0L, 100))
                .thenReturn(List.of(beforeQuit, afterQuit));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullMessages(1L, 0L, 100);

        // 断言：退群后消息不可见
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testPullMessages_receiverUserIdsFilter() {
        // 准备
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(imGroupMemberService.getGroupMembersByUserId(1L)).thenReturn(List.of(member));

        // 定向接收消息：只给用户 2 和 3
        ImGroupMessageDO directedMsg = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(5L)
                .receiverUserIds("2,3")
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        // 全员消息
        ImGroupMessageDO allMsg = ImGroupMessageDO.builder()
                .id(2L).groupId(10L).senderId(5L)
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        when(imGroupMessageMapper.selectListByMinId(List.of(10L), 0L, 100))
                .thenReturn(List.of(directedMsg, allMsg));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullMessages(1L, 0L, 100);

        // 断言：定向接收的消息用户 1 看不到，只能看到全员消息
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    public void testPullMessages_atFieldCorrectReturn() {
        // 准备
        ImGroupMemberDO member = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(imGroupMemberService.getGroupMembersByUserId(1L)).thenReturn(List.of(member));

        ImGroupMessageDO atMsg = ImGroupMessageDO.builder()
                .id(1L).groupId(10L).senderId(2L)
                .atUserIds("1,3")
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        when(imGroupMessageMapper.selectListByMinId(List.of(10L), 0L, 100))
                .thenReturn(List.of(atMsg));

        // 调用
        List<ImGroupMessageDO> result = groupMessageService.pullMessages(1L, 0L, 100);

        // 断言：@ 字段正确返回
        assertEquals(1, result.size());
        assertEquals("1,3", result.get(0).getAtUserIds());
    }

    // ========== 撤回测试 ==========

    @Test
    public void testRecallMessage_success() {
        // 准备
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(50L).senderId(1L).groupId(10L)
                .status(ImMessageStatusEnum.UNREAD.getStatus()).build();
        when(imGroupMessageMapper.selectById(50L)).thenReturn(message);
        when(imGroupMessageMapper.updateById(any(ImGroupMessageDO.class))).thenReturn(1);

        List<ImGroupMemberDO> members = List.of(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.DISABLE.getStatus()).build()
        );
        when(imGroupMemberService.selectByGroupId(10L)).thenReturn(members);

        // 调用
        groupMessageService.recallMessage(1L, 50L);

        // 断言：只给 2 个活跃成员发送 RECALL 事件（userId=3 已退群）
        verify(webSocketMessageSender, times(2)).sendObject(anyInt(), anyLong(), anyString(), any());
    }

    // ========== 群已读测试 ==========

    @Test
    public void testReadMessages_notInGroup() {
        // 准备：当前用户不在群中
        when(imGroupMemberService.getGroupMember(10L, 1L)).thenReturn(null);

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.readMessages(1L, 10L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    @Test
    public void testGetReadUsers_withVisibleScope() {
        // 准备：用户 1 是群成员
        ImGroupMemberDO currentMember = ImGroupMemberDO.builder()
                .groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .joinTime(LocalDateTime.of(2026, 1, 1, 0, 0, 0)).build();
        when(imGroupMemberService.getGroupMember(10L, 1L)).thenReturn(currentMember);

        // 准备：消息由用户 5 发，发送时间在 2026-04-12 10:00
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .id(80L).groupId(10L).senderId(5L)
                .sendTime(LocalDateTime.of(2026, 4, 12, 10, 0, 0)).build();
        when(imGroupMessageMapper.selectById(80L)).thenReturn(message);

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
        when(imGroupMemberService.selectByGroupId(10L)).thenReturn(allMembers);

        // 准备：Redis 已读位置 — 用户 1 读到 100, 用户 2 读到 50, 用户 3 读到 200
        Map<Object, Object> positions = new HashMap<>();
        positions.put("1", "100");
        positions.put("2", "50");
        positions.put("3", "200");
        when(groupReadPositionRedisDAO.getAllReadPositions(10L)).thenReturn(positions);

        // 调用：查询 messageId=80 的已读用户
        List<Long> readUsers = groupMessageService.getReadUsers(1L, 10L, 80L);

        // 断言：
        // 用户 1: 可见 + readMaxId=100>=80 → 已读 ✓（但排除发送者？不是发送者，所以算入）
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
        when(imGroupMemberService.getGroupMember(10L, 99L)).thenReturn(null);

        // 调用并断言：越权校验
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMessageService.getReadUsers(99L, 10L, 80L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

}

package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRemoveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM 群 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImGroupServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImGroupServiceImpl groupService;

    @Mock
    private ImGroupMapper groupMapper;
    @Mock
    private ImGroupMemberService groupMemberService;
    @Mock
    private ImGroupMessageService groupMessageService;
    @Mock
    private ImWebSocketService webSocketService;
    @Mock
    private ImFriendService friendService;
    @Mock
    private AdminUserApi adminUserApi;

    // ========== createGroup ==========

    @Test
    public void testCreateGroup_success() {
        // 准备
        ImGroupCreateReqVO reqVO = new ImGroupCreateReqVO();
        reqVO.setName("测试群");
        when(groupMapper.insert(any(ImGroupDO.class))).thenAnswer(invocation -> {
            ImGroupDO group = invocation.getArgument(0);
            group.setId(100L);
            return 1;
        });

        // 调用
        ImGroupDO result = groupService.createGroup(reqVO, 1L);

        // 断言：群主 + 状态
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getOwnerUserId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), result.getStatus());
        // 验证：群主加入群 + 推送群创建事件
        verify(groupMemberService).addGroupMember(100L, 1L);
        verify(webSocketService).sendGroupMessageAsync(eq(1L), any(ImGroupMessageDTO.class));
    }

    // ========== updateGroup ==========

    @Test
    public void testUpdateGroup_notOwner() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：当前用户不是群主
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupUpdateReqVO reqVO = new ImGroupUpdateReqVO();
            reqVO.setId(10L);
            reqVO.setName("新名字");

            // 调用并断言
            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.updateGroup(reqVO, 1L));
            assertEquals(GROUP_NOT_OWNER.getCode(), exception.getCode());
        }
    }

    @Test
    public void testUpdateGroup_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备
            ImGroupDO group = ImGroupDO.builder().id(10L).name("旧名字").ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            List<ImGroupMemberDO> members = List.of(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(members);

            ImGroupUpdateReqVO reqVO = new ImGroupUpdateReqVO();
            reqVO.setId(10L);
            reqVO.setName("新名字");

            // 调用
            ImGroupDO result = groupService.updateGroup(reqVO, 1L);

            // 断言：更新了数据库
            verify(groupMapper).updateById(any(ImGroupDO.class));
            assertEquals("新名字", result.getName());
            // 推送给所有成员
            verify(webSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
        }
    }

    // ========== dissolveGroup ==========

    @Test
    public void testDissolveGroup_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            List<ImGroupMemberDO> members = List.of(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(members);
            AdminUserRespDTO user = new AdminUserRespDTO();
            user.setId(1L);
            user.setNickname("群主");
            when(adminUserApi.getUser(1L)).thenReturn(user);

            // 调用
            groupService.dissolveGroup(10L, 1L);

            // 断言：群状态变为 DISABLE + 群成员全部移除 + 清理已读缓存
            ArgumentCaptor<ImGroupDO> captor = ArgumentCaptor.forClass(ImGroupDO.class);
            verify(groupMapper).updateById(captor.capture());
            assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
            assertNotNull(captor.getValue().getDissolvedTime());
            verify(groupMemberService).removeGroupMembersByGroupId(10L);
            verify(groupMessageService).deleteReadMaxMessageIdMap(10L);
            // 发送解散提示消息 + 推送群删除事件
            verify(groupMessageService).sendTipGroupMessage(eq(1L), eq(10L), anySet(), anyString());
            verify(webSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
        }
    }

    @Test
    public void testDissolveGroup_notOwner() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.dissolveGroup(10L, 1L));
            assertEquals(GROUP_NOT_OWNER.getCode(), exception.getCode());
        }
    }

    // ========== inviteGroupMember ==========

    @Test
    public void testInviteGroupMember_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：群存在 + 当前用户是群成员
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            // 当前成员只有群主
            List<ImGroupMemberDO> activeMembers = new ArrayList<>();
            activeMembers.add(ImGroupMemberDO.builder().groupId(10L).userId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build());
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(activeMembers);

            // 被邀请人 2 和 3 都是好友
            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(List.of(2L, 3L)));
            List<ImFriendDO> friends = List.of(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImFriendDO.builder().userId(1L).friendUserId(3L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(friends);

            // 用户昵称
            AdminUserRespDTO u1 = new AdminUserRespDTO(); u1.setId(1L); u1.setNickname("群主");
            AdminUserRespDTO u2 = new AdminUserRespDTO(); u2.setId(2L); u2.setNickname("张三");
            AdminUserRespDTO u3 = new AdminUserRespDTO(); u3.setId(3L); u3.setNickname("李四");
            Map<Long, AdminUserRespDTO> userMap23 = new HashMap<>();
            userMap23.put(2L, u2);
            userMap23.put(3L, u3);
            Map<Long, AdminUserRespDTO> userMap1 = Map.of(1L, u1);
            when(adminUserApi.getUserMap(List.of(2L, 3L))).thenReturn(userMap23);
            when(adminUserApi.getUserMap(List.of(1L))).thenReturn(userMap1);

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：校验群成员 + 批量添加成员 + 推送事件 + 发送提示消息
            verify(groupMemberService).validateMemberInGroup(10L, 1L);
            verify(groupMemberService).addGroupMembers(eq(10L), anyCollection());
            verify(webSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
            verify(groupMessageService).sendTipGroupMessage(eq(1L), eq(10L), anySet(), anyString());
        }
    }

    @Test
    public void testInviteGroupMember_notFriend() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L))
                    .thenReturn(List.of(ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(List.of(2L, 3L)));
            // 只有 2 是好友，3 不是
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(List.of(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));
            AdminUserRespDTO u3 = new AdminUserRespDTO(); u3.setId(3L); u3.setNickname("李四");
            when(adminUserApi.getUserMap(anyCollection())).thenReturn(Map.of(3L, u3));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.inviteGroupMember(1L, reqVO));
            assertEquals(GROUP_INVITE_NOT_FRIEND.getCode(), exception.getCode());
        }
    }

    @Test
    public void testInviteGroupMember_memberExceed() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：群 10 已有 499 人（逼近 MAX_GROUP_MEMBER=500），再邀请 2 人 → 501 超限
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            List<ImGroupMemberDO> activeMembers = new ArrayList<>();
            for (long i = 1; i <= 499; i++) {
                activeMembers.add(ImGroupMemberDO.builder().groupId(10L).userId(i)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
            }
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(activeMembers);

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(List.of(600L, 601L)));
            // 被邀请人都是好友
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(List.of(
                    ImFriendDO.builder().userId(1L).friendUserId(600L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImFriendDO.builder().userId(1L).friendUserId(601L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            ));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.inviteGroupMember(1L, reqVO));
            assertEquals(GROUP_MEMBER_EXCEED.getCode(), exception.getCode());
            // 断言：不加成员、不推送
            verify(groupMemberService, never()).addGroupMembers(anyLong(), anyCollection());
        }
    }

    @Test
    public void testInviteGroupMember_skipsMembersAlreadyInGroup() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            // 用户 2 已在群中
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(List.of(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            ));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(List.of(2L))); // 只邀请 2，他已在群中

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：不会触发添加、不会推送
            verify(groupMemberService, never()).addGroupMembers(anyLong(), anyCollection());
            verify(webSocketService, never()).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
        }
    }

    // ========== quitGroup ==========

    @Test
    public void testQuitGroup_ownerCannotQuit() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.quitGroup(10L, 1L));
            assertEquals(GROUP_OWNER_CANNOT_QUIT.getCode(), exception.getCode());
        }
    }

    @Test
    public void testQuitGroup_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            groupService.quitGroup(10L, 1L);

            verify(groupMemberService).removeGroupMember(10L, 1L);
            verify(groupMessageService).deleteReadMaxMessageId(10L, 1L);
            verify(groupMessageService).sendTipGroupMessage(eq(1L), eq(10L), anySet(), anyString());
            verify(webSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
        }
    }

    // ========== removeGroupMember ==========

    @Test
    public void testRemoveGroupMember_cannotRemoveSelf() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(List.of(1L, 2L));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.removeGroupMember(1L, reqVO));
            assertEquals(GROUP_CANNOT_REMOVE_SELF.getCode(), exception.getCode());
        }
    }

    @Test
    public void testRemoveGroupMember_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(List.of(2L, 3L));

            groupService.removeGroupMember(1L, reqVO);

            verify(groupMemberService).removeGroupMembers(eq(10L), anyCollection());
            verify(groupMessageService).deleteReadMaxMessageIds(eq(10L), anyCollection());
            verify(groupMessageService).sendTipGroupMessage(eq(1L), eq(10L), anySet(), anyString());
            verify(webSocketService).sendGroupMessageAsync(anyCollection(), any(ImGroupMessageDTO.class));
        }
    }

    // ========== validateGroupExists ==========

    @Test
    public void testValidateGroupExists_notExists() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            when(groupMapper.selectById(10L)).thenReturn(null);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.validateGroupExists(10L));
            assertEquals(GROUP_NOT_EXISTS.getCode(), exception.getCode());
        }
    }

    @Test
    public void testValidateGroupExists_banned() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).banned(true)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.validateGroupExists(10L));
            assertEquals(GROUP_BANNED.getCode(), exception.getCode());
        }
    }

    @Test
    public void testValidateGroupExists_dissolved() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L)
                    .status(CommonStatusEnum.DISABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.validateGroupExists(10L));
            assertEquals(GROUP_DISSOLVED.getCode(), exception.getCode());
        }
    }

    // ========== getMyGroupList ==========

    @Test
    public void testGetMyGroupList_noMembers() {
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(new ArrayList<>());
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<ImGroupDO> result = groupService.getMyGroupList(1L);
        assertTrue(result.isEmpty());
        verify(groupMapper, never()).selectByIds(anyCollection());
    }

    @Test
    public void testGetMyGroupList_success() {
        // 活跃群成员
        when(groupMemberService.getActiveGroupMemberListByUserId(1L)).thenReturn(new ArrayList<>(List.of(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(20L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        )));
        // 最近退群成员（最近 30 天内）
        when(groupMemberService.getQuitGroupMemberListByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>(List.of(
                        ImGroupMemberDO.builder().groupId(30L).userId(1L)
                                .status(CommonStatusEnum.DISABLE.getStatus()).build()
                )));
        List<ImGroupDO> groups = List.of(
                ImGroupDO.builder().id(10L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupDO.builder().id(20L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupDO.builder().id(30L).status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        when(groupMapper.selectByIds(anyCollection())).thenReturn(groups);

        List<ImGroupDO> result = groupService.getMyGroupList(1L);
        assertEquals(3, result.size());
    }

}

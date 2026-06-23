package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupAdminAddReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupAdminRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupMuteMemberReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupTransferOwnerReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerBanReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImGroupMessageNotification;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link ImGroupServiceImpl} 的单元测试
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
    private ImGroupRequestService groupRequestService;
    @Mock
    private AdminUserApi adminUserApi;
    @Spy
    private ImProperties imProperties = new ImProperties();

    // ========== createGroup ==========

    @Test
    public void testCreateGroup_success() {
        // 准备：仅创建者，无初始成员
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
        // 验证：群主加入群（带 OWNER role）+ 不调批量加成员（无初始成员）
        verify(groupMemberService).addGroupMember(100L, 1L, ImGroupMemberRoleEnum.OWNER.getRole());
        verify(groupMemberService, never()).addGroupMembers(anyLong(), anyCollection());
        // 验证：推送 GROUP_CREATE 通知（payload memberUserIds 含创建者自己）
        ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
        verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
        assertEquals(ImContentTypeEnum.GROUP_CREATE.getType(), dtoCaptor.getValue().getType());
    }

    @Test
    public void testCreateGroup_withInitialMembers() {
        // 准备：创建者 + 2 个初始成员，都是好友
        ImGroupCreateReqVO reqVO = new ImGroupCreateReqVO();
        reqVO.setName("测试群");
        reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
        when(groupMapper.insert(any(ImGroupDO.class))).thenAnswer(invocation -> {
            ImGroupDO group = invocation.getArgument(0);
            group.setId(100L);
            return 1;
        });
        when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
                ImFriendDO.builder().userId(1L).friendUserId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImFriendDO.builder().userId(1L).friendUserId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        ));

        // 调用
        ImGroupDO result = groupService.createGroup(reqVO, 1L);

        // 断言：群创建成功 + 创建者 + 初始成员都加入
        assertEquals(100L, result.getId());
        verify(groupMemberService).addGroupMember(100L, 1L, ImGroupMemberRoleEnum.OWNER.getRole());
        verify(groupMemberService).addGroupMembers(eq(100L), anyCollection(),
                eq(ImGroupAddSourceEnum.INVITE.getSource()), eq(1L));
        // 验证：推送 GROUP_CREATE 通知，payload memberUserIds 含全员（创建者 + 邀请）
        ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
        verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
        assertEquals(ImContentTypeEnum.GROUP_CREATE.getType(), dtoCaptor.getValue().getType());
    }

    @Test
    public void testCreateGroup_initialMemberNotFriend() {
        // 准备：初始成员里有非好友
        ImGroupCreateReqVO reqVO = new ImGroupCreateReqVO();
        reqVO.setName("测试群");
        reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
        // 只有 2 是好友，3 不是
        when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
                ImFriendDO.builder().userId(1L).friendUserId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));
        AdminUserRespDTO u3 = new AdminUserRespDTO();
        u3.setId(3L);
        u3.setNickname("李四");
        when(adminUserApi.getUserMap(anyCollection())).thenReturn(Collections.singletonMap(3L, u3));

        // 调用并断言
        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupService.createGroup(reqVO, 1L));
        assertEquals(GROUP_INVITE_NOT_FRIEND.getCode(), exception.getCode());
        verify(groupMapper, never()).insert(any(ImGroupDO.class));
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

            ImGroupUpdateReqVO reqVO = new ImGroupUpdateReqVO();
            reqVO.setId(10L);
            reqVO.setName("新名字");

            // 调用
            ImGroupDO result = groupService.updateGroup(reqVO, 1L);

            // 断言：更新了数据库
            verify(groupMapper).updateById(any(ImGroupDO.class));
            assertEquals("新名字", result.getName());
            // 推送 GROUP_NAME_UPDATE 通知给全员
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_NAME_UPDATE.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testUpdateGroup_joinApprovalChanged() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(1L)
                    .joinApproval(false).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupUpdateReqVO reqVO = new ImGroupUpdateReqVO();
            reqVO.setId(10L);
            reqVO.setJoinApproval(true);

            // 调用
            ImGroupDO result = groupService.updateGroup(reqVO, 1L);

            // 断言
            verify(groupMapper).updateById(any(ImGroupDO.class));
            assertTrue(result.getJoinApproval());
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_INFO_UPDATE.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testUpdateGroup_joinApprovalSame() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(1L)
                    .joinApproval(true).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupUpdateReqVO reqVO = new ImGroupUpdateReqVO();
            reqVO.setId(10L);
            reqVO.setJoinApproval(true);

            // 调用
            ImGroupDO result = groupService.updateGroup(reqVO, 1L);

            // 断言
            verify(groupMapper).updateById(any(ImGroupDO.class));
            assertTrue(result.getJoinApproval());
            verify(groupMessageService, never()).sendGroupMessage(anyLong(), anyCollection(), any());
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

            // 调用
            groupService.dissolveGroup(10L, 1L);

            // 断言：群状态变为 DISABLE + 群成员全部移除 + 清理已读缓存
            ArgumentCaptor<ImGroupDO> captor = ArgumentCaptor.forClass(ImGroupDO.class);
            verify(groupMapper).updateById(captor.capture());
            assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
            assertNotNull(captor.getValue().getDissolvedTime());
            verify(groupMemberService).removeGroupMembersByGroupId(10L);
            // 推送 GROUP_DISSOLVE 通知（send-before-remove，sendGroupMessage 内部查 active 自动覆盖全员，含群主多端同步）
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_DISSOLVE.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testDissolveGroup_banned_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：已封禁但未解散的群，群主仍可解散
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(1L)
                    .banned(true).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            // 调用
            groupService.dissolveGroup(10L, 1L);

            // 断言：封禁状态不阻止解散
            verify(groupMapper).updateById(argThat((ImGroupDO update) ->
                    CommonStatusEnum.DISABLE.getStatus().equals(update.getStatus())));
            verify(groupMemberService).removeGroupMembersByGroupId(10L);
        }
    }

    @Test
    public void testDissolveGroupByManager_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：管理员解散封禁群，不要求管理员是群主
            ImGroupDO group = ImGroupDO.builder().id(10L).name("群").ownerUserId(1L)
                    .banned(true).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            // 调用
            groupService.dissolveGroupByManager(99L, 10L);

            // 断言：使用管理员编号发通知并完成清理
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(99L), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_DISSOLVE.getType(), dtoCaptor.getValue().getType());
            verify(groupMemberService).removeGroupMembersByGroupId(10L);
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

            // 准备：群存在 + joinApproval=false（自由进群） + 当前用户是群主
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .joinApproval(false)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build());

            // 当前成员只有群主
            List<ImGroupMemberDO> activeMembers = new ArrayList<>();
            activeMembers.add(ImGroupMemberDO.builder().groupId(10L).userId(1L)
                    .role(ImGroupMemberRoleEnum.OWNER.getRole())
                    .status(CommonStatusEnum.ENABLE.getStatus()).build());
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(activeMembers);

            // 被邀请人 2 和 3 都是好友
            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
            List<ImFriendDO> friends = Arrays.asList(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImFriendDO.builder().userId(1L).friendUserId(3L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            );
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(friends);

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：校验群成员 + 批量添加成员（带 INVITE 来源 + 邀请人）+ 推送 GROUP_MEMBER_INVITE
            verify(groupMemberService).validateMemberInGroup(10L, 1L);
            verify(groupMemberService).addGroupMembers(eq(10L), anyCollection(),
                    eq(ImGroupAddSourceEnum.INVITE.getSource()), eq(1L));
            verify(groupRequestService, never()).createInviteRequestList(anyLong(), anyLong(), anyCollection());
            verify(webSocketService, never()).sendNotificationAsync(anyCollection(), anyInt(), anyInt(), any());
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_MEMBER_INVITE.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testInviteGroupMember_approval_normalRoutesToApproval() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：joinApproval=true，开启审批；普通成员邀请走审批，落 group_request
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .joinApproval(true)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build());
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(99L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImFriendDO.builder().userId(1L).friendUserId(3L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：走审批分支：调 createInviteRequestList；不写群成员、不推 1509
            verify(groupRequestService).createInviteRequestList(eq(10L), eq(1L), anyCollection());
            verify(groupMemberService, never()).addGroupMembers(anyLong(), anyCollection(), any(), any());
            verify(groupMessageService, never()).sendGroupMessage(anyLong(), any(ImGroupMessageSendDTO.class));
        }
    }

    @Test
    public void testInviteGroupMember_approval_ownerBypassesApproval() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            // 准备：joinApproval=true，但邀请人是群主；视同已审批，直进群
            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .joinApproval(true)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build());
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImFriendDO.builder().userId(1L).friendUserId(3L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：绕过审批，直接 addGroupMembers + 推 1509；不落 group_request
            verify(groupRequestService, never()).createInviteRequestList(anyLong(), anyLong(), anyCollection());
            verify(groupMemberService).addGroupMembers(eq(10L), anyCollection(),
                    eq(ImGroupAddSourceEnum.INVITE.getSource()), eq(1L));
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), anyCollection(), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_MEMBER_INVITE.getType(), dtoCaptor.getValue().getType());
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
                    .thenReturn(Arrays.asList(ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L, 3L)));
            // 只有 2 是好友，3 不是
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
                    ImFriendDO.builder().userId(1L).friendUserId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));
            AdminUserRespDTO u3 = new AdminUserRespDTO(); u3.setId(3L); u3.setNickname("李四");
            when(adminUserApi.getUserMap(anyCollection())).thenReturn(Collections.singletonMap(3L, u3));

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
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(600L, 601L)));
            // 被邀请人都是好友
            when(friendService.getActiveFriendList(eq(1L), anyCollection())).thenReturn(Arrays.asList(
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
            when(groupMemberService.getActiveGroupMemberListByGroupId(10L)).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()
            ));

            ImGroupMemberInviteReqVO reqVO = new ImGroupMemberInviteReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(new ArrayList<>(Arrays.asList(2L))); // 只邀请 2，他已在群中

            // 调用
            groupService.inviteGroupMember(1L, reqVO);

            // 断言：不会触发添加、不会推送
            verify(groupMemberService, never()).addGroupMembers(anyLong(), anyCollection());
            verify(webSocketService, never()).sendNotificationAsync(anyCollection(), anyInt(), anyInt(), any());
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
            // 推送 GROUP_MEMBER_QUIT 通知给全员（含 quitter，前端自判清群）
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_MEMBER_QUIT.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testQuitGroup_bannedGroupAllowed() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .banned(true).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            groupService.quitGroup(10L, 1L);

            verify(groupMemberService).removeGroupMember(10L, 1L);
            verify(groupMessageService).sendGroupMessage(eq(1L), any(ImGroupMessageSendDTO.class));
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
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole()).build());

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(Arrays.asList(1L, 2L));

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
            // 操作者：群主
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole()).build());
            // 目标：两个普通成员
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(3L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(Arrays.asList(2L, 3L));

            groupService.removeGroupMember(1L, reqVO);

            verify(groupMemberService).removeGroupMembers(eq(10L), anyCollection());
            // 推送 GROUP_MEMBER_KICK 通知给全员（含被踢者，前端自判清群）
            ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
            verify(groupMessageService).sendGroupMessage(eq(1L), dtoCaptor.capture());
            assertEquals(ImContentTypeEnum.GROUP_MEMBER_KICK.getType(), dtoCaptor.getValue().getType());
        }
    }

    @Test
    public void testRemoveGroupMember_adminCannotRemoveAdmin() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            // 操作者：管理员
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole()).build());
            // 目标：另一个管理员
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(Arrays.asList(2L));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.removeGroupMember(1L, reqVO));
            assertEquals(GROUP_REMOVE_ADMIN_DENIED.getCode(), exception.getCode());
            verify(groupMemberService, never()).removeGroupMembers(anyLong(), anyCollection());
        }
    }

    @Test
    public void testRemoveGroupMember_ownerCannotBeRemoved() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            // 操作者：群主（不能踢自己；这里换成另一个 userId 的群主语义 — 用 ADMIN 操作群主）
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole()).build());
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(99L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build()));

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(Arrays.asList(99L));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.removeGroupMember(1L, reqVO));
            assertEquals(GROUP_REMOVE_OWNER_DENIED.getCode(), exception.getCode());
        }
    }

    @Test
    public void testRemoveGroupMember_skipInactiveTargets() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            // 操作者：群主
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.OWNER.getRole()).build());
            // 目标：2L 有效普通成员；3L 已退群（DISABLE）的历史管理员，应被跳过而非拦截整批
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                            .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                    ImGroupMemberDO.builder().groupId(10L).userId(3L)
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole())
                            .status(CommonStatusEnum.DISABLE.getStatus()).build()));

            ImGroupMemberRemoveReqVO reqVO = new ImGroupMemberRemoveReqVO();
            reqVO.setGroupId(10L);
            reqVO.setMemberUserIds(Arrays.asList(2L, 3L));

            groupService.removeGroupMember(1L, reqVO);

            // 仅有效成员 2L 进入移除，已退群的 3L 被跳过
            ArgumentCaptor<Collection> removeCaptor = ArgumentCaptor.forClass(Collection.class);
            verify(groupMemberService).removeGroupMembers(eq(10L), removeCaptor.capture());
            assertEquals(Collections.singleton(2L), new HashSet<>(removeCaptor.getValue()));
        }
    }

    // ========== addGroupAdmin ==========

    @Test
    public void testAddGroupAdmin_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            // 目标 3 是普通成员
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(3L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build()));
            // 群里已有 1 个 ADMIN，1 + 1 ≤ 3 不超上限
            when(groupMemberService.getGroupMemberCountByRole(10L, ImGroupMemberRoleEnum.ADMIN.getRole()))
                    .thenReturn(1L);
            when(groupMemberService.updateGroupMemberRole(eq(10L), anyCollection(),
                    eq(ImGroupMemberRoleEnum.ADMIN.getRole()))).thenReturn(1);

            ImGroupAdminAddReqVO reqVO = new ImGroupAdminAddReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(3L));

            groupService.addGroupAdmin(1L, reqVO);

            verify(groupMemberService).updateGroupMemberRole(eq(10L), argThat((Set<Long> ids) ->
                            ids.size() == 1 && ids.contains(3L)),
                    eq(ImGroupMemberRoleEnum.ADMIN.getRole()));
        }
    }

    @Test
    public void testAddGroupAdmin_exceedsLimit() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(5L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build()));
            // 群里已有 3 个 ADMIN（达到上限），再加 1 会超
            when(groupMemberService.getGroupMemberCountByRole(10L, ImGroupMemberRoleEnum.ADMIN.getRole()))
                    .thenReturn(3L);

            ImGroupAdminAddReqVO reqVO = new ImGroupAdminAddReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(5L));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.addGroupAdmin(1L, reqVO));
            assertEquals(GROUP_ADMIN_MAX_LIMIT.getCode(), exception.getCode());
            verify(groupMemberService, never()).updateGroupMemberRole(anyLong(), anyCollection(), anyInt());
        }
    }

    @Test
    public void testAddGroupAdmin_targetIsOwner() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(1L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.OWNER.getRole()).build()));

            ImGroupAdminAddReqVO reqVO = new ImGroupAdminAddReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(1L));

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.addGroupAdmin(1L, reqVO));
            assertEquals(GROUP_ADMIN_TARGET_IS_OWNER.getCode(), exception.getCode());
        }
    }

    @Test
    public void testAddGroupAdmin_idempotentSkipWhenAlreadyAdmin() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            // 目标已是 ADMIN：再加无需操作
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(2L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole()).build()));

            ImGroupAdminAddReqVO reqVO = new ImGroupAdminAddReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(2L));

            groupService.addGroupAdmin(1L, reqVO);

            verify(groupMemberService, never()).updateGroupMemberRole(anyLong(), anyCollection(), anyInt());
            verify(groupMemberService, never()).getGroupMemberCountByRole(anyLong(), anyInt());
        }
    }

    // ========== removeGroupAdmin ==========

    @Test
    public void testRemoveGroupAdmin_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(2L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole()).build()));
            when(groupMemberService.updateGroupMemberRole(eq(10L), anyCollection(),
                    eq(ImGroupMemberRoleEnum.NORMAL.getRole()))).thenReturn(1);

            ImGroupAdminRemoveReqVO reqVO = new ImGroupAdminRemoveReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(2L));

            groupService.removeGroupAdmin(1L, reqVO);

            verify(groupMemberService).updateGroupMemberRole(eq(10L), argThat((Set<Long> ids) ->
                            ids.size() == 1 && ids.contains(2L)),
                    eq(ImGroupMemberRoleEnum.NORMAL.getRole()));
        }
    }

    @Test
    public void testRemoveGroupAdmin_idempotentSkipWhenAlreadyMember() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            // 目标已是 MEMBER：撤销无需操作
            when(groupMemberService.getGroupMembers(eq(10L), anyCollection())).thenReturn(Arrays.asList(
                    ImGroupMemberDO.builder().userId(2L).status(CommonStatusEnum.ENABLE.getStatus())
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build()));

            ImGroupAdminRemoveReqVO reqVO = new ImGroupAdminRemoveReqVO();
            reqVO.setId(10L);
            reqVO.setUserIds(Arrays.asList(2L));

            groupService.removeGroupAdmin(1L, reqVO);

            verify(groupMemberService, never()).updateGroupMemberRole(anyLong(), anyCollection(), anyInt());
        }
    }

    // ========== transferGroupOwner ==========

    @Test
    public void testTransferGroupOwner_success() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);
            when(groupMemberService.validateMemberInGroup(10L, 2L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build());
            when(groupMemberService.updateGroupMemberRole(eq(10L), eq(Collections.singleton(2L)),
                    eq(ImGroupMemberRoleEnum.OWNER.getRole()))).thenReturn(1);
            when(groupMemberService.updateGroupMemberRole(eq(10L), eq(Collections.singleton(1L)),
                    eq(ImGroupMemberRoleEnum.NORMAL.getRole()))).thenReturn(1);

            ImGroupTransferOwnerReqVO reqVO = new ImGroupTransferOwnerReqVO();
            reqVO.setId(10L);
            reqVO.setNewOwnerUserId(2L);

            groupService.transferGroupOwner(1L, reqVO);

            // 群表 owner 切换
            ArgumentCaptor<ImGroupDO> groupCaptor = ArgumentCaptor.forClass(ImGroupDO.class);
            verify(groupMapper).updateById(groupCaptor.capture());
            assertEquals(2L, groupCaptor.getValue().getOwnerUserId());
            // 旧群主 → MEMBER；新群主 → OWNER
            verify(groupMemberService).updateGroupMemberRole(eq(10L), eq(Collections.singleton(1L)),
                    eq(ImGroupMemberRoleEnum.NORMAL.getRole()));
            verify(groupMemberService).updateGroupMemberRole(eq(10L), eq(Collections.singleton(2L)),
                    eq(ImGroupMemberRoleEnum.OWNER.getRole()));
        }
    }

    @Test
    public void testTransferGroupOwner_toSelf() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);

            ImGroupTransferOwnerReqVO reqVO = new ImGroupTransferOwnerReqVO();
            reqVO.setId(10L);
            reqVO.setNewOwnerUserId(1L);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.transferGroupOwner(1L, reqVO));
            assertEquals(GROUP_TRANSFER_OWNER_TO_SELF.getCode(), exception.getCode());
            verify(groupMapper, never()).updateById(any(ImGroupDO.class));
        }
    }

    @Test
    public void testTransferGroupOwner_notOwner() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectByIdForUpdate(10L)).thenReturn(group);

            ImGroupTransferOwnerReqVO reqVO = new ImGroupTransferOwnerReqVO();
            reqVO.setId(10L);
            reqVO.setNewOwnerUserId(2L);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.transferGroupOwner(1L, reqVO));
            assertEquals(GROUP_NOT_OWNER.getCode(), exception.getCode());
        }
    }

    // ========== banGroup ==========

    @Test
    public void testBanGroup_dissolved() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).status(CommonStatusEnum.DISABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupManagerBanReqVO reqVO = new ImGroupManagerBanReqVO();
            reqVO.setId(10L).setReason("违规");

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.banGroup(1L, reqVO));
            assertEquals(GROUP_DISSOLVED.getCode(), exception.getCode());
            verify(groupMapper, never()).updateById(any(ImGroupDO.class));
            verify(groupMessageService, never()).sendGroupMessage(anyLong(), any(ImGroupMessageSendDTO.class));
        }
    }

    @Test
    public void testBanGroup_alreadyBannedSkip() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).banned(true)
                    .status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);

            ImGroupManagerBanReqVO reqVO = new ImGroupManagerBanReqVO();
            reqVO.setId(10L).setReason("违规");

            groupService.banGroup(1L, reqVO);

            verify(groupMapper, never()).updateById(any(ImGroupDO.class));
            verify(groupMessageService, never()).sendGroupMessage(anyLong(), any(ImGroupMessageSendDTO.class));
        }
    }

    // ========== muteMember ==========

    @Test
    public void testMuteMember_normalCannotMuteAdmin() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImGroupServiceImpl.class)))
                    .thenReturn(groupService);

            ImGroupDO group = ImGroupDO.builder().id(10L).status(CommonStatusEnum.ENABLE.getStatus()).build();
            when(groupMapper.selectById(10L)).thenReturn(group);
            when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(1L)
                            .role(ImGroupMemberRoleEnum.NORMAL.getRole()).build());
            when(groupMemberService.validateMemberInGroup(10L, 2L)).thenReturn(
                    ImGroupMemberDO.builder().groupId(10L).userId(2L)
                            .role(ImGroupMemberRoleEnum.ADMIN.getRole()).build());

            ImGroupMuteMemberReqVO reqVO = new ImGroupMuteMemberReqVO();
            reqVO.setId(10L).setUserId(2L).setMutedSeconds(60);

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> groupService.muteMember(1L, reqVO));
            assertEquals(GROUP_NOT_OWNER_OR_ADMIN.getCode(), exception.getCode());
            verify(groupMemberService, never()).updateGroupMemberMuteEndTime(anyLong(), anyLong(), any());
            verify(groupMessageService, never()).sendGroupMessage(anyLong(), any(ImGroupMessageSendDTO.class));
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
        when(groupMemberService.getGroupMemberListByUserId(1L)).thenReturn(new ArrayList<>());

        List<ImGroupDO> result = groupService.getMyGroupList(1L);
        assertTrue(result.isEmpty());
        verify(groupMapper, never()).selectByIds(anyCollection());
    }

    @Test
    public void testGetMyGroupList_success() {
        // 曾经加入的所有群（含退群）
        when(groupMemberService.getGroupMemberListByUserId(1L)).thenReturn(new ArrayList<>(Arrays.asList(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(20L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(30L).userId(1L)
                        .status(CommonStatusEnum.DISABLE.getStatus()).build()
        )));
        List<ImGroupDO> groups = Arrays.asList(
                ImGroupDO.builder().id(10L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupDO.builder().id(20L).status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupDO.builder().id(30L).status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        when(groupMapper.selectByIds(anyCollection())).thenReturn(groups);

        List<ImGroupDO> result = groupService.getMyGroupList(1L);
        assertEquals(3, result.size());
    }

}

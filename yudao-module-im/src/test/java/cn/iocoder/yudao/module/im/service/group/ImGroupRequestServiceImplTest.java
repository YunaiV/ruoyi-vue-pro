package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupRequestMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM 加群申请 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImGroupRequestServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImGroupRequestServiceImpl groupRequestService;

    @Mock
    private ImGroupRequestMapper groupRequestMapper;
    @Mock
    private ImGroupService groupService;
    @Mock
    private ImGroupMemberService groupMemberService;
    @Mock
    private ImGroupMessageService groupMessageService;
    @Mock
    private ImWebSocketService websocketService;
    @Mock
    private AdminUserApi adminUserApi;

    // ==================== applyJoinGroup ====================

    @Test
    public void testApplyJoinGroup_freeMode_directJoin() {
        // 准备：群是 FREE 模式
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(false)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);

        ImGroupRequestApplyReqVO reqVO = new ImGroupRequestApplyReqVO();
        reqVO.setGroupId(10L);
        reqVO.setAddSource(ImGroupAddSourceEnum.SEARCH.getSource());

        // 调用
        ImGroupRequestDO result = groupRequestService.applyJoinGroup(1L, reqVO);

        // 断言：FREE 路径直接入群，不落申请记录
        assertNull(result);
        verify(groupService).validateMemberCountLimit(10L, 1);
        verify(groupMemberService).addGroupMember(eq(10L), eq(1L),
                eq(ImGroupMemberRoleEnum.NORMAL.getRole()),
                eq(ImGroupAddSourceEnum.SEARCH.getSource()), isNull());
        verify(groupRequestMapper, never()).insert(any(ImGroupRequestDO.class));
        // 推 1510 自由进群
        ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
        verify(groupMessageService).sendGroupMessage(eq(1L), dtoCaptor.capture());
        assertEquals(ImMessageTypeEnum.GROUP_MEMBER_ENTER.getType(), dtoCaptor.getValue().getType());
    }

    @Test
    public void testApplyJoinGroup_approvalMode_createsRequest() {
        // 准备：群是 APPLY 模式
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(true)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        // 群里有 owner + 一个 admin，作为 1503 推送目标
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(99L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(98L)
                        .role(ImGroupMemberRoleEnum.ADMIN.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));
        when(adminUserApi.getUser(1L)).thenReturn(buildUser(1L, "申请人"));

        ImGroupRequestApplyReqVO reqVO = new ImGroupRequestApplyReqVO();
        reqVO.setGroupId(10L);
        reqVO.setApplyContent("我想进群");
        reqVO.setAddSource(ImGroupAddSourceEnum.SEARCH.getSource());

        // 调用
        ImGroupRequestDO result = groupRequestService.applyJoinGroup(1L, reqVO);

        // 断言：申请记录已落库 + 不直进群
        assertNotNull(result);
        verify(groupMemberService, never()).addGroupMember(anyLong(), anyLong(), anyInt(), anyInt(), anyLong());
        verify(groupRequestMapper).insert(any(ImGroupRequestDO.class));
        // 1503 推送给 owner(99) + admin(98)，去重后两条
        verify(websocketService, times(2)).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testApplyJoinGroup_insertDuplicateKey_reuseOldRequest() {
        // 准备：群是 APPLY 模式，首次查询不存在，插入时命中唯一键
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(true)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(99L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));
        when(adminUserApi.getUser(1L)).thenReturn(buildUser(1L, "申请人"));
        ImGroupRequestDO old = new ImGroupRequestDO().setId(50L).setGroupId(10L).setUserId(1L)
                .setHandleResult(ImGroupRequestHandleResultEnum.REFUSED.getResult());
        when(groupRequestMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(null, old);
        when(groupRequestMapper.insert(any(ImGroupRequestDO.class))).thenThrow(new DuplicateKeyException("dup"));

        ImGroupRequestApplyReqVO reqVO = new ImGroupRequestApplyReqVO();
        reqVO.setGroupId(10L);
        reqVO.setApplyContent("我想进群");
        reqVO.setAddSource(ImGroupAddSourceEnum.SEARCH.getSource());

        // 调用
        ImGroupRequestDO result = groupRequestService.applyJoinGroup(1L, reqVO);

        // 断言：复用并重置旧申请
        verify(groupRequestMapper).updateApplyByIdReset(eq(50L), eq("我想进群"),
                eq(ImGroupAddSourceEnum.SEARCH.getSource()), any());
        assertEquals(50L, result.getId());
        assertEquals(ImGroupRequestHandleResultEnum.UNHANDLED.getResult(), result.getHandleResult());
        verify(websocketService).sendPrivateMessageAsync(eq(99L), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testApplyJoinGroup_alreadyMember_throws() {
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(true)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        when(groupMemberService.getGroupMember(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());

        ImGroupRequestApplyReqVO reqVO = new ImGroupRequestApplyReqVO();
        reqVO.setGroupId(10L);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupRequestService.applyJoinGroup(1L, reqVO));
        assertEquals(GROUP_REQUEST_ALREADY_MEMBER.getCode(), exception.getCode());
    }

    // ==================== agreeGroupRequest ====================

    @Test
    public void testAgreeGroupRequest_success_activeApply() {
        // 准备：主动申请未处理，操作人是 admin
        ImGroupRequestDO request = new ImGroupRequestDO()
                .setGroupId(10L).setUserId(2L).setInviterUserId(null)
                .setAddSource(ImGroupAddSourceEnum.SEARCH.getSource())
                .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
        request.setId(50L);
        when(groupRequestMapper.selectById(50L)).thenReturn(request);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.ADMIN.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        when(groupRequestMapper.updateByIdAndHandleResult(eq(50L),
                eq(ImGroupRequestHandleResultEnum.UNHANDLED.getResult()), any())).thenReturn(1);

        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.getGroup(10L)).thenReturn(group);
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(99L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        // 调用
        groupRequestService.agreeGroupRequest(1L, 50L);

        // 断言：人数校验 + 写群成员 + 推 1505 + 推 1510（主动申请）
        verify(groupService).validateMemberCountLimit(10L, 1);
        verify(groupMemberService).addGroupMember(eq(10L), eq(2L),
                eq(ImGroupMemberRoleEnum.NORMAL.getRole()),
                eq(ImGroupAddSourceEnum.SEARCH.getSource()), isNull());
        ArgumentCaptor<ImGroupMessageSendDTO> dtoCaptor = ArgumentCaptor.forClass(ImGroupMessageSendDTO.class);
        verify(groupMessageService).sendGroupMessage(eq(1L), dtoCaptor.capture());
        assertEquals(ImMessageTypeEnum.GROUP_MEMBER_ENTER.getType(), dtoCaptor.getValue().getType());
        // 1505 推送给申请人 + owner，去重后两条
        verify(websocketService, times(2)).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    @Test
    public void testAgreeGroupRequest_concurrent_secondCallFails() {
        // 准备：申请存在但乐观锁更新返回 0（被并发处理过）
        ImGroupRequestDO request = new ImGroupRequestDO()
                .setGroupId(10L).setUserId(2L)
                .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
        request.setId(50L);
        when(groupRequestMapper.selectById(50L)).thenReturn(request);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        when(groupRequestMapper.updateByIdAndHandleResult(eq(50L),
                eq(ImGroupRequestHandleResultEnum.UNHANDLED.getResult()), any())).thenReturn(0);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupRequestService.agreeGroupRequest(1L, 50L));
        assertEquals(GROUP_REQUEST_HANDLED.getCode(), exception.getCode());
        // 不写群成员
        verify(groupMemberService, never()).addGroupMember(anyLong(), anyLong(), anyInt(), any(), any());
    }

    @Test
    public void testAgreeGroupRequest_notOwnerOrAdmin_throws() {
        ImGroupRequestDO request = new ImGroupRequestDO()
                .setGroupId(10L).setUserId(2L)
                .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
        request.setId(50L);
        when(groupRequestMapper.selectById(50L)).thenReturn(request);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.NORMAL.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupRequestService.agreeGroupRequest(1L, 50L));
        assertEquals(GROUP_REQUEST_NOT_TO_ME.getCode(), exception.getCode());
    }

    // ==================== refuseGroupRequest ====================

    @Test
    public void testRefuseGroupRequest_success() {
        ImGroupRequestDO request = new ImGroupRequestDO()
                .setGroupId(10L).setUserId(2L)
                .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
        request.setId(50L);
        when(groupRequestMapper.selectById(50L)).thenReturn(request);
        when(groupMemberService.validateMemberInGroup(10L, 1L)).thenReturn(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        when(groupRequestMapper.updateByIdAndHandleResult(eq(50L),
                eq(ImGroupRequestHandleResultEnum.UNHANDLED.getResult()), any())).thenReturn(1);

        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.getGroup(10L)).thenReturn(group);
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));

        groupRequestService.refuseGroupRequest(1L, 50L, "暂不通过");

        // 不写群成员；推 1506 给申请人 + 群主（同一人 1L 时去重为 1 + 申请人 2L = 2 条）
        verify(groupMemberService, never()).addGroupMember(anyLong(), anyLong(), anyInt(), any(), any());
        verify(websocketService, times(2)).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
    }

    // ==================== createInviteRequestList ====================

    @Test
    public void testCreateInviteRequestList_success() {
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(true)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        when(groupRequestMapper.selectByGroupIdAndUserId(eq(10L), anyLong())).thenReturn(null);
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(99L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));
        when(adminUserApi.getUserMap(anyCollection())).thenReturn(MapUtil.<Long, AdminUserRespDTO>builder()
                .put(2L, buildUser(2L, "用户A"))
                .put(3L, buildUser(3L, "用户B"))
                .build());

        // 调用：邀请人 1L 邀请 2L、3L（都没有旧记录）
        groupRequestService.createInviteRequestList(10L, 1L, ListUtil.of(2L, 3L));

        // 断言：插入 2 条 + 推 1503 给 owner（每条 1 帧）共 2 帧
        ArgumentCaptor<ImGroupRequestDO> captor = ArgumentCaptor.forClass(ImGroupRequestDO.class);
        verify(groupRequestMapper, times(2)).insert(captor.capture());
        verify(websocketService, times(2)).sendPrivateMessageAsync(anyLong(), any(ImPrivateMessageDTO.class));
        // 断言：每条记录 inviterUserId=1 + addSource=INVITE，避免审批通过后回写群成员留痕的来源为空 / 脏带旧值
        Collection<ImGroupRequestDO> inserted = captor.getAllValues();
        assertEquals(2, inserted.size());
        inserted.forEach(insert -> {
            assertEquals(1L, insert.getInviterUserId());
            assertEquals(ImGroupAddSourceEnum.INVITE.getSource(), insert.getAddSource());
        });
    }

    @Test
    public void testCreateInviteRequestList_insertDuplicateKey_reuseOldRequest() {
        ImGroupDO group = ImGroupDO.builder().id(10L).ownerUserId(99L)
                .joinApproval(true)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupService.validateGroupExists(10L)).thenReturn(group);
        ImGroupRequestDO old = new ImGroupRequestDO().setId(50L).setGroupId(10L).setUserId(2L)
                .setHandleResult(ImGroupRequestHandleResultEnum.REFUSED.getResult());
        when(groupRequestMapper.selectByGroupIdAndUserId(10L, 2L)).thenReturn(null, old);
        when(groupRequestMapper.insert(any(ImGroupRequestDO.class))).thenThrow(new DuplicateKeyException("dup"));
        when(groupMemberService.getGroupMemberListByOwnerAndAdmin(10L)).thenReturn(ListUtil.of(
                ImGroupMemberDO.builder().groupId(10L).userId(99L)
                        .role(ImGroupMemberRoleEnum.OWNER.getRole())
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()));
        when(adminUserApi.getUserMap(anyCollection())).thenReturn(MapUtil.of(2L, buildUser(2L, "用户A")));

        // 调用
        groupRequestService.createInviteRequestList(10L, 1L, ListUtil.of(2L));

        // 断言：复用并重置旧邀请申请
        verify(groupRequestMapper).updateInviteByIdReset(eq(50L), eq(1L),
                eq(ImGroupAddSourceEnum.INVITE.getSource()), any());
        verify(websocketService).sendPrivateMessageAsync(eq(99L), any(ImPrivateMessageDTO.class));
    }

    private AdminUserRespDTO buildUser(Long id, String nickname) {
        AdminUserRespDTO user = new AdminUserRespDTO();
        user.setId(id);
        user.setNickname(nickname);
        return user;
    }

}

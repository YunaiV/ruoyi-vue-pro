package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_IN_GROUP;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IM 群成员 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImGroupMemberServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImGroupMemberServiceImpl groupMemberService;

    @Mock
    private ImGroupMemberMapper groupMemberMapper;
    @Mock
    private ImWebSocketService webSocketService;

    // ========== addGroupMember ==========

    @Test
    public void testAddGroupMember_newInsert() {
        // 准备：成员记录不存在
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(null);

        // 调用
        ImGroupMemberDO result = groupMemberService.addGroupMember(10L, 1L);

        // 断言：执行了 insert，返回记录的 status 为 ENABLE
        assertNotNull(result);
        assertEquals(10L, result.getGroupId());
        assertEquals(1L, result.getUserId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), result.getStatus());
        assertNotNull(result.getJoinTime());
        verify(groupMemberMapper).insert(any(ImGroupMemberDO.class));
    }

    @Test
    public void testAddGroupMember_existingEnabledReturns() {
        // 准备：已存在且 ENABLE，只返回已有记录，不做其它操作
        ImGroupMemberDO exists = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(exists);
        when(groupMemberMapper.selectById(50L)).thenReturn(exists);

        ImGroupMemberDO result = groupMemberService.addGroupMember(10L, 1L);

        assertEquals(50L, result.getId());
        verify(groupMemberMapper, never()).insert(any(ImGroupMemberDO.class));
        verify(groupMemberMapper, never()).updateById(any(ImGroupMemberDO.class));
    }

    @Test
    public void testAddGroupMember_existingDisabledRecovers() {
        // 准备：已存在且 DISABLE，应更新为 ENABLE
        ImGroupMemberDO exists = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(exists);
        ImGroupMemberDO recovered = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectById(50L)).thenReturn(recovered);

        ImGroupMemberDO result = groupMemberService.addGroupMember(10L, 1L);

        ArgumentCaptor<ImGroupMemberDO> captor = ArgumentCaptor.forClass(ImGroupMemberDO.class);
        verify(groupMemberMapper).updateById(captor.capture());
        assertEquals(50L, captor.getValue().getId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getJoinTime());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), result.getStatus());
    }

    @Test
    public void testAddGroupMember_duplicateKeyFallsBackToSelect() {
        // 准备：第一次 select 返回 null，insert 抛 DuplicateKey，再次 select 返回已插入记录
        ImGroupMemberDO inserted = ImGroupMemberDO.builder().id(80L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(null).thenReturn(inserted);
        when(groupMemberMapper.insert(any(ImGroupMemberDO.class)))
                .thenThrow(new DuplicateKeyException("concurrent insert"));

        // 调用：冲突后降级 select
        ImGroupMemberDO result = groupMemberService.addGroupMember(10L, 1L);

        assertNotNull(result);
        assertEquals(80L, result.getId());
    }

    // ========== addGroupMembers ==========

    @Test
    public void testAddGroupMembers_mixedInsertAndUpdate() {
        // 准备：用户 2 不存在（新增），用户 3 已存在且 DISABLE（恢复），用户 4 已存在且 ENABLE（跳过）
        ImGroupMemberDO exist3 = ImGroupMemberDO.builder().id(30L).groupId(10L).userId(3L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        ImGroupMemberDO exist4 = ImGroupMemberDO.builder().id(40L).groupId(10L).userId(4L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectListByGroupIdAndUserIds(eq(10L), anyCollection()))
                .thenReturn(List.of(exist3, exist4));

        // 调用
        groupMemberService.addGroupMembers(10L, List.of(2L, 3L, 4L));

        // 断言：updates 只有用户 3；inserts 只有用户 2
        verify(groupMemberMapper).updateBatch(argThat((List<ImGroupMemberDO> list) ->
                list.size() == 1 && list.get(0).getId().equals(30L)
                        && CommonStatusEnum.ENABLE.getStatus().equals(list.get(0).getStatus())));
        verify(groupMemberMapper).insertBatch(argThat((List<ImGroupMemberDO> list) ->
                list.size() == 1 && list.get(0).getUserId().equals(2L)));
    }

    @Test
    public void testAddGroupMembers_allExisting_onlyUpdates() {
        // 准备：传入的 3 个用户都已有记录（全部 DISABLE） → 只做 update，不做 insert
        List<ImGroupMemberDO> existing = List.of(
                ImGroupMemberDO.builder().id(1L).groupId(10L).userId(2L)
                        .status(CommonStatusEnum.DISABLE.getStatus()).build(),
                ImGroupMemberDO.builder().id(2L).groupId(10L).userId(3L)
                        .status(CommonStatusEnum.DISABLE.getStatus()).build()
        );
        when(groupMemberMapper.selectListByGroupIdAndUserIds(eq(10L), anyCollection()))
                .thenReturn(existing);

        groupMemberService.addGroupMembers(10L, List.of(2L, 3L));

        verify(groupMemberMapper).updateBatch(anyList());
        verify(groupMemberMapper, never()).insertBatch(anyList());
    }

    @Test
    public void testAddGroupMembers_allNew_onlyInserts() {
        // 准备：都不存在 → 只做 insert
        when(groupMemberMapper.selectListByGroupIdAndUserIds(eq(10L), anyCollection()))
                .thenReturn(List.of());

        groupMemberService.addGroupMembers(10L, List.of(2L, 3L));

        verify(groupMemberMapper, never()).updateBatch(anyList());
        verify(groupMemberMapper).insertBatch(anyList());
    }

    @Test
    public void testAddGroupMembers_allExistingEnabled_nothingHappens() {
        // 准备：都已存在且 ENABLE → 既不 update 也不 insert
        List<ImGroupMemberDO> existing = List.of(
                ImGroupMemberDO.builder().id(1L).groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        when(groupMemberMapper.selectListByGroupIdAndUserIds(eq(10L), anyCollection()))
                .thenReturn(existing);

        groupMemberService.addGroupMembers(10L, List.of(2L));

        verify(groupMemberMapper, never()).updateBatch(anyList());
        verify(groupMemberMapper, never()).insertBatch(anyList());
    }

    @Test
    public void testAddGroupMembers_batchInsertDuplicateFallback() {
        // 准备：两个新增成员，批量插入失败时降级为逐个 addGroupMember
        when(groupMemberMapper.selectListByGroupIdAndUserIds(eq(10L), anyCollection()))
                .thenReturn(List.of());
        doThrow(new DuplicateKeyException("concurrent batch insert"))
                .when(groupMemberMapper).insertBatch(anyList());
        // addGroupMember 单条兜底逻辑
        when(groupMemberMapper.selectByGroupIdAndUserId(eq(10L), anyLong())).thenReturn(null);

        // 调用
        groupMemberService.addGroupMembers(10L, List.of(2L, 3L));

        // 断言：降级为逐条调用 insert
        verify(groupMemberMapper, times(2)).insert(any(ImGroupMemberDO.class));
    }

    // ========== validateMemberInGroup ==========

    @Test
    public void testValidateMemberInGroup_notInGroup() {
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMemberService.validateMemberInGroup(10L, 1L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    @Test
    public void testValidateMemberInGroup_disabledEqualsNotInGroup() {
        ImGroupMemberDO member = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(member);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMemberService.validateMemberInGroup(10L, 1L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    @Test
    public void testValidateMemberInGroup_success() {
        ImGroupMemberDO member = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(member);

        ImGroupMemberDO result = groupMemberService.validateMemberInGroup(10L, 1L);
        assertEquals(50L, result.getId());
    }

    // ========== updateGroupMember ==========

    @Test
    public void testUpdateGroupMember_success() {
        ImGroupMemberDO member = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(member);

        ImGroupMemberUpdateReqVO reqVO = new ImGroupMemberUpdateReqVO()
                .setGroupId(10L).setMuted(true).setDisplayUserName("昵称");

        groupMemberService.updateGroupMember(1L, reqVO);

        ArgumentCaptor<ImGroupMemberDO> captor = ArgumentCaptor.forClass(ImGroupMemberDO.class);
        verify(groupMemberMapper).updateById(captor.capture());
        assertEquals(50L, captor.getValue().getId());
        assertTrue(captor.getValue().getMuted());
        verify(webSocketService).sendGroupMessageAsync(eq(1L), any(ImGroupMessageDTO.class));
    }

    // ========== removeGroupMember ==========

    @Test
    public void testRemoveGroupMember_success() {
        ImGroupMemberDO member = ImGroupMemberDO.builder().id(50L).groupId(10L).userId(1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(member);

        groupMemberService.removeGroupMember(10L, 1L);

        ArgumentCaptor<ImGroupMemberDO> captor = ArgumentCaptor.forClass(ImGroupMemberDO.class);
        verify(groupMemberMapper).updateById(captor.capture());
        assertEquals(50L, captor.getValue().getId());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getQuitTime());
    }

    @Test
    public void testRemoveGroupMember_notInGroup() {
        when(groupMemberMapper.selectByGroupIdAndUserId(10L, 1L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> groupMemberService.removeGroupMember(10L, 1L));
        assertEquals(GROUP_MEMBER_NOT_IN_GROUP.getCode(), exception.getCode());
    }

    // ========== removeGroupMembers ==========

    @Test
    public void testRemoveGroupMembers_batch() {
        groupMemberService.removeGroupMembers(10L, List.of(2L, 3L));

        ArgumentCaptor<ImGroupMemberDO> captor = ArgumentCaptor.forClass(ImGroupMemberDO.class);
        verify(groupMemberMapper).updateByGroupIdAndUserIdsAndStatus(eq(10L), anyCollection(),
                eq(CommonStatusEnum.ENABLE.getStatus()), captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getQuitTime());
    }

    @Test
    public void testRemoveGroupMembersByGroupId() {
        groupMemberService.removeGroupMembersByGroupId(10L);

        ArgumentCaptor<ImGroupMemberDO> captor = ArgumentCaptor.forClass(ImGroupMemberDO.class);
        verify(groupMemberMapper).updateByGroupIdAndStatus(eq(10L),
                eq(CommonStatusEnum.ENABLE.getStatus()), captor.capture());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), captor.getValue().getStatus());
        assertNotNull(captor.getValue().getQuitTime());
    }

    // ========== getActiveGroupMemberUserIdsByGroupId ==========

    @Test
    public void testGetActiveGroupMemberUserIdsByGroupId_extractsUserIds() {
        // 准备：3 个 ENABLE 成员
        List<ImGroupMemberDO> members = List.of(
                ImGroupMemberDO.builder().groupId(10L).userId(1L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(2L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                ImGroupMemberDO.builder().groupId(10L).userId(3L)
                        .status(CommonStatusEnum.ENABLE.getStatus()).build()
        );
        when(groupMemberMapper.selectListByGroupIdAndStatus(
                10L, CommonStatusEnum.ENABLE.getStatus())).thenReturn(members);

        // 调用
        List<Long> userIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L);

        // 断言：只返回 userId、顺序保留
        assertEquals(List.of(1L, 2L, 3L), userIds);
    }

    @Test
    public void testGetActiveGroupMemberUserIdsByGroupId_emptyList() {
        when(groupMemberMapper.selectListByGroupIdAndStatus(
                10L, CommonStatusEnum.ENABLE.getStatus())).thenReturn(List.of());

        List<Long> userIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(10L);

        assertTrue(userIds.isEmpty());
    }

}

package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportExcelVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemIterationUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemNameUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPlanningSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemCommentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemMemberDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemBoardMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemCommentMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemMemberMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemStatusMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemUserSortMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemWorkLogMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemDefectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemLifecycleStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_DEFECT_TYPE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_DELETE_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_PARENT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_SORT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_TYPE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.MessageTemplateConstants.WORK_ITEM_ASSIGNED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.WORK_ITEM_CREATED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.WORK_ITEM_NAME_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsWorkItemServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsWorkItemServiceImpl.class, PmsWorkItemUserSortServiceImpl.class, PmsWorkItemStatusServiceImpl.class,
        PmsWorkItemBoardServiceImpl.class,
        PmsWorkItemCommentServiceImpl.class, PmsWorkItemWorkLogServiceImpl.class})
public class PmsWorkItemServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemServiceImpl workItemService;

    @Resource
    private PmsWorkItemMapper workItemMapper;
    @Resource
    private PmsWorkItemStatusMapper workItemStatusMapper;
    @Resource
    private PmsWorkItemBoardMapper workItemBoardMapper;
    @Resource
    private PmsWorkItemMemberMapper workItemMemberMapper;
    @Resource
    private PmsWorkItemCommentMapper workItemCommentMapper;
    @Resource
    private PmsWorkItemWorkLogMapper workItemWorkLogMapper;
    @Resource
    private PmsWorkItemUserSortMapper workItemUserSortMapper;

    @MockitoBean
    private PmsProjectMemberService projectMemberService;
    @MockitoBean
    private PmsIterationService iterationService;
    @MockitoBean
    private PmsWorkItemLabelService workItemLabelService;
    @MockitoBean
    private PmsWorkItemActivityService workItemActivityService;
    @Resource
    private PmsWorkItemStatusServiceImpl workItemStatusService;

    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @Test
    public void testCreateWorkItem_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType());
        when(projectMemberService.validateProjectWritable(projectId, userId)).thenReturn(project);
        workItemStatusService.initProjectWorkItemStatuses(projectId, project.getType());
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(projectId, PmsWorkItemTypeEnum.TASK.getType());
        reqVO.setAssigneeUserId(randomLongId()).setMemberUserIds(Arrays.asList(randomLongId(), randomLongId()))
                .setFileUrls(Arrays.asList("https://example.com/a.png", "https://example.com/b.pdf"));

        // 调用
        Long workItemId = workItemService.createWorkItem(reqVO, userId);

        // 断言
        PmsWorkItemDO workItem = workItemMapper.selectById(workItemId);
        assertNotNull(workItem);
        assertEquals(reqVO.getName(), workItem.getName());
        assertEquals(1, workItem.getSerialNumber());
        assertEquals(PmsWorkItemStatusTypeEnum.PENDING.getType(), workItem.getStatus());
        assertEquals(reqVO.getFileUrls(), workItem.getFileUrls());
        assertEquals(2, workItemMemberMapper.selectListByWorkItemIds(Collections.singleton(workItemId)).size());
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
        verify(projectMemberService).validateProjectMemberList(eq(projectId), anySet());
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor = ArgumentCaptor
                .forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(captor.capture());
        assertEquals(reqVO.getAssigneeUserId(), captor.getValue().getUserId());
        assertEquals(WORK_ITEM_ASSIGNED, captor.getValue().getTemplateCode());
        assertEquals(project.getName(), captor.getValue().getTemplateParams().get("projectName"));
        assertEquals("/pms/pm/project/detail/" + projectId + "?tabs=task",
                captor.getValue().getTemplateParams().get("route"));
    }

    @Test
    public void testCreateWorkItem_withChildrenAndWorkLog() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(projectId, PmsWorkItemTypeEnum.TASK.getType())
                .setChildWorkItemNames(Arrays.asList("接口联调", "页面联调"))
                .setActualHours(2).setRemainingHours(6);

        // 调用
        Long workItemId = workItemService.createWorkItem(reqVO, userId);

        // 断言
        PmsWorkItemDO parent = workItemMapper.selectById(workItemId);
        List<PmsWorkItemDO> children = workItemMapper.selectList(new PmsWorkItemPageReqVO()
                .setProjectId(projectId).setParentId(workItemId));
        assertEquals(2, children.size());
        assertTrue(children.stream().allMatch(child -> child.getStatusId().equals(parent.getStatusId())));
        List<PmsWorkItemWorkLogDO> workLogs = workItemWorkLogMapper.selectListByWorkItemId(workItemId);
        assertEquals(1, workLogs.size());
        assertEquals(2, workLogs.get(0).getActualHours());
        assertEquals(6, workLogs.get(0).getRemainingHours());
    }

    @Test
    public void testCreateWorkItem_generalProjectTypeInvalid() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.GENERAL.getType()));
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(projectId, PmsWorkItemTypeEnum.REQUIREMENT.getType());

        // 调用，并断言异常
        assertServiceException(() -> workItemService.createWorkItem(reqVO, userId), WORK_ITEM_TYPE_INVALID);
    }

    @Test
    public void testCreateWorkItem_defectTypeRequired() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(projectId, PmsWorkItemTypeEnum.DEFECT.getType());

        // 调用，并断言异常
        assertServiceException(() -> workItemService.createWorkItem(reqVO, userId), WORK_ITEM_DEFECT_TYPE_INVALID);
    }

    @Test
    public void testUpdateWorkItem_parentCycle() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO parent = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(parent);
        PmsWorkItemDO child = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setParentId(parent.getId());
        workItemMapper.insert(child);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(parent);
        reqVO.setParentId(child.getId());

        // 调用，并断言异常
        assertServiceException(() -> workItemService.updateWorkItem(reqVO, userId), WORK_ITEM_PARENT_INVALID);
    }

    @Test
    public void testUpdateWorkItem_clearOptionalFields() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO parent = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(parent);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setDescription("旧描述").setAssigneeUserId(randomLongId()).setParentId(parent.getId())
                .setStartTime(LocalDateTime.now()).setEndTime(LocalDateTime.now().plusDays(1))
                .setEstimatedHours(8).setFileUrls(Collections.singletonList("https://example.com/a.png"));
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(workItem);
        reqVO.setDescription(null).setAssigneeUserId(null).setParentId(null)
                .setStartTime(null).setEndTime(null).setEstimatedHours(null).setFileUrls(null);

        // 调用
        workItemService.updateWorkItem(reqVO, userId);

        // 断言
        PmsWorkItemDO updatedWorkItem = workItemMapper.selectById(workItem.getId());
        assertNull(updatedWorkItem.getDescription());
        assertNull(updatedWorkItem.getAssigneeUserId());
        assertNull(updatedWorkItem.getParentId());
        assertNull(updatedWorkItem.getStartTime());
        assertNull(updatedWorkItem.getEndTime());
        assertNull(updatedWorkItem.getEstimatedHours());
        assertNull(updatedWorkItem.getFileUrls());
    }

    @Test
    public void testUpdateWorkItem_assigneeChangedNotify() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        Long oldAssigneeUserId = randomLongId();
        Long newAssigneeUserId = randomLongId();
        PmsProjectDO project = randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType());
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1)
                .setAssigneeUserId(oldAssigneeUserId);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId)).thenReturn(project);
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(workItem).setAssigneeUserId(newAssigneeUserId);

        // 调用
        workItemService.updateWorkItem(reqVO, userId);

        // 断言
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor = ArgumentCaptor
                .forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(captor.capture());
        assertEquals(newAssigneeUserId, captor.getValue().getUserId());
        assertEquals(reqVO.getName(), captor.getValue().getTemplateParams().get("workItemName"));
    }

    @Test
    public void testUpdateWorkItemName_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        PmsWorkItemNameUpdateReqVO reqVO = new PmsWorkItemNameUpdateReqVO().setId(workItem.getId())
                .setName("  子任务新名称  ");

        // 调用
        workItemService.updateWorkItemName(reqVO, userId);

        // 断言
        assertEquals("子任务新名称", workItemMapper.selectById(workItem.getId()).getName());
        verify(workItemActivityService).createWorkItemActivity(
                projectId, workItem.getId(), userId, WORK_ITEM_NAME_UPDATED,
                workItem.getName(), "子任务新名称");
    }

    @Test
    public void testUpdateWorkItem_createFieldActivitiesDelegates() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        Long oldAssigneeUserId = randomLongId();
        Long newAssigneeUserId = randomLongId();
        Long oldMemberUserId = randomLongId();
        Long newMemberUserId = randomLongId();
        Long oldLabelId = randomLongId();
        Long newLabelId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1)
                .setDescription("旧描述").setAssigneeUserId(oldAssigneeUserId)
                .setFileUrls(Collections.singletonList("https://example.com/old.png"))
                .setLabelIds(Collections.singletonList(oldLabelId));
        workItemMapper.insert(workItem);
        workItemMemberMapper.insert(new PmsWorkItemMemberDO().setProjectId(projectId)
                .setWorkItemId(workItem.getId()).setUserId(oldMemberUserId));
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        Map<Long, AdminUserRespDTO> userMap = new HashMap<>();
        userMap.put(oldAssigneeUserId, new AdminUserRespDTO().setId(oldAssigneeUserId).setNickname("旧负责人"));
        userMap.put(newAssigneeUserId, new AdminUserRespDTO().setId(newAssigneeUserId).setNickname("新负责人"));
        userMap.put(oldMemberUserId, new AdminUserRespDTO().setId(oldMemberUserId).setNickname("旧参与人"));
        userMap.put(newMemberUserId, new AdminUserRespDTO().setId(newMemberUserId).setNickname("新参与人"));
        when(adminUserApi.getUserMap(anySet())).thenReturn(userMap);
        Map<Long, PmsWorkItemLabelDO> labelMap = new HashMap<>();
        labelMap.put(oldLabelId, new PmsWorkItemLabelDO().setId(oldLabelId).setName("旧标签"));
        labelMap.put(newLabelId, new PmsWorkItemLabelDO().setId(newLabelId).setName("新标签"));
        when(workItemLabelService.getWorkItemLabelMap(anySet())).thenReturn(labelMap);
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(workItem).setDescription("新描述")
                .setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority()).setAssigneeUserId(newAssigneeUserId)
                .setMemberUserIds(Collections.singletonList(newMemberUserId))
                .setFileUrls(Collections.singletonList("https://example.com/new.pdf"))
                .setLabelIds(Collections.singletonList(newLabelId));

        // 调用
        workItemService.updateWorkItem(reqVO, userId);

        // 断言
        verify(workItemActivityService).createWorkItemUpdateActivities(eq(workItem), any(PmsWorkItemDO.class),
                anyCollection(), anyCollection(), eq(userId));
    }

    @Test
    public void testUpdateWorkItemStatus_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO pendingStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(pendingStatus);
        PmsWorkItemStatusDO completedStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.COMPLETED.getType(), false, 2);
        workItemStatusMapper.insert(completedStatus);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), pendingStatus, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemStatusUpdateReqVO reqVO = new PmsWorkItemStatusUpdateReqVO().setId(workItem.getId())
                .setStatusId(completedStatus.getId());

        // 调用
        workItemService.updateWorkItemStatus(reqVO, userId);

        // 断言
        PmsWorkItemDO updatedWorkItem = workItemMapper.selectById(workItem.getId());
        assertEquals(completedStatus.getId(), updatedWorkItem.getStatusId());
        assertEquals(PmsWorkItemStatusTypeEnum.COMPLETED.getType(), updatedWorkItem.getStatus());
        assertEquals(0, updatedWorkItem.getProgress());
    }

    @Test
    public void testUpdateWorkItemIteration_planAndUnplan() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        Long iterationId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        when(iterationService.getIteration(iterationId, userId))
                .thenReturn(new PmsIterationDO().setId(iterationId).setProjectId(projectId));

        // 调用：规划到迭代
        workItemService.updateWorkItemIteration(new PmsWorkItemIterationUpdateReqVO()
                .setId(workItem.getId()).setIterationId(iterationId), userId);

        // 断言
        assertEquals(iterationId, workItemMapper.selectById(workItem.getId()).getIterationId());

        // 调用：移回待规划，并断言
        workItemService.updateWorkItemIteration(new PmsWorkItemIterationUpdateReqVO()
                .setId(workItem.getId()), userId);
        assertNull(workItemMapper.selectById(workItem.getId()).getIterationId());
    }

    @Test
    public void testCreateWorkItem_emptyMembersDefaultsToCreator() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        // 准备参数
        PmsWorkItemSaveReqVO reqVO = randomSaveReqVO(projectId, PmsWorkItemTypeEnum.TASK.getType());

        // 调用
        Long workItemId = workItemService.createWorkItem(reqVO, userId);

        // 断言
        List<PmsWorkItemMemberDO> members = workItemMemberMapper
                .selectListByWorkItemIds(Collections.singleton(workItemId));
        assertEquals(1, members.size());
        assertEquals(userId, CollUtil.getFirst(members).getUserId());
    }

    @Test
    public void testUpdateWorkItemSort_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO first = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(first);
        PmsWorkItemDO second = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2);
        workItemMapper.insert(second);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemSortReqVO reqVO = new PmsWorkItemSortReqVO().setStatusId(status.getId())
                .setWorkItemIds(Arrays.asList(second.getId(), first.getId()));

        // 调用
        workItemService.updateWorkItemSort(reqVO, userId);

        // 断言
        assertEquals(1, workItemMapper.selectById(second.getId()).getSort());
        assertEquals(2, workItemMapper.selectById(first.getId()).getSort());
    }

    @Test
    public void testUpdateWorkItemSort_duplicateId() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemSortReqVO reqVO = new PmsWorkItemSortReqVO().setStatusId(status.getId())
                .setWorkItemIds(Arrays.asList(workItem.getId(), workItem.getId()));

        // 调用，并断言异常
        assertServiceException(() -> workItemService.updateWorkItemSort(reqVO, userId), WORK_ITEM_SORT_INVALID);
    }

    @Test
    public void testUpdateWorkItemPlanningSort_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO taskStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(taskStatus);
        PmsWorkItemStatusDO defectStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.DEFECT.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(defectStatus);
        PmsWorkItemDO first = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), taskStatus, 1);
        workItemMapper.insert(first);
        PmsWorkItemDO second = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.DEFECT.getType(), defectStatus, 2);
        workItemMapper.insert(second);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPlanningSortReqVO reqVO = new PmsWorkItemPlanningSortReqVO().setProjectId(projectId)
                .setWorkItemIds(Arrays.asList(second.getId(), first.getId()));

        // 调用
        workItemService.updateWorkItemPlanningSort(reqVO, userId);
        workItemService.updateWorkItemPlanningSort(reqVO, userId);

        // 断言：公共顺序不变，个人顺序按当前用户单独保存
        assertEquals(1, workItemMapper.selectById(first.getId()).getSort());
        assertEquals(2, workItemMapper.selectById(second.getId()).getSort());
        List<PmsWorkItemUserSortDO> userSorts = workItemUserSortMapper
                .selectListByProjectIdAndUserId(projectId, userId);
        assertEquals(Arrays.asList(second.getId(), first.getId()),
                convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));

        // 断言：当前用户和其他用户读取到的 Backlog 顺序互不影响
        Long otherUserId = randomLongId();
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        when(projectMemberService.validateProjectReadable(projectId, otherUserId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        PmsWorkItemPageReqVO pageReqVO = new PmsWorkItemPageReqVO().setProjectId(projectId)
                .setPlanningOnly(true).setUnplannedOnly(true);
        assertEquals(Arrays.asList(second.getId(), first.getId()),
                convertList(workItemService.getWorkItemPage(pageReqVO, userId).getList(), PmsWorkItemDO::getId));
        assertEquals(Arrays.asList(first.getId(), second.getId()),
                convertList(workItemService.getWorkItemPage(pageReqVO, otherUserId).getList(), PmsWorkItemDO::getId));
    }

    @Test
    public void testDeleteWorkItem_requirementCleanup() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO requirementStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.REQUIREMENT.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(requirementStatus);
        PmsWorkItemStatusDO defectStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.DEFECT.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(defectStatus);
        PmsWorkItemDO requirement = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.REQUIREMENT.getType(),
                requirementStatus, 1).setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.RECYCLED.getStatus());
        workItemMapper.insert(requirement);
        PmsWorkItemDO defect = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.DEFECT.getType(), defectStatus, 2)
                .setRelatedRequirementId(requirement.getId()).setDefectType(PmsWorkItemDefectTypeEnum.CODE.getType());
        workItemMapper.insert(defect);
        workItemMemberMapper.insert(new PmsWorkItemMemberDO().setProjectId(projectId)
                .setWorkItemId(requirement.getId()).setUserId(randomLongId()));
        PmsWorkItemWorkLogDO workLog = new PmsWorkItemWorkLogDO().setProjectId(projectId)
                .setWorkItemId(requirement.getId()).setActualHours(2);
        workItemWorkLogMapper.insert(workLog);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));

        // 调用
        workItemService.deleteWorkItem(requirement.getId(), userId);

        // 断言
        assertNull(workItemMapper.selectById(requirement.getId()));
        assertNull(workItemMapper.selectById(defect.getId()).getRelatedRequirementId());
        assertEquals(0, workItemMemberMapper.selectListByWorkItemIds(
                Collections.singleton(requirement.getId())).size());
        assertNull(workItemWorkLogMapper.selectById(workLog.getId()));
    }

    @Test
    public void testArchiveAndRestoreWorkItem_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));

        // 调用：归档
        workItemService.archiveWorkItem(workItem.getId(), userId);

        // 断言
        PmsWorkItemDO archivedWorkItem = workItemMapper.selectById(workItem.getId());
        assertEquals(PmsWorkItemLifecycleStatusEnum.ARCHIVED.getStatus(), archivedWorkItem.getLifecycleStatus());
        assertNotNull(archivedWorkItem.getArchiveTime());

        // 调用：恢复，并断言
        workItemService.restoreWorkItem(workItem.getId(), userId);
        PmsWorkItemDO restoredWorkItem = workItemMapper.selectById(workItem.getId());
        assertEquals(PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus(), restoredWorkItem.getLifecycleStatus());
        assertNull(restoredWorkItem.getArchiveTime());
        assertNull(restoredWorkItem.getRecycleTime());
    }

    @Test
    public void testRecycleAndDeleteWorkItem_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));

        // 调用：移入回收站
        workItemService.recycleWorkItem(workItem.getId(), userId);

        // 断言
        PmsWorkItemDO recycledWorkItem = workItemMapper.selectById(workItem.getId());
        assertEquals(PmsWorkItemLifecycleStatusEnum.RECYCLED.getStatus(), recycledWorkItem.getLifecycleStatus());
        assertNotNull(recycledWorkItem.getRecycleTime());

        // 调用：彻底删除，并断言
        workItemService.deleteWorkItem(workItem.getId(), userId);
        assertNull(workItemMapper.selectById(workItem.getId()));
    }

    @Test
    public void testDeleteWorkItem_activeStatusInvalid() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);

        // 调用，并断言异常
        assertServiceException(() -> workItemService.deleteWorkItem(workItem.getId(), userId),
                WORK_ITEM_DELETE_STATUS_INVALID);
    }

    @Test
    public void testDeleteWorkItemListByProjectId_cleanupComments() {
        // mock 数据
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO workItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(workItem);
        PmsWorkItemCommentDO comment = new PmsWorkItemCommentDO().setWorkItemId(workItem.getId())
                .setUserId(randomLongId()).setContent("项目删除时清理评论");
        workItemCommentMapper.insert(comment);

        // 调用
        workItemService.deleteWorkItemListByProjectId(projectId);

        // 断言
        assertNull(workItemMapper.selectById(workItem.getId()));
        assertNull(workItemCommentMapper.selectById(comment.getId()));
    }

    @Test
    public void testGetWorkItemBoard_keepsEmptyStatus() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO pendingStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1).setBoardName("状态1");
        workItemStatusMapper.insert(pendingStatus);
        workItemStatusMapper.insert(randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PROCESSING.getType(), false, 2).setBoardName("状态2"));
        workItemBoardMapper.insert(new PmsWorkItemBoardDO().setProjectId(projectId)
                .setWorkItemType(PmsWorkItemTypeEnum.TASK.getType()).setName("状态1").setSort(1));
        workItemBoardMapper.insert(new PmsWorkItemBoardDO().setProjectId(projectId)
                .setWorkItemType(PmsWorkItemTypeEnum.TASK.getType()).setName("状态2").setSort(2));
        PmsWorkItemDO parent = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), pendingStatus, 1);
        workItemMapper.insert(parent);
        PmsWorkItemDO child = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), pendingStatus, 2)
                .setParentId(parent.getId());
        workItemMapper.insert(child);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemBoardReqVO reqVO = new PmsWorkItemBoardReqVO();
        reqVO.setProjectId(projectId);
        reqVO.setType(PmsWorkItemTypeEnum.TASK.getType());

        // 调用
        List<PmsWorkItemBoardRespVO> board = workItemService.getWorkItemBoard(reqVO, userId);

        // 断言
        assertEquals(2, board.size());
        assertEquals(1, CollUtil.getFirst(board).getItems().size());
        assertEquals(parent.getId(), CollUtil.getFirst(CollUtil.getFirst(board).getItems()).getId());
        PmsWorkItemBoardRespVO emptyBoard = CollUtil.findOne(board, column -> "状态2".equals(column.getName()));
        assertNotNull(emptyBoard);
        assertEquals(0, emptyBoard.getItems().size());
    }

    @Test
    public void testGetWorkItemBoard_groupsStatusesByBoardName() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO developingStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PROCESSING.getType(), false, 1).setBoardName("处理中");
        workItemStatusMapper.insert(developingStatus);
        PmsWorkItemStatusDO testingStatus = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PROCESSING.getType(), false, 2).setBoardName("处理中");
        workItemStatusMapper.insert(testingStatus);
        workItemMapper.insert(randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), developingStatus, 1));
        workItemMapper.insert(randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), testingStatus, 2));
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        workItemBoardMapper.insert(new PmsWorkItemBoardDO().setProjectId(projectId)
                .setWorkItemType(PmsWorkItemTypeEnum.TASK.getType()).setName("处理中").setSort(1));

        // 调用
        PmsWorkItemBoardReqVO reqVO = new PmsWorkItemBoardReqVO();
        reqVO.setProjectId(projectId);
        reqVO.setType(PmsWorkItemTypeEnum.TASK.getType());
        List<PmsWorkItemBoardRespVO> board = workItemService.getWorkItemBoard(reqVO, userId);

        // 断言
        assertEquals(1, board.size());
        PmsWorkItemBoardRespVO processingBoard = CollUtil.findOne(board,
                column -> "处理中".equals(column.getName()));
        assertNotNull(processingBoard);
        assertEquals(2, processingBoard.getStatuses().size());
        assertEquals(2, processingBoard.getItems().size());
    }

    @Test
    public void testGetWorkItemPage_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO matched = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1)
                .setName("登录任务");
        workItemMapper.insert(matched);
        workItemMapper.insert(randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setName("其他任务"));
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId)
                .setType(PmsWorkItemTypeEnum.TASK.getType()).setName("登录");

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(matched.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetWorkItemPage_orderByPriorityAndSerialNumber() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO mediumWorkItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1)
                .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority());
        workItemMapper.insert(mediumWorkItem);
        PmsWorkItemDO highSecondWorkItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 3)
                .setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority());
        workItemMapper.insert(highSecondWorkItem);
        PmsWorkItemDO highFirstWorkItem = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority());
        workItemMapper.insert(highFirstWorkItem);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(
                new PmsWorkItemPageReqVO().setProjectId(projectId).setRootOnly(true), userId);

        // 断言
        assertEquals(Arrays.asList(highFirstWorkItem.getId(), highSecondWorkItem.getId(), mediumWorkItem.getId()),
                convertList(pageResult.getList(), PmsWorkItemDO::getId));
    }

    @Test
    public void testGetWorkItemPage_searchSerialNumber() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        workItemMapper.insert(randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1)
                .setName("其他事项"));
        PmsWorkItemDO matched = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2);
        workItemMapper.insert(matched);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId).setName("#2");

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(matched.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetWorkItemPage_rootOnly() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO parent = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(parent);
        PmsWorkItemDO child = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setParentId(parent.getId());
        workItemMapper.insert(child);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId).setRootOnly(true);

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(parent.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetProjectWorkItemStatusCountMap_success() {
        // mock 数据
        Long firstProjectId = randomLongId();
        Long secondProjectId = randomLongId();
        PmsWorkItemStatusDO pendingStatus = randomStatusDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(pendingStatus);
        PmsWorkItemStatusDO completedStatus = randomStatusDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.COMPLETED.getType(), false, 2);
        workItemStatusMapper.insert(completedStatus);
        workItemMapper.insert(randomWorkItemDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(), pendingStatus, 1));
        workItemMapper.insert(randomWorkItemDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(), pendingStatus, 2));
        workItemMapper.insert(randomWorkItemDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(), completedStatus, 3));
        PmsWorkItemDO archived = randomWorkItemDO(firstProjectId, PmsWorkItemTypeEnum.TASK.getType(),
                completedStatus, 4).setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.ARCHIVED.getStatus());
        workItemMapper.insert(archived);
        PmsWorkItemStatusDO processingStatus = randomStatusDO(secondProjectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PROCESSING.getType(), true, 1);
        workItemStatusMapper.insert(processingStatus);
        workItemMapper.insert(randomWorkItemDO(secondProjectId, PmsWorkItemTypeEnum.TASK.getType(),
                processingStatus, 1));

        // 调用
        Map<Long, Map<Integer, Long>> countMap = workItemService.getProjectWorkItemStatusCountMap(
                Arrays.asList(firstProjectId, secondProjectId));

        // 断言
        assertEquals(2L, countMap.get(firstProjectId).get(PmsWorkItemStatusTypeEnum.PENDING.getType()));
        assertEquals(1L, countMap.get(firstProjectId).get(PmsWorkItemStatusTypeEnum.COMPLETED.getType()));
        assertEquals(1L, countMap.get(secondProjectId).get(PmsWorkItemStatusTypeEnum.PROCESSING.getType()));
    }

    @Test
    public void testGetWorkItemPage_filterPriorityAndParentId() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO parent = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(parent);
        PmsWorkItemDO matched = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setParentId(parent.getId()).setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority());
        workItemMapper.insert(matched);
        PmsWorkItemDO other = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 3)
                .setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority());
        workItemMapper.insert(other);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId)
                .setPriority(PmsWorkItemPriorityEnum.HIGH.getPriority()).setParentId(parent.getId());

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(matched.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testBuildQueryWrapper_filterAnyLabel() {
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(randomLongId())
                .setLabelIds(Arrays.asList(randomLongId(), randomLongId()));

        // 调用
        String sqlSegment = PmsWorkItemMapper.buildQueryWrapper(reqVO).getSqlSegment();

        // 断言
        assertTrue(sqlSegment.contains("JSON_CONTAINS(label_ids, JSON_ARRAY("));
        assertTrue(sqlSegment.contains(" OR "));
    }

    @Test
    public void testBuildQueryWrapper_combinationFilters() {
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(randomLongId())
                .setTypes(Arrays.asList(PmsWorkItemTypeEnum.REQUIREMENT.getType(),
                        PmsWorkItemTypeEnum.TASK.getType()))
                .setStatuses(Arrays.asList(PmsWorkItemStatusTypeEnum.PENDING.getType(),
                        PmsWorkItemStatusTypeEnum.PROCESSING.getType()))
                .setPriorities(Arrays.asList(PmsWorkItemPriorityEnum.MEDIUM.getPriority(),
                        PmsWorkItemPriorityEnum.HIGH.getPriority()))
                .setIterationIds(Arrays.asList(randomLongId(), randomLongId()))
                .setExcludedIterationIds(Collections.singletonList(randomLongId()))
                .setAssigneeUserIds(Arrays.asList(randomLongId(), randomLongId()));

        // 调用
        String sqlSegment = PmsWorkItemMapper.buildQueryWrapper(reqVO).getSqlSegment();

        // 断言
        assertTrue(sqlSegment.contains("type IN"));
        assertTrue(sqlSegment.contains("status IN"));
        assertTrue(sqlSegment.contains("priority IN"));
        assertTrue(sqlSegment.contains("iteration_id IN"));
        assertTrue(sqlSegment.contains("iteration_id NOT IN"));
        assertTrue(sqlSegment.contains("iteration_id IS NULL"));
        assertTrue(sqlSegment.contains("assignee_user_id IN"));
    }

    @Test
    public void testGetWorkItemPage_filterLifecycleStatus() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        workItemMapper.insert(randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1));
        PmsWorkItemDO archived = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.ARCHIVED.getStatus());
        workItemMapper.insert(archived);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId)
                .setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.ARCHIVED.getStatus());

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(archived.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetWorkItemPage_unplannedOnly() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = randomStatusDO(projectId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), true, 1);
        workItemStatusMapper.insert(status);
        PmsWorkItemDO unplanned = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 1);
        workItemMapper.insert(unplanned);
        PmsWorkItemDO planned = randomWorkItemDO(projectId, PmsWorkItemTypeEnum.TASK.getType(), status, 2)
                .setIterationId(randomLongId());
        workItemMapper.insert(planned);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        // 准备参数
        PmsWorkItemPageReqVO reqVO = new PmsWorkItemPageReqVO().setProjectId(projectId)
                .setUnplannedOnly(true);

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workItemService.getWorkItemPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(unplanned.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testImportWorkItemList_partialSuccess() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        // 准备参数
        PmsWorkItemImportExcelVO valid = PmsWorkItemImportExcelVO.builder().name("导入任务")
                .priority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).progress(0).build();
        PmsWorkItemImportExcelVO invalid = PmsWorkItemImportExcelVO.builder()
                .priority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).progress(0).build();

        // 调用
        PmsWorkItemImportRespVO importResult = workItemService.importWorkItemList(projectId,
                PmsWorkItemTypeEnum.TASK.getType(), Arrays.asList(valid, invalid), userId);

        // 断言
        assertEquals(1, importResult.getSuccessCount());
        assertEquals(1, importResult.getFailureReasons().size());
        assertNotNull(importResult.getFailureReasons().get(3));
        assertEquals(1, workItemMapper.selectListByProjectId(projectId).size());
    }

    @Test
    public void testImportWorkItemList_failureReason() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId, PmsProjectTypeEnum.AGILE.getType()));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        doThrow(new IllegalStateException("动态记录失败")).when(workItemActivityService)
                .createWorkItemActivity(anyLong(), anyLong(), anyLong(),
                        eq(WORK_ITEM_CREATED));
        // 准备参数
        PmsWorkItemImportExcelVO importItem = PmsWorkItemImportExcelVO.builder().name("导入任务")
                .priority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).progress(0).build();

        // 调用
        PmsWorkItemImportRespVO importResult = workItemService.importWorkItemList(projectId,
                PmsWorkItemTypeEnum.TASK.getType(), Collections.singletonList(importItem), userId);

        // 断言
        assertEquals(0, importResult.getSuccessCount());
        assertEquals("动态记录失败", importResult.getFailureReasons().get(2));
        assertEquals(1, workItemMapper.selectListByProjectId(projectId).size());
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
    }

    // ========== 随机对象 ==========

    private static PmsWorkItemSaveReqVO randomSaveReqVO(Long projectId, Integer type) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return randomPojo(PmsWorkItemSaveReqVO.class, reqVO -> reqVO.setId(null).setProjectId(projectId)
                .setType(type).setName("登录任务").setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority())
                .setProgress(0).setDescription(null).setAssigneeUserId(null).setMemberUserIds(null)
                .setIterationId(null).setParentId(null).setRelatedRequirementId(null).setDefectType(null)
                .setStartTime(startTime).setEndTime(startTime.plusDays(2)).setEstimatedHours(null)
                .setFileUrls(null).setLabelIds(null).setChildWorkItemNames(null)
                .setActualHours(null).setRemainingHours(null));
    }

    private static PmsWorkItemSaveReqVO randomSaveReqVO(PmsWorkItemDO workItem) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return randomPojo(PmsWorkItemSaveReqVO.class, reqVO -> reqVO.setId(workItem.getId())
                .setProjectId(workItem.getProjectId()).setType(workItem.getType()).setName(workItem.getName())
                .setPriority(workItem.getPriority()).setProgress(workItem.getProgress())
                .setDescription(null).setAssigneeUserId(null).setMemberUserIds(null).setIterationId(null)
                .setParentId(null).setRelatedRequirementId(null).setDefectType(null)
                .setStartTime(startTime).setEndTime(startTime.plusDays(2)).setEstimatedHours(null)
                .setFileUrls(null).setLabelIds(null).setChildWorkItemNames(null)
                .setActualHours(null).setRemainingHours(null));
    }

    private static PmsProjectDO randomProjectDO(Long id, Integer type) {
        return randomPojo(PmsProjectDO.class, project -> project.setId(id).setName("测试项目")
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus()).setType(type).setLevel(3).setOpenStatus(false));
    }

    private static PmsWorkItemStatusDO randomStatusDO(Long projectId, Integer workItemType, Integer statusType,
                                                      boolean defaultStatus, int sort) {
        return randomPojo(PmsWorkItemStatusDO.class, status -> status.setId(null).setProjectId(projectId)
                .setWorkItemType(workItemType).setName("状态" + sort).setStatusType(statusType)
                .setBoardName(null).setSystemCode(null).setDefaultStatus(defaultStatus).setSort(sort));
    }

    private static PmsWorkItemDO randomWorkItemDO(Long projectId, Integer type, PmsWorkItemStatusDO status,
                                                 int serialNumber) {
        return randomPojo(PmsWorkItemDO.class, workItem -> workItem.setId(null).setProjectId(projectId).setType(type)
                .setSerialNumber(serialNumber).setName("工作项" + serialNumber)
                .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).setStatusId(status.getId())
                .setStatus(status.getStatusType()).setProgress(0).setSort(serialNumber)
                .setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .setDescription(null).setAssigneeUserId(null).setArchiveTime(null).setRecycleTime(null)
                .setIterationId(null).setParentId(null).setRelatedRequirementId(null).setDefectType(null)
                .setStartTime(null).setEndTime(null).setEstimatedHours(null).setFileUrls(null).setLabelIds(null));
    }

}

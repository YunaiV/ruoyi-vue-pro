package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemBoardConfigSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusConfigUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusDeleteReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemBoardMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemStatusMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_TRANSFER_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link PmsWorkItemStatusServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsWorkItemStatusServiceImpl.class, PmsWorkItemBoardServiceImpl.class, PmsWorkItemServiceImpl.class})
public class PmsWorkItemStatusServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemStatusServiceImpl workItemStatusService;

    @Resource
    private PmsWorkItemStatusMapper workItemStatusMapper;
    @Resource
    private PmsWorkItemBoardMapper workItemBoardMapper;
    @Resource
    private PmsWorkItemMapper workItemMapper;

    @MockBean
    private PmsProjectMemberService projectMemberService;
    @MockBean
    private PmsIterationService iterationService;
    @MockBean
    private PmsWorkItemCommentService workItemCommentService;
    @MockBean
    private PmsWorkItemWorkLogService workItemWorkLogService;
    @MockBean
    private PmsWorkItemUserSortService workItemUserSortService;
    @MockBean
    private PmsWorkItemLabelService workItemLabelService;
    @MockBean
    private PmsWorkItemActivityService workItemActivityService;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;
    @MockBean
    private AdminUserApi adminUserApi;

    @Test
    public void testInitProjectWorkItemStatuses_success() {
        // 准备参数
        Long projectId = randomLongId();

        // 调用
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());

        // 断言
        assertEquals(3, statuses.size());
        assertEquals(PmsWorkItemStatusTypeEnum.PENDING.getType(), CollUtil.getFirst(statuses).getStatusType());
        assertEquals(PmsWorkItemStatusTypeEnum.PENDING.name(), CollUtil.getFirst(statuses).getSystemCode());
        assertEquals(PmsWorkItemStatusTypeEnum.PENDING.getName(), CollUtil.getFirst(statuses).getBoardName());
        assertTrue(CollUtil.getFirst(statuses).getDefaultStatus());
        assertFalse(statuses.get(1).getDefaultStatus());
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
    }

    @Test
    public void testGetWorkItemStatusList_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());

        // 调用
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType(), userId);

        // 断言
        assertEquals(3, statuses.size());
        assertEquals(PmsWorkItemStatusTypeEnum.PENDING.getType(), CollUtil.getFirst(statuses).getStatusType());
    }

    @Test
    public void testGetWorkItemBoardList_readOnly() {
        // 准备参数
        Long projectId = randomLongId();

        // 调用
        List<PmsWorkItemBoardDO> boards = workItemStatusService.getWorkItemBoardList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());

        // 断言：读取看板配置不会补建任何数据
        assertTrue(CollUtil.isEmpty(boards));
        assertTrue(CollUtil.isEmpty(workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType())));
    }

    @Test
    public void testGetWorkItemStatus_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        PmsWorkItemStatusDO status = new PmsWorkItemStatusDO().setProjectId(projectId)
                .setWorkItemType(PmsWorkItemTypeEnum.TASK.getType()).setName("开发中")
                .setStatusType(PmsWorkItemStatusTypeEnum.PROCESSING.getType())
                .setBoardName("进行中").setDefaultStatus(false).setSort(2);
        workItemStatusMapper.insert(status);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));

        // 调用
        PmsWorkItemStatusDO result = workItemStatusService.getWorkItemStatus(status.getId(), userId);

        // 断言
        assertEquals(status.getId(), result.getId());
    }

    @Test
    public void testInitProjectWorkItemStatuses_generalOnlyTask() {
        // 准备参数
        Long projectId = randomLongId();

        // 调用
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());

        // 断言
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
        assertEquals(3, workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
        assertEquals(0, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.REQUIREMENT.getType()).size());
        assertEquals(0, workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.REQUIREMENT.getType()).size());
    }

    @Test
    public void testInitProjectWorkItemStatuses_agileAllTypes() {
        // 准备参数
        Long projectId = randomLongId();

        // 调用
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());

        // 断言
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
        assertEquals(3, workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.TASK.getType()).size());
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.REQUIREMENT.getType()).size());
        assertEquals(3, workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.REQUIREMENT.getType()).size());
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.DEFECT.getType()).size());
        assertEquals(3, workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.DEFECT.getType()).size());
    }

    @Test
    public void testInitProjectWorkItemStatuses_idempotent() {
        // 准备参数
        Long projectId = randomLongId();

        // 调用
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());

        // 断言
        assertEquals(3, workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId,
                PmsWorkItemTypeEnum.REQUIREMENT.getType()).size());
    }

    @Test
    public void testGetWorkItemStatusList_doesNotRecreateDeletedSystemStatus() {
        // 准备参数
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());
        PmsWorkItemStatusDO processingStatus = statuses.get(1);

        // 调用：删除进行中系统状态后重新加载状态列表
        workItemStatusService.deleteWorkItemStatus(new PmsWorkItemStatusDeleteReqVO()
                .setId(processingStatus.getId()), userId);
        List<PmsWorkItemStatusDO> recreatedStatuses = workItemStatusService
                .getWorkItemStatusList(projectId, PmsWorkItemTypeEnum.TASK.getType());

        // 断言：读取接口只查询已保存数据，不会重新创建被删除的系统状态
        assertEquals(2, recreatedStatuses.size());
        assertFalse(recreatedStatuses.stream().anyMatch(status ->
                PmsWorkItemStatusTypeEnum.PROCESSING.name().equals(status.getSystemCode())));
    }

    @Test
    public void testValidateWorkItemStatus_projectMismatch() {
        // mock 数据
        PmsWorkItemStatusDO status = new PmsWorkItemStatusDO().setProjectId(randomLongId())
                .setWorkItemType(PmsWorkItemTypeEnum.TASK.getType()).setName("未开始")
                .setStatusType(PmsWorkItemStatusTypeEnum.PENDING.getType()).setDefaultStatus(true).setSort(1);
        workItemStatusMapper.insert(status);

        // 调用，并断言异常
        assertServiceException(() -> workItemStatusService.validateWorkItemStatus(status.getId(), randomLongId(),
                PmsWorkItemTypeEnum.TASK.getType()), WORK_ITEM_STATUS_INVALID);
    }

    @Test
    public void testCreateAndUpdateWorkItemStatus_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.AGILE.getType());
        // 准备参数并创建
        Long statusId = workItemStatusService.createWorkItemStatus(new PmsWorkItemStatusCreateReqVO()
                .setProjectId(projectId).setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setName("待验收").setStatusType(PmsWorkItemStatusTypeEnum.PROCESSING.getType())
                .setDescription("等待产品验收"), userId);
        PmsWorkItemDO workItem = new PmsWorkItemDO().setProjectId(projectId)
                .setType(PmsWorkItemTypeEnum.TASK.getType()).setSerialNumber(1).setName("任务")
                .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).setStatusId(statusId)
                .setStatus(PmsWorkItemStatusTypeEnum.PROCESSING.getType()).setProgress(0).setSort(1);
        workItemMapper.insert(workItem);

        // 调用更新
        workItemStatusService.updateWorkItemStatusConfig(new PmsWorkItemStatusConfigUpdateReqVO().setId(statusId)
                .setName("已验收").setStatusType(PmsWorkItemStatusTypeEnum.COMPLETED.getType())
                .setDescription("产品验收完成"), userId);

        // 断言
        assertEquals("已验收", workItemStatusMapper.selectById(statusId).getName());
        assertEquals("产品验收完成", workItemStatusMapper.selectById(statusId).getDescription());
        assertNull(workItemStatusMapper.selectById(statusId).getBoardName());
        assertEquals(PmsWorkItemStatusTypeEnum.COMPLETED.getType(),
                workItemMapper.selectById(workItem.getId()).getStatus());
    }

    @Test
    public void testUpdateWorkItemBoardConfig_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());
        // 准备参数
        PmsWorkItemBoardConfigSaveReqVO reqVO = new PmsWorkItemBoardConfigSaveReqVO()
                .setProjectId(projectId).setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setBoards(Arrays.asList(
                        new PmsWorkItemBoardConfigSaveReqVO.Board().setName("待处理")
                                .setStatusIds(Collections.singletonList(statuses.get(0).getId())),
                        new PmsWorkItemBoardConfigSaveReqVO.Board().setName("处理中")
                                .setStatusIds(Arrays.asList(statuses.get(1).getId(), statuses.get(2).getId())),
                        new PmsWorkItemBoardConfigSaveReqVO.Board().setName("待发布")
                                .setStatusIds(Collections.emptyList())));

        // 调用
        workItemStatusService.updateWorkItemBoardConfig(reqVO, userId);

        // 断言
        List<PmsWorkItemBoardDO> boards = workItemBoardMapper
                .selectListByProjectIdAndWorkItemType(projectId, PmsWorkItemTypeEnum.TASK.getType());
        assertEquals(3, boards.size());
        assertEquals("待发布", boards.get(2).getName());
        assertEquals("待处理", workItemStatusMapper.selectById(statuses.get(0).getId()).getBoardName());
        assertEquals("处理中", workItemStatusMapper.selectById(statuses.get(2).getId()).getBoardName());
    }

    @Test
    public void testCreateWorkItemStatus_nameDuplicate() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));

        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        // 调用，并断言异常：已初始化的系统状态不能重复创建
        assertServiceException(() -> workItemStatusService.createWorkItemStatus(new PmsWorkItemStatusCreateReqVO()
                .setProjectId(projectId).setWorkItemType(PmsWorkItemTypeEnum.TASK.getType())
                .setName("未开始").setStatusType(PmsWorkItemStatusTypeEnum.PENDING.getType()), userId),
                WORK_ITEM_STATUS_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateDefaultAndSortWorkItemStatuses_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());

        // 调用
        workItemStatusService.updateDefaultWorkItemStatus(statuses.get(1).getId(), userId);
        workItemStatusService.updateWorkItemStatusSort(new PmsWorkItemStatusSortReqVO().setStatusIds(Arrays.asList(
                statuses.get(2).getId(), statuses.get(1).getId(), CollUtil.getFirst(statuses).getId())), userId);

        // 断言
        List<PmsWorkItemStatusDO> results = workItemStatusMapper
                .selectListByProjectIdAndWorkItemType(projectId, PmsWorkItemTypeEnum.TASK.getType());
        assertEquals(statuses.get(2).getId(), CollUtil.getFirst(results).getId());
        assertTrue(workItemStatusMapper.selectById(statuses.get(1).getId()).getDefaultStatus());
    }

    @Test
    public void testDeleteWorkItemStatus_transferWorkItems() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());
        PmsWorkItemDO workItem = new PmsWorkItemDO().setProjectId(projectId)
                .setType(PmsWorkItemTypeEnum.TASK.getType()).setSerialNumber(1).setName("任务")
                .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).setStatusId(statuses.get(1).getId())
                .setStatus(statuses.get(1).getStatusType()).setProgress(0).setSort(1);
        workItemMapper.insert(workItem);

        // 调用
        workItemStatusService.deleteWorkItemStatus(new PmsWorkItemStatusDeleteReqVO().setId(statuses.get(1).getId())
                .setTransferStatusId(statuses.get(2).getId()), userId);

        // 断言
        assertNull(workItemStatusMapper.selectById(statuses.get(1).getId()));
        assertEquals(statuses.get(2).getId(), workItemMapper.selectById(workItem.getId()).getStatusId());
        assertEquals(statuses.get(2).getStatusType(), workItemMapper.selectById(workItem.getId()).getStatus());
    }

    @Test
    public void testDeleteWorkItemStatus_defaultStatus() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        PmsWorkItemStatusDO defaultStatus = CollUtil.getFirst(workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType()));

        // 调用，并断言异常
        assertServiceException(() -> workItemStatusService.deleteWorkItemStatus(new PmsWorkItemStatusDeleteReqVO()
                .setId(defaultStatus.getId()), userId), WORK_ITEM_STATUS_DEFAULT_CANNOT_DELETE);
    }

    @Test
    public void testDeleteWorkItemStatus_missingTransferStatus() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(randomProjectDO(projectId));
        workItemStatusService.initProjectWorkItemStatuses(projectId, PmsProjectTypeEnum.GENERAL.getType());
        List<PmsWorkItemStatusDO> statuses = workItemStatusService.getWorkItemStatusList(projectId,
                PmsWorkItemTypeEnum.TASK.getType());
        PmsWorkItemDO workItem = new PmsWorkItemDO().setProjectId(projectId)
                .setType(PmsWorkItemTypeEnum.TASK.getType()).setSerialNumber(1).setName("任务")
                .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).setStatusId(statuses.get(1).getId())
                .setStatus(statuses.get(1).getStatusType()).setProgress(0).setSort(1);
        workItemMapper.insert(workItem);

        // 调用，并断言异常
        assertServiceException(() -> workItemStatusService.deleteWorkItemStatus(new PmsWorkItemStatusDeleteReqVO()
                .setId(statuses.get(1).getId()), userId), WORK_ITEM_STATUS_TRANSFER_INVALID);
    }

    // ========== 随机对象 ==========

    private static PmsProjectDO randomProjectDO(Long id) {
        return new PmsProjectDO().setId(id).setName("测试项目")
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus()).setType(PmsProjectTypeEnum.AGILE.getType())
                .setLevel(3).setOpenStatus(false);
    }

}

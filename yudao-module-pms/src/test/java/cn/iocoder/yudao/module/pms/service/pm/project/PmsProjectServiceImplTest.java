package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSceneTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSortTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemStatusService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemWorkLogService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_OWNER_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsProjectServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectServiceImpl.class)
public class PmsProjectServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectServiceImpl projectService;

    @Resource
    private PmsProjectMapper projectMapper;

    @MockitoBean
    private PmsProjectMemberService projectMemberService;
    @MockitoBean
    private PmsProjectGroupService projectGroupService;
    @MockitoBean
    private PmsProjectFavoriteService projectFavoriteService;
    @MockitoBean
    private PmsProjectAnnouncementService projectAnnouncementService;
    @MockitoBean
    private PmsIterationService iterationService;
    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsWorkItemStatusService workItemStatusService;
    @MockitoBean
    private PmsWorkItemWorkLogService workItemWorkLogService;
    @MockitoBean
    private PermissionApi permissionApi;

    @Test
    public void testCreateProject_success() {
        // 准备参数
        Long userId = randomLongId();
        PmsProjectSaveReqVO reqVO = randomProjectSaveReqVO();
        reqVO.setMemberUserIds(asList(randomLongId(), randomLongId()));

        // 调用
        Long projectId = projectService.createProject(reqVO, userId);

        // 断言
        PmsProjectDO project = projectMapper.selectById(projectId);
        assertNotNull(project);
        assertEquals(reqVO.getName(), project.getName());
        assertEquals(PmsProjectStatusEnum.ACTIVE.getStatus(), project.getStatus());
        verify(projectMemberService).createProjectMemberList(projectId, userId, reqVO.getMemberUserIds());
        verify(workItemStatusService).initProjectWorkItemStatuses(projectId, reqVO.getType());
    }

    @Test
    public void testUpdateProject_notAdmin() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(project);
        when(projectMemberService.hasProjectManagerPermission(project.getId(), userId)).thenReturn(false);
        // 准备参数
        PmsProjectSaveReqVO reqVO = randomProjectSaveReqVO(project.getId());

        // 调用，并断言异常
        assertServiceException(() -> projectService.updateProject(reqVO, userId), PROJECT_ADMIN_REQUIRED);
    }

    @Test
    public void testGetProjectPage_allIncludesOpenProject() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO openProject = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus()).setOpenStatus(true);
        projectMapper.insert(openProject);
        when(projectMemberService.getProjectIdListByUserId(userId)).thenReturn(emptyList());
        // 准备参数
        PmsProjectPageReqVO reqVO = new PmsProjectPageReqVO()
                .setSceneType(PmsProjectSceneTypeEnum.ALL.getType())
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus());

        // 调用
        PageResult<PmsProjectDO> pageResult = projectService.getProjectPage(reqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(openProject.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetProjectPage_createTimeAscending() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO oldProject = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus()).setOpenStatus(true);
        oldProject.setCreateTime(LocalDateTime.now().minusDays(2));
        PmsProjectDO newProject = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus()).setOpenStatus(true);
        newProject.setCreateTime(LocalDateTime.now().minusDays(1));
        projectMapper.insert(oldProject);
        projectMapper.insert(newProject);
        when(projectMemberService.getProjectIdListByUserId(userId)).thenReturn(emptyList());
        // 准备参数
        PmsProjectPageReqVO reqVO = new PmsProjectPageReqVO()
                .setSceneType(PmsProjectSceneTypeEnum.ALL.getType())
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus())
                .setSortType(PmsProjectSortTypeEnum.CREATE_TIME.getType());

        // 调用
        PageResult<PmsProjectDO> pageResult = projectService.getProjectPage(reqVO, userId);

        // 断言：创建时间按升序，旧项目在前
        assertEquals(Arrays.asList(oldProject.getId(), newProject.getId()),
                convertList(pageResult.getList(), PmsProjectDO::getId));
    }

    @Test
    public void testGetFavoriteProjectList_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO favoriteProject = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(favoriteProject);
        PmsProjectDO nonMemberProject = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(nonMemberProject);
        PmsProjectDO archivedProject = randomProjectDO(PmsProjectStatusEnum.ARCHIVED.getStatus());
        projectMapper.insert(archivedProject);
        when(projectFavoriteService.getFavoriteProjectIdListByUserId(userId))
                .thenReturn(asList(favoriteProject.getId(), nonMemberProject.getId(), archivedProject.getId()));
        when(projectMemberService.getProjectIdListByUserId(userId))
                .thenReturn(asList(favoriteProject.getId(), archivedProject.getId()));

        // 调用
        List<PmsProjectDO> projects = projectService.getFavoriteProjectList(userId);

        // 断言
        assertEquals(1, projects.size());
        assertEquals(favoriteProject.getId(), CollUtil.getFirst(projects).getId());
    }

    @Test
    public void testArchiveProject_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(project);
        when(projectMemberService.validateProjectReadable(project.getId(), userId)).thenReturn(project);
        when(projectMemberService.hasProjectManagerPermission(project.getId(), userId)).thenReturn(true);

        // 调用
        projectService.archiveProject(project.getId(), userId);

        // 断言
        PmsProjectDO archivedProject = projectMapper.selectById(project.getId());
        assertEquals(PmsProjectStatusEnum.ARCHIVED.getStatus(), archivedProject.getStatus());
        assertNotNull(archivedProject.getArchiveTime());
        verify(projectGroupService).deleteProjectGroupRelationListByProjectId(project.getId());
        verify(projectFavoriteService).deleteProjectFavoriteListByProjectId(project.getId());
    }

    @Test
    public void testRecycleProject_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(project);
        when(projectMemberService.hasProjectManagerPermission(project.getId(), userId)).thenReturn(true);

        // 调用
        projectService.recycleProject(project.getId(), userId);

        // 断言
        PmsProjectDO recycledProject = projectMapper.selectById(project.getId());
        assertEquals(PmsProjectStatusEnum.RECYCLED.getStatus(), recycledProject.getStatus());
        assertNotNull(recycledProject.getRecycleTime());
        verify(projectGroupService).deleteProjectGroupRelationListByProjectId(project.getId());
        verify(projectFavoriteService).deleteProjectFavoriteListByProjectId(project.getId());
    }

    @Test
    public void testRestoreProject_success() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.ARCHIVED.getStatus())
                .setArchiveTime(LocalDateTime.now());
        projectMapper.insert(project);
        when(projectMemberService.validateProjectReadable(project.getId(), userId)).thenReturn(project);
        when(projectMemberService.hasProjectManagerPermission(project.getId(), userId)).thenReturn(true);

        // 调用
        projectService.restoreProject(project.getId(), userId);

        // 断言
        PmsProjectDO restoredProject = projectMapper.selectById(project.getId());
        assertEquals(PmsProjectStatusEnum.ACTIVE.getStatus(), restoredProject.getStatus());
        assertNull(restoredProject.getArchiveTime());
        assertNull(restoredProject.getRecycleTime());
    }

    @Test
    public void testDeleteProject_statusInvalid() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.ACTIVE.getStatus());
        projectMapper.insert(project);
        when(projectMemberService.hasProjectOwnerPermission(project.getId(), userId)).thenReturn(true);

        // 调用，并断言异常
        assertServiceException(() -> projectService.deleteProject(project.getId(), userId), PROJECT_STATUS_INVALID);
    }

    @Test
    public void testDeleteProject_ownerRequired() {
        // mock 数据
        Long userId = randomLongId();
        PmsProjectDO project = randomProjectDO(PmsProjectStatusEnum.RECYCLED.getStatus());
        projectMapper.insert(project);
        when(projectMemberService.hasProjectOwnerPermission(project.getId(), userId)).thenReturn(false);

        // 调用，并断言异常
        assertServiceException(() -> projectService.deleteProject(project.getId(), userId), PROJECT_OWNER_REQUIRED);
    }

    @Test
    public void testGetProjectOverview_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        PmsWorkItemDO pendingWorkItem = randomWorkItemDO(userId, PmsWorkItemTypeEnum.TASK.getType(),
                PmsWorkItemStatusTypeEnum.PENDING.getType(), 1);
        PmsWorkItemDO processingWorkItem = randomWorkItemDO(userId, PmsWorkItemTypeEnum.DEFECT.getType(),
                PmsWorkItemStatusTypeEnum.PROCESSING.getType(), 2);
        PmsWorkItemDO completedWorkItem = randomWorkItemDO(randomLongId(),
                PmsWorkItemTypeEnum.REQUIREMENT.getType(), PmsWorkItemStatusTypeEnum.COMPLETED.getType(), 3);
        when(workItemService.getActiveWorkItemListByProjectId(projectId))
                .thenReturn(Arrays.asList(pendingWorkItem, processingWorkItem, completedWorkItem));

        // 调用
        PmsProjectOverviewRespVO overview = projectService.getProjectOverview(projectId, userId);

        // 断言
        assertEquals(3L, overview.getTotalCount());
        assertEquals(1L, overview.getPendingCount());
        assertEquals(1L, overview.getProcessingCount());
        assertEquals(1L, overview.getCompletedCount());
        assertEquals(1L, overview.getTypeCountMap().get(PmsWorkItemTypeEnum.TASK.getType()));
        assertEquals(14, overview.getCompletedTrends().size());
        assertEquals(2, overview.getAssignedWorkItems().size());
        verify(projectMemberService).validateProjectReadable(projectId, userId);
    }

    // ========== 随机对象 ==========

    private static PmsProjectSaveReqVO randomProjectSaveReqVO() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return randomPojo(PmsProjectSaveReqVO.class,
                reqVO -> reqVO.setId(null).setType(1).setMemberUserIds(emptyList())
                .setName("官网重构").setLevel(3).setDescription("测试项目").setOpenStatus(false)
                .setIcon("ep:folder").setStartTime(startTime).setEndTime(startTime.plusMonths(1)));
    }

    private static PmsProjectSaveReqVO randomProjectSaveReqVO(Long id) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return randomPojo(PmsProjectSaveReqVO.class, reqVO -> reqVO.setId(id).setType(1)
                .setMemberUserIds(emptyList()).setName("新项目名称").setLevel(3)
                .setDescription("更新描述").setOpenStatus(true).setIcon("ep:folder")
                .setStartTime(startTime).setEndTime(startTime.plusMonths(1)));
    }

    private static PmsProjectDO randomProjectDO(Integer status) {
        return randomPojo(PmsProjectDO.class, project -> project.setName("测试项目").setStatus(status)
                .setType(1).setLevel(3).setOpenStatus(false).setCreator(String.valueOf(randomLongId())));
    }

    private static PmsWorkItemDO randomWorkItemDO(Long assigneeUserId, Integer type, Integer status, Integer serial) {
        PmsWorkItemDO workItem = randomPojo(PmsWorkItemDO.class,
                item -> item.setId(randomLongId()).setAssigneeUserId(assigneeUserId)
                        .setType(type).setStatus(status).setStatusId(randomLongId()).setSerialNumber(serial)
                        .setName("测试工作项").setProgress(0).setSort(serial));
        workItem.setUpdateTime(LocalDateTime.now());
        return workItem;
    }

}

package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectSceneTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemStatusService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemWorkLogService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ADMIN_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_OWNER_REQUIRED;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;

/**
 * PMS 项目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsProjectServiceImpl implements PmsProjectService {

    /**
     * 项目完成趋势统计天数
     */
    private static final int TREND_DAYS = 14;
    /**
     * 项目概况展示的待办工作项数量上限
     */
    private static final int ASSIGNED_WORK_ITEM_LIMIT = 10;

    @Resource
    private PmsProjectMapper projectMapper;

    @Resource
    private PmsProjectMemberService projectMemberService;
    @Resource
    private PmsProjectGroupService projectGroupService;
    @Resource
    private PmsProjectFavoriteService projectFavoriteService;
    @Resource
    private PmsProjectAnnouncementService projectAnnouncementService;
    @Resource
    private PmsIterationService iterationService;
    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsWorkItemStatusService workItemStatusService;
    @Resource
    private PmsWorkItemWorkLogService workItemWorkLogService;

    @Resource
    private PermissionApi permissionApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProject(PmsProjectSaveReqVO saveReqVO, Long userId) {
        // 1. 创建进行中的项目
        PmsProjectDO project = BeanUtils.toBean(saveReqVO, PmsProjectDO.class)
                .setStatus(PmsProjectStatusEnum.ACTIVE.getStatus()).setSort(0).setAccessTime(LocalDateTime.now());
        projectMapper.insert(project);

        // 2.1 创建人作为项目管理员，并初始化其他项目成员
        projectMemberService.createProjectMemberList(project.getId(), userId, saveReqVO.getMemberUserIds());
        // 2.2 初始化项目支持的工作项看板状态
        workItemStatusService.initProjectWorkItemStatuses(project.getId(), project.getType());
        return project.getId();
    }

    @Override
    public void updateProject(PmsProjectSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(saveReqVO.getId());
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验当前用户可以管理项目
        validateProjectManagerPermission(saveReqVO.getId(), userId);

        // 2. 更新项目基本信息
        PmsProjectDO updateObj = BeanUtils.toBean(saveReqVO, PmsProjectDO.class)
                .setType(null);
        projectMapper.updateById(updateObj);
    }

    @Override
    public PmsProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public PmsProjectDO getProjectAndUpdateVisitTime(Long id, Long userId) {
        // 1. 校验当前用户可以查看项目
        PmsProjectDO project = projectMemberService.validateProjectReadable(id, userId);

        // 2. 记录最近访问时间
        LocalDateTime accessTime = LocalDateTime.now();
        projectMapper.updateById(new PmsProjectDO().setId(id).setAccessTime(accessTime));
        project.setAccessTime(accessTime);
        return project;
    }

    @Override
    public PageResult<PmsProjectDO> getProjectPage(PmsProjectPageReqVO pageReqVO, Long userId) {
        List<Long> projectIds;
        boolean includeOpenProject = false;
        boolean includeAllProject = false;

        // 1. 按项目状态和列表场景确定可查询的项目范围
        if (ObjectUtil.notEqual(PmsProjectStatusEnum.ACTIVE.getStatus(), pageReqVO.getStatus())) {
            projectIds = projectMemberService.getManagedProjectIdListByUserId(userId);
        } else if (PmsProjectSceneTypeEnum.ALL.getType().equals(pageReqVO.getSceneType())) {
            projectIds = projectMemberService.getProjectIdListByUserId(userId);
            includeOpenProject = true;
            includeAllProject = permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode());
        } else if (PmsProjectSceneTypeEnum.MANAGED.getType().equals(pageReqVO.getSceneType())) {
            projectIds = projectMemberService.getManagedProjectIdListByUserId(userId);
        } else {
            projectIds = projectGroupService.filterProjectIdListByGroupId(pageReqVO.getGroupId(), userId,
                    projectMemberService.getProjectIdListByUserId(userId));
        }
        if (!includeOpenProject && CollUtil.isEmpty(projectIds)) {
            return PageResult.empty();
        }

        // 2. 查询项目分页
        return projectMapper.selectPage(pageReqVO, projectIds, includeOpenProject, includeAllProject);
    }

    @Override
    public List<PmsProjectDO> getFavoriteProjectList(Long userId) {
        // 1. 查询当前用户参与且已星标的项目编号
        List<Long> favoriteProjectIds = projectFavoriteService.getFavoriteProjectIdListByUserId(userId);
        List<Long> memberProjectIds = projectMemberService.getProjectIdListByUserId(userId);
        List<Long> readableFavoriteProjectIds = filterList(favoriteProjectIds, memberProjectIds::contains);
        if (CollUtil.isEmpty(readableFavoriteProjectIds)) {
            return Collections.emptyList();
        }
        // 2. 查询进行中的星标项目列表
        return projectMapper.selectListByIdsAndStatus(readableFavoriteProjectIds, PmsProjectStatusEnum.ACTIVE.getStatus());
    }

    @Override
    public List<PmsProjectDO> getProjectList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return projectMapper.selectByIds(ids);
    }

    @Override
    public PmsProjectOverviewRespVO getProjectOverview(Long id, Long userId) {
        // 1.1 校验项目可访问
        projectMemberService.validateProjectReadable(id, userId);
        // 1.2 查询项目的全部工作项
        List<PmsWorkItemDO> workItems = workItemService.getActiveWorkItemListByProjectId(id);

        // 2. 统计工作项状态和类型
        long pendingCount = 0L;
        long processingCount = 0L;
        long completedCount = 0L;
        Map<Integer, Long> typeCountMap = new LinkedHashMap<>();
        for (PmsWorkItemDO workItem : workItems) {
            typeCountMap.put(workItem.getType(), typeCountMap.getOrDefault(workItem.getType(), 0L) + 1);
            if (PmsWorkItemStatusTypeEnum.PENDING.getType().equals(workItem.getStatus())) {
                pendingCount++;
            } else if (PmsWorkItemStatusTypeEnum.PROCESSING.getType().equals(workItem.getStatus())) {
                processingCount++;
            } else if (PmsWorkItemStatusTypeEnum.COMPLETED.getType().equals(workItem.getStatus())) {
                completedCount++;
            }
        }

        // 3. 统计近十四日完成趋势
        LocalDate beginDate = LocalDate.now().minusDays(TREND_DAYS - 1L);
        Map<LocalDate, Long> completedCountMap = new LinkedHashMap<>();
        for (int index = 0; index < TREND_DAYS; index++) {
            completedCountMap.put(beginDate.plusDays(index), 0L);
        }
        for (PmsWorkItemDO workItem : workItems) {
            if (PmsWorkItemStatusTypeEnum.COMPLETED.getType().equals(workItem.getStatus())
                    && workItem.getUpdateTime() != null
                    && !workItem.getUpdateTime().toLocalDate().isBefore(beginDate)) {
                LocalDate completedDate = workItem.getUpdateTime().toLocalDate();
                completedCountMap.computeIfPresent(completedDate, (date, count) -> count + 1);
            }
        }
        List<PmsProjectOverviewRespVO.TrendPoint> completedTrends = new ArrayList<>();
        completedCountMap.forEach((date, count) -> completedTrends.add(
                new PmsProjectOverviewRespVO.TrendPoint().setDate(date.toString()).setCount(count)));

        // 4. 查询分配给当前用户的未完成工作项
        List<PmsWorkItemDO> assignedWorkItems = new ArrayList<>();
        for (PmsWorkItemDO workItem : workItems) {
            if (userId.equals(workItem.getAssigneeUserId())
                    && ObjectUtil.notEqual(PmsWorkItemStatusTypeEnum.COMPLETED.getType(), workItem.getStatus())) {
                assignedWorkItems.add(workItem);
            }
        }
        assignedWorkItems.sort(Comparator.comparing(PmsWorkItemDO::getEndTime,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(PmsWorkItemDO::getId));
        if (assignedWorkItems.size() > ASSIGNED_WORK_ITEM_LIMIT) {
            assignedWorkItems = assignedWorkItems.subList(0, ASSIGNED_WORK_ITEM_LIMIT);
        }
        List<PmsProjectOverviewRespVO.AssignedWorkItem> assignedWorkItemVOs = new ArrayList<>();
        for (PmsWorkItemDO workItem : assignedWorkItems) {
            assignedWorkItemVOs.add(new PmsProjectOverviewRespVO.AssignedWorkItem().setId(workItem.getId())
                    .setSerialNumber(workItem.getSerialNumber()).setType(workItem.getType())
                    .setName(workItem.getName()).setStatus(workItem.getStatus()).setEndTime(workItem.getEndTime())
                    .setProgress(workItem.getProgress()));
        }

        // 5. 组装项目概况
        return new PmsProjectOverviewRespVO().setTotalCount((long) workItems.size())
                .setPendingCount(pendingCount).setProcessingCount(processingCount)
                .setCompletedCount(completedCount).setTypeCountMap(typeCountMap)
                .setCompletedTrends(completedTrends).setAssignedWorkItems(assignedWorkItemVOs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveProject(Long id, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(id);
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验当前用户可以管理项目
        validateProjectManagerPermission(id, userId);

        // 2. 归档项目，并清除个人项目分组和星标关系
        int updateCount = projectMapper.updateStatusAndArchiveTimeById(id, PmsProjectStatusEnum.ACTIVE.getStatus(),
                PmsProjectStatusEnum.ARCHIVED.getStatus(), LocalDateTime.now());
        if (updateCount == 0) {
            throw exception(PROJECT_STATUS_INVALID);
        }
        projectGroupService.deleteProjectGroupRelationListByProjectId(id);
        projectFavoriteService.deleteProjectFavoriteListByProjectId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleProject(Long id, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(id);
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验当前用户可以管理项目
        validateProjectManagerPermission(id, userId);

        // 2. 将项目移入回收站，并清除个人项目分组和星标关系
        int updateCount = projectMapper.updateStatusAndRecycleTimeById(id, PmsProjectStatusEnum.ACTIVE.getStatus(),
                PmsProjectStatusEnum.RECYCLED.getStatus(), LocalDateTime.now());
        if (updateCount == 0) {
            throw exception(PROJECT_STATUS_INVALID);
        }
        projectGroupService.deleteProjectGroupRelationListByProjectId(id);
        projectFavoriteService.deleteProjectFavoriteListByProjectId(id);
    }

    @Override
    public void restoreProject(Long id, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(id);
        // 1.2 校验项目已归档或位于回收站
        if (PmsProjectStatusEnum.ACTIVE.getStatus().equals(project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }
        // 1.3 校验当前用户可以管理项目
        validateProjectManagerPermission(id, userId);

        // 2. 恢复为进行中的项目，并清空生命周期时间
        int updateCount = projectMapper.updateToRestoreById(id, PmsProjectStatusEnum.ACTIVE.getStatus());
        if (updateCount == 0) {
            throw exception(PROJECT_STATUS_INVALID);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id, Long userId) {
        // 1.1 校验项目存在
        PmsProjectDO project = validateProjectExists(id);
        // 1.2 校验项目位于回收站
        if (ObjectUtil.notEqual(PmsProjectStatusEnum.RECYCLED.getStatus(), project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }
        // 1.3 校验当前用户拥有项目所有者权限
        validateProjectOwnerPermission(id, userId);

        // 2. 删除项目工时、工作项、迭代、成员、个人分组关系和项目
        workItemWorkLogService.deleteWorkItemWorkLogListByProjectId(id);
        workItemService.deleteWorkItemListByProjectId(id);
        iterationService.deleteIterationListByProjectId(id);
        projectGroupService.deleteProjectGroupRelationListByProjectId(id);
        projectMemberService.deleteProjectMemberListByProjectId(id);
        projectFavoriteService.deleteProjectFavoriteListByProjectId(id);
        projectAnnouncementService.deleteProjectAnnouncementListByProjectId(id);
        projectMapper.deleteById(id);
    }

    /**
     * 校验当前用户可以管理项目
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    private void validateProjectManagerPermission(Long projectId, Long userId) {
        if (!projectMemberService.hasProjectManagerPermission(projectId, userId)) {
            throw exception(PROJECT_ADMIN_REQUIRED);
        }
    }

    /**
     * 校验当前用户拥有项目所有者权限
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    private void validateProjectOwnerPermission(Long projectId, Long userId) {
        if (!projectMemberService.hasProjectOwnerPermission(projectId, userId)) {
            throw exception(PROJECT_OWNER_REQUIRED);
        }
    }

    /**
     * 校验项目处于进行中
     *
     * @param project 项目
     */
    private void validateActiveProject(PmsProjectDO project) {
        if (ObjectUtil.notEqual(PmsProjectStatusEnum.ACTIVE.getStatus(), project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }
    }

    /**
     * 校验项目存在
     *
     * @param id 项目编号
     * @return 项目
     */
    private PmsProjectDO validateProjectExists(Long id) {
        PmsProjectDO project = projectMapper.selectById(id);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project;
    }

}

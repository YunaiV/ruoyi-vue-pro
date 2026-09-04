package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportExcelVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemIterationUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemNameUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPlanningSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemMemberDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemMemberMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemLifecycleStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.google.common.collect.Maps;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_DEFECT_TYPE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_DELETE_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_ITERATION_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_LIFECYCLE_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_PARENT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_RELATED_REQUIREMENT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_SORT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_TYPE_INVALID;
import static cn.iocoder.yudao.module.pms.enums.MessageTemplateConstants.WORK_ITEM_ASSIGNED;

/**
 * PMS 工作项 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PmsWorkItemServiceImpl implements PmsWorkItemService {

    @Resource
    private PmsWorkItemMapper workItemMapper;

    @Resource
    private PmsWorkItemMemberMapper workItemMemberMapper;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemCommentService workItemCommentService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemWorkLogService workItemWorkLogService;
    @Resource
    private PmsWorkItemUserSortService workItemUserSortService;
    @Resource
    private PmsProjectMemberService projectMemberService;
    @Resource
    private PmsIterationService iterationService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemStatusService workItemStatusService;
    @Resource
    private PmsWorkItemLabelService workItemLabelService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemActivityService workItemActivityService;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkItem(PmsWorkItemSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(saveReqVO.getProjectId(), userId);
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验工作项类型可用
        validateWorkItemType(project, saveReqVO.getType());
        // 1.4 校验工作项关联关系
        validateWorkItemRelations(null, project, saveReqVO.getType(), saveReqVO, userId);
        // 1.5 校验工作项参与人
        validateWorkItemMembers(project.getId(), saveReqVO, userId);
        // 1.6 校验工作项标签
        workItemLabelService.validateWorkItemLabelIds(saveReqVO.getLabelIds());

        // 2. 创建工作项，生命周期初始状态由后端控制
        PmsWorkItemStatusDO defaultStatus = workItemStatusService
                .getDefaultWorkItemStatus(project.getId(), saveReqVO.getType());
        Integer maxSerialNumber = workItemMapper.selectMaxSerialNumberByProjectId(project.getId());
        Integer maxSort = workItemMapper.selectMaxSortByStatusId(defaultStatus.getId());
        PmsWorkItemDO workItem = BeanUtils.toBean(saveReqVO, PmsWorkItemDO.class)
                .setName(saveReqVO.getName().trim())
                .setSerialNumber(maxSerialNumber == null ? 1 : maxSerialNumber + 1)
                .setStatusId(defaultStatus.getId()).setStatus(defaultStatus.getStatusType())
                .setLifecycleStatus(PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .setSort(maxSort == null ? 1 : maxSort + 1);
        workItemMapper.insert(workItem);

        // 3.1 保存工作项参与人
        saveWorkItemMembers(workItem.getId(), project.getId(), getMemberUserIds(saveReqVO, userId));
        // 3.2 记录工作项创建动态
        workItemActivityService.createWorkItemActivity(
                project.getId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_CREATED);

        // 4.1 创建内联子工作项，沿用父项类型、迭代和看板状态
        List<String> childWorkItemNames = saveReqVO.getChildWorkItemNames();
        if (CollUtil.isNotEmpty(childWorkItemNames)) {
            for (String childWorkItemName : childWorkItemNames) {
                PmsWorkItemSaveReqVO childSaveReqVO = new PmsWorkItemSaveReqVO().setProjectId(project.getId())
                        .setType(workItem.getType()).setName(childWorkItemName)
                        .setPriority(PmsWorkItemPriorityEnum.MEDIUM.getPriority()).setProgress(0)
                        .setIterationId(workItem.getIterationId()).setParentId(workItem.getId());
                Long childWorkItemId = createWorkItem(childSaveReqVO, userId);
                // 子工作项创建后同步父项状态和排序，保持看板中的位置一致
                workItemMapper.updateById(new PmsWorkItemDO().setId(childWorkItemId)
                        .setStatusId(workItem.getStatusId()).setStatus(workItem.getStatus()).setSort(workItem.getSort()));
            }
        }
        // 4.2 创建时登记工时
        if (saveReqVO.getActualHours() != null) {
            workItemWorkLogService.createWorkItemWorkLog(new PmsWorkItemWorkLogSaveReqVO()
                    .setWorkItemId(workItem.getId()).setActualHours(saveReqVO.getActualHours())
                    .setRemainingHours(saveReqVO.getRemainingHours()), userId);
        }

        // 5. 通知工作项负责人
        sendWorkItemAssignedNotify(workItem.getAssigneeUserId(), userId, project.getId(),
                project.getName(), workItem.getSerialNumber(), workItem.getName());
        return workItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItem(PmsWorkItemSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(saveReqVO.getId());
        // 1.2 校验工作项处于正常状态
        validateActiveWorkItem(workItem);
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);
        // 1.5 校验工作项关联关系
        validateWorkItemRelations(workItem.getId(), project, workItem.getType(), saveReqVO, userId);
        // 1.6 校验工作项参与人
        validateWorkItemMembers(project.getId(), saveReqVO, userId);
        // 1.7 校验工作项标签
        workItemLabelService.validateWorkItemLabelIds(saveReqVO.getLabelIds());
        Set<Long> oldMemberUserIds = convertSet(workItemMemberMapper.selectListByWorkItemIds(
                Collections.singleton(workItem.getId())), PmsWorkItemMemberDO::getUserId);
        Set<Long> newMemberUserIds = getMemberUserIds(saveReqVO, userId);

        // 2. 更新工作项业务字段，不允许表单修改类型和生命周期状态
        PmsWorkItemDO updateObj = BeanUtils.toBean(saveReqVO, PmsWorkItemDO.class)
                .setProjectId(null).setType(null).setName(saveReqVO.getName().trim());
        workItemMapper.updateForEdit(updateObj);

        // 3.1 重建工作项参与人关系
        workItemMemberMapper.deleteByWorkItemId(workItem.getId());
        saveWorkItemMembers(workItem.getId(), project.getId(), newMemberUserIds);
        // 3.2 按字段记录工作项更新动态
        workItemActivityService.createWorkItemUpdateActivities(workItem, updateObj,
                oldMemberUserIds, newMemberUserIds, userId);

        // 4. 负责人发生变化时通知新负责人
        if (ObjectUtil.notEqual(workItem.getAssigneeUserId(), saveReqVO.getAssigneeUserId())) {
            sendWorkItemAssignedNotify(saveReqVO.getAssigneeUserId(), userId, project.getId(),
                    project.getName(), workItem.getSerialNumber(), updateObj.getName());
        }
    }

    /**
     * 发送工作项指派站内信。通知配置异常不能阻断工作项主业务
     */
    private void sendWorkItemAssignedNotify(Long receiverUserId, Long operatorUserId, Long projectId,
                                            String projectName, Integer serialNumber, String workItemName) {
        if (receiverUserId == null || receiverUserId.equals(operatorUserId)) {
            return;
        }
        Map<String, Object> templateParams = Maps.newHashMapWithExpectedSize(4);
        templateParams.put("projectName", projectName);
        templateParams.put("serialNumber", serialNumber);
        templateParams.put("workItemName", workItemName);
        // 工作项通知跳转到项目详情的任务页，与前端项目详情路由及 tabs 参数保持一致
        templateParams.put("route", "/pms/pm/project/detail/" + projectId + "?tabs=task");
        try {
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(receiverUserId).setTemplateCode(WORK_ITEM_ASSIGNED)
                    .setTemplateParams(templateParams));
        } catch (RuntimeException ex) {
            log.warn("[sendWorkItemAssignedNotify][向用户({})发送工作项指派站内信失败，参数为({})]",
                    receiverUserId, templateParams, ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemName(PmsWorkItemNameUpdateReqVO updateReqVO, Long userId) {
        // 1. 校验工作项可编辑
        PmsWorkItemDO workItem = getWritableWorkItem(updateReqVO.getId(), userId);

        // 2.1 更新工作项名称
        String name = updateReqVO.getName().trim();
        workItemMapper.updateById(BeanUtils.toBean(updateReqVO, PmsWorkItemDO.class)
                .setId(workItem.getId()).setName(name));
        // 2.2 记录工作项名称更新动态
        workItemActivityService.createWorkItemActivity(
                workItem.getProjectId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_NAME_UPDATED,
                workItem.getName(), name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemStatus(PmsWorkItemStatusUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(updateReqVO.getId());
        // 1.2 校验工作项处于正常状态
        validateActiveWorkItem(workItem);
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);
        // 1.5 校验目标状态属于当前项目和工作项类型
        PmsWorkItemStatusDO status = workItemStatusService.validateWorkItemStatus(updateReqVO.getStatusId(),
                workItem.getProjectId(), workItem.getType());

        // 2.1 更新看板状态、语义状态和显示顺序
        Integer maxSort = workItemMapper.selectMaxSortByStatusId(status.getId());
        workItemMapper.updateStatusAndSortById(workItem.getId(), status.getId(), status.getStatusType(),
                maxSort == null ? 1 : maxSort + 1);
        // 2.2 记录工作项状态更新动态
        workItemActivityService.createWorkItemActivity(
                project.getId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_STATUS_UPDATED,
                workItemStatusService.getWorkItemStatus(workItem.getStatusId()).getName(), status.getName());
    }

    @Override
    public void updateWorkItemStatusTypeByStatusId(Long statusId, Integer statusType) {
        workItemMapper.updateStatusTypeByStatusId(statusId, statusType);
    }

    @Override
    public Long getWorkItemCountByStatusId(Long statusId) {
        return workItemMapper.selectCountByStatusId(statusId);
    }

    @Override
    public void transferWorkItemStatus(Long sourceStatusId, Long targetStatusId, Integer targetStatus) {
        workItemMapper.updateStatusBySourceStatusId(sourceStatusId, targetStatusId, targetStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemIteration(PmsWorkItemIterationUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(updateReqVO.getId());
        // 1.2 校验工作项处于正常状态
        validateActiveWorkItem(workItem);
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);
        // 1.5 校验项目支持迭代
        if (ObjectUtil.notEqual(PmsProjectTypeEnum.AGILE.getType(), project.getType())) {
            throw exception(WORK_ITEM_ITERATION_INVALID);
        }
        // 1.6 校验目标迭代属于同一项目
        PmsIterationDO iteration = null;
        if (updateReqVO.getIterationId() != null) {
            iteration = iterationService.getIteration(updateReqVO.getIterationId(), userId);
            if (ObjectUtil.notEqual(project.getId(), iteration.getProjectId())) {
                throw exception(WORK_ITEM_ITERATION_INVALID);
            }
        }

        // 2.1 更新所属迭代
        workItemMapper.updateIterationIdById(workItem.getId(), updateReqVO.getIterationId());
        // 2.2 记录工作项迭代更新动态
        workItemActivityService.createWorkItemIterationActivityIfChanged(workItem, userId,
                workItem.getIterationId(), updateReqVO.getIterationId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemSort(PmsWorkItemSortReqVO sortReqVO, Long userId) {
        // 1.1 校验目标状态存在
        PmsWorkItemStatusDO status = workItemStatusService.getWorkItemStatus(sortReqVO.getStatusId());
        // 1.2 校验目标状态所属项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(status.getProjectId(), userId);
        // 1.3 校验项目处于进行中
        validateActiveProject(project);

        // 2.1 校验排序列表中的工作项编号不重复
        Set<Long> workItemIds = new LinkedHashSet<>(sortReqVO.getWorkItemIds());
        List<PmsWorkItemDO> workItems = workItemMapper.selectListByStatusId(status.getId());
        if (workItemIds.size() != sortReqVO.getWorkItemIds().size()) {
            throw exception(WORK_ITEM_SORT_INVALID);
        }
        // 2.2 校验排序列表包含目标状态下的全部工作项
        if (workItems.size() != workItemIds.size()
                || ObjectUtil.notEqual(workItemIds, convertSet(workItems, PmsWorkItemDO::getId))) {
            throw exception(WORK_ITEM_SORT_INVALID);
        }
        for (PmsWorkItemDO workItem : workItems) {
            // 2.3 校验工作项属于目标项目
            if (ObjectUtil.notEqual(status.getProjectId(), workItem.getProjectId())) {
                throw exception(WORK_ITEM_SORT_INVALID);
            }
            // 2.4 校验工作项类型与目标状态一致
            if (ObjectUtil.notEqual(status.getWorkItemType(), workItem.getType())) {
                throw exception(WORK_ITEM_SORT_INVALID);
            }
            // 2.5 校验工作项属于目标状态
            if (ObjectUtil.notEqual(status.getId(), workItem.getStatusId())) {
                throw exception(WORK_ITEM_SORT_INVALID);
            }
        }

        // 3. 按请求顺序更新列内显示顺序
        Map<Long, Integer> sortMap = new LinkedHashMap<>();
        for (int index = 0; index < sortReqVO.getWorkItemIds().size(); index++) {
            sortMap.put(sortReqVO.getWorkItemIds().get(index), index + 1);
        }
        List<Long> sortedWorkItemIds = new ArrayList<>(workItemIds);
        Collections.sort(sortedWorkItemIds);
        workItemMapper.updateBatch(convertList(sortedWorkItemIds,
                workItemId -> new PmsWorkItemDO().setId(workItemId).setSort(sortMap.get(workItemId))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemPlanningSort(PmsWorkItemPlanningSortReqVO sortReqVO, Long userId) {
        // 1.1 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(sortReqVO.getProjectId(), userId);
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验目标迭代属于当前项目
        if (sortReqVO.getIterationId() != null) {
            PmsIterationDO iteration = iterationService.getIteration(sortReqVO.getIterationId(), userId);
            if (ObjectUtil.notEqual(project.getId(), iteration.getProjectId())) {
                throw exception(WORK_ITEM_SORT_INVALID);
            }
        }

        // 1.4 校验请求包含目标区域内的全部一级工作项
        Set<Long> workItemIds = new LinkedHashSet<>(sortReqVO.getWorkItemIds());
        List<PmsWorkItemDO> workItems = workItemMapper.selectPlanningList(
                sortReqVO.getProjectId(), sortReqVO.getIterationId());
        if (workItemIds.size() != sortReqVO.getWorkItemIds().size() || workItems.size() != workItemIds.size()
                || ObjectUtil.notEqual(workItemIds, convertSet(workItems, PmsWorkItemDO::getId))) {
            throw exception(WORK_ITEM_SORT_INVALID);
        }

        // 3. Backlog 按当前用户保存个人顺序
        if (sortReqVO.getIterationId() == null) {
            workItemUserSortService.updateWorkItemUserSort(
                    sortReqVO.getProjectId(), sortReqVO.getWorkItemIds(), userId);
            return;
        }

        // 4. 迭代内顺序由项目成员共享
        Map<Long, Integer> sortMap = new LinkedHashMap<>();
        for (int index = 0; index < sortReqVO.getWorkItemIds().size(); index++) {
            sortMap.put(sortReqVO.getWorkItemIds().get(index), index + 1);
        }
        List<Long> sortedWorkItemIds = new ArrayList<>(workItemIds);
        Collections.sort(sortedWorkItemIds);
        workItemMapper.updateBatch(convertList(sortedWorkItemIds,
                workItemId -> new PmsWorkItemDO().setId(workItemId).setSort(sortMap.get(workItemId))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveWorkItem(Long id, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);
        // 1.2 校验工作项处于正常状态
        validateActiveWorkItem(workItem);
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);

        // 2.1 归档工作项
        workItemMapper.updateLifecycleStatusById(id, PmsWorkItemLifecycleStatusEnum.ARCHIVED.getStatus(),
                LocalDateTime.now(), null);
        // 2.2 记录工作项归档动态
        workItemActivityService.createWorkItemActivity(
                project.getId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_ARCHIVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleWorkItem(Long id, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);
        // 1.2 校验工作项不在回收站
        if (PmsWorkItemLifecycleStatusEnum.RECYCLED.getStatus().equals(workItem.getLifecycleStatus())) {
            throw exception(WORK_ITEM_LIFECYCLE_STATUS_INVALID);
        }
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);

        // 2.1 将工作项移入回收站
        workItemMapper.updateLifecycleStatusById(id, PmsWorkItemLifecycleStatusEnum.RECYCLED.getStatus(),
                workItem.getArchiveTime(), LocalDateTime.now());
        // 2.2 记录工作项回收动态
        workItemActivityService.createWorkItemActivity(
                project.getId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_RECYCLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreWorkItem(Long id, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);
        // 1.2 校验工作项已归档或位于回收站
        if (PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus().equals(workItem.getLifecycleStatus())) {
            throw exception(WORK_ITEM_LIFECYCLE_STATUS_INVALID);
        }
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);

        // 2.1 恢复工作项
        workItemMapper.updateLifecycleStatusById(
                id, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus(), null, null);
        // 2.2 记录工作项恢复动态
        workItemActivityService.createWorkItemActivity(
                project.getId(), workItem.getId(), userId, PmsWorkItemActivityContentEnum.WORK_ITEM_RESTORED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkItem(Long id, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);
        // 1.2 校验工作项位于回收站
        if (ObjectUtil.notEqual(PmsWorkItemLifecycleStatusEnum.RECYCLED.getStatus(), workItem.getLifecycleStatus())) {
            throw exception(WORK_ITEM_DELETE_STATUS_INVALID);
        }
        // 1.3 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 1.4 校验项目处于进行中
        validateActiveProject(project);

        // 2.1 解除子工作项和关联需求关系
        workItemMapper.updateParentIdToNullByParentId(id);
        if (PmsWorkItemTypeEnum.REQUIREMENT.getType().equals(workItem.getType())) {
            workItemMapper.updateRelatedRequirementIdToNullByRelatedRequirementId(id);
        }
        // 2.2 删除参与人、个人排序、评论、工时和动态
        workItemMemberMapper.deleteByWorkItemId(id);
        workItemUserSortService.deleteWorkItemUserSortByWorkItemId(id);
        workItemCommentService.deleteWorkItemCommentListByWorkItemId(id);
        workItemWorkLogService.deleteWorkItemWorkLogListByWorkItemId(id);
        workItemActivityService.deleteWorkItemActivityListByWorkItemId(id);
        // 2.3 删除工作项主记录
        workItemMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkItemListByProjectId(Long projectId) {
        // 1. 删除项目工作项动态、评论、个人排序和参与人
        workItemActivityService.deleteWorkItemActivityListByProjectId(projectId);
        List<PmsWorkItemDO> workItems = workItemMapper.selectListByProjectId(projectId);
        workItemCommentService.deleteWorkItemCommentListByWorkItemIds(convertSet(workItems, PmsWorkItemDO::getId));
        workItemWorkLogService.deleteWorkItemWorkLogListByProjectId(projectId);
        workItemMemberMapper.deleteByProjectId(projectId);
        workItemUserSortService.deleteWorkItemUserSortByProjectId(projectId);

        // 2. 删除项目工作项和看板状态
        workItemMapper.deleteByProjectId(projectId);
        workItemStatusService.deleteWorkItemStatusListByProjectId(projectId);
    }

    @Override
    public void clearWorkItemIterationId(Long iterationId) {
        workItemMapper.updateIterationIdToNullByIterationId(iterationId);
    }

    @Override
    public PmsWorkItemDO getWorkItem(Long id, Long userId) {
        // 1. 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);

        // 2. 校验当前用户可以访问项目
        projectMemberService.validateProjectReadable(workItem.getProjectId(), userId);
        return workItem;
    }

    @Override
    public PmsWorkItemDO getWritableWorkItem(Long id, Long userId) {
        // 1.1 校验工作项存在
        PmsWorkItemDO workItem = validateWorkItemExists(id);
        // 1.2 校验工作项处于正常状态
        validateActiveWorkItem(workItem);

        // 2.1 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(workItem.getProjectId(), userId);
        // 2.2 校验项目处于进行中
        validateActiveProject(project);
        return workItem;
    }

    @Override
    public PageResult<PmsWorkItemDO> getWorkItemPage(PmsWorkItemPageReqVO pageReqVO, Long userId) {
        // 1.1 校验当前用户可以访问项目
        PmsProjectDO project = projectMemberService.validateProjectReadable(pageReqVO.getProjectId(), userId);
        // 1.2 校验工作项类型可用
        if (pageReqVO.getType() != null) {
            validateWorkItemType(project, pageReqVO.getType());
        }
        if (CollUtil.isNotEmpty(pageReqVO.getTypes())) {
            pageReqVO.getTypes().forEach(type -> validateWorkItemType(project, type));
        }

        // 2. 待规划页面按当前用户的个人顺序分页；
        // 【特殊】个人排序存在"无排序记录时保持默认顺序"的短路语义，难以在 SQL 中等价表达，待规划区数据量为项目内未规划根工作项，因此内存排序后分页
        if (Boolean.TRUE.equals(pageReqVO.getPlanningOnly()) && Boolean.TRUE.equals(pageReqVO.getUnplannedOnly())) {
            List<PmsWorkItemDO> workItems = workItemMapper.selectListByPlanning(pageReqVO);
            workItemUserSortService.sortWorkItemList(workItems, pageReqVO.getProjectId(), userId);
            int fromIndex = Math.min((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize(), workItems.size());
            int toIndex = Math.min(fromIndex + pageReqVO.getPageSize(), workItems.size());
            return new PageResult<>(new ArrayList<>(workItems.subList(fromIndex, toIndex)), (long) workItems.size());
        }

        // 3. 查询其他工作项分页
        return workItemMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<Long, Map<Integer, Long>> getProjectWorkItemStatusCountMap(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        return workItemMapper.selectStatusCountMapByProjectIds(projectIds);
    }

    @Override
    public List<PmsWorkItemDO> getWorkItemList(PmsWorkItemPageReqVO pageReqVO, Long userId) {
        // 1. 校验当前用户可以访问项目
        projectMemberService.validateProjectReadable(pageReqVO.getProjectId(), userId);

        // 2. 查询全部符合条件的工作项，工作项类型由查询条件直接过滤
        return workItemMapper.selectList(pageReqVO);
    }

    @Override
    public List<PmsWorkItemDO> getActiveWorkItemListByProjectId(Long projectId) {
        return workItemMapper.selectActiveListByProjectId(projectId);
    }

    @Override
    public List<PmsWorkItemDO> getActiveWorkItemListByIterationId(Long iterationId) {
        return workItemMapper.selectListByIterationId(iterationId);
    }

    @Override
    public Map<Long, Map<Integer, Long>> getIterationWorkItemStatusCountMap(Collection<Long> iterationIds) {
        if (CollUtil.isEmpty(iterationIds)) {
            return Collections.emptyMap();
        }
        return workItemMapper.selectStatusCountMapByIterationIds(iterationIds);
    }

    @Override
    public PageResult<PmsWorkItemDO> getAssignedWorkItemPage(PmsWorkbenchPageReqVO pageReqVO,
                                                              Collection<Long> projectIds, Long userId) {
        if (CollUtil.isEmpty(projectIds)) {
            return PageResult.empty();
        }
        return workItemMapper.selectWorkbenchPage(pageReqVO, projectIds, userId, pageReqVO.getProjectId(),
                pageReqVO.getType(), pageReqVO.getName(), pageReqVO.getStatus(), pageReqVO.getPriority(),
                pageReqVO.getIterationId(), pageReqVO.getEndTime(),
                PmsWorkItemStatusTypeEnum.COMPLETED.getType());
    }

    @Override
    public Map<Integer, Long> getAssignedWorkItemTypeCountMap(PmsWorkbenchPageReqVO pageReqVO,
                                                              Collection<Long> projectIds, Long userId) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        return workItemMapper.selectWorkbenchTypeCountMap(pageReqVO, projectIds, userId,
                PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus(),
                PmsWorkItemStatusTypeEnum.COMPLETED.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public PmsWorkItemImportRespVO importWorkItemList(Long projectId, Integer workItemType,
                                                      List<PmsWorkItemImportExcelVO> importRows, Long userId) {
        // 1. 参数校验
        PmsProjectDO project = projectMemberService.validateProjectWritable(projectId, userId);
        validateActiveProject(project);
        validateWorkItemType(project, workItemType);

        // 2. 查询状态和标签
        Map<String, Long> statusIdMap = convertMap(workItemStatusService.getWorkItemStatusList(projectId, workItemType),
                PmsWorkItemStatusDO::getName, PmsWorkItemStatusDO::getId);
        Map<String, Long> labelIdMap = convertMap(workItemLabelService.getWorkItemLabelList((String) null),
                PmsWorkItemLabelDO::getName, PmsWorkItemLabelDO::getId);

        // 3. 遍历，逐个导入
        int successCount = 0;
        Map<Integer, String> failureReasons = new LinkedHashMap<>();
        for (int index = 0; index < importRows.size(); index++) {
            PmsWorkItemImportExcelVO importItem = importRows.get(index);
            int rowNumber = index + 2;
            try {
                // 3.1 转换并校验工作项数据
                PmsWorkItemSaveReqVO saveReqVO = parseImportData(importItem, projectId, workItemType,
                        statusIdMap, labelIdMap);
                Long workItemId = createWorkItem(saveReqVO, userId);
                // 3.2 更新指定状态
                if (importItem.getStatusId() != null) {
                    updateWorkItemStatus(new PmsWorkItemStatusUpdateReqVO()
                            .setId(workItemId).setStatusId(importItem.getStatusId()), userId);
                }
                // 3.3 记录工时
                if (importItem.getActualHours() != null) {
                    workItemWorkLogService.createWorkItemWorkLog(new PmsWorkItemWorkLogSaveReqVO()
                            .setWorkItemId(workItemId).setActualHours(importItem.getActualHours())
                            .setRemainingHours(importItem.getRemainingHours() == null
                                    ? 0 : importItem.getRemainingHours()), userId);
                }
                successCount++;
            } catch (Exception ex) {
                failureReasons.put(rowNumber, StrUtil.blankToDefault(ex.getMessage(), "数据校验失败"));
            }
        }
        return PmsWorkItemImportRespVO.builder().successCount(successCount).failureReasons(failureReasons).build();
    }

    /**
     * 将 Excel 导入行转换为工作项保存数据
     */
    private PmsWorkItemSaveReqVO parseImportData(PmsWorkItemImportExcelVO importItem, Long projectId, Integer workItemType,
                                                 Map<String, Long> statusIdMap, Map<String, Long> labelIdMap) {
        // 1.1 解析导入字段
        importItem.setPriority(importItem.getPriority() == null ? PmsWorkItemPriorityEnum.NONE.getPriority() : importItem.getPriority())
                .setProgress(importItem.getProgress() == null ? 0 : importItem.getProgress())
                .setEstimatedHours(importItem.getEstimatedHours() == null ? 0 : importItem.getEstimatedHours())
                .setWorkItemType(workItemType);
        // 1.2 解析状态
        if (StrUtil.isNotBlank(importItem.getStatusName())) {
            Long statusId = statusIdMap.get(importItem.getStatusName());
            Assert.notNull(statusId, "状态不存在或不属于当前工作项类型：" + importItem.getStatusName());
            importItem.setStatusId(statusId);
        }
        // 1.3 解析标签
        importItem.setLabelIds(new ArrayList<>(convertSet(importItem.getLabels(), labelName -> {
            Long labelId = labelIdMap.get(labelName);
            Assert.notNull(labelId, "标签不存在：" + labelName);
            return labelId;
        })));
        // 1.4 校验导入数据
        ValidationUtils.validate(importItem);

        // 2. 构造并校验工作项
        PmsWorkItemSaveReqVO saveReqVO = BeanUtils.toBean(importItem, PmsWorkItemSaveReqVO.class)
                .setProjectId(projectId).setType(workItemType);
        ValidationUtils.validate(saveReqVO);
        return saveReqVO;
    }

    @Override
    public List<PmsWorkItemBoardRespVO> getWorkItemBoard(PmsWorkItemBoardReqVO queryReqVO, Long userId) {
        // 1.1 校验当前用户可以访问项目
        projectMemberService.validateProjectReadable(queryReqVO.getProjectId(), userId);
        // 1.2 查询看板状态和符合条件的工作项
        List<PmsWorkItemStatusDO> statuses = workItemStatusService
                .getWorkItemStatusList(queryReqVO.getProjectId(), queryReqVO.getType());
        List<PmsWorkItemBoardDO> boards = workItemStatusService
                .getWorkItemBoardList(queryReqVO.getProjectId(), queryReqVO.getType());
        Map<Long, List<PmsWorkItemDO>> workItemListMap = new LinkedHashMap<>();
        for (PmsWorkItemDO workItem : workItemMapper.selectRootList(queryReqVO)) {
            workItemListMap.computeIfAbsent(workItem.getStatusId(), key -> new ArrayList<>()).add(workItem);
        }

        // 3. 按独立看板列顺序组装状态和工作项，未放入看板的状态不展示
        Map<String, List<PmsWorkItemStatusDO>> statusListMap = new LinkedHashMap<>();
        for (PmsWorkItemStatusDO status : statuses) {
            // 空看板状态不属于任何列，不参与看板展示
            if (StrUtil.isBlank(status.getBoardName())) {
                continue;
            }
            statusListMap.computeIfAbsent(status.getBoardName(), key -> new ArrayList<>()).add(status);
        }
        List<PmsWorkItemBoardRespVO> result = new ArrayList<>();
        for (PmsWorkItemBoardDO board : boards) {
            PmsWorkItemBoardRespVO column = new PmsWorkItemBoardRespVO().setId(board.getId())
                    .setName(board.getName()).setStatuses(new ArrayList<>()).setItems(new ArrayList<>());
            for (PmsWorkItemStatusDO status : statusListMap.getOrDefault(board.getName(), Collections.emptyList())) {
                column.getStatuses().add(BeanUtils.toBean(status, PmsWorkItemStatusRespVO.class));
                column.getItems().addAll(BeanUtils.toBean(workItemListMap.getOrDefault(
                        status.getId(), Collections.emptyList()), PmsWorkItemRespVO.class));
            }
            result.add(column);
        }
        return result;
    }

    @Override
    public Map<Long, List<Long>> getWorkItemMemberUserIdListMap(Collection<Long> workItemIds) {
        if (CollUtil.isEmpty(workItemIds)) {
            return Collections.emptyMap();
        }
        List<PmsWorkItemMemberDO> members = workItemMemberMapper.selectListByWorkItemIds(workItemIds);
        return convertMultiMap(members, PmsWorkItemMemberDO::getWorkItemId, PmsWorkItemMemberDO::getUserId);
    }

    @Override
    public List<PmsWorkItemDO> getWorkItemList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workItemMapper.selectByIds(ids);
    }

    /**
     * 校验并获得工作项
     *
     * @param id 工作项编号
     * @return 工作项
     */
    private PmsWorkItemDO validateWorkItemExists(Long id) {
        PmsWorkItemDO workItem = workItemMapper.selectById(id);
        if (workItem == null) {
            throw exception(WORK_ITEM_NOT_EXISTS);
        }
        return workItem;
    }

    /**
     * 校验工作项处于正常状态
     *
     * @param workItem 工作项
     */
    private void validateActiveWorkItem(PmsWorkItemDO workItem) {
        if (ObjectUtil.notEqual(PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus(), workItem.getLifecycleStatus())) {
            throw exception(WORK_ITEM_LIFECYCLE_STATUS_INVALID);
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
     * 校验项目支持工作项类型
     *
     * @param project 项目
     * @param workItemType 工作项类型
     */
    private void validateWorkItemType(PmsProjectDO project, Integer workItemType) {
        boolean valid = PmsProjectTypeEnum.GENERAL.getType().equals(project.getType())
                ? PmsWorkItemTypeEnum.TASK.getType().equals(workItemType)
                : PmsWorkItemTypeEnum.valueOf(workItemType) != null;
        if (!valid) {
            throw exception(WORK_ITEM_TYPE_INVALID);
        }
    }

    /**
     * 校验工作项的迭代、父工作项、关联需求和缺陷类型
     *
     * @param id 当前工作项编号
     * @param project 项目
     * @param workItemType 工作项类型
     * @param reqVO 工作项信息
     * @param userId 用户编号
     */
    private void validateWorkItemRelations(Long id, PmsProjectDO project, Integer workItemType,
                                           PmsWorkItemSaveReqVO reqVO, Long userId) {
        if (reqVO.getIterationId() != null) {
            PmsIterationDO iteration = iterationService.getIteration(reqVO.getIterationId(), userId);
            if (ObjectUtil.notEqual(project.getId(), iteration.getProjectId())
                    || ObjectUtil.notEqual(PmsProjectTypeEnum.AGILE.getType(), project.getType())) {
                throw exception(WORK_ITEM_ITERATION_INVALID);
            }
        }
        validateParentWorkItem(id, project.getId(), workItemType, reqVO.getParentId());
        validateRelatedRequirement(project.getId(), workItemType, reqVO.getRelatedRequirementId());
        boolean defectWorkItem = PmsWorkItemTypeEnum.DEFECT.getType().equals(workItemType);
        if (defectWorkItem && reqVO.getDefectType() == null
                || !defectWorkItem && reqVO.getDefectType() != null) {
            throw exception(WORK_ITEM_DEFECT_TYPE_INVALID);
        }
    }

    /**
     * 校验父工作项属于同一项目和类型，且不会形成循环
     *
     * @param id 当前工作项编号
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param parentId 父工作项编号
     */
    private void validateParentWorkItem(Long id, Long projectId, Integer workItemType, Long parentId) {
        Long currentParentId = parentId;
        Set<Long> visitedIds = new HashSet<>();
        for (int index = 0; index < Short.MAX_VALUE && currentParentId != null; index++) {
            if (ObjectUtil.equal(currentParentId, id) || !visitedIds.add(currentParentId)) {
                throw exception(WORK_ITEM_PARENT_INVALID);
            }
            PmsWorkItemDO parent = workItemMapper.selectById(currentParentId);
            if (parent == null || ObjectUtil.notEqual(projectId, parent.getProjectId())
                    || ObjectUtil.notEqual(workItemType, parent.getType())) {
                throw exception(WORK_ITEM_PARENT_INVALID);
            }
            // 工作项详情只展示单级子项，父工作项不能再挂在其他工作项下
            if (parent.getParentId() != null) {
                throw exception(WORK_ITEM_PARENT_INVALID);
            }
            validateActiveWorkItem(parent);
            currentParentId = parent.getParentId();
        }
        if (currentParentId != null) {
            throw exception(WORK_ITEM_PARENT_INVALID);
        }
    }

    /**
     * 校验关联需求属于同一项目，且当前工作项不是需求
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param requirementId 需求编号
     */
    private void validateRelatedRequirement(Long projectId, Integer workItemType, Long requirementId) {
        if (requirementId == null) {
            return;
        }
        PmsWorkItemDO requirement = workItemMapper.selectById(requirementId);
        if (PmsWorkItemTypeEnum.REQUIREMENT.getType().equals(workItemType) || requirement == null
                || ObjectUtil.notEqual(projectId, requirement.getProjectId())
                || ObjectUtil.notEqual(PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus(), requirement.getLifecycleStatus())
                || ObjectUtil.notEqual(PmsWorkItemTypeEnum.REQUIREMENT.getType(), requirement.getType())) {
            throw exception(WORK_ITEM_RELATED_REQUIREMENT_INVALID);
        }
    }

    /**
     * 校验负责人和参与人都是项目成员
     *
     * @param projectId 项目编号
     * @param reqVO 工作项信息
     * @param userId 操作人用户编号
     */
    private void validateWorkItemMembers(Long projectId, PmsWorkItemSaveReqVO reqVO, Long userId) {
        Set<Long> userIds = new LinkedHashSet<>();
        if (reqVO.getAssigneeUserId() != null) {
            userIds.add(reqVO.getAssigneeUserId());
        }
        userIds.addAll(getMemberUserIds(reqVO, userId));
        projectMemberService.validateProjectMemberList(projectId, userIds);
    }

    /**
     * 获得工作项参与人，未选择参与人时默认使用操作人
     *
     * @param reqVO 工作项信息
     * @param userId 操作人用户编号
     * @return 参与人用户编号集合
     */
    private Set<Long> getMemberUserIds(PmsWorkItemSaveReqVO reqVO, Long userId) {
        return CollUtil.isEmpty(reqVO.getMemberUserIds()) ? Collections.singleton(userId)
                : new LinkedHashSet<>(reqVO.getMemberUserIds());
    }

    /**
     * 保存工作项参与人关系
     *
     * @param workItemId 工作项编号
     * @param projectId 项目编号
     * @param memberUserIds 参与人用户编号列表
     */
    private void saveWorkItemMembers(Long workItemId, Long projectId, Collection<Long> memberUserIds) {
        if (CollUtil.isEmpty(memberUserIds)) {
            return;
        }
        workItemMemberMapper.insertBatch(convertList(memberUserIds,
                memberUserId -> new PmsWorkItemMemberDO().setProjectId(projectId)
                        .setWorkItemId(workItemId).setUserId(memberUserId)));
    }

}

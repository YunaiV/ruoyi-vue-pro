package cn.iocoder.yudao.module.pms.service.pm.iteration;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationStartReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.iteration.PmsIterationMapper;
import cn.iocoder.yudao.module.pms.enums.pm.iteration.PmsIterationStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemActivityService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemWorkLogService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sum;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.ITERATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.ITERATION_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;

/**
 * PMS 项目迭代 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsIterationServiceImpl implements PmsIterationService {

    /**
     * 完成趋势统计天数
     */
    private static final int TREND_DAYS = 14;
    /**
     * 最近活动最大展示数量
     */
    private static final int RECENT_ACTIVITY_LIMIT = 50;

    @Resource
    private PmsIterationMapper iterationMapper;

    @Resource
    private PmsProjectMemberService projectMemberService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsWorkItemActivityService workItemActivityService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemWorkLogService workItemWorkLogService;

    @Override
    public Long createIteration(PmsIterationSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验项目可编辑
        validateProjectWritable(saveReqVO.getProjectId(), userId);
        // 1.2 校验负责人
        validateOwnerUser(saveReqVO.getProjectId(), saveReqVO.getOwnerUserId());

        // 2. 创建未开始的迭代
        Integer maxSort = iterationMapper.selectMaxSortByProjectId(saveReqVO.getProjectId());
        PmsIterationDO iteration = BeanUtils.toBean(saveReqVO, PmsIterationDO.class)
                .setStatus(PmsIterationStatusEnum.PLANNED.getStatus())
                .setSort(maxSort == null ? 1 : maxSort + 1);
        iterationMapper.insert(iteration);
        return iteration.getId();
    }

    @Override
    public void updateIteration(PmsIterationSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验迭代存在
        PmsIterationDO iteration = validateIterationExists(saveReqVO.getId());
        // 1.2 校验项目可编辑
        validateProjectWritable(iteration.getProjectId(), userId);
        // 1.3 校验负责人
        validateOwnerUser(iteration.getProjectId(), saveReqVO.getOwnerUserId());

        // 2. 更新迭代基本信息
        iterationMapper.updateById(BeanUtils.toBean(saveReqVO, PmsIterationDO.class)
                .setProjectId(null));
    }

    @Override
    public void startIteration(PmsIterationStartReqVO startReqVO, Long userId) {
        // 1.1 校验迭代存在
        PmsIterationDO iteration = validateIterationExists(startReqVO.getId());
        // 1.2 校验项目可编辑
        validateProjectWritable(iteration.getProjectId(), userId);
        // 1.3 校验迭代处于未开始状态
        if (ObjectUtil.notEqual(PmsIterationStatusEnum.PLANNED.getStatus(), iteration.getStatus())) {
            throw exception(ITERATION_STATUS_INVALID);
        }

        // 2. 开始迭代并保存周期
        iterationMapper.updateById(BeanUtils.toBean(startReqVO, PmsIterationDO.class)
                .setId(iteration.getId()).setProjectId(null).setStatus(PmsIterationStatusEnum.ACTIVE.getStatus()));
    }

    @Override
    public void completeIteration(Long id, Long userId) {
        // 1.1 校验迭代存在
        PmsIterationDO iteration = validateIterationExists(id);
        // 1.2 校验项目可编辑
        validateProjectWritable(iteration.getProjectId(), userId);
        // 1.3 校验迭代处于进行中状态
        if (ObjectUtil.notEqual(PmsIterationStatusEnum.ACTIVE.getStatus(), iteration.getStatus())) {
            throw exception(ITERATION_STATUS_INVALID);
        }

        // 2. 完成迭代
        iterationMapper.updateById(new PmsIterationDO().setId(id)
                .setStatus(PmsIterationStatusEnum.COMPLETED.getStatus()).setFinishTime(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIteration(Long id, Long userId) {
        // 1.1 校验迭代存在
        PmsIterationDO iteration = validateIterationExists(id);
        // 1.2 校验项目可编辑
        validateProjectWritable(iteration.getProjectId(), userId);

        // 2. 解除工作项的所属迭代关系后删除迭代
        workItemService.clearWorkItemIterationId(id);
        iterationMapper.deleteById(id);
    }

    @Override
    public void deleteIterationListByProjectId(Long projectId) {
        iterationMapper.deleteByProjectId(projectId);
    }

    @Override
    public PmsIterationDO getIteration(Long id, Long userId) {
        // 1. 校验迭代存在
        PmsIterationDO iteration = validateIterationExists(id);

        // 2. 校验项目读取权限
        projectMemberService.validateProjectReadable(iteration.getProjectId(), userId);
        return iteration;
    }

    @Override
    public PmsIterationOverviewRespVO getIterationOverview(Long id, Long userId) {
        // 1.1 查询可访问的迭代
        PmsIterationDO iteration = getIteration(id, userId);
        // 1.2 查询迭代关联的工作项
        List<PmsWorkItemDO> workItems = workItemService.getActiveWorkItemListByIterationId(id);

        // 2. 统计状态、类型和完成趋势
        int pendingCount = CollUtil.count(workItems, item ->
                PmsWorkItemStatusTypeEnum.PENDING.getType().equals(item.getStatus()));
        int processingCount = CollUtil.count(workItems, item ->
                PmsWorkItemStatusTypeEnum.PROCESSING.getType().equals(item.getStatus()));
        int completedCount = CollUtil.count(workItems, item ->
                PmsWorkItemStatusTypeEnum.COMPLETED.getType().equals(item.getStatus()));
        Map<Integer, Integer> typeCountMap = new LinkedHashMap<>();
        Map<Integer, Map<Integer, Integer>> typeStatusCountMap = new LinkedHashMap<>();
        workItems.forEach(item -> {
            typeCountMap.merge(item.getType(), 1, Integer::sum);
            typeStatusCountMap.computeIfAbsent(item.getType(), key -> new LinkedHashMap<>())
                    .merge(item.getStatus(), 1, Integer::sum);
        });

        // 3. 计算状态趋势、燃尽数据和最近活动
        int progress = CollUtil.isEmpty(workItems) ? 0 : completedCount * 100 / workItems.size();
        return new PmsIterationOverviewRespVO().setTotalCount(workItems.size()).setPendingCount(pendingCount)
                .setProcessingCount(processingCount).setCompletedCount(completedCount).setProgress(progress)
                .setTypeCountMap(typeCountMap).setTypeStatusCountMap(typeStatusCountMap)
                .setStatusTrends(buildStatusTrends(workItems))
                .setBurnDowns(buildBurnDowns(iteration, workItems)).setRecentActivities(buildRecentActivities(workItems));
    }

    /**
     * 构建迭代内工作项的最近活动列表
     *
     * @param workItems 工作项列表
     * @return 最近活动列表
     */
    private List<PmsIterationOverviewRespVO.ActivityItem> buildRecentActivities(List<PmsWorkItemDO> workItems) {
        Map<Long, PmsWorkItemDO> workItemMap = convertMap(workItems, PmsWorkItemDO::getId);
        List<PmsWorkItemActivityDO> activities = workItemActivityService.getWorkItemActivityListByWorkItemIds(
                convertSet(workItems, PmsWorkItemDO::getId), RECENT_ACTIVITY_LIMIT);
        return convertList(activities, activity -> {
            PmsWorkItemDO workItem = workItemMap.get(activity.getWorkItemId());
            return new PmsIterationOverviewRespVO.ActivityItem().setId(activity.getId())
                    .setWorkItemId(activity.getWorkItemId()).setWorkItemSerialNumber(workItem.getSerialNumber())
                    .setWorkItemName(workItem.getName()).setOperatorUserId(activity.getOperatorUserId())
                    .setContent(activity.getContent()).setCreateTime(activity.getCreateTime());
        });
    }

    /**
     * 构建最近固定天数的每日状态趋势
     *
     * @param workItems 工作项列表
     * @return 状态趋势列表
     */
    private List<PmsIterationOverviewRespVO.TrendItem> buildStatusTrends(List<PmsWorkItemDO> workItems) {
        LocalDate beginDate = LocalDate.now().minusDays(TREND_DAYS - 1L);
        List<PmsIterationOverviewRespVO.TrendItem> statusTrends = new ArrayList<>();
        for (int index = 0; index < TREND_DAYS; index++) {
            LocalDate date = beginDate.plusDays(index);
            int pendingCount = CollUtil.count(workItems, item ->
                    PmsWorkItemStatusTypeEnum.PENDING.getType().equals(item.getStatus())
                            && item.getUpdateTime() != null && date.equals(item.getUpdateTime().toLocalDate()));
            int processingCount = CollUtil.count(workItems, item ->
                    PmsWorkItemStatusTypeEnum.PROCESSING.getType().equals(item.getStatus())
                            && item.getUpdateTime() != null && date.equals(item.getUpdateTime().toLocalDate()));
            int completedCount = CollUtil.count(workItems, item ->
                    PmsWorkItemStatusTypeEnum.COMPLETED.getType().equals(item.getStatus())
                            && item.getUpdateTime() != null && date.equals(item.getUpdateTime().toLocalDate()));
            statusTrends.add(new PmsIterationOverviewRespVO.TrendItem().setDate(date.toString())
                    .setPendingCount(pendingCount).setProcessingCount(processingCount)
                    .setCompletedCount(completedCount));
        }
        return statusTrends;
    }

    /**
     * 根据迭代周期和工时登记记录构建燃尽数据
     *
     * @param iteration 迭代
     * @param workItems 工作项列表
     * @return 燃尽数据列表
     */
    private List<PmsIterationOverviewRespVO.BurnDownItem> buildBurnDowns(PmsIterationDO iteration,
                                                                          List<PmsWorkItemDO> workItems) {
        // 1. 未配置完整迭代周期时，不生成燃尽数据
        if (iteration.getStartTime() == null || iteration.getEndTime() == null) {
            return new ArrayList<>();
        }
        // 2. 汇总迭代工作项的预估工时和每日实际投入工时
        int totalEstimatedHours = (int) sum(workItems,
                item -> item.getEstimatedHours() == null ? 0 : item.getEstimatedHours());
        Map<LocalDate, Integer> actualHoursByDate = new LinkedHashMap<>();
        List<Long> workItemIds = convertList(workItems, PmsWorkItemDO::getId);
        workItemWorkLogService.getWorkItemWorkLogListByWorkItemIds(workItemIds).forEach(workLog -> {
            if (workLog.getCreateTime() == null || workLog.getActualHours() == null) {
                return;
            }
            LocalDate logDate = workLog.getCreateTime().toLocalDate();
            actualHoursByDate.merge(logDate, workLog.getActualHours(), Integer::sum);
        });

        // 3. 生成限定长度的日期轴，并逐日计算理想和实际剩余工时
        LocalDate beginDate = iteration.getStartTime().toLocalDate();
        LocalDate endDate = iteration.getEndTime().toLocalDate();
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(beginDate, endDate));
        List<PmsIterationOverviewRespVO.BurnDownItem> burnDowns = new ArrayList<>();
        int actualHours = 0;
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long elapsedDays = ChronoUnit.DAYS.between(beginDate, date);
            int idealRemaining = (int) Math.ceil(totalEstimatedHours * (totalDays - elapsedDays) * 1.0D / totalDays);
            actualHours += actualHoursByDate.getOrDefault(date, 0);
            burnDowns.add(new PmsIterationOverviewRespVO.BurnDownItem().setDate(date.toString())
                    .setIdealRemaining(idealRemaining).setActualRemaining(totalEstimatedHours - actualHours));
        }
        return burnDowns;
    }

    @Override
    public PageResult<PmsIterationDO> getIterationPage(PmsIterationPageReqVO pageReqVO, Long userId) {
        // 1. 校验项目读取权限
        projectMemberService.validateProjectReadable(pageReqVO.getProjectId(), userId);

        // 2. 查询迭代分页
        return iterationMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<Long, Integer> getIterationProgressMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        // 1. 批量聚合迭代关联的正常工作项状态数量
        Map<Long, Map<Integer, Long>> iterationStatusCountMap = workItemService.getIterationWorkItemStatusCountMap(ids);

        // 2. 计算每个迭代的完成进度
        Map<Long, Integer> progressMap = new LinkedHashMap<>();
        ids.forEach(id -> {
            Map<Integer, Long> statusCountMap = iterationStatusCountMap.getOrDefault(id, Collections.emptyMap());
            long totalCount = statusCountMap.values().stream().mapToLong(Long::longValue).sum();
            long completedCount = statusCountMap.getOrDefault(PmsWorkItemStatusTypeEnum.COMPLETED.getType(), 0L);
            progressMap.put(id, totalCount == 0 ? 0 : (int) (completedCount * 100 / totalCount));
        });
        return progressMap;
    }

    @Override
    public List<PmsIterationDO> getIterationList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return iterationMapper.selectByIds(ids);
    }

    @Override
    public PageResult<PmsIterationDO> getUncompletedIterationPage(PmsWorkbenchPageReqVO pageReqVO,
                                                                  Collection<Long> projectIds, Long ownerUserId) {
        if (CollUtil.isEmpty(projectIds)) {
            return PageResult.empty();
        }
        return iterationMapper.selectWorkbenchPage(pageReqVO, projectIds, pageReqVO.getProjectId(),
                pageReqVO.getName(), pageReqVO.getStatus(), pageReqVO.getEndTime(),
                PmsIterationStatusEnum.COMPLETED.getStatus(), ownerUserId);
    }

    @Override
    public Long getUncompletedIterationCount(PmsWorkbenchPageReqVO pageReqVO,
                                             Collection<Long> projectIds, Long ownerUserId) {
        if (CollUtil.isEmpty(projectIds)) {
            return 0L;
        }
        return iterationMapper.selectWorkbenchCount(projectIds, pageReqVO.getProjectId(), pageReqVO.getName(),
                pageReqVO.getStatus(), pageReqVO.getEndTime(),
                PmsIterationStatusEnum.COMPLETED.getStatus(), ownerUserId);
    }

    @Override
    public Map<Long, Map<Integer, Long>> getProjectIterationStatusCountMap(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        return iterationMapper.selectStatusCountMapByProjectIds(projectIds);
    }

    /**
     * 校验迭代存在
     *
     * @param id 迭代编号
     * @return 迭代
     */
    private PmsIterationDO validateIterationExists(Long id) {
        PmsIterationDO iteration = iterationMapper.selectById(id);
        if (iteration == null) {
            throw exception(ITERATION_NOT_EXISTS);
        }
        return iteration;
    }

    /**
     * 校验项目可编辑且处于进行中
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    private void validateProjectWritable(Long projectId, Long userId) {
        PmsProjectDO project = projectMemberService.validateProjectWritable(projectId, userId);
        if (ObjectUtil.notEqual(PmsProjectStatusEnum.ACTIVE.getStatus(), project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }
    }

    /**
     * 校验迭代负责人是项目成员
     *
     * @param projectId 项目编号
     * @param ownerUserId 负责人用户编号
     */
    private void validateOwnerUser(Long projectId, Long ownerUserId) {
        if (ownerUserId != null) {
            projectMemberService.validateProjectMemberExists(projectId, ownerUserId);
        }
    }

}

package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSummaryRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemWorkLogMapper;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import javax.annotation.Resource;
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
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_WORK_LOG_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_WORK_LOG_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.WORK_LOG_CREATED;
import static cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.WORK_LOG_UPDATED;

/**
 * PMS 工作项工时记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemWorkLogServiceImpl implements PmsWorkItemWorkLogService {

    @Resource
    private PmsWorkItemWorkLogMapper workLogMapper;

    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsProjectMemberService projectMemberService;
    @Resource
    private PmsWorkItemActivityService workItemActivityService;
    @Resource
    private PmsIterationService iterationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkItemWorkLog(PmsWorkItemWorkLogSaveReqVO saveReqVO, Long userId) {
        // 1. 校验工作项可编辑
        PmsWorkItemDO workItem = workItemService.getWritableWorkItem(saveReqVO.getWorkItemId(), userId);

        // 2.1 创建工时记录
        PmsWorkItemWorkLogDO workLog = BeanUtils.toBean(saveReqVO, PmsWorkItemWorkLogDO.class)
                .setProjectId(workItem.getProjectId()).setDescription(StrUtil.trim(saveReqVO.getDescription()));
        workLogMapper.insert(workLog);
        // 2.2 记录工时创建动态
        workItemActivityService.createWorkItemActivity(
                workItem.getProjectId(), workItem.getId(), userId, WORK_LOG_CREATED, saveReqVO.getActualHours());
        return workLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemWorkLog(PmsWorkItemWorkLogSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验工时记录存在
        PmsWorkItemWorkLogDO workLog = validateWorkLogExists(saveReqVO.getId());
        // 1.2 校验工时记录属于请求工作项
        if (ObjectUtil.notEqual(workLog.getWorkItemId(), saveReqVO.getWorkItemId())) {
            throw exception(WORK_ITEM_WORK_LOG_INVALID);
        }
        // 1.3 校验工作项可编辑
        PmsWorkItemDO workItem = workItemService.getWritableWorkItem(workLog.getWorkItemId(), userId);

        // 2.1 更新投入工时和说明
        workLogMapper.updateForEdit(BeanUtils.toBean(saveReqVO, PmsWorkItemWorkLogDO.class)
                .setId(workLog.getId()).setDescription(StrUtil.trim(saveReqVO.getDescription())));
        // 2.2 记录工时更新动态
        workItemActivityService.createWorkItemActivity(
                workItem.getProjectId(), workItem.getId(), userId, WORK_LOG_UPDATED);
    }

    @Override
    public PmsWorkItemWorkLogDO getWorkItemWorkLog(Long id, Long userId) {
        // 1. 校验工时记录存在
        PmsWorkItemWorkLogDO workLog = validateWorkLogExists(id);
        // 2. 校验工作项可查看
        workItemService.getWorkItem(workLog.getWorkItemId(), userId);
        return workLog;
    }

    @Override
    public PmsWorkItemWorkLogSummaryRespVO getWorkItemWorkLogSummary(Long workItemId, Long userId) {
        // 1.1 校验工作项可查看
        PmsWorkItemDO workItem = workItemService.getWorkItem(workItemId, userId);
        // 1.2 查询工作项工时记录
        List<PmsWorkItemWorkLogDO> workLogs = workLogMapper.selectListByWorkItemId(workItemId);

        // 2. 汇总已登记工时，剩余工时取最新一条登记记录
        int actualHours = workLogs.stream().mapToInt(PmsWorkItemWorkLogDO::getActualHours).sum();
        Integer remainingHours = CollUtil.isNotEmpty(workLogs)
                ? CollUtil.getFirst(workLogs).getRemainingHours() : workItem.getEstimatedHours();
        if (remainingHours == null && workItem.getEstimatedHours() != null) {
            remainingHours = Math.max(workItem.getEstimatedHours() - actualHours, 0);
        }
        List<PmsWorkItemWorkLogRespVO> workLogVOs = convertList(workLogs, workLog ->
                BeanUtils.toBean(workLog, PmsWorkItemWorkLogRespVO.class)
                        .setCreatorUserId(NumberUtils.parseLong(workLog.getCreator())));
        return new PmsWorkItemWorkLogSummaryRespVO().setEstimatedHours(workItem.getEstimatedHours())
                .setActualHours(actualHours).setRemainingHours(remainingHours).setRecords(workLogVOs);
    }

    @Override
    public List<PmsWorkItemWorkLogDO> getWorkItemWorkLogListByWorkItemIds(Collection<Long> workItemIds) {
        if (CollUtil.isEmpty(workItemIds)) {
            return Collections.emptyList();
        }
        return workLogMapper.selectListByWorkItemIds(workItemIds);
    }

    @Override
    public PmsProjectWorkLogReportRespVO getProjectWorkItemWorkLogReport(
            PmsProjectWorkLogReportReqVO reqVO, Long userId) {
        // 1.1 校验项目可访问
        projectMemberService.validateProjectReadable(reqVO.getProjectId(), userId);
        // 1.2 校验工时登记时间范围
        LocalDateTime beginTime = reqVO.getCreateTime()[0];
        LocalDateTime endTime = reqVO.getCreateTime()[1];
        if (endTime.isBefore(beginTime)) {
            throw exception(WORK_ITEM_WORK_LOG_INVALID);
        }

        // 2.1 查询范围内工时和关联工作项
        List<PmsWorkItemWorkLogDO> workLogs = workLogMapper.selectListByProjectIdAndCreateTimeBetween(
                reqVO.getProjectId(), beginTime, endTime);
        Map<Long, PmsWorkItemDO> workItemMap = convertMap(workItemService.getWorkItemList(
                convertSet(workLogs, PmsWorkItemWorkLogDO::getWorkItemId)), PmsWorkItemDO::getId);
        // 2.2 查询迭代并按迭代名称筛选
        Set<Long> iterationIds = convertSet(workItemMap.values(), PmsWorkItemDO::getIterationId);
        iterationIds.remove(null);
        Map<Long, PmsIterationDO> iterationMap = convertMap(iterationService.getIterationList(iterationIds),
                PmsIterationDO::getId);
        if (StrUtil.isNotBlank(reqVO.getIterationName())) {
            workItemMap.entrySet().removeIf(entry -> {
                PmsIterationDO iteration = entry.getValue().getIterationId() != null
                        ? iterationMap.get(entry.getValue().getIterationId()) : null;
                String currentIterationName = iteration != null ? iteration.getName() : "未规划事项";
                return !StrUtil.containsIgnoreCase(currentIterationName, reqVO.getIterationName());
            });
            workLogs.removeIf(workLog -> !workItemMap.containsKey(workLog.getWorkItemId()));
        }

        // 3. 按迭代和工作项汇总每日工时
        Map<Long, List<PmsWorkItemDO>> groupedWorkItems = convertMultiMap(workItemMap.values(),
                item -> item.getIterationId() != null ? item.getIterationId() : 0L);
        Map<Long, List<PmsWorkItemWorkLogDO>> groupedWorkLogs = convertMultiMap(
                workLogs, PmsWorkItemWorkLogDO::getWorkItemId);
        List<PmsProjectWorkLogReportRespVO.Group> groups = new ArrayList<>();
        for (Map.Entry<Long, List<PmsWorkItemDO>> entry : groupedWorkItems.entrySet()) {
            Long iterationId = entry.getKey() != 0L ? entry.getKey() : null;
            List<PmsProjectWorkLogReportRespVO.Item> items = new ArrayList<>();
            for (PmsWorkItemDO workItem : entry.getValue()) {
                Map<String, Integer> dailyHours = new LinkedHashMap<>();
                for (PmsWorkItemWorkLogDO workLog : groupedWorkLogs.getOrDefault(
                        workItem.getId(), Collections.emptyList())) {
                    dailyHours.merge(workLog.getCreateTime().toLocalDate().toString(),
                            workLog.getActualHours(), Integer::sum);
                }
                int totalHours = dailyHours.values().stream().mapToInt(Integer::intValue).sum();
                items.add(new PmsProjectWorkLogReportRespVO.Item().setWorkItemId(workItem.getId())
                        .setSerialNumber(workItem.getSerialNumber()).setName(workItem.getName())
                        .setType(workItem.getType()).setTotalHours(totalHours).setDailyHours(dailyHours));
            }
            items.sort(Comparator.comparingInt(PmsProjectWorkLogReportRespVO.Item::getSerialNumber));
            PmsIterationDO iteration = iterationId != null ? iterationMap.get(iterationId) : null;
            groups.add(new PmsProjectWorkLogReportRespVO.Group().setIterationId(iterationId)
                    .setIterationName(iteration != null ? iteration.getName() : "未规划事项")
                    .setTotalHours(items.stream().mapToInt(PmsProjectWorkLogReportRespVO.Item::getTotalHours).sum())
                    .setItems(items));
        }
        groups.sort((first, second) -> first.getIterationId() == null ? 1
                : second.getIterationId() == null ? -1 : first.getIterationName().compareTo(second.getIterationName()));

        // 4. 返回连续日期轴和总工时
        LocalDate beginDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        List<String> dates = new ArrayList<>();
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date.toString());
        }
        return new PmsProjectWorkLogReportRespVO().setDates(dates)
                .setTotalHours(groups.stream().mapToInt(PmsProjectWorkLogReportRespVO.Group::getTotalHours).sum())
                .setGroups(groups);
    }

    @Override
    public void deleteWorkItemWorkLogListByProjectId(Long projectId) {
        workLogMapper.deleteByProjectId(projectId);
    }

    @Override
    public void deleteWorkItemWorkLogListByWorkItemId(Long workItemId) {
        workLogMapper.deleteByWorkItemId(workItemId);
    }

    /**
     * 校验工时记录存在
     *
     * @param id 工时记录编号
     * @return 工时记录
     */
    private PmsWorkItemWorkLogDO validateWorkLogExists(Long id) {
        PmsWorkItemWorkLogDO workLog = workLogMapper.selectById(id);
        if (workLog == null) {
            throw exception(WORK_ITEM_WORK_LOG_NOT_EXISTS);
        }
        return workLog;
    }

}

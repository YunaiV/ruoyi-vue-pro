package cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemLifecycleStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PmsWorkItemMapper extends BaseMapperX<PmsWorkItemDO> {

    default PageResult<PmsWorkItemDO> selectPage(PmsWorkItemPageReqVO pageReqVO) {
        LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper = buildQueryWrapper(pageReqVO);
        if (Boolean.TRUE.equals(pageReqVO.getPlanningOnly())) {
            queryWrapper.isNull(PmsWorkItemDO::getParentId)
                    .orderByAsc(PmsWorkItemDO::getSort)
                    .orderByDesc(PmsWorkItemDO::getId);
        } else {
            queryWrapper.orderByDesc(PmsWorkItemDO::getPriority)
                    .orderByAsc(PmsWorkItemDO::getSerialNumber)
                    .orderByAsc(PmsWorkItemDO::getId);
        }
        return selectPage(pageReqVO, queryWrapper);
    }

    default List<PmsWorkItemDO> selectRootList(PmsWorkItemPageReqVO pageReqVO) {
        return selectList(buildQueryWrapper(pageReqVO)
                .isNull(PmsWorkItemDO::getParentId)
                .orderByAsc(PmsWorkItemDO::getSort)
                .orderByDesc(PmsWorkItemDO::getId));
    }

    default List<PmsWorkItemDO> selectList(PmsWorkItemPageReqVO pageReqVO) {
        LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper = buildQueryWrapper(pageReqVO);
        if (Boolean.TRUE.equals(pageReqVO.getRootOnly())) {
            queryWrapper.isNull(PmsWorkItemDO::getParentId);
        }
        return selectList(queryWrapper.orderByDesc(PmsWorkItemDO::getPriority)
                .orderByAsc(PmsWorkItemDO::getSerialNumber)
                .orderByAsc(PmsWorkItemDO::getId));
    }

    default Integer selectMaxSerialNumberByProjectId(Long projectId) {
        PmsWorkItemDO workItem = selectLastOne(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .select(PmsWorkItemDO::getSerialNumber)
                .eq(PmsWorkItemDO::getProjectId, projectId)
                .orderByAsc(PmsWorkItemDO::getSerialNumber)
                .orderByAsc(PmsWorkItemDO::getId));
        return workItem != null ? workItem.getSerialNumber() : null;
    }

    /**
     * 统计各项目下不同工作项状态的数量，仅统计生命周期正常的工作项
     *
     * @param projectIds 项目编号集合
     * @return 项目编号 ->（状态 -> 工作项数量）
     */
    default Map<Long, Map<Integer, Long>> selectStatusCountMapByProjectIds(Collection<Long> projectIds) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsWorkItemDO>()
                .select("project_id AS projectId", "status", "COUNT(*) AS count")
                .in("project_id", projectIds)
                .eq("lifecycle_status", PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .groupBy("project_id", "status"));
        Map<Long, Map<Integer, Long>> projectStatusCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long projectId = ((Number) row.get("projectId")).longValue();
            Integer status = ((Number) row.get("status")).intValue();
            projectStatusCountMap.computeIfAbsent(projectId, key -> new LinkedHashMap<>())
                    .put(status, ((Number) row.get("count")).longValue());
        }
        return projectStatusCountMap;
    }

    /**
     * 统计各迭代下不同语义状态的工作项数量
     *
     * @param iterationIds 迭代编号集合
     * @return 迭代编号 ->（语义状态 -> 工作项数量）
     */
    default Map<Long, Map<Integer, Long>> selectStatusCountMapByIterationIds(Collection<Long> iterationIds) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsWorkItemDO>()
                .select("iteration_id AS iterationId", "status", "COUNT(*) AS count")
                .in("iteration_id", iterationIds)
                .eq("lifecycle_status", PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .groupBy("iteration_id", "status"));
        Map<Long, Map<Integer, Long>> iterationStatusCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long iterationId = ((Number) row.get("iterationId")).longValue();
            Integer status = ((Number) row.get("status")).intValue();
            iterationStatusCountMap.computeIfAbsent(iterationId, key -> new LinkedHashMap<>())
                    .put(status, ((Number) row.get("count")).longValue());
        }
        return iterationStatusCountMap;
    }

    default Integer selectMaxSortByStatusId(Long statusId) {
        PmsWorkItemDO workItem = selectLastOne(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .select(PmsWorkItemDO::getSort)
                .eq(PmsWorkItemDO::getStatusId, statusId)
                .orderByAsc(PmsWorkItemDO::getSort)
                .orderByAsc(PmsWorkItemDO::getId));
        return workItem != null ? workItem.getSort() : null;
    }

    default List<PmsWorkItemDO> selectListByStatusId(Long statusId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .eq(PmsWorkItemDO::getStatusId, statusId)
                .eq(PmsWorkItemDO::getLifecycleStatus, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .isNull(PmsWorkItemDO::getParentId)
                .orderByAsc(PmsWorkItemDO::getSort)
                .orderByAsc(PmsWorkItemDO::getId));
    }

    default List<PmsWorkItemDO> selectListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .eq(PmsWorkItemDO::getProjectId, projectId)
                .orderByDesc(PmsWorkItemDO::getId));
    }

    default List<PmsWorkItemDO> selectActiveListByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .eq(PmsWorkItemDO::getProjectId, projectId)
                .eq(PmsWorkItemDO::getLifecycleStatus, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .orderByDesc(PmsWorkItemDO::getId));
    }

    default List<PmsWorkItemDO> selectListByIterationId(Long iterationId) {
        return selectList(new LambdaQueryWrapperX<PmsWorkItemDO>()
                .eq(PmsWorkItemDO::getIterationId, iterationId)
                .eq(PmsWorkItemDO::getLifecycleStatus, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .orderByDesc(PmsWorkItemDO::getId));
    }

    default List<PmsWorkItemDO> selectPlanningList(Long projectId, Long iterationId) {
        LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(PmsWorkItemDO::getProjectId, projectId)
                .eq(PmsWorkItemDO::getLifecycleStatus, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .isNull(PmsWorkItemDO::getParentId);
        if (iterationId == null) {
            queryWrapper.isNull(PmsWorkItemDO::getIterationId);
        } else {
            queryWrapper.eq(PmsWorkItemDO::getIterationId, iterationId);
        }
        return selectList(queryWrapper.orderByAsc(PmsWorkItemDO::getSort).orderByDesc(PmsWorkItemDO::getId));
    }

    /**
     * 查询待规划的根工作项列表
     *
     * @param pageReqVO 工作项筛选参数
     * @return 待规划工作项列表
     */
    default List<PmsWorkItemDO> selectListByPlanning(PmsWorkItemPageReqVO pageReqVO) {
        return selectList(buildQueryWrapper(pageReqVO)
                .isNull(PmsWorkItemDO::getParentId)
                .orderByAsc(PmsWorkItemDO::getSort)
                .orderByDesc(PmsWorkItemDO::getId));
    }

    /**
     * 分页查询当前用户负责的工作项，用于工作台展示
     *
     * @param pageParam 分页参数
     * @param projectIds 当前用户可访问的项目编号集合
     * @param userId 当前用户编号
     * @param projectId 项目编号，可选
     * @param type 工作项类型，可选
     * @param name 工作项名称或编号，可选
     * @param status 工作项状态，可选
     * @param priority 优先级，可选
     * @param iterationId 迭代编号，可选
     * @param endTime 结束时间范围，可选
     * @param completedStatus 已完成状态
     * @return 工作项分页结果
     */
    default PageResult<PmsWorkItemDO> selectWorkbenchPage(PageParam pageParam, Collection<Long> projectIds,
                                                           Long userId, Long projectId, Integer type, String name,
                                                           Integer status, Integer priority, Long iterationId,
                                                           LocalDateTime[] endTime,
                                                           Integer completedStatus) {
        LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper = new LambdaQueryWrapperX<PmsWorkItemDO>()
                .in(PmsWorkItemDO::getProjectId, projectIds)
                .eq(PmsWorkItemDO::getAssigneeUserId, userId)
                .eq(PmsWorkItemDO::getLifecycleStatus, PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .eqIfPresent(PmsWorkItemDO::getProjectId, projectId)
                .eqIfPresent(PmsWorkItemDO::getType, type)
                .eqIfPresent(PmsWorkItemDO::getStatus, status)
                .eqIfPresent(PmsWorkItemDO::getPriority, priority)
                .eqIfPresent(PmsWorkItemDO::getIterationId, iterationId)
                .betweenIfPresent(PmsWorkItemDO::getEndTime, endTime);
        queryWrapper.ne(status == null, PmsWorkItemDO::getStatus, completedStatus);
        appendNameOrSerialNumberQuery(queryWrapper, name);
        return selectPage(pageParam, queryWrapper
                .orderByAsc(PmsWorkItemDO::getEndTime)
                .orderByDesc(PmsWorkItemDO::getId));
    }

    /**
     * 统计工作台当前筛选条件下各工作项类型的数量
     *
     * @param pageReqVO 工作台筛选条件
     * @param projectIds 可访问的项目编号集合
     * @param userId 当前用户编号
     * @param lifecycleStatus 工作项生命周期状态
     * @param completedStatus 已完成状态
     * @return 工作项类型 -> 工作项数量
     */
    default Map<Integer, Long> selectWorkbenchTypeCountMap(PmsWorkbenchPageReqVO pageReqVO,
                                                           Collection<Long> projectIds, Long userId,
                                                           Integer lifecycleStatus, Integer completedStatus) {
        QueryWrapperX<PmsWorkItemDO> queryWrapper = new QueryWrapperX<PmsWorkItemDO>()
                .in("project_id", projectIds)
                .eq("assignee_user_id", userId)
                .eq("lifecycle_status", lifecycleStatus)
                .eqIfPresent("project_id", pageReqVO.getProjectId())
                .eqIfPresent("status", pageReqVO.getStatus())
                .eqIfPresent("priority", pageReqVO.getPriority())
                .eqIfPresent("iteration_id", pageReqVO.getIterationId())
                .betweenIfPresent("end_time", pageReqVO.getEndTime());
        queryWrapper.ne(pageReqVO.getStatus() == null, "status", completedStatus);
        appendNameOrSerialNumberQuery(queryWrapper, pageReqVO.getName());
        queryWrapper.select("type", "COUNT(*) AS count").groupBy("type");
        List<Map<String, Object>> rows = selectMaps(queryWrapper);
        Map<Integer, Long> typeCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            typeCountMap.put(((Number) row.get("type")).intValue(), ((Number) row.get("count")).longValue());
        }
        return typeCountMap;
    }

    default Long selectCountByStatusId(Long statusId) {
        return selectCount(PmsWorkItemDO::getStatusId, statusId);
    }

    default void updateStatusBySourceStatusId(Long sourceStatusId, Long targetStatusId, Integer targetStatus) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getStatusId, targetStatusId)
                .set(PmsWorkItemDO::getStatus, targetStatus)
                .eq(PmsWorkItemDO::getStatusId, sourceStatusId));
    }

    default void updateStatusTypeByStatusId(Long statusId, Integer statusType) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getStatus, statusType)
                .eq(PmsWorkItemDO::getStatusId, statusId));
    }

    default void updateStatusAndSortById(Long id, Long statusId, Integer status, Integer sort) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getStatusId, statusId)
                .set(PmsWorkItemDO::getStatus, status)
                .set(PmsWorkItemDO::getSort, sort)
                .eq(PmsWorkItemDO::getId, id));
    }

    default int updateForEdit(PmsWorkItemDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(updateObj.getDescription() == null, PmsWorkItemDO::getDescription, null)
                .set(updateObj.getAssigneeUserId() == null, PmsWorkItemDO::getAssigneeUserId, null)
                .set(updateObj.getIterationId() == null, PmsWorkItemDO::getIterationId, null)
                .set(updateObj.getParentId() == null, PmsWorkItemDO::getParentId, null)
                .set(updateObj.getRelatedRequirementId() == null, PmsWorkItemDO::getRelatedRequirementId, null)
                .set(updateObj.getDefectType() == null, PmsWorkItemDO::getDefectType, null)
                .set(updateObj.getStartTime() == null, PmsWorkItemDO::getStartTime, null)
                .set(updateObj.getEndTime() == null, PmsWorkItemDO::getEndTime, null)
                .set(updateObj.getEstimatedHours() == null, PmsWorkItemDO::getEstimatedHours, null)
                .set(updateObj.getFileUrls() == null, PmsWorkItemDO::getFileUrls, null)
                .set(updateObj.getLabelIds() == null, PmsWorkItemDO::getLabelIds, null)
                .eq(PmsWorkItemDO::getId, updateObj.getId()));
    }

    default void updateIterationIdToNullByIterationId(Long iterationId) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getIterationId, null)
                .eq(PmsWorkItemDO::getIterationId, iterationId));
    }

    default void updateParentIdToNullByParentId(Long parentId) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getParentId, null)
                .eq(PmsWorkItemDO::getParentId, parentId));
    }

    default void updateRelatedRequirementIdToNullByRelatedRequirementId(Long relatedRequirementId) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getRelatedRequirementId, null)
                .eq(PmsWorkItemDO::getRelatedRequirementId, relatedRequirementId));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsWorkItemDO::getProjectId, projectId);
    }

    static LambdaQueryWrapperX<PmsWorkItemDO> buildQueryWrapper(PmsWorkItemPageReqVO pageReqVO) {
        LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper = new LambdaQueryWrapperX<PmsWorkItemDO>()
                .eq(PmsWorkItemDO::getProjectId, pageReqVO.getProjectId())
                .eq(PmsWorkItemDO::getLifecycleStatus, pageReqVO.getLifecycleStatus() != null
                        ? pageReqVO.getLifecycleStatus() : PmsWorkItemLifecycleStatusEnum.ACTIVE.getStatus())
                .eqIfPresent(PmsWorkItemDO::getType, pageReqVO.getType())
                .inIfPresent(PmsWorkItemDO::getType, pageReqVO.getTypes())
                .eqIfPresent(PmsWorkItemDO::getStatus, pageReqVO.getStatus())
                .inIfPresent(PmsWorkItemDO::getStatus, pageReqVO.getStatuses())
                .eqIfPresent(PmsWorkItemDO::getPriority, pageReqVO.getPriority())
                .inIfPresent(PmsWorkItemDO::getPriority, pageReqVO.getPriorities())
                .eqIfPresent(PmsWorkItemDO::getStatusId, pageReqVO.getStatusId())
                .eqIfPresent(PmsWorkItemDO::getIterationId, pageReqVO.getIterationId())
                .inIfPresent(PmsWorkItemDO::getIterationId, pageReqVO.getIterationIds())
                .eqIfPresent(PmsWorkItemDO::getParentId, pageReqVO.getParentId())
                .eqIfPresent(PmsWorkItemDO::getAssigneeUserId, pageReqVO.getAssigneeUserId())
                .inIfPresent(PmsWorkItemDO::getAssigneeUserId, pageReqVO.getAssigneeUserIds());
        appendNameOrSerialNumberQuery(queryWrapper, pageReqVO.getName());
        if (Boolean.TRUE.equals(pageReqVO.getUnplannedOnly())) {
            queryWrapper.isNull(PmsWorkItemDO::getIterationId);
        }
        if (Boolean.TRUE.equals(pageReqVO.getRootOnly())) {
            queryWrapper.isNull(PmsWorkItemDO::getParentId);
        }
        if (CollUtil.isNotEmpty(pageReqVO.getExcludedIterationIds())) {
            queryWrapper.and(wrapper -> wrapper.notIn(PmsWorkItemDO::getIterationId,
                    pageReqVO.getExcludedIterationIds()).or().isNull(PmsWorkItemDO::getIterationId));
        }
        // label_ids 为 JSON 数组列，使用 MySQL JSON_CONTAINS 匹配元素，H2 单测不覆盖该分支
        if (CollUtil.isNotEmpty(pageReqVO.getLabelIds())) {
            queryWrapper.and(labelWrapper -> {
                for (Long labelId : pageReqVO.getLabelIds()) {
                    labelWrapper.or().apply("JSON_CONTAINS(label_ids, JSON_ARRAY({0}))", labelId);
                }
            });
        }
        return queryWrapper;
    }

    /**
     * 按工作项名称或编号追加模糊查询条件
     *
     * @param queryWrapper 查询条件
     * @param keyword 关键词，可匹配名称或去掉前缀后的编号
     */
    static void appendNameOrSerialNumberQuery(LambdaQueryWrapperX<PmsWorkItemDO> queryWrapper, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return;
        }
        String serialNumberKeyword = StrUtil.removePrefix(keyword, "#");
        queryWrapper.and(wrapper -> {
            wrapper.like(PmsWorkItemDO::getName, keyword);
            if (StrUtil.isNotBlank(serialNumberKeyword)) {
                wrapper.or().like(PmsWorkItemDO::getSerialNumber, serialNumberKeyword);
            }
        });
    }

    static void appendNameOrSerialNumberQuery(QueryWrapperX<PmsWorkItemDO> queryWrapper, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return;
        }
        String serialNumberKeyword = StrUtil.removePrefix(keyword, "#");
        queryWrapper.and(wrapper -> {
            wrapper.like("name", keyword);
            if (StrUtil.isNotBlank(serialNumberKeyword)) {
                wrapper.or().like("serial_number", serialNumberKeyword);
            }
        });
    }

    default void updateIterationIdById(Long id, Long iterationId) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getIterationId, iterationId)
                .eq(PmsWorkItemDO::getId, id));
    }

    default void updateLifecycleStatusById(Long id, Integer lifecycleStatus, LocalDateTime archiveTime,
                                           LocalDateTime recycleTime) {
        update(null, new LambdaUpdateWrapper<PmsWorkItemDO>()
                .set(PmsWorkItemDO::getLifecycleStatus, lifecycleStatus)
                .set(PmsWorkItemDO::getArchiveTime, archiveTime)
                .set(PmsWorkItemDO::getRecycleTime, recycleTime)
                .eq(PmsWorkItemDO::getId, id));
    }

}

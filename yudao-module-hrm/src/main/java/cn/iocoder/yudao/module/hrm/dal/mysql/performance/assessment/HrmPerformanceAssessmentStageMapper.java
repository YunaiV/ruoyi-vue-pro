package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface HrmPerformanceAssessmentStageMapper
        extends BaseMapperX<HrmPerformanceAssessmentStageDO> {

    default List<HrmPerformanceAssessmentStageDO> selectListByAssessmentId(Long assessmentId) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getId));
    }

    default List<HrmPerformanceAssessmentStageDO> selectListByAssessmentIds(
            Collection<Long> assessmentIds) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .in(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentIds)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getAssessmentId)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getSort));
    }

    default List<HrmPerformanceAssessmentStageDO> selectListByAssessmentIdAndType(
            Long assessmentId, Integer type) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .eq(HrmPerformanceAssessmentStageDO::getType, type)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getId));
    }

    default PageResult<HrmPerformanceAssessmentStageDO> selectPortalTaskPage(
            HrmPortalPerformanceTaskPageReqVO reqVO, Long handlerEmployeeId,
            Collection<Integer> types) {
        MPJLambdaWrapperX<HrmPerformanceAssessmentStageDO> query =
                new MPJLambdaWrapperX<HrmPerformanceAssessmentStageDO>()
                        .selectAll(HrmPerformanceAssessmentStageDO.class)
                        .innerJoin(HrmPerformanceAssessmentDO.class,
                                HrmPerformanceAssessmentDO::getId,
                                HrmPerformanceAssessmentStageDO::getAssessmentId)
                        .innerJoin(HrmPerformancePlanDO.class,
                                HrmPerformancePlanDO::getId,
                                HrmPerformanceAssessmentDO::getPlanId)
                        .eq(HrmPerformanceAssessmentStageDO::getHandlerEmployeeId, handlerEmployeeId)
                        .in(HrmPerformanceAssessmentStageDO::getType, types)
                        .eq(HrmPerformanceAssessmentStageDO::getStatus, reqVO.getStageStatus());
        if (StrUtil.isNotBlank(reqVO.getSearch())) {
            query.innerJoin(HrmEmployeeDO.class,
                    HrmEmployeeDO::getId, HrmPerformanceAssessmentDO::getEmployeeId);
            query.and(wrapper -> wrapper.like(HrmPerformancePlanDO::getName, reqVO.getSearch())
                    .or().like(HrmEmployeeDO::getName, reqVO.getSearch())
                    .or().like(HrmEmployeeDO::getJobNumber, reqVO.getSearch()));
        }
        if (HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()
                .equals(reqVO.getStageStatus())) {
            query.eq(HrmPerformancePlanDO::getStatus,
                    HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                    .orderByAsc(HrmPerformanceAssessmentStageDO::getDeadlineTime);
        } else {
            query.orderByDesc(HrmPerformanceAssessmentStageDO::getSubmitTime);
        }
        query.orderByDesc(HrmPerformanceAssessmentStageDO::getId);
        return selectJoinPage(reqVO, HrmPerformanceAssessmentStageDO.class, query);
    }

    default Map<Integer, Map<Integer, Long>> selectPortalTaskCountMap(
            Long handlerEmployeeId, String search) {
        MPJLambdaWrapperX<HrmPerformanceAssessmentStageDO> query =
                new MPJLambdaWrapperX<>();
        query.selectAs(HrmPerformanceAssessmentStageDO::getType, "type")
                .selectAs(HrmPerformanceAssessmentStageDO::getStatus, "status")
                .selectCount(HrmPerformanceAssessmentStageDO::getId, "count");
        query.innerJoin(HrmPerformanceAssessmentDO.class,
                        HrmPerformanceAssessmentDO::getId,
                        HrmPerformanceAssessmentStageDO::getAssessmentId)
                .innerJoin(HrmPerformancePlanDO.class,
                        HrmPerformancePlanDO::getId,
                        HrmPerformanceAssessmentDO::getPlanId)
                .eq(HrmPerformanceAssessmentStageDO::getHandlerEmployeeId, handlerEmployeeId);
        query.and(wrapper -> wrapper
                .ne(HrmPerformanceAssessmentStageDO::getStatus,
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                .or().eq(HrmPerformancePlanDO::getStatus,
                        HrmPerformancePlanStatusEnum.RUNNING.getStatus()));
        query.groupBy(HrmPerformanceAssessmentStageDO::getType);
        query.groupBy(HrmPerformanceAssessmentStageDO::getStatus);
        if (StrUtil.isNotBlank(search)) {
            query.innerJoin(HrmEmployeeDO.class,
                    HrmEmployeeDO::getId, HrmPerformanceAssessmentDO::getEmployeeId);
            query.and(wrapper -> wrapper.like(HrmPerformancePlanDO::getName, search)
                    .or().like(HrmEmployeeDO::getName, search)
                    .or().like(HrmEmployeeDO::getJobNumber, search));
        }
        Map<Integer, Map<Integer, Long>> countMap = new LinkedHashMap<>();
        for (Map<String, Object> item : selectJoinMaps(query)) {
            countMap.computeIfAbsent(MapUtil.getInt(item, "type"),
                    key -> new LinkedHashMap<>())
                    .put(MapUtil.getInt(item, "status"), MapUtil.getLong(item, "count"));
        }
        return countMap;
    }

    default List<HrmPerformanceAssessmentStageDO> selectListByAssessmentIdAndTypes(
            Long assessmentId, Collection<Integer> types) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .in(HrmPerformanceAssessmentStageDO::getType, types)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getId));
    }

    default List<HrmPerformanceAssessmentStageDO> selectListByTypeAndStatusAndDeadlineTimeBeforeOrEqual(
            Integer type, Integer status, LocalDateTime deadlineTime) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getType, type)
                .eq(HrmPerformanceAssessmentStageDO::getStatus, status)
                .le(HrmPerformanceAssessmentStageDO::getDeadlineTime, deadlineTime)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getDeadlineTime)
                .orderByAsc(HrmPerformanceAssessmentStageDO::getId));
    }

    default void deleteByAssessmentIds(Collection<Long> assessmentIds) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentStageDO>()
                .in(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentIds));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateToResetByAssessmentIdAndType(Long assessmentId, Integer type, Integer status) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .eq(HrmPerformanceAssessmentStageDO::getType, type)
                .set(HrmPerformanceAssessmentStageDO::getStatus, status)
                .set(HrmPerformanceAssessmentStageDO::getComment, null)
                .set(HrmPerformanceAssessmentStageDO::getRejectReason, null)
                .set(HrmPerformanceAssessmentStageDO::getSubmitTime, null)
                .set(HrmPerformanceAssessmentStageDO::getDeadlineTime, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateToResetByAssessmentIdAndTypeAndSortGreaterThanOrEqual(
            Long assessmentId, Integer type, Integer sort, Integer status) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .eq(HrmPerformanceAssessmentStageDO::getType, type)
                .ge(HrmPerformanceAssessmentStageDO::getSort, sort)
                .set(HrmPerformanceAssessmentStageDO::getStatus, status)
                .set(HrmPerformanceAssessmentStageDO::getComment, null)
                .set(HrmPerformanceAssessmentStageDO::getRejectReason, null)
                .set(HrmPerformanceAssessmentStageDO::getSubmitTime, null)
                .set(HrmPerformanceAssessmentStageDO::getDeadlineTime, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateToResetByIds(Collection<Long> ids, Integer status) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentStageDO>()
                .in(HrmPerformanceAssessmentStageDO::getId, ids)
                .set(HrmPerformanceAssessmentStageDO::getStatus, status)
                .set(HrmPerformanceAssessmentStageDO::getScore, null)
                .set(HrmPerformanceAssessmentStageDO::getComment, null)
                .set(HrmPerformanceAssessmentStageDO::getRejectReason, null)
                .set(HrmPerformanceAssessmentStageDO::getSubmitTime, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateToResetForReject(Long id, Integer status, String rejectReason) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getId, id)
                .set(HrmPerformanceAssessmentStageDO::getStatus, status)
                .set(HrmPerformanceAssessmentStageDO::getScore, null)
                .set(HrmPerformanceAssessmentStageDO::getComment, null)
                .set(HrmPerformanceAssessmentStageDO::getRejectReason, rejectReason)
                .set(HrmPerformanceAssessmentStageDO::getSubmitTime, null));
    }

    default int updateToProcessedByAssessmentIdAndTypeAndSortGreaterThan(
            Long assessmentId, Integer type, Integer sort, LocalDateTime submitTime) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentStageDO>()
                .eq(HrmPerformanceAssessmentStageDO::getAssessmentId, assessmentId)
                .eq(HrmPerformanceAssessmentStageDO::getType, type)
                .gt(HrmPerformanceAssessmentStageDO::getSort, sort)
                .set(HrmPerformanceAssessmentStageDO::getStatus,
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                .set(HrmPerformanceAssessmentStageDO::getSubmitTime, submitTime));
    }

}

package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchivePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface HrmPerformanceAssessmentMapper extends BaseMapperX<HrmPerformanceAssessmentDO> {

    default List<HrmPerformanceAssessmentDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .eq(HrmPerformanceAssessmentDO::getPlanId, planId)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default List<HrmPerformanceAssessmentDO> selectListByPlanIds(Collection<Long> planIds) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .in(HrmPerformanceAssessmentDO::getPlanId, planIds)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default List<HrmPerformanceAssessmentDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .eq(HrmPerformanceAssessmentDO::getStatus, status)
                .orderByDesc(HrmPerformanceAssessmentDO::getArchiveTime)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default List<HrmPerformanceAssessmentDO> selectListByIdsAndStatus(
            Collection<Long> ids, Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .in(HrmPerformanceAssessmentDO::getId, ids)
                .eq(HrmPerformanceAssessmentDO::getStatus, status));
    }

    default List<HrmPerformanceAssessmentDO> selectListByPlanIdsAndEmployeeIdsAndStatus(Collection<Long> planIds,
                                                                                          Collection<Long> employeeIds,
                                                                                          Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .in(HrmPerformanceAssessmentDO::getPlanId, planIds)
                .in(HrmPerformanceAssessmentDO::getEmployeeId, employeeIds)
                .eqIfPresent(HrmPerformanceAssessmentDO::getStatus, status)
                .orderByDesc(HrmPerformanceAssessmentDO::getArchiveTime)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default List<HrmPerformanceAssessmentDO> selectListByEmployeeIdsAndStatus(
            Collection<Long> employeeIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .in(HrmPerformanceAssessmentDO::getEmployeeId, employeeIds)
                .eq(HrmPerformanceAssessmentDO::getStatus, status)
                .orderByDesc(HrmPerformanceAssessmentDO::getArchiveTime)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default PageResult<HrmPerformanceAssessmentDO> selectPage(HrmPerformanceAssessmentPageReqVO reqVO) {
        LambdaQueryWrapperX<HrmPerformanceAssessmentDO> query = new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .eqIfPresent(HrmPerformanceAssessmentDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(HrmPerformanceAssessmentDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmPerformanceAssessmentDO::getEmployeeId, reqVO.getEmployeeIds())
                .eqIfPresent(HrmPerformanceAssessmentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(HrmPerformanceAssessmentDO::getStageType, reqVO.getStageType())
                .eqIfPresent(HrmPerformanceAssessmentDO::getAppealStatus, reqVO.getAppealStatus())
                .eqIfPresent(HrmPerformanceAssessmentDO::getResultLevel, reqVO.getResultLevel());
        if (Boolean.TRUE.equals(reqVO.getResultLevelEmpty())) {
            query.isNull(HrmPerformanceAssessmentDO::getResultLevel);
        }
        return selectPage(reqVO, query.orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default PageResult<HrmPerformanceAssessmentDO> selectPortalPage(
            HrmPortalPerformanceAssessmentPageReqVO reqVO, Long employeeId) {
        MPJLambdaWrapperX<HrmPerformanceAssessmentDO> query = new MPJLambdaWrapperX<HrmPerformanceAssessmentDO>()
                .selectAll(HrmPerformanceAssessmentDO.class)
                .innerJoin(HrmPerformancePlanDO.class,
                        HrmPerformancePlanDO::getId, HrmPerformanceAssessmentDO::getPlanId)
                .eq(HrmPerformanceAssessmentDO::getEmployeeId, employeeId)
                .likeIfPresent(HrmPerformancePlanDO::getName, reqVO.getSearch())
                .orderByDesc(HrmPerformanceAssessmentDO::getId);
        if (Boolean.TRUE.equals(reqVO.getArchived())) {
            query.eq(HrmPerformanceAssessmentDO::getStatus,
                    HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        } else {
            query.ne(HrmPerformanceAssessmentDO::getStatus,
                    HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        }
        return selectJoinPage(reqVO, HrmPerformanceAssessmentDO.class, query);
    }

    default Map<Integer, Long> selectCountMapByEmployeeIdAndPlanName(
            Long employeeId, String planName) {
        List<Map<String, Object>> result = selectJoinMaps(
                new MPJLambdaWrapperX<HrmPerformanceAssessmentDO>()
                        .selectAs(HrmPerformanceAssessmentDO::getStatus, "status")
                        .selectCount(HrmPerformanceAssessmentDO::getId, "count")
                        .innerJoin(HrmPerformancePlanDO.class,
                                HrmPerformancePlanDO::getId, HrmPerformanceAssessmentDO::getPlanId)
                        .eq(HrmPerformanceAssessmentDO::getEmployeeId, employeeId)
                        .likeIfPresent(HrmPerformancePlanDO::getName, planName)
                        .groupBy(HrmPerformanceAssessmentDO::getStatus));
        return convertMap(result, item -> MapUtil.getInt(item, "status"),
                item -> MapUtil.getLong(item, "count"), (first, second) -> first,
                LinkedHashMap::new);
    }

    default PageResult<HrmPerformanceAssessmentDO> selectPageByStatus(
            HrmPerformanceArchivePageReqVO reqVO, Integer status) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmPerformanceAssessmentDO>()
                .eqIfPresent(HrmPerformanceAssessmentDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(HrmPerformanceAssessmentDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmPerformanceAssessmentDO::getEmployeeId, reqVO.getEmployeeIds())
                .eq(HrmPerformanceAssessmentDO::getStatus, status)
                .orderByDesc(HrmPerformanceAssessmentDO::getArchiveTime)
                .orderByDesc(HrmPerformanceAssessmentDO::getId));
    }

    default PageResult<HrmEmployeeDO> selectArchiveEmployeePage(
            HrmPerformanceArchiveEmployeePageReqVO reqVO, Integer status) {
        MPJLambdaWrapperX<HrmPerformanceAssessmentDO> query = new MPJLambdaWrapperX<>();
        query.selectAll(HrmEmployeeDO.class)
                .innerJoin(HrmEmployeeDO.class,
                        HrmEmployeeDO::getId, HrmPerformanceAssessmentDO::getEmployeeId)
                .eq(HrmPerformanceAssessmentDO::getStatus, status)
                .orderByDesc(HrmEmployeeDO::getId);
        query.distinct();
        if (StrUtil.isNotBlank(reqVO.getSearch())) {
            query.and(wrapper -> wrapper.like(HrmEmployeeDO::getName, reqVO.getSearch())
                    .or().like(HrmEmployeeDO::getJobNumber, reqVO.getSearch()));
        }
        return selectJoinPage(reqVO, HrmEmployeeDO.class, query);
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateResultToClearById(Long id) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentDO>()
                .eq(HrmPerformanceAssessmentDO::getId, id)
                .set(HrmPerformanceAssessmentDO::getScore, null)
                .set(HrmPerformanceAssessmentDO::getResultLevel, null)
                .set(HrmPerformanceAssessmentDO::getCoefficient, BigDecimal.ONE)
                .set(HrmPerformanceAssessmentDO::getSelfComment, null)
                .set(HrmPerformanceAssessmentDO::getReviewerComment, null));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateAppealResultToClearById(Long id) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentDO>()
                .eq(HrmPerformanceAssessmentDO::getId, id)
                .set(HrmPerformanceAssessmentDO::getAppealTime, null)
                .set(HrmPerformanceAssessmentDO::getAppealComment, null));
    }

}

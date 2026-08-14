package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface HrmPerformanceAssessmentAppealRecordMapper
        extends BaseMapperX<HrmPerformanceAssessmentAppealRecordDO> {

    default List<HrmPerformanceAssessmentAppealRecordDO> selectListByAssessmentIdAndStatus(
            Long assessmentId, Integer status) {
        return selectListByAssessmentIdsAndStatus(Collections.singleton(assessmentId), status);
    }

    default List<HrmPerformanceAssessmentAppealRecordDO> selectListByAssessmentIdsAndStatus(
            Collection<Long> assessmentIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentAppealRecordDO>()
                .in(HrmPerformanceAssessmentAppealRecordDO::getAssessmentId, assessmentIds)
                .eqIfPresent(HrmPerformanceAssessmentAppealRecordDO::getStatus, status)
                .orderByAsc(HrmPerformanceAssessmentAppealRecordDO::getAssessmentId)
                .orderByAsc(HrmPerformanceAssessmentAppealRecordDO::getId));
    }

    default void deleteByAssessmentId(Long assessmentId) {
        delete(HrmPerformanceAssessmentAppealRecordDO::getAssessmentId, assessmentId);
    }

    default void deleteByAssessmentIds(Collection<Long> assessmentIds) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentAppealRecordDO>()
                .in(HrmPerformanceAssessmentAppealRecordDO::getAssessmentId, assessmentIds));
    }

}

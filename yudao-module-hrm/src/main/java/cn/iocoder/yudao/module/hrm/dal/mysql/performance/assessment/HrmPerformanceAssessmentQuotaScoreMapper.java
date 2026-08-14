package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmPerformanceAssessmentQuotaScoreMapper
        extends BaseMapperX<HrmPerformanceAssessmentQuotaScoreDO> {

    default List<HrmPerformanceAssessmentQuotaScoreDO> selectListByAssessmentStageIds(
            Collection<Long> assessmentStageIds) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaScoreDO>()
                .in(HrmPerformanceAssessmentQuotaScoreDO::getAssessmentStageId,
                        assessmentStageIds));
    }

    default void deleteByAssessmentStageId(Long assessmentStageId) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaScoreDO>()
                .eq(HrmPerformanceAssessmentQuotaScoreDO::getAssessmentStageId, assessmentStageId));
    }

    default void deleteByAssessmentStageIds(Collection<Long> assessmentStageIds) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaScoreDO>()
                .in(HrmPerformanceAssessmentQuotaScoreDO::getAssessmentStageId,
                        assessmentStageIds));
    }

}

package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmPerformanceAssessmentQuotaMapper extends BaseMapperX<HrmPerformanceAssessmentQuotaDO> {

    default List<HrmPerformanceAssessmentQuotaDO> selectListByAssessmentId(Long assessmentId) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaDO>()
                .eq(HrmPerformanceAssessmentQuotaDO::getAssessmentId, assessmentId)
                .orderByAsc(HrmPerformanceAssessmentQuotaDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentQuotaDO::getId));
    }

    default List<HrmPerformanceAssessmentQuotaDO> selectListByAssessmentIds(
            Collection<Long> assessmentIds) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaDO>()
                .in(HrmPerformanceAssessmentQuotaDO::getAssessmentId, assessmentIds)
                .orderByAsc(HrmPerformanceAssessmentQuotaDO::getAssessmentId)
                .orderByAsc(HrmPerformanceAssessmentQuotaDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentQuotaDO::getId));
    }

    default void deleteByAssessmentIds(Collection<Long> assessmentIds) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentQuotaDO>()
                .in(HrmPerformanceAssessmentQuotaDO::getAssessmentId, assessmentIds));
    }

    default int updateFinalScoreToNullByAssessmentId(Long assessmentId) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentQuotaDO>()
                .eq(HrmPerformanceAssessmentQuotaDO::getAssessmentId, assessmentId)
                .set(HrmPerformanceAssessmentQuotaDO::getFinalScore, null));
    }

    default int updateReviewScoreToNullByAssessmentId(Long assessmentId) {
        return update(new LambdaUpdateWrapper<HrmPerformanceAssessmentQuotaDO>()
                .eq(HrmPerformanceAssessmentQuotaDO::getAssessmentId, assessmentId)
                .set(HrmPerformanceAssessmentQuotaDO::getSelfScore, null)
                .set(HrmPerformanceAssessmentQuotaDO::getReviewerScore, null)
                .set(HrmPerformanceAssessmentQuotaDO::getFinalScore, null));
    }

}

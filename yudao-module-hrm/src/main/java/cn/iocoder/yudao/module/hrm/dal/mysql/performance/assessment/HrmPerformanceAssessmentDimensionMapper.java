package cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * HRM 绩效员工维度 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface HrmPerformanceAssessmentDimensionMapper
        extends BaseMapperX<HrmPerformanceAssessmentDimensionDO> {

    default List<HrmPerformanceAssessmentDimensionDO> selectListByAssessmentId(Long assessmentId) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDimensionDO>()
                .eq(HrmPerformanceAssessmentDimensionDO::getAssessmentId, assessmentId)
                .orderByAsc(HrmPerformanceAssessmentDimensionDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentDimensionDO::getId));
    }

    default List<HrmPerformanceAssessmentDimensionDO> selectListByAssessmentIds(
            Collection<Long> assessmentIds) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentDimensionDO>()
                .in(HrmPerformanceAssessmentDimensionDO::getAssessmentId, assessmentIds)
                .orderByAsc(HrmPerformanceAssessmentDimensionDO::getAssessmentId)
                .orderByAsc(HrmPerformanceAssessmentDimensionDO::getSort)
                .orderByAsc(HrmPerformanceAssessmentDimensionDO::getId));
    }

    default void deleteByAssessmentId(Long assessmentId) {
        delete(HrmPerformanceAssessmentDimensionDO::getAssessmentId, assessmentId);
    }

    default void deleteByAssessmentIds(Collection<Long> assessmentIds) {
        delete(new LambdaQueryWrapperX<HrmPerformanceAssessmentDimensionDO>()
                .in(HrmPerformanceAssessmentDimensionDO::getAssessmentId, assessmentIds));
    }

}

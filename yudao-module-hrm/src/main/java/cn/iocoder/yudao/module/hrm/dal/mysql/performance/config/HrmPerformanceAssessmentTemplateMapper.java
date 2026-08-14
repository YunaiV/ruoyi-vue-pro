package cn.iocoder.yudao.module.hrm.dal.mysql.performance.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmPerformanceAssessmentTemplateMapper extends BaseMapperX<HrmPerformanceAssessmentTemplateDO> {

    default PageResult<HrmPerformanceAssessmentTemplateDO> selectPage(
            HrmPerformanceAssessmentTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmPerformanceAssessmentTemplateDO>()
                .likeIfPresent(HrmPerformanceAssessmentTemplateDO::getName, reqVO.getName())
                .eq(HrmPerformanceAssessmentTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(HrmPerformanceAssessmentTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceAssessmentTemplateDO::getId));
    }

    default HrmPerformanceAssessmentTemplateDO selectByName(String name) {
        return selectFirstOne(HrmPerformanceAssessmentTemplateDO::getName, name,
                HrmPerformanceAssessmentTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

    default List<HrmPerformanceAssessmentTemplateDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceAssessmentTemplateDO>()
                .eq(HrmPerformanceAssessmentTemplateDO::getStatus, status)
                .orderByDesc(HrmPerformanceAssessmentTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceAssessmentTemplateDO::getId));
    }

}

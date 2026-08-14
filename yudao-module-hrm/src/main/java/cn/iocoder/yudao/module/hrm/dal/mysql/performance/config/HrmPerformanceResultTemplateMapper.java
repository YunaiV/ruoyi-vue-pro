package cn.iocoder.yudao.module.hrm.dal.mysql.performance.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmPerformanceResultTemplateMapper extends BaseMapperX<HrmPerformanceResultTemplateDO> {

    default PageResult<HrmPerformanceResultTemplateDO> selectPage(HrmPerformanceResultTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmPerformanceResultTemplateDO>()
                .likeIfPresent(HrmPerformanceResultTemplateDO::getName, reqVO.getName())
                .eq(HrmPerformanceResultTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByDesc(HrmPerformanceResultTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceResultTemplateDO::getId));
    }

    default HrmPerformanceResultTemplateDO selectByName(String name) {
        return selectFirstOne(HrmPerformanceResultTemplateDO::getName, name,
                HrmPerformanceResultTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

    default List<HrmPerformanceResultTemplateDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmPerformanceResultTemplateDO>()
                .eqIfPresent(HrmPerformanceResultTemplateDO::getStatus, status)
                .orderByDesc(HrmPerformanceResultTemplateDO::getUpdateTime)
                .orderByDesc(HrmPerformanceResultTemplateDO::getId));
    }

}

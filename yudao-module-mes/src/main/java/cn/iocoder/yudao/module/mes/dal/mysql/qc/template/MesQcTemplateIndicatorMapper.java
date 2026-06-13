package cn.iocoder.yudao.module.mes.dal.mysql.qc.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator.MesQcTemplateIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 质检方案-检测指标项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcTemplateIndicatorMapper extends BaseMapperX<MesQcTemplateIndicatorDO> {

    default PageResult<MesQcTemplateIndicatorDO> selectPage(MesQcTemplateIndicatorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcTemplateIndicatorDO>()
                .eqIfPresent(MesQcTemplateIndicatorDO::getTemplateId, reqVO.getTemplateId())
                .orderByAsc(MesQcTemplateIndicatorDO::getId));
    }

    default List<MesQcTemplateIndicatorDO> selectListByTemplateId(Long templateId) {
        return selectList(MesQcTemplateIndicatorDO::getTemplateId, templateId);
    }

    default Long selectCountByUnitMeasureId(Long unitMeasureId) {
        return selectCount(MesQcTemplateIndicatorDO::getUnitMeasureId, unitMeasureId);
    }

    default void deleteByTemplateId(Long templateId) {
        delete(new LambdaQueryWrapperX<MesQcTemplateIndicatorDO>()
                .eq(MesQcTemplateIndicatorDO::getTemplateId, templateId));
    }

}

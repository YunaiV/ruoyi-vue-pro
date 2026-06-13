package cn.iocoder.yudao.module.mes.dal.mysql.md.unitmeasure;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasurePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 计量单位 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdUnitMeasureMapper extends BaseMapperX<MesMdUnitMeasureDO> {

    default PageResult<MesMdUnitMeasureDO> selectPage(MesMdUnitMeasurePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesMdUnitMeasureDO>()
                .likeIfPresent(MesMdUnitMeasureDO::getCode, reqVO.getCode())
                .likeIfPresent(MesMdUnitMeasureDO::getName, reqVO.getName())
                .eqIfPresent(MesMdUnitMeasureDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesMdUnitMeasureDO::getId));
    }

    default MesMdUnitMeasureDO selectByCode(String code) {
        return selectOne(MesMdUnitMeasureDO::getCode, code);
    }

    default List<MesMdUnitMeasureDO> selectListByStatus(Integer status) {
        return selectList(MesMdUnitMeasureDO::getStatus, status);
    }

    default Long selectCountByPrimaryId(Long primaryId) {
        return selectCount(MesMdUnitMeasureDO::getPrimaryId, primaryId);
    }

}

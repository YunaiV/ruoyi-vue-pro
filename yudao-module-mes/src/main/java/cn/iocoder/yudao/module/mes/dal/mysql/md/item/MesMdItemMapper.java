package cn.iocoder.yudao.module.mes.dal.mysql.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.MesMdItemPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * MES 物料产品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdItemMapper extends BaseMapperX<MesMdItemDO> {

    default PageResult<MesMdItemDO> selectPage(MesMdItemPageReqVO reqVO, Collection<Long> itemTypeIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesMdItemDO>()
                .likeIfPresent(MesMdItemDO::getCode, reqVO.getCode())
                .likeIfPresent(MesMdItemDO::getName, reqVO.getName())
                .inIfPresent(MesMdItemDO::getItemTypeId, itemTypeIds)
                .eqIfPresent(MesMdItemDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesMdItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesMdItemDO::getId));
    }

    default MesMdItemDO selectByCode(String code) {
        return selectOne(MesMdItemDO::getCode, code);
    }

    default MesMdItemDO selectByName(String name) {
        return selectOne(MesMdItemDO::getName, name);
    }

    default Long selectCountByItemTypeId(Long itemTypeId) {
        return selectCount(MesMdItemDO::getItemTypeId, itemTypeId);
    }

    default Long selectCountByUnitMeasureId(Long unitMeasureId) {
        return selectCount(MesMdItemDO::getUnitMeasureId, unitMeasureId);
    }

}

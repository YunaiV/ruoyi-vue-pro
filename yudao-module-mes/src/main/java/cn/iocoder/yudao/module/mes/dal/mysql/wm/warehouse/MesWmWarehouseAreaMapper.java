package cn.iocoder.yudao.module.mes.dal.mysql.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area.MesWmWarehouseAreaPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * MES 库位 Mapper
 */
@Mapper
public interface MesWmWarehouseAreaMapper extends BaseMapperX<MesWmWarehouseAreaDO> {

    default PageResult<MesWmWarehouseAreaDO> selectPage(MesWmWarehouseAreaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .likeIfPresent(MesWmWarehouseAreaDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmWarehouseAreaDO::getName, reqVO.getName())
                .eqIfPresent(MesWmWarehouseAreaDO::getLocationId, reqVO.getLocationId())
                .eqIfPresent(MesWmWarehouseAreaDO::getPositionX, reqVO.getPositionX())
                .eqIfPresent(MesWmWarehouseAreaDO::getPositionY, reqVO.getPositionY())
                .eqIfPresent(MesWmWarehouseAreaDO::getPositionZ, reqVO.getPositionZ())
                .orderByDesc(MesWmWarehouseAreaDO::getId));
    }

    default MesWmWarehouseAreaDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .eq(MesWmWarehouseAreaDO::getCode, code));
    }

    default MesWmWarehouseAreaDO selectByCode(Long locationId, String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .eq(MesWmWarehouseAreaDO::getLocationId, locationId)
                .eq(MesWmWarehouseAreaDO::getCode, code));
    }

    default MesWmWarehouseAreaDO selectByName(Long locationId, String name) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .eq(MesWmWarehouseAreaDO::getLocationId, locationId)
                .eq(MesWmWarehouseAreaDO::getName, name));
    }

    default List<MesWmWarehouseAreaDO> selectSimpleList(Long locationId) {
        return selectList(new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .eqIfPresent(MesWmWarehouseAreaDO::getLocationId, locationId)
                .orderByDesc(MesWmWarehouseAreaDO::getId));
    }

    default List<MesWmWarehouseAreaDO> selectListByIds(Collection<Long> ids) {
        return selectByIds(ids);
    }

    default Long selectCountByLocationId(Long locationId) {
        return selectCount(MesWmWarehouseAreaDO::getLocationId, locationId);
    }

    /**
     * 批量更新指定库区下所有库位
     */
    default void updateByLocationId(Long locationId, MesWmWarehouseAreaDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<MesWmWarehouseAreaDO>()
                .eq(MesWmWarehouseAreaDO::getLocationId, locationId));
    }

}

package cn.iocoder.yudao.module.mes.dal.mysql.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location.MesWmWarehouseLocationPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * MES 库区 Mapper
 */
@Mapper
public interface MesWmWarehouseLocationMapper extends BaseMapperX<MesWmWarehouseLocationDO> {

    default PageResult<MesWmWarehouseLocationDO> selectPage(MesWmWarehouseLocationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmWarehouseLocationDO>()
                .likeIfPresent(MesWmWarehouseLocationDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmWarehouseLocationDO::getName, reqVO.getName())
                .eqIfPresent(MesWmWarehouseLocationDO::getWarehouseId, reqVO.getWarehouseId())
                .orderByDesc(MesWmWarehouseLocationDO::getId));
    }

    default MesWmWarehouseLocationDO selectByCode(Long warehouseId, String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseLocationDO>()
                .eq(MesWmWarehouseLocationDO::getWarehouseId, warehouseId)
                .eq(MesWmWarehouseLocationDO::getCode, code));
    }

    default MesWmWarehouseLocationDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseLocationDO>()
                .eq(MesWmWarehouseLocationDO::getCode, code));
    }

    default MesWmWarehouseLocationDO selectByName(Long warehouseId, String name) {
        return selectOne(new LambdaQueryWrapperX<MesWmWarehouseLocationDO>()
                .eq(MesWmWarehouseLocationDO::getWarehouseId, warehouseId)
                .eq(MesWmWarehouseLocationDO::getName, name));
    }

    default List<MesWmWarehouseLocationDO> selectSimpleList(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<MesWmWarehouseLocationDO>()
                .eqIfPresent(MesWmWarehouseLocationDO::getWarehouseId, warehouseId)
                .orderByDesc(MesWmWarehouseLocationDO::getId));
    }

    default List<MesWmWarehouseLocationDO> selectListByIds(Collection<Long> ids) {
        return selectByIds(ids);
    }

    default Long selectCountByWarehouseId(Long warehouseId) {
        return selectCount(MesWmWarehouseLocationDO::getWarehouseId, warehouseId);
    }

}

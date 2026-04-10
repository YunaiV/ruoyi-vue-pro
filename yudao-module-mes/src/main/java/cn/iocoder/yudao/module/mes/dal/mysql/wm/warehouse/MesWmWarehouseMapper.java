package cn.iocoder.yudao.module.mes.dal.mysql.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.MesWmWarehousePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * MES 仓库 Mapper
 */
@Mapper
public interface MesWmWarehouseMapper extends BaseMapperX<MesWmWarehouseDO> {

    default PageResult<MesWmWarehouseDO> selectPage(MesWmWarehousePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmWarehouseDO>()
                .likeIfPresent(MesWmWarehouseDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmWarehouseDO::getName, reqVO.getName())
                .eqIfPresent(MesWmWarehouseDO::getFrozen, reqVO.getFrozen())
                .orderByDesc(MesWmWarehouseDO::getId));
    }

    default MesWmWarehouseDO selectByCode(String code) {
        return selectOne(MesWmWarehouseDO::getCode, code);
    }

    default MesWmWarehouseDO selectByName(String name) {
        return selectOne(MesWmWarehouseDO::getName, name);
    }

    default List<MesWmWarehouseDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<MesWmWarehouseDO>()
                .orderByDesc(MesWmWarehouseDO::getId));
    }

    default List<MesWmWarehouseDO> selectListByIds(Collection<Long> ids) {
        return selectByIds(ids);
    }

}

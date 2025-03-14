package cn.iocoder.yudao.module.wms.dal.mysql.warehouse.area;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo.*;

/**
 * 库区 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseAreaMapper extends BaseMapperX<WmsWarehouseAreaDO> {

    default PageResult<WmsWarehouseAreaDO> selectPage(WmsWarehouseAreaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseAreaDO>()
				.eqIfPresent(WmsWarehouseAreaDO::getCode, reqVO.getCode())
				.likeIfPresent(WmsWarehouseAreaDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseAreaDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsWarehouseAreaDO::getStockType, reqVO.getStockType())
				.eqIfPresent(WmsWarehouseAreaDO::getPartitionType, reqVO.getPartitionType())
				.eqIfPresent(WmsWarehouseAreaDO::getStatus, reqVO.getStatus())
				.eqIfPresent(WmsWarehouseAreaDO::getPriority, reqVO.getPriority())
				.betweenIfPresent(WmsWarehouseAreaDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsWarehouseAreaDO::getId));
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseAreaDO
     */
    default WmsWarehouseAreaDO getByCode(String code, boolean deleted) {
        LambdaQueryWrapperX<WmsWarehouseAreaDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseAreaDO::getCode, code);
        ;
        if (deleted) {
            wrapper.eq(WmsWarehouseAreaDO::getDeleted, true);
        }
        return selectOne(wrapper);
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseAreaDO
     */
    default WmsWarehouseAreaDO getByCode(String code) {
        return getByCode(code, false);
    }

    /**
     * 按 warehouse_id 查询 WmsWarehouseAreaDO 清单
     */
    default List<WmsWarehouseAreaDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsWarehouseAreaDO>().eq(WmsWarehouseAreaDO::getWarehouseId, warehouseId));
    }
}

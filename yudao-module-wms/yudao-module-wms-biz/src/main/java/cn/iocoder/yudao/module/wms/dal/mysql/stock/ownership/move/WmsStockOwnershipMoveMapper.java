package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.move;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.*;

/**
 * 所有者库存移动 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershipMoveMapper extends BaseMapperX<WmsStockOwnershipMoveDO> {

    default PageResult<WmsStockOwnershipMoveDO> selectPage(WmsStockOwnershipMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershipMoveDO>()
				.eqIfPresent(WmsStockOwnershipMoveDO::getNo, reqVO.getNo())
				.eqIfPresent(WmsStockOwnershipMoveDO::getExecuteStatus, reqVO.getExecuteStatus())
				.eqIfPresent(WmsStockOwnershipMoveDO::getWarehouseId, reqVO.getWarehouseId())
				.betweenIfPresent(WmsStockOwnershipMoveDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockOwnershipMoveDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsStockOwnershipMoveDO
     */
    default WmsStockOwnershipMoveDO getByNo(String no) {
        LambdaQueryWrapperX<WmsStockOwnershipMoveDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockOwnershipMoveDO::getNo, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsStockOwnershipMoveDO 清单
     */
    default List<WmsStockOwnershipMoveDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsStockOwnershipMoveDO>().eq(WmsStockOwnershipMoveDO::getWarehouseId, warehouseId));
    }
}
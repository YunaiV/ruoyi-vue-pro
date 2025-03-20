package cn.iocoder.yudao.module.wms.dal.mysql.stock.flow;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;

/**
 * 库存流水 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockFlowMapper extends BaseMapperX<WmsStockFlowDO> {

    default PageResult<WmsStockFlowDO> selectPage(WmsStockFlowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockFlowDO>()
				.eqIfPresent(WmsStockFlowDO::getStockType, reqVO.getStockType())
				.eqIfPresent(WmsStockFlowDO::getStockId, reqVO.getStockId())
				.eqIfPresent(WmsStockFlowDO::getReason, reqVO.getReason())
				.eqIfPresent(WmsStockFlowDO::getReasonBillId, reqVO.getReasonBillId())
				.eqIfPresent(WmsStockFlowDO::getReasonItemId, reqVO.getReasonItemId())
				.eqIfPresent(WmsStockFlowDO::getPrevFlowId, reqVO.getPrevFlowId())
				.eqIfPresent(WmsStockFlowDO::getDeltaQuantity, reqVO.getDeltaQuantity())
				.eqIfPresent(WmsStockFlowDO::getPurchasePlanQuantity, reqVO.getPurchasePlanQuantity())
				.eqIfPresent(WmsStockFlowDO::getPurchaseTransitQuantity, reqVO.getPurchaseTransitQuantity())
				.eqIfPresent(WmsStockFlowDO::getReturnTransitQuantity, reqVO.getReturnTransitQuantity())
				.eqIfPresent(WmsStockFlowDO::getShelvingPendingQuantity, reqVO.getShelvingPendingQuantity())
				.eqIfPresent(WmsStockFlowDO::getAvailableQuantity, reqVO.getAvailableQuantity())
				.eqIfPresent(WmsStockFlowDO::getSellableQuantity, reqVO.getSellableQuantity())
				.eqIfPresent(WmsStockFlowDO::getOutboundPendingQuantity, reqVO.getOutboundPendingQuantity())
				.eqIfPresent(WmsStockFlowDO::getDefectiveQuantity, reqVO.getDefectiveQuantity())
				.betweenIfPresent(WmsStockFlowDO::getFlowTime, reqVO.getFlowTime())
				.betweenIfPresent(WmsStockFlowDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockFlowDO::getId));
    }

    /**
     * 按 stock_type,stock_id 查询 WmsStockFlowDO 清单
     */
    default List<WmsStockFlowDO> selectByStockTypeAndStockId(Integer stockType, Long stockId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId));
    }

    /**
     * 按 product_id 查询 WmsStockFlowDO 清单
     */
    default List<WmsStockFlowDO> selectByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getProductId, productId));
    }

    /**
     * 按 warehouse_id,stock_type,stock_id 查询 WmsStockFlowDO 清单
     */
    default List<WmsStockFlowDO> selectByIdxStock(Long warehouseId, Integer stockType, Long stockId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getWarehouseId, warehouseId).eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId));
    }

    /**
     * 按 warehouse_id 查询 WmsStockFlowDO 清单
     */
    default List<WmsStockFlowDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getWarehouseId, warehouseId));
    }

    default WmsStockFlowDO getLastFlow(Long warehouseId, Integer stockType, Long stockId) {
        return selectOne(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getWarehouseId, warehouseId).eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId).eq(WmsStockFlowDO::getNextFlowId, 0));
    }

    default List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId));
    }
}
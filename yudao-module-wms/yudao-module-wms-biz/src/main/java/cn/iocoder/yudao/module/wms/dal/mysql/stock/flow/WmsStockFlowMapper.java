package cn.iocoder.yudao.module.wms.dal.mysql.stock.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库存流水 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockFlowMapper extends BaseMapperX<WmsStockFlowDO> {

    default PageResult<WmsStockFlowDO> selectPage(WmsStockFlowPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsStockFlowDO> wrapper = new MPJLambdaWrapperX();
        // 库存分类的固定条件
        wrapper.eq(WmsStockFlowDO::getStockType, reqVO.getStockType());
        wrapper.in(WmsStockFlowDO::getReason, reqVO.getReason());
        // 连接产品视图
        if (reqVO.getProductCode() != null) {
            wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockFlowDO::getProductId).likeIfExists(WmsProductDO::getCode, reqVO.getProductCode());
        }
        wrapper.eqIfPresent(WmsStockFlowDO::getWarehouseId, reqVO.getWarehouseId());
        wrapper.eqIfPresent(WmsStockFlowDO::getDirection, reqVO.getDirection());
        wrapper.eqIfPresent(WmsStockFlowDO::getProductId, reqVO.getProductId());
        // if(reqVO.getZoneId()!=null) {
        // wrapper.innerJoin(WmsWarehouseBinDO.class, WmsWarehouseBinDO::getId, WmsStockBinDO::getBinId)
        // .eqIfExists(WmsWarehouseBinDO::getZoneId, reqVO.getZoneId());
        // }
        // // 按仓库
        // wrapper.eqIfPresent(WmsStockBinDO::getWarehouseId, reqVO.getWarehouseId())
        // // 按产品ID
        // .eqIfPresent(WmsStockBinDO::getProductId, reqVO.getProductId())
        // .eqIfPresent(WmsStockBinDO::getBinId, reqVO.getBinId())
        // ;
        wrapper.betweenIfPresent(WmsStockFlowDO::getAvailableQty, reqVO.getAvailableQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getDefectiveQty, reqVO.getDefectiveQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getDeltaQty, reqVO.getDeltaQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getOutboundPendingQty, reqVO.getOutboundPendingQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getTransitQty, reqVO.getTransitQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getMakePendingQty, reqVO.getMakePendingQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getReturnTransitQty, reqVO.getReturnTransitQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getSellableQty, reqVO.getSellableQty());
        wrapper.betweenIfPresent(WmsStockFlowDO::getShelvingPendingQty, reqVO.getShelvingPendingQty());
        //过滤公司
         if(reqVO.getCompanyId()!=null) {
             wrapper.innerJoin(WmsStockLogicDO.class, WmsStockLogicDO::getId, WmsStockFlowDO::getStockId)
                 .eqIfExists(WmsStockLogicDO::getCompanyId, reqVO.getCompanyId());
         }
         wrapper.orderByDesc(WmsStockFlowDO::getCreateTime);
        return selectPage(reqVO, wrapper);
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
     * 按 reason_item_id 和 reason_bill_id 查询 WmsStockFlowDO 清单
     */
    default List<WmsStockFlowDO> selectByReasonItemIdAndReasonBillId(Long reasonItemId, Long reasonBillId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getReasonItemId, reasonItemId).eq(WmsStockFlowDO::getReasonBillId, reasonBillId));
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
        // 解决高并发插入冲突
        List<WmsStockFlowDO> list = selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getWarehouseId, warehouseId).eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId).eq(WmsStockFlowDO::getNextFlowId, 0));
        return list.isEmpty() ? null : list.get(0);
//        LambdaQueryWrapperX<WmsStockFlowDO> wrapper = new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getWarehouseId, warehouseId).eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId).eq(WmsStockFlowDO::getNextFlowId, 0);
        // 无并发
//        return selectOne(wrapper);
    }

    default List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId) {
        return selectList(new LambdaQueryWrapperX<WmsStockFlowDO>().eq(WmsStockFlowDO::getStockType, stockType).eq(WmsStockFlowDO::getStockId, stockId));
    }
}

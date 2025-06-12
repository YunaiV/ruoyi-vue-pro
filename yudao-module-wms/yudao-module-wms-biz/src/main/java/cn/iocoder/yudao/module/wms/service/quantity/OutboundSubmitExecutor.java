package cn.iocoder.yudao.module.wms.service.quantity;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:15
 * @description:
 */

@Component
public class OutboundSubmitExecutor extends OutboundExecutor {

    public OutboundSubmitExecutor() {
        super(WmsStockReason.OUTBOUND_SUBMIT);
    }

    @Override
    protected Integer getExecuteQty(WmsOutboundItemRespVO item) {
        return item.getPlanQty();
    }

    /**
     * 更新仓库库存
     **/
    @Override
    protected WmsStockFlowDirection updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity) {

        // 可用量
        // stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() - quantity);
        // 可售量
        stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() - quantity);
        if ((stockWarehouseDO.getAvailableQty() - quantity) < 0) {
            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
        }
        // 待出库量
        stockWarehouseDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty() + quantity);

        return WmsStockFlowDirection.OUT;

    }

    /**
     * 更新入库单详情
     **/
    @Override
    protected List<WmsItemFlowDO> processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        Long actionId = IdUtil.getSnowflakeNextId();
        outboundRespVO.setLatestOutboundActionId(actionId);

//        if (item.getInboundItemId() != null) {
//            // 指定从批次库存出库：此时指定了出库的库存批次，但未指定仓位
//            processInboundItemForInbound(outboundRespVO, item,actionId,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
//        } else {
            // 指定从仓位出库：此时未指定出库的批次库存，但指定了仓位
            return processInboundItemForBin(outboundRespVO, item,actionId,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
//        }

    }

//    /**
//     * 从指定批次出库,此时需要动态计算出库仓位
//     **/
//    protected void processInboundItemForInbound(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long actionId ,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
//
//        WmsInboundItemDO inboundItem = inboundItemService.getInboundItem(item.getInboundItemId());
//        if(inboundItem==null) {
//            throw exception(INBOUND_ITEM_NOT_EXISTS);
//        }
//        if(quantity>inboundItem.getOutboundAvailableQty()) {
//            throw exception(INBOUND_ITEM_OUTBOUND_AVAILABLE_QTY_NOT_ENOUGH);
//        }
//
//        // 从多个有可用库存的入库批次，以先进先出的原则扣除
//        List<WmsInboundItemDO> itemsToUpdate = new ArrayList<>();
//        List<WmsItemFlowDO> itemFlowList = new ArrayList<>();
//
//        Integer available = inboundItem.getOutboundAvailableQty();
//        Integer flowQty = quantity;
//        available = available - flowQty;
//        inboundItem.setOutboundAvailableQty(available);
//        itemsToUpdate.add(inboundItem);
//
//        //
//        WmsItemFlowDO flowDO = new WmsItemFlowDO();
//        flowDO.setOutboundActionId(actionId);
//        flowDO.setInboundId(inboundItem.getInboundId());
//        flowDO.setInboundItemId(inboundItem.getId());
//        flowDO.setProductId(inboundItem.getProductId());
//        flowDO.setOutboundQty(flowQty);
//        flowDO.setOutboundId(outboundId);
//        flowDO.setOutboundItemId(outboundItemId);
//        itemFlowList.add(flowDO);
//
//        // 保存详情与流水
//        inboundItemService.saveItems(itemsToUpdate,itemFlowList);
//
//    }

    /**
     * 从指定仓位出库,此时需要动态计算库存批次
     **/
    protected List<WmsItemFlowDO> processInboundItemForBin(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long actionId, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        WmsStockBinDO stockBinDO = stockBinService.getStockBin(binId, productId, false);
        // 如果不存在抛出异常
        if (stockBinDO == null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        if (stockBinDO.getAvailableQty() + stockBinDO.getOutboundPendingQty() < quantity) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }

        // 从指定仓位出库：未指定出库的批次库存，但指定了仓位
        List<WmsInboundItemDO> itemsList=inboundItemService.selectItemListHasAvailableQty(warehouseId,productId);
        if(CollectionUtils.isEmpty(itemsList)) {
            throw exception(INBOUND_ITEM_PRODUCT_NOT_EXISTS, productId);
        }

        // 检查入库批次库存是否充足
//        int totalOutboundAvailableQty=0;
//        for (WmsInboundItemDO inboundItemDO : itemsList) {
//            totalOutboundAvailableQty += inboundItemDO.getOutboundAvailableQty() == null ? 0 : inboundItemDO.getOutboundAvailableQty();
//        }
//        if(quantity>totalOutboundAvailableQty) {
//            throw exception(INBOUND_ITEM_OUTBOUND_AVAILABLE_QTY_NOT_ENOUGH);
//        }


        // 从多个有可用库存的入库批次，以先进先出的原则扣除
        List<WmsInboundItemDO> itemsToUpdate = new ArrayList<>();
        List<WmsItemFlowDO> itemFlowList = new ArrayList<>();
        for (WmsInboundItemDO itemDO : itemsList) {
            Integer available = itemDO.getOutboundAvailableQty();
            Integer flowQty = 0;
            // 需要多次扣除
            if (available > quantity) {
                flowQty = quantity;
                available = available - flowQty;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);
                //
                WmsItemFlowDO flowDO = new WmsItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());

                flowDO.setBillType(BillType.WMS_OUTBOUND.getValue());
                flowDO.setBillId(outboundRespVO.getId());
                flowDO.setBillItemId(item.getId());

                flowDO.setDirection(WmsStockFlowDirection.OUT.getValue());
                flowDO.setOutboundAvailableDeltaQty(flowQty);
                flowDO.setOutboundAvailableQty(itemDO.getOutboundAvailableQty());
                flowDO.setActualQty(itemDO.getActualQty());
                flowDO.setShelveClosedQty(itemDO.getShelveClosedQty());

                itemFlowList.add(flowDO);

            } else if (available.equals(quantity)) {
                // 刚好单次扣除
                flowQty = available;
                available = 0;
                quantity = 0;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);
                //
                WmsItemFlowDO flowDO = new WmsItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());

                flowDO.setBillType(BillType.WMS_OUTBOUND.getValue());
                flowDO.setBillId(outboundRespVO.getId());
                flowDO.setBillItemId(item.getId());

                flowDO.setDirection(WmsStockFlowDirection.OUT.getValue());
                flowDO.setOutboundAvailableDeltaQty(flowQty);
                flowDO.setOutboundAvailableQty(itemDO.getOutboundAvailableQty());
                flowDO.setActualQty(itemDO.getActualQty());
                flowDO.setShelveClosedQty(itemDO.getShelveClosedQty());

                itemFlowList.add(flowDO);

                break;
            } else { // 单次扣除
                flowQty = quantity;
                available = available - flowQty;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);

                //
                WmsItemFlowDO flowDO = new WmsItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());

                flowDO.setBillType(BillType.WMS_OUTBOUND.getValue());
                flowDO.setBillId(outboundRespVO.getId());
                flowDO.setBillItemId(item.getId());

                flowDO.setDirection(WmsStockFlowDirection.OUT.getValue());
                flowDO.setOutboundAvailableDeltaQty(flowQty);
                flowDO.setOutboundAvailableQty(itemDO.getOutboundAvailableQty());
                flowDO.setActualQty(itemDO.getActualQty());
                flowDO.setShelveClosedQty(itemDO.getShelveClosedQty());

                itemFlowList.add(flowDO);

                break;
            }
        }
        // 保存详情与流水
        inboundItemService.saveItems(itemsToUpdate, itemFlowList);

        return itemFlowList;
    }



    /**
     * 更新逻辑库存
     **/
    @Override
    protected WmsStockFlowDirection updateStockLogicQty(WmsStockLogicDO stockLogicDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用量
        // stockLogicDO.setAvailableQty(stockLogicDO.getAvailableQty()-quantity);
        // 待出库量
        stockLogicDO.setOutboundPendingQty(stockLogicDO.getOutboundPendingQty() + quantity);

        return WmsStockFlowDirection.IN;
    }

    /**
     * 更新库存货位
     **/
    @Override
    protected  WmsStockFlowDirection updateSingleStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item, Integer quantity) {

        // 可用库存
        // stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() - quantity);
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() - quantity);
        if (stockBinDO.getAvailableQty() + stockBinDO.getOutboundPendingQty() < 0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        // 待出库量
        stockBinDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty() + quantity);

        return WmsStockFlowDirection.OUT;
    }

//    @Override
//    protected void updateMultiStockBinQty(Long warehouseId, Long productId, WmsOutboundItemRespVO item,Integer quantity) {
//
//        // 判断仓位库存是否充足
//        List<WmsStockBinDO> wmsStockBinDOList = stockBinService.selectBinsByInboundItemId(warehouseId, productId, item.getInboundItemId());
//        Long totalSellableQty=0L;
//        for (WmsStockBinDO stockBinDO : wmsStockBinDOList) {
//            totalSellableQty+=stockBinDO.getSellableQty();
//        }
//        if(quantity>totalSellableQty) {
//            throw exception(STOCK_BIN_NOT_ENOUGH);
//        }
//
//        //TODO 按库存批次出库暂不实现
//
//    }

    /**
     * 更新出库单
     **/
    @Override
    protected void updateOutbound(WmsOutboundRespVO outboundRespVO) {
        List<WmsOutboundItemRespVO> itemRespVOList=outboundRespVO.getItemList();
        for (WmsOutboundItemRespVO itemRespVO : itemRespVOList) {
            itemRespVO.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
        }
        outboundRespVO.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
        outboundRespVO.setOutboundTime(LocalDateTime.now());
    }
}

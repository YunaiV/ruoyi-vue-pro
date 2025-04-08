package cn.iocoder.yudao.module.wms.service.quantity;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_ENOUGH;

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
        // 待出库量
        stockWarehouseDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty() + quantity);

        return WmsStockFlowDirection.OUT;

    }

    /**
     * 更新入库单详情
     **/
    protected void processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        Long actionId= IdUtil.getSnowflakeNextId();

        outboundRespVO.setLatestOutboundActionId(actionId);

        WmsStockBinDO stockBinDO =stockBinService.getStockBin(binId,productId, false);
        // 如果不存在抛出异常
        if(stockBinDO==null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        if(stockBinDO.getSellableQty()<quantity) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }

        List<WmsInboundItemDO> itemsList=inboundItemService.selectItemListHasAvailableQty(warehouseId,productId);
        if(CollectionUtils.isEmpty(itemsList)) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }

        // 从多个有可用库存的库中，从第一个开始扣除
        List<WmsInboundItemDO> itemsToUpdate=new ArrayList<>();
        List<WmsInboundItemFlowDO> inboundItemFlowList = new ArrayList<>();
        for (WmsInboundItemDO itemDO : itemsList) {
            Integer available=itemDO.getOutboundAvailableQty();
            Integer flowQty=0;
            if(available>quantity) { // 需要多次扣除
                flowQty=quantity;
                available=available-flowQty;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);
                //
                WmsInboundItemFlowDO flowDO=new WmsInboundItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());
                flowDO.setOutboundQty(flowQty);
                flowDO.setOutboundId(outboundId);
                flowDO.setOutboundItemId(outboundItemId);
                inboundItemFlowList.add(flowDO);

            } else if (available.equals(quantity)) { // 刚好单次扣除
                flowQty=available;
                available=0;
                quantity=0;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);
                //
                WmsInboundItemFlowDO flowDO=new WmsInboundItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());
                flowDO.setOutboundQty(flowQty);
                flowDO.setOutboundId(outboundId);
                flowDO.setOutboundItemId(outboundItemId);
                inboundItemFlowList.add(flowDO);

                break;
            } else { // 单次扣除
                flowQty=quantity;
                available=available-flowQty;
                itemDO.setOutboundAvailableQty(available);
                itemsToUpdate.add(itemDO);

                //
                WmsInboundItemFlowDO flowDO=new WmsInboundItemFlowDO();
                flowDO.setOutboundActionId(actionId);
                flowDO.setInboundId(itemDO.getInboundId());
                flowDO.setInboundItemId(itemDO.getId());
                flowDO.setProductId(itemDO.getProductId());
                flowDO.setOutboundQty(flowQty);
                flowDO.setOutboundId(outboundId);
                flowDO.setOutboundItemId(outboundItemId);
                inboundItemFlowList.add(flowDO);

                break;
            }
        }

        // 保存详情与流水
        inboundItemService.saveItems(itemsToUpdate,inboundItemFlowList);


    }

    /**
     * 更新所有者库存
     **/
    @Override
    protected  WmsStockFlowDirection updateStockOwnershipQty(WmsStockOwnershipDO stockOwnershipDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用量
        // stockOwnershipDO.setAvailableQty(stockOwnershipDO.getAvailableQty()-quantity);
        // 待出库量
        stockOwnershipDO.setOutboundPendingQty(stockOwnershipDO.getOutboundPendingQty()+quantity);

        return WmsStockFlowDirection.IN;
    }

    /**
     * 更新库存货位
     **/
    @Override
    protected  WmsStockFlowDirection updateStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用库存
        // stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() - quantity);
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() - quantity);
        // 待出库量
        stockBinDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty() + quantity);

        return WmsStockFlowDirection.OUT;
    }

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

package cn.iocoder.yudao.module.wms.service.quantity;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsInboundItemFlowService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:15
 * @description:
 */

@Component
public class OutboundRejectExecutor extends OutboundExecutor {

    public OutboundRejectExecutor() {
        super(StockReason.OUTBOUND_REJECT);
    }

    @Resource
    @Lazy
    private WmsInboundItemFlowService inboundItemFlowService;


    @Override
    protected void updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, Integer quantity) {
        // 可用量
        stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() + quantity);
        // 可售量
        stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() + quantity);
        // 待出库量
        stockWarehouseDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty() - quantity);
    }

    @Override
    protected Integer getExecuteQuantity(WmsOutboundItemRespVO item) {
        return item.getPlanQty();
    }

    protected void processInboundItem(WmsOutboundRespVO outboundRespVO, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {



        List<WmsInboundItemFlowDO> flowDOList = inboundItemFlowService.selectByActionId(outboundRespVO.getLatestOutboundActionId());

        Long actionId= IdUtil.getSnowflakeNextId();
        outboundRespVO.setLatestOutboundActionId(actionId);

        List<Long> inboundItemIds= StreamX.from(flowDOList).toList(WmsInboundItemFlowDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemsList=inboundItemService.selectByIds(inboundItemIds);
        List<WmsInboundItemFlowDO> inboundItemFlowList = new ArrayList<>();
        Map<Long,WmsInboundItemDO> map=StreamX.from(inboundItemsList).toMap(WmsInboundItemDO::getId);
        for (WmsInboundItemFlowDO flowDO : flowDOList) {
            WmsInboundItemDO inboundItemDO = map.get(flowDO.getInboundItemId());
            Integer qty=flowDO.getOutboundQty();
            inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty()+qty);


            //
            WmsInboundItemFlowDO newFlowDO=new WmsInboundItemFlowDO();
            newFlowDO.setOutboundActionId(actionId);
            newFlowDO.setInboundId(inboundItemDO.getInboundId());
            newFlowDO.setInboundItemId(inboundItemDO.getId());
            newFlowDO.setProductId(inboundItemDO.getProductId());
            newFlowDO.setOutboundQty(qty);
            newFlowDO.setOutboundId(outboundId);
            newFlowDO.setOutboundItemId(outboundItemId);
            inboundItemFlowList.add(newFlowDO);

        }
        // 保存详情与流水
        inboundItemService.saveItems(inboundItemsList,inboundItemFlowList);

    }

    @Override
    protected void updateStockOwnershipQty(WmsStockOwnershipDO stockOwnershipDO, Integer quantity) {
        // 可用量
        stockOwnershipDO.setAvailableQty(stockOwnershipDO.getAvailableQty() + quantity);
        // 待出库量
        stockOwnershipDO.setOutboundPendingQty(stockOwnershipDO.getOutboundPendingQty() - quantity);
    }

    @Override
    protected void updateStockBinQty(WmsStockBinDO stockBinDO, Integer quantity) {
        // 可用库存
        stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() + quantity);
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() + quantity);
        // 待出库量
        stockBinDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty() - quantity);
    }

}

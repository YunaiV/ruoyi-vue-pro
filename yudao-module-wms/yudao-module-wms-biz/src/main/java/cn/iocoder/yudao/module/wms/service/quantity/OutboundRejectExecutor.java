package cn.iocoder.yudao.module.wms.service.quantity;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
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
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsItemFlowService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:15
 * @description:
 */

@Component
public class OutboundRejectExecutor extends OutboundExecutor {

    public OutboundRejectExecutor() {
        super(WmsStockReason.OUTBOUND_REJECT);
    }

    @Resource
    @Lazy
    private WmsItemFlowService itemFlowService;


    /**
     * 更新仓库库存
     **/
    @Override
    protected WmsStockFlowDirection updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用量
        // stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() + quantity);
        // 可售量
        stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() + quantity);
        // 待出库量
        stockWarehouseDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty() - quantity);
//        if(stockWarehouseDO.getOutboundPendingQty()<0) {
//            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
//        }

        return WmsStockFlowDirection.IN;
    }


    @Override
    protected Integer getExecuteQty(WmsOutboundItemRespVO item) {
        return item.getPlanQty();
    }

    /**
     * 更新入库单明细
     **/
    @Override
    protected List<WmsItemFlowDO> processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {


//        List<WmsItemFlowDO> flowDOList = itemFlowService.selectByActionId(outboundRespVO.getLatestOutboundActionId());
        List<WmsItemFlowDO> flowDOList = itemFlowService.selectByOutboundId(outboundId, productId);

        Long actionId= IdUtil.getSnowflakeNextId();
        outboundRespVO.setLatestOutboundActionId(actionId);

        List<Long> inboundItemIds = StreamX.from(flowDOList).toList(WmsItemFlowDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemsList=inboundItemService.selectByIds(inboundItemIds);
        List<WmsItemFlowDO> itemFlowList = new ArrayList<>();
        Map<Long,WmsInboundItemDO> map=StreamX.from(inboundItemsList).toMap(WmsInboundItemDO::getId);
        for (WmsItemFlowDO flowDO : flowDOList) {
            WmsInboundItemDO inboundItemDO = map.get(flowDO.getInboundItemId());
            Integer qty=flowDO.getOutboundAvailableDeltaQty();
            inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty()+qty);

            //
            WmsItemFlowDO newFlowDO = new WmsItemFlowDO();
            newFlowDO.setOutboundActionId(actionId);
            newFlowDO.setInboundId(inboundItemDO.getInboundId());
            newFlowDO.setInboundItemId(inboundItemDO.getId());
            newFlowDO.setProductId(inboundItemDO.getProductId());

            newFlowDO.setBillType(BillType.WMS_OUTBOUND.getValue());
            newFlowDO.setBillId(outboundRespVO.getId());
            newFlowDO.setBillItemId(item.getId());

            newFlowDO.setDirection(WmsStockFlowDirection.IN.getValue());
            newFlowDO.setOutboundAvailableDeltaQty(qty);
            newFlowDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty());

//            newFlowDO.setOutboundQty(qty);
//            newFlowDO.setOutboundId(outboundId);
//            newFlowDO.setOutboundItemId(outboundItemId);
            itemFlowList.add(newFlowDO);

        }
        // 保存详情与流水
        inboundItemService.saveItems(inboundItemsList, itemFlowList);

        return itemFlowList;

    }

    /**
     * 更新逻辑库存
     **/
    @Override
    protected WmsStockFlowDirection updateStockLogicQty(WmsStockLogicDO stockLogicDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用量
        // stockLogicDO.setAvailableQty(stockLogicDO.getAvailableQty() + quantity);
        // 待出库量
        stockLogicDO.setOutboundPendingQty(stockLogicDO.getOutboundPendingQty() - quantity);
//        if (stockLogicDO.getOutboundPendingQty() < 0) {
//            throw exception(STOCK_LOGIC_NOT_ENOUGH);
//        }
        return WmsStockFlowDirection.IN;
    }

    /**
     * 更新库存货位
     **/
    @Override
    protected  WmsStockFlowDirection updateSingleStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item, Integer quantity) {
        // 可用库存
        // stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() + quantity);
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() + quantity);
        // 待出库量
        stockBinDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty() - quantity);
//        if(stockBinDO.getOutboundPendingQty()<0) {
//            throw exception(STOCK_BIN_NOT_ENOUGH);
//        }

        return WmsStockFlowDirection.IN;
    }

//    @Override
//    protected void updateMultiStockBinQty(Long warehouseId, Long productId, WmsOutboundItemRespVO item,Integer quantity) {
//        //TODO 按库存批次出库暂不实现
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

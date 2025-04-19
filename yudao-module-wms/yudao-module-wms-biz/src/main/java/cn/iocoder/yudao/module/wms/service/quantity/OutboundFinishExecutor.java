package cn.iocoder.yudao.module.wms.service.quantity;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
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
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsInboundItemFlowService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_ENOUGH;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_NOT_ENOUGH;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_WAREHOUSE_NOT_ENOUGH;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:15
 * @description:
 */

@Component
public class OutboundFinishExecutor extends OutboundExecutor {

    @Resource
    @Lazy
    private WmsInboundItemFlowService inboundItemFlowService;

    public OutboundFinishExecutor() {
        super(WmsStockReason.OUTBOUND_AGREE);
    }

    @Override
    protected Integer getExecuteQty(WmsOutboundItemRespVO item) {
        // 取消原先的计划入库量，加上本次的实际入库量
        return item.getActualQty() - item.getPlanQty();
    }

    /**
     * 更新仓库库存
     */
    @Override
    protected WmsStockFlowDirection updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity) {

        Integer actualQty=item.getActualQty();

        // 可用量
        stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty() - actualQty);
        if(stockWarehouseDO.getAvailableQty()<0) {
            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
        }
        // 可售量
        stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty() - quantity);
        if(stockWarehouseDO.getSellableQty()<0) {
            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
        }
        // 待出库量
        stockWarehouseDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty() - actualQty);
        if(stockWarehouseDO.getOutboundPendingQty()<0) {
            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
        }

        return WmsStockFlowDirection.OUT;
    }

    /**
     * 更新入库单库存
     */
    protected void processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        List<WmsInboundItemFlowDO> flowDOList = inboundItemFlowService.selectByActionId(outboundRespVO.getLatestOutboundActionId());

        Long actionId= IdUtil.getSnowflakeNextId();
        outboundRespVO.setLatestOutboundActionId(actionId);

        List<Long> inboundItemIds= StreamX.from(flowDOList).toList(WmsInboundItemFlowDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemsList=inboundItemService.selectByIds(inboundItemIds);
        List<WmsInboundItemFlowDO> inboundItemFlowList = new ArrayList<>();
        Map<Long,WmsInboundItemDO> map=StreamX.from(inboundItemsList).toMap(WmsInboundItemDO::getId);
        for (WmsInboundItemFlowDO flowDO : flowDOList) {
            WmsInboundItemDO inboundItemDO = map.get(flowDO.getInboundItemId());
            inboundItemDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty()+quantity);

            // 记录流水
            WmsInboundItemFlowDO newFlowDO=new WmsInboundItemFlowDO();
            newFlowDO.setOutboundActionId(actionId);
            newFlowDO.setInboundId(inboundItemDO.getInboundId());
            newFlowDO.setInboundItemId(inboundItemDO.getId());
            newFlowDO.setProductId(inboundItemDO.getProductId());
            newFlowDO.setOutboundQty(quantity);
            newFlowDO.setOutboundId(outboundId);
            newFlowDO.setOutboundItemId(outboundItemId);
            inboundItemFlowList.add(newFlowDO);

        }
        // 保存详情与流水
        inboundItemService.saveItems(inboundItemsList,inboundItemFlowList);

    }

    /**
     * 更新所有者库存
     **/
    @Override
    protected WmsStockFlowDirection updateStockOwnershipQty(WmsStockOwnershipDO stockOwnershipDO, WmsOutboundItemRespVO item, Integer quantity) {
        Integer actualQty=item.getActualQty();
        // 可用量
        stockOwnershipDO.setAvailableQty(stockOwnershipDO.getAvailableQty() - actualQty);
        if(stockOwnershipDO.getAvailableQty()<0) {
            throw exception(STOCK_OWNERSHIP_NOT_ENOUGH);
        }
        // 待出库量
        stockOwnershipDO.setOutboundPendingQty(stockOwnershipDO.getOutboundPendingQty() - actualQty);
        if(stockOwnershipDO.getOutboundPendingQty()<0) {
            throw exception(STOCK_OWNERSHIP_NOT_ENOUGH);
        }
        return WmsStockFlowDirection.OUT;
    }

    /**
     * 更新库存货位
     **/
    @Override
    protected  WmsStockFlowDirection updateSingleStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item, Integer quantity) {
        Integer actualQty=item.getActualQty();
        // 可用库存
        stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() - actualQty);
        if(stockBinDO.getAvailableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() - quantity);
        if(stockBinDO.getSellableQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        // 待出库量
        stockBinDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty() - actualQty);
        if(stockBinDO.getOutboundPendingQty()<0) {
            throw exception(STOCK_BIN_NOT_ENOUGH);
        }
        return WmsStockFlowDirection.OUT;
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
            itemRespVO.setOutboundStatus(WmsOutboundStatus.ALL.getValue());
        }
        outboundRespVO.setOutboundStatus(WmsOutboundStatus.ALL.getValue());
        outboundRespVO.setOutboundTime(LocalDateTime.now());
    }


}

package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.InventoryContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 盘点数量执行器
 */
@Slf4j
@Component
public class InventoryExecutor extends QuantityExecutor<InventoryContext> {



    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsStockBinMoveService stockBinMoveService;

    public InventoryExecutor() {
        super(WmsStockReason.STOCK_BIN_MOVE);
    }



    @Override
    public void execute(InventoryContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        WmsInventoryDO inventoryDO = context.getInventoryDO();
        List<WmsInventoryBinDO> wmsInventoryBinDOList = context.getWmsInventoryBinDOList();

        // 入库单产品明细(按库位汇总)
        Map<Long, WmsInboundItemSaveReqVO> inboundItemSaveReqVOMap = new HashMap<>();
        // 拣货单产品明细
        List<WmsPickupItemSaveReqVO> pickupItemSaveReqVOList = new ArrayList<>();
        // 出库单产品明细
        List<WmsOutboundItemSaveReqVO> outboundItemSaveReqVOList = new ArrayList<>();

        // 循环盘点明细
        for (WmsInventoryBinDO inventoryBinDO : wmsInventoryBinDOList) {
            int deltaQty = inventoryBinDO.getActualQuantity() - inventoryBinDO.getExpectedQty();

            // 如果盘赢，形成入库单+拣货单
            if (deltaQty > 0) {

                // 入库单明细
                WmsInboundItemSaveReqVO inboundItemSaveReqVO = inboundItemSaveReqVOMap.computeIfAbsent(inventoryBinDO.getProductId(), productId -> {
                    WmsInboundItemSaveReqVO inboundItem = new WmsInboundItemSaveReqVO();
                    inboundItem.setActualQty(0);
                    inboundItem.setPlanQty(0);
                    inboundItem.setProductId(productId);
                    return inboundItem;
                });
                inboundItemSaveReqVO.setPlanQty(inboundItemSaveReqVO.getPlanQty() + deltaQty);
                inboundItemSaveReqVO.setActualQty(inboundItemSaveReqVO.getActualQty() + deltaQty);

                // 拣货单明细
                WmsPickupItemSaveReqVO pickupItemSaveReqVO = new WmsPickupItemSaveReqVO();
                pickupItemSaveReqVO.setProductId(inventoryBinDO.getProductId());
                pickupItemSaveReqVO.setQty(deltaQty);
                pickupItemSaveReqVOList.add(pickupItemSaveReqVO);
            }

            // 如果盘亏，形成出库单
            if (deltaQty > 0) {

                // 拣货单明细
                WmsOutboundItemSaveReqVO outboundItemSaveReqVO = new WmsOutboundItemSaveReqVO();
                outboundItemSaveReqVO.setProductId(inventoryBinDO.getProductId());
                outboundItemSaveReqVO.setPlanQty(deltaQty);
                outboundItemSaveReqVO.setActualQty(deltaQty);
                outboundItemSaveReqVOList.add(outboundItemSaveReqVO);

            }

        }

        // 如果有盘盈的货
        if (!inboundItemSaveReqVOMap.isEmpty()) {
            executeInboundAndPickup(inventoryDO,inboundItemSaveReqVOMap,pickupItemSaveReqVOList);
        }


        // 如果有盘盈的货
        if (!inboundItemSaveReqVOMap.isEmpty()) {
            executeOutbound(inventoryDO,outboundItemSaveReqVOList);
        }




    }

    /**
     * 执行盘点入库并拣货上架
     **/
    private void executeInboundAndPickup(WmsInventoryDO inventoryDO, Map<Long, WmsInboundItemSaveReqVO> inboundItemSaveReqVOMap, List<WmsPickupItemSaveReqVO> pickupItemSaveReqVOList) {

        WmsInboundSaveReqVO inboundSaveReqVO = new WmsInboundSaveReqVO();

        inboundSaveReqVO.setWarehouseId(inventoryDO.getWarehouseId());
        // inboundSaveReqVO.

    }

    /**
     * 执行盘点出库
     **/
    private void executeOutbound(WmsInventoryDO inventoryDO, List<WmsOutboundItemSaveReqVO> outboundItemSaveReqVOList) {
    }



}

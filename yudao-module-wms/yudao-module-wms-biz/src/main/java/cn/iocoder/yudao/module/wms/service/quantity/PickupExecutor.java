package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsInboundItemFlowService;
import cn.iocoder.yudao.module.wms.service.quantity.context.PickupContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 拣货数量执行器
 */
@Slf4j
@Component
public class PickupExecutor extends ActionExecutor<PickupContext> {



    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    @Resource
    @Lazy
    private WmsInboundItemFlowService inboundItemFlowService;

    public PickupExecutor() {
        super(WmsStockReason.PICKUP);
    }

    @Override
    public void execute(PickupContext context) {

        // 确认在事务内
        JdbcUtils.requireTransaction();

        // 准备需要的数据
        WmsPickupDO pickup = context.getPickup();
        List<WmsPickupItemDO> wmsPickupItemDOList = context.getWmsPickupItemDOList();
        List<WmsInboundItemRespVO> inboundItemVOList = context.getInboundItemVOList();
        //
        Map<Long, WmsInboundItemRespVO> inboundItemVOMap = StreamX.from(inboundItemVOList).toMap(WmsInboundItemRespVO::getId);
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(inboundItemVOList).toList(WmsInboundItemRespVO::getInboundId));
        Map<Long, WmsInboundDO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId);
        // 循环明细
        for (WmsPickupItemDO pickupItemDO : wmsPickupItemDOList) {
            WmsInboundItemRespVO inboundItemVO = inboundItemVOMap.get(pickupItemDO.getInboundItemId());
            if (inboundItemVO == null) {
                throw exception(INBOUND_ITEM_NOT_EXISTS);
            }
            // 拣货量不能大于入库量
            if(pickupItemDO.getQty()> inboundItemVO.getActualQty()) {
                throw exception(INBOUND_ITEM_ACTUAL_QTY_ERROR);
            }
            // 拣货量不能大于可拣货的量
            Integer pickupAvaAty = inboundItemVO.getActualQty() - inboundItemVO.getShelvedQty();
            if(pickupItemDO.getQty() > pickupAvaAty) {
                throw exception(INBOUND_ITEM_ACTUAL_QTY_ERROR);
            }
            // 设置已拣货量
            pickupItemDO.setInboundId(inboundItemVO.getInboundId());
            pickupItemDO.setInboundItemId(inboundItemVO.getId());
            pickupItemDO.setProductId(inboundItemVO.getProductId());
            pickupItemDO.setPickupId(pickup.getId());
            // 调整仓位库存
            this.processItem(pickup, pickupItemDO, inboundMap.get(inboundItemVO.getInboundId()), inboundItemVO);
            inboundItemService.updateById(BeanUtils.toBean(inboundItemVO, WmsInboundItemDO.class));
        }

    }


    /**
     * 处理明细行
     **/
    private void processItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {
        this.processStockBin(pickup, pickupItemDO, inboundDO, inboundItemVO);
        this.processStockWarehouseItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
        this.processStockOwnershipItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
        this.processInboundItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
    }


    /**
     * 处理库存仓位
     **/
    private void processStockBin(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {

        JdbcUtils.requireTransaction();
        WmsStockBinDO stockBinDO = stockBinService.getStockBin(pickupItemDO.getBinId(), inboundItemVO.getProductId(), true);

        // 可用库存
        stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() + pickupItemDO.getQty());
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() + pickupItemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(stockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), WmsStockFlowDirection.IN, inboundItemVO.getProductId(), stockBinDO, pickupItemDO.getQty(), pickupItemDO.getPickupId(), pickupItemDO.getId());
    }

    /**
     * 处理库存库位
     **/
    private void processInboundItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {

        Integer quantity = pickupItemDO.getQty();

        Integer shelvedQtyAfterPickup=inboundItemVO.getShelvedQty()+quantity;

        // 判断拣货量是否大于实际入库量
        if(shelvedQtyAfterPickup>inboundItemVO.getActualQty()) {
            throw  exception(INBOUND_ITEM_PICKUP_LEFT_QUANTITY_NOT_ENOUGH);
        }

        // 更新入库记录
        inboundItemVO.setOutboundAvailableQty(inboundItemVO.getOutboundAvailableQty()+quantity);
        inboundItemVO.setShelvedQty(shelvedQtyAfterPickup);

        WmsInboundItemDO inboundItemDO = BeanUtils.toBean(inboundItemVO, WmsInboundItemDO.class);
        inboundItemService.updateById(inboundItemDO);

        // 记录流水
        WmsInboundItemFlowDO flowDO = new WmsInboundItemFlowDO();
        flowDO.setInboundId(inboundItemDO.getInboundId());
        flowDO.setInboundItemId(inboundItemDO.getId());
        flowDO.setProductId(inboundItemDO.getProductId());
        flowDO.setOutboundQty(quantity);
        flowDO.setOutboundId(-1L);
        flowDO.setOutboundItemId(-1L);
        inboundItemFlowService.insert(flowDO);

    }

    /**
     * 处理库存库位
     **/
    private void processStockWarehouseItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {


        // 刷新库存
        Long warehouseId = pickup.getWarehouseId();
        Long productId = inboundItemVO.getProductId();
        Integer quantity = pickupItemDO.getQty();

        //List<WmsStockBinDO> stockBinList = stockBinService.selectStockBin(warehouseId, productId);

//        Integer availableQuantity = 0;
//        Integer sellableQuantity = 0;
//
//        for (WmsStockBinDO wmsStockBinDO : stockBinList) {
//            availableQuantity += wmsStockBinDO.getAvailableQty();
//            sellableQuantity += wmsStockBinDO.getSellableQty();
//        }

        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getStockWarehouse(warehouseId, productId, false);
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        }
        // 可用
        stockWarehouseDO.setAvailableQty(stockWarehouseDO.getAvailableQty()+quantity);
        // 可售
        stockWarehouseDO.setSellableQty(stockWarehouseDO.getSellableQty()+quantity);
        // 待上架
        stockWarehouseDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() - quantity);

        // 更新库存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(),WmsStockFlowDirection.OUT, productId, stockWarehouseDO, quantity, pickup.getId(), pickupItemDO.getId());
    }


    /**
     * 处理库存库位
     **/
    private void processStockOwnershipItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {

        Long warehouseId = pickup.getWarehouseId();
        Long productId = inboundItemVO.getProductId();
        Long companyId = inboundDO.getCompanyId();
        Integer quantity = pickupItemDO.getQty();
        // 刷新库存
        Long deptId = inboundItemVO.getDeptId();
        if (deptId == null) {
            deptId = inboundItemVO.getProduct().getDeptId();
        }
        // 刷新所有者库存
        // wmsStockOwnershipService.refreshForPickup(pickup.getWarehouseId(), inboundDO.getCompanyId(), deptId,inboundItemVO.getProductId(), pickup.getId(), pickupItemDO.getId(),);

        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockOwnershipDO stockOwnershipDO = stockOwnershipService.getByUkProductOwner(warehouseId, companyId, deptId, productId, false);
        // 如果不存在就创建
        if (stockOwnershipDO == null) {
            throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        } else {
            // 如果存在就修改
            // 可用量
            stockOwnershipDO.setAvailableQty(stockOwnershipDO.getAvailableQty() + quantity);
            // 待上架量
            stockOwnershipDO.setShelvingPendingQty(stockOwnershipDO.getShelvingPendingQty() - quantity);
        }
        // 保存
        stockOwnershipService.insertOrUpdate(stockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwnership(this.getReason(), WmsStockFlowDirection.IN, productId, stockOwnershipDO, quantity, pickup.getId(), pickupItemDO.getId());

    }


}

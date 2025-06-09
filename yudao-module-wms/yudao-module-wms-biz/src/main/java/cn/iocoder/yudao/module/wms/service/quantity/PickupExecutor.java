package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
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
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 9:34
 * @description: 拣货数量执行器
 */
@Slf4j
@Component
public class PickupExecutor extends QuantityExecutor<PickupContext> {



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
        inboundItemService.assembleProducts(inboundItemVOList);
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
            Integer pickupAvaAty = inboundItemVO.getActualQty() - inboundItemVO.getShelveClosedQty();
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

        // 更新入库单上架状态
        inboundService.updateShelvingStatus(StreamX.from(inboundItemVOList).toSet(WmsInboundItemRespVO::getInboundId));


    }


    /**
     * 处理明细行
     **/
    private void processItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {
        Long flowId = this.processInboundItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
        this.processStockBin(pickup, pickupItemDO, inboundDO, inboundItemVO,flowId);
        this.processStockWarehouseItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
        this.processStockLogicItem(pickup, pickupItemDO, inboundDO, inboundItemVO);
    }


    /**
     * 处理库存仓位
     **/
    private void processStockBin(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO,Long flowId) {

        JdbcUtils.requireTransaction();
        WmsStockBinDO stockBinDO = stockBinService.getStockBin(pickupItemDO.getBinId(), inboundItemVO.getProductId(), true);

        // 可用库存
        stockBinDO.setAvailableQty(stockBinDO.getAvailableQty() + pickupItemDO.getQty());
        // 可售库存
        stockBinDO.setSellableQty(stockBinDO.getSellableQty() + pickupItemDO.getQty());
        // 保存
        stockBinService.insertOrUpdate(stockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), WmsStockFlowDirection.IN, inboundItemVO.getProductId(), stockBinDO, pickupItemDO.getQty(), pickupItemDO.getPickupId(), pickupItemDO.getId(),flowId);
    }

    /**
     * 处理库存库位
     **/
    private Long processInboundItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {

        Integer quantity = pickupItemDO.getQty();

        Integer ShelveClosedQtyAfterPickup = inboundItemVO.getShelveClosedQty() + quantity;

        // 判断拣货量是否大于实际入库量
        if (ShelveClosedQtyAfterPickup > inboundItemVO.getActualQty()) {
            throw  exception(INBOUND_ITEM_PICKUP_LEFT_QUANTITY_NOT_ENOUGH);
        }

        // 更新入库记录
        inboundItemVO.setOutboundAvailableQty(inboundItemVO.getOutboundAvailableQty()+quantity);
        inboundItemVO.setShelveClosedQty(ShelveClosedQtyAfterPickup);

        WmsInboundItemDO inboundItemDO = BeanUtils.toBean(inboundItemVO, WmsInboundItemDO.class);
        inboundItemService.updateById(inboundItemDO);

        // 记录流水
        WmsInboundItemFlowDO flowDO = new WmsInboundItemFlowDO();
        flowDO.setInboundId(inboundItemDO.getInboundId());
        flowDO.setInboundItemId(inboundItemDO.getId());
        flowDO.setProductId(inboundItemDO.getProductId());

        flowDO.setBillType(BillType.WMS_PICKUP.getValue());
        flowDO.setBillId(pickup.getId());
        flowDO.setBillItemId(pickupItemDO.getId());

        flowDO.setDirection(WmsStockFlowDirection.IN.getValue());
        flowDO.setOutboundAvailableDeltaQty(quantity);
        flowDO.setOutboundAvailableQty(inboundItemDO.getOutboundAvailableQty());
        flowDO.setActualQty(inboundItemDO.getActualQty());
        flowDO.setShelveClosedQty(inboundItemDO.getShelveClosedQty());

        inboundItemFlowService.insert(flowDO);

        return flowDO.getId();

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
        if(stockWarehouseDO.getShelvingPendingQty()<0) {
            throw exception(STOCK_WAREHOUSE_NOT_ENOUGH);
        }
        // 更新库存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(),WmsStockFlowDirection.OUT, productId, stockWarehouseDO, quantity, pickup.getId(), pickupItemDO.getId());
    }


    /**
     * 处理库存库位
     **/
    private void processStockLogicItem(WmsPickupDO pickup, WmsPickupItemDO pickupItemDO, WmsInboundDO inboundDO, WmsInboundItemRespVO inboundItemVO) {

        Long warehouseId = pickup.getWarehouseId();
        Integer quantity = pickupItemDO.getQty();
        Long productId = inboundItemVO.getProductId();

        // 公司ID首先以细行为准，明细行未指定时使用单据中的公司ID
        Long companyId = inboundItemVO.getCompanyId();
        if(companyId==null) {
            companyId = inboundDO.getCompanyId();
        }


        // 部门ID首先考虑明细行，明细行未指定时使用单据中的部门ID
        Long deptId = inboundItemVO.getDeptId();
        if(deptId==null) {
            deptId=inboundDO.getDeptId();
        }
        // 如果入库单及明细上未指定部门,默认按产品的部门ID
        if (deptId == null) {
            deptId = inboundItemVO.getProduct().getDeptId();
        }


        // 刷新逻辑库存
        // wmsStockLogicService.refreshForPickup(pickup.getWarehouseId(), inboundDO.getCompanyId(), deptId,inboundItemVO.getProductId(), pickup.getId(), pickupItemDO.getId(),);

        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockLogicDO stockLogicDO = stockLogicService.getByUkProductOwner(warehouseId, companyId, deptId, productId, false);
        // 如果不存在就创建
        if (stockLogicDO == null) {
            throw exception(STOCK_LOGIC_NOT_EXISTS);
        } else {
            // 如果存在就修改
            // 可用量
            stockLogicDO.setAvailableQty(stockLogicDO.getAvailableQty() + quantity);
            // 待上架量
            stockLogicDO.setShelvePendingQty(stockLogicDO.getShelvePendingQty() - quantity);
            if (stockLogicDO.getShelvePendingQty() < 0) {
                throw exception(STOCK_LOGIC_NOT_ENOUGH);
            }
        }
        // 保存
        stockLogicService.insertOrUpdate(stockLogicDO);
        // 记录流水
        stockFlowService.createForStockLogic(this.getReason(), WmsStockFlowDirection.IN, productId, stockLogicDO, quantity, pickup.getId(), pickupItemDO.getId());

    }


}

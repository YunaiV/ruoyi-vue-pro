package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:16
 * @description: 出库执行器
 */
@Slf4j
public abstract class OutboundExecutor extends ActionExecutor<OutboundContext> {

    @Resource
    protected WmsOutboundService outboundService;

    @Resource
    protected WmsInboundItemService inboundItemService;

    public OutboundExecutor(WmsStockReason reason) {
        super(reason);
    }

    /**
     * 根据不同的业务动作获得执行量
     **/
    protected abstract Integer getExecuteQty(WmsOutboundItemRespVO item);
    /**
     * 更新仓库库存量
     **/
    protected abstract WmsStockFlowDirection updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity);
    /**
     * 更新库存货位库存量
     **/
    protected abstract void processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId);
    /**
     * 更新库存货位库存量
     **/
    protected abstract WmsStockFlowDirection updateStockOwnershipQty(WmsStockOwnershipDO stockOwnershipDO, WmsOutboundItemRespVO item, Integer quantity);
    /**
     * 更新库存货位库存量
     **/
    protected abstract WmsStockFlowDirection updateStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item,Integer quantity);
    /**
     * 更新出库单
     **/
    protected abstract void updateOutbound(WmsOutboundRespVO outboundRespVO);

    @Override
    public void execute(OutboundContext context) {

        WmsOutboundRespVO outboundRespVO=outboundService.getOutboundWithItemList(context.getOutboundId());
        Long warehouseId = outboundRespVO.getWarehouseId();
        Long companyId = outboundRespVO.getCompanyId();
        Long outboundDeptId = outboundRespVO.getDeptId();

        List<WmsOutboundItemRespVO> itemList=outboundRespVO.getItemList();
        for (WmsOutboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            Long deptId = outboundDeptId;
            // 如果入库单上未指定部门,默认按产品的部门ID
            if (deptId == null) {
                WmsProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 执行入库的原子操作
            Integer quantity= getExecuteQty(item);
            outboundSingleItem(outboundRespVO,item,companyId, deptId, warehouseId, item.getBinId(),productId, quantity, outboundRespVO.getId(), item.getId());
        }
        updateOutbound(outboundRespVO);
        // 完成最终的入库
        outboundService.finishOutbound(outboundRespVO);

    }

    /**
     * 处理单个详情
     **/
    private WmsOutboundStatus outboundSingleItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        WmsOutboundStatus status = WmsOutboundStatus.NONE;
        try {
            status=this.processItem(outboundRespVO,item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        } catch (Exception e) {
            log.error("outboundSingleItemTransactional Error", e);
            throw e;
        }
        return status;
    }


    /**
     * 按不同的分类处理库存
     **/
    private WmsOutboundStatus processItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        this.processStockWarehouseItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processInboundItem(outboundRespVO,item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processStockOwnerShipItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processStockBinItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        // 当前逻辑,默认全部入库
        return WmsOutboundStatus.ALL;
    }


    /**
     * 处理仓库库存
     **/
    private void processStockWarehouseItem(WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getByWarehouseIdAndProductId(warehouseId, productId);

        WmsStockFlowDirection wmsStockFlowDirection = null;
        // 如果没有就创建
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        } else {
            wmsStockFlowDirection = this.updateStockWarehouseQty(stockWarehouseDO, item, quantity);
        }

        // 更新库存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(),wmsStockFlowDirection, productId, stockWarehouseDO, quantity, outboundId, outboundItemId);

    }


    /**
     * 处理所有者库存
     **/
    private void processStockOwnerShipItem(WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockOwnershipDO stockOwnershipDO = stockOwnershipService.getByUkProductOwner(warehouseId, companyId, deptId, productId);
        WmsStockFlowDirection wmsStockFlowDirection = null;
        // 如果不存在就创建
        if (stockOwnershipDO == null) {
            throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        } else { // 如果存在就修改
            wmsStockFlowDirection = this.updateStockOwnershipQty(stockOwnershipDO, item, quantity);
        }
        // 保存
        stockOwnershipService.insertOrUpdate(stockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwnership(this.getReason(),wmsStockFlowDirection, productId, stockOwnershipDO,quantity, outboundId, outboundItemId);
    }


    /**
     * 处理仓位库存
     **/
    private void processStockBinItem(WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 调整仓位库存
        JdbcUtils.requireTransaction();
        WmsStockBinDO stockBinDO = stockBinService.getStockBin(binId, productId);
        WmsStockFlowDirection wmsStockFlowDirection = null;
        if(stockBinDO==null) {
            throw exception(STOCK_BIN_NOT_EXISTS);
        } else {
            wmsStockFlowDirection=this.updateStockBinQty(stockBinDO,item,quantity);
        }
        // 保存
        stockBinService.insertOrUpdate(stockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(),wmsStockFlowDirection,productId, stockBinDO,quantity, outboundId, outboundItemId);
    }

}

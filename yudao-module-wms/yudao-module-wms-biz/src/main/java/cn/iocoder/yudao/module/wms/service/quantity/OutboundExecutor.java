package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 13:16
 * @description:
 */
@Slf4j
public abstract class OutboundExecutor extends ActionExecutor<OutboundContext> {

    @Resource
    protected WmsOutboundService outboundService;

    @Resource
    protected WmsInboundItemService inboundItemService;

    public OutboundExecutor(StockReason reason) {
        super(reason);
    }

    protected abstract Integer getExecuteQuantity(WmsOutboundItemRespVO item);
    protected abstract void updateStockWarehouseQty(WmsStockWarehouseDO stockWarehouseDO, WmsOutboundItemRespVO item, Integer quantity);
    protected abstract void processInboundItem(WmsOutboundRespVO outboundRespVO, WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId);
    protected abstract void updateStockOwnershipQty(WmsStockOwnershipDO stockOwnershipDO, WmsOutboundItemRespVO item, Integer quantity);
    protected abstract void updateStockBinQty(WmsStockBinDO stockBinDO, WmsOutboundItemRespVO item,Integer quantity);
    protected abstract void updateOutbound(WmsOutboundRespVO outboundRespVO);

    @Override
    @Transactional
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
                ErpProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 执行入库的原子操作
            Integer quantity=getExecuteQuantity(item);
            outboundSingleItemAtomically(outboundRespVO,item,companyId, deptId, warehouseId, item.getBinId(),productId, quantity, outboundRespVO.getId(), item.getId());
        }
        updateOutbound(outboundRespVO);
        // 完成最终的入库
        outboundService.finishOutbound(outboundRespVO);

    }

    /**
     * 执行出库的原子操作,以加锁的方式单个出入库
     */
    private OutboundStatus outboundSingleItemAtomically(WmsOutboundRespVO outboundRespVO,WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        AtomicReference<OutboundStatus> status = new AtomicReference<>();
        lockRedisDAO.lockStockLevels(warehouseId, productId, () -> {
            try {
                OutboundStatus outboundStatus=this.processItem(outboundRespVO,item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
                status.set(outboundStatus);
            } catch (Exception e) {
                log.error("outboundSingleItemTransactional Error", e);
                throw e;
            }
        });
        return status.get();
    }


    private OutboundStatus processItem(WmsOutboundRespVO outboundRespVO,WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        this.processStockWarehouseItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processInboundItem(outboundRespVO,item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processStockOwnerShipItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        this.processStockBinItem(item,companyId, deptId, warehouseId, binId, productId, quantity, outboundId, outboundItemId);
        // 当前逻辑,默认全部入库
        return OutboundStatus.ALL;
    }



    /**
     * 在事务中执行出库操作
     */

    private void processStockWarehouseItem(WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getByWarehouseIdAndProductId(warehouseId, productId);


        // 如果没有就创建
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        } else {
            this.updateStockWarehouseQty(stockWarehouseDO, item,quantity);
        }

        // 更新库存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(), productId, stockWarehouseDO, quantity, outboundId, outboundItemId);

    }



    private void processStockOwnerShipItem(WmsOutboundItemRespVO item, Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {

        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockOwnershipDO stockOwnershipDO = stockOwnershipService.getByUkProductOwner(warehouseId, companyId, deptId, productId);
        // 如果不存在就创建
        if (stockOwnershipDO == null) {
            throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        } else { // 如果存在就修改
            this.updateStockOwnershipQty(stockOwnershipDO,item,quantity);
        }
        // 保存
        stockOwnershipService.insertOrUpdate(stockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwner(this.getReason(), productId, stockOwnershipDO,quantity, outboundId, outboundItemId);
    }


    private void processStockBinItem(WmsOutboundItemRespVO item,Long companyId, Long deptId, Long warehouseId, Long binId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 调整仓位库存
        JdbcUtils.requireTransaction();
        WmsStockBinDO stockBinDO = stockBinService.getStockBin(binId, productId);
        if(stockBinDO==null) {
            throw exception(STOCK_BIN_NOT_EXISTS);
        } else {
            this.updateStockBinQty(stockBinDO,item,quantity);
        }
        // 保存
        stockBinService.insertOrUpdate(stockBinDO);
        // 记录流水
        stockFlowService.createForStockBin(this.getReason(), productId, stockBinDO,quantity, outboundId, outboundItemId);
    }




}

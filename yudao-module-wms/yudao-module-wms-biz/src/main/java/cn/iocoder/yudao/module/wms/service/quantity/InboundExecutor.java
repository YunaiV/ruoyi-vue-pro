package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:41
 * @description:
 */

@Slf4j
@Component
public class InboundExecutor extends ActionExecutor<InboundContext> {


    public InboundExecutor() {
        super(StockReason.INBOUND);
    }

    @Override
    @Transactional
    public void execute(InboundContext context) {

        WmsInboundRespVO inboundRespVO = inboundService.getInboundWithItemList(context.getInboundId());

        Long warehouseId = inboundRespVO.getWarehouseId();
        Long companyId = inboundRespVO.getCompanyId();
        Long inboundDeptId = inboundRespVO.getDeptId();

        List<WmsInboundItemRespVO> itemList = inboundRespVO.getItemList();
        for (WmsInboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            Long deptId = inboundDeptId;
            // 如果入库单上未指定部门,默认按产品的部门ID
            if (deptId == null) {
                ErpProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 执行入库的原子操作
            InboundStatus inboundStatus = inboundSingleItemAtomically(companyId, deptId, warehouseId, productId, item.getActualQty(), inboundRespVO.getId(), item.getId());
            item.setInboundStatus(inboundStatus.getValue());

        }
        // 完成最终的入库
        inboundService.finishInbound(inboundRespVO);

    }

    /**
     * 执行入库的原子操作,以加锁的方式单个出入库
     */
    private InboundStatus inboundSingleItemAtomically(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        AtomicReference<InboundStatus> status = new AtomicReference<>();
        lockRedisDAO.lockStockLevels(warehouseId, productId, () -> {
            try {
                InboundStatus inboundStatus = this.processItem(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId);
                status.set(inboundStatus);
            } catch (Exception e) {
                log.error("inboundSingleItemTransactional Error" , e);
                throw e;
            }
        });
        return status.get();
    }

    protected InboundStatus processItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {

        // 调整仓库库存
        this.processStockWarehouseItem(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId);
        // 调整归属库存
        this.processStockOwnershipItem(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId);
        // 当前逻辑,默认全部入库
        return InboundStatus.ALL;
    }

    /**
     * 调整仓库库存
     **/
    protected void processStockWarehouseItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getByWarehouseIdAndProductId(warehouseId, productId);
        // 如果没有就创建
        if (stockWarehouseDO == null) {
            stockWarehouseDO = new WmsStockWarehouseDO();
            stockWarehouseDO.setWarehouseId(warehouseId);
            stockWarehouseDO.setProductId(productId);
            // 待上架量
            stockWarehouseDO.setShelvingPendingQty(quantity);
        } else {
            // 待上架量
            stockWarehouseDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + quantity);
        }
        // 保存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(), productId, stockWarehouseDO, quantity, inboundId, inboundItemId);

    }


    /**
     * 调整归属库存
     * 此方法必须包含在 WmsStockWarehouseServiceImpl.inboundSingleItemTransactional 方法中
     */
    private void processStockOwnershipItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockOwnershipDO stockOwnershipDO = stockOwnershipService.getByUkProductOwner(warehouseId, companyId, deptId, productId);
        // 如果不存在就创建
        if (stockOwnershipDO == null) {
            stockOwnershipDO = new WmsStockOwnershipDO();
            //
            stockOwnershipDO.setCompanyId(companyId);
            stockOwnershipDO.setDeptId(deptId);
            stockOwnershipDO.setWarehouseId(warehouseId);
            stockOwnershipDO.setProductId(productId);
            // 待上架量
            stockOwnershipDO.setShelvingPendingQty(quantity);
        } else {
            // 待上架量
            stockOwnershipDO.setShelvingPendingQty(stockOwnershipDO.getShelvingPendingQty() + quantity);
        }
        // 保存
        stockOwnershipService.insertOrUpdate(stockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwner(this.getReason(), productId, stockOwnershipDO, quantity, inboundId, inboundItemId);
    }


}

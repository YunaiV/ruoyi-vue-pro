package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:41
 * @description: 入库单执行器
 */

@Slf4j
@Component
public class InboundExecutor extends ActionExecutor<InboundContext> {


    public InboundExecutor() {
        super(WmsStockReason.INBOUND);
    }


    @Override
    public void execute(InboundContext context) {

        final WmsInboundRespVO inboundRespVO = inboundService.getInboundWithItemList(context.getInboundId());
        Long warehouseId = inboundRespVO.getWarehouseId();
        Long companyId = inboundRespVO.getCompanyId();
        Long inboundDeptId = inboundRespVO.getDeptId();

        List<WmsInboundItemRespVO> itemList = inboundRespVO.getItemList();
        for (WmsInboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            Long deptId = inboundDeptId;
            // 如果入库单上未指定部门,默认按产品的部门ID
            if (deptId == null) {
                WmsProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 如果实际量未填，则按计划量入库
            if(item.getActualQty()==null) {
                item.setActualQty(item.getPlanQty());
            }
            // 执行入库的原子操作
            WmsInboundStatus inboundStatus = inboundSingleItem(companyId, deptId, warehouseId, productId, item.getPlanQty(), item.getActualQty(), inboundRespVO.getId(), item.getId());
            item.setInboundStatus(inboundStatus.getValue());

        }
        // 完成最终的入库
        inboundService.finishInbound(inboundRespVO);

    }

    /**
     * 执行入库
     */
    private WmsInboundStatus inboundSingleItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer planQuantity, Integer actualQuantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        AtomicReference<WmsInboundStatus> status = new AtomicReference<>();

        try {
            WmsInboundStatus inboundStatus = this.processItem(companyId, deptId, warehouseId, productId, planQuantity,actualQuantity, inboundId, inboundItemId);
            status.set(inboundStatus);
        } catch (Exception e) {
            log.error("inboundSingleItem Error" , e);
            throw e;
        }

        return status.get();
    }

    /**
     * 处理单个详情
     **/
    protected WmsInboundStatus processItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer planQuantity, Integer actualQuantity, Long inboundId, Long inboundItemId) {

        // 调整仓库库存
        this.processStockWarehouseItem(companyId, deptId, warehouseId, productId, actualQuantity, inboundId, inboundItemId);
        // 调整归属库存
        this.processStockOwnershipItem(companyId, deptId, warehouseId, productId, actualQuantity, inboundId, inboundItemId);
        // 当前逻辑,默认全部入库
        if(actualQuantity==0) {
            return WmsInboundStatus.NONE;
        } else if(actualQuantity<planQuantity) {
            return WmsInboundStatus.PART;
        } else {
            return WmsInboundStatus.ALL;
        }
    }

    /**
     * 调整仓库库存
     **/
    protected void processStockWarehouseItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer actualQuantity, Long inboundId, Long inboundItemId) {
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
            stockWarehouseDO.setShelvingPendingQty(actualQuantity);
        } else {
            // 待上架量
            stockWarehouseDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + actualQuantity);
        }
        // 保存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(), productId, stockWarehouseDO, actualQuantity, inboundId, inboundItemId);

    }


    /**
     * 调整归属库存
     */
    private void processStockOwnershipItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer actualQuantity, Long inboundId, Long inboundItemId) {
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
            stockOwnershipDO.setShelvingPendingQty(actualQuantity);
        } else {
            // 待上架量
            stockOwnershipDO.setShelvingPendingQty(stockOwnershipDO.getShelvingPendingQty() + actualQuantity);
        }
        // 保存
        stockOwnershipService.insertOrUpdate(stockOwnershipDO);
        // 记录流水
        stockFlowService.createForStockOwner(this.getReason(), productId, stockOwnershipDO, actualQuantity, inboundId, inboundItemId);
    }


}

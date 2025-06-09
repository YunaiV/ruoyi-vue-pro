package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author: LeeFJ
 * @date: 2025/3/25 8:41
 * @description: 入库单执行器
 */

@Slf4j
@Component
public class InboundExecutor extends QuantityExecutor<InboundContext> {


    public InboundExecutor() {
        super(WmsStockReason.INBOUND);
    }


    @Override
    public void execute(InboundContext context) {

        final WmsInboundRespVO inboundRespVO = inboundService.getInboundWithItemList(context.getInboundId());
        Long warehouseId = inboundRespVO.getWarehouseId();



        List<WmsInboundItemRespVO> itemList = inboundRespVO.getItemList();
        for (WmsInboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            // 公司ID首先以细行为准，明细行未指定时使用单据中的公司ID
            Long companyId = item.getCompanyId();
            if(companyId==null) {
                companyId=inboundRespVO.getCompanyId();
            }
            // 部门ID首先考虑明细行，明细行未指定时使用单据中的部门ID
            Long deptId = item.getDeptId();
            if(deptId==null) {
                deptId=inboundRespVO.getDeptId();
            }
            // 如果入库单及明细上未指定部门,默认按产品的部门ID
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
            item.setInboundCompanyId(companyId);
            item.setInboundDeptId(deptId);
            //仓库信息更新
            this.updateStockWarehouse(inboundRespVO, item);
        }
        // 完成最终的入库
        inboundService.finishInbound(inboundRespVO);

    }

    private void updateStockWarehouse(WmsInboundRespVO inboundRespVO, WmsInboundItemRespVO item) {
        WmsStockWarehouseDO stockWarehouse = stockWarehouseService.getStockWarehouse(inboundRespVO.getWarehouseId(), item.getProductId(), FALSE);
        WmsStockWarehouseSaveReqVO createReqVO = BeanUtils.toBean(stockWarehouse, WmsStockWarehouseSaveReqVO.class);
        createReqVO.setTransitQty(item.getPlanQty());
        stockWarehouseService.updateStockWarehouse(createReqVO);
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
        this.processStockLogicItem(companyId, deptId, warehouseId, productId, actualQuantity, inboundId, inboundItemId);
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
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getStockWarehouse(warehouseId, productId, true);
        // 待上架量
        stockWarehouseDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + actualQuantity);
        // 保存
        stockWarehouseService.insertOrUpdate(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(this.getReason(), WmsStockFlowDirection.IN,productId, stockWarehouseDO, actualQuantity, inboundId, inboundItemId);

    }


    /**
     * 调整归属库存
     */
    private void processStockLogicItem(Long companyId, Long deptId, Long warehouseId, Long productId, Integer actualQuantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 查询库存记录
        WmsStockLogicDO stockLogicDO = stockLogicService.getByUkProductOwner(warehouseId, companyId, deptId, productId, true);

        // 待上架量
        stockLogicDO.setShelvePendingQty(stockLogicDO.getShelvePendingQty() + actualQuantity);

        // 保存
        stockLogicService.insertOrUpdate(stockLogicDO);
        // 记录流水
        stockFlowService.createForStockLogic(this.getReason(), WmsStockFlowDirection.IN, productId, stockLogicDO, actualQuantity, inboundId, inboundItemId);
    }


    public static void setShelveAvailableQty(List<? extends WmsInboundItemRespVO> items) {
        items.forEach(item -> {
            item.setShelveAvailableQty(item.getActualQty() - item.getShelveClosedQty());
        });
    }

    public static void setShelveAvailableQty(Collection<? extends WmsInboundItemFlowSimpleVO> items) {
        items.forEach(item -> {
            item.setShelveAvailableQty(item.getActualQty() - item.getShelveClosedQty());
        });
    }

    public void updateTransitQty(WmsInboundRespVO inbound) {
        List<WmsInboundItemRespVO> itemList = inbound.getItemList();
        for (WmsInboundItemRespVO item : itemList) {
            WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getStockWarehouse(inbound.getWarehouseId(), item.getProductId(), TRUE);
            stockWarehouseDO.setTransitQty(stockWarehouseDO.getTransitQty() + item.getPlanQty());
            stockWarehouseService.updateStockWarehouse(BeanUtils.toBean(stockWarehouseDO, WmsStockWarehouseSaveReqVO.class));
        }
    }
}

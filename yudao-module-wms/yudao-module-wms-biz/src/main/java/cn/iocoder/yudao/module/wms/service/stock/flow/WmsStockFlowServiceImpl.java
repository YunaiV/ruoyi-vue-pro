package cn.iocoder.yudao.module.wms.service.stock.flow;

import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.enums.stock.StockType;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库存流水 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsStockFlowServiceImpl implements WmsStockFlowService {

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

    /**
     * @sign : 5CDC0A12A8B023F4
     */
    @Override
    public WmsStockFlowDO createStockFlow(WmsStockFlowSaveReqVO createReqVO) {
        // 插入
        WmsStockFlowDO stockFlow = BeanUtils.toBean(createReqVO, WmsStockFlowDO.class);
        stockFlowMapper.insert(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 92744785BC7A4404
     */
    @Override
    public WmsStockFlowDO updateStockFlow(WmsStockFlowSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockFlowDO exists = validateStockFlowExists(updateReqVO.getId());
        // 更新
        WmsStockFlowDO stockFlow = BeanUtils.toBean(updateReqVO, WmsStockFlowDO.class);
        stockFlowMapper.updateById(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 2C67EEBE8FF6B925
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockFlow(Long id) {
        // 校验存在
        WmsStockFlowDO stockFlow = validateStockFlowExists(id);
        // 删除
        stockFlowMapper.deleteById(id);
    }

    /**
     * @sign : 9FD17EF243CE9F52
     */
    private WmsStockFlowDO validateStockFlowExists(Long id) {
        WmsStockFlowDO stockFlow = stockFlowMapper.selectById(id);
        if (stockFlow == null) {
            throw exception(STOCK_FLOW_NOT_EXISTS);
        }
        return stockFlow;
    }

    @Override
    public WmsStockFlowDO getStockFlow(Long id) {
        return stockFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockFlowDO> getStockFlowPage(WmsStockFlowPageReqVO pageReqVO) {
        return stockFlowMapper.selectPage(pageReqVO);
    }

    @Override
    public WmsStockFlowDO getLastFlow(Long warehouseId, Integer stockType, Long stockId) {
        return stockFlowMapper.getLastFlow(warehouseId, stockType, stockId);
    }

    /**
     * 为入库创建流水
     * 此方法必须包含在 WmsStockWarehouseServiceImpl.inboundSingleItemTransactional 方法中
     */
    @Override
    public void createForInbound(Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId, WmsStockWarehouseDO stockWarehouseDO) {
        StockType stockType = StockType.WAREHOUSE;
        StockReason reason = StockReason.INBOUND;
        // 获取上一个流水
        WmsStockFlowDO lastStockFlowDO = this.getLastFlow(warehouseId, stockType.getValue(), stockWarehouseDO.getId());
        // 创建本次流水
        WmsStockFlowDO stockFlowDO = new WmsStockFlowDO();
        stockFlowDO.setWarehouseId(warehouseId);
        stockFlowDO.setProductId(productId);
        stockFlowDO.setStockType(stockType.getValue());
        stockFlowDO.setStockId(stockWarehouseDO.getId());
        stockFlowDO.setReason(reason.getValue());
        stockFlowDO.setReasonBillId(inboundId);
        stockFlowDO.setReasonItemId(inboundItemId);
        if (lastStockFlowDO != null) {
            stockFlowDO.setPrevFlowId(lastStockFlowDO.getId());
        } else {
            stockFlowDO.setPrevFlowId(0L);
        }
        // 变更量
        stockFlowDO.setDeltaQuantity(quantity);
        // 采购计划量
        stockFlowDO.setPurchasePlanQuantity(stockWarehouseDO.getPurchasePlanQuantity());
        // 采购在途量
        stockFlowDO.setPurchaseTransitQuantity(stockWarehouseDO.getPurchaseTransitQuantity());
        // 退货在途量
        stockFlowDO.setReturnTransitQuantity(stockWarehouseDO.getReturnTransitQuantity());
        // 可售量，未被单据占用的良品数量
        stockFlowDO.setSellableQuantity(stockWarehouseDO.getSellableQuantity());
        // 可用量，在库的良品数量
        stockFlowDO.setAvailableQuantity(stockWarehouseDO.getAvailableQuantity());
        // 待上架数量
        stockFlowDO.setShelvingPendingQuantity(stockWarehouseDO.getShelvingPendingQuantity() + quantity);
        // 不良品数量
        stockFlowDO.setDefectiveQuantity(stockWarehouseDO.getDefectiveQuantity());
        // 待出库量
        stockFlowDO.setOutboundPendingQuantity(stockWarehouseDO.getOutboundPendingQuantity());
        // 
        stockFlowDO.setFlowTime(new Timestamp(System.currentTimeMillis()));
        // 保存
        stockFlowMapper.insert(stockFlowDO);
        // 关联前项流水
        if (lastStockFlowDO != null) {
            lastStockFlowDO.setNextFlowId(stockFlowDO.getId());
            this.updateStockFlow(BeanUtils.toBean(lastStockFlowDO, WmsStockFlowSaveReqVO.class));
        }
    }

    /**
     * 为入库创建流水
     * 此方法必须包含在 WmsStockOwnershipServiceImpl.inboundSingleItem 方法中
     */
    @Override
    public void createForInbound(Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId, WmsStockOwnershipDO stockOwnershipDO) {
        StockType stockType = StockType.OWNER;
        StockReason reason = StockReason.INBOUND;
        // 获取上一个流水
        WmsStockFlowDO lastStockFlowDO = this.getLastFlow(warehouseId, stockType.getValue(), stockOwnershipDO.getId());
        // 创建本次流水
        WmsStockFlowDO stockFlowDO = new WmsStockFlowDO();
        stockFlowDO.setWarehouseId(warehouseId);
        stockFlowDO.setProductId(productId);
        stockFlowDO.setStockType(stockType.getValue());
        stockFlowDO.setStockId(stockOwnershipDO.getId());
        stockFlowDO.setReason(reason.getValue());
        stockFlowDO.setReasonBillId(inboundId);
        stockFlowDO.setReasonItemId(inboundItemId);
        if (lastStockFlowDO != null) {
            stockFlowDO.setPrevFlowId(lastStockFlowDO.getId());
        } else {
            stockFlowDO.setPrevFlowId(0L);
        }
        // 变更量
        stockFlowDO.setDeltaQuantity(quantity);
        // 采购计划量
        // stockFlowDO.setPurchasePlanQuantity(stockOwnershipDO.getPurchasePlanQuantity());
        // 采购在途量
        // stockFlowDO.setPurchaseTransitQuantity(stockOwnershipDO.getPurchaseTransitQuantity());
        // 退货在途量
        // stockFlowDO.setReturnTransitQuantity(stockOwnershipDO.getReturnTransitQuantity());
        // 可售量，未被单据占用的良品数量
        // stockFlowDO.setSellableQuantity(stockOwnershipDO.getSellableQuantity());
        // 可用量，在库的良品数量
        stockFlowDO.setAvailableQuantity(stockOwnershipDO.getAvailableQuantity());
        // 待上架数量
        stockFlowDO.setShelvingPendingQuantity(stockOwnershipDO.getShelvingPendingQuantity() + quantity);
        // 不良品数量
        // stockFlowDO.setDefectiveQuantity(stockOwnershipDO.getDefectiveQuantity());
        // 待出库量
        stockFlowDO.setOutboundPendingQuantity(stockOwnershipDO.getOutboundPendingQuantity());
        // 
        stockFlowDO.setFlowTime(new Timestamp(System.currentTimeMillis()));
        // 保存
        stockFlowMapper.insert(stockFlowDO);
        // 关联前项流水
        if (lastStockFlowDO != null) {
            lastStockFlowDO.setNextFlowId(stockFlowDO.getId());
            this.updateStockFlow(BeanUtils.toBean(lastStockFlowDO, WmsStockFlowSaveReqVO.class));
        }
    }
}
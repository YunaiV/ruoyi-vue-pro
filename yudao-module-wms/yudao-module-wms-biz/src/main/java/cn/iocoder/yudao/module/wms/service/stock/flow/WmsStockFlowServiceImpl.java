package cn.iocoder.yudao.module.wms.service.stock.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.service.exchange.WmsExchangeService;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsItemFlowService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.pickup.WmsPickupService;
import cn.iocoder.yudao.module.wms.service.quantity.InboundExecutor;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import cn.iocoder.yudao.module.wms.service.stock.logic.WmsStockLogicService;
import cn.iocoder.yudao.module.wms.service.stock.logic.move.WmsStockLogicMoveService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.wms.service.stockcheck.WmsStockCheckService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCK_FLOW_NOT_EXISTS;

/**
 * 库存流水 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsStockFlowServiceImpl implements WmsStockFlowService {

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    @Lazy
    private WmsExchangeService exchangeService;

    @Resource
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;

    @Resource
    @Lazy
    private WmsPickupService pickupService;

    @Resource
    @Lazy
    private WmsStockCheckService stockCheckService;

    @Resource
    @Lazy
    private WmsStockBinMoveService stockBinMoveService;

    @Resource
    @Lazy
    private WmsStockLogicMoveService stockLogicMoveService;

    @Resource
    @Lazy
    private WmsStockLogicService stockLogicService;

    @Resource
    @Lazy
    private WmsStockWarehouseService stockWarehouseService;

    @Resource
    @Lazy
    private WmsItemFlowService itemFlowService;

    @Resource
    private DeptApi deptApi;

    @Resource
    private FmsCompanyApi companyApi;

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
     * 创建仓库库存变化流水(原方法)
     */
//    @Override
//    public void createForStockWarehouse(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockWarehouseDO stockWarehouseDO, Integer quantity, Long reasonId, Long reasonItemId) {
//        createFor(reason, WmsStockType.WAREHOUSE, direction, stockWarehouseDO.getId(), stockWarehouseDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
//            // 在制量
//            stockFlowDO.setMakePendingQty(stockWarehouseDO.getMakePendingQty());
//            // 在途量
//            stockFlowDO.setTransitQty(stockWarehouseDO.getTransitQty());
//            // 退货在途量
//            stockFlowDO.setReturnTransitQty(stockWarehouseDO.getReturnTransitQty());
//            // 可售量，未被单据占用的良品数量
//            stockFlowDO.setSellableQty(stockWarehouseDO.getSellableQty());
//            // 可用量，在库的良品数量
//            stockFlowDO.setAvailableQty(stockWarehouseDO.getAvailableQty());
//            // 待上架数量
//            stockFlowDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + quantity);
//            // 不良品数量
//            stockFlowDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty());
//            // 待出库量
//            stockFlowDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty());
//        });
//    }

    /**
     * 创建仓库库存变化流水(新方法)
     */
    @Override
    public void createForStockWarehouse(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockWarehouseDO stockWarehouseDO, Integer quantity, Long reasonId, Long reasonItemId, Integer beforeQty, Integer afterQty, Long inboundId) {
        createFor(reason, WmsStockType.WAREHOUSE, direction, stockWarehouseDO.getId(), stockWarehouseDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, null, beforeQty, afterQty, inboundId, stockFlowDO -> {
            // 在制量
            stockFlowDO.setMakePendingQty(stockWarehouseDO.getMakePendingQty());
            // 在途量
            stockFlowDO.setTransitQty(stockWarehouseDO.getTransitQty());
            // 退货在途量
            stockFlowDO.setReturnTransitQty(stockWarehouseDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            stockFlowDO.setSellableQty(stockWarehouseDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockWarehouseDO.getAvailableQty());
            // 待上架数量
            stockFlowDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + quantity);
            // 不良品数量
            stockFlowDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty());
        });
    }

//    /**
//     * 创建逻辑库存变化流水(原方法)
//     */
//    @Override
//    public void createForStockLogic(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockLogicDO stockLogicDO, Integer quantity, Long reasonId, Long reasonItemId) {
//        createFor(reason, WmsStockType.LOGIC, direction, stockLogicDO.getId(), stockLogicDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
//            // 采购计划量
//            // stockFlowDO.setPurchasePlanQty(stockLogicDO.getPurchasePlanQty());
//            // 采购在途量
//            // stockFlowDO.setPurchaseTransitQty(stockLogicDO.getPurchaseTransitQty());
//            // 退货在途量
//            // stockFlowDO.setReturnTransitQty(stockLogicDO.getReturnTransitQty());
//            // 可售量，未被单据占用的良品数量
//            // stockFlowDO.setSellableQty(stockLogicDO.getSellableQty());
//            // 可用量，在库的良品数量
//            stockFlowDO.setAvailableQty(stockLogicDO.getAvailableQty());
//            // 待上架数量
//            stockFlowDO.setShelvingPendingQty(stockLogicDO.getShelvePendingQty() + quantity);
//            // 不良品数量
//            // stockFlowDO.setDefectiveQty(stockLogicDO.getItemQty());
//            // 待出库量
//            stockFlowDO.setOutboundPendingQty(stockLogicDO.getOutboundPendingQty());
//        });
//    }

    /**
     * 创建逻辑库存变化流水(新方法)
     */
    @Override
    public void createForStockLogic(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockLogicDO stockLogicDO, Integer quantity, Long reasonId, Long reasonItemId, Integer beforeQty, Integer afterQty, Long inboundId) {
        createFor(reason, WmsStockType.LOGIC, direction, stockLogicDO.getId(), stockLogicDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, null, beforeQty, afterQty, inboundId, stockFlowDO -> {
            // 采购计划量
            // stockFlowDO.setPurchasePlanQty(stockLogicDO.getPurchasePlanQty());
            // 采购在途量
            // stockFlowDO.setPurchaseTransitQty(stockLogicDO.getPurchaseTransitQty());
            // 退货在途量
            // stockFlowDO.setReturnTransitQty(stockLogicDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            // stockFlowDO.setSellableQty(stockLogicDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockLogicDO.getAvailableQty());
            // 待上架数量
            stockFlowDO.setShelvingPendingQty(stockLogicDO.getShelvePendingQty() + quantity);
            // 不良品数量
            // stockFlowDO.setDefectiveQty(stockLogicDO.getItemQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockLogicDO.getOutboundPendingQty());
        });
    }

//    /**
//     * 创建仓位库存变化流水(原方法)
//     */
//    @Override
//    public void createForStockBin(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockBinDO stockBinDO, Integer quantity, Long reasonId, Long reasonItemId, Long inboundItemFlowId) {
//        createFor(reason, WmsStockType.BIN, direction, stockBinDO.getId(), stockBinDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
//            // 采购计划量
//            // stockFlowDO.setPurchasePlanQty(stockLogicDO.getPurchasePlanQty());
//            // 采购在途量
//            // stockFlowDO.setPurchaseTransitQty(stockLogicDO.getPurchaseTransitQty());
//            // 退货在途量
//            // stockFlowDO.setReturnTransitQty(stockLogicDO.getReturnTransitQty());
//            // 可售量，未被单据占用的良品数量
//            stockFlowDO.setSellableQty(stockBinDO.getSellableQty());
//            // 可用量，在库的良品数量
//            stockFlowDO.setAvailableQty(stockBinDO.getAvailableQty());
//            // 待上架数量
//            // stockFlowDO.setShelvingPendingQty(stockBinDO.getShelvingPendingQty() + quantity);
//            // 不良品数量
//            // stockFlowDO.setDefectiveQty(stockLogicDO.getItemQty());
//            // 待出库量
//            stockFlowDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty());
//            // 库存批次的流水ID
//            stockFlowDO.setInboundItemFlowId(inboundItemFlowId);
//        });
//    }

    /**
     * 创建仓位库存变化流水(新方法)
     */
    @Override
    public void createForStockBin(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockBinDO stockBinDO, Integer quantity, Long reasonId, Long reasonItemId, Long binId, Integer beforeQty, Integer afterQty, Long inboundId) {
        createFor(reason, WmsStockType.BIN, direction, stockBinDO.getId(), stockBinDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, binId, beforeQty, afterQty, inboundId, stockFlowDO -> {
            // 采购计划量
            // stockFlowDO.setPurchasePlanQty(stockLogicDO.getPurchasePlanQty());
            // 采购在途量
            // stockFlowDO.setPurchaseTransitQty(stockLogicDO.getPurchaseTransitQty());
            // 退货在途量
            // stockFlowDO.setReturnTransitQty(stockLogicDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            stockFlowDO.setSellableQty(stockBinDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockBinDO.getAvailableQty());
            // 待上架数量
            // stockFlowDO.setShelvingPendingQty(stockBinDO.getShelvingPendingQty() + quantity);
            // 不良品数量
            // stockFlowDO.setDefectiveQty(stockLogicDO.getItemQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty());
        });
    }

//    /**
//     * 创建仓库库存变化流水(原方法)
//     */
//    public void createFor(WmsStockReason reason, WmsStockType stockType, WmsStockFlowDirection direction, Long stockId, Long warehouseId, Long productId, Integer quantity, Long reasonId, Long reasonItemId, Consumer<WmsStockFlowDO> consumer) {
//        // 校验本方法在事务中
//        JdbcUtils.requireTransaction();
//        // 获取上一个流水
//        WmsStockFlowDO lastStockFlowDO = this.getLastFlow(warehouseId, stockType.getValue(), stockId);
//        // 创建本次流水
//        WmsStockFlowDO stockFlowDO = new WmsStockFlowDO();
//        stockFlowDO.setWarehouseId(warehouseId);
//        stockFlowDO.setProductId(productId);
//        stockFlowDO.setStockType(stockType.getValue());
//        stockFlowDO.setStockId(stockId);
//        stockFlowDO.setReason(reason.getValue());
//        stockFlowDO.setReasonBillId(reasonId);
//        stockFlowDO.setDirection(direction.getValue());
//        stockFlowDO.setReasonItemId(reasonItemId);
//        if (lastStockFlowDO != null) {
//            stockFlowDO.setPrevFlowId(lastStockFlowDO.getId());
//        } else {
//            stockFlowDO.setPrevFlowId(0L);
//        }
//        // 变更量
//        stockFlowDO.setDeltaQty(quantity);
//        consumer.accept(stockFlowDO);
//        //
//        stockFlowDO.setFlowTime(new Timestamp(System.currentTimeMillis()));
//        // 保存
//        stockFlowMapper.insert(stockFlowDO);
//        // 关联前项流水
//        if (lastStockFlowDO != null) {
//            lastStockFlowDO.setNextFlowId(stockFlowDO.getId());
//            this.updateStockFlow(BeanUtils.toBean(lastStockFlowDO, WmsStockFlowSaveReqVO.class));
//        }
//    }

    /**
     * 创建仓库库存变化流水(新方法)
     */
    public void createFor(WmsStockReason reason, WmsStockType stockType, WmsStockFlowDirection direction, Long stockId, Long warehouseId, Long productId, Integer quantity, Long reasonId, Long reasonItemId, Long binId, Integer beforeQty, Integer afterQty, Long inboundId, Consumer<WmsStockFlowDO> consumer) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获取上一个流水
        WmsStockFlowDO lastStockFlowDO = this.getLastFlow(warehouseId, stockType.getValue(), stockId);
        // 创建本次流水
        WmsStockFlowDO stockFlowDO = new WmsStockFlowDO();
        stockFlowDO.setWarehouseId(warehouseId);
        stockFlowDO.setProductId(productId);
        stockFlowDO.setStockType(stockType.getValue());
        stockFlowDO.setStockId(stockId);
        stockFlowDO.setReason(reason.getValue());
        stockFlowDO.setReasonBillId(reasonId);
        stockFlowDO.setDirection(direction.getValue());
        stockFlowDO.setReasonItemId(reasonItemId);
        stockFlowDO.setBinId(binId);
        stockFlowDO.setBeforeQty(beforeQty);
        stockFlowDO.setAfterQty(afterQty);
        stockFlowDO.setInboundId(inboundId);
        if (lastStockFlowDO != null) {
            stockFlowDO.setPrevFlowId(lastStockFlowDO.getId());
        } else {
            stockFlowDO.setPrevFlowId(0L);
        }
        // 变更量
        stockFlowDO.setDeltaQty(quantity);
        consumer.accept(stockFlowDO);
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

    @Override
    public List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId) {
        return stockFlowMapper.selectStockFlow(stockType, stockId);
    }

    @Override
    public void assembleProducts(List<WmsStockFlowRespVO> list) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockFlowRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockFlowRespVO::getProductId, WmsStockFlowRespVO::setProduct);
    }

    @Override
    public void assembleWarehouse(List<WmsStockFlowRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockFlowRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsStockFlowRespVO::getWarehouseId, WmsStockFlowRespVO::setWarehouse);
    }

    @Override
    public void assembleBin(List<WmsStockFlowRespVO> list) {
        // 过滤库位的动账流水
        List<WmsStockFlowRespVO> binFlowList = StreamX.from(list).filter(v -> Objects.equals(WmsStockType.BIN.getValue(), v.getStockType())).toList();
        List<Long> stockBinIds = StreamX.from(binFlowList).toList(WmsStockFlowRespVO::getStockId).stream().distinct().toList();
        // 查询到对应的库位库存记录
        List<WmsStockBinDO> stockBinDOList = stockBinService.selectStockBinByIds(stockBinIds);
        Map<Long, WmsStockBinDO> stockBinDOMap = StreamX.from(stockBinDOList).toMap(WmsStockBinDO::getId);
        Set<Long> binIds = StreamX.from(stockBinDOList).toSet(WmsStockBinDO::getBinId);
        // 查询到库位记录
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(binIds);
        Map<Long, WmsWarehouseBinRespVO> binVOMap = StreamX.from(binDOList).toMap(WmsWarehouseBinDO::getId, binDO -> BeanUtils.toBean(binDO, WmsWarehouseBinRespVO.class));
        // 循环库位的动账流水
        for (WmsStockFlowRespVO binRespVO : binFlowList) {
            // 找到流水对应的仓位库存记录
            WmsStockBinDO stockBinDO = stockBinDOMap.get(binRespVO.getStockId());
            // 通过仓位库存记录找到对应的仓位
            WmsWarehouseBinRespVO binVO = binVOMap.get(stockBinDO.getBinId());
            // 设置仓位
            binRespVO.setBin(binVO);
        }
    }

    @Override
    public void assembleInbound(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> inboundFlowList = StreamX.from(list).filter(v -> Objects.equals(WmsStockReason.INBOUND.getValue(), v.getReason())).toList();
        //库存日志逻辑改造后，入库单号直接从inboundId字段取
        if (inboundFlowList.isEmpty()) {
            List<Long> inboundIdList = StreamX.from(list).map(WmsStockFlowRespVO::getInboundId).filter(Objects::nonNull).toList();
            if (CollectionUtils.isEmpty(inboundIdList)) {
                return;
            }
            List<WmsInboundDO> inboundDOList = inboundService.selectByIds(inboundIdList);
            Map<Long, WmsInboundSimpleRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
            StreamX.from(list).assemble(inboundMap, WmsStockFlowRespVO::getInboundId, WmsStockFlowRespVO::setInbound);
            return;
        }
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(inboundFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsInboundSimpleRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(inboundFlowList).assemble(inboundMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setInbound);
    }

    @Override
    public void assembleOutbound(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> outboundFlowList = StreamX.from(list).filter(v ->
            Objects.equals(WmsStockReason.OUTBOUND_FINISH.getValue(), v.getReason()) ||
                Objects.equals(WmsStockReason.OUTBOUND_SUBMIT.getValue(), v.getReason()) ||
                Objects.equals(WmsStockReason.OUTBOUND_REJECT.getValue(), v.getReason()) ||
                Objects.equals(WmsStockReason.OUTBOUND_AGREE.getValue(), v.getReason())
        ).toList();
        List<WmsOutboundDO> outboundDOList = outboundService.selectByIds(StreamX.from(outboundFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsOutboundSimpleRespVO> outboundMap = StreamX.from(outboundDOList).toMap(WmsOutboundDO::getId, outboundDO -> BeanUtils.toBean(outboundDO, WmsOutboundSimpleRespVO.class));
        StreamX.from(outboundFlowList).assemble(outboundMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setOutbound);
    }

    @Override
    public void assemblePickup(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> pickupFlowList = StreamX.from(list).filter(v -> Objects.equals(WmsStockReason.PICKUP.getValue(), v.getReason())).toList();
        List<WmsPickupDO> pickupDOList = pickupService.selectByIds(StreamX.from(pickupFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsPickupSimpleRespVO> pickupMap = StreamX.from(pickupDOList).toMap(WmsPickupDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsPickupSimpleRespVO.class));
        StreamX.from(pickupFlowList).assemble(pickupMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setPickup);
    }

    @Override
    public void assembleStockCheck(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> stockCheckFlowList = StreamX.from(list).filter(
            v -> Objects.equals(WmsStockReason.STOCKCHECK_NEGATIVE.getValue(), v.getReason()) || Objects.equals(WmsStockReason.STOCKCHECK_POSITIVE.getValue(), v.getReason())
        ).toList();
        List<WmsStockCheckDO> stockCheckDOList = stockCheckService.selectByIds(StreamX.from(stockCheckFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsStockCheckRespVO> stockCheckMap = StreamX.from(stockCheckDOList).toMap(WmsStockCheckDO::getId, elem -> BeanUtils.toBean(elem, WmsStockCheckRespVO.class));
        StreamX.from(stockCheckFlowList).assemble(stockCheckMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setStockCheck);
    }

    @Override
    public void assembleBinMove(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> flowList = StreamX.from(list).filter(
            v -> Objects.equals(WmsStockReason.STOCK_BIN_MOVE.getValue(), v.getReason())
        ).toList();
        List<WmsStockBinMoveDO> doList = stockBinMoveService.selectByIds(StreamX.from(flowList).toSet(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsStockBinMoveRespVO> voMap = StreamX.from(doList).toMap(WmsStockBinMoveDO::getId, elem -> BeanUtils.toBean(elem, WmsStockBinMoveRespVO.class));
        StreamX.from(flowList).assemble(voMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setStockBinMove);
    }

    @Override
    public void assembleLogicMove(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> flowList = StreamX.from(list).filter(
            v -> Objects.equals(WmsStockReason.STOCK_LOGIC_MOVE.getValue(), v.getReason())
        ).toList();
        List<WmsStockLogicMoveDO> doList = stockLogicMoveService.selectByIds(StreamX.from(flowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsStockLogicMoveRespVO> voMap = StreamX.from(doList).toMap(WmsStockLogicMoveDO::getId, elem -> BeanUtils.toBean(elem, WmsStockLogicMoveRespVO.class));
        StreamX.from(flowList).assemble(voMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setStockLogicMove);
    }

    @Override
    public void assembleBinStock(List<WmsStockFlowRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        List<WmsWarehouseBinRespVO> binList = StreamX.from(list).map(WmsStockFlowRespVO::getBin).filter(Objects::nonNull).toList();

        if (binList.isEmpty()) {
            return;
        }

        List<Long> binIds = binList.stream().map(WmsWarehouseBinRespVO::getId).filter(Objects::nonNull).distinct().toList();

        List<WmsStockBinDO> stockBinList = stockBinService.selectStockBinByIds(binIds);
        Map<Long, WmsStockBinDO> stockBinMap = StreamX.from(stockBinList).toMap(WmsStockBinDO::getId);

        if (stockBinMap.isEmpty()) {
            return;
        }

        StreamX.from(list).forEach(itemFlow -> {
            WmsWarehouseBinRespVO bin = itemFlow.getBin();
            if (bin == null || bin.getId() == null) {
                return;
            }

            WmsStockBinDO stockBin = stockBinMap.get(bin.getId());
            if (stockBin != null) {
                itemFlow.setStockBin(BeanUtils.toBean(stockBin, WmsStockBinRespVO.class));
            }
        });
    }


    @Override
    public void assembleExchange(List<WmsStockFlowRespVO> list) {
        List<WmsStockFlowRespVO> stockCheckFlowList = StreamX.from(list).filter(
            v -> Objects.equals(WmsStockReason.EXCHANGE.getValue(), v.getReason())
        ).toList();
        List<WmsExchangeDO> exchangeDOList = exchangeService.selectByIds(StreamX.from(stockCheckFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsExchangeRespVO> exchangeMap = StreamX.from(exchangeDOList).toMap(WmsExchangeDO::getId, elem -> BeanUtils.toBean(elem, WmsExchangeRespVO.class));
        StreamX.from(stockCheckFlowList).assemble(exchangeMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setExchange);
    }

    @Override
    public void assembleStockWarehouse(List<WmsStockFlowRespVO> list) {
        List<WmsWarehouseProductVO> wmsWarehouseProductVOList = new ArrayList<>();
        for (WmsStockFlowRespVO flowRespVO : list) {
            wmsWarehouseProductVOList.add(WmsWarehouseProductVO.builder().warehouseId(flowRespVO.getWarehouseId()).productId(flowRespVO.getProductId()).build());
        }
        List<WmsStockWarehouseDO> stockWarehouseDOList = stockWarehouseService.selectStockWarehouse(wmsWarehouseProductVOList);
        Map<String, WmsStockWarehouseSimpleVO> stockWarehouseDOMap = StreamX.from(stockWarehouseDOList).toMap(e -> e.getProductId() + "-" + e.getWarehouseId(), e -> BeanUtils.toBean(e, WmsStockWarehouseSimpleVO.class));
        StreamX.from(list).assemble(stockWarehouseDOMap, e -> e.getProductId() + "-" + e.getWarehouseId(), WmsStockFlowRespVO::setStockWarehouse);
    }

    @Override
    public void assembleCompanyAndDept(List<WmsStockFlowRespVO> list) {
        // 过滤逻辑库存的动账流水
        List<WmsStockFlowRespVO> logicFlowList = StreamX.from(list).filter(v -> Objects.equals(WmsStockType.LOGIC.getValue(), v.getStockType())).toList();
        List<Long> stockLogicIds = StreamX.from(logicFlowList).toList(WmsStockFlowRespVO::getStockId).stream().distinct().toList();
        // 查询到对应的逻辑库存记录
        List<WmsStockLogicDO> stockLogicDOList = stockLogicService.selectByIds(stockLogicIds);
        Map<Long, WmsStockLogicDO> logicDOMap = StreamX.from(stockLogicDOList).toMap(WmsStockLogicDO::getId);
        Set<Long> companyIds = StreamX.from(stockLogicDOList).toSet(WmsStockLogicDO::getCompanyId);
        List<Long> deptIds = StreamX.from(stockLogicDOList).toList(WmsStockLogicDO::getDeptId);
        // 查询到部门记录
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(deptIds);
        Map<Long, FmsCompanyDTO> companyDTOMap = companyApi.getCompanyMap(companyIds);
        Map<Long, DeptSimpleRespVO> deptSimpleVOMap = BeanUtils.toBean(deptDTOMap, DeptSimpleRespVO.class);
        // 循环库位的动账流水
        for (WmsStockFlowRespVO logicRespVO : logicFlowList) {
            // 找到流水对应的逻辑库存记录
            WmsStockLogicDO stockLogicDO = logicDOMap.get(logicRespVO.getStockId());
            // 通过逻辑库存记录找到对应的部门
            DeptSimpleRespVO deptVO = deptSimpleVOMap.get(stockLogicDO.getDeptId());
            // 设置部门
            logicRespVO.setDept(deptVO);
            // 通过逻辑库存记录找到对应的财务公司
            FmsCompanyDTO companyDTO = companyDTOMap.get(stockLogicDO.getCompanyId());
            // 设置公司
            logicRespVO.setCompany(BeanUtils.toBean(companyDTO, FmsCompanySimpleRespVO.class));
        }


    }

    /**
     * 按 ID 集合查询 WmsStockFlowDO
     */
    @Override
    public List<WmsStockFlowDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockFlowMapper.selectByIds(idList);
    }

    @Override
    public void assembleInboundItemFlow(List<WmsStockFlowRespVO> list) {
        List<WmsItemFlowDO> inboundItemFlowDOS = itemFlowService.selectByIds(StreamX.from(list).toSet(WmsStockFlowRespVO::getInboundItemFlowId));
        Map<Long, WmsInboundItemFlowSimpleVO> inboundItemFlowDOMap = StreamX.from(inboundItemFlowDOS).toMap(WmsItemFlowDO::getId, e -> BeanUtils.toBean(e, WmsInboundItemFlowSimpleVO.class));
        StreamX.from(list).assemble(inboundItemFlowDOMap, WmsStockFlowRespVO::getInboundItemFlowId, WmsStockFlowRespVO::setInboundItemFlow);
        InboundExecutor.setShelveAvailableQty(inboundItemFlowDOMap.values());

        List<Long> inboundIds = StreamX.from(inboundItemFlowDOMap.values()).toList(WmsInboundItemFlowSimpleVO::getInboundId);
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(inboundIds);
        Map<Long, WmsInboundDO> inboundDOMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId);
        for (WmsStockFlowRespVO flowRespVO : list) {
            WmsInboundItemFlowSimpleVO inboundItemFlow = flowRespVO.getInboundItemFlow();
            if(inboundItemFlow!=null) {
                WmsInboundDO inboundDO = inboundDOMap.get(inboundItemFlow.getInboundId());
                flowRespVO.setInbound(BeanUtils.toBean(inboundDO,WmsInboundSimpleRespVO.class));
            }
        }

    }


}

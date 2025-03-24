package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse.WmsStockWarehouseMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundStatus;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_WAREHOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE;

/**
 * 仓库库存 Service 实现类
 *
 * @author 李方捷
 */
@Slf4j
@Service
public class WmsStockWarehouseServiceImpl implements WmsStockWarehouseService {

    @Resource
    private WmsStockWarehouseMapper stockWarehouseMapper;

    @Resource
    private WmsStockFlowService stockFlowService;

    @Resource
    @Lazy
    private WmsStockOwnershipService stockOwnershipService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;


    @Resource
    private WmsStockBinService stockBinService;



    @Resource
    private WmsLockRedisDAO lockRedisDAO;

    /**
     * @sign : 8EB2B4EFB4F2439A
     */
    @Override
    public WmsStockWarehouseDO createStockWarehouse(WmsStockWarehouseSaveReqVO createReqVO) {
        if (stockWarehouseMapper.getByWarehouseIdAndProductId(createReqVO.getWarehouseId(), createReqVO.getProductId()) != null) {
            throw exception(STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsStockWarehouseDO stockWarehouse = BeanUtils.toBean(createReqVO, WmsStockWarehouseDO.class);
        stockWarehouseMapper.insert(stockWarehouse);
        // 返回
        return stockWarehouse;
    }

    /**
     * @sign : DC724E64364D70F5
     */
    @Override
    public WmsStockWarehouseDO updateStockWarehouse(WmsStockWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockWarehouseDO exists = validateStockWarehouseExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getWarehouseId(), exists.getWarehouseId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsStockWarehouseDO stockWarehouse = BeanUtils.toBean(updateReqVO, WmsStockWarehouseDO.class);
        stockWarehouseMapper.updateById(stockWarehouse);
        // 返回
        return stockWarehouse;
    }

    /**
     * @sign : BC4B0A75A01DA133
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockWarehouse(Long id) {
        // 校验存在
        WmsStockWarehouseDO stockWarehouse = validateStockWarehouseExists(id);
        // 唯一索引去重
        stockWarehouse.setWarehouseId(stockWarehouseMapper.flagUKeyAsLogicDelete(stockWarehouse.getWarehouseId()));
        stockWarehouse.setProductId(stockWarehouseMapper.flagUKeyAsLogicDelete(stockWarehouse.getProductId()));
        stockWarehouseMapper.updateById(stockWarehouse);
        // 删除
        stockWarehouseMapper.deleteById(id);
    }

    /**
     * @sign : 0AC227DD0DAC3D98
     */
    private WmsStockWarehouseDO validateStockWarehouseExists(Long id) {
        WmsStockWarehouseDO stockWarehouse = stockWarehouseMapper.selectById(id);
        if (stockWarehouse == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        }
        return stockWarehouse;
    }

    @Override
    public WmsStockWarehouseDO getStockWarehouse(Long id) {
        return stockWarehouseMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockWarehouseDO> getStockWarehousePage(WmsStockWarehousePageReqVO pageReqVO) {
        return stockWarehouseMapper.selectPage(pageReqVO);
    }

    @Override
    public WmsStockWarehouseDO getStockWarehouse(Long warehouseId, Long productId) {
        return stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(WmsInboundRespVO inboundRespVO) {

        Long warehouseId = inboundRespVO.getWarehouseId();
        Long companyId = inboundRespVO.getCompanyId();
        Long inboundDeptId = inboundRespVO.getDeptId();

        List<WmsInboundItemRespVO> itemList=inboundRespVO.getItemList();
        for (WmsInboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            Long deptId = inboundDeptId;
            // 如果入库单上未指定部门,默认按产品的部门ID
            if (deptId == null) {
                ErpProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 执行入库的原子操作
            InboundStatus inboundStatus=inboundSingleItemAtomically(companyId, deptId, warehouseId, productId, item.getActualQuantity(), inboundRespVO.getId(), item.getId());
            item.setInboundStatus(inboundStatus.getValue());
            if(inboundStatus!=InboundStatus.NONE) {
                item.setLeftQuantity(item.getActualQuantity());
            }
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
        WmsStockWarehouseServiceImpl currentProxy = SpringUtils.getBeanByExactType(WmsStockWarehouseServiceImpl.class);
        AtomicReference<InboundStatus> status = new AtomicReference<>();
        lockRedisDAO.lockStockLevels(warehouseId, productId, () -> {
            try {
                status.set(currentProxy.inboundSingleItemTransactional(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId));
            } catch (Exception e) {
                log.error("inboundSingleItemTransactional Error", e);
            }
        });
        return status.get();
    }

    /**
     * 在事务中执行入库操作
     */

    protected InboundStatus inboundSingleItemTransactional(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
        // 如果没有就创建
        if (stockWarehouseDO == null) {
            stockWarehouseDO = new WmsStockWarehouseDO();
            stockWarehouseDO.setWarehouseId(warehouseId);
            stockWarehouseDO.setProductId(productId);
            // 采购计划量
            stockWarehouseDO.setPurchasePlanQuantity(0);
            // 采购在途量
            stockWarehouseDO.setPurchaseTransitQuantity(0);
            // 退货在途量
            stockWarehouseDO.setReturnTransitQuantity(0);
            // 待上架量
            stockWarehouseDO.setShelvingPendingQuantity(quantity);
            // 可用量，在库的良品数量
            stockWarehouseDO.setAvailableQuantity(0);
            // 可售量
            stockWarehouseDO.setSellableQuantity(0);
            // 待出库量
            stockWarehouseDO.setOutboundPendingQuantity(0);
            // 不良品数量
            stockWarehouseDO.setDefectiveQuantity(0);
            stockWarehouseMapper.insert(stockWarehouseDO);
        } else {
            // 如果有就修改
            // 采购计划量
            // stockWarehouseDO.setPurchasePlanQuantity(0);
            // 采购在途量
            // stockWarehouseDO.setPurchaseTransitQuantity(0);
            // 退货在途量
            // stockWarehouseDO.setReturnTransitQuantity(0);
            // 待上架量
            stockWarehouseDO.setShelvingPendingQuantity(stockWarehouseDO.getShelvingPendingQuantity() + quantity);
            // 可用量，在库的良品数量
            // stockWarehouseDO.setAvailableQuantity(0);
            // 可售量
            // stockWarehouseDO.setSellableQuantity(0);
            // 待出库量
            // stockWarehouseDO.setOutboundPendingQuantity(0);
            // 不良品数量
            // stockWarehouseDO.setDefectiveQuantity(0);
            stockWarehouseMapper.updateById(stockWarehouseDO);
        }
        // 记录流水
        stockFlowService.createForStockWarehouse(StockReason.INBOUND, productId,stockWarehouseDO, quantity, inboundId, inboundItemId);
        // 调整归属库存
        stockOwnershipService.inboundSingleItem(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId);
        // TODO 如果是采购入库
        log.info("入库:productId=" + productId + ";quantity=" + quantity);

        // 当前逻辑,默认全部入库
        return InboundStatus.ALL;
    }

    /**
     * 汇总拣货库存,仅处理可用与可售
     **/
    @Override
    public void refreshForPickup(Long warehouseId, Long productId,Long pickupId,Long pickupItemId) {

        List<WmsStockBinDO> stockBinList =  stockBinService.selectStockBin(warehouseId, productId);

        Integer availableQuantity = 0;
        Integer sellableQuantity = 0;

        for (WmsStockBinDO wmsStockBinDO : stockBinList) {
            availableQuantity+=wmsStockBinDO.getAvailableQuantity();
            sellableQuantity+=wmsStockBinDO.getSellableQuantity();
        }

        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);

        stockWarehouseDO.setAvailableQuantity(availableQuantity);
        stockWarehouseDO.setShelvingPendingQuantity(sellableQuantity);

        // 更新库存
        stockWarehouseMapper.updateById(stockWarehouseDO);
        // 记录流水
        stockFlowService.createForStockWarehouse(StockReason.PICKUP, productId,stockWarehouseDO, sellableQuantity,pickupId, pickupItemId);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(WmsOutboundRespVO outboundRespVO) {

        Long warehouseId = outboundRespVO.getWarehouseId();
        Long companyId = outboundRespVO.getCompanyId();
        Long inboundDeptId = outboundRespVO.getDeptId();

        List<WmsOutboundItemRespVO> itemList=outboundRespVO.getItemList();
        for (WmsOutboundItemRespVO item : itemList) {
            Long productId = item.getProductId();
            Long deptId = inboundDeptId;
            // 如果入库单上未指定部门,默认按产品的部门ID
            if (deptId == null) {
                ErpProductRespSimpleVO productVO = item.getProduct();
                deptId = productVO.getDeptId();
            }
            // 执行入库的原子操作
            OutboundStatus outboundStatus=outboundSingleItemAtomically(companyId, deptId, warehouseId, productId, item.getActualQuantity(), outboundRespVO.getId(), item.getId());
            item.setOutboundStatus(outboundStatus.getValue());
            if(outboundStatus!=OutboundStatus.NONE) {
                //item.setLeftQuantity(item.getActualQuantity());
            }
        }
        // 完成最终的入库
        outboundService.finishOutbound(outboundRespVO);

    }

    /**
     * 执行出库的原子操作,以加锁的方式单个出入库
     */
    private OutboundStatus outboundSingleItemAtomically(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long outboundId, Long outboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        WmsStockWarehouseServiceImpl currentProxy = SpringUtils.getBeanByExactType(WmsStockWarehouseServiceImpl.class);
        AtomicReference<OutboundStatus> status = new AtomicReference<>();
        lockRedisDAO.lockStockLevels(warehouseId, productId, () -> {
            try {
                status.set(currentProxy.outboundSingleItemTransactional(companyId, deptId, warehouseId, productId, quantity, outboundId, outboundItemId));
            } catch (Exception e) {
                log.error("inboundSingleItemTransactional Error", e);
            }
        });
        return status.get();
    }


    /**
     * 在事务中执行出库操作
     */

    protected OutboundStatus outboundSingleItemTransactional(Long companyId, Long deptId, Long warehouseId, Long productId, Integer quantity, Long inboundId, Long inboundItemId) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获得仓库库存记录
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
        // 如果没有就创建
        if (stockWarehouseDO == null) {
             throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        } else {
            // 如果有就修改
            // 采购计划量
            // stockWarehouseDO.setPurchasePlanQuantity(0);
            // 采购在途量
            // stockWarehouseDO.setPurchaseTransitQuantity(0);
            // 退货在途量
            // stockWarehouseDO.setReturnTransitQuantity(0);
            // 待上架量
            // stockWarehouseDO.setShelvingPendingQuantity(stockWarehouseDO.getShelvingPendingQuantity() + quantity);
            // 可用量，在库的良品数量
            stockWarehouseDO.setAvailableQuantity(stockWarehouseDO.getAvailableQuantity()-quantity);
            // 可售量
            stockWarehouseDO.setSellableQuantity(stockWarehouseDO.getSellableQuantity()-quantity);
            // 待出库量
            stockWarehouseDO.setOutboundPendingQuantity(stockWarehouseDO.getOutboundPendingQuantity()+quantity);
            // 不良品数量
            // stockWarehouseDO.setDefectiveQuantity(0);
            stockWarehouseMapper.updateById(stockWarehouseDO);
        }
        // 记录流水
        stockFlowService.createForStockWarehouse(StockReason.OUTBOUND, productId,stockWarehouseDO, quantity, inboundId, inboundItemId);
        // 调整归属库存
        stockOwnershipService.outboundSingleItem(companyId, deptId, warehouseId, productId, quantity, inboundId, inboundItemId);

        log.info("出库:productId=" + productId + ";quantity=" + quantity);

        // 当前逻辑,默认全部入库
        return OutboundStatus.ALL;
    }
}

package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse.WmsStockWarehouseMapper;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
    public WmsStockWarehouseDO getByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        return stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Override
    public void insertOrUpdate(WmsStockWarehouseDO stockWarehouseDO) {
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        }
        // 采购计划量
        if (stockWarehouseDO.getPurchasePlanQty() == null) {
            stockWarehouseDO.setPurchasePlanQty(0);
        }
        // 采购在途量
        if (stockWarehouseDO.getPurchaseTransitQty() == null) {
            stockWarehouseDO.setPurchaseTransitQty(0);
        }
        // 退货在途量
        if (stockWarehouseDO.getReturnTransitQty() == null) {
            stockWarehouseDO.setReturnTransitQty(0);
        }
        // 待上架量
        if (stockWarehouseDO.getShelvingPendingQty() == null) {
            stockWarehouseDO.setShelvingPendingQty(0);
        }
        // 可用量，在库的良品数量
        if (stockWarehouseDO.getAvailableQty() == null) {
            stockWarehouseDO.setAvailableQty(0);
        }
        // 可售量
        if (stockWarehouseDO.getSellableQty() == null) {
            stockWarehouseDO.setSellableQty(0);
        }
        // 待出库量
        if (stockWarehouseDO.getOutboundPendingQty() == null) {
            stockWarehouseDO.setOutboundPendingQty(0);
        }
        // 不良品数量
        if (stockWarehouseDO.getDefectiveQty() == null) {
            stockWarehouseDO.setDefectiveQty(0);
        }
        if (stockWarehouseDO.getId() == null) {
            stockWarehouseMapper.insert(stockWarehouseDO);
        } else {
            stockWarehouseMapper.updateById(stockWarehouseDO);
        }
    }
}

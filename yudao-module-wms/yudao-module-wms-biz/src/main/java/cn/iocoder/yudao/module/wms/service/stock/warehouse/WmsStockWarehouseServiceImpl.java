package cn.iocoder.yudao.module.wms.service.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseQueryDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseSimpleDTO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse.WmsStockWarehouseMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse.WmsStockWarehouseProductMapper;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.logic.WmsStockLogicService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCK_WAREHOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE;

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
    private WmsStockWarehouseProductMapper stockWarehouseProductMapper;

    @Resource
    @Lazy
    private WmsStockLogicService stockLogicService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;

    @Resource
    private WmsStockBinService stockBinService;

    @Resource
    private WmsWarehouseService warehouseService;

    @Resource
    private ErpProductApi productApi;

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
    @Transactional(rollbackFor = Exception.class)
    public void updateStockWarehouse(WmsStockWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockWarehouseDO exists = validateStockWarehouseExists(updateReqVO.getProductId(), updateReqVO.getWarehouseId());
//        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getWarehouseId(), exists.getWarehouseId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
//            throw exception(STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE);
//        }
        // 更新
        int makePendingQty = Math.max(exists.getMakePendingQty() + updateReqVO.getMakePendingQty(), 0);
        exists.setMakePendingQty(makePendingQty);
        stockWarehouseMapper.updateById(exists);
    }

    /**
     * 用于采购订单 - 在途数量更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quickUpdateStockWarehouse(WmsStockWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockWarehouseDO stockWarehouse = stockWarehouseMapper.getByWarehouseIdAndProductId(updateReqVO.getProductId(), updateReqVO.getWarehouseId());
        if (stockWarehouse == null) {
            log.info(String.valueOf(STOCK_WAREHOUSE_NOT_EXISTS), updateReqVO.getWarehouseId(), updateReqVO.getProductId());
            return;
        }
        // 更新
        int makePendingQty = Math.max(stockWarehouse.getMakePendingQty() + updateReqVO.getMakePendingQty(), 0);
        stockWarehouse.setMakePendingQty(makePendingQty);
        stockWarehouseMapper.updateById(stockWarehouse);
    }

    /**
     * @sign : BC4B0A75A01DA133
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockWarehouse(Long id) {
//        // 校验存在
//        WmsStockWarehouseDO stockWarehouse = validateStockWarehouseExists(id);
//        // 唯一索引去重
//        stockWarehouse.setWarehouseId(stockWarehouseMapper.flagUKeyAsLogicDelete(stockWarehouse.getWarehouseId()));
//        stockWarehouse.setProductId(stockWarehouseMapper.flagUKeyAsLogicDelete(stockWarehouse.getProductId()));
//        stockWarehouseMapper.updateById(stockWarehouse);
//        // 删除
//        stockWarehouseMapper.deleteById(id);
    }

    /**
     * @sign : 0AC227DD0DAC3D98
     */
    private WmsStockWarehouseDO validateStockWarehouseExists(Long productId, Long warehouseId) {
        WmsStockWarehouseDO stockWarehouse = stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
        if (stockWarehouse == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS, warehouseId, productId);
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
    public WmsStockWarehouseDO getStockWarehouse(Long warehouseId, Long productId, boolean createNew) {
        WmsStockWarehouseDO stockWarehouseDO = stockWarehouseMapper.getByWarehouseIdAndProductId(warehouseId, productId);
        if (stockWarehouseDO == null && createNew) {
            stockWarehouseDO = new WmsStockWarehouseDO();
            stockWarehouseDO.setWarehouseId(warehouseId);
            stockWarehouseDO.setProductId(productId);
            // 待上架量
            stockWarehouseDO.setShelvingPendingQty(0);
            // 采购计划量
            stockWarehouseDO.setMakePendingQty(0);
            // 采购在途量
            stockWarehouseDO.setTransitQty(0);
            // 退货在途量
            stockWarehouseDO.setReturnTransitQty(0);
            // 可售量，未被单据占用的良品数量
            stockWarehouseDO.setSellableQty(0);
            // 可用量，在库的良品数量
            stockWarehouseDO.setAvailableQty(0);
            // 待出库量
            stockWarehouseDO.setOutboundPendingQty(0);
            // 不良品数量
            stockWarehouseDO.setDefectiveQty(0);
            // 待出库量
            stockWarehouseDO.setOutboundPendingQty(0);
            // 
            stockWarehouseMapper.insert(stockWarehouseDO);
        }
        return stockWarehouseDO;
    }

    @Override
    public void insertOrUpdate(WmsStockWarehouseDO stockWarehouseDO) {
        if (stockWarehouseDO == null) {
            throw exception(STOCK_WAREHOUSE_NOT_EXISTS);
        }
        // 在制量
        if (stockWarehouseDO.getMakePendingQty() == null) {
            stockWarehouseDO.setMakePendingQty(0);
        }
        // 采购在途量
        if (stockWarehouseDO.getTransitQty() == null) {
            stockWarehouseDO.setTransitQty(0);
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

    @Override
    public void assembleProducts(List<WmsStockWarehouseRespVO> list) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockWarehouseRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockWarehouseRespVO::getProductId, WmsStockWarehouseRespVO::setProduct);
    }

    @Override
    public void assembleWarehouse(List<WmsStockWarehouseRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockWarehouseRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsStockWarehouseRespVO::getWarehouseId, WmsStockWarehouseRespVO::setWarehouse);
    }

    @Override
    public String getWarehouseProductKey(Long warehouseId, Long productId) {
        return warehouseId + "_" + productId;
    }

    @Override
    public void assembleStockBin(List<WmsStockWarehouseRespVO> list) {
        List<WmsWarehouseProductVO> warehouseProductList = StreamX.from(list).toList(v -> BeanUtils.toBean(v, WmsWarehouseProductVO.class));
        Map<String, List<WmsStockBinRespVO>> StockBinVOMap = stockBinService.selectStockBinGroup(warehouseProductList, true);
        StreamX.from(list).assemble(StockBinVOMap, e -> {
            return getWarehouseProductKey(e.getWarehouseId(), e.getProductId());
        }, WmsStockWarehouseRespVO::setStockBinList);
    }

    @Override
    public List<WmsStockWarehouseDO> selectByWarehouse(Long warehouseId) {
        return stockWarehouseMapper.selectByWarehouse(warehouseId);
    }

    @Override
    public List<WmsStockWarehouseDO> getByProductIds(Long warehouseId, List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        return stockWarehouseMapper.getByProductIds(warehouseId, productIds);
    }

    @Override
    public List<WmsStockWarehouseDO> selectStockWarehouse(List<WmsWarehouseProductVO> wmsWarehouseProductVOList) {
        if (CollectionUtils.isEmpty(wmsWarehouseProductVOList)) {
            return List.of();
        }
        return stockWarehouseMapper.selectStockWarehouse(wmsWarehouseProductVOList);
    }

    @Override
    public PageResult<WmsStockWarehouseProductRespVO> getStockGroupedWarehousePage(WmsStockWarehousePageReqVO pageReqVO) {
        PageResult<WmsProductDO> pageResult = stockWarehouseProductMapper.getStockGroupedWarehousePage(pageReqVO);
        if(pageResult.getList().isEmpty()) {
            return new PageResult<>();
        }
        PageResult<WmsStockWarehouseProductRespVO> voPageResult = BeanUtils.toBean(pageResult, WmsStockWarehouseProductRespVO.class);
        List<WmsStockWarehouseDO> list = stockWarehouseMapper.selectByProductIds(StreamX.from(pageResult.getList()).toSet(WmsProductDO::getId));
        //筛选仓库
        if(pageReqVO.getWarehouseId() != null) {
            list = list.stream().filter(v -> v.getWarehouseId().equals(pageReqVO.getWarehouseId())).toList();
        }
        List<WmsStockWarehouseRespVO> voList = BeanUtils.toBean(list, WmsStockWarehouseRespVO.class);
        this.assembleProducts(voList);
        this.assembleWarehouse(voList);
        this.assembleStockBin(voList);
        Map<Long, List<WmsStockWarehouseRespVO>> map = StreamX.from(voList).groupBy(WmsStockWarehouseRespVO::getProductId);
        StreamX.from(voPageResult.getList()).assemble(map, WmsStockWarehouseProductRespVO::getId, WmsStockWarehouseProductRespVO::setStockWarehouseList);
        StreamX.from(voPageResult.getList()).assemble(pageResult.getList(), WmsProductDO::getId, WmsStockWarehouseProductRespVO::getId, (e, p) -> {
            e.setProduct(BeanUtils.toBean(p, WmsProductRespSimpleVO.class));
        });
        return voPageResult;
    }

    /**
     * 按 ID 集合查询 WmsStockWarehouseDO
     */
    @Override
    public List<WmsStockWarehouseDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockWarehouseMapper.selectByIds(idList);
    }

    @Override
    public Map<Long, List<WmsStockWarehouseDO>>  selectSellableQty(WmsWarehouseQueryDTO wmsWarehouseQueryDTO) {
        Map<Long, List<WmsStockWarehouseDO>> map = Maps.newHashMap();
        List<WmsWarehouseSimpleDTO> warehouses = wmsWarehouseQueryDTO.getWarehouses();
        if (CollectionUtils.isEmpty(warehouses)) {
            return Map.of();
        }
        warehouses.forEach(wmsWarehouse -> {
            LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
            wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, wmsWarehouse.getWarehouseId())
                    .inIfPresent(WmsStockWarehouseDO::getProductId, wmsWarehouse.getProductIds())
                    .ge(WmsStockWarehouseDO::getSellableQty, 0);
            List<WmsStockWarehouseDO> rtnList = stockWarehouseMapper.selectList(wrapper);
            map.put(wmsWarehouse.getWarehouseId(), rtnList);
        });

        return map;
    }

    @Override
    public List<WmsStockWarehouseDO> selectSellableQtyList(WmsWarehouseQueryDTO wmsWarehouseQueryDTO) {
        if (CollectionUtils.isEmpty(wmsWarehouseQueryDTO.getWarehouses())) {
            return List.of();
        }
        List<WmsStockWarehouseDO> rtnList = new ArrayList<>();
        for (WmsWarehouseSimpleDTO warehouse : wmsWarehouseQueryDTO.getWarehouses()) {
            LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
            wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, warehouse.getWarehouseId())
                .inIfPresent(WmsStockWarehouseDO::getProductId, warehouse.getProductIds())
                .ge(WmsStockWarehouseDO::getSellableQty, 0);
            List<WmsStockWarehouseDO> results = stockWarehouseMapper.selectList(wrapper);
            if (!results.isEmpty()) {
                rtnList.add(results.get(0));
            }
        }
        return rtnList;
    }
}

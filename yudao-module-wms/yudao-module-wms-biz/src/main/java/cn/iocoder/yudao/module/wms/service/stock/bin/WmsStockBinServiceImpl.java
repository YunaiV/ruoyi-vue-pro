package cn.iocoder.yudao.module.wms.service.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespBinVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.WmsStockBinMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.WmsStockBinProductMapper;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_BIN_ID_PRODUCT_ID_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;

/**
 * 仓位库存 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinServiceImpl implements WmsStockBinService {

    @Resource
    private WmsStockBinMapper stockBinMapper;

    @Resource
    private WmsStockBinProductMapper stockBinProductMapper;

    @Resource
    WmsStockFlowService stockFlowService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    @Lazy
    WmsStockWarehouseService stockWarehouseService;

    @Autowired
    private WmsStockOwnershipService wmsStockOwnershipService;

    @Autowired
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    @Lazy
    WmsInboundItemService inboundItemService;

    @Resource
    private ErpProductApi productApi;

    @Resource
    private DeptApi deptApi;

    /**
     * @sign : 1D6010DA80E2C817
     */
    @Override
    public WmsStockBinDO createStockBin(WmsStockBinSaveReqVO createReqVO) {
        if (stockBinMapper.getByBinIdAndProductId(createReqVO.getBinId(), createReqVO.getProductId()) != null) {
            throw exception(STOCK_BIN_BIN_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsStockBinDO stockBin = BeanUtils.toBean(createReqVO, WmsStockBinDO.class);
        stockBinMapper.insert(stockBin);
        // 返回
        return stockBin;
    }

    /**
     * @sign : F969DF8A6A239ED2
     */
    @Override
    public WmsStockBinDO updateStockBin(WmsStockBinSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockBinDO exists = validateStockBinExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getBinId(), exists.getBinId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCK_BIN_BIN_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsStockBinDO stockBin = BeanUtils.toBean(updateReqVO, WmsStockBinDO.class);
        stockBinMapper.updateById(stockBin);
        // 返回
        return stockBin;
    }

    /**
     * @sign : 9F91D4D4AB3EF77A
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockBin(Long id) {
        // 校验存在
        WmsStockBinDO stockBin = validateStockBinExists(id);
        // 唯一索引去重
        stockBin.setBinId(stockBinMapper.flagUKeyAsLogicDelete(stockBin.getBinId()));
        stockBin.setProductId(stockBinMapper.flagUKeyAsLogicDelete(stockBin.getProductId()));
        stockBinMapper.updateById(stockBin);
        // 删除
        stockBinMapper.deleteById(id);
    }

    /**
     * @sign : 001873E63AE3E620
     */
    private WmsStockBinDO validateStockBinExists(Long id) {
        WmsStockBinDO stockBin = stockBinMapper.selectById(id);
        if (stockBin == null) {
            throw exception(STOCK_BIN_NOT_EXISTS);
        }
        return stockBin;
    }

    @Override
    public WmsStockBinDO getStockBin(Long id) {
        return stockBinMapper.selectById(id);
    }

    @Override
    public WmsStockBinDO getStockBin(Long binId, Long productId, boolean createNew) {
        WmsStockBinDO binDO=stockBinMapper.getByBinIdAndProductId(binId, productId);
        if(binDO==null && createNew) {
            Long warehouseId = warehouseBinService.getWarehouseBin(binId).getWarehouseId();
            binDO = new WmsStockBinDO();
            binDO.setWarehouseId(warehouseId);
            binDO.setBinId(binId);
            binDO.setProductId(productId);
            // 可用库存
            binDO.setAvailableQty(0);
            // 可售库存
            binDO.setSellableQty(0);
            // 待出库量
            binDO.setOutboundPendingQty(0);
            stockBinMapper.insert(binDO);
        }
        return binDO;
    }

    @Override
    public PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO) {
        return stockBinMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsStockBinDO> selectStockBin(Long warehouseId, Long binId,Long productId) {
        return stockBinMapper.selectStockBin(warehouseId, binId, productId);
    }

    @Override
    public List<WmsStockBinDO> selectStockBin(Long warehouseId, Long productId) {
        return WmsStockBinService.super.selectStockBin(warehouseId, productId);
    }

    @Override
    public Map<Long, Map<Long, WmsStockBinDO>> getStockBinMap(Collection<Long> binIds, Collection<Long> productIds) {
        Map<Long, Map<Long, WmsStockBinDO>> result = new HashMap<>();
        List<WmsStockBinDO> list = stockBinMapper.selectStockBinListInBin(binIds, productIds);
        for (WmsStockBinDO stockBinDO : list) {
            Map<Long, WmsStockBinDO> map = result.computeIfAbsent(stockBinDO.getBinId(), k -> new HashMap<>());
            map.put(stockBinDO.getProductId(), stockBinDO);
        }
        return result;
    }

    @Override
    public void insertOrUpdate(WmsStockBinDO stockBinDO) {
        if (stockBinDO == null) {
            throw exception(STOCK_BIN_NOT_EXISTS);
        }
        // 可用库存
        if (stockBinDO.getAvailableQty() == null) {
            stockBinDO.setAvailableQty(0);
        }
        // 可售库存
        if (stockBinDO.getSellableQty() == null) {
            stockBinDO.setSellableQty(0);
        }
        // 待上出库量
        if (stockBinDO.getOutboundPendingQty() == null) {
            stockBinDO.setOutboundPendingQty(0);
        }
        if (stockBinDO.getId() == null) {
            stockBinMapper.insert(stockBinDO);
        } else {
            stockBinMapper.updateById(stockBinDO);
        }
    }

    public List<WmsStockBinRespVO> selectStockBinList(List<WmsWarehouseProductVO> warehouseProductList, Boolean withBin) {
        if(CollectionUtils.isEmpty(warehouseProductList)) {
            return List.of();
        }
        List<WmsStockBinDO> doList =  stockBinMapper.selectStockBinListInWarehouse(warehouseProductList);
        List<WmsStockBinRespVO> voList = BeanUtils.toBean(doList, WmsStockBinRespVO.class);
        if(withBin) {
            assembleBin(voList);
        }
        return voList;
    }

    @Override
    public Map<String, List<WmsStockBinRespVO>> selectStockBinGroup(List<WmsWarehouseProductVO> warehouseProductList, Boolean withBin) {
        if(CollectionUtils.isEmpty(warehouseProductList)) {
            return Map.of();
        }
        List<WmsStockBinRespVO> voList = selectStockBinList(warehouseProductList,withBin);
        return StreamX.from(voList).groupBy(vo->{
            return stockWarehouseService.getWarehouseProductKey(vo.getWarehouseId(),vo.getProductId());
        });
    }

    @Override
    public void assembleProducts(List<WmsStockBinRespVO> list) {

        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockBinRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockBinRespVO::getProductId, WmsStockBinRespVO::setProduct);

    }

    @Override
    public void assembleWarehouse(List<WmsStockBinRespVO> list) {

        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockBinRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v-> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockBinRespVO::getWarehouseId, WmsStockBinRespVO::setWarehouse);

    }

    @Override
    public void assembleBin(List<WmsStockBinRespVO> list) {
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(list).toList(WmsStockBinRespVO::getBinId).stream().distinct().toList());
        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);
        StreamX.from(list).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsStockBinRespVO::getBinId,WmsStockBinRespVO::setBin);
    }

    @Override
    public List<WmsStockBinDO> selectStockBinByIds(List<Long> stockBinIds) {
        if(CollectionUtils.isEmpty(stockBinIds)) {
            return List.of();
        }
        return stockBinMapper.selectBatchIds(stockBinIds);

    }

    /**
     * 按库存批次详情ID 查询仓位库存清单
     **/
    public List<WmsStockBinDO> selectBinsByInboundItemId(Long warehouseId, Long productId, Long inboundItemId) {
        return stockBinMapper.selectBinsByInboundItemId(warehouseId, productId, inboundItemId);
    }

    @Override
    public PageResult<WmsProductRespBinVO> getGroupedStockBinPage(WmsStockBinPageReqVO pageReqVO) {
        PageResult<WmsProductDO> doPageResult = stockBinProductMapper.getGroupedStockBinPage(pageReqVO);
        PageResult<WmsProductRespBinVO> voPageResult = BeanUtils.toBean(doPageResult, WmsProductRespBinVO.class);
        return voPageResult;
    }


    @Override
    public void assembleDept(List<WmsProductRespBinVO> list) {
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(StreamX.from(list).map(WmsProductRespBinVO::getDeptId).toList());
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(list).assemble(deptVOMap, WmsProductRespBinVO::getDeptId, WmsProductRespBinVO::setDept);
    }


    @Override
    public void assembleStockWarehouseList(Long warehouseId, List<WmsProductRespBinVO> list) {

        // 查询仓库库存
        List<WmsStockWarehouseDO> stockWarehouseDOList = stockWarehouseService.getByProductIds(warehouseId,StreamX.from(list).toList(WmsProductRespBinVO::getId));
        List<WmsStockWarehouseRespVO> stockWarehouseVOList = BeanUtils.toBean(stockWarehouseDOList, WmsStockWarehouseRespVO.class);

        // 查询仓位库存
        List<WmsWarehouseProductVO> warehouseProductList = new ArrayList<>();
        for (WmsStockWarehouseRespVO simpleRespVO : stockWarehouseVOList) {
            warehouseProductList.add(WmsWarehouseProductVO.builder().warehouseId(simpleRespVO.getWarehouseId()).productId(simpleRespVO.getProductId()).build());
        }
        List<WmsStockBinRespVO> stockBinList = this.selectStockBinList(warehouseProductList, false);
        Map<String,List<WmsStockBinRespVO>> stockBinMap = StreamX.from(stockBinList).groupBy(e -> e.getWarehouseId()+"-"+e.getProductId());
        StreamX.from(stockWarehouseVOList).assemble(stockBinMap, e->  e.getWarehouseId()+"-"+e.getProductId() , WmsStockWarehouseRespVO::setStockBinList);

        // 装配仓库
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(stockWarehouseVOList).toSet(WmsStockWarehouseRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v-> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(stockWarehouseVOList).assemble(warehouseVOMap, WmsStockWarehouseRespVO::getWarehouseId, WmsStockWarehouseRespVO::setWarehouse);





        // 仓库按产品分组
        Map<Long, List<WmsStockWarehouseRespVO>> stockWarehouseMap = StreamX.from(stockWarehouseVOList).groupBy(v->v.getProductId());



        // 装配仓库库存清单
        StreamX.from(list).assemble(stockWarehouseMap, WmsProductRespBinVO::getId, WmsProductRespBinVO::setStockWarehouseList);

    }


}

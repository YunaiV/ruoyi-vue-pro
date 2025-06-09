package cn.iocoder.yudao.module.wms.service.stock.logic;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.WmsStockLogicMapper;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * 逻辑库存 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockLogicServiceImpl implements WmsStockLogicService {

    @Resource
    private WmsStockLogicMapper stockLogicMapper;

    @Resource
    private WmsStockFlowService stockFlowService;

    @Resource
    private ErpProductApi productApi;

    @Resource
    private DeptApi deptApi;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private FmsCompanyApi companyApi;
    @Autowired
    DictDataApi dictDataApi;
    @Autowired
    private WmsWarehouseService wmsWarehouseService;

    /**
     * @sign : 4AF969274F47ADC0
     */
    @Override
    public WmsStockLogicDO createStockLogic(WmsStockLogicSaveReqVO createReqVO) {
        if (stockLogicMapper.getByUkProductOwner(createReqVO.getWarehouseId(), createReqVO.getCompanyId(), createReqVO.getDeptId(), createReqVO.getProductId()) != null) {
            throw exception(STOCK_LOGIC_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsStockLogicDO stockLogic = BeanUtils.toBean(createReqVO, WmsStockLogicDO.class);
        stockLogicMapper.insert(stockLogic);
        // 返回
        return stockLogic;
    }

    /**
     * @sign : 355EF86754CB268D
     */
    @Override
    public WmsStockLogicDO updateStockLogic(WmsStockLogicSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockLogicDO exists = validateStockLogicExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getWarehouseId(), exists.getWarehouseId()) && Objects.equals(updateReqVO.getCompanyId(), exists.getCompanyId()) && Objects.equals(updateReqVO.getDeptId(), exists.getDeptId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCK_LOGIC_WAREHOUSE_ID_COMPANY_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsStockLogicDO stockLogic = BeanUtils.toBean(updateReqVO, WmsStockLogicDO.class);
        stockLogicMapper.updateById(stockLogic);
        // 返回
        return stockLogic;
    }

    /**
     * @sign : AE46873A88A81B7D
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockLogic(Long id) {
        // 校验存在
        WmsStockLogicDO stockLogic = validateStockLogicExists(id);
        // 唯一索引去重
        stockLogic.setWarehouseId(stockLogicMapper.flagUKeyAsLogicDelete(stockLogic.getWarehouseId()));
        stockLogic.setCompanyId(stockLogicMapper.flagUKeyAsLogicDelete(stockLogic.getCompanyId()));
        stockLogic.setDeptId(stockLogicMapper.flagUKeyAsLogicDelete(stockLogic.getDeptId()));
        stockLogic.setProductId(stockLogicMapper.flagUKeyAsLogicDelete(stockLogic.getProductId()));
        stockLogicMapper.updateById(stockLogic);
        // 删除
        stockLogicMapper.deleteById(id);
    }

    /**
     * @sign : DC4B871B2E372A75
     */
    private WmsStockLogicDO validateStockLogicExists(Long id) {
        WmsStockLogicDO stockLogic = stockLogicMapper.selectById(id);
        if (stockLogic == null) {
            throw exception(STOCK_LOGIC_NOT_EXISTS);
        }
        return stockLogic;
    }

    @Override
    public WmsStockLogicDO getStockLogic(Long id) {
        return stockLogicMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockLogicDO> getStockLogicPage(WmsStockLogicPageReqVO pageReqVO) {
        return stockLogicMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsStockLogicDO> selectStockLogic(@NotNull Long warehouseId, Long productId, Long companyId, Long deptId) {
        return stockLogicMapper.selectStockLogic(warehouseId, productId, companyId, deptId);
    }

    @Override
    public List<WmsStockLogicDO> selectStockLogic(@NotNull Long warehouseId, List<Long> productIdList) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return List.of();
        }
        return stockLogicMapper.selectStockLogic(warehouseId, productIdList);
    }

    @Override
    public WmsStockLogicDO getByUkProductOwner(Long warehouseId, Long companyId, Long deptId, Long productId, boolean createNew) {
        WmsStockLogicDO stockLogicDO = stockLogicMapper.getByUkProductOwner(warehouseId, companyId, deptId, productId);
        if (stockLogicDO == null && createNew) {
            stockLogicDO = new WmsStockLogicDO();
            stockLogicDO.setWarehouseId(warehouseId);
            stockLogicDO.setCompanyId(companyId);
            stockLogicDO.setDeptId(deptId);
            stockLogicDO.setProductId(productId);
            // 可用库存
            stockLogicDO.setAvailableQty(0);
            // 待上架数量
            stockLogicDO.setShelvePendingQty(0);
            // 待出库量
            stockLogicDO.setOutboundPendingQty(0);
            // 不良品数量
            stockLogicMapper.insert(stockLogicDO);
        }
        return stockLogicDO;
    }

    @Override
    public void insertOrUpdate(WmsStockLogicDO stockLogicDO) {
        if (stockLogicDO == null) {
            throw exception(STOCK_LOGIC_NOT_EXISTS);
        }
        // 可用量
        if (stockLogicDO.getAvailableQty() == null) {
            stockLogicDO.setAvailableQty(0);
        }
        // 待上架量
        if (stockLogicDO.getShelvePendingQty() == null) {
            stockLogicDO.setShelvePendingQty(0);
        }
        // 待出库量
        if (stockLogicDO.getOutboundPendingQty() == null) {
            stockLogicDO.setOutboundPendingQty(0);
        }
        if (stockLogicDO.getId() == null) {
            stockLogicMapper.insert(stockLogicDO);
        } else {
            stockLogicMapper.updateById(stockLogicDO);
        }
    }

    @Override
    public void assembleProducts(List<WmsStockLogicRespVO> list) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockLogicRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockLogicRespVO::getProductId, WmsStockLogicRespVO::setProduct);
    }

    @Override
    public void assembleWarehouse(List<WmsStockLogicRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockLogicRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsStockLogicRespVO::getWarehouseId, WmsStockLogicRespVO::setWarehouse);
    }

    @Override
    public void assembleDept(List<WmsStockLogicRespVO> list) {
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(StreamX.from(list).map(WmsStockLogicRespVO::getDeptId).toList());
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(list).assemble(deptVOMap, WmsStockLogicRespVO::getDeptId, WmsStockLogicRespVO::setDept);
    }

    @Override
    public void assembleCompany(List<WmsStockLogicRespVO> list) {
        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(StreamX.from(list).toSet(WmsStockLogicRespVO::getCompanyId));
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));
        StreamX.from(list).assemble(companyVOMap, WmsStockLogicRespVO::getCompanyId, WmsStockLogicRespVO::setCompany);
    }

    @Override
    public List<WmsStockLogicDO> selectByIds(List<Long> stockLogicIds) {
        if (CollectionUtils.isEmpty(stockLogicIds)) {
            return List.of();
        }
        return stockLogicMapper.selectByIds(stockLogicIds);
    }

    @Override
    public List<WmsStockLogicDO> selectByDeptIdAndProductIdAndCountryId(Long deptId, @NotNull List<Long> productIds, @NotNull String country) {

        return stockLogicMapper.selectByDeptIdAndProductIdAndCountryId(deptId, productIds, country);
    }
}

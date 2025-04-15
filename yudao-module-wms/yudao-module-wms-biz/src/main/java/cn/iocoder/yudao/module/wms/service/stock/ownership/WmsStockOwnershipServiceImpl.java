package cn.iocoder.yudao.module.wms.service.stock.ownership;

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
import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.WmsStockOwnershipMapper;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_WAREHOUSE_ID_COMPANY_ID_DEPT_ID_PRODUCT_ID_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE;

/**
 * 所有者库存 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockOwnershipServiceImpl implements WmsStockOwnershipService {

    @Resource
    private WmsStockOwnershipMapper stockOwnershipMapper;

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

    /**
     * @sign : 4AF969274F47ADC0
     */
    @Override
    public WmsStockOwnershipDO createStockOwnership(WmsStockOwnershipSaveReqVO createReqVO) {
        if (stockOwnershipMapper.getByUkProductOwner(createReqVO.getWarehouseId(), createReqVO.getCompanyId(), createReqVO.getDeptId(), createReqVO.getProductId()) != null) {
            throw exception(STOCK_OWNERSHIP_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsStockOwnershipDO stockOwnership = BeanUtils.toBean(createReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.insert(stockOwnership);
        // 返回
        return stockOwnership;
    }

    /**
     * @sign : 355EF86754CB268D
     */
    @Override
    public WmsStockOwnershipDO updateStockOwnership(WmsStockOwnershipSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockOwnershipDO exists = validateStockOwnershipExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getWarehouseId(), exists.getWarehouseId()) && Objects.equals(updateReqVO.getCompanyId(), exists.getCompanyId()) && Objects.equals(updateReqVO.getDeptId(), exists.getDeptId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCK_OWNERSHIP_WAREHOUSE_ID_COMPANY_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsStockOwnershipDO stockOwnership = BeanUtils.toBean(updateReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.updateById(stockOwnership);
        // 返回
        return stockOwnership;
    }

    /**
     * @sign : AE46873A88A81B7D
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOwnership(Long id) {
        // 校验存在
        WmsStockOwnershipDO stockOwnership = validateStockOwnershipExists(id);
        // 唯一索引去重
        stockOwnership.setWarehouseId(stockOwnershipMapper.flagUKeyAsLogicDelete(stockOwnership.getWarehouseId()));
        stockOwnership.setCompanyId(stockOwnershipMapper.flagUKeyAsLogicDelete(stockOwnership.getCompanyId()));
        stockOwnership.setDeptId(stockOwnershipMapper.flagUKeyAsLogicDelete(stockOwnership.getDeptId()));
        stockOwnership.setProductId(stockOwnershipMapper.flagUKeyAsLogicDelete(stockOwnership.getProductId()));
        stockOwnershipMapper.updateById(stockOwnership);
        // 删除
        stockOwnershipMapper.deleteById(id);
    }

    /**
     * @sign : DC4B871B2E372A75
     */
    private WmsStockOwnershipDO validateStockOwnershipExists(Long id) {
        WmsStockOwnershipDO stockOwnership = stockOwnershipMapper.selectById(id);
        if (stockOwnership == null) {
            throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        }
        return stockOwnership;
    }

    @Override
    public WmsStockOwnershipDO getStockOwnership(Long id) {
        return stockOwnershipMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockOwnershipDO> getStockOwnershipPage(WmsStockOwnershipPageReqVO pageReqVO) {
        return stockOwnershipMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, Long productId) {
        return stockOwnershipMapper.selectStockOwnership(warehouseId, productId);
    }

    @Override
    public List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, List<Long> productIdList) {
        if(CollectionUtils.isEmpty(productIdList)) {
            return List.of();
        }
        return stockOwnershipMapper.selectStockOwnership(warehouseId, productIdList);
    }

    @Override
    public WmsStockOwnershipDO getByUkProductOwner(Long warehouseId, Long companyId, Long deptId, Long productId, boolean createNew) {
        WmsStockOwnershipDO stockOwnershipDO = stockOwnershipMapper.getByUkProductOwner(warehouseId, companyId, deptId, productId);
        if(stockOwnershipDO==null && createNew) {
            stockOwnershipDO = new WmsStockOwnershipDO();
            stockOwnershipDO.setWarehouseId(warehouseId);
            stockOwnershipDO.setCompanyId(companyId);
            stockOwnershipDO.setDeptId(deptId);
            stockOwnershipDO.setProductId(productId);
            // 可用库存
            stockOwnershipDO.setAvailableQty(0);
            // 待上架数量
            stockOwnershipDO.setShelvingPendingQty(0);
            // 待出库量
            stockOwnershipDO.setOutboundPendingQty(0);
            // 不良品数量
            stockOwnershipMapper.insert(stockOwnershipDO);
        }
        return stockOwnershipDO;
    }

    @Override
    public void insertOrUpdate(WmsStockOwnershipDO stockOwnershipDO) {
        if (stockOwnershipDO == null) {
            throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        }
        // 可用量
        if (stockOwnershipDO.getAvailableQty() == null) {
            stockOwnershipDO.setAvailableQty(0);
        }
        // 待上架量
        if (stockOwnershipDO.getShelvingPendingQty() == null) {
            stockOwnershipDO.setShelvingPendingQty(0);
        }
        // 待出库量
        if (stockOwnershipDO.getOutboundPendingQty() == null) {
            stockOwnershipDO.setOutboundPendingQty(0);
        }
        if (stockOwnershipDO.getId() == null) {
            stockOwnershipMapper.insert(stockOwnershipDO);
        } else {
            stockOwnershipMapper.updateById(stockOwnershipDO);
        }
    }

    @Override
    public void assembleProducts(List<WmsStockOwnershipRespVO> list) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockOwnershipRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockOwnershipRespVO::getProductId, WmsStockOwnershipRespVO::setProduct);
    }

    @Override
    public void assembleWarehouse(List<WmsStockOwnershipRespVO> list) {

        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockOwnershipRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v-> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockOwnershipRespVO::getWarehouseId, WmsStockOwnershipRespVO::setWarehouse);
    }

    @Override
    public void assembleDept(List<WmsStockOwnershipRespVO> list) {

        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(StreamX.from(list).map(WmsStockOwnershipRespVO::getDeptId).toList());
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(list).assemble(deptVOMap, WmsStockOwnershipRespVO::getDeptId, WmsStockOwnershipRespVO::setDept);

    }

    @Override
    public void assembleCompany(List<WmsStockOwnershipRespVO> list) {
        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(StreamX.from(list).toList(WmsStockOwnershipRespVO::getCompanyId));
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));
        StreamX.from(list).assemble(companyVOMap, WmsStockOwnershipRespVO::getCompanyId, WmsStockOwnershipRespVO::setCompany);
    }

    @Override
    public List<WmsStockOwnershipDO> selectByIds(List<Long> stockOwnershipIds) {
        if(CollectionUtils.isEmpty(stockOwnershipIds)) {
            return List.of();
        }
        return stockOwnershipMapper.selectByIds(stockOwnershipIds);
    }
}

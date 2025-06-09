package cn.iocoder.yudao.module.wms.service.stock.logic.move.item;

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
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveImportExcelVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.move.item.WmsStockLogicMoveItemMapper;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCK_LOGIC_MOVE_ITEM_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCK_LOGIC_MOVE_ITEM_NOT_EXISTS;

/**
 * 逻辑库存移动详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockLogicMoveItemServiceImpl implements WmsStockLogicMoveItemService {

    @Resource
    private WmsStockLogicMoveItemMapper stockLogicMoveItemMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    private DeptApi deptApi;

    @Resource
    private FmsCompanyApi companyApi;

    @Autowired
    @Lazy
    private WmsWarehouseService warehouseService;

    /**
     * @sign : E8E4A45DABECEF25
     */
    @Override
    public WmsStockLogicMoveItemDO createStockLogicMoveItem(WmsStockLogicMoveItemSaveReqVO createReqVO) {
        if (stockLogicMoveItemMapper.getByUk(createReqVO.getLogicMoveId(), createReqVO.getProductId(), createReqVO.getFromCompanyId(), createReqVO.getFromDeptId(), createReqVO.getToCompanyId(), createReqVO.getToDeptId()) != null) {
            throw exception(STOCK_LOGIC_MOVE_ITEM_EXISTS);
        }
        // 插入
        WmsStockLogicMoveItemDO stockLogicMoveItem = BeanUtils.toBean(createReqVO, WmsStockLogicMoveItemDO.class);
        stockLogicMoveItemMapper.insert(stockLogicMoveItem);
        // 返回
        return stockLogicMoveItem;
    }

    /**
     * @sign : E33574DE9F25D07A
     */
    @Override
    public WmsStockLogicMoveItemDO updateStockLogicMoveItem(WmsStockLogicMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockLogicMoveItemDO exists = validateStockLogicMoveItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getLogicMoveId(), exists.getLogicMoveId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId()) && Objects.equals(updateReqVO.getFromCompanyId(), exists.getFromCompanyId()) && Objects.equals(updateReqVO.getFromDeptId(), exists.getFromDeptId()) && Objects.equals(updateReqVO.getToCompanyId(), exists.getToCompanyId()) && Objects.equals(updateReqVO.getToDeptId(), exists.getToDeptId())) {
            throw exception(STOCK_LOGIC_MOVE_ITEM_EXISTS);
        }
        // 更新
        WmsStockLogicMoveItemDO stockLogicMoveItem = BeanUtils.toBean(updateReqVO, WmsStockLogicMoveItemDO.class);
        stockLogicMoveItemMapper.updateById(stockLogicMoveItem);
        // 返回
        return stockLogicMoveItem;
    }

    /**
     * @sign : 21E849FB3DCA539F
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockLogicMoveItem(Long id) {
        // 校验存在
        WmsStockLogicMoveItemDO stockLogicMoveItem = validateStockLogicMoveItemExists(id);
        // 唯一索引去重
        stockLogicMoveItem.setLogicMoveId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getLogicMoveId()));
        stockLogicMoveItem.setProductId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getProductId()));
        stockLogicMoveItem.setFromCompanyId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getFromCompanyId()));
        stockLogicMoveItem.setFromDeptId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getFromDeptId()));
        stockLogicMoveItem.setToCompanyId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getToCompanyId()));
        stockLogicMoveItem.setToDeptId(stockLogicMoveItemMapper.flagUKeyAsLogicDelete(stockLogicMoveItem.getToDeptId()));
        stockLogicMoveItemMapper.updateById(stockLogicMoveItem);
        // 删除
        stockLogicMoveItemMapper.deleteById(id);
    }

    /**
     * @sign : 0DEF112540D86D2B
     */
    private WmsStockLogicMoveItemDO validateStockLogicMoveItemExists(Long id) {
        WmsStockLogicMoveItemDO stockLogicMoveItem = stockLogicMoveItemMapper.selectById(id);
        if (stockLogicMoveItem == null) {
            throw exception(STOCK_LOGIC_MOVE_ITEM_NOT_EXISTS);
        }
        return stockLogicMoveItem;
    }

    @Override
    public WmsStockLogicMoveItemDO getStockLogicMoveItem(Long id) {
        return stockLogicMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockLogicMoveItemDO> getStockLogicMoveItemPage(WmsStockLogicMoveItemPageReqVO pageReqVO) {
        return stockLogicMoveItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockLogicMoveItemDO
     */
    @Override
    public List<WmsStockLogicMoveItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockLogicMoveItemMapper.selectByIds(idList);
    }

    @Override
    public List<WmsStockLogicMoveItemDO> selectByLogicMoveId(Long logicMoveId) {
        return stockLogicMoveItemMapper.selectByLogicMoveId(logicMoveId);
    }

    @Override
    public void assembleProduct(List<WmsStockLogicMoveItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsStockLogicMoveItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsStockLogicMoveItemRespVO::getProductId, WmsStockLogicMoveItemRespVO::setProduct);
    }

    @Override
    public void assembleCompanyAndDept(List<WmsStockLogicMoveItemRespVO> itemList) {
        // 装配部门
        Set<Long> deptIds = new HashSet<>();
        deptIds.addAll(StreamX.from(itemList).map(WmsStockLogicMoveItemRespVO::getFromDeptId).toList());
        deptIds.addAll(StreamX.from(itemList).map(WmsStockLogicMoveItemRespVO::getToDeptId).toList());
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(deptIds);
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(itemList).assemble(deptVOMap, WmsStockLogicMoveItemRespVO::getFromDeptId, WmsStockLogicMoveItemRespVO::setFromDept);
        StreamX.from(itemList).assemble(deptVOMap, WmsStockLogicMoveItemRespVO::getToDeptId, WmsStockLogicMoveItemRespVO::setToDept);
        // 装配公司
        Set<Long> companyIds = new HashSet<>();
        companyIds.addAll(StreamX.from(itemList).map(WmsStockLogicMoveItemRespVO::getFromCompanyId).toList());
        companyIds.addAll(StreamX.from(itemList).map(WmsStockLogicMoveItemRespVO::getToCompanyId).toList());
        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(companyIds);
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));
        StreamX.from(itemList).assemble(companyVOMap, WmsStockLogicMoveItemRespVO::getFromCompanyId, WmsStockLogicMoveItemRespVO::setFromCompany);
        StreamX.from(itemList).assemble(companyVOMap, WmsStockLogicMoveItemRespVO::getToCompanyId, WmsStockLogicMoveItemRespVO::setToCompany);
    }

    @Override
    public void assembleWarehouseForImp(List<WmsStockLogicMoveImportExcelVO> impVOList) {
        Map<String, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMapByCode(StreamX.from(impVOList).toSet(WmsStockLogicMoveImportExcelVO::getWarehouseCode));
        StreamX.from(impVOList).assemble(warehouseDOMap, WmsStockLogicMoveImportExcelVO::getWarehouseCode, (e, p) -> {
            if (p != null) {
                e.setWarehouseId(p.getId());
            }
        });
    }

    @Override
    public void assembleCompanyAndDeptForImp(List<WmsStockLogicMoveImportExcelVO> impVOList) {
        // 装配部门
        Set<String> deptNames = new HashSet<>();
        deptNames.addAll(StreamX.from(impVOList).map(WmsStockLogicMoveImportExcelVO::getFromDeptName).toList());
        deptNames.addAll(StreamX.from(impVOList).map(WmsStockLogicMoveImportExcelVO::getToDeptName).toList());
        Map<String, DeptRespDTO> deptDTOMap = deptApi.getDeptMapByNames(deptNames);
        StreamX.from(impVOList).assemble(deptDTOMap, WmsStockLogicMoveImportExcelVO::getFromDeptName, (itm, dept) -> {
            if (dept != null) {
                itm.setFromDeptId(dept.getId());
            }
        });
        StreamX.from(impVOList).assemble(deptDTOMap, WmsStockLogicMoveImportExcelVO::getToDeptName, (itm, dept) -> {
            if (dept != null) {
                itm.setToDeptId(dept.getId());
            }
        });
        // 装配公司
        Set<String> companyNames = new HashSet<>();
        companyNames.addAll(StreamX.from(impVOList).map(WmsStockLogicMoveImportExcelVO::getFromCompanyName).toSet());
        companyNames.addAll(StreamX.from(impVOList).map(WmsStockLogicMoveImportExcelVO::getToCompanyName).toSet());
        Map<String, FmsCompanyDTO> companyMap = companyApi.getCompanyMapByNames(companyNames);
        StreamX.from(impVOList).assemble(companyMap, WmsStockLogicMoveImportExcelVO::getFromCompanyName, (itm, com) -> {
            if (com != null) {
                itm.setFromCompanyId(com.getId());
            }
        });
        StreamX.from(impVOList).assemble(companyMap, WmsStockLogicMoveImportExcelVO::getToCompanyName, (itm, com) -> {
            if (com != null) {
                itm.setToCompanyId(com.getId());
            }
        });
    }

    @Override
    public void assembleProductForImp(List<WmsStockLogicMoveImportExcelVO> impVOList) {
        Map<String, ErpProductDTO> productDTOMap = productApi.getProductMapByCode(StreamX.from(impVOList).map(WmsStockLogicMoveImportExcelVO::getProductCode).toSet());
        Map<String, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getCode(), productVO);
        }
        StreamX.from(impVOList).assemble(productVOMap, WmsStockLogicMoveImportExcelVO::getProductCode, (e, v) -> {
            if (v != null) {
                e.setProductId(v.getId());
            }
        });
    }
}

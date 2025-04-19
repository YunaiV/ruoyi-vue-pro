package cn.iocoder.yudao.module.wms.service.stock.ownership.move.item;

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
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.move.item.WmsStockOwnershipMoveItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_ITEM_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS;

/**
 * 所有者库存移动详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockOwnershipMoveItemServiceImpl implements WmsStockOwnershipMoveItemService {

    @Resource
    private WmsStockOwnershipMoveItemMapper stockOwnershipMoveItemMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    private DeptApi deptApi;

    @Resource
    private FmsCompanyApi companyApi;
    /**
     * @sign : E8E4A45DABECEF25
     */
    @Override
    public WmsStockOwnershipMoveItemDO createStockOwnershipMoveItem(WmsStockOwnershipMoveItemSaveReqVO createReqVO) {
        if (stockOwnershipMoveItemMapper.getByUk(createReqVO.getOwnershipMoveId(), createReqVO.getProductId(), createReqVO.getFromCompanyId(), createReqVO.getFromDeptId(), createReqVO.getToCompanyId(), createReqVO.getToDeptId()) != null) {
            throw exception(STOCK_OWNERSHIP_MOVE_ITEM_EXISTS);
        }
        // 插入
        WmsStockOwnershipMoveItemDO stockOwnershipMoveItem = BeanUtils.toBean(createReqVO, WmsStockOwnershipMoveItemDO.class);
        stockOwnershipMoveItemMapper.insert(stockOwnershipMoveItem);
        // 返回
        return stockOwnershipMoveItem;
    }

    /**
     * @sign : E33574DE9F25D07A
     */
    @Override
    public WmsStockOwnershipMoveItemDO updateStockOwnershipMoveItem(WmsStockOwnershipMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockOwnershipMoveItemDO exists = validateStockOwnershipMoveItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getOwnershipMoveId(), exists.getOwnershipMoveId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId()) && Objects.equals(updateReqVO.getFromCompanyId(), exists.getFromCompanyId()) && Objects.equals(updateReqVO.getFromDeptId(), exists.getFromDeptId()) && Objects.equals(updateReqVO.getToCompanyId(), exists.getToCompanyId()) && Objects.equals(updateReqVO.getToDeptId(), exists.getToDeptId())) {
            throw exception(STOCK_OWNERSHIP_MOVE_ITEM_EXISTS);
        }
        // 更新
        WmsStockOwnershipMoveItemDO stockOwnershipMoveItem = BeanUtils.toBean(updateReqVO, WmsStockOwnershipMoveItemDO.class);
        stockOwnershipMoveItemMapper.updateById(stockOwnershipMoveItem);
        // 返回
        return stockOwnershipMoveItem;
    }

    /**
     * @sign : 21E849FB3DCA539F
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOwnershipMoveItem(Long id) {
        // 校验存在
        WmsStockOwnershipMoveItemDO stockOwnershipMoveItem = validateStockOwnershipMoveItemExists(id);
        // 唯一索引去重
        stockOwnershipMoveItem.setOwnershipMoveId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getOwnershipMoveId()));
        stockOwnershipMoveItem.setProductId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getProductId()));
        stockOwnershipMoveItem.setFromCompanyId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getFromCompanyId()));
        stockOwnershipMoveItem.setFromDeptId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getFromDeptId()));
        stockOwnershipMoveItem.setToCompanyId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getToCompanyId()));
        stockOwnershipMoveItem.setToDeptId(stockOwnershipMoveItemMapper.flagUKeyAsLogicDelete(stockOwnershipMoveItem.getToDeptId()));
        stockOwnershipMoveItemMapper.updateById(stockOwnershipMoveItem);
        // 删除
        stockOwnershipMoveItemMapper.deleteById(id);
    }

    /**
     * @sign : 0DEF112540D86D2B
     */
    private WmsStockOwnershipMoveItemDO validateStockOwnershipMoveItemExists(Long id) {
        WmsStockOwnershipMoveItemDO stockOwnershipMoveItem = stockOwnershipMoveItemMapper.selectById(id);
        if (stockOwnershipMoveItem == null) {
            throw exception(STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS);
        }
        return stockOwnershipMoveItem;
    }

    @Override
    public WmsStockOwnershipMoveItemDO getStockOwnershipMoveItem(Long id) {
        return stockOwnershipMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockOwnershipMoveItemDO> getStockOwnershipMoveItemPage(WmsStockOwnershipMoveItemPageReqVO pageReqVO) {
        return stockOwnershipMoveItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockOwnershipMoveItemDO
     */
    public List<WmsStockOwnershipMoveItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockOwnershipMoveItemMapper.selectByIds(idList);
    }

    @Override
    public List<WmsStockOwnershipMoveItemDO> selectByOwnershipMoveId(Long ownershipMoveId) {
        return stockOwnershipMoveItemMapper.selectByOwnershipMoveId(ownershipMoveId);
    }


    @Override
    public void assembleProduct(List<WmsStockOwnershipMoveItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsStockOwnershipMoveItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsStockOwnershipMoveItemRespVO::getProductId, WmsStockOwnershipMoveItemRespVO::setProduct);
    }

    @Override
    public void assembleCompanyAndDept(List<WmsStockOwnershipMoveItemRespVO> itemList) {

        // 装配部门
        Set<Long> deptIds=new HashSet<>();
        deptIds.addAll(StreamX.from(itemList).map(WmsStockOwnershipMoveItemRespVO::getFromDeptId).toList());
        deptIds.addAll(StreamX.from(itemList).map(WmsStockOwnershipMoveItemRespVO::getToDeptId).toList());

        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(deptIds);
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(itemList).assemble(deptVOMap, WmsStockOwnershipMoveItemRespVO::getFromDeptId, WmsStockOwnershipMoveItemRespVO::setFromDept);
        StreamX.from(itemList).assemble(deptVOMap, WmsStockOwnershipMoveItemRespVO::getToDeptId, WmsStockOwnershipMoveItemRespVO::setToDept);


        // 装配公司
        List<Long> companyIds=new ArrayList<>();
        companyIds.addAll(StreamX.from(itemList).map(WmsStockOwnershipMoveItemRespVO::getFromCompanyId).toList());
        companyIds.addAll(StreamX.from(itemList).map(WmsStockOwnershipMoveItemRespVO::getToCompanyId).toList());

        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(companyIds);
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));

        StreamX.from(itemList).assemble(companyVOMap, WmsStockOwnershipMoveItemRespVO::getFromCompanyId, WmsStockOwnershipMoveItemRespVO::setFromCompany);
        StreamX.from(itemList).assemble(companyVOMap, WmsStockOwnershipMoveItemRespVO::getToCompanyId, WmsStockOwnershipMoveItemRespVO::setToCompany);

    }
}

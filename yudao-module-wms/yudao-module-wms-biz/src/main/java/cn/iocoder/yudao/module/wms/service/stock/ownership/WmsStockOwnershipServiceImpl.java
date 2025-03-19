package cn.iocoder.yudao.module.wms.service.stock.ownership;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.WmsStockOwnershipMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

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

    /**
     * @sign : 986513F27AB1F6B0
     */
    @Override
    public WmsStockOwnershipDO createStockOwnership(WmsStockOwnershipSaveReqVO createReqVO) {
        if (stockOwnershipMapper.getByUk(createReqVO.getWarehouseId(), createReqVO.getDeptId(), createReqVO.getProductId()) != null) {
            throw exception(STOCK_OWNERSHIP_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 插入
        WmsStockOwnershipDO stockOwnership = BeanUtils.toBean(createReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.insert(stockOwnership);
        // 返回
        return stockOwnership;
    }

    /**
     * @sign : ACC816A98557AA2E
     */
    @Override
    public WmsStockOwnershipDO updateStockOwnership(WmsStockOwnershipSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockOwnershipDO exists = validateStockOwnershipExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getWarehouseId(), exists.getWarehouseId()) && Objects.equals(updateReqVO.getDeptId(), exists.getDeptId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCK_OWNERSHIP_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE);
        }
        // 更新
        WmsStockOwnershipDO stockOwnership = BeanUtils.toBean(updateReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.updateById(stockOwnership);
        // 返回
        return stockOwnership;
    }

    /**
     * @sign : 211EF09CCAD1AEF1
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOwnership(Long id) {
        // 校验存在
        WmsStockOwnershipDO stockOwnership = validateStockOwnershipExists(id);
        // 唯一索引去重
        stockOwnership.setWarehouseId(stockOwnershipMapper.flagUKeyAsLogicDelete(stockOwnership.getWarehouseId()));
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
}
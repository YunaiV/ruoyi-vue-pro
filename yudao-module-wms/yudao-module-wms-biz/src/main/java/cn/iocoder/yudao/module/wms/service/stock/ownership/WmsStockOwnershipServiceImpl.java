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

    @Override
    public Long createStockOwnership(WmsStockOwnershipSaveReqVO createReqVO) {
        // 插入
        WmsStockOwnershipDO stockOwnership = BeanUtils.toBean(createReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.insert(stockOwnership);
        // 返回
        return stockOwnership.getId();
    }

    @Override
    public void updateStockOwnership(WmsStockOwnershipSaveReqVO updateReqVO) {
        // 校验存在
        validateStockOwnershipExists(updateReqVO.getId());
        // 更新
        WmsStockOwnershipDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockOwnershipDO.class);
        stockOwnershipMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockOwnership(Long id) {
        // 校验存在
        validateStockOwnershipExists(id);
        // 删除
        stockOwnershipMapper.deleteById(id);
    }

    private void validateStockOwnershipExists(Long id) {
        if (stockOwnershipMapper.selectById(id) == null) {
           // throw exception(STOCK_OWNERSHIP_NOT_EXISTS);
        }
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
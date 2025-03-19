package cn.iocoder.yudao.module.wms.service.stock.bin;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.WmsStockBinMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

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

    @Override
    public Long createStockBin(WmsStockBinSaveReqVO createReqVO) {
        // 插入
        WmsStockBinDO stockBin = BeanUtils.toBean(createReqVO, WmsStockBinDO.class);
        stockBinMapper.insert(stockBin);
        // 返回
        return stockBin.getId();
    }

    @Override
    public void updateStockBin(WmsStockBinSaveReqVO updateReqVO) {
        // 校验存在
        validateStockBinExists(updateReqVO.getId());
        // 更新
        WmsStockBinDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockBinDO.class);
        stockBinMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockBin(Long id) {
        // 校验存在
        validateStockBinExists(id);
        // 删除
        stockBinMapper.deleteById(id);
    }

    private void validateStockBinExists(Long id) {
        if (stockBinMapper.selectById(id) == null) {
            //throw exception(STOCK_BIN_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockBinDO getStockBin(Long id) {
        return stockBinMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO) {
        return stockBinMapper.selectPage(pageReqVO);
    }

}
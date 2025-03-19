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
    public PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO) {
        return stockBinMapper.selectPage(pageReqVO);
    }
}
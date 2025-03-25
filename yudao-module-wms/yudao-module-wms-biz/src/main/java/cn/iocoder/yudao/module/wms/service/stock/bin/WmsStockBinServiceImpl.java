package cn.iocoder.yudao.module.wms.service.stock.bin;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
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

    @Resource
    WmsStockFlowService stockFlowService;

    @Resource
    @Lazy
    WmsStockWarehouseService stockWarehouseService;

    @Autowired
    private WmsStockOwnershipService wmsStockOwnershipService;

    @Resource
    @Lazy
    WmsInboundItemService inboundItemService;

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
    public WmsStockBinDO getStockBin(Long binId, Long productId) {
        return stockBinMapper.getByBinIdAndProductId(binId, productId);
    }

    @Override
    public PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO) {
        return stockBinMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsStockBinDO> selectStockBin(Long warehouseId, Long productId) {
        return stockBinMapper.selectStockBin(warehouseId, productId);
    }

    @Override
    public Map<Long, Map<Long, WmsStockBinDO>> getStockBinMap(Collection<Long> binIds, Collection<Long> productIds) {
        Map<Long, Map<Long, WmsStockBinDO>> result = new HashMap<>();
        List<WmsStockBinDO> list = stockBinMapper.selectStockBinList(binIds, productIds);
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
}

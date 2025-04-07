package cn.iocoder.yudao.module.wms.service.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item.WmsStockBinMoveItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_NOT_EXISTS;

/**
 * 库位移动详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinMoveItemServiceImpl implements WmsStockBinMoveItemService {

    @Resource
    private WmsStockBinMoveItemMapper stockBinMoveItemMapper;

    /**
     * @sign : C50CC3A83D97A3B3
     */
    @Override
    public WmsStockBinMoveItemDO createStockBinMoveItem(WmsStockBinMoveItemSaveReqVO createReqVO) {
        if (stockBinMoveItemMapper.getByUk(createReqVO.getBinMoveId(), createReqVO.getProductId(), createReqVO.getFromBinId(), createReqVO.getToBinId()) != null) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }
        // 插入
        WmsStockBinMoveItemDO stockBinMoveItem = BeanUtils.toBean(createReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.insert(stockBinMoveItem);
        // 返回
        return stockBinMoveItem;
    }

    /**
     * @sign : E840376DE7D4CCD9
     */
    @Override
    public WmsStockBinMoveItemDO updateStockBinMoveItem(WmsStockBinMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockBinMoveItemDO exists = validateStockBinMoveItemExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getBinMoveId(), exists.getBinMoveId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId()) && Objects.equals(updateReqVO.getFromBinId(), exists.getFromBinId()) && Objects.equals(updateReqVO.getToBinId(), exists.getToBinId())) {
            throw exception(STOCK_BIN_MOVE_ITEM_EXISTS);
        }
        // 更新
        WmsStockBinMoveItemDO stockBinMoveItem = BeanUtils.toBean(updateReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.updateById(stockBinMoveItem);
        // 返回
        return stockBinMoveItem;
    }

    /**
     * @sign : 7588E6DEA13FF799
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockBinMoveItem(Long id) {
        // 校验存在
        WmsStockBinMoveItemDO stockBinMoveItem = validateStockBinMoveItemExists(id);
        // 唯一索引去重
        stockBinMoveItem.setBinMoveId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getBinMoveId()));
        stockBinMoveItem.setProductId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getProductId()));
        stockBinMoveItem.setFromBinId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getFromBinId()));
        stockBinMoveItem.setToBinId(stockBinMoveItemMapper.flagUKeyAsLogicDelete(stockBinMoveItem.getToBinId()));
        stockBinMoveItemMapper.updateById(stockBinMoveItem);
        // 删除
        stockBinMoveItemMapper.deleteById(id);
    }

    /**
     * @sign : F8648A012C510A3D
     */
    private WmsStockBinMoveItemDO validateStockBinMoveItemExists(Long id) {
        WmsStockBinMoveItemDO stockBinMoveItem = stockBinMoveItemMapper.selectById(id);
        if (stockBinMoveItem == null) {
            throw exception(STOCK_BIN_MOVE_ITEM_NOT_EXISTS);
        }
        return stockBinMoveItem;
    }

    @Override
    public WmsStockBinMoveItemDO getStockBinMoveItem(Long id) {
        return stockBinMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveItemDO> getStockBinMoveItemPage(WmsStockBinMoveItemPageReqVO pageReqVO) {
        return stockBinMoveItemMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockBinMoveItemDO
     */
    public List<WmsStockBinMoveItemDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockBinMoveItemMapper.selectByIds(idList);
    }
}

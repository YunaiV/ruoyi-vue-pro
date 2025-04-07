package cn.iocoder.yudao.module.wms.service.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item.WmsStockBinMoveItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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

    @Override
    public Long createStockBinMoveItem(WmsStockBinMoveItemSaveReqVO createReqVO) {
        // 插入
        WmsStockBinMoveItemDO stockBinMoveItem = BeanUtils.toBean(createReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.insert(stockBinMoveItem);
        // 返回
        return stockBinMoveItem.getId();
    }

    @Override
    public void updateStockBinMoveItem(WmsStockBinMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        validateStockBinMoveItemExists(updateReqVO.getId());
        // 更新
        WmsStockBinMoveItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockBinMoveItemDO.class);
        stockBinMoveItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockBinMoveItem(Long id) {
        // 校验存在
        validateStockBinMoveItemExists(id);
        // 删除
        stockBinMoveItemMapper.deleteById(id);
    }

    private void validateStockBinMoveItemExists(Long id) {
        if (stockBinMoveItemMapper.selectById(id) == null) {
//            throw exception(STOCK_BIN_MOVE_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockBinMoveItemDO getStockBinMoveItem(Long id) {
        return stockBinMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveItemDO> getStockBinMoveItemPage(WmsStockBinMoveItemPageReqVO pageReqVO) {
        return stockBinMoveItemMapper.selectPage(pageReqVO);
    }

}
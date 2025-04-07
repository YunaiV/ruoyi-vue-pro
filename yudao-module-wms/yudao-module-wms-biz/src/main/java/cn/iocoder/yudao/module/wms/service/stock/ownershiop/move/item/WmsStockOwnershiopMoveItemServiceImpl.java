package cn.iocoder.yudao.module.wms.service.stock.ownershiop.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo.WmsStockOwnershiopMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo.WmsStockOwnershiopMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 所有者库存移动详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockOwnershiopMoveItemServiceImpl implements WmsStockOwnershiopMoveItemService {

    @Resource
    private WmsStockOwnershiopMoveItemMapper stockOwnershiopMoveItemMapper;

    @Override
    public Long createStockOwnershiopMoveItem(WmsStockOwnershiopMoveItemSaveReqVO createReqVO) {
        // 插入
        WmsStockOwnershiopMoveItemDO stockOwnershiopMoveItem = BeanUtils.toBean(createReqVO, WmsStockOwnershiopMoveItemDO.class);
        stockOwnershiopMoveItemMapper.insert(stockOwnershiopMoveItem);
        // 返回
        return stockOwnershiopMoveItem.getId();
    }

    @Override
    public void updateStockOwnershiopMoveItem(WmsStockOwnershiopMoveItemSaveReqVO updateReqVO) {
        // 校验存在
        validateStockOwnershiopMoveItemExists(updateReqVO.getId());
        // 更新
        WmsStockOwnershiopMoveItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockOwnershiopMoveItemDO.class);
        stockOwnershiopMoveItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockOwnershiopMoveItem(Long id) {
        // 校验存在
        validateStockOwnershiopMoveItemExists(id);
        // 删除
        stockOwnershiopMoveItemMapper.deleteById(id);
    }

    private void validateStockOwnershiopMoveItemExists(Long id) {
        if (stockOwnershiopMoveItemMapper.selectById(id) == null) {
            // throw exception(STOCK_OWNERSHIOP_MOVE_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockOwnershiopMoveItemDO getStockOwnershiopMoveItem(Long id) {
        return stockOwnershiopMoveItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockOwnershiopMoveItemDO> getStockOwnershiopMoveItemPage(WmsStockOwnershiopMoveItemPageReqVO pageReqVO) {
        return stockOwnershiopMoveItemMapper.selectPage(pageReqVO);
    }

}
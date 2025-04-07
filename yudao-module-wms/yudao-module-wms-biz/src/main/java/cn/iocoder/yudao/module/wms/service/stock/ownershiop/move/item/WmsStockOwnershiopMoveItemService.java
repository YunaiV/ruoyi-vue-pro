package cn.iocoder.yudao.module.wms.service.stock.ownershiop.move.item;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 所有者库存移动详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockOwnershiopMoveItemService {

    /**
     * 创建所有者库存移动详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockOwnershiopMoveItem(@Valid WmsStockOwnershiopMoveItemSaveReqVO createReqVO);

    /**
     * 更新所有者库存移动详情
     *
     * @param updateReqVO 更新信息
     */
    void updateStockOwnershiopMoveItem(@Valid WmsStockOwnershiopMoveItemSaveReqVO updateReqVO);

    /**
     * 删除所有者库存移动详情
     *
     * @param id 编号
     */
    void deleteStockOwnershiopMoveItem(Long id);

    /**
     * 获得所有者库存移动详情
     *
     * @param id 编号
     * @return 所有者库存移动详情
     */
    WmsStockOwnershiopMoveItemDO getStockOwnershiopMoveItem(Long id);

    /**
     * 获得所有者库存移动详情分页
     *
     * @param pageReqVO 分页查询
     * @return 所有者库存移动详情分页
     */
    PageResult<WmsStockOwnershiopMoveItemDO> getStockOwnershiopMoveItemPage(WmsStockOwnershiopMoveItemPageReqVO pageReqVO);

}
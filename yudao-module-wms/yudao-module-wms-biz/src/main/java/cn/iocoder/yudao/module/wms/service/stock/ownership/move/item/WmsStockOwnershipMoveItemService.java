package cn.iocoder.yudao.module.wms.service.stock.ownership.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 所有者库存移动详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockOwnershipMoveItemService {

    /**
     * 创建所有者库存移动详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockOwnershipMoveItemDO createStockOwnershipMoveItem(@Valid WmsStockOwnershipMoveItemSaveReqVO createReqVO);

    /**
     * 更新所有者库存移动详情
     *
     * @param updateReqVO 更新信息
     */
    WmsStockOwnershipMoveItemDO updateStockOwnershipMoveItem(@Valid WmsStockOwnershipMoveItemSaveReqVO updateReqVO);

    /**
     * 删除所有者库存移动详情
     *
     * @param id 编号
     */
    void deleteStockOwnershipMoveItem(Long id);

    /**
     * 获得所有者库存移动详情
     *
     * @param id 编号
     * @return 所有者库存移动详情
     */
    WmsStockOwnershipMoveItemDO getStockOwnershipMoveItem(Long id);

    /**
     * 获得所有者库存移动详情分页
     *
     * @param pageReqVO 分页查询
     * @return 所有者库存移动详情分页
     */
    PageResult<WmsStockOwnershipMoveItemDO> getStockOwnershipMoveItemPage(WmsStockOwnershipMoveItemPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockOwnershipMoveItemDO
     */
    List<WmsStockOwnershipMoveItemDO> selectByIds(List<Long> idList);

    List<WmsStockOwnershipMoveItemDO> selectByOwnershipMoveId(Long ownershipMoveId);

    void assembleProduct(List<WmsStockOwnershipMoveItemRespVO> itemList);

    void assembleCompanyAndDept(List<WmsStockOwnershipMoveItemRespVO> itemList);
}

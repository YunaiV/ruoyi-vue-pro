package cn.iocoder.yudao.module.wms.service.stock.ownership.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 所有者库存移动 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockOwnershipMoveService {

    /**
     * 创建所有者库存移动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockOwnershipMoveDO createStockOwnershipMove(@Valid WmsStockOwnershipMoveSaveReqVO createReqVO);

    /**
     * 更新所有者库存移动
     *
     * @param updateReqVO 更新信息
     */
    WmsStockOwnershipMoveDO updateStockOwnershipMove(@Valid WmsStockOwnershipMoveSaveReqVO updateReqVO);

    /**
     * 删除所有者库存移动
     *
     * @param id 编号
     */
    void deleteStockOwnershipMove(Long id);

    /**
     * 获得所有者库存移动
     *
     * @param id 编号
     * @return 所有者库存移动
     */
    WmsStockOwnershipMoveDO getStockOwnershipMove(Long id);

    /**
     * 获得所有者库存移动分页
     *
     * @param pageReqVO 分页查询
     * @return 所有者库存移动分页
     */
    PageResult<WmsStockOwnershipMoveDO> getStockOwnershipMovePage(WmsStockOwnershipMovePageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockOwnershipMoveDO
     */
    List<WmsStockOwnershipMoveDO> selectByIds(List<Long> idList);

    void finishMove(WmsStockOwnershipMoveDO ownershipMoveDO, List<WmsStockOwnershipMoveItemDO> ownershipMoveItemDOList);
}

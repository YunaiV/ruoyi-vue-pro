package cn.iocoder.yudao.module.wms.service.order.movement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDO;
import jakarta.validation.Valid;

/**
 * WMS 移库单 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsMovementOrderService {

    /**
     * 创建移库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMovementOrder(@Valid WmsMovementOrderSaveReqVO createReqVO);

    /**
     * 更新移库单
     *
     * @param updateReqVO 更新信息
     */
    void updateMovementOrder(@Valid WmsMovementOrderSaveReqVO updateReqVO);

    /**
     * 删除移库单
     *
     * @param id 编号
     */
    void deleteMovementOrder(Long id);

    /**
     * 完成移库
     *
     * @param id 编号
     */
    void completeMovementOrder(Long id);

    /**
     * 作废移库单
     *
     * @param id 编号
     */
    void cancelMovementOrder(Long id);

    /**
     * 获得移库单
     *
     * @param id 编号
     * @return 移库单
     */
    WmsMovementOrderDO getMovementOrder(Long id);

    /**
     * 获得移库单分页
     *
     * @param pageReqVO 分页查询
     * @return 移库单分页
     */
    PageResult<WmsMovementOrderDO> getMovementOrderPage(WmsMovementOrderPageReqVO pageReqVO);

    /**
     * 获得指定仓库的移库单数量
     *
     * @param warehouseId 仓库编号
     * @return 移库单数量
     */
    long getMovementOrderCountByWarehouseId(Long warehouseId);

}

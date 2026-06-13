package cn.iocoder.yudao.module.wms.service.order.movement;

import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;

import java.util.Collection;
import java.util.List;

/**
 * WMS 移库单明细 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsMovementOrderDetailService {

    /**
     * 创建移库单明细列表
     *
     * @param orderId 移库单编号
     * @param reqVO 移库单保存信息
     */
    void createMovementOrderDetailList(Long orderId, WmsMovementOrderSaveReqVO reqVO);

    /**
     * 更新移库单明细列表
     *
     * @param orderId 移库单编号
     * @param reqVO 移库单保存信息
     */
    void updateMovementOrderDetailList(Long orderId, WmsMovementOrderSaveReqVO reqVO);

    /**
     * 按移库单编号删除明细列表
     *
     * @param orderId 移库单编号
     */
    void deleteMovementOrderDetailListByOrderId(Long orderId);

    /**
     * 按移库单编号获得明细列表
     *
     * @param orderId 移库单编号
     * @return 明细列表
     */
    List<WmsMovementOrderDetailDO> getMovementOrderDetailList(Long orderId);

    /**
     * 按移库单编号集合获得明细列表
     *
     * @param orderIds 移库单编号集合
     * @return 明细列表
     */
    List<WmsMovementOrderDetailDO> getMovementOrderDetailList(Collection<Long> orderIds);

    /**
     * 校验移库单明细列表存在
     *
     * @param orderId 移库单编号
     * @return 明细列表
     */
    List<WmsMovementOrderDetailDO> validateMovementOrderDetailListExists(Long orderId);

    /**
     * 获得指定 SKU 的移库单明细数量
     *
     * @param skuId SKU 编号
     * @return 移库单明细数量
     */
    long getMovementOrderDetailCountBySkuId(Long skuId);

}

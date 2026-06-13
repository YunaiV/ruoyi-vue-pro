package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;

import java.util.Collection;
import java.util.List;

/**
 * WMS 出库单明细 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsShipmentOrderDetailService {

    /**
     * 创建出库单明细列表
     *
     * @param orderId 出库单编号
     * @param reqVO 出库单保存信息
     */
    void createShipmentOrderDetailList(Long orderId, WmsShipmentOrderSaveReqVO reqVO);

    /**
     * 更新出库单明细列表
     *
     * @param orderId 出库单编号
     * @param reqVO 出库单保存信息
     */
    void updateShipmentOrderDetailList(Long orderId, WmsShipmentOrderSaveReqVO reqVO);

    /**
     * 按出库单编号删除明细列表
     *
     * @param orderId 出库单编号
     */
    void deleteShipmentOrderDetailListByOrderId(Long orderId);

    /**
     * 按出库单编号获得明细列表
     *
     * @param orderId 出库单编号
     * @return 明细列表
     */
    List<WmsShipmentOrderDetailDO> getShipmentOrderDetailList(Long orderId);

    /**
     * 按出库单编号集合获得明细列表
     *
     * @param orderIds 出库单编号集合
     * @return 明细列表
     */
    List<WmsShipmentOrderDetailDO> getShipmentOrderDetailList(Collection<Long> orderIds);

    /**
     * 校验出库单明细列表存在
     *
     * @param orderId 出库单编号
     * @return 明细列表
     */
    List<WmsShipmentOrderDetailDO> validateShipmentOrderDetailListExists(Long orderId);

    /**
     * 获得指定 SKU 的出库单明细数量
     *
     * @param skuId SKU 编号
     * @return 出库单明细数量
     */
    long getShipmentOrderDetailCountBySkuId(Long skuId);

}

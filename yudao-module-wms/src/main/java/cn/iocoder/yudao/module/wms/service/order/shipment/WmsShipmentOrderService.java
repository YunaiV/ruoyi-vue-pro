package cn.iocoder.yudao.module.wms.service.order.shipment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDO;
import jakarta.validation.Valid;

/**
 * WMS 出库单 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsShipmentOrderService {

    /**
     * 创建出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createShipmentOrder(@Valid WmsShipmentOrderSaveReqVO createReqVO);

    /**
     * 更新出库单
     *
     * @param updateReqVO 更新信息
     */
    void updateShipmentOrder(@Valid WmsShipmentOrderSaveReqVO updateReqVO);

    /**
     * 删除出库单
     *
     * @param id 编号
     */
    void deleteShipmentOrder(Long id);

    /**
     * 完成出库
     *
     * @param id 编号
     */
    void completeShipmentOrder(Long id);

    /**
     * 作废出库单
     *
     * @param id 编号
     */
    void cancelShipmentOrder(Long id);

    /**
     * 获得出库单
     *
     * @param id 编号
     * @return 出库单
     */
    WmsShipmentOrderDO getShipmentOrder(Long id);

    /**
     * 获得出库单分页
     *
     * @param pageReqVO 分页查询
     * @return 出库单分页
     */
    PageResult<WmsShipmentOrderDO> getShipmentOrderPage(WmsShipmentOrderPageReqVO pageReqVO);

    /**
     * 获得指定往来企业的出库单数量
     *
     * @param merchantId 往来企业编号
     * @return 出库单数量
     */
    long getShipmentOrderCountByMerchantId(Long merchantId);

    /**
     * 获得指定仓库的出库单数量
     *
     * @param warehouseId 仓库编号
     * @return 出库单数量
     */
    long getShipmentOrderCountByWarehouseId(Long warehouseId);

}

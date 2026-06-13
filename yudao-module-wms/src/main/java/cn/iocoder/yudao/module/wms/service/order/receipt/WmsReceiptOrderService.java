package cn.iocoder.yudao.module.wms.service.order.receipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDO;
import jakarta.validation.Valid;

/**
 * WMS 入库单 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsReceiptOrderService {

    /**
     * 创建入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReceiptOrder(@Valid WmsReceiptOrderSaveReqVO createReqVO);

    /**
     * 更新入库单
     *
     * @param updateReqVO 更新信息
     */
    void updateReceiptOrder(@Valid WmsReceiptOrderSaveReqVO updateReqVO);

    /**
     * 删除入库单
     *
     * @param id 编号
     */
    void deleteReceiptOrder(Long id);

    /**
     * 完成入库
     *
     * @param id 编号
     */
    void completeReceiptOrder(Long id);

    /**
     * 作废入库单
     *
     * @param id 编号
     */
    void cancelReceiptOrder(Long id);

    /**
     * 获得入库单
     *
     * @param id 编号
     * @return 入库单
     */
    WmsReceiptOrderDO getReceiptOrder(Long id);

    /**
     * 获得入库单分页
     *
     * @param pageReqVO 分页查询
     * @return 入库单分页
     */
    PageResult<WmsReceiptOrderDO> getReceiptOrderPage(WmsReceiptOrderPageReqVO pageReqVO);

    /**
     * 获得指定往来企业的入库单数量
     *
     * @param merchantId 往来企业编号
     * @return 入库单数量
     */
    long getReceiptOrderCountByMerchantId(Long merchantId);

    /**
     * 获得指定仓库的入库单数量
     *
     * @param warehouseId 仓库编号
     * @return 入库单数量
     */
    long getReceiptOrderCountByWarehouseId(Long warehouseId);

}

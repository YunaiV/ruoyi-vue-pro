package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * ERP 销售订单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSaleOrderService {

    /**
     * 创建销售订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSaleOrder(@Valid ErpSaleOrderSaveReqVO createReqVO);

    /**
     * 更新销售订单
     *
     * @param updateReqVO 更新信息
     */
    void updateSaleOrder(@Valid ErpSaleOrderSaveReqVO updateReqVO);

    /**
     * 更新销售订单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateSaleOrderStatus(Long id, Integer status);

    /**
     * 删除销售订单
     *
     * @param ids 编号数组
     */
    void deleteSaleOrder(List<Long> ids);

    /**
     * 获得销售订单
     *
     * @param id 编号
     * @return 销售订单
     */
    ErpSaleOrderDO getSaleOrder(Long id);

    /**
     * 获得销售订单分页
     *
     * @param pageReqVO 分页查询
     * @return 销售订单分页
     */
    PageResult<ErpSaleOrderDO> getSaleOrderPage(ErpSaleOrderPageReqVO pageReqVO);

    // ==================== 销售订单项 ====================

    /**
     * 获得销售订单项列表
     *
     * @param orderId 销售订单编号
     * @return 销售订单项列表
     */
    List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderId(Long orderId);

    /**
     * 获得销售订单项 List
     *
     * @param orderIds 销售订单编号数组
     * @return 销售订单项 List
     */
    List<ErpSaleOrderItemDO> getSaleOrderItemListByOrderIds(Collection<Long> orderIds);

}
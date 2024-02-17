package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 采购订单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpPurchaseOrderService {

    /**
     * 创建采购订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseOrder(@Valid ErpPurchaseOrderSaveReqVO createReqVO);

    /**
     * 更新采购订单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseOrder(@Valid ErpPurchaseOrderSaveReqVO updateReqVO);

    /**
     * 更新采购订单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updatePurchaseOrderStatus(Long id, Integer status);

    /**
     * 更新采购订单的入库数量
     *
     * @param id 编号
     * @param inCountMap 入库数量 Map：key 采购订单项编号；value 入库数量
     */
    void updatePurchaseOrderInCount(Long id, Map<Long, BigDecimal> inCountMap);

    /**
     * 更新采购订单的退货数量
     *
     * @param orderId 编号
     * @param returnCountMap 退货数量 Map：key 采购订单项编号；value 退货数量
     */
    void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 删除采购订单
     *
     * @param ids 编号数组
     */
    void deletePurchaseOrder(List<Long> ids);

    /**
     * 获得采购订单
     *
     * @param id 编号
     * @return 采购订单
     */
    ErpPurchaseOrderDO getPurchaseOrder(Long id);

    /**
     * 校验采购订单，已经审核通过
     *
     * @param id 编号
     * @return 采购订单
     */
    ErpPurchaseOrderDO validatePurchaseOrder(Long id);

    /**
     * 获得采购订单分页
     *
     * @param pageReqVO 分页查询
     * @return 采购订单分页
     */
    PageResult<ErpPurchaseOrderDO> getPurchaseOrderPage(ErpPurchaseOrderPageReqVO pageReqVO);

    // ==================== 采购订单项 ====================

    /**
     * 获得采购订单项列表
     *
     * @param orderId 采购订单编号
     * @return 采购订单项列表
     */
    List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId);

    /**
     * 获得采购订单项 List
     *
     * @param orderIds 采购订单编号数组
     * @return 采购订单项 List
     */
    List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds);

}
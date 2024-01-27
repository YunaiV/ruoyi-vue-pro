package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import jakarta.validation.Valid;

/**
 * ERP 销售订单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSaleOrderService {

    /**
     * 创建ERP 销售订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSaleOrder(@Valid ErpSaleOrderSaveReqVO createReqVO);

    /**
     * 更新ERP 销售订单
     *
     * @param updateReqVO 更新信息
     */
    void updateSaleOrder(@Valid ErpSaleOrderSaveReqVO updateReqVO);

    /**
     * 删除ERP 销售订单
     *
     * @param id 编号
     */
    void deleteSaleOrder(Long id);

    /**
     * 获得ERP 销售订单
     *
     * @param id 编号
     * @return ERP 销售订单
     */
    ErpSaleOrderDO getSaleOrder(Long id);

    /**
     * 获得ERP 销售订单分页
     *
     * @param pageReqVO 分页查询
     * @return ERP 销售订单分页
     */
    PageResult<ErpSaleOrderDO> getSaleOrderPage(ErpSaleOrderPageReqVO pageReqVO);

}
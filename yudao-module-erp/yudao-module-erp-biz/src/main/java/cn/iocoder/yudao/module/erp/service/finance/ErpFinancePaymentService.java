package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * ERP 付款单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpFinancePaymentService {

    /**
     * 创建付款单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinancePayment(@Valid ErpFinancePaymentSaveReqVO createReqVO);

    /**
     * 更新付款单
     *
     * @param updateReqVO 更新信息
     */
    void updateFinancePayment(@Valid ErpFinancePaymentSaveReqVO updateReqVO);

    /**
     * 更新付款单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateFinancePaymentStatus(Long id, Integer status);

    /**
     * 删除付款单
     *
     * @param ids 编号数组
     */
    void deleteFinancePayment(List<Long> ids);

    /**
     * 获得付款单
     *
     * @param id 编号
     * @return 付款单
     */
    ErpFinancePaymentDO getFinancePayment(Long id);

    /**
     * 获得付款单分页
     *
     * @param pageReqVO 分页查询
     * @return 付款单分页
     */
    PageResult<ErpFinancePaymentDO> getFinancePaymentPage(ErpFinancePaymentPageReqVO pageReqVO);

    // ==================== 付款单项 ====================

    /**
     * 获得付款单项列表
     *
     * @param paymentId 付款单编号
     * @return 付款单项列表
     */
    List<ErpFinancePaymentItemDO> getFinancePaymentItemListByPaymentId(Long paymentId);

    /**
     * 获得付款单项 List
     *
     * @param paymentIds 付款单编号数组
     * @return 付款单项 List
     */
    List<ErpFinancePaymentItemDO> getFinancePaymentItemListByPaymentIds(Collection<Long> paymentIds);

}
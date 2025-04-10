package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 采购退货 Service 接口
 *
 * @author 芋道源码
 */
public interface SrmPurchaseReturnService {

    /**
     * 创建采购退货
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseReturn(@Valid SrmPurchaseReturnSaveReqVO createReqVO);

    /**
     * 更新采购退货
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseReturn(@Valid SrmPurchaseReturnSaveReqVO updateReqVO);


    /**
     * 更新采购退货的退款金额
     *
     * @param id 编号
     * @param refundPrice 退款金额
     */
    void updatePurchaseReturnRefundPrice(Long id, BigDecimal refundPrice);

    /**
     * 删除采购退货
     *
     * @param ids 编号数组
     */
    void deletePurchaseReturn(List<Long> ids);

    /**
     * 获得采购退货
     *
     * @param id 编号
     * @return 采购退货
     */
    SrmPurchaseReturnDO getPurchaseReturn(Long id);

    /**
     * 校验采购退货，已经审核通过
     *
     * @param id 编号
     * @return 采购退货
     */
    SrmPurchaseReturnDO validatePurchaseReturn(Long id);

    /**
     * 获得采购退货分页
     *
     * @param pageReqVO 分页查询
     * @return 采购退货分页
     */
    PageResult<SrmPurchaseReturnDO> getPurchaseReturnPage(SrmPurchaseReturnPageReqVO pageReqVO);

    // ==================== 采购退货项 ====================

    /**
     * 获得采购退货项列表
     *
     * @param returnId 采购退货编号
     * @return 采购退货项列表
     */
    List<SrmPurchaseReturnItemDO> getPurchaseReturnItemListByReturnId(Long returnId);

    /**
     * 获得采购退货项 List
     *
     * @param returnIds 采购退货编号数组
     * @return 采购退货项 List
     */
    List<SrmPurchaseReturnItemDO> getPurchaseReturnItemListByReturnIds(Collection<Long> returnIds);

    // ==================== 审核|付款|退货？ ====================
    /**
     * 提交审核采购订单
     *
     * @param ids 退货单ids
     */
    void submitAudit(@NotNull Collection<Long> ids);

    /**
     * 审核/反审核采购订单
     *
     * @param req 审核请求体
     */
    void review(SrmPurchaseReturnAuditReqVO req);

    /**
     * 切换退款状态
     */
    void refund(SrmPurchaseReturnAuditReqVO vo);
}
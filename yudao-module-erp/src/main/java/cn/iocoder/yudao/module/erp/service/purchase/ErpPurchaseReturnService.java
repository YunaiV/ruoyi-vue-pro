package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 采购退货 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpPurchaseReturnService {

    /**
     * 创建采购退货
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseReturn(@Valid ErpPurchaseReturnSaveReqVO createReqVO);

    /**
     * 更新采购退货
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseReturn(@Valid ErpPurchaseReturnSaveReqVO updateReqVO);

    /**
     * 更新采购退货的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updatePurchaseReturnStatus(Long id, Integer status);

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
    ErpPurchaseReturnDO getPurchaseReturn(Long id);

    /**
     * 校验采购退货，已经审核通过
     *
     * @param id 编号
     * @return 采购退货
     */
    ErpPurchaseReturnDO validatePurchaseReturn(Long id);

    /**
     * 获得采购退货分页
     *
     * @param pageReqVO 分页查询
     * @return 采购退货分页
     */
    PageResult<ErpPurchaseReturnDO> getPurchaseReturnPage(ErpPurchaseReturnPageReqVO pageReqVO);

    // ==================== 采购退货项 ====================

    /**
     * 获得采购退货项列表
     *
     * @param returnId 采购退货编号
     * @return 采购退货项列表
     */
    List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnId(Long returnId);

    /**
     * 获得采购退货项 List
     *
     * @param returnIds 采购退货编号数组
     * @return 采购退货项 List
     */
    List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnIds(Collection<Long> returnIds);

}
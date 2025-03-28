package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInPayReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 采购入库 Service 接口
 *
 * @author 芋道源码
 */
public interface SrmPurchaseInService {

    /**
     * 创建采购入库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseIn(@Valid SrmPurchaseInSaveReqVO createReqVO);

    /**
     * 更新采购入库
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseIn(@Valid SrmPurchaseInSaveReqVO updateReqVO);

//    /**
//     * 更新采购入库的状态
//     *
//     * @param id 编号
//     * @param status 状态
//     */
//    void updatePurchaseInStatus(Long id, Integer status);

    /**
     * 更新采购入库的付款金额
     *
     * @param id 编号
     * @param paymentPrice 付款金额
     */
    void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice);

    /**
     * 删除采购入库
     *
     * @param ids 编号数组
     */
    void deletePurchaseIn(List<Long> ids);

    /**
     * 获得采购入库
     *
     * @param id 编号
     * @return 采购入库
     */
    SrmPurchaseInDO getPurchaseIn(Long id);

    /**
     * 校验采购入库，已经审核通过
     *
     * @param id 编号
     * @return 采购入库
     */
    SrmPurchaseInDO validatePurchaseIn(Long id);

    /**
     * 获得采购入库分页
     *
     * @param pageReqVO 分页查询
     * @return 采购入库分页
     */
    PageResult<SrmPurchaseInDO> getPurchaseInPage(SrmPurchaseInPageReqVO pageReqVO);

    // ==================== 采购入库项 ====================

    /**
     * 获得采购入库项列表
     *
     * @param inId 采购入库编号
     * @return 采购入库项列表
     */
    List<SrmPurchaseInItemDO> getPurchaseInItemListByInId(Long inId);

    /**
     * 获得采购入库项 List
     *
     * @param inIds 采购入库编号数组
     * @return 采购入库项 List
     */
    List<SrmPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds);


    void submitAudit(Collection<Long > inIds);

    void review(SrmPurchaseInAuditReqVO req);

    /**
     * 切换付款状态
     *
     * @param vo 入库项ids
     */
    void switchPayStatus(SrmPurchaseInPayReqVO vo);
}
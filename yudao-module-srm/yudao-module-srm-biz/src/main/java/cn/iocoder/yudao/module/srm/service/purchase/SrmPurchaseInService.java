package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPayReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInBO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 采购到货 Service 接口
 *
 * @author 芋道源码
 */
public interface SrmPurchaseInService {

    /**
     * 创建采购到货
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseIn(@Valid SrmPurchaseInSaveReqVO createReqVO);

    /**
     * 更新采购到货
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseIn(@Valid SrmPurchaseInSaveReqVO updateReqVO);

    /**
     * 更新采购到货的付款金额
     *
     * @param id           编号
     * @param paymentPrice 付款金额
     */
    void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice);

    /**
     * 删除采购到货
     *
     * @param ids 编号数组
     */
    void deletePurchaseIn(List<Long> ids);

    /**
     * 获得采购到货
     *
     * @param id 编号
     * @return 采购到货
     */
    SrmPurchaseInDO getPurchaseIn(Long id);

    /**
     * 获取采购到货列表
     *
     * @param ids 入库单ids
     * @return 采购到货列表
     */
    List<SrmPurchaseInDO> getPurchaseInList(List<Long> ids);

    /**
     * 校验入库订单
     * @param id 入库单id
     * @return SrmPurchaseInDO
     */
    SrmPurchaseInDO validatePurchaseInExists(Long id);
    /**
     * 获得采购到货分页
     *
     * @param pageReqVO 分页查询
     * @return 采购到货分页
     */
    PageResult<SrmPurchaseInBO> getPurchaseInBOPage(SrmPurchaseInPageReqVO pageReqVO);

    //list
    List<SrmPurchaseInBO> getPurchaseInBOList(List<Long> ids);

    //id
    SrmPurchaseInBO getPurchaseInBOById(Long id);
    // ==================== 采购到货项 ====================

    /**
     * 获得采购到货项列表
     *
     * @param inId 采购到货编号
     * @return 采购到货项列表
     */
    List<SrmPurchaseInItemDO> getPurchaseInItemListByInId(Long inId);

    /**
     * 获得采购到货项 List
     *
     * @param inIds 采购到货编号数组
     * @return 采购到货项 List
     */
    List<SrmPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds);

    /**
     * 检验入库id是否存在
     *
     * @param inIds 入库id
     */
    List<SrmPurchaseInItemDO> validatePurchaseInItemExists(List<Long> inIds);

    /**
     * 提交审核
     *
     * @param inIds 入库单ids
     */
    void submitAudit(Collection<Long> inIds);

    /**
     * 审核|反审核
     *
     * @param req vo
     */
    void review(SrmPurchaseInAuditReqVO req);

    /**
     * 切换付款状态
     *
     * @param vo 入库项ids
     */
    void switchPayStatus(SrmPurchaseInPayReqVO vo);


}
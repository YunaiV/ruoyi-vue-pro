package cn.iocoder.yudao.module.srm.service.purchase.payment.term;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term.SrmPaymentTermDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 付款条款 Service 接口
 *
 * @author wdy
 */
public interface SrmPaymentTermService {

    /**
     * 创建付款条款
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPaymentTerm(@Valid SrmPaymentTermSaveReqVO createReqVO);

    /**
     * 更新付款条款
     *
     * @param updateReqVO 更新信息
     */
    void updatePaymentTerm(@Valid SrmPaymentTermSaveReqVO updateReqVO);

    /**
     * 删除付款条款
     *
     * @param id 编号
     */
    void deletePaymentTerm(Long id);

    /**
     * 获得付款条款
     *
     * @param id 编号
     * @return 付款条款
     */
    SrmPaymentTermDO getPaymentTerm(Long id);

    /**
     * 获得付款条款分页
     *
     * @param pageReqVO 分页查询
     * @return 付款条款分页
     */
    PageResult<SrmPaymentTermDO> getPaymentTermPage(SrmPaymentTermPageReqVO pageReqVO);

    /**
     * 获得付款条款列表
     *
     * @return 付款条款列表
     */
    List<SrmPaymentTermDO> getPaymentTermList();
}
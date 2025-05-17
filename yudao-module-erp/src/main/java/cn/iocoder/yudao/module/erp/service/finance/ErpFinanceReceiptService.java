package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * ERP 收款单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpFinanceReceiptService {

    /**
     * 创建收款单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinanceReceipt(@Valid ErpFinanceReceiptSaveReqVO createReqVO);

    /**
     * 更新收款单
     *
     * @param updateReqVO 更新信息
     */
    void updateFinanceReceipt(@Valid ErpFinanceReceiptSaveReqVO updateReqVO);

    /**
     * 更新收款单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateFinanceReceiptStatus(Long id, Integer status);

    /**
     * 删除收款单
     *
     * @param ids 编号数组
     */
    void deleteFinanceReceipt(List<Long> ids);

    /**
     * 获得收款单
     *
     * @param id 编号
     * @return 收款单
     */
    ErpFinanceReceiptDO getFinanceReceipt(Long id);

    /**
     * 获得收款单分页
     *
     * @param pageReqVO 分页查询
     * @return 收款单分页
     */
    PageResult<ErpFinanceReceiptDO> getFinanceReceiptPage(ErpFinanceReceiptPageReqVO pageReqVO);

    // ==================== 收款单项 ====================

    /**
     * 获得收款单项列表
     *
     * @param receiptId 收款单编号
     * @return 收款单项列表
     */
    List<ErpFinanceReceiptItemDO> getFinanceReceiptItemListByReceiptId(Long receiptId);

    /**
     * 获得收款单项 List
     *
     * @param receiptIds 收款单编号数组
     * @return 收款单项 List
     */
    List<ErpFinanceReceiptItemDO> getFinanceReceiptItemListByReceiptIds(Collection<Long> receiptIds);

}
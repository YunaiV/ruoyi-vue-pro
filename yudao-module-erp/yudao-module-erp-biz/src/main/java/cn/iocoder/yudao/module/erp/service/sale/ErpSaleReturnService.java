package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.returns.ErpSaleReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnItemDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 销售退货 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSaleReturnService {

    /**
     * 创建销售退货
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSaleReturn(@Valid ErpSaleReturnSaveReqVO createReqVO);

    /**
     * 更新销售退货
     *
     * @param updateReqVO 更新信息
     */
    void updateSaleReturn(@Valid ErpSaleReturnSaveReqVO updateReqVO);

    /**
     * 更新销售退货的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateSaleReturnStatus(Long id, Integer status);

    /**
     * 更新销售退货的退款金额
     *
     * @param id 编号
     * @param refundPrice 退款金额
     */
    void updateSaleReturnRefundPrice(Long id, BigDecimal refundPrice);

    /**
     * 删除销售退货
     *
     * @param ids 编号数组
     */
    void deleteSaleReturn(List<Long> ids);

    /**
     * 获得销售退货
     *
     * @param id 编号
     * @return 销售退货
     */
    ErpSaleReturnDO getSaleReturn(Long id);

    /**
     * 校验销售退货，已经审核通过
     *
     * @param id 编号
     * @return 销售退货
     */
    ErpSaleReturnDO validateSaleReturn(Long id);

    /**
     * 获得销售退货分页
     *
     * @param pageReqVO 分页查询
     * @return 销售退货分页
     */
    PageResult<ErpSaleReturnDO> getSaleReturnPage(ErpSaleReturnPageReqVO pageReqVO);

    // ==================== 销售退货项 ====================

    /**
     * 获得销售退货项列表
     *
     * @param returnId 销售退货编号
     * @return 销售退货项列表
     */
    List<ErpSaleReturnItemDO> getSaleReturnItemListByReturnId(Long returnId);

    /**
     * 获得销售退货项 List
     *
     * @param returnIds 销售退货编号数组
     * @return 销售退货项 List
     */
    List<ErpSaleReturnItemDO> getSaleReturnItemListByReturnIds(Collection<Long> returnIds);

}
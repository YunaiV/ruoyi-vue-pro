package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 销售出库 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSaleOutService {

    /**
     * 创建销售出库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSaleOut(@Valid ErpSaleOutSaveReqVO createReqVO);

    /**
     * 更新销售出库
     *
     * @param updateReqVO 更新信息
     */
    void updateSaleOut(@Valid ErpSaleOutSaveReqVO updateReqVO);

    /**
     * 更新销售出库的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateSaleOutStatus(Long id, Integer status);

    /**
     * 更新销售出库的收款金额
     *
     * @param id 编号
     * @param receiptPrice 收款金额
     */
    void updateSaleInReceiptPrice(Long id, BigDecimal receiptPrice);

    /**
     * 删除销售出库
     *
     * @param ids 编号数组
     */
    void deleteSaleOut(List<Long> ids);

    /**
     * 获得销售出库
     *
     * @param id 编号
     * @return 销售出库
     */
    ErpSaleOutDO getSaleOut(Long id);

    /**
     * 校验销售出库，已经审核通过
     *
     * @param id 编号
     * @return 销售出库
     */
    ErpSaleOutDO validateSaleOut(Long id);

    /**
     * 获得销售出库分页
     *
     * @param pageReqVO 分页查询
     * @return 销售出库分页
     */
    PageResult<ErpSaleOutDO> getSaleOutPage(ErpSaleOutPageReqVO pageReqVO);

    // ==================== 销售出库项 ====================

    /**
     * 获得销售出库项列表
     *
     * @param outId 销售出库编号
     * @return 销售出库项列表
     */
    List<ErpSaleOutItemDO> getSaleOutItemListByOutId(Long outId);

    /**
     * 获得销售出库项 List
     *
     * @param outIds 销售出库编号数组
     * @return 销售出库项 List
     */
    List<ErpSaleOutItemDO> getSaleOutItemListByOutIds(Collection<Long> outIds);


}
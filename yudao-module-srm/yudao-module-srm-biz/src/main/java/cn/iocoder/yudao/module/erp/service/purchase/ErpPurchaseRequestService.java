package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestSaveReqVO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * ERP采购申请单 Service 接口
 *
 * @author 索迈管理员
 */
public interface ErpPurchaseRequestService {

    /**
     * 创建ERP采购申请单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseRequest(@Valid ErpPurchaseRequestSaveReqVO createReqVO);

    /**
     * 更新ERP采购申请单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseRequest(@Valid ErpPurchaseRequestSaveReqVO updateReqVO);

    /**
     * 删除ERP采购申请单
     *
     * @param ids 编号数组
     */
    void deletePurchaseRequest(List<Long> ids);

    /**
     * 获得ERP采购申请单
     *
     * @param id 编号
     * @return ERP采购申请单
     */
    ErpPurchaseRequestDO getPurchaseRequest(Long id);

    /**
     * 获得ERP采购申请单分页
     *
     * @param pageReqVO 分页查询
     * @return ERP采购申请单分页
     */
    PageResult<ErpPurchaseRequestDO> getPurchaseRequestPage(ErpPurchaseRequestPageReqVO pageReqVO);

    /**
     * 获得采购申请单单项列表
     *
     * @param requestId 采购申请单id
     * @return 采购申请单项列表
     */
    List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderId(Long requestId);

    /**
     * 更新采购申请单状态
     *
     * @param id 采购申请单id
     * @param status 状态
     */
    void updatePurchaseRequestStatus(Long id, Integer status);

    /**
     * 获得采购订单项 List
     *
     * @param requestIds 采购订单编号数组
     * @return 采购订单项 List
     */
    List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderIds(Collection<Long> requestIds);
}
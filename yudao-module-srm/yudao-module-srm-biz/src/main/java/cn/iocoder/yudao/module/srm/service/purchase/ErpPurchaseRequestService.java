package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
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
     * @param ids 主表ids
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

//    /**
//     * 更新采购申请单状态
//     * 该方法用于更新指定采购申请单的状态信息，包括审核状态、订单状态和下架状态。
//     * @param id 采购申请单的唯一标识符
//     * @param auditStatus 审核状态，用于表示采购申请单的审核情况
//     * @param orderStatus 订单状态，用于表示采购申请单的订单处理情况
//     * @param offStatus 下架状态，用于表示采购申请单是否被下架
//     */
//    void updatePurchaseRequestStatus(Long id, Integer auditStatus, Integer orderStatus, Integer offStatus);

//    /**
//     * 更新采购申请单子项状态(审核状态+关闭状态)
//     *
//     * @param itemIds     子表id
//     * @param orderStatus 采购状态
//     * @param offStatus   关闭状态
//     */
//    void updateItemStatus(List<Long> itemIds, Integer orderStatus, Integer offStatus);

    /**
     * 获得采购订单项 List
     *
     * @param requestIds 采购订单编号数组
     * @return 采购订单项 List
     */
    List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderIds(Collection<Long> requestIds);

    /**
     * 审核/反审核采购订单
     *
     * @param req 审核采购订单的请求对象，包含了采购订单的ID和审核状态等信息。
     */
    void reviewPurchaseOrder(ErpPurchaseRequestAuditReqVO req);

    /**
     * 提交审核
     *
     * @param ids 采购订单id
     */
    void submitAudit(Collection<Long> ids);

    /**
     * 启用/关闭申请单子项，自动更新父订单状态
     *
     * @param requestId 采购订单id
     * @param itemIds   采购订单子项id集合
     * @param enable    开启/关闭
     */
    void switchPurchaseOrderStatus(Long requestId, List<Long> itemIds, Boolean enable);

    /**
     * 校验采购订单是否存在
     *
     * @param id id
     * @return 采购订单
     */
    ErpPurchaseRequestDO validateIdExists(Long id);

    /**
     * 校验采购订单的子项目是否合法
     *
     * @param items 采购订单子项目集合
     *              1、校验产品有效性 2、校验仓库有效性
     * @Return 采购订单子项目
     */
    List<ErpPurchaseRequestItemsDO> validatePurchaseRequestItems(List<ErpPurchaseRequestItemsSaveReqVO> items);

    /**
     * 校验采购订单的子项表id否关联主表
     *
     * @param masterId 主表id-申请单
     * @param itemIds  itemIds 子表id集合-申请项
     * @Return void
     */
    void validatePurchaseRequestItemsMasterId(Long masterId, List<Long> itemIds);

    /**
     * 合并采购申请单子项
     *
     * @param reqVO 采购申请单子项
     * @Return 订单id
     */
    Long merge(ErpPurchaseRequestMergeReqVO reqVO);
}
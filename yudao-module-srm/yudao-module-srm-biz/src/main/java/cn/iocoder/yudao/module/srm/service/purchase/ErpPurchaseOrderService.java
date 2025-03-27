package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.ErpPurchaseOrderItemBO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP 采购订单 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpPurchaseOrderService {

    /**
     * 创建采购订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseOrder(@Valid ErpPurchaseOrderSaveReqVO createReqVO);

    /**
     * 更新采购订单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseOrder(@Valid ErpPurchaseOrderSaveReqVO updateReqVO);

    //    /**
    //     * 更新采购订单的状态
    //     *
    //     * @param id 编号
    //     * @param status 状态
    //     */
    //    void updatePurchaseOrderStatus(Long id, Integer status);

    /**
     * 更新采购订单的入库数量
     *
     * @param itemId     编号
     * @param inCountMap 入库数量 Map：key 采购订单项编号；value 入库数量
     */
    void updatePurchaseOrderInCount(Long itemId, Map<Long, BigDecimal> inCountMap);

    /**
     * 更新采购订单的退货数量
     *
     * @param orderId        编号
     * @param returnCountMap 退货数量 Map：key 采购订单项编号；value 退货数量
     */
    void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 删除采购订单
     *
     * @param ids 编号数组
     */
    void deletePurchaseOrder(List<Long> ids);

    /**
     * 获得采购订单
     *
     * @param id 编号
     * @return 采购订单
     */
    ErpPurchaseOrderDO getPurchaseOrder(Long id);

    /**
     * 校验采购订单，已经审核通过
     *
     * @param id 编号
     * @return 采购订单
     */
    ErpPurchaseOrderDO validatePurchaseOrder(Long id);

    /**
     * 获得采购订单分页
     *
     * @param pageReqVO 分页查询
     * @return 采购订单分页
     */
    PageResult<ErpPurchaseOrderDO> getPurchaseOrderPage(ErpPurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购订单分页BO，一个订单项+对应订单
     */
    PageResult<ErpPurchaseOrderItemBO> getPurchaseOrderPageBO(ErpPurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购订单BO集合，一个订单项+对应订单
     */
    List<ErpPurchaseOrderItemBO> getPurchaseOrderBOList(ErpPurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购订单BO，一个订单项+对应订单
     */
    ErpPurchaseOrderItemBO getPurchaseOrderBO(Long id);

    /**
     * 根据订单id获得订单map
     */
    default Map<Long, ErpPurchaseOrderDO> getPurchaseOrderMap(Collection<Long> orderIds) {
        return this.getPurchaseOrderList(orderIds).stream()
            .collect(Collectors.toMap(ErpPurchaseOrderDO::getId, v -> v));
    }

    /**
     * 根据id 获得订单集合
     *
     * @param orderIds 订单ids
     * @return 订单集合
     */
    Collection<ErpPurchaseOrderDO> getPurchaseOrderList(Collection<Long> orderIds);

    //根据订单项id获得订单
    ErpPurchaseOrderDO getPurchaseOrderByItemId(Long itemId);

    // ==================== 采购订单项 ====================

    /**
     * 根据订单项id获得订单map
     */
    default Map<Long, ErpPurchaseOrderDO> getPurchaseOrderItemMap(Collection<Long> itemIds) {
        //TODO 可以优化批量
        return this.getPurchaseOrderItemList(itemIds).stream()
            .collect(Collectors.toMap(ErpPurchaseOrderItemDO::getId, v -> getPurchaseOrderByItemId(v.getId())));
    }

    List<ErpPurchaseOrderItemDO> getPurchaseOrderItemList(Collection<Long> itemIds);

    /**
     * 校验采购订单项是否存在
     *
     * @param id 订单项id
     */
    ErpPurchaseOrderItemDO validatePurchaseOrderItemExists(Long id);

    /**
     * 获得采购订单项列表
     *
     * @param orderId 采购订单编号
     * @return 采购订单项列表
     */
    List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId);

    /**
     * 获得采购订单项 List
     *
     * @param orderIds 采购订单编号数组
     * @return 采购订单项 List
     */
    List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds);

    /**
     * 提交审核采购订单
     *
     * @param orderIds 订单ids
     */
    void submitAudit(@NotNull Collection<Long> orderIds);

    /**
     * 审核/反审核采购订单
     *
     * @param req 审核请求体
     */
    void reviewPurchaseOrder(ErpPurchaseOrderAuditReqVO req);

    /**
     * 开关
     *
     * @param itemIds 采购订单项编号数组
     * @param open    是否开启/关闭
     */
    void switchPurchaseOrderStatus(Collection<Long> itemIds, Boolean open);

    /**
     * 合并入库
     *
     * @param reqVO 请求体
     */
    void merge(ErpPurchaseOrderMergeReqVO reqVO);

    /**
     * 生成采购合同
     *
     * @param reqVO vo
     */
    void generateContract(ErpPurchaseOrderGenerateContractReqVO reqVO, HttpServletResponse response);

    /**
     * 查询采购合同模板
     *
     * @return 模板名称集合
     */
    List<String> getTemplateList();
}
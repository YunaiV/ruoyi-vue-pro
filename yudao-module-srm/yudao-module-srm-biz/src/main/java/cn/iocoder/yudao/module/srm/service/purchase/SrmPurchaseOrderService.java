package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderBO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ERP 采购订单 Service 接口
 *
 * @author 芋道源码
 */
public interface SrmPurchaseOrderService {

    /**
     * 创建采购订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseOrder(@Valid SrmPurchaseOrderSaveReqVO createReqVO);

    /**
     * 更新采购订单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseOrder(@Valid SrmPurchaseOrderSaveReqVO updateReqVO);

    /**
     * 更新item订单项的json属性，完工单+验货单,(审核后)
     */
    void updatePurchaseOrderJson(@Valid SrmPurchaseOrderSaveJsonReqVO reqVO);

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
     * @param returnCountMap 退货数量 Map：key 采购订单项编号；value 退货数量(最终结果)
     */
    void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap);

    /**
     * 删除采购订单
     *
     * @param ids 主表Ids
     */
    void deletePurchaseOrder(List<Long> ids);

    /**
     * 获得采购订单
     *
     * @param id 编号
     * @return 采购订单
     */
    SrmPurchaseOrderDO getPurchaseOrder(Long id);

    /**
     * 校验采购订单，已经审核通过
     *
     * @param id 编号
     * @return 采购订单
     */
    SrmPurchaseOrderDO validatePurchaseOrder(Long id);

    /**
     * 获得采购订单分页BO，一个订单项+对应订单
     */
    PageResult<SrmPurchaseOrderBO> getPurchaseOrderPageBO(SrmPurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购订单BO集合，一个订单项+对应订单
     */
    List<SrmPurchaseOrderBO> getPurchaseOrderBOList(SrmPurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购订单BO，一个订单项+对应订单
     */
    SrmPurchaseOrderBO getPurchaseOrderBO(Long id);

    /**
     * 根据订单id获得订单map
     */
    default Map<Long, SrmPurchaseOrderDO> getPurchaseOrderMap(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<SrmPurchaseOrderDO> orderList = this.getPurchaseOrderList(orderIds);
        if (orderList == null || orderList.isEmpty()) {
            return Collections.emptyMap();
        }
        return orderList.stream()
                .filter(Objects::nonNull) // 防止列表里有null
                .collect(Collectors.toMap(SrmPurchaseOrderDO::getId, Function.identity(), (a, b) -> a));//合并函数防止冲突key
    }


    /**
     * 根据id 获得订单集合
     *
     * @param orderIds 订单ids
     * @return 订单集合
     */
    Collection<SrmPurchaseOrderDO> getPurchaseOrderList(Collection<Long> orderIds);

    //根据订单项id获得订单
    SrmPurchaseOrderDO getPurchaseOrderByItemId(Long itemId);

    // ==================== 采购订单项 ====================

    /**
     * 根据订单项id获得订单map
     */
    default Map<Long, SrmPurchaseOrderDO> getPurchaseOrderItemMap(Collection<Long> itemIds) {
        return this.getPurchaseOrderItemList(itemIds).stream()
            .collect(Collectors.toMap(SrmPurchaseOrderItemDO::getId, v -> getPurchaseOrderByItemId(v.getId())));
    }

    List<SrmPurchaseOrderItemDO> getPurchaseOrderItemList(Collection<Long> itemIds);

    /**
     * 校验采购订单项是否存在
     *
     * @param id 订单项id
     */
    SrmPurchaseOrderItemDO validatePurchaseOrderItemExists(Long id);

    /**
     * 校验采购订单项是否存在
     *
     * @param ids 订单项id集合
     * @return 订单项集合
     */
    List<SrmPurchaseOrderItemDO> validatePurchaseOrderItemExists(@NotNull Collection<Long> ids);

    /**
     * 获得采购订单项列表
     *
     * @param orderId 采购订单编号
     * @return 采购订单项列表
     */
    List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId);

    /**
     * 获得订单项列表-根据采购申请项Id集合
     */
    List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByApplyIds(Collection<Long> applyIds);

    /**
     * 获得采购订单项 List
     *
     * @param orderIds 采购订单编号数组
     * @return 采购订单项 List
     */
    List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds);

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
    void reviewPurchaseOrder(SrmPurchaseOrderAuditReqVO req);

    /**
     * 开关
     *
     * @param itemIds 采购订单项编号数组
     * @param open    是否开启/关闭
     */
    void switchPurchaseOrderStatus(List<Long> itemIds, Boolean open);

    /**
     * 合并入库
     *
     * @param reqVO 请求体
     */
    void merge(SrmPurchaseOrderMergeReqVO reqVO);

    /**
     * 生成采购合同
     *
     * @param reqVO vo
     */
    void generateContract(SrmPurchaseOrderGenerateContractReqVO reqVO, HttpServletResponse response);

    /**
     * 查询采购合同模板
     *
     * @return 模板名称集合
     */
    List<String> getTemplateList();

    /**
     * 获得最新可用的采购订单编号
     *
     * @return 最新的采购订单编号
     */
    String getMaxSerialNumber();
}
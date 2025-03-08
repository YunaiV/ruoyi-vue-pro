package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 采购订单 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class ErpPurchaseOrderServiceImpl implements ErpPurchaseOrderService {

    @Resource(name = ErpStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderDO> offMachine;
    @Resource(name = ErpStateMachines.PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseOrderDO> auditMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_ITEM_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> requestItemMachine;

    @Resource
    private ErpPurchaseOrderMapper purchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderItemMapper purchaseOrderItemMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpPurchaseRequestItemsMapper requestItemsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(ErpPurchaseOrderSaveReqVO createReqVO) {
        // 校验编号
        validatePurchaseOrderExists(createReqVO.getNo());
        // 1.1 校验订单项的有效性
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_ORDER_NO_PREFIX, PURCHASE_ORDER_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(no) != null, PURCHASE_ORDER_NO_EXISTS);
        // 2.1 插入订单
        ErpPurchaseOrderDO purchaseOrder = BeanUtils.toBean(createReqVO, ErpPurchaseOrderDO.class, in -> in.setNo(no));
        calculateTotalPrice(purchaseOrder, purchaseOrderItems);
        // 2.1.1 插入单据日期+结算日期
        purchaseOrder.setNoTime(LocalDateTime.now());
        purchaseOrder.setSettlementDate(createReqVO.getSettlementDate() == null ? LocalDateTime.now() : createReqVO.getSettlementDate());

        ThrowUtil.ifSqlThrow(purchaseOrderMapper.insert(purchaseOrder), GlobalErrorCodeConstants.DB_INSERT_ERROR);
        // 2.2 插入订单项
        purchaseOrderItems.forEach(o -> o.setOrderId(purchaseOrder.getId()));
        purchaseOrderItemMapper.insertBatch(purchaseOrderItems);
        //3.0 设置初始化状态
        //主表
        //开关
        offMachine.fireEvent(ErpOffStatus.OPEN, ErpEventEnum.OFF_INIT, purchaseOrder);
        //审核
        auditMachine.fireEvent(ErpAuditStatus.DRAFT, ErpEventEnum.AUDIT_INIT, purchaseOrder);
        //入库
        //执行
        //付款
//        orderMachine.fireEvent(ErpOrderStatus.OT_ORDERED, ErpEventEnum.ORDER_INIT, purchaseRequest);
        //子表
        //开关
        //付款
        //入库
        //执行

        //联动采购申请项的库存
        for (ErpPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(itemId -> {
                ErpPurchaseRequestItemsDO itemsDO = requestItemsMapper.selectById(itemId);
                //采购调整,设置下单数量进入DO，申请单去增加
//                int oldCount = itemsDO.getOrderedQuantity() == null ? 0 : itemsDO.getOrderedQuantity();
                itemsDO.setOrderedQuantity(orderItemDO.getCount().toBigInteger().negate().intValue());
                requestItemMachine.fireEvent(ErpOrderStatus.OT_ORDERED, ErpEventEnum.ORDER_ADJUSTMENT, itemsDO);
            });
        }
        return purchaseOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(ErpPurchaseOrderSaveReqVO updateReqVO) {
        validatePurchaseOrderExists(updateReqVO.getNo());
        // 1.1 校验存在,校验不处于已审批+TODO 已关闭+手动关闭
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVED.getCode().equals(purchaseOrder.getStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getNo());
        }
        // 1.2 校验供应商
        supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验订单项的有效性
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(updateReqVO.getItems());

        // 2.1 更新订单
        ErpPurchaseOrderDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseOrderDO.class);
        calculateTotalPrice(updateObj, purchaseOrderItems);//计算item合计。
        purchaseOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseOrderItemList(updateReqVO.getId(), purchaseOrderItems);
    }

    //计算采购订单的总价、税费、折扣价格
    private void calculateTotalPrice(ErpPurchaseOrderDO purchaseOrder, List<ErpPurchaseOrderItemDO> purchaseOrderItems) {
        purchaseOrder.setTotalCount(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getCount, BigDecimal::add));
        purchaseOrder.setTotalProductPrice(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalTaxPrice(getSumValue(purchaseOrderItems, ErpPurchaseOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalPrice(purchaseOrder.getTotalProductPrice().add(purchaseOrder.getTotalTaxPrice()));
        // 计算优惠价格
        if (purchaseOrder.getDiscountPercent() == null) {
            purchaseOrder.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseOrder.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseOrder.getTotalPrice(), purchaseOrder.getDiscountPercent()));
        purchaseOrder.setTotalPrice(purchaseOrder.getTotalPrice().subtract(purchaseOrder.getDiscountPrice()));
    }

    //检查订单No的编号唯一
    private void validatePurchaseOrderExists(String No) {
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderMapper.selectByNo(No);
        if (purchaseOrder != null) {
            throw exception(PURCHASE_ORDER_CODE_DUPLICATE, No);
        }
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updatePurchaseOrderStatus(Long id, Integer status) {
//        boolean approve = ErpAuditStatus.APPROVED.getCode().equals(status);
//        // 1.1 校验存在
//        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
//        // 1.2 校验状态
//        if (purchaseOrder.getStatus().equals(status)) {
//            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
//        }
//        // 1.3 存在采购入单，无法反审核
//        if (!approve && purchaseOrder.getTotalInCount().compareTo(BigDecimal.ZERO) > 0) {
//            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN);
//        }
//        // 1.4 存在采购退货单，无法反审核
//        if (!approve && purchaseOrder.getTotalReturnCount().compareTo(BigDecimal.ZERO) > 0) {
//            throw exception(PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN);
//        }
//        // 2. 更新状态
//        int updateCount = purchaseOrderMapper.updateByIdAndStatus(id, purchaseOrder.getStatus(), new ErpPurchaseOrderDO().setStatus(status).setAuditorId(SecurityFrameworkUtils.getLoginUserId()).setAuditTime(LocalDateTime.now()));//2.1 设置审核人的id+时间
//        if (updateCount == 0) {
//            throw exception(approve ? PURCHASE_ORDER_APPROVE_FAIL : PURCHASE_ORDER_PROCESS_FAIL);
//        }
//    }

    private List<ErpPurchaseOrderItemDO> validatePurchaseOrderItems(List<ErpPurchaseOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(convertSet(list, ErpPurchaseOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpPurchaseOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseOrderItemDO.class, item -> {
            item.setProductUnitId(productMap.get(item.getProductId()).getUnitId());
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void updatePurchaseOrderItemList(Long id, List<ErpPurchaseOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseOrderItemDO> oldList = purchaseOrderItemMapper.selectListByOrderId(id);
        List<List<ErpPurchaseOrderItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id));
            for (ErpPurchaseOrderItemDO orderItemDO : diffList.get(0)) {
                //新增如果有申请单项则发事件
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(purchaseApplyItemId);
                    requestItemsDO.setOrderedQuantity(orderItemDO.getCount().toBigInteger().negate().intValue());
                    requestItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), ErpEventEnum.ORDER_ADJUSTMENT, requestItemsDO);
                });
            }
            purchaseOrderItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            for (ErpPurchaseOrderItemDO orderItemDO : diffList.get(1)) {
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(purchaseApplyItemId);
                    ErpPurchaseOrderItemDO oldOrderItem = purchaseOrderItemMapper.selectById(orderItemDO.getId());
//                    int newCount = requestItemsDO.getOrderedQuantity();
                    int newCount = orderItemDO.getCount().intValue();
                    int oldCount = oldOrderItem.getCount().intValue();
                    requestItemsDO.setOrderedQuantity(newCount - oldCount);
                    requestItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), ErpEventEnum.ORDER_ADJUSTMENT, requestItemsDO);
                });
            }
            //跟旧数据对比，申请数量差异，则发采购事件调整
            purchaseOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            //减少申请项的采购数量
            for (ErpPurchaseOrderItemDO orderItemDO : diffList.get(0)) {
                //联动申请项
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(purchaseApplyItemId);
                    requestItemsDO.setOrderedQuantity(orderItemDO.getCount().toBigInteger().intValue());
                    requestItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), ErpEventEnum.ORDER_ADJUSTMENT, requestItemsDO);
                });
            }
            purchaseOrderItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpPurchaseOrderItemDO::getId));
        }
    }

    @Override
    public void updatePurchaseOrderInCount(Long id, Map<Long, BigDecimal> inCountMap) {
        List<ErpPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(id);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInCount().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED, productService.getProduct(item.getProductId()).getName(), item.getCount());
            }
            purchaseOrderItemMapper.updateById(new ErpPurchaseOrderItemDO().setId(item.getId()).setInCount(inCount));
        });
        // 2. 更新采购订单
        BigDecimal totalInCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new ErpPurchaseOrderDO().setId(id).setTotalInCount(totalInCount));
    }

    @Override
    public void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<ErpPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getInCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED, productService.getProduct(item.getProductId()).getName(), item.getInCount());
            }
            purchaseOrderItemMapper.updateById(new ErpPurchaseOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 2. 更新采购订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new ErpPurchaseOrderDO().setId(orderId).setTotalReturnCount(totalReturnCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseOrderDO> purchaseOrders = purchaseOrderMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(purchaseOrders)) {
            return;
        }
        purchaseOrders.forEach(purchaseOrder -> {
            if (ErpAuditStatus.APPROVED.getCode().equals(purchaseOrder.getStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, purchaseOrder.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        purchaseOrders.forEach(purchaseOrder -> {
            //联动申请项
            for (ErpPurchaseOrderItemDO orderItemDO : purchaseOrderItemMapper.selectListByOrderId(purchaseOrder.getId())) {
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(id -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(id);
                    requestItemsDO.setOrderedQuantity(orderItemDO.getCount().intValue());
                    //
                    requestItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), ErpEventEnum.ORDER_ADJUSTMENT, requestItemsDO);
                });
            }
            // 2.1 删除订单
            purchaseOrderMapper.deleteById(purchaseOrder.getId());
            // 2.2 删除订单项
            purchaseOrderItemMapper.deleteByOrderId(purchaseOrder.getId());
        });
    }

    private ErpPurchaseOrderDO validatePurchaseOrderExists(Long id) {
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        return purchaseOrder;
    }

    @Override
    public ErpPurchaseOrderDO getPurchaseOrder(Long id) {
        return purchaseOrderMapper.selectById(id);
    }

    @Override
    public ErpPurchaseOrderDO validatePurchaseOrder(Long id) {
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
        if (ObjectUtil.notEqual(purchaseOrder.getStatus(), ErpAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_ORDER_NOT_APPROVE);
        }
        return purchaseOrder;
    }

    @Override
    public PageResult<ErpPurchaseOrderDO> getPurchaseOrderPage(ErpPurchaseOrderPageReqVO pageReqVO) {
        return purchaseOrderMapper.selectPage(pageReqVO);
    }

    // ==================== 订单项 ====================

    @Override
    public List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId) {
        return purchaseOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return purchaseOrderItemMapper.selectListByOrderIds(orderIds);
    }

    @Override
    public void submitAudit(Collection<Long> itemIds) {
        //TODO 提交审核
    }

    @Override
    public void reviewPurchaseOrder(ErpPurchaseOrderAuditReqVO req) {
        //TODO 审核/反审核
    }

    @Override
    public void switchPurchaseOrderStatus(Collection<Long> itemIds, Boolean open) {
        //TODO 开/关
        ErpEventEnum event = Boolean.TRUE.equals(open) ? ErpEventEnum.ACTIVATE : ErpEventEnum.MANUAL_CLOSE;
        if (itemIds != null && !itemIds.isEmpty()) {
            // 批量处理采购订单子项状态
            List<ErpPurchaseOrderItemDO> orderItemDOS = purchaseOrderItemMapper.selectBatchIds(itemIds);
            if (orderItemDOS != null && !orderItemDOS.isEmpty()) {
//                orderItemDOS.forEach(orderItemDO ->
//                    offItemMachine.fireEvent(ErpOffStatus.fromCode(orderItemDO.getOffStatus()), event, orderItemDO)
//                );
            }
        }
    }
}
package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.finance.ErpAccountApi;
import cn.iocoder.yudao.module.erp.api.finance.ErpFinanceSubjectApi;
import cn.iocoder.yudao.module.erp.api.finance.dto.ErpFinanceSubjectDTO;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.erp.api.purchase.SrmPayCountDTO;
import cn.iocoder.yudao.module.erp.api.purchase.TmsOrderCountDTO;
import cn.iocoder.yudao.module.erp.config.purchase.PurchaseOrderTemplateManager;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.*;
import cn.iocoder.yudao.module.erp.convert.purchase.ErpOrderConvert;
import cn.iocoder.yudao.module.erp.convert.purchase.ErpOrderInConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.*;
import cn.iocoder.yudao.module.erp.service.purchase.bo.ErpPurchaseOrderWordBO;
import com.alibaba.cola.statemachine.StateMachine;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.SrmStateMachines.*;

/**
 * ERP 采购订单 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpPurchaseOrderServiceImpl implements ErpPurchaseOrderService {

    private final ErpPurchaseOrderMapper purchaseOrderMapper;
    private final ErpPurchaseOrderItemMapper purchaseOrderItemMapper;
    private final ErpPurchaseRequestItemsMapper requestItemsMapper;
    private final ErpPurchaseInItemMapper erpPurchaseInItemMapper;
    private final SrmNoRedisDAO noRedisDAO;
    private final ErpSupplierService supplierService;
    private final ErpPurchaseInService purchaseInService;
    @Resource
    private final ErpAccountApi erpAccountApi;
    private final ErpProductApi erpProductApi;
    private final ErpProductUnitApi erpProductUnitApi;
    @Resource
    private final ErpFinanceSubjectApi erpFinanceSubjectApi;
    private final ResourcePatternResolver resourcePatternResolver;
    private final PurchaseOrderTemplateManager purchaseOrderTemplateManager;

    @Resource(name = PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> offMachine;
    @Resource(name = PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseOrderAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> orderItemMachine;
    //子项开关状态机
    @Resource(name = PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderItemDO> orderItemOffMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> requestItemStorageMachine;
    @Resource(name = PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    StateMachine<ErpStorageStatus, SrmEventEnum, ErpPurchaseOrderDO> purchaseOrderStorageMachine;
    //    @Resource(name = PURCHASE_ORDER_ITEM_PURCHASE_STATE_MACHINE_NAME)
    //    StateMachine<ErpOrderStatus, SrmEventEnum, ErpPurchaseOrderItemDO> requestItemPurchaseMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<ErpPaymentStatus, SrmEventEnum, SrmPayCountDTO> requestItemPaymentMachine;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> purchaseOrderPaymentMachine;
    @Resource(name = PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderDO> purchaseOrderExecutionMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderItemDO> requestItemExecutionMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestItemsDO> requestItemOffMachine;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(ErpPurchaseOrderSaveReqVO createReqVO) {
        // 校验编号
        //        validatePurchaseOrderExists(createReqVO.getNo());
        // 1.1 校验订单项的有效性
        List<ErpPurchaseOrderItemDO> orderItems = validatePurchaseOrderItems(createReqVO.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(createReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            erpAccountApi.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 生成订单号，并校验唯一性
        String no = noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_ORDER_NO_PREFIX, PURCHASE_ORDER_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(no) != null, PURCHASE_ORDER_NO_EXISTS);
        // 2.1 插入订单
        ErpPurchaseOrderDO orderDO = BeanUtils.toBean(createReqVO, ErpPurchaseOrderDO.class, in -> in.setNo(no));
        calculateTotalPrice(orderDO, orderItems);
        // 2.1.1 插入单据日期+结算日期
        orderDO.setNoTime(LocalDateTime.now());
        orderDO.setSettlementDate(createReqVO.getSettlementDate() == null ? LocalDateTime.now() : createReqVO.getSettlementDate());
        orderDO.setOrderTime(LocalDateTime.now());
        ThrowUtil.ifSqlThrow(purchaseOrderMapper.insert(orderDO), GlobalErrorCodeConstants.DB_INSERT_ERROR);
        // 2.2 插入订单项
        orderItems.forEach(o -> o.setOrderId(orderDO.getId()).setSource("WEB录入"));
        purchaseOrderItemMapper.insertBatch(orderItems);
        orderItems = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        //3.0 设置初始化状态
        initMasterState(orderDO);
        initSlaveStatus(orderItems);
        return orderDO.getId();
    }

    /**
     * 初始化状态
     */
    private void initSlaveStatus(List<ErpPurchaseOrderItemDO> purchaseOrderItems) {
        //子表
        //查询订单下面的产品项
        for (ErpPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            //开关
            orderItemOffMachine.fireEvent(ErpOffStatus.OPEN, SrmEventEnum.OFF_INIT, orderItemDO);
            //付款
            requestItemPaymentMachine.fireEvent(ErpPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, SrmPayCountDTO.builder().orderItemId(orderItemDO.getId()).build());
            //入库
            requestItemStorageMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmInCountDTO.builder().orderItemId(orderItemDO.getId()).inCount(orderItemDO.getCount()).build());
            //执行
            requestItemExecutionMachine.fireEvent(ErpExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderItemDO);
        }
        //联动采购申请项的库存
        for (ErpPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(itemId -> {
                ErpPurchaseRequestItemsDO itemsDO = requestItemsMapper.selectById(itemId);
                //采购调整,设置下单数量进入DO，申请单去增加
                //                int oldCount = itemsDO.getOrderedQuantity() == null ? 0 : itemsDO.getOrderedQuantity();
                TmsOrderCountDTO dto = TmsOrderCountDTO.builder().purchaseOrderItemId(itemsDO.getId()).quantity(orderItemDO.getCount().intValue()).build();
                orderItemMachine.fireEvent(ErpOrderStatus.fromCode(itemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
            });
        }
    }

    private void initMasterState(ErpPurchaseOrderDO orderDO) {
        //主表
        //开关
        offMachine.fireEvent(ErpOffStatus.OPEN, SrmEventEnum.OFF_INIT, orderDO);
        //审核
        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, ErpPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build());
        //入库
        purchaseOrderStorageMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, orderDO);
        //执行
        purchaseOrderExecutionMachine.fireEvent(ErpExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderDO);
        //付款
        purchaseOrderPaymentMachine.fireEvent(ErpPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, orderDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(ErpPurchaseOrderSaveReqVO updateReqVO) {
        //        validatePurchaseOrderExists(updateReqVO.getNo());
        // 1.1 校验存在,校验不处于已审批+TODO 已关闭+手动关闭
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(updateReqVO.getId());
        if (SrmAuditStatus.APPROVED.getCode().equals(purchaseOrder.getAuditStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getNo());
        }
        if (ErpOffStatus.OPEN.getCode().equals(purchaseOrder.getAuditStatus()))
            // 1.2 校验供应商
            supplierService.validateSupplier(updateReqVO.getSupplierId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            erpAccountApi.validateAccount(updateReqVO.getAccountId());
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

    //计算采购订单的总价、税费、折扣价格,|计算总数量|计算总商品价格|计算总税费|计算折扣价格
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

    private List<ErpPurchaseOrderItemDO> validatePurchaseOrderItems(List<ErpPurchaseOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDTO> productList = erpProductApi.validProductList(convertSet(list, ErpPurchaseOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDTO> dtoMap = convertMap(productList, ErpProductDTO::getId);
        // 2. 转化为 ErpPurchaseOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseOrderItemDO.class, item -> {
            //            item = ErpOrderConvert.INSTANCE.convertToErpPurchaseOrderItemDO(o);//convert转换一下
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
            //根据产品来设置产品单位
            item.setProductUnitId(dtoMap.get(item.getProductId()).getUnitId());
            //产品名称
            item.setProductName(dtoMap.get(item.getProductId()).getName());
            item.setBarCode(dtoMap.get(item.getProductId()).getBarCode());
            //产品单位名称(产品必有单位)
            item.setProductUnitName(erpProductUnitApi.getProductUnitList(Collections.singleton(dtoMap.get(item.getProductId()).getUnitId())).get(0).getName());
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
            purchaseOrderItemMapper.insertBatch(diffList.get(0));
            //行状态初始化
            initSlaveStatus(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            // 批量查询所有需要的采购订单项（purchase order items）
            Set<Long> orderItemIds = diffList.get(1).stream().map(ErpPurchaseOrderItemDO::getId).collect(Collectors.toSet());
            Map<Long, ErpPurchaseOrderItemDO> orderItemMap = purchaseOrderItemMapper.selectListByItemIds(orderItemIds).stream().collect(Collectors.toMap(ErpPurchaseOrderItemDO::getId, Function.identity()));

            // 批量查询所有需要的采购申请项（purchase request items）
            Set<Long> purchaseApplyItemIds = diffList.get(1).stream().map(ErpPurchaseOrderItemDO::getPurchaseApplyItemId).filter(Objects::nonNull)  // 过滤掉为空的purchaseApplyItemId
                .collect(Collectors.toSet());

            Map<Long, ErpPurchaseRequestItemsDO> requestItemsMap = requestItemsMapper.selectListByIds(purchaseApplyItemIds).stream().collect(Collectors.toMap(ErpPurchaseRequestItemsDO::getId, Function.identity()));
            // 遍历并处理订单项
            for (ErpPurchaseOrderItemDO orderItemDO : diffList.get(1)) {
                ErpPurchaseOrderItemDO oldOrderItem = orderItemMap.get(orderItemDO.getId());  // 从map中获取旧的order item
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMap.get(purchaseApplyItemId);  // 从map中获取request item
                    // 验证:采购的产品数量 <= 申请项的剩余订购数量(批准数量 - 已订购数量)
                    int newCount = orderItemDO.getCount().intValue();
                    int oldCount = oldOrderItem.getCount().intValue();
                    if (newCount != oldCount) {
                        // 数量有改变
                        ThrowUtil.ifThrow((requestItemsDO.getApproveCount() - requestItemsDO.getOrderedQuantity()) < orderItemDO.getCount().intValue(), PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED);
                    }
                    TmsOrderCountDTO dto = TmsOrderCountDTO.builder().purchaseOrderItemId(requestItemsDO.getId()).quantity(newCount - oldCount).build();
                    orderItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                });
            }

            //跟旧数据对比，申请数量差异，则发采购事件调整
            purchaseOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            //减少申请项的采购数量
            // 批量查询所有需要的采购申请项（purchase request items）
            Set<Long> purchaseApplyItemIds = diffList.get(0).stream().map(ErpPurchaseOrderItemDO::getPurchaseApplyItemId).filter(Objects::nonNull)  // 过滤掉为空的purchaseApplyItemId
                .collect(Collectors.toSet());

            Map<Long, ErpPurchaseRequestItemsDO> requestItemsMap = requestItemsMapper.selectListByIds(purchaseApplyItemIds).stream().collect(Collectors.toMap(ErpPurchaseRequestItemsDO::getId, Function.identity()));

            // 遍历并处理订单项
            for (ErpPurchaseOrderItemDO orderItemDO : diffList.get(0)) {
                Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMap.get(purchaseApplyItemId);  // 从map中获取request item
                    if (requestItemsDO != null) {
                        TmsOrderCountDTO dto = TmsOrderCountDTO.builder().purchaseOrderItemId(requestItemsDO.getId()).quantity(orderItemDO.getCount().negate().intValue())  // 负值数量
                            .build();
                        orderItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                    }
                });
            }
            purchaseOrderItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpPurchaseOrderItemDO::getId));
        }
    }

    @Override
    public void updatePurchaseOrderInCount(Long itemId, Map<Long, BigDecimal> inCountMap) {
        List<ErpPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(itemId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInCount().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED, erpProductApi.getProductDto(item.getProductId()).getName(), item.getCount());
            }
            purchaseOrderItemMapper.updateById(new ErpPurchaseOrderItemDO().setId(item.getId()).setInCount(inCount));
        });
        // 2. 更新采购订单
        BigDecimal totalInCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new ErpPurchaseOrderDO().setId(itemId).setTotalInCount(totalInCount));
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
                throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED, erpProductApi.getProductDto(item.getProductId()).getName(), item.getInCount());
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
        List<ErpPurchaseOrderDO> purchaseOrders = purchaseOrderMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseOrders)) {
            return;
        }
        purchaseOrders.forEach(orderDO -> {
            if (SrmAuditStatus.APPROVED.getCode().equals(orderDO.getAuditStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, orderDO.getNo());
            }
            //存在对应的采购入库项->异常
            purchaseOrderItemMapper.selectListByOrderId(orderDO.getId()).forEach(item -> {
                boolean b = erpPurchaseInItemMapper.existsByOrderItemId(item.getId());
                ThrowUtil.ifThrow(b, PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_DEL, item.getId());
            });
        });
        // 2. 遍历删除，并记录操作日志
        purchaseOrders.forEach(purchaseOrder -> {
            List<ErpPurchaseOrderItemDO> dos = purchaseOrderItemMapper.selectListByOrderId(purchaseOrder.getId());
            //校验是否存在入库项
            dos.forEach(ErpPurchaseOrderServiceImpl::validHasInPurchaseItem);
            //联动申请项
            for (ErpPurchaseOrderItemDO item : dos) {
                Optional.ofNullable(item.getPurchaseApplyItemId()).ifPresent(id -> {
                    ErpPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(id);
                    TmsOrderCountDTO dto = TmsOrderCountDTO.builder().purchaseOrderItemId(item.getId()).quantity(item.getCount().negate().intValue()).build();//减少申请个数的订购数量
                    //订购状态
                    orderItemMachine.fireEvent(ErpOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                    //开关状态,不是开启
                    if (!ErpOffStatus.OPEN.getCode().equals(requestItemsDO.getOffStatus())) {
                        requestItemOffMachine.fireEvent(ErpOffStatus.fromCode(requestItemsDO.getOffStatus()), SrmEventEnum.ACTIVATE, requestItemsDO);
                    }
                });
            }

            // 2.1 删除订单
            purchaseOrderMapper.deleteById(purchaseOrder.getId());
            // 2.2 删除订单项
            purchaseOrderItemMapper.deleteByOrderId(purchaseOrder.getId());
        });
    }

    /**
     * 校验是否存在入库项
     *
     * @param item 订单项
     */
    private static void validHasInPurchaseItem(ErpPurchaseOrderItemDO item) {
        //判断订单项是否存在入库项
        if (!Objects.equals(item.getInStatus(), ErpStorageStatus.NONE_IN_STORAGE.getCode())) {
            //存在入库项，则发抛出异常
            throw exception(PURCHASE_ORDER_DELETE_FAIL);
        }
    }

    @Override
    public ErpPurchaseOrderItemDO validatePurchaseOrderItemExists(Long id) {
        //校验采购订单项是否存在
        ErpPurchaseOrderItemDO aDo = purchaseOrderItemMapper.selectById(id);
        if (aDo == null) {
            throw exception(PURCHASE_ORDER_ITEM_NOT_EXISTS, id);
        }
        return aDo;
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
        //只有已审核的才可以
        ErpPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
        if (ObjectUtil.notEqual(purchaseOrder.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_ORDER_NOT_APPROVE, purchaseOrder.getNo());
        }
        return purchaseOrder;
    }

    @Override
    public PageResult<ErpPurchaseOrderDO> getPurchaseOrderPage(ErpPurchaseOrderPageReqVO pageReqVO) {
        return purchaseOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public Collection<ErpPurchaseOrderDO> getPurchaseOrderList(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return purchaseOrderMapper.selectByIds(orderIds);
    }

    @Override
    public ErpPurchaseOrderDO getPurchaseOrderByItemId(Long itemId) {
        AtomicReference<ErpPurchaseOrderDO> orderDO = new AtomicReference<>();
        Optional.ofNullable(purchaseOrderItemMapper.selectById(itemId)).ifPresent(item -> orderDO.set(purchaseOrderMapper.selectById(item.getOrderId())));
        return orderDO.get();
    }
    // ==================== 订单项 ====================

    //}
    @Override
    public List<ErpPurchaseOrderItemDO> getPurchaseOrderItemList(Collection<Long> itemIds) {
        if (CollUtil.isEmpty(itemIds)) return Collections.emptyList();
        return purchaseOrderItemMapper.selectByIds(itemIds);
    }

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
    public void submitAudit(Collection<Long> orderIds) {
        // 提前校验传入的订单ID是否存在
        if (CollUtil.isEmpty(orderIds)) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        // 1. 批量查询订单信息
        List<ErpPurchaseOrderDO> orderDOS = purchaseOrderMapper.selectByIds(orderIds);
        if (CollUtil.isEmpty(orderDOS)) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        // 2. 触发事件
        orderDOS.forEach(orderDO -> {
            auditMachine.fireEvent(SrmAuditStatus.fromCode(orderDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, ErpPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build());
        });
    }

    @Override
    public void reviewPurchaseOrder(ErpPurchaseOrderAuditReqVO req) {
        // 查询采购订单信息
        ErpPurchaseOrderDO orderDO = purchaseOrderMapper.selectById(req.getOrderIds().get(0));

        if (orderDO == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        // 获取当前订单状态
        SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(orderDO.getAuditStatus());
        if (Boolean.TRUE.equals(req.getReviewed())) {
            // 审核操作
            if (req.getPass()) {
                log.debug("采购订单通过审核，ID: {}", orderDO.getId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, req);
            } else {
                log.debug("采购订单拒绝审核，ID: {}", orderDO.getId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, req);
            }
        } else {
            //反审核
            //存在对应的采购入库项->异常
            purchaseOrderItemMapper.selectListByOrderId(orderDO.getId()).forEach(item -> {
                boolean b = erpPurchaseInItemMapper.existsByOrderItemId(item.getId());
                ThrowUtil.ifThrow(b, PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_IN, item.getId());
            });
            log.debug("采购订单撤回审核，ID: {}", orderDO.getId());
            auditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
        }
    }

    @Override
    public void switchPurchaseOrderStatus(Collection<Long> itemIds, Boolean open) {
        SrmEventEnum event = Boolean.TRUE.equals(open) ? SrmEventEnum.ACTIVATE : SrmEventEnum.MANUAL_CLOSE;
        if (itemIds != null && !itemIds.isEmpty()) {
            // 批量处理采购订单子项状态
            List<ErpPurchaseOrderItemDO> orderItemDOS = purchaseOrderItemMapper.selectListByItemIds(itemIds);
            if (orderItemDOS != null && !orderItemDOS.isEmpty()) {
                orderItemDOS.forEach(orderItemDO -> orderItemOffMachine.fireEvent(ErpOffStatus.fromCode(orderItemDO.getOffStatus()), event, orderItemDO));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(ErpPurchaseOrderMergeReqVO reqVO) {

        //校验
        for (ErpPurchaseOrderMergeReqVO.item item : reqVO.getItems()) {
            Long itemId = item.getItemId();
            ErpPurchaseOrderItemDO aDo = validatePurchaseOrderItemExists(itemId);
            ErpPurchaseOrderDO order = getPurchaseOrder(aDo.getOrderId());
            //非已审核+非开启+非完全入库,异常
            ThrowUtil.ifThrow(!Objects.equals(order.getAuditStatus(), SrmAuditStatus.APPROVED.getCode()), PURCHASE_ORDER_ITEM_NOT_AUDIT, itemId);
            ThrowUtil.ifThrow(!Objects.equals(aDo.getOffStatus(), ErpOffStatus.OPEN.getCode()), PURCHASE_ORDER_ITEM_NOT_OPEN, itemId);
            ThrowUtil.ifThrow(!Objects.equals(aDo.getInStatus(), ErpStorageStatus.ALL_IN_STORAGE.getCode()), PURCHASE_ORDER_IN_ITEM_NOT_OPEN, itemId);
        }
        List<Long> itemIds = reqVO.getItems().stream().map(ErpPurchaseOrderMergeReqVO.item::getItemId).collect(Collectors.toList());
        List<ErpPurchaseOrderItemDO> orderItemDOS = purchaseOrderItemMapper.selectListByItemIds(itemIds);
        //转换
        ErpPurchaseInSaveReqVO vo = BeanUtils.toBean(reqVO, ErpPurchaseInSaveReqVO.class, saveReqVO -> {
            saveReqVO
                .setNo(null)
                .setItems(ErpOrderInConvert.INSTANCE.convertToErpPurchaseInSaveReqVOItems(orderItemDOS))
                .setId(null)
                .setInTime(LocalDateTime.now());
        });
        //service持久化
        Long purchaseIn = purchaseInService.createPurchaseIn(vo);
        //修改采购单项的source = 合并入库
        List<ErpPurchaseInItemDO> itemDOS = erpPurchaseInItemMapper.selectListByInId(purchaseIn);
        itemDOS.forEach(itemDO -> itemDO.setSource("合并入库"));
        erpPurchaseInItemMapper.updateBatch(itemDOS);
    }

    @Override
    public void generateContract(ErpPurchaseOrderGenerateContractReqVO reqVO, HttpServletResponse response) {
        ErpPurchaseOrderDO orderDO = validatePurchaseOrderExists(reqVO.getOrderId());
        //1 从OSS拿到模板word
        XWPFTemplate xwpfTemplate = purchaseOrderTemplateManager.getTemplate("purchase/order/%s".formatted(reqVO.getTemplateName()));
        //2 模板word渲染数据
        List<ErpPurchaseOrderItemDO> itemDOS = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        Map<Long, ErpFinanceSubjectDTO> dtoMap = convertMap(erpFinanceSubjectApi.validateFinanceSubject(List.of(reqVO.getPartyAId(), reqVO.getPartyBId())), ErpFinanceSubjectDTO::getId);
        ErpPurchaseOrderWordBO wordBO = ErpOrderConvert.INSTANCE.bindDataFormOrderItemDO(itemDOS, orderDO, reqVO, dtoMap);
        xwpfTemplate.render(wordBO);
        //3 转换pdf，返回响应
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 用于捕获输出流
        try (OutputStream out = response.getOutputStream()) {
            // 将生成的模板写入 ByteArrayOutputStream
            xwpfTemplate.write(byteArrayOutputStream);
            // 将字节数组转成输入流
            InputStream inputStreamResult = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());// 获取字节数组
            // 设置响应头，准备下载
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("采购合同.pdf", StandardCharsets.UTF_8));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 写入响应
            Document document = new Document(inputStreamResult);
            document.save(out, SaveFormat.PDF);
            out.flush();
        } catch (Exception e) {
            throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_ERROR);
        }
    }


    /**
     * 获取 JAR 包中的模板文件列表（只列出以 .docx 结尾的文件）
     *
     * @return 文件名列表
     */
    public List<String> getTemplateList() {
        List<String> templateList = new ArrayList<>();
        try {
            org.springframework.core.io.Resource[] resources = resourcePatternResolver.getResources("classpath:purchase/order/*.docx");
            //获取文件名列表
            for (org.springframework.core.io.Resource resource : resources) {
                Optional.ofNullable(resource.getFilename()).ifPresent(fileName -> {
                    if (fileName.endsWith(".docx")) {
                        templateList.add(fileName);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return templateList;
    }

}
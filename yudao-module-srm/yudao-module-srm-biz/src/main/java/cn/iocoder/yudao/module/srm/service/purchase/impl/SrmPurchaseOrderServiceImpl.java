package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsAccountApi;
import cn.iocoder.yudao.module.fms.api.finance.FmsFinanceSubjectApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsFinanceSubjectDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmOrderCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPayCountDTO;
import cn.iocoder.yudao.module.srm.config.purchase.PurchaseOrderTemplateManager;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.*;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderConvert;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderInConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.SrmPurchaseOrderItemBO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseRequestService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.bo.SrmPurchaseOrderWordBO;
import com.alibaba.cola.statemachine.StateMachine;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import static cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO.PURCHASE_ORDER_NO_PREFIX;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;

/**
 * ERP 采购订单 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseOrderServiceImpl implements SrmPurchaseOrderService {

    private final SrmPurchaseOrderMapper purchaseOrderMapper;
    private final SrmPurchaseOrderItemMapper purchaseOrderItemMapper;
    private final SrmPurchaseRequestItemsMapper requestItemsMapper;
    private final SrmPurchaseInItemMapper srmPurchaseInItemMapper;
    private final SrmNoRedisDAO noRedisDAO;
    private final SrmSupplierService supplierService;
    private final SrmPurchaseInService purchaseInService;
    private final FmsAccountApi erpAccountApi;
    private final ErpProductApi erpProductApi;
    private final ErpProductUnitApi erpProductUnitApi;
    private final FmsFinanceSubjectApi erpFinanceSubjectApi;
    private final ResourcePatternResolver resourcePatternResolver;
    private final PurchaseOrderTemplateManager purchaseOrderTemplateManager;

    @Resource(name = PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> offMachine;
    @Resource(name = PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> purchaseOrderStorageMachine;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> purchaseOrderPaymentMachine;
    @Resource(name = PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> purchaseOrderExecutionMachine;
    //
    @Resource(name = PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    StateMachine<SrmOrderStatus, SrmEventEnum, SrmOrderCountDTO> requestOrderItemMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> requestItemOffMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderItemDO> orderItemOffMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> requestItemStorageMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPayCountDTO> requestItemPaymentMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> requestItemExecutionMachine;

    private final String SOURCE = "WEB录入";
    @Autowired
    @Lazy
    private SrmPurchaseRequestService srmPurchaseRequestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(SrmPurchaseOrderSaveReqVO vo) {
        // 校验编号
        //        validatePurchaseOrderExists(vo.getNo());
        // 1.1 校验订单项的有效性
        List<SrmPurchaseOrderItemDO> orderItems = validatePurchaseOrderItems(vo.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(vo.getSupplierId());
        // 1.3 校验结算账户
        if (vo.getAccountId() != null) {
            erpAccountApi.validateAccount(vo.getAccountId());
        }
        // 1.3.1 校验订单项是否可以被创建
        List<Long> purchaseApplyItemIds = orderItems.stream().map(SrmPurchaseOrderItemDO::getPurchaseApplyItemId).distinct().toList();
        //构造purchaseApplyItemIds:count 的Map
        validPurchaseApplyItemId(purchaseApplyItemIds, orderItems);
        // 1.4 生成订单号，并校验唯一性
        voSetNo(vo);
        // 2.1 插入订单
        SrmPurchaseOrderDO orderDO = BeanUtils.toBean(vo, SrmPurchaseOrderDO.class, in -> in.setNo(vo.getNo()));
        calculateTotalPrice(orderDO, orderItems);
        // 2.1.1 插入单据日期+结算日期
        orderDO.setNoTime(LocalDateTime.now());
        orderDO.setSettlementDate(vo.getSettlementDate() == null ? LocalDateTime.now() : vo.getSettlementDate());
        orderDO.setOrderTime(LocalDateTime.now());
        ThrowUtil.ifSqlThrow(purchaseOrderMapper.insert(orderDO), GlobalErrorCodeConstants.DB_INSERT_ERROR);
        // 2.2 插入订单项
        orderItems.forEach(o -> {
            if (o.getSource() == null) {
                o.setSource(SOURCE);
            }
            o.setOrderId(orderDO.getId());
        });
        purchaseOrderItemMapper.insertBatch(orderItems);
        orderItems = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        //3.0 设置初始化状态
        initMasterState(orderDO);
        initSlaveStatus(orderItems);
        return orderDO.getId();
    }

    /**
     * @param purchaseApplyItemIds 采购申请项ids
     */
    private void validPurchaseApplyItemId(List<Long> purchaseApplyItemIds, List<SrmPurchaseOrderItemDO> orderItems) {
        //Map
        Map<Long, SrmPurchaseOrderItemDO> map = convertMap(orderItems, SrmPurchaseOrderItemDO::getPurchaseApplyItemId);

        List<SrmPurchaseRequestItemsDO> requestItemsDOS = srmPurchaseRequestService.validItemIdsExist(purchaseApplyItemIds);
        for (SrmPurchaseRequestItemsDO requestItemsDO : requestItemsDOS) {
            //剩余可采购数量 > 采购数量 -> e
            int i = requestItemsDO.getApproveCount() - requestItemsDO.getOrderedQuantity();
            if (i < map.get(requestItemsDO.getId()).getCount().intValue()) {
                //数量不足
                throw exception(PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED, requestItemsDO.getId(), i);
            }
        }
    }

    /**
     * 初始化状态
     */
    private void initSlaveStatus(List<SrmPurchaseOrderItemDO> purchaseOrderItems) {
        //子表
        //查询订单下面的产品项
        for (SrmPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            //开关
            orderItemOffMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, orderItemDO);
            //付款
            requestItemPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, SrmPayCountDTO.builder().orderItemId(orderItemDO.getId()).build());
            //入库
            requestItemStorageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmInCountDTO.builder().orderItemId(orderItemDO.getId()).build());
            //            requestItemStorageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmInCountDTO.builder().orderItemId(orderItemDO.getId()).inCount(orderItemDO.getCount()).build());
            //执行
            requestItemExecutionMachine.fireEvent(SrmExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderItemDO);
        }
        //联动采购申请项的库存
        for (SrmPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(itemId -> {
                SrmPurchaseRequestItemsDO itemsDO = srmPurchaseRequestService.validItemIdExist(itemId);
                //下单数量 <-> 申请单已订购数量
                SrmOrderCountDTO dto = SrmOrderCountDTO.builder().purchaseRequestItemId(itemsDO.getId()).quantity(orderItemDO.getCount().intValue()).build();
                requestOrderItemMachine.fireEvent(SrmOrderStatus.fromCode(itemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
            });
        }
    }

    private void initMasterState(SrmPurchaseOrderDO orderDO) {
        //主表
        //开关
        offMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, orderDO);
        //审核
        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, SrmPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build());
        //入库
        purchaseOrderStorageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, orderDO);
        //执行
        purchaseOrderExecutionMachine.fireEvent(SrmExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderDO);
        //付款
        purchaseOrderPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, orderDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(SrmPurchaseOrderSaveReqVO vo) {
        //        validatePurchaseOrderExists(vo.getNo());
        // 1.1 校验存在,校验不处于已审批+TODO 已关闭+手动关闭
        SrmPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(vo.getId());
        if (SrmAuditStatus.APPROVED.getCode().equals(purchaseOrder.getAuditStatus())) {
            throw exception(PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getNo());
        }
        // 1.2 校验供应商
        if (SrmOffStatus.OPEN.getCode().equals(purchaseOrder.getAuditStatus())) {
            supplierService.validateSupplier(vo.getSupplierId());
        }
        // 1.3 校验结算账户
        if (vo.getAccountId() != null) {
            erpAccountApi.validateAccount(vo.getAccountId());
        }
        // 1.3.1 设置no
        String oldNo = purchaseOrder.getNo();
        if (!oldNo.equals(vo.getNo())) {
            voSetNo(vo);
        }
        // 1.4 校验订单项的有效性
        List<SrmPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(vo.getItems());

        // 2.1 更新订单
        SrmPurchaseOrderDO updateObj = BeanUtils.toBean(vo, SrmPurchaseOrderDO.class);
        calculateTotalPrice(updateObj, purchaseOrderItems);//计算item合计。
        purchaseOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseOrderItemList(vo.getId(), purchaseOrderItems);
    }

    private void voSetNo(SrmPurchaseOrderSaveReqVO vo) {
        //生成单据编号
        if (vo.getNo() != null) {
            ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(vo.getNo()) != null, PURCHASE_ORDER_NO_HAS_EXISTS, vo.getNo());
            noRedisDAO.setManualSerial(PURCHASE_ORDER_NO_PREFIX, vo.getNo());
        } else {
            vo.setNo(noRedisDAO.generate(PURCHASE_ORDER_NO_PREFIX, PURCHASE_ORDER_NO_OUT_OF_BOUNDS));
            //1.1 校验编号no是否在数据库中重复
            ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(vo.getNo()) != null, PURCHASE_ORDER_NO_EXISTS, vo.getNo());
        }
    }

    @Override
    public void updatePurchaseOrderItemList(List<SrmPurchaseOrderItemDO> itemsDOList) {
        purchaseOrderItemMapper.updateBatch(itemsDOList);
    }

    //计算采购订单的总价、税费、折扣价格,|计算总数量|计算总商品价格|计算总税费|计算折扣价格
    private void calculateTotalPrice(SrmPurchaseOrderDO purchaseOrder, List<SrmPurchaseOrderItemDO> purchaseOrderItems) {
        purchaseOrder.setTotalCount(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getCount, BigDecimal::add));
        purchaseOrder.setTotalProductPrice(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalTaxPrice(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
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
        SrmPurchaseOrderDO purchaseOrder = purchaseOrderMapper.selectByNo(No);
        if (purchaseOrder != null) {
            throw exception(PURCHASE_ORDER_CODE_DUPLICATE, No);
        }
    }

    private List<SrmPurchaseOrderItemDO> validatePurchaseOrderItems(List<SrmPurchaseOrderSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDTO> productList = erpProductApi.validProductList(convertSet(list, SrmPurchaseOrderSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDTO> dtoMap = convertMap(productList, ErpProductDTO::getId);
        // 2. 转化为 SrmPurchaseOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, SrmPurchaseOrderItemDO.class, item -> {
            //            item = SrmOrderConvert.INSTANCE.convertToErpPurchaseOrderItemDO(o);//convert转换一下
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

    private void updatePurchaseOrderItemList(Long id, List<SrmPurchaseOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SrmPurchaseOrderItemDO> oldList = purchaseOrderItemMapper.selectListByOrderId(id);
        List<List<SrmPurchaseOrderItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id).setSource(SOURCE));
            purchaseOrderItemMapper.insertBatch(diffList.get(0));
            //行状态初始化
            initSlaveStatus(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            // 批量查询所有需要的采购订单项（purchase order items）
            updatePurchaseRequestItem(diffList.get(1));
            //跟旧数据对比，申请数量差异，则发采购事件调整
            purchaseOrderItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            // 遍历并处理订单项
            for (SrmPurchaseOrderItemDO orderItemDO : diffList.get(0)) {
                deleteSyncLogic(orderItemDO);
            }
            purchaseOrderItemMapper.deleteBatchIds(convertList(diffList.get(2), SrmPurchaseOrderItemDO::getId));
        }
    }

    /**
     * 更新采购申请项,根据list集合
     *
     * @param diffList 订单项集合
     */
    private void updatePurchaseRequestItem(List<SrmPurchaseOrderItemDO> diffList) {
        Set<Long> orderItemIds = diffList.stream().map(SrmPurchaseOrderItemDO::getId).collect(Collectors.toSet());
        Map<Long, SrmPurchaseOrderItemDO> orderItemMap = validatePurchaseOrderItemExists(orderItemIds).stream().collect(Collectors.toMap(SrmPurchaseOrderItemDO::getId, Function.identity()));

        // 批量查询所有需要的采购申请项（purchase request items）
        Set<Long> purchaseApplyItemIds = diffList.stream().map(SrmPurchaseOrderItemDO::getPurchaseApplyItemId).filter(Objects::nonNull)  // 过滤掉为空的purchaseApplyItemId
            .collect(Collectors.toSet());
        Map<Long, SrmPurchaseRequestItemsDO> requestItemsMap = srmPurchaseRequestService.validItemIdsExist(purchaseApplyItemIds).stream().collect(Collectors.toMap(SrmPurchaseRequestItemsDO::getId, Function.identity()));
        // 遍历并处理订单项
        for (SrmPurchaseOrderItemDO orderItemDO : diffList) {
            SrmPurchaseOrderItemDO oldOrderItem = orderItemMap.get(orderItemDO.getId());  // 从map中获取旧的order item
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                SrmPurchaseRequestItemsDO requestItemsDO = requestItemsMap.get(purchaseApplyItemId);  // 从map中获取request item
                // 验证:采购的产品数量 <= 申请项的剩余订购数量(批准数量 - 已订购数量)
                int newCount = orderItemDO.getCount().intValue();
                int oldCount = oldOrderItem.getCount().intValue();
                int changCount = newCount - oldCount;
                SrmOrderCountDTO dto = SrmOrderCountDTO.builder().purchaseRequestItemId(requestItemsDO.getId()).quantity(changCount).build();
                if (changCount < 0) {
                    //采购数量减少了
                    requestOrderItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                } else if (changCount > 0) {
                    //采购数量增多了
                    int i = requestItemsDO.getApproveCount() - requestItemsDO.getOrderedQuantity();
                    ThrowUtil.ifThrow(changCount > i, PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED, requestItemsDO.getId(), i);
                    requestOrderItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                }
            });
        }
    }

    @Override
    public void updatePurchaseOrderInCount(Long itemId, Map<Long, BigDecimal> inCountMap) {
        List<SrmPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(itemId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInCount().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED, erpProductApi.getProductDto(item.getProductId()).getName(), item.getCount());
            }
            purchaseOrderItemMapper.updateById(new SrmPurchaseOrderItemDO().setId(item.getId()).setInCount(inCount));
        });
        // 2. 更新采购订单
        BigDecimal totalInCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new SrmPurchaseOrderDO().setId(itemId).setTotalInCount(totalInCount));
    }

    @Override
    public void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<SrmPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal returnCount = returnCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getReturnCount().equals(returnCount)) {
                return;
            }
            if (returnCount.compareTo(item.getInCount()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED, erpProductApi.getProductDto(item.getProductId()).getName(), item.getInCount());
            }
            purchaseOrderItemMapper.updateById(new SrmPurchaseOrderItemDO().setId(item.getId()).setReturnCount(returnCount));
        });
        // 2. 更新采购订单
        BigDecimal totalReturnCount = getSumValue(returnCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new SrmPurchaseOrderDO().setId(orderId).setTotalReturnCount(totalReturnCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(List<Long> ids) {
        // 1. 校验不处于已审批
        List<SrmPurchaseOrderDO> purchaseOrders = purchaseOrderMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseOrders)) {
            return;
        }
        purchaseOrders.forEach(orderDO -> {
            if (SrmAuditStatus.APPROVED.getCode().equals(orderDO.getAuditStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, orderDO.getNo());
            }
            //存在对应的采购入库项->异常
            purchaseOrderItemMapper.selectListByOrderId(orderDO.getId()).forEach(item -> {
                boolean b = srmPurchaseInItemMapper.existsByOrderItemId(item.getId());
                ThrowUtil.ifThrow(b, PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_DEL, item.getId());
            });
        });
        // 2. 遍历删除
        purchaseOrders.forEach(purchaseOrder -> {
            List<SrmPurchaseOrderItemDO> dos = purchaseOrderItemMapper.selectListByOrderId(purchaseOrder.getId());
            //校验是否存在入库项
            dos.forEach(SrmPurchaseOrderServiceImpl::validHasInPurchaseItem);
            //联动申请项
            for (SrmPurchaseOrderItemDO item : dos) {
                deleteSyncLogic(item);
            }

            // 2.1 删除订单
            purchaseOrderMapper.deleteById(purchaseOrder.getId());
            // 2.2 删除订单项
            purchaseOrderItemMapper.deleteByOrderId(purchaseOrder.getId());
        });
    }

    /**
     * 删除同步逻辑
     *
     * @param item 订单项DO
     */
    private void deleteSyncLogic(SrmPurchaseOrderItemDO item) {
        Optional.ofNullable(item.getPurchaseApplyItemId()).ifPresent(id -> {
            SrmPurchaseRequestItemsDO requestItemsDO = requestItemsMapper.selectById(id);
            SrmOrderCountDTO dto = SrmOrderCountDTO.builder().purchaseRequestItemId(item.getPurchaseApplyItemId()).quantity(item.getCount().negate().intValue()).build();//减少申请个数的订购数量
            //撤销自动关闭
            requestItemOffMachine.fireEvent(SrmOffStatus.fromCode(requestItemsDO.getOffStatus()), SrmEventEnum.CANCEL_DELETE, requestItemsDO);
            //订购状态
            requestOrderItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
        });
    }

    /**
     * 校验是否存在入库项
     *
     * @param item 订单项
     */
    private static void validHasInPurchaseItem(SrmPurchaseOrderItemDO item) {
        //判断订单项是否存在入库项
        if (!Objects.equals(item.getInStatus(), SrmStorageStatus.NONE_IN_STORAGE.getCode())) {
            //存在入库项，则发抛出异常
            throw exception(PURCHASE_ORDER_DELETE_FAIL);
        }
    }

    @Override
    public SrmPurchaseOrderItemDO validatePurchaseOrderItemExists(Long id) {
        //校验采购订单项是否存在
        SrmPurchaseOrderItemDO aDo = purchaseOrderItemMapper.selectById(id);
        if (aDo == null) {
            throw exception(PURCHASE_ORDER_ITEM_NOT_EXISTS, id);
        }
        return aDo;
    }

    //校验orderItem是否存在， 入参集合ids
    @Override
    public List<SrmPurchaseOrderItemDO> validatePurchaseOrderItemExists(@NotNull Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<SrmPurchaseOrderItemDO> purchaseOrderItems = purchaseOrderItemMapper.selectBatchIds(ids);
        //校验是否和ids数量一直，报错未对应的订单项
        if (purchaseOrderItems.size() != ids.size()) {
            throw exception(PURCHASE_ORDER_ITEM_NOT_EXISTS, CollUtil.subtract(ids, CollUtil.newArrayList(purchaseOrderItems.stream().map(SrmPurchaseOrderItemDO::getId).collect(Collectors.toSet()))));
        }
        return purchaseOrderItems;
    }

    private SrmPurchaseOrderDO validatePurchaseOrderExists(Long id) {
        SrmPurchaseOrderDO purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        return purchaseOrder;
    }

    @Override
    public SrmPurchaseOrderDO getPurchaseOrder(Long id) {
        return purchaseOrderMapper.selectById(id);
    }

    @Override
    public SrmPurchaseOrderDO validatePurchaseOrder(Long id) {
        //只有已审核的才可以
        SrmPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(id);
        if (ObjectUtil.notEqual(purchaseOrder.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_ORDER_NOT_APPROVE, purchaseOrder.getNo());
        }
        return purchaseOrder;
    }

    @Override
    public PageResult<SrmPurchaseOrderDO> getPurchaseOrderPage(SrmPurchaseOrderPageReqVO pageReqVO) {
        List<Long> orderIds = null;
        if (pageReqVO.getErpPurchaseRequestItemNo() != null && !StrUtil.isEmpty(pageReqVO.getErpPurchaseRequestItemNo())) {
            //查找对应的DO，限定申请单对应的订单id，汇总ids
            orderIds = new ArrayList<>(purchaseOrderItemMapper.selectIdsByErpPurchaseRequestItemNo(pageReqVO.getErpPurchaseRequestItemNo()).stream().map(SrmPurchaseOrderItemDO::getOrderId).distinct().toList());
            if (orderIds.isEmpty()) {
                orderIds = new ArrayList<>(1); // 初始化一个新的ArrayList
                orderIds.add(-1L); // 指定一个不存在的数据,说明ItemNo 不存在对应的订单
            }
        }
        return purchaseOrderMapper.selectPage(pageReqVO, orderIds);
    }

    @Override
    public PageResult<SrmPurchaseOrderItemBO> getPurchaseOrderPageBO(SrmPurchaseOrderPageReqVO pageReqVO) {
        return purchaseOrderItemMapper.selectErpPurchaseOrderItemBOPage(pageReqVO);
    }

    @Override
    public SrmPurchaseOrderItemBO getPurchaseOrderBO(Long id) {
        return purchaseOrderItemMapper.selectErpPurchaseOrderItemBOById(id);
    }

    @Override
    public List<SrmPurchaseOrderItemBO> getPurchaseOrderBOList(SrmPurchaseOrderPageReqVO pageReqVO) {
        return purchaseOrderItemMapper.selectErpPurchaseOrderItemBOS(pageReqVO);
    }

    @Override
    public Collection<SrmPurchaseOrderDO> getPurchaseOrderList(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        return purchaseOrderMapper.selectByIds(orderIds);
    }

    @Override
    public SrmPurchaseOrderDO getPurchaseOrderByItemId(Long itemId) {
        AtomicReference<SrmPurchaseOrderDO> orderDO = new AtomicReference<>();
        Optional.ofNullable(purchaseOrderItemMapper.selectById(itemId)).ifPresent(item -> orderDO.set(purchaseOrderMapper.selectById(item.getOrderId())));
        return orderDO.get();
    }
    // ==================== 订单项 ====================

    //}
    @Override
    public List<SrmPurchaseOrderItemDO> getPurchaseOrderItemList(Collection<Long> itemIds) {
        if (CollUtil.isEmpty(itemIds)) {
            return Collections.emptyList();
        }
        return purchaseOrderItemMapper.selectByIds(itemIds);
    }

    @Override
    public List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId) {
        return purchaseOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByApplyIds(Collection<Long> applyIds) {
        return purchaseOrderItemMapper.selectListByApplyIds(applyIds);
    }

    @Override
    public List<SrmPurchaseOrderItemDO> getPurchaseOrderItemListByOrderIds(Collection<Long> orderIds) {
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
        List<SrmPurchaseOrderDO> orderDOS = purchaseOrderMapper.selectByIds(orderIds);
        if (CollUtil.isEmpty(orderDOS)) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        // 2. 触发事件
        orderDOS.forEach(orderDO -> {
            auditMachine.fireEvent(SrmAuditStatus.fromCode(orderDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, SrmPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build());
        });
    }

    @Override
    public void reviewPurchaseOrder(SrmPurchaseOrderAuditReqVO req) {
        // 查询采购订单信息
        SrmPurchaseOrderDO orderDO = purchaseOrderMapper.selectById(req.getOrderIds().get(0));

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
                boolean b = srmPurchaseInItemMapper.existsByOrderItemId(item.getId());
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
            List<SrmPurchaseOrderItemDO> orderItemDOS = validatePurchaseOrderItemExists(itemIds);
            if (!orderItemDOS.isEmpty()) {
                orderItemDOS.forEach(orderItemDO -> orderItemOffMachine.fireEvent(SrmOffStatus.fromCode(orderItemDO.getOffStatus()), event, orderItemDO));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(SrmPurchaseOrderMergeReqVO reqVO) {

        //校验
        for (SrmPurchaseOrderMergeReqVO.item item : reqVO.getItems()) {
            Long itemId = item.getItemId();
            SrmPurchaseOrderItemDO aDo = validatePurchaseOrderItemExists(itemId);
            SrmPurchaseOrderDO order = getPurchaseOrder(aDo.getOrderId());
            //非已审核+非开启+非完全入库,异常
            ThrowUtil.ifThrow(!Objects.equals(order.getAuditStatus(), SrmAuditStatus.APPROVED.getCode()), PURCHASE_ORDER_ITEM_NOT_AUDIT, itemId);
            ThrowUtil.ifThrow(!Objects.equals(aDo.getOffStatus(), SrmOffStatus.OPEN.getCode()), PURCHASE_ORDER_ITEM_NOT_OPEN, itemId);
            ThrowUtil.ifThrow(!Objects.equals(aDo.getInStatus(), SrmStorageStatus.ALL_IN_STORAGE.getCode()), PURCHASE_ORDER_IN_ITEM_NOT_OPEN, itemId);
        }
        List<Long> itemIds = reqVO.getItems().stream().map(SrmPurchaseOrderMergeReqVO.item::getItemId).collect(Collectors.toList());
        List<SrmPurchaseOrderItemDO> orderItemDOS = purchaseOrderItemMapper.selectListByItemIds(itemIds);
        //转换
        SrmPurchaseInSaveReqVO vo = BeanUtils.toBean(reqVO, SrmPurchaseInSaveReqVO.class, saveReqVO -> {
            saveReqVO.setNo(null).setItems(SrmOrderInConvert.INSTANCE.convertToErpPurchaseInSaveReqVOItems(orderItemDOS)).setId(null).setInTime(LocalDateTime.now());
        });
        //service持久化
        Long purchaseIn = purchaseInService.createPurchaseIn(vo);
        //修改采购单项的source = 合并入库
        List<SrmPurchaseInItemDO> itemDOS = srmPurchaseInItemMapper.selectListByInId(purchaseIn);
        itemDOS.forEach(itemDO -> itemDO.setSource("合并入库"));
        srmPurchaseInItemMapper.updateBatch(itemDOS);
    }

    @Override
    public void generateContract(SrmPurchaseOrderGenerateContractReqVO reqVO, HttpServletResponse response) {
        SrmPurchaseOrderDO orderDO = validatePurchaseOrderExists(reqVO.getOrderId());
        //1 从OSS拿到模板word
        XWPFTemplate xwpfTemplate = purchaseOrderTemplateManager.getTemplate("purchase/order/%s".formatted(reqVO.getTemplateName()));
        //2 模板word渲染数据
        List<SrmPurchaseOrderItemDO> itemDOS = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        Map<Long, FmsFinanceSubjectDTO> dtoMap = convertMap(erpFinanceSubjectApi.validateFinanceSubject(List.of(reqVO.getPartyAId(), reqVO.getPartyBId())), FmsFinanceSubjectDTO::getId);
        SrmPurchaseOrderWordBO wordBO = SrmOrderConvert.INSTANCE.bindDataFormOrderItemDO(itemDOS, orderDO, reqVO, dtoMap);
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

    @Override
    public String getMaxSerialNumber() {
        return noRedisDAO.getMaxSerial(PURCHASE_ORDER_NO_PREFIX, PURCHASE_ORDER_NO_OUT_OF_BOUNDS);
    }
}
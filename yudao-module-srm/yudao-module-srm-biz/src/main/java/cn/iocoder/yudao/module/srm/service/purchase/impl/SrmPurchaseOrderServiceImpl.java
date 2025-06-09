package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.template.core.TemplateService;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsAccountApi;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.srm.config.machine.SrmOrderInCountContext;
import cn.iocoder.yudao.module.srm.config.machine.SrmPayCountContext;
import cn.iocoder.yudao.module.srm.config.machine.SrmQuantityOrderedCountContext;
import cn.iocoder.yudao.module.srm.config.machine.order.SrmOrderItemOffContext;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.*;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderConvert;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderInConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.LogRecordConstants;
import cn.iocoder.yudao.module.srm.enums.SrmChannelEnum;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmPurchaseOrderSourceEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseRequestService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderItemBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.word.SrmPurchaseOrderWordBO;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWareHouseUpdateReqDTO;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.deepoove.poi.XWPFTemplate;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
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

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO.PURCHASE_ORDER_NO_PREFIX;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;
import static jodd.util.StringUtil.truncate;

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
    private final FmsCompanyApi erpCompanyApi;
    private final ResourcePatternResolver resourcePatternResolver;
    private final TemplateService templateService;

    @Resource(name = PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> orderOffMachine;
    @Resource(name = PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> orderAuditMachine;
    @Resource(name = PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> orderStorageMachine;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> orderPaymentMachine;
    @Resource(name = PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> orderExecutionMachine;
    //子项
    @Resource(name = PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    StateMachine<SrmOrderStatus, SrmEventEnum, SrmQuantityOrderedCountContext> requestItemMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> requestItemOffMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmOrderItemOffContext> orderItemOffMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmOrderInCountContext> orderItemStorageMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPayCountContext> orderItemPaymentMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> orderItemExecutionMachine;
    @Autowired
    @Lazy
    private SrmPurchaseRequestService srmPurchaseRequestService;
    @Resource(name = SrmChannelEnum.PURCHASE_ORDER)
    MessageChannel purchaseOrderChannel;
    @Autowired
    private WmsWarehouseApi wmsWarehouseApi;

    /**
     * 校验是否存在入库项
     *
     * @param item 订单项
     */
    private static void validHasInPurchaseItem(SrmPurchaseOrderItemDO item) {
        //判断订单项是否存在入库项
        if (!Objects.equals(item.getInboundStatus(), SrmStorageStatus.NONE_IN_STORAGE.getCode())) {
            //存在入库项，则发抛出异常
            throw exception(PURCHASE_ORDER_DELETE_FAIL);
        }
    }

    /**
     * 判断当前状态是否可以更新
     *
     * @param purchaseOrder purchaseOrder
     */
    private static void updateStatusCheck(SrmPurchaseOrderDO purchaseOrder) {
        //1.1 不处于草稿、审核不通过、审核撤销 状态->e
        ThrowUtil.ifThrow(
            !SrmAuditStatus.DRAFT.getCode().equals(purchaseOrder.getAuditStatus()) && !SrmAuditStatus.REJECTED.getCode()
                .equals(purchaseOrder.getAuditStatus()) && !SrmAuditStatus.REVOKED.getCode()
                .equals(purchaseOrder.getAuditStatus()), PURCHASE_ORDER_UPDATE_FAIL_APPROVE, purchaseOrder.getCode(),
            SrmAuditStatus.fromCode(purchaseOrder.getAuditStatus()).getDesc());

        //2.0 非开启 -> e
        ThrowUtil.ifThrow(!SrmOffStatus.OPEN.getCode().equals(purchaseOrder.getOffStatus()), PURCHASE_ORDER_UPDATE_FAIL_OFF, purchaseOrder.getCode());
    }

    /**
     * @param purchaseApplyItemIds 采购申请项ids
     */
    private void validPurchaseApplyItemId(List<Long> purchaseApplyItemIds, List<SrmPurchaseOrderItemDO> orderItems) {
        ///判断purchaseApplyItemIds 是否是空的,且如果只有一个元素，元素不能为空
        if (CollUtil.isEmpty(purchaseApplyItemIds) || purchaseApplyItemIds.size() == 1 && purchaseApplyItemIds.get(0) == null) {
            return;
        }
        //Map
        Map<Long, SrmPurchaseOrderItemDO> map = convertMap(orderItems, SrmPurchaseOrderItemDO::getPurchaseApplyItemId);

        List<SrmPurchaseRequestItemsDO> requestItemsDOS = srmPurchaseRequestService.validItemIdsExist(purchaseApplyItemIds);
        for (SrmPurchaseRequestItemsDO requestItemsDO : requestItemsDOS) {
            //剩余可采购数量 > 采购数量 -> e
            int i = requestItemsDO.getApprovedQty() - requestItemsDO.getOrderClosedQty();
            if (i < map.get(requestItemsDO.getId()).getQty().intValue()) {
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
            orderItemOffMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, new SrmOrderItemOffContext().setItemId(orderItemDO.getId()));
            //付款
            orderItemPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, SrmPayCountContext.builder().orderItemId(orderItemDO.getId()).build());
            //入库
            orderItemStorageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmOrderInCountContext.builder().orderItemId(orderItemDO.getId()).build());
            //执行
            orderItemExecutionMachine.fireEvent(SrmExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderItemDO);
        }
        //联动采购申请项的库存
        for (SrmPurchaseOrderItemDO orderItemDO : purchaseOrderItems) {
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(itemId -> {
                SrmPurchaseRequestItemsDO itemsDO = srmPurchaseRequestService.validItemIdExist(itemId);
                //下单数量 <-> 申请单已订购数量
                SrmQuantityOrderedCountContext dto = SrmQuantityOrderedCountContext.builder().purchaseRequestItemId(itemsDO.getId()).quantity(orderItemDO.getQty().intValue()).build();
                requestItemMachine.fireEvent(SrmOrderStatus.fromCode(itemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
            });
        }
    }

    private void initMasterState(SrmPurchaseOrderDO orderDO) {
        //主表
        //开关
        orderOffMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, orderDO);
        //审核
        orderAuditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT,
            SrmPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build());
        //入库
        orderStorageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, orderDO);
        //执行
        orderExecutionMachine.fireEvent(SrmExecutionStatus.PENDING, SrmEventEnum.EXECUTION_INIT, orderDO);
        //付款
        orderPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, orderDO);
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = LogRecordConstants.SRM_PURCHASE_ORDER_CREATE_SUB_TYPE,
        bizNo = "{{#id}}",
        extra = "{{#vo.code}}",
        success = "创建了采购订单【{{#vo.code}}】")
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(SrmPurchaseOrderSaveReqVO vo) {
        // 1.1 校验订单项的有效性
        List<SrmPurchaseOrderItemDO> orderItems = validatePurchaseOrderItems(vo.getItems());
        // 1.2 校验供应商
        supplierService.validateSupplier(vo.getSupplierId());
        // 1.3 校验结算账户
        if (vo.getAccountId() != null) {
            erpAccountApi.validateAccount(vo.getAccountId());
        }
        // 1.3.1 校验订单项是否可以被创建,数量够不够
        List<Long> purchaseApplyItemIds = orderItems.stream().map(SrmPurchaseOrderItemDO::getPurchaseApplyItemId).distinct().toList();
        validPurchaseApplyItemId(purchaseApplyItemIds, orderItems);
        // 1.3.2
        // 1.4 生成订单号，并校验唯一性
        voSetNo(vo);
        // 2.1 插入订单
        SrmPurchaseOrderDO orderDO = BeanUtils.toBean(vo, SrmPurchaseOrderDO.class, in -> in.setCode(vo.getCode()));
        //合计total
        calculateTotalPrice(orderDO, orderItems);
        // 2.1.1 插入单据日期+结算日期
        orderDO.setBillTime(vo.getBillTime() == null ? LocalDateTime.now() : vo.getBillTime());
        ThrowUtil.ifSqlThrow(purchaseOrderMapper.insert(orderDO), DB_INSERT_ERROR);
        // 2.2 插入订单项
        orderItems.forEach(o -> {
            o.setSource(o.getSource() == null ? SrmPurchaseOrderSourceEnum.WEB_ENTRY.getDesc() : o.getSource());
            o.setOrderId(orderDO.getId());
        });
        ThrowUtil.ifThrow(!purchaseOrderItemMapper.insertBatch(orderItems), DB_BATCH_INSERT_ERROR);
        orderItems = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        //3.0 设置初始化状态
        initMasterState(orderDO);
        initSlaveStatus(orderItems);
        //回填log记录
        LogRecordContext.putVariable("id", orderDO.getId());
        //发送消息
        purchaseOrderChannel.send(MessageBuilder.withPayload(Collections.singletonList(orderDO.getId())).build());
        return orderDO.getId();
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = LogRecordConstants.SRM_PURCHASE_ORDER_UPDATE_SUB_TYPE,
        bizNo = "{{#vo.id}}",
        extra = "{{#vo.code}}",
        success = "更新了采购订单【{{#vo.code}}】: {_DIFF{#vo}}")
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(SrmPurchaseOrderSaveReqVO vo) {
        SrmPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(vo.getId());
        // 1.1 处于未审核、审核不通过才可以修改
        updateStatusCheck(purchaseOrder);
        // 1.2 校验供应商
        if (SrmOffStatus.OPEN.getCode().equals(purchaseOrder.getAuditStatus())) {
            supplierService.validateSupplier(vo.getSupplierId());
        }
        // 1.3 校验结算账户
        if (vo.getAccountId() != null) {
            erpAccountApi.validateAccount(vo.getAccountId());
        }
        // 1.3.1 设置no
        String oldNo = purchaseOrder.getCode();
        if (!oldNo.equals(vo.getCode())) {
            voSetNo(vo);
        }
        // 1.4 校验订单项的有效性
        List<SrmPurchaseOrderItemDO> purchaseOrderItems = validatePurchaseOrderItems(vo.getItems());

        // 1.5 设置旧值
        // 3. 设置日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(purchaseOrder, SrmPurchaseOrderSaveReqVO.class, spk -> spk.setItems(BeanUtils.toBean(purchaseOrderItems, SrmPurchaseOrderSaveReqVO.Item.class))));

        // 2.1 更新订单
        SrmPurchaseOrderDO updateObj = BeanUtils.toBean(vo, SrmPurchaseOrderDO.class);
        calculateTotalPrice(updateObj, purchaseOrderItems);//计算item合计。
        purchaseOrderMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseOrderItemList(vo.getId(), purchaseOrderItems);
        vo.getItems().sort(Comparator.comparing(SrmPurchaseOrderSaveReqVO.Item::getId, Comparator.nullsFirst(Long::compareTo)));
        //发送消息
        purchaseOrderChannel.send(MessageBuilder.withPayload(Collections.singletonList(vo.getId())).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderJson(SrmPurchaseOrderSaveJsonReqVO reqVO) {
        SrmPurchaseOrderDO purchaseOrder = validatePurchaseOrderExists(reqVO.getId());
        //不处于已审核 -> e
        ThrowUtil.ifThrow(!SrmAuditStatus.APPROVED.getCode().equals(purchaseOrder.getAuditStatus()), PURCHASE_ORDER_ITEM_IN_FAIL_APPROVE,
            purchaseOrder.getCode());
        //验证子表id存在
        List<Long> itemIds = reqVO.getItems().stream().map(SrmPurchaseOrderSaveJsonReqVO.Item::getId).distinct().toList();
        //map
        Map<Long, SrmPurchaseOrderSaveJsonReqVO.Item> itemMap =
            reqVO.getItems().stream().collect(Collectors.toMap(SrmPurchaseOrderSaveJsonReqVO.Item::getId, Function.identity()));
        List<SrmPurchaseOrderItemDO> itemDOS = validatePurchaseOrderItemExists(itemIds);
        itemDOS.forEach(itemDO -> BeanUtils.copyProperties(itemMap.get(itemDO.getId()), itemDO));
        //更新
        ThrowUtil.ifThrow(purchaseOrderItemMapper.updateBatch(itemDOS), DB_UPDATE_ERROR);
    }

    private void voSetNo(SrmPurchaseOrderSaveReqVO vo) {
        //生成单据编号
        if (vo.getCode() != null) {
            ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(vo.getCode()) != null, PURCHASE_ORDER_NO_HAS_EXISTS, vo.getCode());
            //如果符合"^" + PURCHASE_ORDER_NO_PREFIX + "-\\d{8}-[0-8]\\d{5}$" 正则再执行
            String pattern = "^" + PURCHASE_ORDER_NO_PREFIX + "-\\d{8}-[0-8]\\d{5}$";
            if (vo.getCode().matches(pattern)) {
                noRedisDAO.setManualSerial(PURCHASE_ORDER_NO_PREFIX, vo.getCode());
            }
            noRedisDAO.setManualSerial(PURCHASE_ORDER_NO_PREFIX, vo.getCode());
        } else {
            vo.setCode(noRedisDAO.generate(PURCHASE_ORDER_NO_PREFIX, PURCHASE_ORDER_NO_OUT_OF_BOUNDS));
            //1.1 校验编号no是否在数据库中重复
            ThrowUtil.ifThrow(purchaseOrderMapper.selectByNo(vo.getCode()) != null, PURCHASE_ORDER_NO_EXISTS, vo.getCode());
        }
    }


    //计算采购订单的总价、税费、折扣价格,|计算总数量|计算总商品价格|计算总税费|计算折扣价格
    private void calculateTotalPrice(SrmPurchaseOrderDO purchaseOrder, List<SrmPurchaseOrderItemDO> purchaseOrderItems) {
        purchaseOrder.setTotalCount(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getQty, BigDecimal::add));
        purchaseOrder.setTotalProductPrice(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalGrossPrice(getSumValue(purchaseOrderItems, SrmPurchaseOrderItemDO::getTax, BigDecimal::add, BigDecimal.ZERO));
        purchaseOrder.setTotalPrice(purchaseOrder.getTotalProductPrice().add(purchaseOrder.getTotalGrossPrice()));
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

        // 2. 获取并校验采购申请项信息
        Set<Long> purchaseApplyItemIds = list.stream()
            .map(SrmPurchaseOrderSaveReqVO.Item::getPurchaseApplyItemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        // 使用不可变的 Map
        final Map<Long, SrmPurchaseRequestItemsDO> requestItemMap = CollUtil.isEmpty(purchaseApplyItemIds) ?
            Collections.emptyMap() :
            Collections.unmodifiableMap(convertMap(srmPurchaseRequestService.validItemIdsExist(purchaseApplyItemIds),
                SrmPurchaseRequestItemsDO::getId));

        // 3. 转化为 SrmPurchaseOrderItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, SrmPurchaseOrderItemDO.class, item -> {
            // 计算总价和税费
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getQty()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxRate() != null) {
                item.setTax(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxRate()));
            }

            // 设置产品相关信息
            ErpProductDTO product = dtoMap.get(item.getProductId());
            item.setProductUnitId(product.getUnitId());
            item.setProductName(product.getName());
            item.setProductCode(product.getCode());
            item.setProductUnitName(erpProductUnitApi.getProductUnitList(Collections.singleton(product.getUnitId())).get(0).getName());

            // 设置采购申请单相关信息
            if (item.getPurchaseApplyItemId() != null) {
                SrmPurchaseRequestItemsDO requestItem = requestItemMap.get(item.getPurchaseApplyItemId());
                if (requestItem == null) {
                    throw exception(PURCHASE_REQUEST_ITEM_NOT_EXISTS, item.getPurchaseApplyItemId());
                }
                // 获取并设置采购申请单编号
                item.setPurchaseApplyCode(srmPurchaseRequestService.getPurchaseRequest(requestItem.getRequestId()).getCode());
            }
        }));
    }

    private void updatePurchaseOrderItemList(Long id, List<SrmPurchaseOrderItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SrmPurchaseOrderItemDO> oldList = purchaseOrderItemMapper.selectListByOrderId(id);
        List<List<SrmPurchaseOrderItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOrderId(id).setSource(SrmPurchaseOrderSourceEnum.WEB_ENTRY.getDesc()));
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
            purchaseOrderItemMapper.deleteByIds(convertList(diffList.get(2), SrmPurchaseOrderItemDO::getId));
        }
    }

    /**
     * 更新采购申请项,根据list集合
     *
     * @param diffList 订单项集合
     */
    private void updatePurchaseRequestItem(List<SrmPurchaseOrderItemDO> diffList) {
        Set<Long> orderItemIds = diffList.stream().map(SrmPurchaseOrderItemDO::getId).collect(Collectors.toSet());
        Map<Long, SrmPurchaseOrderItemDO> orderItemMap =
            validatePurchaseOrderItemExists(orderItemIds).stream().collect(Collectors.toMap(SrmPurchaseOrderItemDO::getId, Function.identity()));

        // 批量查询所有需要的采购申请项（purchase request items）
        Set<Long> purchaseApplyItemIds =
            diffList.stream().map(SrmPurchaseOrderItemDO::getPurchaseApplyItemId).filter(Objects::nonNull)  // 过滤掉为空的purchaseApplyItemId
                .collect(Collectors.toSet());
        Map<Long, SrmPurchaseRequestItemsDO> requestItemsMap = srmPurchaseRequestService.validItemIdsExist(purchaseApplyItemIds).stream()
            .collect(Collectors.toMap(SrmPurchaseRequestItemsDO::getId, Function.identity()));
        // 遍历并处理订单项
        for (SrmPurchaseOrderItemDO orderItemDO : diffList) {
            SrmPurchaseOrderItemDO oldOrderItem = orderItemMap.get(orderItemDO.getId());  // 从map中获取旧的order item
            Optional.ofNullable(orderItemDO.getPurchaseApplyItemId()).ifPresent(purchaseApplyItemId -> {
                SrmPurchaseRequestItemsDO requestItemsDO = requestItemsMap.get(purchaseApplyItemId);  // 从map中获取request item
                // 验证:采购的产品数量 <= 申请项的剩余订购数量(批准数量 - 已订购数量)
                int newCount = orderItemDO.getQty().intValue();
                int oldCount = oldOrderItem.getQty().intValue();
                int changCount = newCount - oldCount;
                SrmQuantityOrderedCountContext dto = SrmQuantityOrderedCountContext.builder().purchaseRequestItemId(requestItemsDO.getId()).quantity(changCount).build();
                if (changCount < 0) {
                    //采购数量减少了
                    requestItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                } else if (changCount > 0) {
                    //采购数量增多了
                    int i = requestItemsDO.getApprovedQty() - requestItemsDO.getOrderClosedQty();
                    ThrowUtil.ifThrow(changCount > i, PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED, requestItemsDO.getId(), i);
                    requestItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
                }
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderInCount(Long itemId, Map<Long, BigDecimal> inCountMap) {
        List<SrmPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(itemId);
        // 1. 更新每个采购订单项
        orderItems.forEach(item -> {
            BigDecimal inCount = inCountMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (item.getInboundClosedQty().equals(inCount)) {
                return;
            }
            if (inCount.compareTo(item.getQty()) > 0) {
                throw exception(PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED, erpProductApi.getProductDto(item.getProductId()).getName(), item.getQty());
            }
            purchaseOrderItemMapper.updateById(new SrmPurchaseOrderItemDO().setId(item.getId()).setInboundClosedQty(inCount));
        });
        // 2. 更新采购订单
        BigDecimal totalInboundCount = getSumValue(inCountMap.values(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new SrmPurchaseOrderDO().setId(itemId).setTotalInboundCount(totalInboundCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderReturnCount(Long orderId, Map<Long, BigDecimal> returnCountMap) {
        List<SrmPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(orderId);
        // 1. 更新对应的采购订单项
        Map<Long, SrmPurchaseOrderItemDO> orderItemMap = orderItems.stream().collect(Collectors.toMap(SrmPurchaseOrderItemDO::getId, Function.identity()));
        returnCountMap.keySet().forEach(itemId -> {
            SrmPurchaseOrderItemDO item = orderItemMap.get(itemId);
            if (item != null) {
                BigDecimal lastedReturnCount = returnCountMap.getOrDefault(itemId, BigDecimal.ZERO);
                if (item.getReturnCount().equals(lastedReturnCount)) {
                    return;
                }
                if (lastedReturnCount.compareTo(item.getInboundClosedQty()) > 0) {
                    throw exception(PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED, item.getId(), erpProductApi.getProductDto(item.getProductId()).getName(), item.getInboundClosedQty());
                }
                purchaseOrderItemMapper.updateById(new SrmPurchaseOrderItemDO().setId(item.getId()).setReturnCount(lastedReturnCount));
            }
        });

        // 2. 更新采购订单
        BigDecimal totalReturnCount = getSumValue(orderItems.stream().map(SrmPurchaseOrderItemDO::getReturnCount).toList(), value -> value, BigDecimal::add, BigDecimal.ZERO);
        purchaseOrderMapper.updateById(new SrmPurchaseOrderDO().setId(orderId).setTotalReturnCount(totalReturnCount));
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = LogRecordConstants.SRM_PURCHASE_ORDER_DELETE_SUB_TYPE,
        bizNo = "{{#ids[0]}}",
        extra = "{{#businessName}}",
        success = "删除了采购订单【{{#businessName}}】")
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(List<Long> ids) {
        // 获取业务名称用于日志记录
        List<SrmPurchaseOrderDO> orders = purchaseOrderMapper.selectByIds(ids);
        String businessName = CollUtil.join(orders.stream().map(SrmPurchaseOrderDO::getCode).collect(Collectors.toList()), ",");
        LogRecordContext.putVariable("businessName", businessName);

        // 1. 校验不处于已审批
        List<SrmPurchaseOrderDO> purchaseOrders = purchaseOrderMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseOrders)) {
            return;
        }
        purchaseOrders.forEach(orderDO -> {
            //已审核 -> e
            if (SrmAuditStatus.APPROVED.getCode().equals(orderDO.getAuditStatus())) {
                throw exception(PURCHASE_ORDER_DELETE_FAIL_APPROVE, orderDO.getCode());
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
            SrmQuantityOrderedCountContext dto = SrmQuantityOrderedCountContext.builder().purchaseRequestItemId(item.getPurchaseApplyItemId()).quantity(item.getQty().negate().intValue())
                .build();//减少申请个数的订购数量
            //触发关闭撤销
            requestItemOffMachine.fireEvent(SrmOffStatus.fromCode(requestItemsDO.getOffStatus()), SrmEventEnum.CANCEL_DELETE, requestItemsDO);
            //订购状态调整
            requestItemMachine.fireEvent(SrmOrderStatus.fromCode(requestItemsDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, dto);
        });
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
        List<SrmPurchaseOrderItemDO> purchaseOrderItems = purchaseOrderItemMapper.selectByIds(ids);
        //校验是否和ids数量一直，报错未对应的订单项
        if (purchaseOrderItems.size() != ids.size()) {
            throw exception(PURCHASE_ORDER_ITEM_NOT_EXISTS,
                CollUtil.subtract(ids, CollUtil.newArrayList(purchaseOrderItems.stream().map(SrmPurchaseOrderItemDO::getId).collect(Collectors.toSet()))));
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
            throw exception(PURCHASE_ORDER_NOT_APPROVE, purchaseOrder.getCode());
        }
        return purchaseOrder;
    }

    @Override
    public PageResult<SrmPurchaseOrderBO> getPurchaseOrderPageBO(SrmPurchaseOrderPageReqVO pageReqVO) {
        PageResult<SrmPurchaseOrderItemBO> orderItemBOPage = purchaseOrderItemMapper.selectErpPurchaseOrderItemBOPage(pageReqVO);
        // convert
        List<SrmPurchaseOrderBO> orderBOS = SrmOrderConvert.INSTANCE.convertToSrmPurchaseOrderBOList(orderItemBOPage.getList());

        return new PageResult<>(orderBOS, orderItemBOPage.getTotal());
    }

    @Override
    public SrmPurchaseOrderBO getPurchaseOrderBO(Long id) {
        //主表
        SrmPurchaseOrderDO srmPurchaseOrderDO = validatePurchaseOrderExists(id);
        //子表
        List<SrmPurchaseOrderItemDO> srmPurchaseOrderItemDOS = purchaseOrderItemMapper.selectListByOrderId(id);
        //convert
        return BeanUtils.toBean(srmPurchaseOrderDO, SrmPurchaseOrderBO.class, p -> p.setSrmPurchaseOrderItemDOS(srmPurchaseOrderItemDOS));
    }

    /**
     * 查询采购订单列表。
     * 数据量后期会很大，待优化(缓存+并行convert)
     *
     * @param pageReqVO 分页查询条件，复用一下暂时
     * @return 采购订单列表
     */
    @Override
    public List<SrmPurchaseOrderBO> getPurchaseOrderBOList(SrmPurchaseOrderPageReqVO pageReqVO) {
        List<SrmPurchaseOrderItemBO> srmPurchaseOrderItemBOS = purchaseOrderItemMapper.selectErpPurchaseOrderItemBOS(pageReqVO);
        //
        return SrmOrderConvert.INSTANCE.convertToSrmPurchaseOrderBOList(srmPurchaseOrderItemBOS);
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
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = LogRecordConstants.SRM_PURCHASE_ORDER_AUDIT_SUB_TYPE,
        bizNo = "{{#vo.orderIds[0]}}",
        extra = "{{#code}}",
        success = "{{#vo.reviewed ? (#vo.pass ? '审核通过' : '审核不通过') : '反审核'}}了采购订单【{{#code}}】")
    @Transactional(rollbackFor = Exception.class)
    public void reviewPurchaseOrder(SrmPurchaseOrderAuditReqVO vo) {
        // 查询采购订单信息
        SrmPurchaseOrderDO orderDO = purchaseOrderMapper.selectById(vo.getOrderIds().get(0));
        if (orderDO == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        //日志上下文
        LogRecordContext.putVariable("code", orderDO.getCode());
        // 获取当前订单状态
        SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(orderDO.getAuditStatus());
        if (Boolean.TRUE.equals(vo.getReviewed())) {
            // 审核操作
            if (vo.getPass()) {
                log.debug("采购订单通过审核，ID: {}", orderDO.getId());
                orderAuditMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, vo);
                //更新WMS仓库在制数量
                this.updateWareHouseGNumber(orderDO, false);
            } else {
                log.debug("采购订单拒绝审核，ID: {}", orderDO.getId());
                orderAuditMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, vo);
            }
        } else {
            //反审核
            //存在对应的采购入库项->异常
            purchaseOrderItemMapper.selectListByOrderId(orderDO.getId()).forEach(item -> {
                boolean b = srmPurchaseInItemMapper.existsByOrderItemId(item.getId());
                ThrowUtil.ifThrow(b, PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_IN, item.getId());
            });
            log.debug("采购订单撤回审核，ID: {}", orderDO.getId());
            orderAuditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, vo);
            //减少wms对应产品的在制数量
            this.updateWareHouseGNumber(orderDO, true);
        }
    }

    /**
     * 更新WMS仓库在制数量
     *
     * @param orderDO 订单DO
     */
    private void updateWareHouseGNumber(SrmPurchaseOrderDO orderDO, Boolean isReverse) {
        // 获取订单项列表
        List<SrmPurchaseOrderItemDO> orderItems = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
        // 遍历订单项，更新每个产品的在制数量
        for (SrmPurchaseOrderItemDO item : orderItems) {
            WmsWareHouseUpdateReqDTO reqDTO = new WmsWareHouseUpdateReqDTO();
            reqDTO.setProductId(item.getProductId());
            reqDTO.setWarehouseId(item.getWarehouseId());
            // 根据isReverse参数决定是否使用负数
            reqDTO.setMakePendingQty(isReverse ? item.getQty().negate().intValue() : item.getQty().intValue());
            wmsWarehouseApi.updateStockWarehouse(reqDTO);
        }

    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = "{{#open ? '开启采购订单' : '关闭采购订单'}}",
        bizNo = "{{#itemIds[0]}}",
        extra = "{{#codes}}",
        success = "{{#open ? '开启了采购订单【' + #codes + '】' : '关闭了采购订单【' + #codes + '】'}}")
    @Transactional(rollbackFor = Exception.class)
    public void switchPurchaseOrderStatus(List<Long> itemIds, Boolean open) {
        SrmEventEnum event = Boolean.TRUE.equals(open) ? SrmEventEnum.ACTIVATE : SrmEventEnum.MANUAL_CLOSE;
        if (itemIds != null && !itemIds.isEmpty()) {
            // 批量处理采购订单子项状态
            List<SrmPurchaseOrderItemDO> orderItemDOS = validatePurchaseOrderItemExists(itemIds);
            if (!orderItemDOS.isEmpty()) {
                // 获取订单编号用于日志记录
                List<SrmPurchaseOrderDO> orders = purchaseOrderMapper.selectByIds(orderItemDOS.stream().map(SrmPurchaseOrderItemDO::getOrderId).collect(Collectors.toSet()));
                String codes = CollUtil.join(orders.stream().map(SrmPurchaseOrderDO::getCode).collect(Collectors.toList()), ",");
                LogRecordContext.putVariable("codes", codes);
                orderItemDOS.forEach(orderItemDO -> orderItemOffMachine.fireEvent(SrmOffStatus.fromCode(orderItemDO.getOffStatus()), event, new SrmOrderItemOffContext().setItemId(orderItemDO.getId())));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(SrmPurchaseOrderMergeReqVO reqVO) {
        //TODO 逻辑处理待优化

        // 校验
        for (SrmPurchaseOrderMergeReqVO.Item item : reqVO.getItems()) {
            Long itemId = item.getItemId();
            SrmPurchaseOrderItemDO aDo = validatePurchaseOrderItemExists(itemId);
            SrmPurchaseOrderDO order = getPurchaseOrder(aDo.getOrderId());
            // 非已审核+非开启+完全入库,异常
            ThrowUtil.ifThrow(!Objects.equals(order.getAuditStatus(), SrmAuditStatus.APPROVED.getCode()), PURCHASE_ORDER_ITEM_NOT_AUDIT, itemId);
//            ThrowUtil.ifThrow(!Objects.equals(aDo.getOffStatus(), SrmOffStatus.OPEN.getCode()), PURCHASE_ORDER_ITEM_NOT_OPEN, itemId);
            ThrowUtil.ifThrow(Objects.equals(aDo.getInboundStatus(), SrmStorageStatus.ALL_IN_STORAGE.getCode()), PURCHASE_ORDER_IN_ITEM_NOT_OPEN, itemId);
        }
        List<Long> itemIds = reqVO.getItems().stream().map(SrmPurchaseOrderMergeReqVO.Item::getItemId).collect(Collectors.toList());
        List<SrmPurchaseOrderItemDO> orderItemDOS = purchaseOrderItemMapper.selectListByItemIds(itemIds);

        // 1. 构建 itemId -> qty 的映射
        Map<Long, BigDecimal> itemIdToQtyMap = reqVO.getItems().stream().collect(Collectors.toMap(SrmPurchaseOrderMergeReqVO.Item::getItemId, SrmPurchaseOrderMergeReqVO.Item::getQty));

        //convert
        List<SrmPurchaseInSaveReqVO.Item> inItems = SrmOrderInConvert.INSTANCE.convertToErpPurchaseInSaveReqVOItems(orderItemDOS);
        // 2. 转换并赋值到货数量
        for (SrmPurchaseInSaveReqVO.Item inItem : inItems) {
            BigDecimal qty = itemIdToQtyMap.get(inItem.getOrderItemId());
            if (qty != null) {
                inItem.setQty(qty);
            }
        }
        //3 转换赋值仓库编号
        Map<Long, Long> itemIdToWarehouseIdMap = reqVO.getItems().stream().collect(Collectors.toMap(SrmPurchaseOrderMergeReqVO.Item::getItemId, SrmPurchaseOrderMergeReqVO.Item::getWarehouseId));
        for (SrmPurchaseInSaveReqVO.Item inItem : inItems) {
            inItem.setWarehouseId(itemIdToWarehouseIdMap.get(inItem.getOrderItemId()));
        }
        //4.0 因为同一个供应商的币种都是相同的，而明细行只允许同一个供应商，所以取第一个关联订单的币种信息
        AtomicReference<Long> currencyId = new AtomicReference<>();
        reqVO.getItems().stream().findFirst().ifPresent(inItem -> {
            Long orderItemId = inItem.getItemId();
            SrmPurchaseOrderDO srmPurchaseOrderDO = this.getPurchaseOrderByItemId(orderItemId);
            currencyId.set(srmPurchaseOrderDO.getCurrencyId());
        });

        // 3. 构建 saveVO
        SrmPurchaseInSaveReqVO vo = BeanUtils.toBean(reqVO, SrmPurchaseInSaveReqVO.class, saveReqVO ->
            saveReqVO.setCode(null)
                .setItems(inItems)
                .setId(null)
                .setArriveTime(reqVO.getBillTime() == null ? LocalDateTime.now() : reqVO.getBillTime())
                .setCurrencyId(currencyId.get()) // 币别
        );
        // service持久化
        try {
            Long purchaseIn = purchaseInService.createPurchaseIn(vo);
        } catch (Exception e) {
            log.error("合并采购订单生成到货单失败，参数：{}，异常：{}", vo, e.getMessage(), e);
            throw exception(PURCHASE_ORDER_MERGE_IN_FAIL, "合并到货单", truncate(e.getMessage(), 200));
        }
    }

    @Override
    public void generateContract(SrmPurchaseOrderGenerateContractReqVO reqVO, HttpServletResponse response) {
        SrmPurchaseOrderDO orderDO = validatePurchaseOrderExists(reqVO.getOrderId());
        // 校验订单状态是否已审核 未审核 -> e
        //        ThrowUtil.ifThrow(!Objects.equals(orderDO.getAuditStatus(), SrmAuditStatus.APPROVED.getCode()), PURCHASE_ORDER_NOT_AUDIT, orderDO.getId());
        //1 从OSS拿到模板word
        org.springframework.core.io.Resource resource = getResourceByFilePath(reqVO);
        try (XWPFTemplate xwpfTemplate = templateService.buildXWPDFTemplate(resource)) {
            //2 模板word渲染数据
            List<SrmPurchaseOrderItemDO> itemDOS = purchaseOrderItemMapper.selectListByOrderId(orderDO.getId());
            Map<Long, FmsCompanyDTO> dtoMap = convertMap(erpCompanyApi.validateCompany(Set.of(reqVO.getPartyAId(), reqVO.getPartyBId())), FmsCompanyDTO::getId);
            SrmPurchaseOrderWordBO wordBO = SrmOrderConvert.INSTANCE.bindDataFormOrderItemDO(itemDOS, orderDO, reqVO, dtoMap);
            xwpfTemplate.render(wordBO);
            //3 转换pdf，返回响应
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 用于捕获输出流
            try (OutputStream out = response.getOutputStream()) {
                xwpfTemplate.write(byteArrayOutputStream);
                // 设置响应头，准备下载
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("采购合同.pdf", StandardCharsets.UTF_8));
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                // 将字节数组转成输入流
                try (InputStream inputStreamResult = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                    // 使用 Aspose 转换为 PDF
                    Document document = new Document(inputStreamResult);
                    document.save(out, SaveFormat.PDF);
                }
                out.flush();
            }
        } catch (Exception e) {
            throw exception(PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_ERROR, reqVO.getTemplateName(), e.getMessage());
        }
    }

    private org.springframework.core.io.Resource getResourceByFilePath(SrmPurchaseOrderGenerateContractReqVO reqVO) {
        return resourcePatternResolver.getResource("classpath:purchase/order/" + reqVO.getTemplateName());
    }

    /**
     * 获取 JAR 包中的模板文件列表（只列出以 .docx 结尾的文件）
     *
     * @return 文件名列表
     */
    @Override
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

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_ORDER_TYPE,
        subType = LogRecordConstants.SRM_PURCHASE_ORDER_SUBMIT_AUDIT_SUB_TYPE,
        bizNo = "{{#orderIds[0]}}",
        extra = "{{#codes}}",
        success = "提交了采购订单【{{#codes}}】审核")
    @Transactional(rollbackFor = Exception.class)
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
        // 获取单据编号用于日志记录
        String codes = CollUtil.join(orderDOS.stream().map(SrmPurchaseOrderDO::getCode).collect(Collectors.toList()), ",");
        LogRecordContext.putVariable("codes", codes);

        // 2. 触发事件
        orderDOS.forEach(orderDO ->
            orderAuditMachine.fireEvent(SrmAuditStatus.fromCode(orderDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW,
                SrmPurchaseOrderAuditReqVO.builder().orderIds(Collections.singletonList(orderDO.getId())).build()));
    }
}
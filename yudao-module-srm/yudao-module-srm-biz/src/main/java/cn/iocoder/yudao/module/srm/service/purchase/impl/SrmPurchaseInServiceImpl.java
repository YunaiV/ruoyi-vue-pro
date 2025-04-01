package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.fms.api.finance.FmsAccountApi;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPayReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderInConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnItemMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.CANCEL_PAYMENT;
import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.COMPLETE_PAYMENT;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;


/**
 * ERP 采购入库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseInServiceImpl implements SrmPurchaseInService {

    private final SrmPurchaseInMapper purchaseInMapper;
    private final SrmPurchaseInItemMapper purchaseInItemMapper;
    private final ErpProductApi erpProductApi;
    private final SrmNoRedisDAO noRedisDAO;
    private final FmsAccountApi erpAccountApi;
    private final SrmPurchaseReturnItemMapper srmPurchaseReturnItemMapper;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    SrmPurchaseOrderService purchaseOrderService;


    @Resource(name = PURCHASE_IN_AUDIT_STATE_MACHINE)
    private StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_IN_PAYMENT_STATE_MACHINE)
    private StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> paymentMachine;
    @Resource(name = PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE)
    private StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInItemDO> itemPaymentMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> orderItemStorageMachine;
    @Resource(name = PURCHASE_IN_AUDIT_STATE_MACHINE)
    private StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> purchaseInAuditStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseIn(SrmPurchaseInSaveReqVO createReqVO) {
        // 1.1 校验采购订单已审核
        for (SrmPurchaseInSaveReqVO.Item item : createReqVO.getItems()) {
            SrmPurchaseOrderItemDO aDo = purchaseOrderService.validatePurchaseOrderItemExists(item.getOrderItemId());
            if (aDo != null) {
                Optional.ofNullable(aDo.getOrderId()).ifPresent(orderId -> purchaseOrderService.validatePurchaseOrder(orderId));
            }
        }
        // 1.2 校验入库项的有效性
        List<SrmPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(createReqVO.getItems());
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(createReqVO.getAccountId());
        // 1.4 生成入库单号，并校验唯一性
        String no = noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_IN_NO_PREFIX, PURCHASE_IN_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseInMapper.selectByNo(no) != null, PURCHASE_IN_NO_EXISTS);

        // 2.1 插入入库
        SrmPurchaseInDO purchaseIn = BeanUtils.toBean(createReqVO, SrmPurchaseInDO.class, in -> in.setNo(no));
        calculateTotalPrice(purchaseIn, purchaseInItems);
        ThrowUtil.ifSqlThrow(purchaseInMapper.insert(purchaseIn), GlobalErrorCodeConstants.DB_INSERT_ERROR);
        // 2.2 插入入库项
        purchaseInItems.forEach(o -> o.setInId(purchaseIn.getId()));
        BeanUtils.copyProperties(createReqVO.getItems(), purchaseInItems);
        ThrowUtil.ifThrow(!purchaseInItemMapper.insertBatch(purchaseInItems), GlobalErrorCodeConstants.DB_BATCH_INSERT_ERROR);
        //3.0 设置初始化状态
        initMasterStatus(purchaseIn);
        initSlaveStatus(purchaseInItems);
        return purchaseIn.getId();
    }

    private void initSlaveStatus(List<SrmPurchaseInItemDO> purchaseInItems) {
        for (SrmPurchaseInItemDO purchaseInItem : purchaseInItems) {
            itemPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, purchaseInItem);
        }
    }

    private void initMasterStatus(SrmPurchaseInDO purchaseIn) {
        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, SrmPurchaseInAuditReqVO.builder().inId(purchaseIn.getId()).build());
        paymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, purchaseIn);
    }

    private void rollbackSlaveStatus(List<SrmPurchaseInItemDO> diffList) {
        for (SrmPurchaseInItemDO inItemDO : diffList) {
            Optional.ofNullable(inItemDO.getOrderItemId()).ifPresent(orderItemId -> {
                SrmPurchaseOrderItemDO orderItemDO = purchaseOrderService.validatePurchaseOrderItemExists(orderItemId);
                orderItemStorageMachine.fireEvent(SrmStorageStatus.fromCode(orderItemDO.getInStatus()), SrmEventEnum.STOCK_ADJUSTMENT, SrmInCountDTO.builder().orderItemId(orderItemId)
                    //取反数量
                    .inCount(inItemDO.getQty().negate()).build());
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseIn(SrmPurchaseInSaveReqVO vo) {
        // 1.1 校验存在
        SrmPurchaseInDO purchaseIn = validatePurchaseInExists(vo.getId());
        if (SrmAuditStatus.APPROVED.getCode().equals(purchaseIn.getAuditStatus())) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_APPROVE, purchaseIn.getNo());
        }
        // 1.2 校验采购订单已审核
        for (SrmPurchaseInSaveReqVO.Item item : vo.getItems()) {
            SrmPurchaseOrderItemDO aDo = purchaseOrderService.validatePurchaseOrderItemExists(item.getOrderItemId());
            if (aDo != null) {
                Optional.ofNullable(aDo.getOrderId()).ifPresent(orderId -> purchaseOrderService.validatePurchaseOrder(orderId));
            }
        }
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 校验订单项的有效性
        List<SrmPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(vo.getItems());

        // 2.1 更新入库
        SrmPurchaseInDO updateObj = BeanUtils.toBean(vo, SrmPurchaseInDO.class);
        //            .setOrderNo(purchaseOrder.getNo())
        //            .setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(updateObj, purchaseInItems);//合计
        purchaseInMapper.updateById(updateObj);
        // 2.2 更新入库项
        updatePurchaseInItemList(vo.getId(), purchaseInItems);

    }

    private void calculateTotalPrice(SrmPurchaseInDO purchaseIn, List<SrmPurchaseInItemDO> purchaseInItems) {
        purchaseIn.setTotalCount(getSumValue(purchaseInItems, SrmPurchaseInItemDO::getQty, BigDecimal::add));
        purchaseIn.setTotalProductPrice(getSumValue(purchaseInItems, SrmPurchaseInItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalTaxPrice(getSumValue(purchaseInItems, SrmPurchaseInItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalPrice(purchaseIn.getTotalProductPrice().add(purchaseIn.getTotalTaxPrice()));
        // 计算优惠价格
        if (purchaseIn.getDiscountPercent() == null) {
            purchaseIn.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseIn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseIn.getTotalPrice(), purchaseIn.getDiscountPercent()));
        purchaseIn.setTotalPrice(purchaseIn.getTotalPrice().subtract(purchaseIn.getDiscountPrice()).add(purchaseIn.getOtherPrice()));
    }

    @Override
    public void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice) {
        SrmPurchaseInDO purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn.getPaymentPrice().equals(paymentPrice)) {
            return;
        }
        if (paymentPrice.compareTo(purchaseIn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED, paymentPrice, purchaseIn.getTotalPrice());
        }
        purchaseInMapper.updateById(new SrmPurchaseInDO().setId(id).setPaymentPrice(paymentPrice));
    }

    private List<SrmPurchaseInItemDO> validatePurchaseInItems(List<SrmPurchaseInSaveReqVO.Item> voItems) {
        // 1.1 批量获取订单项,根据入库项的订单项id
        Map<Long, SrmPurchaseOrderItemDO> orderItemMap = convertMap(purchaseOrderService.getPurchaseOrderItemList(convertSet(voItems, SrmPurchaseInSaveReqVO.Item::getOrderItemId)), SrmPurchaseOrderItemDO::getId);
        //
        return convertList(voItems, voItem -> BeanUtils.toBean(voItem, SrmPurchaseInItemDO.class, inItemDO -> {
            // 金额计算
            inItemDO.setTotalPrice(MoneyUtils.priceMultiply(inItemDO.getProductPrice(), inItemDO.getQty()));
            if (inItemDO.getTaxPercent() != null && inItemDO.getTotalPrice() != null) {
                inItemDO.setTaxPrice(MoneyUtils.priceMultiplyPercent(inItemDO.getTotalPrice(), inItemDO.getTaxPercent()));
            }
            // 填充订单项字段
//            Optional.ofNullable(inItemDO.getOrderItemId()).ifPresent(orderItemId -> {
//                SrmPurchaseOrderItemDO orderItemDO = orderItemMap.get(orderItemId);
//                if (orderItemDO != null) {
//                    BeanUtils.copyProperties(SrmOrderInConvert.INSTANCE.toPurchaseInItem(orderItemDO), inItemDO);
//                    inItemDO.setOrderItemId(orderItemDO.getId());
//                }
//            });
        }));
    }

    private void updatePurchaseInItemList(Long id, List<SrmPurchaseInItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SrmPurchaseInItemDO> oldList = purchaseInItemMapper.selectListByInId(id);
        List<List<SrmPurchaseInItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> {
                o.setInId(id);
                o.setSource("WEB录入");
            });
            purchaseInItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {

            purchaseInItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            if (diffList.get(2) != null) {
                purchaseInItemMapper.deleteByIds(convertList(diffList.get(2), SrmPurchaseInItemDO::getId));
            }
        }
    }


    private void linkSlaveStatus(List<SrmPurchaseInItemDO> purchaseInItems) {
        //执行状态+入库状态
        for (SrmPurchaseInItemDO purchaseInItem : purchaseInItems) {
            //传递给订单项入库状态机 数量
            Optional.ofNullable(purchaseInItem.getOrderItemId()).ifPresent(orderItemId -> {//存在订单项
                //校验订单项是否存在
                SrmPurchaseOrderItemDO orderItemDO = purchaseOrderService.validatePurchaseOrderItemExists(orderItemId);
                //更新订单项入库数量+状态 入库状态机,创建入库单->增加入库数量
                orderItemStorageMachine.fireEvent(SrmStorageStatus.fromCode(orderItemDO.getInStatus()), SrmEventEnum.STOCK_ADJUSTMENT, SrmInCountDTO.builder().orderItemId(orderItemId)
                    //正数
                    .inCount(purchaseInItem.getQty()).build());
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseIn(List<Long> ids) {
        // 1. 已审批->无法删除
        List<SrmPurchaseInDO> purchaseIns = purchaseInMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseIns)) {
            return;
        }
        for (SrmPurchaseInDO inDO : purchaseIns) {
            //校验,入库项存在对应的退货项 -> 异常
            purchaseInItemMapper.selectListByInId(inDO.getId()).forEach(purchaseInItem -> {
                boolean b = srmPurchaseReturnItemMapper.existsByInItemId(purchaseInItem.getId());
                ThrowUtil.ifThrow(b, PURCHASE_IN_DELETE_FAIL, purchaseInItem.getId());
            });
        }

        //2. 联动回滚状态数量
        purchaseIns.forEach(purchaseIn -> {
            if (SrmAuditStatus.APPROVED.getCode().equals(purchaseIn.getAuditStatus())) {
                throw exception(PURCHASE_IN_DELETE_FAIL_APPROVE, purchaseIn.getNo());
            }
            rollbackSlaveStatus(purchaseInItemMapper.selectListByInId(purchaseIn.getId()));
        });
        // 2. 遍历删除，并记录操作日志
        purchaseIns.forEach(purchaseIn -> {
            // 2.1 删除订单
            purchaseInMapper.deleteById(purchaseIn.getId());
            // 2.2 删除订单项
            purchaseInItemMapper.deleteByInId(purchaseIn.getId());
        });

    }

    private SrmPurchaseInDO validatePurchaseInExists(Long id) {
        SrmPurchaseInDO purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    //验证入库项是否存在
    private SrmPurchaseInItemDO validatePurchaseInItemExists(Long id) {
        SrmPurchaseInItemDO purchaseInItem = purchaseInItemMapper.selectById(id);
        if (purchaseInItem == null) {
            throw exception(PURCHASE_IN_ITEM_NOT_EXISTS, id);
        }
        return purchaseInItem;
    }

    @Override
    public SrmPurchaseInDO getPurchaseIn(Long id) {
        return purchaseInMapper.selectById(id);
    }

    @Override
    public SrmPurchaseInDO validatePurchaseIn(Long id) {
        SrmPurchaseInDO purchaseIn = validatePurchaseInExists(id);
        if (ObjectUtil.notEqual(purchaseIn.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_IN_NOT_APPROVE, purchaseIn.getNo());
        }
        return purchaseIn;
    }

    @Override
    public PageResult<SrmPurchaseInDO> getPurchaseInPage(SrmPurchaseInPageReqVO pageReqVO) {
        return purchaseInMapper.selectPage(pageReqVO);
    }

    // ==================== 采购入库项 ====================

    @Override
    public List<SrmPurchaseInItemDO> getPurchaseInItemListByInId(Long inId) {
        return purchaseInItemMapper.selectListByInId(inId);
    }

    @Override
    public List<SrmPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return purchaseInItemMapper.selectListByInIds(inIds);
    }

    @Override
    public List<SrmPurchaseInItemDO> validatePurchaseInItemExists(List<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        List<SrmPurchaseInItemDO> inItemDOS = purchaseInItemMapper.selectByIds(inIds);
        //检验是否和ids数量一致，报错未对应入库项
        if (inItemDOS.size() != inIds.size()) {
            throw exception(PURCHASE_IN_ITEM_NOT_EXISTS, CollUtil.subtract(inIds, CollUtil.newArrayList(
                inItemDOS.stream().map(SrmPurchaseInItemDO::getId).collect(Collectors.toSet()))));
        }
        return inItemDOS;
    }

    @Override
    public void submitAudit(Collection<Long> inIds) {
        for (Long inId : inIds) {
            SrmPurchaseInDO srmPurchaseInDO = validatePurchaseInExists(inId);
            purchaseInAuditStateMachine.fireEvent(SrmAuditStatus.fromCode(srmPurchaseInDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, SrmPurchaseInAuditReqVO.builder().inId(inId).build());//提交审核
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(SrmPurchaseInAuditReqVO req) {
        //        purchaseInAuditStateMachine.fireEvent(SrmAuditStatus.fromCode(erpPurchaseInDO.getAuditorStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, req);//提交审核
        // 查询采购订单信息
        SrmPurchaseInDO inDO = validatePurchaseInExists(req.getInId());
        // 获取当前订单状态
        SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(inDO.getAuditStatus());
        if (Boolean.TRUE.equals(req.getReviewed())) {
            // 审核操作
            if (req.getPass()) {
                log.debug("采购订单通过审核，ID: {}", inDO.getId());
                //联动状态
                auditMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, req);
                linkSlaveStatus(purchaseInItemMapper.selectListByInId(inDO.getId()));
            } else {
                log.debug("采购订单拒绝审核，ID: {}", inDO.getId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, req);
            }
        } else {
            //撤销审核
            //校验,入库项存在对应的退货项 -> 异常
            purchaseInItemMapper.selectListByInId(inDO.getId()).forEach(purchaseInItem -> {
                boolean b = srmPurchaseReturnItemMapper.existsByInItemId(purchaseInItem.getId());
                ThrowUtil.ifThrow(b, PURCHASE_IN_PROCESS_FAIL_RETURN_ITEM_EXISTS, purchaseInItem.getId());
            });

            // 1.3 校验已付款
            if (!Objects.equals(inDO.getPayStatus(), SrmPaymentStatus.NONE_PAYMENT.getCode())) {
                throw exception(PURCHASE_IN_PROCESS_FAIL_PAYMENT_STATUS);
            }
            //回滚状态
            rollbackSlaveStatus(purchaseInItemMapper.selectListByInId(inDO.getId()));
            log.debug("采购订单撤回审核，ID: {}", inDO.getId());
            auditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
        }
    }

    @Override
    public void switchPayStatus(SrmPurchaseInPayReqVO vo) {
        //1.0 校验，已审核才可以
        vo.getInItemIds().stream().distinct().forEach(item -> {
            Long inId = purchaseInItemMapper.selectById(item).getInId();
            SrmPurchaseInDO purchaseInDO = purchaseInMapper.selectById(inId);
            ThrowUtil.ifThrow(!purchaseInDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_IN_NOT_APPROVE, purchaseInDO.getNo());
        });
        SrmEventEnum eventEnum;
        if (vo.getPass()) {
            eventEnum = COMPLETE_PAYMENT;
        } else {
            eventEnum = CANCEL_PAYMENT;
        }
        SrmEventEnum finalEventEnum = eventEnum;
        vo.getInItemIds().stream().distinct().forEach(inItemId -> {
            //校验
            SrmPurchaseInItemDO inItemDO = validatePurchaseInItemExists(inItemId);
            if (inItemDO.getPayStatus() == null) {
                itemPaymentMachine.fireEvent(SrmPaymentStatus.NONE_PAYMENT, finalEventEnum, inItemDO);
            } else {
                itemPaymentMachine.fireEvent(SrmPaymentStatus.fromCode(inItemDO.getPayStatus()), finalEventEnum, inItemDO);
            }

        });
    }
}
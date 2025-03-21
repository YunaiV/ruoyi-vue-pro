package cn.iocoder.yudao.module.srm.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.finance.ErpAccountApi;
import cn.iocoder.yudao.module.srm.api.product.ErpProductApi;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInPayReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;

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
public class ErpPurchaseInServiceImpl implements ErpPurchaseInService {

    @Resource
    private ErpPurchaseInMapper purchaseInMapper;
    @Resource
    private ErpPurchaseInItemMapper purchaseInItemMapper;

    @Resource
    ErpProductApi erpProductApi;
    @Resource
    private SrmNoRedisDAO noRedisDAO;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpAccountApi erpAccountApi;
    @Resource
    ErpPurchaseOrderItemMapper orderItemMapper;
    @Resource(name = PURCHASE_IN_AUDIT_STATE_MACHINE)
    private StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseInAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_IN_PAYMENT_STATE_MACHINE)
    private StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseInDO> paymentMachine;
    @Resource(name = PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE)
    private StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseInItemDO> itemPaymentMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> orderItemStorageMachine;
    @Resource(name = PURCHASE_IN_AUDIT_STATE_MACHINE)
    private StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseInAuditReqVO> purchaseInAuditStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseIn(ErpPurchaseInSaveReqVO createReqVO) {
        // 1.1 校验采购订单已审核
        for (ErpPurchaseInSaveReqVO.Item item : createReqVO.getItems()) {
            ErpPurchaseOrderItemDO aDo = orderItemMapper.selectById(item.getOrderItemId());
            if (aDo != null) {
                Optional.ofNullable(aDo.getOrderId()).ifPresent(orderId -> purchaseOrderService.validatePurchaseOrder(orderId));
            }
        }
//        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(createReqVO.getOrderId());
        // 1.2 校验入库项的有效性
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(createReqVO.getItems());
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(createReqVO.getAccountId());
        // 1.4 生成入库单号，并校验唯一性
        String no = noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_IN_NO_PREFIX, PURCHASE_IN_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseInMapper.selectByNo(no) != null ,PURCHASE_IN_NO_EXISTS);

        // 2.1 插入入库
        ErpPurchaseInDO purchaseIn = BeanUtils.toBean(createReqVO, ErpPurchaseInDO.class, in -> in
            .setNo(no));
        calculateTotalPrice(purchaseIn, purchaseInItems);
        ThrowUtil.ifSqlThrow(purchaseInMapper.insert(purchaseIn), GlobalErrorCodeConstants.DB_INSERT_ERROR);
        // 2.2 插入入库项
        purchaseInItems.forEach(o -> o.setInId(purchaseIn.getId()));
        ThrowUtil.ifThrow(!purchaseInItemMapper.insertBatch(purchaseInItems), GlobalErrorCodeConstants.DB_BATCH_INSERT_ERROR);
        //3.0 设置初始化状态
        initMasterStatus(purchaseIn);
        initSlaveStatus(purchaseInItems);
        return purchaseIn.getId();
    }

    private void initSlaveStatus(List<ErpPurchaseInItemDO> purchaseInItems) {
        for (ErpPurchaseInItemDO purchaseInItem : purchaseInItems) {
            itemPaymentMachine.fireEvent(ErpPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, purchaseInItem);
        }
    }

    private void initMasterStatus(ErpPurchaseInDO purchaseIn) {
        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, ErpPurchaseInAuditReqVO.builder().inId(purchaseIn.getId()).build());
        paymentMachine.fireEvent(ErpPaymentStatus.NONE_PAYMENT, SrmEventEnum.PAYMENT_INIT, purchaseIn);
    }

    private void rollbackSlaveStatus(List<ErpPurchaseInItemDO> diffList) {
        for (ErpPurchaseInItemDO inItemDO : diffList) {
            Optional.ofNullable(inItemDO.getOrderItemId()).ifPresent(orderItemId -> {
                ErpPurchaseOrderItemDO orderItemDO = orderItemMapper.selectById(orderItemId);

                orderItemStorageMachine.fireEvent(ErpStorageStatus.fromCode(orderItemDO.getInStatus()), SrmEventEnum.STOCK_ADJUSTMENT,
                    SrmInCountDTO.builder().orderItemId(orderItemId)
                        //取反数量
                        .inCount(inItemDO.getCount().negate()).build());
            });
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseIn(ErpPurchaseInSaveReqVO vo) {
        // 1.1 校验存在
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(vo.getId());
        if (SrmAuditStatus.APPROVED.getCode().equals(purchaseIn.getAuditStatus())) {
            throw exception(PURCHASE_IN_UPDATE_FAIL_APPROVE, purchaseIn.getNo());
        }
        // 1.2 校验采购订单已审核
        for (ErpPurchaseInSaveReqVO.Item item : vo.getItems()) {
            ErpPurchaseOrderItemDO aDo = orderItemMapper.selectById(item.getOrderItemId());
            if (aDo != null) {
                Optional.ofNullable(aDo.getOrderId()).ifPresent(orderId -> purchaseOrderService.validatePurchaseOrder(orderId));
            }
        }
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 校验订单项的有效性
        List<ErpPurchaseInItemDO> purchaseInItems = validatePurchaseInItems(vo.getItems());

        // 2.1 更新入库
        ErpPurchaseInDO updateObj = BeanUtils.toBean(vo, ErpPurchaseInDO.class);
//            .setOrderNo(purchaseOrder.getNo())
//            .setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(updateObj, purchaseInItems);//合计
        purchaseInMapper.updateById(updateObj);
        // 2.2 更新入库项
        updatePurchaseInItemList(vo.getId(), purchaseInItems);

    }

    private void calculateTotalPrice(ErpPurchaseInDO purchaseIn, List<ErpPurchaseInItemDO> purchaseInItems) {
        purchaseIn.setTotalCount(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getCount, BigDecimal::add));
        purchaseIn.setTotalProductPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseIn.setTotalTaxPrice(getSumValue(purchaseInItems, ErpPurchaseInItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
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
        ErpPurchaseInDO purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn.getPaymentPrice().equals(paymentPrice)) {
            return;
        }
        if (paymentPrice.compareTo(purchaseIn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED, paymentPrice, purchaseIn.getTotalPrice());
        }
        purchaseInMapper.updateById(new ErpPurchaseInDO().setId(id).setPaymentPrice(paymentPrice));
    }

    private List<ErpPurchaseInItemDO> validatePurchaseInItems(List<ErpPurchaseInSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDTO> productList = erpProductApi.validProductList(
                convertSet(list, ErpPurchaseInSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDTO> productMap = convertMap(productList, ErpProductDTO::getId);
        // 2. 转化为 ErpPurchaseInItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseInItemDO.class, item -> {
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

    private void updatePurchaseInItemList(Long id, List<ErpPurchaseInItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseInItemDO> oldList = purchaseInItemMapper.selectListByInId(id);
        List<List<ErpPurchaseInItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
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
                purchaseInItemMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseInItemDO::getId));
            }
        }
    }


    private void linkSlaveStatus(List<ErpPurchaseInItemDO> purchaseInItems) {
        //执行状态+入库状态
        for (ErpPurchaseInItemDO purchaseInItem : purchaseInItems) {
            //传递给订单项入库状态机 数量
            Optional.ofNullable(purchaseInItem.getOrderItemId()).ifPresent(orderItemId -> {//存在订单项
                //校验订单项是否存在
                purchaseOrderService.validatePurchaseOrderItemExists(orderItemId);
                ErpPurchaseOrderItemDO orderItemDO = orderItemMapper.selectById(orderItemId);
                //更新订单项入库数量+状态 入库状态机,创建入库单->增加入库数量
                orderItemStorageMachine.fireEvent(ErpStorageStatus.fromCode(orderItemDO.getInStatus()), SrmEventEnum.STOCK_ADJUSTMENT,
                    SrmInCountDTO.builder().orderItemId(orderItemId)
                        //正数
                        .inCount(purchaseInItem.getCount()).build());
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseIn(List<Long> ids) {
        // 1. 已审批->无法删除
        List<ErpPurchaseInDO> purchaseIns = purchaseInMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseIns)) {
            return;
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

    private ErpPurchaseInDO validatePurchaseInExists(Long id) {
        ErpPurchaseInDO purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    //验证入库项是否存在
    private ErpPurchaseInItemDO validatePurchaseInItemExists(Long id) {
        ErpPurchaseInItemDO purchaseInItem = purchaseInItemMapper.selectById(id);
        if (purchaseInItem == null)
            throw exception(PURCHASE_IN_ITEM_NOT_EXISTS, id);
        return purchaseInItem;
    }

    @Override
    public ErpPurchaseInDO getPurchaseIn(Long id) {
        return purchaseInMapper.selectById(id);
    }

    @Override
    public ErpPurchaseInDO validatePurchaseIn(Long id) {
        ErpPurchaseInDO purchaseIn = validatePurchaseInExists(id);
        if (ObjectUtil.notEqual(purchaseIn.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_IN_NOT_APPROVE, purchaseIn.getNo());
        }
        return purchaseIn;
    }

    @Override
    public PageResult<ErpPurchaseInDO> getPurchaseInPage(ErpPurchaseInPageReqVO pageReqVO) {
        return purchaseInMapper.selectPage(pageReqVO);
    }

    // ==================== 采购入库项 ====================

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInId(Long inId) {
        return purchaseInItemMapper.selectListByInId(inId);
    }

    @Override
    public List<ErpPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyList();
        }
        return purchaseInItemMapper.selectListByInIds(inIds);
    }

    @Override
    public void submitAudit(Collection<Long > inIds) {
        for (Long inId : inIds) {
            ErpPurchaseInDO erpPurchaseInDO = validatePurchaseInExists(inId);
            purchaseInAuditStateMachine.fireEvent(SrmAuditStatus.fromCode(erpPurchaseInDO.getAuditStatus())
                , SrmEventEnum.SUBMIT_FOR_REVIEW
                , ErpPurchaseInAuditReqVO.builder().inId(inId).build());//提交审核
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ErpPurchaseInAuditReqVO req) {
//        purchaseInAuditStateMachine.fireEvent(SrmAuditStatus.fromCode(erpPurchaseInDO.getAuditorStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, req);//提交审核
        // 查询采购订单信息
        ErpPurchaseInDO inDO = validatePurchaseInExists(req.getInId());
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
            // 1.3 校验已付款
            if (!Objects.equals(inDO.getPayStatus(), ErpPaymentStatus.NONE_PAYMENT.getCode())) {
                throw exception(PURCHASE_IN_PROCESS_FAIL_PAYMENT_STATUS);
            }
            //回滚状态
            rollbackSlaveStatus(purchaseInItemMapper.selectListByInId(inDO.getId()));
            log.debug("采购订单撤回审核，ID: {}", inDO.getId());
            auditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
        }
    }

    @Override
    public void switchPayStatus(ErpPurchaseInPayReqVO vo) {
        //1.0 校验，已审核才可以
        vo.getInItemIds().stream().distinct().forEach(
            item -> {
                Long inId = purchaseInItemMapper.selectById(item).getInId();
                ErpPurchaseInDO purchaseInDO = purchaseInMapper.selectById(inId);
                ThrowUtil.ifThrow(!purchaseInDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_IN_NOT_APPROVE, purchaseInDO.getNo());
            }
        );
        SrmEventEnum eventEnum;
        if (vo.getPass()) {
            eventEnum = COMPLETE_PAYMENT;
        } else {
            eventEnum = CANCEL_PAYMENT;
        }
        SrmEventEnum finalEventEnum = eventEnum;
        vo.getInItemIds().stream().distinct().forEach(
            inItemId -> {
                //校验
                ErpPurchaseInItemDO inItemDO = validatePurchaseInItemExists(inItemId);
                if (inItemDO.getPayStatus() == null) {
                    itemPaymentMachine.fireEvent(ErpPaymentStatus.NONE_PAYMENT, finalEventEnum, inItemDO);
                } else {
                    itemPaymentMachine.fireEvent(ErpPaymentStatus.fromCode(inItemDO.getPayStatus()), finalEventEnum, inItemDO);
                }

            }
        );
    }
}
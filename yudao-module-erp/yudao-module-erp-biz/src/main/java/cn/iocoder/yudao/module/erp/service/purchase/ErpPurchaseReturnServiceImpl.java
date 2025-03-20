package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.purchase.ErpInCountDTO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpReturnStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockRecordService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErpEventEnum.RETURN_CANCEL;
import static cn.iocoder.yudao.module.erp.enums.ErpEventEnum.RETURN_COMPLETE;
import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 采购退货 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ErpPurchaseReturnServiceImpl implements ErpPurchaseReturnService {

    @Resource
    private ErpPurchaseReturnMapper purchaseReturnMapper;
    @Resource
    private ErpPurchaseReturnItemMapper purchaseReturnItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Autowired
    ErpPurchaseInItemMapper inItemMapper;
    @Resource
    ErpPurchaseOrderItemMapper orderItemMapper;
    @Resource(name = PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseReturnAuditReqVO> auditStatusMachine;
    @Resource(name = PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME)
    StateMachine<ErpReturnStatus, ErpEventEnum, ErpPurchaseReturnDO> refundStateMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<ErpStorageStatus, ErpEventEnum, ErpInCountDTO> orderItemStorageMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(ErpPurchaseReturnSaveReqVO createReqVO) {
        // 1.1 校验入库单已审核
        // 1.2 校验退货项的有效性
        List<ErpPurchaseReturnItemDO> item = validatePurchaseReturnItems(createReqVO.getItems());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 生成退货单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_RETURN_NO_PREFIX, PURCHASE_RETURN_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseReturnMapper.selectByNo(no) != null ,PURCHASE_RETURN_NO_EXISTS);
        // 2.1 插入退货
        ErpPurchaseReturnDO purchaseReturn = BeanUtils.toBean(createReqVO, ErpPurchaseReturnDO.class, in -> in
            .setNo(no).setAuditStatus(ErpAuditStatus.PENDING_REVIEW.getCode()));
//                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(purchaseReturn, item);
        purchaseReturnMapper.insert(purchaseReturn);
        // 2.2 插入退货项
        item.forEach(o -> o.setReturnId(purchaseReturn.getId()));
        purchaseReturnItemMapper.insertBatch(item);
        //更新主子表状态
        initMasterStatus(purchaseReturn);
        //子表-入库状态机
        initSlaveStatus(item);
        return purchaseReturn.getId();
    }

    private void initSlaveStatus(List<ErpPurchaseReturnItemDO> items) {
        //订单入库状态机
        for (ErpPurchaseReturnItemDO item : items) {
            Optional.ofNullable(inItemMapper.selectById(item.getInItemId())).ifPresent(o -> {
                syncCountLogic(o, item.getCount());
            });
        }
    }

    private void initMasterStatus(ErpPurchaseReturnDO purchaseReturn) {
        auditStatusMachine.fireEvent(ErpAuditStatus.DRAFT, ErpEventEnum.AUDIT_INIT, ErpPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(purchaseReturn.getId())).build());
        refundStateMachine.fireEvent(ErpReturnStatus.NOT_RETURN, ErpEventEnum.RETURN_INIT, purchaseReturn);
        //联动订单数据

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturn(ErpPurchaseReturnSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVED.getCode().equals(purchaseReturn.getAuditStatus())) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_APPROVE, purchaseReturn.getNo());
        }
        // 1.2 校验退货单已审核
        // 1.3 校验结算账户
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.4 校验订单项的有效性
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = validatePurchaseReturnItems(updateReqVO.getItems());

        // 2.1 更新退货
        ErpPurchaseReturnDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseReturnDO.class);
        calculateTotalPrice(updateObj, purchaseReturnItems);
        purchaseReturnMapper.updateById(updateObj);
        // 2.2 更新退货项
        updatePurchaseReturnItemList(updateReqVO.getId(), purchaseReturnItems);
    }

    private void calculateTotalPrice(ErpPurchaseReturnDO purchaseReturn, List<ErpPurchaseReturnItemDO> purchaseReturnItems) {
        purchaseReturn.setTotalCount(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getCount, BigDecimal::add));
        purchaseReturn.setTotalProductPrice(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalTaxPrice(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalPrice(purchaseReturn.getTotalProductPrice().add(purchaseReturn.getTotalTaxPrice()));
        // 计算优惠价格
        if (purchaseReturn.getDiscountPercent() == null) {
            purchaseReturn.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseReturn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseReturn.getTotalPrice(), purchaseReturn.getDiscountPercent()));
        purchaseReturn.setTotalPrice(purchaseReturn.getTotalPrice().subtract(purchaseReturn.getDiscountPrice()).add(purchaseReturn.getOtherPrice()));
    }

    public void updatePurchaseReturnStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVED.getCode().equals(status);
        // 1.1 校验存在
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(id);
        // 1.2 校验状态
        if (purchaseReturn.getAuditStatus().equals(status)) {
            throw exception(approve ? PURCHASE_RETURN_APPROVE_FAIL : PURCHASE_RETURN_PROCESS_FAIL);
        }
        // 1.3 校验已退款
        if (!approve && purchaseReturn.getRefundPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND);
        }

        // 2. 更新状态
        int updateCount = purchaseReturnMapper.updateByIdAndStatus(id, purchaseReturn.getAuditStatus(),
            new ErpPurchaseReturnDO().setAuditStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? PURCHASE_RETURN_APPROVE_FAIL : PURCHASE_RETURN_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = purchaseReturnItemMapper.selectListByReturnId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.PURCHASE_RETURN.getType()
                : ErpStockRecordBizTypeEnum.PURCHASE_RETURN_CANCEL.getType();
        purchaseReturnItems.forEach(purchaseReturnItem -> {
            BigDecimal count = approve ? purchaseReturnItem.getCount().negate() : purchaseReturnItem.getCount();
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    purchaseReturnItem.getProductId(), purchaseReturnItem.getWarehouseId(), count,
                    bizType, purchaseReturnItem.getReturnId(), purchaseReturnItem.getId(), purchaseReturn.getNo()));
        });
    }

    @Override
    public void updatePurchaseReturnRefundPrice(Long id, BigDecimal refundPrice) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnMapper.selectById(id);
        if (purchaseReturn.getRefundPrice().equals(refundPrice)) {
            return;
        }
        if (refundPrice.compareTo(purchaseReturn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED, refundPrice, purchaseReturn.getTotalPrice());
        }
        purchaseReturnMapper.updateById(new ErpPurchaseReturnDO().setId(id).setRefundPrice(refundPrice));
    }

    private List<ErpPurchaseReturnItemDO> validatePurchaseReturnItems(List<ErpPurchaseReturnSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpPurchaseReturnSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpPurchaseReturnItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseReturnItemDO.class, item -> {
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

    private void updatePurchaseReturnItemList(Long id, List<ErpPurchaseReturnItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseReturnItemDO> oldList = purchaseReturnItemMapper.selectListByReturnId(id);
        List<List<ErpPurchaseReturnItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setReturnId(id));
            initSlaveStatus(diffList.get(0));
            purchaseReturnItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            for (ErpPurchaseReturnItemDO itemDO : diffList.get(1)) {
                BigDecimal newCount = itemDO.getCount();
                BigDecimal oldCount = purchaseReturnItemMapper.selectById(itemDO.getId()).getCount();
                //
                syncCountLogic(inItemMapper.selectById(itemDO.getInItemId()), newCount.subtract(oldCount));
            }
            purchaseReturnItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            for (ErpPurchaseReturnItemDO itemDO : diffList.get(2)) {
                syncCountLogic(inItemMapper.selectById(itemDO.getInItemId()), itemDO.getCount().negate());

            }
            //
            purchaseReturnItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpPurchaseReturnItemDO::getId));
        }
    }

    /**
     * 同步给订单项退货数量
     *
     * @param inItemDO ErpPurchaseInItemDO
     * @param number   BigDecimal
     */
    private void syncCountLogic(ErpPurchaseInItemDO inItemDO, BigDecimal number) {
        Long orderItemId = inItemDO.getOrderItemId();
        ErpPurchaseOrderItemDO orderItemDO = orderItemMapper.selectById(orderItemId);

        orderItemStorageMachine.fireEvent(ErpStorageStatus.fromCode(orderItemDO.getInStatus()), ErpEventEnum.STOCK_ADJUSTMENT,
            ErpInCountDTO.builder().orderItemId(orderItemId)
                .returnCount(number).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseReturn(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseReturnDO> purchaseReturns = purchaseReturnMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(purchaseReturns)) {
            return;
        }
        purchaseReturns.forEach(purchaseReturn -> {
            if (ErpAuditStatus.APPROVED.getCode().equals(purchaseReturn.getAuditStatus())) {
                throw exception(PURCHASE_RETURN_DELETE_FAIL_APPROVE, purchaseReturn.getNo());
            }
        });
        //同步退货数量
        for (ErpPurchaseReturnDO returnDO : purchaseReturns) {
            List<ErpPurchaseReturnItemDO> returnItemDOS = purchaseReturnItemMapper.selectListByReturnId(returnDO.getId());
            Optional.ofNullable(returnItemDOS).ifPresent(item -> {
                item.forEach(itemDO ->
                    syncCountLogic(inItemMapper.selectById(itemDO.getInItemId()), itemDO.getCount().negate()));
            });
        }
        // 2. 遍历删除，并记录操作日志
        purchaseReturns.forEach(purchaseReturn -> {
            // 2.1 删除订单
            purchaseReturnMapper.deleteById(purchaseReturn.getId());
            // 2.2 删除订单项
            purchaseReturnItemMapper.deleteByReturnId(purchaseReturn.getId());
        });

    }

    private ErpPurchaseReturnDO validatePurchaseReturnExists(Long id) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnMapper.selectById(id);
        if (purchaseReturn == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        return purchaseReturn;
    }

    @Override
    public ErpPurchaseReturnDO getPurchaseReturn(Long id) {
        return purchaseReturnMapper.selectById(id);
    }

    @Override
    public ErpPurchaseReturnDO validatePurchaseReturn(Long id) {
        ErpPurchaseReturnDO purchaseReturn = getPurchaseReturn(id);
        if (ObjectUtil.notEqual(purchaseReturn.getAuditStatus(), ErpAuditStatus.APPROVED.getCode())) {
            throw exception(PURCHASE_RETURN_NOT_APPROVE);
        }
        return purchaseReturn;
    }

    @Override
    public PageResult<ErpPurchaseReturnDO> getPurchaseReturnPage(ErpPurchaseReturnPageReqVO pageReqVO) {
        return purchaseReturnMapper.selectPage(pageReqVO);
    }

    // ==================== 采购退货项 ====================

    @Override
    public List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnId(Long returnId) {
        return purchaseReturnItemMapper.selectListByReturnId(returnId);
    }

    @Override
    public List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyList();
        }
        return purchaseReturnItemMapper.selectListByReturnIds(returnIds);
    }

    @Override
    public void submitAudit(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        //校验 1. 批量查询订单信息
        List<ErpPurchaseReturnDO> dos = purchaseReturnMapper.selectByIds(ids);
        if (CollUtil.isEmpty(dos)) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        // 2. 触发事件
        dos.forEach(returnDO -> {
            auditStatusMachine.fireEvent(ErpAuditStatus.fromCode(returnDO.getAuditStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, ErpPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(returnDO.getId())).build());
        });
    }

    @Override
    public void review(ErpPurchaseReturnAuditReqVO req) {
        // 查询退货单信息
        req.getIds().stream().findFirst().ifPresent(id -> {
            ErpPurchaseReturnDO orderDO = purchaseReturnMapper.selectById(id);
            if (orderDO == null) {
                throw exception(PURCHASE_RETURN_NOT_EXISTS);
            }
            // 获取当前订单状态
            ErpAuditStatus currentStatus = ErpAuditStatus.fromCode(orderDO.getAuditStatus());
            if (Boolean.TRUE.equals(req.getReviewed())) {
                // 审核操作
                if (req.getPass()) {
                    log.debug("退货单通过审核，ID: {}", orderDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, ErpEventEnum.AGREE, req);
                } else {
                    log.debug("退货单拒绝审核，ID: {}", orderDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, ErpEventEnum.REJECT, req);
                }
            } else {
                log.debug("退货单撤回审核，ID: {}", orderDO.getId());
                auditStatusMachine.fireEvent(currentStatus, ErpEventEnum.WITHDRAW_REVIEW, req);
            }
        });
    }

    @Override
    public void refund(ErpPurchaseReturnAuditReqVO vo) {
        ErpEventEnum eventEnum = null;
        if (vo.getPass()) {
            eventEnum = RETURN_COMPLETE;
        } else {
            eventEnum = RETURN_CANCEL;
        }
        ErpEventEnum finalEventEnum = eventEnum;
        vo.getIds().stream().distinct().forEach(
            id -> {
                ErpPurchaseReturnDO orderDO = purchaseReturnMapper.selectById(id);
                //校验
                refundStateMachine.fireEvent(ErpReturnStatus.fromCode(orderDO.getRefundStatus()), finalEventEnum, orderDO);
            }
        );
    }
}
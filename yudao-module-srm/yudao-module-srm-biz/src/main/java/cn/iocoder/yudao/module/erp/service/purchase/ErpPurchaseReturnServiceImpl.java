package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.finance.ErpAccountApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.*;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.*;
import cn.iocoder.yudao.module.erp.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpReturnStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.erp.enums.SrmEventEnum.RETURN_CANCEL;
import static cn.iocoder.yudao.module.erp.enums.SrmEventEnum.RETURN_COMPLETE;
import static cn.iocoder.yudao.module.erp.enums.SrmStateMachines.*;

/**
 * ERP 采购退货 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpPurchaseReturnServiceImpl implements ErpPurchaseReturnService {

    private final ErpPurchaseReturnMapper purchaseReturnMapper;
    private final ErpPurchaseReturnItemMapper purchaseReturnItemMapper;
    private final ErpPurchaseInItemMapper inItemMapper;
    private final SrmNoRedisDAO noRedisDAO;
    private final ErpPurchaseInMapper inMapper;
    private final ErpPurchaseOrderItemMapper orderItemMapper;
    private final ErpProductApi erpProductApi;
    private final ErpAccountApi erpAccountApi;

    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    private StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> orderItemStorageMachine;
    @Resource(name = PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseReturnAuditReqVO> auditStatusMachine;
    @Resource(name = PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME)
    StateMachine<ErpReturnStatus, SrmEventEnum, ErpPurchaseReturnDO> refundStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(ErpPurchaseReturnSaveReqVO vo) {
        // 1.1 校验入库单已审核，已开启
        validReqItemsAuditStatus(vo);
        // 1.2 校验退货项的有效性
        List<ErpPurchaseReturnItemDO> item = validatePurchaseReturnItems(vo.getItems());
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 生成退货单号，并校验唯一性
        String no = noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_RETURN_NO_PREFIX, PURCHASE_RETURN_NO_OUT_OF_BOUNDS);
        ThrowUtil.ifThrow(purchaseReturnMapper.selectByNo(no) != null, PURCHASE_RETURN_NO_EXISTS);
        // 2.1 插入退货
        ErpPurchaseReturnDO purchaseReturn = BeanUtils.toBean(vo, ErpPurchaseReturnDO.class, in -> in
            .setNo(no).setAuditStatus(SrmAuditStatus.PENDING_REVIEW.getCode()));
//                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(purchaseReturn, item);
        purchaseReturnMapper.insert(purchaseReturn);
        // 2.2 插入退货项
        item.forEach(o -> o.setReturnId(purchaseReturn.getId()));
        purchaseReturnItemMapper.insertBatch(item);
        //更新主子表状态
        initMasterStatus(purchaseReturn);
        //子表-入库状态机
//        linkSlaveStatus(item);
        return purchaseReturn.getId();
    }

    /**
     * 校验申请项里面是否符合状态要求
     *
     * @param vo vo
     */
    private void validReqItemsAuditStatus(ErpPurchaseReturnSaveReqVO vo) {
        for (ErpPurchaseReturnSaveReqVO.Item item : vo.getItems()) {
            Long inItemId = item.getInItemId();
            Optional.ofNullable(inItemMapper.selectById(inItemId)).ifPresent(inItemDO -> {
                ErpPurchaseInDO erpPurchaseInDO = inMapper.selectById(inItemDO.getInId());
                //非已审核状态
                ThrowUtil.ifThrow(!erpPurchaseInDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), (PURCHASE_IN_ITEM_NOT_AUDIT));
                //非开启状态
            });
        }
    }

    private void linkSlaveStatus(List<ErpPurchaseReturnItemDO> items) {
        //订单入库状态机
        for (ErpPurchaseReturnItemDO item : items) {
            Optional.ofNullable(inItemMapper.selectById(item.getInItemId())).ifPresent(o -> {
                syncCountLogic(o, item.getCount());
            });
        }
    }

    private void initMasterStatus(ErpPurchaseReturnDO purchaseReturn) {
        auditStatusMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, ErpPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(purchaseReturn.getId())).build());
        refundStateMachine.fireEvent(ErpReturnStatus.NOT_RETURN, SrmEventEnum.RETURN_INIT, purchaseReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturn(ErpPurchaseReturnSaveReqVO vo) {
        ThrowUtil.ifThrow(validAudit(vo.getId()), PURCHASE_RETURN_UPDATE_FAIL_APPROVE);
        validReqItemsAuditStatus(vo);
        // 1.2 校验退货单已审核
        // 1.3 校验结算账户
        erpAccountApi.validateAccount(vo.getAccountId());
        // 1.4 校验订单项的有效性
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = validatePurchaseReturnItems(vo.getItems());

        // 2.1 更新退货
        ErpPurchaseReturnDO updateObj = BeanUtils.toBean(vo, ErpPurchaseReturnDO.class);
        calculateTotalPrice(updateObj, purchaseReturnItems);
        purchaseReturnMapper.updateById(updateObj);
        // 2.2 更新退货项
        updatePurchaseReturnItemList(vo.getId(), purchaseReturnItems);
    }


    private Boolean validAudit(Long returnId) {
        // 1.1 校验已审核
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(returnId);
        return SrmAuditStatus.APPROVED.getCode().equals(purchaseReturn.getAuditStatus());
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
        List<ErpProductDTO> productList = erpProductApi.validProductList(
            convertSet(list, ErpPurchaseReturnSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDTO> productMap = convertMap(productList, ErpProductDTO::getId);
        // 1.1 校验入库项存在
        //收集inItemId集合
        Set<Long> inItemIdSet = list.stream().map(ErpPurchaseReturnSaveReqVO.Item::getInItemId).collect(Collectors.toSet());
        for (Long aLong : inItemIdSet) {
            ErpPurchaseInItemDO inItemDO = inItemMapper.selectById(aLong);
            ThrowUtil.ifThrow(inItemDO == null, PURCHASE_IN_ITEM_NOT_EXISTS, aLong);
        }
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
            purchaseReturnItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            purchaseReturnItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            purchaseReturnItemMapper.deleteBatchIds(convertList(diffList.get(2), ErpPurchaseReturnItemDO::getId));
        }
    }

    /**
     * 联动订单项
     *
     * @param inItemDO ErpPurchaseInItemDO
     * @param number   BigDecimal
     */
    private void syncCountLogic(ErpPurchaseInItemDO inItemDO, BigDecimal number) {
        Long orderItemId = inItemDO.getOrderItemId();
        ErpPurchaseOrderItemDO orderItemDO = orderItemMapper.selectById(orderItemId);

        orderItemStorageMachine.fireEvent(ErpStorageStatus.fromCode(orderItemDO.getInStatus()), SrmEventEnum.STOCK_ADJUSTMENT,
            SrmInCountDTO.builder().orderItemId(orderItemId)
                .returnCount(number).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseReturn(List<Long> ids) {
        List<ErpPurchaseReturnDO> purchaseReturns = purchaseReturnMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(purchaseReturns)) {
            return;
        }
        //1. 校验已审核
        purchaseReturns.forEach(purchaseReturn -> ThrowUtil.ifThrow(validAudit(purchaseReturn.getId()), PURCHASE_RETURN_DELETE_FAIL_APPROVE));

        //1.2 回滚数据状态
        for (ErpPurchaseReturnDO purchaseReturn : purchaseReturns) {
            List<ErpPurchaseReturnItemDO> itemDOS = purchaseReturnItemMapper.selectListByReturnId(purchaseReturn.getId());
            rollBackStatus(itemDOS);
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
        if (ObjectUtil.notEqual(purchaseReturn.getAuditStatus(), SrmAuditStatus.APPROVED.getCode())) {
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
            auditStatusMachine.fireEvent(SrmAuditStatus.fromCode(returnDO.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, ErpPurchaseReturnAuditReqVO.builder().ids(Collections.singletonList(returnDO.getId())).build());
        });
    }

    @Override
    public void review(ErpPurchaseReturnAuditReqVO req) {
        // 查询退货单信息
        req.getIds().stream().findFirst().ifPresent(id -> {
            ErpPurchaseReturnDO purchaseReturnDO = purchaseReturnMapper.selectById(id);
            if (purchaseReturnDO == null) {
                throw exception(PURCHASE_RETURN_NOT_EXISTS);
            }
            // 获取当前订单状态
            SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(purchaseReturnDO.getAuditStatus());
            if (Boolean.TRUE.equals(req.getReviewed())) {
                List<ErpPurchaseReturnItemDO> returnItemDOS = purchaseReturnItemMapper.selectListByReturnId(id);
                // 审核操作
                if (req.getPass()) {
                    log.debug("退货单通过审核，ID: {}", purchaseReturnDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, req);
                    //联动
                    linkSlaveStatus(returnItemDOS);
                } else {
                    //反审核
                    log.debug("退货单拒绝审核，ID: {}", purchaseReturnDO.getId());
                    auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, req);
                    //联动
                    rollBackStatus(returnItemDOS);
                }
            } else {
                log.debug("退货单撤回审核，ID: {}", purchaseReturnDO.getId());
                auditStatusMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
            }
        });
    }

    private void rollBackStatus(List<ErpPurchaseReturnItemDO> returnItemDOS) {
        Optional.ofNullable(returnItemDOS).ifPresent(item -> {
            item.forEach(itemDO ->
                syncCountLogic(inItemMapper.selectById(itemDO.getInItemId()), itemDO.getCount().negate()));
        });
    }

    @Override
    public void refund(ErpPurchaseReturnAuditReqVO vo) {
        // 校验，已审核才可以退款
        vo.getIds().stream().distinct().forEach(id -> {
            ErpPurchaseReturnDO returnDO = purchaseReturnMapper.selectById(id);
            if (returnDO == null) {
                throw exception(PURCHASE_RETURN_NOT_EXISTS);
            }
            if (!SrmAuditStatus.APPROVED.getCode().equals(returnDO.getAuditStatus())) {
                throw exception(PURCHASE_RETURN_PROCESS_FAIL_PAYMENT_STATUS);
            }
        });
        SrmEventEnum eventEnum = null;
        if (vo.getPass()) {
            eventEnum = RETURN_COMPLETE;
        } else {
            eventEnum = RETURN_CANCEL;
        }
        SrmEventEnum finalEventEnum = eventEnum;
        vo.getIds().stream().distinct().forEach(
            id -> {
                ErpPurchaseReturnDO orderDO = purchaseReturnMapper.selectById(id);
                //校验
                refundStateMachine.fireEvent(ErpReturnStatus.fromCode(orderDO.getRefundStatus()), finalEventEnum, orderDO);
            }
        );
    }
}
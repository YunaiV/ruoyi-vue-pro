package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleDeliveryReqVO;
import cn.iocoder.yudao.module.trade.convert.aftersale.TradeAfterSaleConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 交易售后 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TradeAfterSaleServiceImpl implements TradeAfterSaleService {

    @Resource
    private TradeOrderService tradeOrderService;

    @Resource
    private TradeAfterSaleMapper tradeAfterSaleMapper;

    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAfterSale(Long userId, AppTradeAfterSaleCreateReqVO createReqVO) {
        // 第一步，前置校验
        TradeOrderItemDO tradeOrderItem = validateOrderItemApplicable(userId, createReqVO);

        // 第二步，存储交易售后
        TradeAfterSaleDO afterSale = createAfterSale(createReqVO, tradeOrderItem);
        return afterSale.getId();
    }

    private TradeOrderItemDO validateOrderItemApplicable(Long userId, AppTradeAfterSaleCreateReqVO createReqVO) {
        // 校验订单项存在
        TradeOrderItemDO orderItem = tradeOrderService.getOrderItem(userId, createReqVO.getOrderItemId());
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }

        // 已申请售后，不允许再发起售后申请
        if (!TradeOrderItemAfterSaleStatusEnum.isNone(orderItem.getAfterSaleStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED);
        }
        // TODO 芋艿：超过一定时间，不允许售后

        // 申请的退款金额，不能超过商品的价格
        if (createReqVO.getApplyPrice() > orderItem.getOrderDividePrice()) {
            throw exception(AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR);
        }

        // 校验订单存在
        TradeOrderDO order = tradeOrderService.getOrder(userId, orderItem.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 已取消，无法发起售后
        if (TradeOrderStatusEnum.isCanceled(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED);
        }
        // 未支付，无法发起售后
        if (!TradeOrderStatusEnum.havePaid(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID);
        }
        // 如果是【退货退款】的情况，需要额外校验是否发货
        if (createReqVO.getType().equals(TradeAfterSaleTypeEnum.RETURN_AND_REFUND.getType())
            && !TradeOrderStatusEnum.haveDelivered(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED);
        }
        return orderItem;
    }

    private TradeAfterSaleDO createAfterSale(AppTradeAfterSaleCreateReqVO createReqVO,
                                             TradeOrderItemDO tradeOrderItem) {
        // 创建售后单
        TradeAfterSaleDO afterSale = TradeAfterSaleConvert.INSTANCE.convert(createReqVO, tradeOrderItem);
        afterSale.setNo(RandomUtil.randomString(10)); // TODO 芋艿：优化 no 生成逻辑
        afterSale.setStatus(TradeAfterSaleStatusEnum.APPLY.getStatus());
        // TODO 退还积分
        tradeAfterSaleMapper.insert(afterSale);

        // 更新交易订单项的售后状态
        tradeOrderService.updateOrderItemAfterSaleStatus(tradeOrderItem.getId(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus());

        // TODO 记录售后日志

        // TODO 发送售后消息
        return afterSale;
    }

    @Override
    @Transactional
    public void auditAfterSale(Long userId, String userIp,
                               TradeAfterSaleAuditReqVO auditReqVO) {
        // 校验售后单存在，并状态未审批
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(auditReqVO.getId());
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (afterSale.getStatus().equals(TradeAfterSaleStatusEnum.APPLY.getStatus())) {
            throw exception(AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY);
        }

        // 进行审批
        if (auditReqVO.getAudit()) {
            auditAfterSalePass(userId, userIp, auditReqVO, afterSale);
        } else {
            auditAfterSaleReject(userId, auditReqVO, afterSale);
        }
    }

    private void auditAfterSalePass(Long userId, String userIp,
                                    TradeAfterSaleAuditReqVO auditReqVO, TradeAfterSaleDO afterSale) {
        // 更新售后单的状态
        // 情况一：退款：标记为 WAIT_REFUND 状态。后续等退款发起成功后，在标记为 COMPLETE 状态
        // 情况二：退货退款：需要等用户退货后，才能发起退款
        Integer newStatus = afterSale.getType().equals(TradeAfterSaleTypeEnum.REFUND.getType()) ?
                TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus() : TradeAfterSaleStatusEnum.SELLER_PASS.getStatus();
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.APPLY.getStatus(), new TradeAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId)
                .setAuditReason(auditReqVO.getAuditReason()).setAuditTime(LocalDateTime.now()));

        // 如果直接退款，则发起售后退款
        if (afterSale.getType().equals(TradeAfterSaleTypeEnum.REFUND.getType())) {
            createPayRefund(userIp, afterSale);
        }

        // TODO 记录售后日志

        // TODO 发送售后消息
    }

    private void auditAfterSaleReject(Long userId,
                                      TradeAfterSaleAuditReqVO auditReqVO, TradeAfterSaleDO afterSale) {
        // 更新售后单的状态
        Integer newStatus = TradeAfterSaleStatusEnum.SELLER_REFUSE.getStatus();
                updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.APPLY.getStatus(), new TradeAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId)
                .setAuditReason(auditReqVO.getAuditReason()).setAuditTime(LocalDateTime.now()));

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderService.updateOrderItemAfterSaleStatus(afterSale.getOrderItemId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(), TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());

        // TODO 记录售后日志

        // TODO 发送售后消息
    }

    private void createPayRefund(String userIp, TradeAfterSaleDO afterSale) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                // 创建退款单
                PayRefundCreateReqDTO createReqDTO = TradeAfterSaleConvert.INSTANCE.convert(userIp, afterSale, tradeOrderProperties);
                Long payRefundId = payRefundApi.createPayRefund(createReqDTO);
                // 更新售后单的退款单号
                tradeAfterSaleMapper.updateById(new TradeAfterSaleDO().setId(afterSale.getId()).setPayRefundId(payRefundId));
            }
        });
    }

    private void updateAfterSaleStatus(Long id, Integer status, TradeAfterSaleDO updateObj) {
        int updateCount = tradeAfterSaleMapper.updateByIdAndStatus(id, status, updateObj);
        if (updateCount == 0) {
            throw exception(AFTER_SALE_UPDATE_STATUS_FAIL);
        }
    }

    @Override
    public void deliveryAfterSale(Long userId, AppTradeAfterSaleDeliveryReqVO deliveryReqVO) {
        // 校验售后单存在，并状态未退货
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(deliveryReqVO.getId());
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (afterSale.getStatus().equals(TradeAfterSaleStatusEnum.SELLER_PASS.getStatus())) {
            throw exception(AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_BUYER_RETURN);
        }

        // 更新售后单的物流信息
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.SELLER_PASS.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.BUYER_RETURN.getStatus())
                .setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo())
                .setDeliveryTime(deliveryReqVO.getDeliveryTime()));

        // TODO 记录售后日志

        // TODO 发送售后消息
    }

}

package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleDisagreeReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSalePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo.TradeAfterSaleRefuseReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppTradeAfterSaleDeliveryReqVO;
import cn.iocoder.yudao.module.trade.convert.aftersale.TradeAfterSaleConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.TradeAfterSaleLogDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleLogMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.TradeAfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleWayEnum;
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
    private TradeAfterSaleLogMapper tradeAfterSaleLogMapper;

    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Override
    public PageResult<TradeAfterSaleDO> getAfterSalePage(TradeAfterSalePageReqVO pageReqVO) {
        return tradeAfterSaleMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAfterSale(Long userId, AppTradeAfterSaleCreateReqVO createReqVO) {
        // 第一步，前置校验
        TradeOrderItemDO tradeOrderItem = validateOrderItemApplicable(userId, createReqVO);

        // 第二步，存储交易售后
        TradeAfterSaleDO afterSale = createAfterSale(createReqVO, tradeOrderItem);
        return afterSale.getId();
    }

    /**
     * 校验交易订单项是否可以申请售后
     *
     * @param userId 用户编号
     * @param createReqVO 售后创建信息
     * @return 交易订单项
     */
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

        // 申请的退款金额，不能超过商品的价格
        if (createReqVO.getRefundPrice() > orderItem.getOrderDividePrice()) {
            throw exception(AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR);
        }

        // 校验订单存在
        TradeOrderDO order = tradeOrderService.getOrder(userId, orderItem.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // TODO 芋艿：超过一定时间，不允许售后
        // 已取消，无法发起售后
        if (TradeOrderStatusEnum.isCanceled(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED);
        }
        // 未支付，无法发起售后
        if (!TradeOrderStatusEnum.havePaid(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID);
        }
        // 如果是【退货退款】的情况，需要额外校验是否发货
        if (createReqVO.getWay().equals(TradeAfterSaleWayEnum.RETURN_AND_REFUND.getWay())
            && !TradeOrderStatusEnum.haveDelivered(order.getStatus())) {
            throw exception(AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED);
        }
        return orderItem;
    }

    private TradeAfterSaleDO createAfterSale(AppTradeAfterSaleCreateReqVO createReqVO,
                                             TradeOrderItemDO orderItem) {
        // 创建售后单
        TradeAfterSaleDO afterSale = TradeAfterSaleConvert.INSTANCE.convert(createReqVO, orderItem);
        afterSale.setNo(RandomUtil.randomString(10)); // TODO 芋艿：优化 no 生成逻辑
        afterSale.setStatus(TradeAfterSaleStatusEnum.APPLY.getStatus());
        // 标记是售中还是售后
        TradeOrderDO order = tradeOrderService.getOrder(orderItem.getUserId(), orderItem.getOrderId());
        afterSale.setOrderNo(order.getNo()); // 记录 orderNo 订单流水，方便后续检索
        afterSale.setType(TradeOrderStatusEnum.isCompleted(order.getStatus())
            ? TradeAfterSaleTypeEnum.AFTER_SALE.getType() : TradeAfterSaleTypeEnum.IN_SALE.getType());
        // TODO 退还积分
        tradeAfterSaleMapper.insert(afterSale);

        // 更新交易订单项的售后状态
        tradeOrderService.updateOrderItemAfterSaleStatus(orderItem.getId(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(), null);

        // 记录售后日志
        createAfterSaleLog(orderItem.getUserId(), UserTypeEnum.MEMBER.getValue(),
                afterSale, null, afterSale.getStatus());

        // TODO 发送售后消息
        return afterSale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeAfterSale(Long userId, Long id) {
        // 校验售后单存在，并状态未审批
        TradeAfterSaleDO afterSale = validateAfterSaleAuditable(id);

        // 更新售后单的状态
        // 情况一：退款：标记为 WAIT_REFUND 状态。后续等退款发起成功后，在标记为 COMPLETE 状态
        // 情况二：退货退款：需要等用户退货后，才能发起退款
        Integer newStatus = afterSale.getType().equals(TradeAfterSaleWayEnum.REFUND.getWay()) ?
                TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus() : TradeAfterSaleStatusEnum.SELLER_AGREE.getStatus();
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.APPLY.getStatus(), new TradeAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId).setAuditTime(LocalDateTime.now()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.ADMIN.getValue(),
                afterSale, afterSale.getStatus(), newStatus);

        // TODO 发送售后消息
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disagreeAfterSale(Long userId, TradeAfterSaleDisagreeReqVO auditReqVO) {
        // 校验售后单存在，并状态未审批
        TradeAfterSaleDO afterSale = validateAfterSaleAuditable(auditReqVO.getId());

        // 更新售后单的状态
        Integer newStatus = TradeAfterSaleStatusEnum.SELLER_DISAGREE.getStatus();
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.APPLY.getStatus(), new TradeAfterSaleDO()
                .setStatus(newStatus).setAuditUserId(userId).setAuditTime(LocalDateTime.now())
                .setAuditReason(auditReqVO.getAuditReason()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.ADMIN.getValue(),
                afterSale, afterSale.getStatus(), newStatus);

        // TODO 发送售后消息

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderService.updateOrderItemAfterSaleStatus(afterSale.getOrderItemId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    /**
     * 校验售后单是否可审批（同意售后、拒绝售后）
     *
     * @param id 售后编号
     * @return 售后单
     */
    private TradeAfterSaleDO validateAfterSaleAuditable(Long id) {
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), TradeAfterSaleStatusEnum.APPLY.getStatus())) {
            throw exception(AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY);
        }
        return afterSale;
    }

    private void updateAfterSaleStatus(Long id, Integer status, TradeAfterSaleDO updateObj) {
        int updateCount = tradeAfterSaleMapper.updateByIdAndStatus(id, status, updateObj);
        if (updateCount == 0) {
            throw exception(AFTER_SALE_UPDATE_STATUS_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliveryAfterSale(Long userId, AppTradeAfterSaleDeliveryReqVO deliveryReqVO) {
        // 校验售后单存在，并状态未退货
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(deliveryReqVO.getId());
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), TradeAfterSaleStatusEnum.SELLER_AGREE.getStatus())) {
            throw exception(AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE);
        }

        // 更新售后单的物流信息
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.SELLER_AGREE.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())
                .setLogisticsId(deliveryReqVO.getLogisticsId()).setLogisticsNo(deliveryReqVO.getLogisticsNo())
                .setDeliveryTime(deliveryReqVO.getDeliveryTime()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.MEMBER.getValue(),
                afterSale, afterSale.getStatus(), TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus());

        // TODO 发送售后消息
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveAfterSale(Long userId, Long id) {
        // 校验售后单存在，并状态为已退货
        TradeAfterSaleDO afterSale = validateAfterSaleReceivable(id);

        // 更新售后单的状态
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus()).setReceiveTime(LocalDateTime.now()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.ADMIN.getValue(),
                afterSale, afterSale.getStatus(), TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus());

        // TODO 发送售后消息
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuseAfterSale(Long userId, TradeAfterSaleRefuseReqVO refuseReqVO) {
        // 校验售后单存在，并状态为已退货
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(refuseReqVO.getId());
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())) {
            throw exception(AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY);
        }

        // 更新售后单的状态
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.SELLER_REFUSE.getStatus()).setReceiveTime(LocalDateTime.now())
                .setReceiveReason(refuseReqVO.getRefuseMemo()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.ADMIN.getValue(),
                afterSale, afterSale.getStatus(), TradeAfterSaleStatusEnum.SELLER_REFUSE.getStatus());

        // TODO 发送售后消息

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderService.updateOrderItemAfterSaleStatus(afterSale.getOrderItemId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    /**
     * 校验售后单是否可收货，即处于买家已发货
     *
     * @param id 售后编号
     * @return 售后单
     */
    private TradeAfterSaleDO validateAfterSaleReceivable(Long id) {
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), TradeAfterSaleStatusEnum.BUYER_DELIVERY.getStatus())) {
            throw exception(AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY);
        }
        return afterSale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundAfterSale(Long userId, String userIp, Long id) {
        // 校验售后单的状态，并状态待退款
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectByPayRefundId(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(afterSale.getStatus(), TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus())) {
            throw exception(AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND);
        }

        // 发起退款单。注意，需要在事务提交后，再进行发起，避免重复发起
        createPayRefund(userIp, afterSale);

        // 更新售后单的状态为【已完成】
        updateAfterSaleStatus(afterSale.getId(), TradeAfterSaleStatusEnum.WAIT_REFUND.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.COMPLETE.getStatus()).setRefundTime(LocalDateTime.now()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.ADMIN.getValue(),
                afterSale, afterSale.getStatus(), TradeAfterSaleStatusEnum.COMPLETE.getStatus());

        // TODO 发送售后消息

        // 更新交易订单项的售后状态为【已完成】
        tradeOrderService.updateOrderItemAfterSaleStatus(afterSale.getOrderItemId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus(), afterSale.getRefundPrice());
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

    @Override
    public void cancelAfterSale(Long userId, Long id) {
        // 校验售后单的状态，并状态待退款
        TradeAfterSaleDO afterSale = tradeAfterSaleMapper.selectByPayRefundId(id);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        if (ObjectUtils.equalsAny(afterSale.getStatus(), TradeAfterSaleStatusEnum.APPLY.getStatus(),
                TradeAfterSaleStatusEnum.SELLER_AGREE.getStatus())) {
            throw exception(AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE);
        }

        // 更新售后单的状态为【已取消】
        updateAfterSaleStatus(afterSale.getId(), afterSale.getStatus(), new TradeAfterSaleDO()
                .setStatus(TradeAfterSaleStatusEnum.BUYER_CANCEL.getStatus()));

        // 记录售后日志
        createAfterSaleLog(userId, UserTypeEnum.MEMBER.getValue(),
                afterSale, afterSale.getStatus(), TradeAfterSaleStatusEnum.BUYER_CANCEL.getStatus());

        // TODO 发送售后消息

        // 更新交易订单项的售后状态为【未申请】
        tradeOrderService.updateOrderItemAfterSaleStatus(afterSale.getOrderItemId(),
                TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
                TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    private void createAfterSaleLog(Long userId, Integer userType, TradeAfterSaleDO afterSale,
                                    Integer beforeStatus, Integer afterStatus) {
        TradeAfterSaleLogDO afterSaleLog = new TradeAfterSaleLogDO().setUserId(userId).setUserType(userType)
                .setAfterSaleId(afterSale.getId()).setOrderId(afterSale.getOrderId())
                .setOrderItemId(afterSale.getOrderItemId()).setBeforeStatus(beforeStatus).setAfterStatus(afterStatus)
                .setContent(TradeAfterSaleStatusEnum.valueOf(afterStatus).getContent());
        tradeAfterSaleLogMapper.insert(afterSaleLog);
    }

}

package cn.iocoder.yudao.module.pay.service.blockchain.event;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 支付成功事件
 * 由支付回调处理完成后通过 ApplicationEventPublisher 发布
 *
 * @author deepay
 */
@Getter
public class PaymentSuccessEvent {

    private final String orderId;
    private final BigDecimal amount;
    private final String currency;
    private final String paymentTime;
    private final String userId;
    private final String productInfo;
    private final String transactionId;

    public PaymentSuccessEvent(String orderId, BigDecimal amount, String currency,
                               String paymentTime, String userId, String productInfo,
                               String transactionId) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentTime = paymentTime;
        this.userId = userId;
        this.productInfo = productInfo;
        this.transactionId = transactionId;
    }

}

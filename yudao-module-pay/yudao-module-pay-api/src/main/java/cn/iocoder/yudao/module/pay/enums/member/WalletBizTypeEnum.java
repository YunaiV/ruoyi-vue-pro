package cn.iocoder.yudao.module.pay.enums.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包交易业务分类
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletBizTypeEnum {
    RECHARGE(1, "充值"),
    RECHARGE_REFUND(2, "充值退款"),
    PAYMENT(3, "支付"),
    PAYMENT_REFUND(4, "支付退款");

    // TODO 后续增加
    /**
     * 业务分类
     */
    private final Integer bizType;

    /**
     * 说明
     */
    private final String desc;
}

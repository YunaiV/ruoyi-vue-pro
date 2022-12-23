package cn.iocoder.yudao.module.trade.enums.refund;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易退款 - 申请类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeRefundTypeEnum {

    REFUND(10, "退款"),
    RETURN_AND_REFUND(20, "退货退款");

    /**
     * 状态值
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

}

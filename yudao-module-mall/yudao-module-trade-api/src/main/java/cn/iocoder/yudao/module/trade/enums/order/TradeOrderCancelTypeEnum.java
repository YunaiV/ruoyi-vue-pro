package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单 - 关闭类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderCancelTypeEnum {

    PAY_TIMEOUT(10, "超时未支付"),
    REFUND_CLOSE(20, "退款关闭"),
    MEMBER_CANCEL(30, "买家取消"),
    PAY_ON_DELIVERY(40, "已通过货到付款交易"),;

    /**
     * 关闭类型
     */
    private final Integer type;
    /**
     * 关闭类型名
     */
    private final String name;

}

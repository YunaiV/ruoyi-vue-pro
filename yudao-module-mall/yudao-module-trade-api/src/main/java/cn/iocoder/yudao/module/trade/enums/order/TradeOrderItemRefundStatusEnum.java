package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单项 - 退款状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderItemRefundStatusEnum {

    NONE(0, "未申请退款"),
    APPLY(1, "申请退款"),
    WAIT(2, "等待退款"),
    SUCCESS(3, "退款成功");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

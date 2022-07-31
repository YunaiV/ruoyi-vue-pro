package cn.iocoder.yudao.module.trade.enums.refund;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易退款 - 状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeRefundStatusEnum {

    NONE(0, "未退款"),
    AUDIT(10, "审核中"),
    APPROVE(30, "已通过"),
    REJECT(40, "不通过"),;

    /**
     * 状态值
     */
    private final Integer value;
    /**
     * 状态名
     */
    private final String name;

}

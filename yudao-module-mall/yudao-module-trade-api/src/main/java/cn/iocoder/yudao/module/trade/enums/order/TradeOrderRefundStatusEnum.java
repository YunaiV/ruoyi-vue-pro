package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单 - 退款状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderRefundStatusEnum {

    NONE(0, "未退款"),
    PART(1, "部分退款"),
    ALL(2, "全部退款");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

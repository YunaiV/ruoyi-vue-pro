package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单 - 状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderStatusEnum {

    WAITING_PAYMENT(10, "待付款"),
    WAIT_SHIPMENT(20, "待发货"),
    ALREADY_SHIPMENT(30, "待收货"),
    WAITING_COMMENT(40, "待评价"),
    COMPLETED(50, "成功"),
    CLOSED(60, "失败");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

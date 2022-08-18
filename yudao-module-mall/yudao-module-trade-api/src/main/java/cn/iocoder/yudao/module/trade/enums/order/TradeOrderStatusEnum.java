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

    WAITING_PAYMENT(0, "待付款"),
    WAIT_SHIPMENT(1, "待发货"),
    ALREADY_SHIPMENT(2, "待收货"),
    COMPLETED(3, "已完成"),
    CANCEL(4, "已关闭");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

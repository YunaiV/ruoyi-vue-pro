package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单 - 发货状态
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderDeliveryStatusEnum {

    UNDELIVERED(0, "未发货"),
    DELIVERED(1, "已发货"),
    RECEIVED(2, "已收货");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

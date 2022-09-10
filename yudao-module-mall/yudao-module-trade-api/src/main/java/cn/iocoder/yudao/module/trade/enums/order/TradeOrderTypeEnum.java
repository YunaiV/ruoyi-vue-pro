package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易订单 - 类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderTypeEnum {

    NORMAL(0, "普通订单"),
    SECKILL(1, "秒杀订单"),
    TEAM(2, "拼团订单"),
    BARGAIN(3, "砍价订单");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

}

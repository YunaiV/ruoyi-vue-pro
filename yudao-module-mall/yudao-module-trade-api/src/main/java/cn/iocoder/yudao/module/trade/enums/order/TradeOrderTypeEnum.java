package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderTypeEnum implements IntArrayValuable {

    NORMAL(0, "普通订单"),
    SECKILL(1, "秒杀订单"),
    TEAM(2, "拼团订单"),
    BARGAIN(3, "砍价订单");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeOrderTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

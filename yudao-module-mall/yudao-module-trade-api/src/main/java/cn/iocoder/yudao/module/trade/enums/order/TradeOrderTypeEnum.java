package cn.iocoder.yudao.module.trade.enums.order;

import cn.hutool.core.util.ObjectUtil;
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
    BARGAIN(2, "砍价订单"),
    COMBINATION(3, "拼团订单"),
    POINT(4, "积分商城"),
    ;

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

    public static boolean isNormal(Integer type) {
        return ObjectUtil.equal(type, NORMAL.getType());
    }

    public static boolean isSeckill(Integer type) {
        return ObjectUtil.equal(type, SECKILL.getType());
    }

    public static boolean isBargain(Integer type) {
        return ObjectUtil.equal(type, BARGAIN.getType());
    }

    public static boolean isCombination(Integer type) {
        return ObjectUtil.equal(type, COMBINATION.getType());
    }

    public static boolean isPoint(Integer type) {
        return ObjectUtil.equal(type, POINT.getType());
    }

}

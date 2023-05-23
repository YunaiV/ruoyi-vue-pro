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
    // TODO 芋艿：如下三个字段，名字需要改下，等后面表设计完成后。
    KANJIA(2, "砍价订单"),
    PINTUAN(3, "拼团订单"),
    YUSHOU(4, "预售订单"),
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

}

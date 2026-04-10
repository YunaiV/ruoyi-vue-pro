package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 关闭类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderCancelTypeEnum implements ArrayValuable<Integer> {

    PAY_TIMEOUT(10, "超时未支付"),
    AFTER_SALE_CLOSE(20, "退款关闭"),
    MEMBER_CANCEL(30, "买家取消"),
    COMBINATION_CLOSE(40, "拼团关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(TradeOrderCancelTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 关闭类型
     */
    private final Integer type;
    /**
     * 关闭类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

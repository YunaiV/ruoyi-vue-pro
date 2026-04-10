package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 退款状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderRefundStatusEnum implements ArrayValuable<Integer> {

    NONE(0, "未退款"),
    PART(10, "部分退款"),
    ALL(20, "全部退款");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(TradeOrderRefundStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

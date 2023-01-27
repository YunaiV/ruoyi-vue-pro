package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 售后状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderAfterSaleStatusEnum implements IntArrayValuable {

    NONE(0, "未退款"),
    PART(1, "部分退款"),
    ALL(2, "全部退款");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeOrderAfterSaleStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

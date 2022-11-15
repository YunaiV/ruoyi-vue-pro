package cn.iocoder.yudao.module.trade.enums.aftersale;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易售后 - 类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeAfterSaleTypeEnum implements IntArrayValuable {

    REFUND(10, "退款"),
    RETURN_AND_REFUND(20, "退货退款");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeAfterSaleTypeEnum::getType).toArray();

    /**
     * 状态值
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

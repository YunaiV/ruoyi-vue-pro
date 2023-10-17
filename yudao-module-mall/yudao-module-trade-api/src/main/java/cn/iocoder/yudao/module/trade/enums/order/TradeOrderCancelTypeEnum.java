package cn.iocoder.yudao.module.trade.enums.order;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum TradeOrderCancelTypeEnum implements IntArrayValuable {

    PAY_TIMEOUT(10, "超时未支付"),
    AFTER_SALE_CLOSE(20, "退款关闭"),
    MEMBER_CANCEL(30, "买家取消");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeOrderCancelTypeEnum::getType).toArray();

    /**
     * 关闭类型
     */
    private final Integer type;
    /**
     * 关闭类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

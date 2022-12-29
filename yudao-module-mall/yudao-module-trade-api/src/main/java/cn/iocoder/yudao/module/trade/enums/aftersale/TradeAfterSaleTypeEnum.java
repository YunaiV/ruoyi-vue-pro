package cn.iocoder.yudao.module.trade.enums.aftersale;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易售后 - 类型
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum TradeAfterSaleTypeEnum implements IntArrayValuable {

    IN_SALE(10, "售中退款"), // 交易完成前买家申请退款
    AFTER_SALE(20, "售后退款"); // 交易完成后买家申请退款

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeAfterSaleTypeEnum::getType).toArray();

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

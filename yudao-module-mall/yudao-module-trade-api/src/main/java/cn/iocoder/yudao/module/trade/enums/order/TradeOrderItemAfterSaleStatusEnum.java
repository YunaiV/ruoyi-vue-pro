package cn.iocoder.yudao.module.trade.enums.order;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单项 - 售后状态
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderItemAfterSaleStatusEnum implements IntArrayValuable {

    NONE(0, "未售后"),
    APPLY(1, "售后中"),
    SUCCESS(2, "已退款");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeOrderItemAfterSaleStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    // TODO 芋艿：EXPIRED 已失效不允许申请售后
    // TODO 芋艿：PART_AFTER_SALE 部分售后

    @Override
    public int[] array() {
        return ARRAYS;
    }

    /**
     * 判断指定状态，是否正处于【未申请】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isNone(Integer status) {
        return ObjectUtil.equals(status, NONE.getStatus());
    }

}

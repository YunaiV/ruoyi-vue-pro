package cn.iocoder.yudao.module.trade.enums.order;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
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
public enum TradeOrderItemAfterSaleStatusEnum implements ArrayValuable<Integer> {

    NONE(0, "未售后"),
    APPLY(10, "售后中"),
    SUCCESS(20, "售后成功");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(TradeOrderItemAfterSaleStatusEnum::getStatus).toArray(Integer[]::new);

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

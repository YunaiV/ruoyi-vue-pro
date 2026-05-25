package cn.iocoder.yudao.module.trade.enums.order;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 采购状态
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderPurchaseStatusEnum implements ArrayValuable<Integer> {

    NONE(0, "未采购"),
    PICKED(10, "已拣货"),
    ORDERED(20, "已下单"),
    LOGISTICS_SYNCED(30, "已同步物流");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(TradeOrderPurchaseStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isNone(Integer status) {
        return ObjectUtil.equal(NONE.getStatus(), status);
    }

    public static boolean canMarkPicked(Integer status) {
        return ObjectUtils.equalsAny(status, NONE.getStatus());
    }

    public static boolean canMarkOrdered(Integer status) {
        return ObjectUtils.equalsAny(status, NONE.getStatus(), PICKED.getStatus());
    }

}

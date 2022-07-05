package cn.iocoder.yudao.module.market.enums.activity;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;

import java.util.Arrays;

/**
 * 促销活动状态枚举
 */
public enum MarketActivityStatusEnum implements IntArrayValuable {

    WAIT(10, "未开始"),
    RUN(20, "进行中"),
    END(30, "已结束"),
    /**
     * 1. WAIT、RUN、END 可以转换成 INVALID 状态。
     * 2. INVALID 只可以转换成 DELETED 状态。
     */
    INVALID(40, "已撤销"),
    DELETED(50, "已删除"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(MarketActivityStatusEnum::getValue).toArray();

    /**
     * 状态值
     */
    private final Integer value;
    /**
     * 状态名
     */
    private final String name;

    MarketActivityStatusEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}

package cn.iocoder.yudao.framework.common.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 时间间隔类型枚举
 *
 * @author dhb52
 */
@Getter
@AllArgsConstructor
public enum DateIntervalEnum implements IntArrayValuable {

    TODAY(1, "今天"),
    YESTERDAY(2, "昨天"),
    THIS_WEEK(3, "本周"),
    LAST_WEEK(4, "上周"),
    THIS_MONTH(5, "本月"),
    LAST_MONTH(6, "上月"),
    THIS_QUARTER(7, "本季度"),
    LAST_QUARTER(8, "上季度"),
    THIS_YEAR(9, "本年"),
    LAST_YEAR(10, "去年"),
    CUSTOMER(11, "自定义"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DateIntervalEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
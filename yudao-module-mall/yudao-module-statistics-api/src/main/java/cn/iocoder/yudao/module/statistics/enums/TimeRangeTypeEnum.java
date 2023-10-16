package cn.iocoder.yudao.module.statistics.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 时间范围类型的枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum TimeRangeTypeEnum implements IntArrayValuable {

    /**
     * 天
     */
    DAY(1),
    /**
     * 周
     */
    WEEK(7),
    /**
     * 月
     */
    MONTH(30),
    /**
     * 年
     */
    YEAR(365),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TimeRangeTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}

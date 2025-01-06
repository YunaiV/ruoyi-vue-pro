package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 延迟器类型枚举
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmDelayTimerType implements IntArrayValuable {

    FIXED_TIME_DURATION(1, "固定时长"),
    FIXED_DATE_TIME(2, "固定日期");

    private final Integer type;
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmDelayTimerType::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
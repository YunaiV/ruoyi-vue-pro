package cn.iocoder.yudao.module.hrm.enums.attendance.record;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 考勤打卡阶段枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceClockStageEnum implements ArrayValuable<Integer> {

    FIRST(1, "第一段");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceClockStageEnum::getStage).toArray(Integer[]::new);

    /**
     * 阶段
     */
    private final Integer stage;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceClockStageEnum valueOf(Integer stage) {
        return ArrayUtil.firstMatch(item -> item.getStage().equals(stage), values());
    }

}

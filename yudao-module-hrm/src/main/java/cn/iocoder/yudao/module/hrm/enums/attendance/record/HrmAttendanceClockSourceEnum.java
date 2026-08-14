package cn.iocoder.yudao.module.hrm.enums.attendance.record;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 考勤打卡来源
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceClockSourceEnum implements ArrayValuable<Integer> {

    MOBILE(1, "手机端"),
    MANUAL(2, "手工录入");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceClockSourceEnum::getSource).toArray(Integer[]::new);

    /**
     * 来源
     */
    private final Integer source;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceClockSourceEnum valueOf(Integer source) {
        return ArrayUtil.firstMatch(item -> item.getSource().equals(source), values());
    }

}

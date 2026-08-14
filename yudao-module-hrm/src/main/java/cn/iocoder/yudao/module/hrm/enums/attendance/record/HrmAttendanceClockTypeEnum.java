package cn.iocoder.yudao.module.hrm.enums.attendance.record;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 考勤打卡类型
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceClockTypeEnum implements ArrayValuable<Integer> {

    ON_DUTY(1, "上班打卡"),
    OFF_DUTY(2, "下班打卡");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceClockTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceClockTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}

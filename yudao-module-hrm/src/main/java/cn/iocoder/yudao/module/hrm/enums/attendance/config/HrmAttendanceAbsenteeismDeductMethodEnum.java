package cn.iocoder.yudao.module.hrm.enums.attendance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 考勤旷工扣款方式
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceAbsenteeismDeductMethodEnum implements ArrayValuable<Integer> {

    BY_DAY(1, "按旷工天数扣款");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceAbsenteeismDeductMethodEnum::getMethod).toArray(Integer[]::new);

    private final Integer method;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceAbsenteeismDeductMethodEnum valueOf(Integer method) {
        return ArrayUtil.firstMatch(item -> item.getMethod().equals(method), values());
    }

}

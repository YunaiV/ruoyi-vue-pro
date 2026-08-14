package cn.iocoder.yudao.module.hrm.enums.attendance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 考勤缺卡扣款方式
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceMisscardDeductMethodEnum implements ArrayValuable<Integer> {

    BY_COUNT(1, "按缺卡次数扣款");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceMisscardDeductMethodEnum::getMethod).toArray(Integer[]::new);

    private final Integer method;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceMisscardDeductMethodEnum valueOf(Integer method) {
        return ArrayUtil.firstMatch(item -> item.getMethod().equals(method), values());
    }

}

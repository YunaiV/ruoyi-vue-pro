package cn.iocoder.yudao.module.oa.enums.attendance;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 考勤类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaAttendanceTypeEnum implements ArrayValuable<Integer> {

    CLOCK_IN(1, "上班打卡"),
    CLOCK_OUT(2, "下班打卡"),
    LEAVE(3, "请假"),
    TRAVEL(4, "出差");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaAttendanceTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

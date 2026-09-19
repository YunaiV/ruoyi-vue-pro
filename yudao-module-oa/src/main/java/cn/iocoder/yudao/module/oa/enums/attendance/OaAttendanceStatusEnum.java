package cn.iocoder.yudao.module.oa.enums.attendance;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 考勤状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaAttendanceStatusEnum implements ArrayValuable<Integer> {

    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    EARLY(3, "早退"),
    LEAVE(4, "请假"),
    TRAVEL(5, "出差");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaAttendanceStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}

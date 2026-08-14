package cn.iocoder.yudao.module.hrm.enums.attendance.record;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工端打卡按钮状态
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceClockButtonStatusEnum implements ArrayValuable<Integer> {

    NOT_YET(0, "未到时间", false),
    NORMAL(1, "正常打卡", true),
    UPDATE(2, "更新打卡", true),
    LATE(3, "迟到打卡", true),
    EARLY(4, "早退打卡", true);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmAttendanceClockButtonStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;
    /**
     * 是否可以打卡
     */
    private final boolean canClock;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmAttendanceClockButtonStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}

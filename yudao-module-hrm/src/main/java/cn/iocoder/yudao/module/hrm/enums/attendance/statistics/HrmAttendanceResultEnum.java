package cn.iocoder.yudao.module.hrm.enums.attendance.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HRM 考勤结果枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmAttendanceResultEnum {

    NOT_SCHEDULED("未排班"),
    REST("休息"),
    FUTURE("未到日期"),
    LEAVE("请假"),
    LEAVE_DAYS("请假 %s 天"),
    ABSENTEEISM("旷工"),
    ABSENTEEISM_DAYS("旷工 %s 天"),
    MISS_CARD("缺卡 %s 次"),
    LATE("迟到 %s 分钟"),
    EARLY("早退 %s 分钟"),
    PENDING_CLOCK("待打卡"),
    NORMAL("正常");

    /**
     * 结果格式
     */
    private final String format;

    public String format(Object... args) {
        return String.format(format, args);
    }

}

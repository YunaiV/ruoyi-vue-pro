package cn.iocoder.yudao.framework.common.util.date;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间工具类，用于 {@link java.time.LocalDateTime}
 *
 * @author 芋道源码
 */
public class LocalDateTimeUtils {

    /**
     * 空的 LocalDateTime 对象，主要用于 DB 唯一索引的默认值
     */
    public static LocalDateTime EMPTY = buildTime(1970, 1, 1);

    public static LocalDateTime addTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

    public static boolean beforeNow(LocalDateTime date) {
        return date.isBefore(LocalDateTime.now());
    }

    public static boolean afterNow(LocalDateTime date) {
        return date.isAfter(LocalDateTime.now());
    }

    /**
     * 创建指定时间
     *
     * @param year  年
     * @param mouth 月
     * @param day   日
     * @return 指定时间
     */
    public static LocalDateTime buildTime(int year, int mouth, int day) {
        return LocalDateTime.of(year, mouth, day, 0, 0, 0);
    }

    public static LocalDateTime[] buildBetweenTime(int year1, int mouth1, int day1,
                                                   int year2, int mouth2, int day2) {
        return new LocalDateTime[]{buildTime(year1, mouth1, day1), buildTime(year2, mouth2, day2)};
    }

    /**
     * 判断当前时间是否在该时间范围内
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 是否
     */
    public static boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return LocalDateTimeUtil.isIn(LocalDateTime.now(), startTime, endTime);
    }

    /**
     * 检查时间重叠 不包含日期
     *
     * @param startTime1 需要校验的开始时间
     * @param endTime1   需要校验的结束时间
     * @param startTime2 校验所需的开始时间
     * @param endTime2   校验所需的结束时间
     * @return 是否重叠
     */
    public static boolean checkTimeOverlap(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
        // 判断时间是否重叠
        // 开始时间在已配置时段的结束时间之前 且 结束时间在已配置时段的开始时间之后 []
        return startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2)
                // 开始时间在已配置时段的开始时间之前 且 结束时间在已配置时段的开始时间之后 (] 或 ()
                || startTime1.isBefore(startTime2) && endTime1.isAfter(startTime2)
                // 开始时间在已配置时段的结束时间之前 且 结束时间在已配值时段的结束时间之后 [) 或 ()
                || startTime1.isBefore(endTime2) && endTime1.isAfter(endTime2);
    }

}

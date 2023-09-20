package cn.iocoder.yudao.framework.common.util.date;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.Duration;
import java.time.LocalDate;
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

    /**
     * 创建指定时间
     *
     * @param timeStr 时间字符串
     * @return 指定时间
     */
    public static LocalDateTime buildTime(String timeStr) {
        // TODO @puhui999：这个方法的实现，和 LocalDateTimeUtil.parse() 的差异点是啥呀
        return LocalDateTime.of(LocalDate.now(), LocalTime.parse(timeStr));
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
     * 判断当前时间是否在该时间范围内
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 是否
     */
    public static boolean isBetween(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        LocalDate nowDate = LocalDate.now();
        return LocalDateTimeUtil.isIn(LocalDateTime.now(), LocalDateTime.of(nowDate, LocalTime.parse(startTime)),
                LocalDateTime.of(nowDate, LocalTime.parse(endTime)));
    }

    /**
     * 判断时间段是否重叠
     *
     * @param startTime1 开始 time1
     * @param endTime1   结束 time1
     * @param startTime2 开始 time2
     * @param endTime2   结束 time2
     * @return 重叠：true 不重叠：false
     */
    public static boolean isOverlap(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
        LocalDate nowDate = LocalDate.now();
        return LocalDateTimeUtil.isOverlap(LocalDateTime.of(nowDate, startTime1), LocalDateTime.of(nowDate, endTime1),
                LocalDateTime.of(nowDate, startTime2), LocalDateTime.of(nowDate, endTime2));
    }

}

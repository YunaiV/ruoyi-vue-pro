package cn.iocoder.yudao.framework.common.util.date;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.DateIntervalEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.*;

/**
 * 时间工具类，用于 {@link LocalDate}、{@link LocalDateTime}
 *
 * @author 芋道源码
 */
public class LocalDateTimeUtils {

    /**
     * 空的 LocalDateTime 对象，主要用于 DB 唯一索引的默认值
     */
    public static LocalDateTime EMPTY = buildTime(1970, 1, 1);

    public static DateTimeFormatter UTC_MS_WITH_XXX_OFFSET_FORMATTER = createFormatter(UTC_MS_WITH_XXX_OFFSET_PATTERN);

    /**
     * 默认时区
     */
    private static final ZoneId DEFAULT_ZONE_ID = TimeZone.getTimeZone(DateUtils.TIME_ZONE_DEFAULT).toZoneId();

    /**
     * 解析时间
     *
     * 相比 {@link LocalDateTimeUtil#parse(CharSequence)} 方法来说，会尽量去解析，直到成功
     *
     * @param time 时间
     * @return 时间字符串
     */
    public static LocalDateTime parse(String time) {
        try {
            return LocalDateTimeUtil.parse(time, DatePattern.NORM_DATE_PATTERN);
        } catch (DateTimeParseException e) {
            return LocalDateTimeUtil.parse(time);
        }
    }

    /**
     * 解析年月字符串为 {@link YearMonth}，格式为 yyyy-MM
     *
     * @param month 年月字符串
     * @return 年月
     */
    public static YearMonth parseYearMonth(String month) {
        return YearMonth.parse(month, NORM_MONTH_FORMATTER);
    }

    public static LocalDateTime addTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

    public static LocalDateTime minusTime(Duration duration) {
        return LocalDateTime.now().minus(duration);
    }

    public static boolean beforeNow(LocalDateTime date) {
        return date.isBefore(LocalDateTime.now());
    }

    public static boolean afterNow(LocalDateTime date) {
        return date.isAfter(LocalDateTime.now());
    }

    public static boolean beforeOrEqualNow(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        return time.isBefore(now) || time.isEqual(now);
    }

    public static boolean beforeNow(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    public static boolean beforeOrEqualNow(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isBefore(today) || date.isEqual(today);
    }

    public static boolean afterNow(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    /**
     * 判断时间是否处于一月
     *
     * @param time 时间
     * @return 是否处于一月
     */
    public static boolean isJanuary(LocalDateTime time) {
        return time.getMonthValue() == 1;
    }

    /**
     * 判断第一个日期是否早于或等于第二个日期
     *
     * @param firstDate 第一个日期
     * @param secondDate 第二个日期
     * @return 是否早于或等于
     */
    public static boolean isBeforeOrEqual(LocalDate firstDate, LocalDate secondDate) {
        return firstDate.isBefore(secondDate) || firstDate.isEqual(secondDate);
    }

    /**
     * 判断第一个日期是否晚于或等于第二个日期
     *
     * @param firstDate 第一个日期
     * @param secondDate 第二个日期
     * @return 是否晚于或等于
     */
    public static boolean isAfterOrEqual(LocalDate firstDate, LocalDate secondDate) {
        return firstDate.isAfter(secondDate) || firstDate.isEqual(secondDate);
    }

    /**
     * 判断第一个时间是否早于或等于第二个时间
     *
     * @param firstTime 第一个时间
     * @param secondTime 第二个时间
     * @return 是否早于或等于
     */
    public static boolean isBeforeOrEqual(LocalDateTime firstTime, LocalDateTime secondTime) {
        return firstTime.isBefore(secondTime) || firstTime.isEqual(secondTime);
    }

    /**
     * 判断第一个时间是否晚于或等于第二个时间
     *
     * @param firstTime 第一个时间
     * @param secondTime 第二个时间
     * @return 是否晚于或等于
     */
    public static boolean isAfterOrEqual(LocalDateTime firstTime, LocalDateTime secondTime) {
        return firstTime.isAfter(secondTime) || firstTime.isEqual(secondTime);
    }

    /**
     * 判断时间范围是否有效
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 开始、结束时间非空，且结束时间晚于开始时间时返回 {@code true}
     */
    public static boolean isTimeRangeValid(LocalDateTime beginTime, LocalDateTime endTime) {
        return beginTime != null && endTime != null && endTime.isAfter(beginTime);
    }

    /**
     * 判断时间范围是否存在有效边界
     *
     * @param times 时间范围
     * @return 任一边界非空时返回 {@code true}
     */
    public static boolean isTimeRangePresent(LocalDateTime[] times) {
        return ArrayUtils.get(times, 0) != null || ArrayUtils.get(times, 1) != null;
    }

    /**
     * 判断指定日期是否位于闭区间内
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param date 指定日期
     * @return 是否位于闭区间内
     */
    public static boolean isBetween(LocalDate beginDate, LocalDate endDate, LocalDate date) {
        return beginDate != null && endDate != null && date != null
                && isAfterOrEqual(date, beginDate) && isBeforeOrEqual(date, endDate);
    }

    /**
     * 判断指定日期是否不在闭区间内
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param date 指定日期
     * @return 是否不在闭区间内；任一参数为空时返回 {@code true}
     */
    public static boolean isNotBetween(LocalDate beginDate, LocalDate endDate, LocalDate date) {
        return !isBetween(beginDate, endDate, date);
    }

    /**
     * 判断指定时间是否位于闭区间内
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param time 指定时间
     * @return 是否位于闭区间内
     */
    public static boolean isBetween(LocalDateTime beginTime, LocalDateTime endTime, LocalDateTime time) {
        return beginTime != null && endTime != null && time != null
                && isAfterOrEqual(time, beginTime) && isBeforeOrEqual(time, endTime);
    }

    /**
     * 判断指定时间是否不在闭区间内
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param time 指定时间
     * @return 是否不在闭区间内；任一参数为空时返回 {@code true}
     */
    public static boolean isNotBetween(LocalDateTime beginTime, LocalDateTime endTime, LocalDateTime time) {
        return !isBetween(beginTime, endTime, time);
    }

    /**
     * 计算两个日期之间经过的完整年数
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 完整年数；日期为空或开始日期晚于结束日期时，返回 0
     */
    public static int getYearsBetween(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null || endDate == null || beginDate.isAfter(endDate)) {
            return 0;
        }
        return Period.between(beginDate, endDate).getYears();
    }

    /**
     * 从时间范围列表中扣除指定时间范围
     *
     * @param sourceRanges 原时间范围列表
     * @param excludedRange 需要扣除的时间范围
     * @return 扣除后的时间范围列表
     */
    public static List<TimeRange> subtractTimeRanges(List<TimeRange> sourceRanges, TimeRange excludedRange) {
        List<TimeRange> result = new ArrayList<>();
        for (TimeRange sourceRange : sourceRanges) {
            TimeRange overlap = intersectTimeRange(sourceRange, excludedRange);
            if (overlap == null) {
                result.add(sourceRange);
                continue;
            }
            if (sourceRange.getStartTime().isBefore(overlap.getStartTime())) {
                result.add(new TimeRange(sourceRange.getStartTime(), overlap.getStartTime()));
            }
            if (overlap.getEndTime().isBefore(sourceRange.getEndTime())) {
                result.add(new TimeRange(overlap.getEndTime(), sourceRange.getEndTime()));
            }
        }
        return result;
    }

    /**
     * 合并重叠或首尾相接的时间范围
     *
     * @param timeRanges 时间范围列表
     * @return 合并后的时间范围列表
     */
    public static List<TimeRange> mergeTimeRanges(List<TimeRange> timeRanges) {
        if (CollUtil.isEmpty(timeRanges)) {
            return Collections.emptyList();
        }
        List<TimeRange> sortedRanges = timeRanges.stream()
                .sorted(Comparator.comparing(TimeRange::getStartTime))
                .collect(Collectors.toList());
        List<TimeRange> result = new ArrayList<>();
        TimeRange currentRange = CollUtil.getFirst(sortedRanges);
        for (int i = 1; i < sortedRanges.size(); i++) {
            TimeRange nextRange = sortedRanges.get(i);
            if (nextRange.getStartTime().isAfter(currentRange.getEndTime())) {
                result.add(currentRange);
                currentRange = nextRange;
                continue;
            }
            LocalDateTime endTime = currentRange.getEndTime().isAfter(nextRange.getEndTime())
                    ? currentRange.getEndTime() : nextRange.getEndTime();
            currentRange = new TimeRange(currentRange.getStartTime(), endTime);
        }
        result.add(currentRange);
        return result;
    }

    /**
     * 计算两个时间范围列表的交集
     *
     * @param firstRanges 第一组时间范围
     * @param secondRanges 第二组时间范围
     * @return 合并后的交集时间范围列表
     */
    public static List<TimeRange> intersectTimeRanges(
            List<TimeRange> firstRanges, List<TimeRange> secondRanges) {
        List<TimeRange> result = new ArrayList<>();
        for (TimeRange firstRange : firstRanges) {
            for (TimeRange secondRange : secondRanges) {
                TimeRange overlap = intersectTimeRange(firstRange, secondRange);
                if (overlap != null) {
                    result.add(overlap);
                }
            }
        }
        return mergeTimeRanges(result);
    }

    /**
     * 计算两个时间范围的交集
     *
     * @param firstRange 第一个时间范围
     * @param secondRange 第二个时间范围
     * @return 交集时间范围；没有交集时返回 {@code null}
     */
    public static TimeRange intersectTimeRange(TimeRange firstRange, TimeRange secondRange) {
        LocalDateTime startTime = firstRange.getStartTime().isAfter(secondRange.getStartTime())
                ? firstRange.getStartTime() : secondRange.getStartTime();
        LocalDateTime endTime = firstRange.getEndTime().isBefore(secondRange.getEndTime())
                ? firstRange.getEndTime() : secondRange.getEndTime();
        return endTime.isAfter(startTime) ? new TimeRange(startTime, endTime) : null;
    }

    /**
     * 计算时间范围的总分钟数
     *
     * @param timeRanges 时间范围列表
     * @return 总分钟数
     */
    public static int calculateDurationMinutes(List<TimeRange> timeRanges) {
        return timeRanges.stream()
                .mapToInt(range -> Math.toIntExact(Duration.between(
                        range.getStartTime(), range.getEndTime()).toMinutes()))
                .sum();
    }

    /**
     * 构建日期范围内每天对应的时间范围
     *
     * 当结束时刻早于或等于开始时刻时，按跨天时间范围处理。
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param beginTime 每日开始时刻
     * @param endTime 每日结束时刻
     * @return 每日时间范围列表
     */
    public static List<TimeRange> buildDailyTimeRanges(
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime) {
        if (beginDate == null || endDate == null || beginTime == null || endTime == null
                || beginDate.isAfter(endDate)) {
            return Collections.emptyList();
        }
        List<TimeRange> result = new ArrayList<>();
        for (LocalDate date = beginDate; isBeforeOrEqual(date, endDate); date = date.plusDays(1)) {
            LocalDateTime rangeBeginTime = date.atTime(beginTime);
            LocalDateTime rangeEndTime = date.atTime(endTime);
            if (rangeEndTime.isBefore(rangeBeginTime) || rangeEndTime.isEqual(rangeBeginTime)) {
                rangeEndTime = rangeEndTime.plusDays(1);
            }
            result.add(new TimeRange(rangeBeginTime, rangeEndTime));
        }
        return result;
    }

    /**
     * 查找包含指定时刻的每日时间范围
     *
     * 每日时间范围允许跨天，因此会同时检查基准日期的前一天、当天和后一天。
     * 范围匹配使用左右闭区间。
     *
     * @param date 基准日期
     * @param beginTime 每日开始时刻
     * @param endTime 每日结束时刻
     * @param time 指定时刻
     * @return 包含指定时刻的时间范围；不存在时返回 {@code null}
     */
    public static TimeRange findDailyTimeRange(
            LocalDate date, LocalTime beginTime, LocalTime endTime, LocalDateTime time) {
        if (date == null || time == null) {
            return null;
        }
        List<TimeRange> timeRanges = buildDailyTimeRanges(
                date.minusDays(1), date.plusDays(1), beginTime, endTime);
        return CollUtil.findOne(timeRanges, timeRange -> isBetween(
                timeRange.getStartTime(), timeRange.getEndTime(), time));
    }

    /**
     * 将 Unix 秒时间戳转换为默认时区的本地时间
     *
     * @param epochSecond Unix 秒时间戳
     * @return 本地时间
     */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return ofEpochSecond(epochSecond, DEFAULT_ZONE_ID);
    }

    /**
     * 将 Unix 秒时间戳转换为指定时区的本地时间
     *
     * @param epochSecond Unix 秒时间戳
     * @param zoneId 时区编号
     * @return 本地时间
     */
    public static LocalDateTime ofEpochSecond(long epochSecond, ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), zoneId);
    }

    /**
     * 创建指定时间
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 指定时间
     */
    public static LocalDateTime buildTime(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 0, 0, 0);
    }

    public static LocalDateTime[] buildBetweenTime(int year1, int month1, int day1,
                                                   int year2, int month2, int day2) {
        return new LocalDateTime[]{buildTime(year1, month1, day1), buildTime(year2, month2, day2)};
    }

    /**
     * 判指定断时间，是否在该时间范围内
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param time 指定时间
     * @return 是否
     */
    public static boolean isBetween(LocalDateTime startTime, LocalDateTime endTime, Timestamp time) {
        if (startTime == null || endTime == null || time == null) {
            return false;
        }
        return LocalDateTimeUtil.isIn(LocalDateTimeUtil.of(time), startTime, endTime);
    }

    /**
     * 判指定断时间，是否在该时间范围内
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param time 指定时间
     * @return 是否
     */
    public static boolean isBetween(LocalDateTime startTime, LocalDateTime endTime, String time) {
        if (startTime == null || endTime == null || time == null) {
            return false;
        }
        return LocalDateTimeUtil.isIn(parse(time), startTime, endTime);
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
        return LocalDateTimeUtil.isIn(LocalDateTime.now(),
                LocalDateTime.of(nowDate, LocalTime.parse(startTime)),
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

    /**
     * 获得指定日期的开始时间
     *
     * @param date 日期
     * @return 当日开始时间；日期为空时返回 {@code null}
     */
    public static LocalDateTime getDayBeginTime(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    /**
     * 获得指定时间所在日期的开始时间
     *
     * @param time 时间
     * @return 当日开始时间；时间为空时返回 {@code null}
     */
    public static LocalDateTime getDayBeginTime(LocalDateTime time) {
        return time == null ? null : getDayBeginTime(time.toLocalDate());
    }

    /**
     * 获得指定日期的结束时间
     *
     * @param date 日期
     * @return 当日结束时间；日期为空时返回 {@code null}
     */
    public static LocalDateTime getDayEndTime(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX);
    }

    /**
     * 获得自然日期范围对应的日期时间闭区间
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 从开始日期零点到结束日期最后时刻的闭区间；两个日期都为空时返回 {@code null}
     */
    public static LocalDateTime[] getDateTimeRange(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null && endDate == null) {
            return null;
        }
        return new LocalDateTime[]{getDayBeginTime(beginDate), getDayEndTime(endDate)};
    }

    /**
     * 获得指定月份的日期时间闭区间
     *
     * @param year 年份
     * @param month 月份
     * @return 从当月月初零点到月末最后时刻的闭区间
     */
    public static LocalDateTime[] getMonthDateTimeRange(int year, int month) {
        LocalDate monthBeginDate = LocalDate.of(year, month, 1);
        return getDateTimeRange(monthBeginDate, monthBeginDate.with(TemporalAdjusters.lastDayOfMonth()));
    }

    /**
     * 获取指定日期所在的月份的开始时间
     * 例如：2023-09-30 00:00:00,000
     *
     * @param date 日期
     * @return 月份的开始时间
     */
    public static LocalDateTime beginOfMonth(LocalDateTime date) {
        return date.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
    }

    /**
     * 获取指定日期的开始时间
     *
     * @param date 日期
     * @return 当日开始时间
     */
    public static LocalDateTime beginOfDay(LocalDateTime date) {
        return date.with(LocalTime.MIN);
    }

    /**
     * 获取指定日期所在的月份的最后时间
     * 例如：2023-09-30 23:59:59,999
     *
     * @param date 日期
     * @return 月份的结束时间
     */
    public static LocalDateTime endOfMonth(LocalDateTime date) {
        return date.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
    }

    /**
     * 获得指定年月的月初时间
     *
     * @param year 年份
     * @param month 月份
     * @return 月初时间
     */
    public static LocalDateTime getMonthBeginTime(int year, int month) {
        return LocalDateTime.of(year, month, 1, 0, 0);
    }

    /**
     * 获得指定年月的月初时间
     *
     * @param month 年月
     * @return 月初时间
     */
    public static LocalDateTime getMonthBeginTime(YearMonth month) {
        return getMonthBeginTime(month.getYear(), month.getMonthValue());
    }

    /**
     * 获得指定年月的下月月初时间
     *
     * @param year 年份
     * @param month 月份
     * @return 下月月初时间
     */
    public static LocalDateTime getNextMonthBeginTime(int year, int month) {
        return getMonthBeginTime(year, month).plusMonths(1);
    }

    /**
     * 获得指定年月的下月月初时间
     *
     * @param month 年月
     * @return 下月月初时间
     */
    public static LocalDateTime getNextMonthBeginTime(YearMonth month) {
        return getMonthBeginTime(month).plusMonths(1);
    }

    /**
     * 判断两个时间段是否存在正时长的重叠
     *
     * @param beginTime1 第一个范围的开始时间
     * @param endTime1 第一个范围的结束时间
     * @param beginTime2 第二个范围的开始时间
     * @param endTime2 第二个范围的结束时间
     * @return 是否存在正时长的重叠
     */
    public static boolean isOverlap(LocalDateTime beginTime1, LocalDateTime endTime1,
                                    LocalDateTime beginTime2, LocalDateTime endTime2) {
        if (beginTime1 == null || endTime1 == null || beginTime2 == null || endTime2 == null) {
            return false;
        }
        return beginTime1.isBefore(endTime2) && endTime1.isAfter(beginTime2);
    }

    /**
     * 判断两个左右闭合的时间范围是否重叠
     *
     * @param beginTime1 第一个范围的开始时间
     * @param endTime1 第一个范围的结束时间
     * @param beginTime2 第二个范围的开始时间
     * @param endTime2 第二个范围的结束时间
     * @return 是否重叠
     */
    public static boolean isClosedRangeOverlap(LocalDateTime beginTime1, LocalDateTime endTime1,
                                               LocalDateTime beginTime2, LocalDateTime endTime2) {
        if (beginTime1 == null || endTime1 == null || beginTime2 == null || endTime2 == null) {
            return false;
        }
        return isBeforeOrEqual(beginTime1, endTime2) && isAfterOrEqual(endTime1, beginTime2);
    }

    /**
     * 获得指定日期所在季度
     *
     * @param date 日期
     * @return 所在季度
     */
    public static int getQuarterOfYear(LocalDateTime date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * 获取指定日期到现在过了几天，如果指定日期在当前日期之后，获取结果为负
     *
     * @param dateTime 日期
     * @return 相差天数
     */
    public static Long between(LocalDateTime dateTime) {
        return LocalDateTimeUtil.between(dateTime, LocalDateTime.now(), ChronoUnit.DAYS);
    }

    /**
     * 获取今天的开始时间
     *
     * @return 今天
     */
    public static LocalDateTime getToday() {
        return LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
    }

    /**
     * 获取昨天的开始时间
     *
     * @return 昨天
     */
    public static LocalDateTime getYesterday() {
        return LocalDateTimeUtil.beginOfDay(LocalDateTime.now().minusDays(1));
    }

    /**
     * 获取本月的开始时间
     *
     * @return 本月
     */
    public static LocalDateTime getMonth() {
        return beginOfMonth(LocalDateTime.now());
    }

    /**
     * 获取本年的开始时间
     *
     * @return 本年
     */
    public static LocalDateTime getYear() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
    }

    /**
     * 获取最近 N 天的 0 点时刻序列（升序，含今天）
     * <p>
     * 例：getLatestDays(3) 返回 [前天 00:00, 昨天 00:00, 今天 00:00]
     *
     * @param days 天数（含今天）
     * @return 升序的 LocalDateTime 列表
     */
    public static List<LocalDateTime> getLatestDays(int days) {
        LocalDateTime today = getToday();
        List<LocalDateTime> dates = new ArrayList<>(days);
        for (int i = days - 1; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }
        return dates;
    }

    public static List<LocalDateTime[]> getDateRangeList(LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         Integer interval) {
        // 1.1 找到枚举
        DateIntervalEnum intervalEnum = DateIntervalEnum.valueOf(interval);
        Assert.notNull(intervalEnum, "interval({}} 找不到对应的枚举", interval);
        // 1.2 将时间对齐
        startTime = LocalDateTimeUtil.beginOfDay(startTime);
        endTime = LocalDateTimeUtil.endOfDay(endTime);

        // 2. 循环，生成时间范围
        List<LocalDateTime[]> timeRanges = new ArrayList<>();
        switch (intervalEnum) {
            case HOUR:
                while (startTime.isBefore(endTime)) {
                    timeRanges.add(new LocalDateTime[]{startTime, startTime.plusHours(1).minusNanos(1)});
                    startTime = startTime.plusHours(1);
                }
            case DAY:
                while (startTime.isBefore(endTime)) {
                    timeRanges.add(new LocalDateTime[]{startTime, startTime.plusDays(1).minusNanos(1)});
                    startTime = startTime.plusDays(1);
                }
                break;
            case WEEK:
                while (startTime.isBefore(endTime)) {
                    LocalDateTime endOfWeek = startTime.with(DayOfWeek.SUNDAY).plusDays(1).minusNanos(1);
                    timeRanges.add(new LocalDateTime[]{startTime, endOfWeek});
                    startTime = endOfWeek.plusNanos(1);
                }
                break;
            case MONTH:
                while (startTime.isBefore(endTime)) {
                    LocalDateTime endOfMonth = startTime.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).minusNanos(1);
                    timeRanges.add(new LocalDateTime[]{startTime, endOfMonth});
                    startTime = endOfMonth.plusNanos(1);
                }
                break;
            case QUARTER:
                while (startTime.isBefore(endTime)) {
                    int quarterOfYear = getQuarterOfYear(startTime);
                    LocalDateTime quarterEnd = quarterOfYear == 4
                            ? startTime.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).minusNanos(1)
                            : startTime.withMonth(quarterOfYear * 3 + 1).withDayOfMonth(1).minusNanos(1);
                    timeRanges.add(new LocalDateTime[]{startTime, quarterEnd});
                    startTime = quarterEnd.plusNanos(1);
                }
                break;
            case YEAR:
                while (startTime.isBefore(endTime)) {
                    LocalDateTime endOfYear = startTime.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).minusNanos(1);
                    timeRanges.add(new LocalDateTime[]{startTime, endOfYear});
                    startTime = endOfYear.plusNanos(1);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        // 3. 兜底，最后一个时间，需要保持在 endTime 之前
        LocalDateTime[] lastTimeRange = CollUtil.getLast(timeRanges);
        if (lastTimeRange != null) {
            lastTimeRange[1] = endTime;
        }
        return timeRanges;
    }

    /**
     * 获取从开始日期起的日期列表
     *
     * @param startDate 开始日期
     * @param days 天数
     * @return 日期列表，包含开始日期
     */
    public static List<LocalDate> getDateList(LocalDate startDate, int days) {
        List<LocalDate> dateList = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            dateList.add(startDate.plusDays(i));
        }
        return dateList;
    }

    /**
     * 格式化时间范围
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  时间间隔
     * @return 时间范围
     */
    public static String formatDateRange(LocalDateTime startTime, LocalDateTime endTime, Integer interval) {
        // 1. 找到枚举
        DateIntervalEnum intervalEnum = DateIntervalEnum.valueOf(interval);
        Assert.notNull(intervalEnum, "interval({}} 找不到对应的枚举", interval);

        // 2. 循环，生成时间范围
        switch (intervalEnum) {
            case HOUR:
                return LocalDateTimeUtil.format(startTime, DatePattern.NORM_DATETIME_MINUTE_PATTERN);
            case DAY:
                return LocalDateTimeUtil.format(startTime, DatePattern.NORM_DATE_PATTERN);
            case WEEK:
                return LocalDateTimeUtil.format(startTime, DatePattern.NORM_DATE_PATTERN)
                        + StrUtil.format("(第 {} 周)", LocalDateTimeUtil.weekOfYear(startTime));
            case MONTH:
                return LocalDateTimeUtil.format(startTime, DatePattern.NORM_MONTH_PATTERN);
            case QUARTER:
                return StrUtil.format("{}-Q{}", startTime.getYear(), getQuarterOfYear(startTime));
            case YEAR:
                return LocalDateTimeUtil.format(startTime, DatePattern.NORM_YEAR_PATTERN);
            default:
                throw new IllegalArgumentException("Invalid interval: " + interval);
        }
    }

    /**
     * 获取指定日期所在季度的第一天
     *
     * @param date 日期
     * @return 所在季度的第一天
     */
    public static LocalDate getQuarterStart(LocalDate date) {
        Month firstMonthOfQuarter = date.getMonth().firstMonthOfQuarter();
        return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
    }

    /**
     * 获取指定日期所在周的第一天（周一）
     *
     * @param date 日期
     * @return 所在周的周一
     */
    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * 将给定的 {@link LocalDateTime} 转换为自 Unix 纪元时间（1970-01-01T00:00:00Z）以来的秒数。
     *
     * @param sourceDateTime 需要转换的本地日期时间，不能为空
     * @return 自 1970-01-01T00:00:00Z 起的秒数（epoch second）
     * @throws NullPointerException 如果 {@code sourceDateTime} 为 {@code null}
     * @throws DateTimeException 如果转换过程中发生时间超出范围或其他时间处理异常
     */
    public static Long toEpochSecond(LocalDateTime sourceDateTime) {
        return toEpochSecond(sourceDateTime, DEFAULT_ZONE_ID);
    }

    /**
     * 将给定的 {@link LocalDateTime} 按指定时区转换为自 Unix 纪元时间（1970-01-01T00:00:00Z）以来的秒数。
     *
     * @param sourceDateTime 需要转换的本地日期时间，不能为空
     * @param zoneId 时区编号
     * @return 自 1970-01-01T00:00:00Z 起的秒数（epoch second）
     */
    public static Long toEpochSecond(LocalDateTime sourceDateTime, ZoneId zoneId) {
        return sourceDateTime.atZone(zoneId).toEpochSecond();
    }

    /**
     * 时间范围
     */
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    public static class TimeRange {

        /**
         * 开始时间
         */
        private final LocalDateTime startTime;
        /**
         * 结束时间
         */
        private final LocalDateTime endTime;

    }

}

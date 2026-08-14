package cn.iocoder.yudao.framework.common.util.date;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LocalDateTimeUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class LocalDateTimeUtilsTest {

    @Test
    public void testDateCompare() {
        // 准备参数
        LocalDate firstDate = LocalDate.of(2026, 7, 23);
        LocalDate secondDate = LocalDate.of(2026, 7, 24);

        // 调用，并断言
        assertTrue(LocalDateTimeUtils.isBeforeOrEqual(firstDate, secondDate));
        assertTrue(LocalDateTimeUtils.isBeforeOrEqual(secondDate, secondDate));
        assertFalse(LocalDateTimeUtils.isBeforeOrEqual(secondDate, firstDate));
        assertTrue(LocalDateTimeUtils.isAfterOrEqual(secondDate, firstDate));
        assertTrue(LocalDateTimeUtils.isAfterOrEqual(secondDate, secondDate));
        assertFalse(LocalDateTimeUtils.isAfterOrEqual(firstDate, secondDate));
    }

    @Test
    public void testDateTimeCompare() {
        // 准备参数
        LocalDateTime firstTime = LocalDateTime.of(2026, 7, 24, 9, 0);
        LocalDateTime secondTime = LocalDateTime.of(2026, 7, 24, 18, 0);

        // 调用，并断言
        assertTrue(LocalDateTimeUtils.isBeforeOrEqual(firstTime, secondTime));
        assertTrue(LocalDateTimeUtils.isBeforeOrEqual(secondTime, secondTime));
        assertFalse(LocalDateTimeUtils.isBeforeOrEqual(secondTime, firstTime));
        assertTrue(LocalDateTimeUtils.isAfterOrEqual(secondTime, firstTime));
        assertTrue(LocalDateTimeUtils.isAfterOrEqual(secondTime, secondTime));
        assertFalse(LocalDateTimeUtils.isAfterOrEqual(firstTime, secondTime));
    }

    @Test
    public void testIsTimeRangeValid() {
        // 准备参数
        LocalDateTime beginTime = LocalDateTime.of(2026, 7, 24, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 24, 18, 0);

        // 调用，并断言
        assertTrue(LocalDateTimeUtils.isTimeRangeValid(beginTime, endTime));
        assertFalse(LocalDateTimeUtils.isTimeRangeValid(beginTime, beginTime));
        assertFalse(LocalDateTimeUtils.isTimeRangeValid(endTime, beginTime));
        assertFalse(LocalDateTimeUtils.isTimeRangeValid(null, endTime));
        assertFalse(LocalDateTimeUtils.isTimeRangeValid(beginTime, null));
    }

    @Test
    public void testIsTimeRangePresent() {
        // 准备参数
        LocalDateTime time = LocalDateTime.of(2026, 7, 24, 9, 0);

        // 调用，并断言：空数组和双空边界都不是有效范围
        assertFalse(LocalDateTimeUtils.isTimeRangePresent(null));
        assertFalse(LocalDateTimeUtils.isTimeRangePresent(new LocalDateTime[0]));
        assertFalse(LocalDateTimeUtils.isTimeRangePresent(new LocalDateTime[]{null, null}));

        // 调用，并断言：单边或双边存在即为有效范围
        assertTrue(LocalDateTimeUtils.isTimeRangePresent(new LocalDateTime[]{time}));
        assertTrue(LocalDateTimeUtils.isTimeRangePresent(new LocalDateTime[]{null, time}));
        assertTrue(LocalDateTimeUtils.isTimeRangePresent(new LocalDateTime[]{time, time.plusHours(1)}));
    }

    @Test
    public void testGetDayTime() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 24);
        LocalDateTime time = date.atTime(15, 30);

        // 调用，并断言
        assertEquals(LocalDateTime.of(2026, 7, 24, 0, 0),
                LocalDateTimeUtils.getDayBeginTime(date));
        assertEquals(LocalDateTime.of(2026, 7, 24, 0, 0),
                LocalDateTimeUtils.getDayBeginTime(time));
        assertNull(LocalDateTimeUtils.getDayBeginTime((LocalDateTime) null));
        assertEquals(LocalDateTime.of(date, LocalTime.MAX),
                LocalDateTimeUtils.getDayEndTime(date));
    }

    @Test
    public void testGetDateTimeRange() {
        // 准备参数
        LocalDate beginDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        // 调用，并断言：左右两端均为闭区间边界
        LocalDateTime[] result = LocalDateTimeUtils.getDateTimeRange(beginDate, endDate);
        assertEquals(beginDate.atStartOfDay(), result[0]);
        assertEquals(endDate.atTime(LocalTime.MAX), result[1]);

        // 调用，并断言：支持单边范围
        result = LocalDateTimeUtils.getDateTimeRange(beginDate, null);
        assertEquals(beginDate.atStartOfDay(), result[0]);
        assertNull(result[1]);
        result = LocalDateTimeUtils.getDateTimeRange(null, endDate);
        assertNull(result[0]);
        assertEquals(endDate.atTime(LocalTime.MAX), result[1]);

        // 调用，并断言：两个边界都为空时不生成范围
        assertNull(LocalDateTimeUtils.getDateTimeRange(null, null));
    }

    @Test
    public void testGetMonthDateTimeRange() {
        // 调用：闰年二月
        LocalDateTime[] result = LocalDateTimeUtils.getMonthDateTimeRange(2024, 2);

        // 断言：完整包含闰年二月的首尾时刻
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), result[0]);
        assertEquals(LocalDate.of(2024, 2, 29).atTime(LocalTime.MAX), result[1]);
    }

    @Test
    public void testGetMonthBeginTime() {
        // 准备参数
        YearMonth month = YearMonth.of(2026, 12);

        // 调用，并断言
        assertEquals(LocalDateTime.of(2026, 12, 1, 0, 0),
                LocalDateTimeUtils.getMonthBeginTime(month));
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
                LocalDateTimeUtils.getNextMonthBeginTime(month));
    }

    @Test
    public void testIsBetween() {
        // 准备参数
        LocalDate beginDate = LocalDate.of(2026, 7, 23);
        LocalDate endDate = LocalDate.of(2026, 7, 25);
        LocalDateTime beginTime = beginDate.atTime(9, 0);
        LocalDateTime endTime = beginDate.atTime(18, 0);

        // 调用，并断言：日期闭区间
        assertTrue(LocalDateTimeUtils.isBetween(beginDate, endDate, beginDate));
        assertTrue(LocalDateTimeUtils.isBetween(beginDate, endDate, LocalDate.of(2026, 7, 24)));
        assertTrue(LocalDateTimeUtils.isBetween(beginDate, endDate, endDate));
        assertFalse(LocalDateTimeUtils.isBetween(beginDate, endDate, LocalDate.of(2026, 7, 26)));
        assertFalse(LocalDateTimeUtils.isBetween(beginDate, endDate, null));
        assertFalse(LocalDateTimeUtils.isNotBetween(beginDate, endDate, beginDate));
        assertTrue(LocalDateTimeUtils.isNotBetween(beginDate, endDate, LocalDate.of(2026, 7, 26)));
        assertTrue(LocalDateTimeUtils.isNotBetween(beginDate, endDate, null));

        // 调用，并断言：时间闭区间
        assertTrue(LocalDateTimeUtils.isBetween(beginTime, endTime, beginTime));
        assertTrue(LocalDateTimeUtils.isBetween(beginTime, endTime, beginDate.atTime(12, 0)));
        assertTrue(LocalDateTimeUtils.isBetween(beginTime, endTime, endTime));
        assertFalse(LocalDateTimeUtils.isBetween(beginTime, endTime, beginDate.atTime(18, 1)));
        assertFalse(LocalDateTimeUtils.isBetween(beginTime, endTime, (LocalDateTime) null));
        assertFalse(LocalDateTimeUtils.isNotBetween(beginTime, endTime, endTime));
        assertTrue(LocalDateTimeUtils.isNotBetween(beginTime, endTime, beginDate.atTime(18, 1)));
        assertTrue(LocalDateTimeUtils.isNotBetween(beginTime, endTime, null));
    }

    @Test
    public void testIsClosedRangeOverlap() {
        // 准备参数
        LocalDateTime firstBeginTime = LocalDateTime.of(2026, 7, 24, 9, 0);
        LocalDateTime firstEndTime = LocalDateTime.of(2026, 7, 24, 12, 0);

        // 调用，并断言：首尾相接属于闭区间重叠
        assertTrue(LocalDateTimeUtils.isClosedRangeOverlap(
                firstBeginTime, firstEndTime, firstEndTime, firstEndTime.plusHours(1)));
        assertTrue(LocalDateTimeUtils.isClosedRangeOverlap(
                firstBeginTime, firstEndTime, firstBeginTime.minusHours(1), firstBeginTime));
        assertFalse(LocalDateTimeUtils.isClosedRangeOverlap(
                firstBeginTime, firstEndTime, firstEndTime.plusNanos(1), firstEndTime.plusHours(1)));
        assertFalse(LocalDateTimeUtils.isClosedRangeOverlap(
                firstBeginTime, firstEndTime, null, firstEndTime));
    }

    @Test
    public void testSubtractTimeRanges() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 24);
        TimeRange sourceRange = new TimeRange(date.atTime(9, 0), date.atTime(18, 0));

        // 调用：扣除原时段中间的一段
        List<TimeRange> result = LocalDateTimeUtils.subtractTimeRanges(
                Collections.singletonList(sourceRange),
                new TimeRange(date.atTime(12, 0), date.atTime(13, 0)));

        // 断言：原时段被拆分为两段
        assertEquals(2, result.size());
        assertTimeRange(CollUtil.getFirst(result), date.atTime(9, 0), date.atTime(12, 0));
        assertTimeRange(result.get(1), date.atTime(13, 0), date.atTime(18, 0));

        // 调用，并断言：扣除时段完整覆盖原时段
        result = LocalDateTimeUtils.subtractTimeRanges(
                Collections.singletonList(sourceRange),
                new TimeRange(date.atTime(8, 0), date.atTime(19, 0)));
        assertTrue(result.isEmpty());

        // 调用：扣除时段与原时段首尾相接
        result = LocalDateTimeUtils.subtractTimeRanges(
                Collections.singletonList(sourceRange),
                new TimeRange(date.atTime(18, 0), date.atTime(19, 0)));
        // 断言：两个时段仅首尾相接，没有正时长交集，原时段保持不变
        assertEquals(1, result.size());
        assertTimeRange(CollUtil.getFirst(result), date.atTime(9, 0), date.atTime(18, 0));
    }

    @Test
    public void testMergeTimeRanges() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 24);
        List<TimeRange> timeRanges = Arrays.asList(
                new TimeRange(date.atTime(13, 0), date.atTime(14, 0)),
                new TimeRange(date.atTime(9, 0), date.atTime(11, 0)),
                new TimeRange(date.atTime(10, 0), date.atTime(13, 0)),
                new TimeRange(date.atTime(16, 0), date.atTime(17, 0)));

        // 调用
        List<TimeRange> result = LocalDateTimeUtils.mergeTimeRanges(timeRanges);

        // 断言：重叠或首尾相接的时段被合并，独立时段保持不变
        assertEquals(2, result.size());
        assertTimeRange(CollUtil.getFirst(result), date.atTime(9, 0), date.atTime(14, 0));
        assertTimeRange(result.get(1), date.atTime(16, 0), date.atTime(17, 0));
        // 调用，并断言：空列表
        assertTrue(LocalDateTimeUtils.mergeTimeRanges(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testIntersectTimeRanges() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 24);
        List<TimeRange> firstRanges = Arrays.asList(
                new TimeRange(date.atTime(9, 0), date.atTime(12, 0)),
                new TimeRange(date.atTime(13, 0), date.atTime(18, 0)));
        List<TimeRange> secondRanges = Arrays.asList(
                new TimeRange(date.atTime(10, 0), date.atTime(14, 0)),
                new TimeRange(date.atTime(17, 0), date.atTime(19, 0)));

        // 调用
        List<TimeRange> result = LocalDateTimeUtils.intersectTimeRanges(firstRanges, secondRanges);

        // 断言
        assertEquals(3, result.size());
        assertTimeRange(CollUtil.getFirst(result), date.atTime(10, 0), date.atTime(12, 0));
        assertTimeRange(result.get(1), date.atTime(13, 0), date.atTime(14, 0));
        assertTimeRange(result.get(2), date.atTime(17, 0), date.atTime(18, 0));
        // 调用，并断言：两个时段仅首尾相接时不生成零时长交集
        assertNull(LocalDateTimeUtils.intersectTimeRange(
                new TimeRange(date.atTime(9, 0), date.atTime(10, 0)),
                new TimeRange(date.atTime(10, 0), date.atTime(11, 0))));
    }

    @Test
    public void testCalculateDurationMinutes() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 24);
        List<TimeRange> timeRanges = Arrays.asList(
                new TimeRange(date.atTime(9, 0), date.atTime(10, 0)),
                new TimeRange(date.atTime(13, 0), date.atTime(14, 30)));

        // 调用，并断言
        assertEquals(150, LocalDateTimeUtils.calculateDurationMinutes(timeRanges));
        // 调用，并断言：空列表
        assertEquals(0, LocalDateTimeUtils.calculateDurationMinutes(Collections.emptyList()));
    }

    @Test
    public void testBuildDailyTimeRanges() {
        // 准备参数
        LocalDate beginDate = LocalDate.of(2026, 7, 24);
        LocalDate endDate = LocalDate.of(2026, 7, 25);

        // 调用：构建跨天时间范围
        List<TimeRange> result = LocalDateTimeUtils.buildDailyTimeRanges(
                beginDate, endDate, LocalTime.of(22, 0), LocalTime.of(6, 0));

        // 断言
        assertEquals(2, result.size());
        assertTimeRange(CollUtil.getFirst(result), beginDate.atTime(22, 0), beginDate.plusDays(1).atTime(6, 0));
        assertTimeRange(result.get(1), endDate.atTime(22, 0), endDate.plusDays(1).atTime(6, 0));

        // 调用：每日开始和结束时刻相同
        result = LocalDateTimeUtils.buildDailyTimeRanges(
                beginDate, beginDate, LocalTime.of(9, 0), LocalTime.of(9, 0));
        // 断言：按完整一天处理
        assertEquals(1, result.size());
        assertTimeRange(CollUtil.getFirst(result), beginDate.atTime(9, 0), beginDate.plusDays(1).atTime(9, 0));

        // 调用，并断言：开始日期晚于结束日期
        assertTrue(LocalDateTimeUtils.buildDailyTimeRanges(
                endDate, beginDate, LocalTime.of(9, 0), LocalTime.of(18, 0)).isEmpty());
    }

    @Test
    public void testFindDailyTimeRange() {
        // 准备参数
        LocalDate date = LocalDate.of(2026, 7, 8);

        // 调用，并断言普通时间范围的左右边界
        TimeRange result = LocalDateTimeUtils.findDailyTimeRange(
                date, LocalTime.of(8, 0), LocalTime.of(10, 0), date.atTime(8, 0));
        assertEquals(date.atTime(8, 0), result.getStartTime());
        assertEquals(date.atTime(10, 0), result.getEndTime());
        assertNotNull(LocalDateTimeUtils.findDailyTimeRange(
                date, LocalTime.of(8, 0), LocalTime.of(10, 0), date.atTime(10, 0)));

        // 调用，并断言跨日时间范围
        result = LocalDateTimeUtils.findDailyTimeRange(
                date, LocalTime.of(20, 0), LocalTime.of(6, 0), date.plusDays(1).atTime(5, 0));
        assertEquals(date.atTime(20, 0), result.getStartTime());
        assertEquals(date.plusDays(1).atTime(6, 0), result.getEndTime());
        assertNull(LocalDateTimeUtils.findDailyTimeRange(
                date, LocalTime.of(8, 0), LocalTime.of(10, 0), date.atTime(12, 0)));
    }

    private static void assertTimeRange(TimeRange timeRange,
                                        LocalDateTime expectedStartTime,
                                        LocalDateTime expectedEndTime) {
        assertEquals(expectedStartTime, timeRange.getStartTime());
        assertEquals(expectedEndTime, timeRange.getEndTime());
    }

}

package cn.iocoder.yudao.module.im.service.statistics;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.dal.mysql.statistics.ImStatisticsManagerMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link ImStatisticsManagerServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class ImStatisticsManagerServiceImplTest extends BaseMockitoUnitTest {

    private static final LocalDateTime BEGIN = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 2, 1, 0, 0);

    @InjectMocks
    private ImStatisticsManagerServiceImpl service;

    @Mock
    private ImStatisticsManagerMapper statisticsMapper;

    // ========== 单值委托 ==========

    @Test
    public void testGetTotalUserCount_delegate() {
        when(statisticsMapper.selectTotalUserCount()).thenReturn(99L);
        assertEquals(99L, service.getTotalUserCount());
    }

    @Test
    public void testGetNewUserCount_delegate() {
        when(statisticsMapper.selectNewUserCount(BEGIN, END)).thenReturn(10L);
        assertEquals(10L, service.getNewUserCount(BEGIN, END));
    }

    @Test
    public void testGetActiveUserCount_delegate() {
        when(statisticsMapper.selectActiveUserCount(BEGIN, END)).thenReturn(5L);
        assertEquals(5L, service.getActiveUserCount(BEGIN, END));
    }

    @Test
    public void testGetTotalGroupCount_delegate() {
        when(statisticsMapper.selectTotalGroupCount()).thenReturn(33L);
        assertEquals(33L, service.getTotalGroupCount());
    }

    @Test
    public void testGetPrivateMessageCount_delegate() {
        when(statisticsMapper.selectPrivateMessageCount(BEGIN, END)).thenReturn(123L);
        assertEquals(123L, service.getPrivateMessageCount(BEGIN, END));
    }

    @Test
    public void testGetGroupMessageCount_delegate() {
        when(statisticsMapper.selectGroupMessageCount(BEGIN, END)).thenReturn(456L);
        assertEquals(456L, service.getGroupMessageCount(BEGIN, END));
    }

    // ========== 每日序列 ==========

    @Test
    public void testGetNewUserDailyCountMap_dateConvert() {
        // 准备：date 字段以 SQL Date / String 混入，count 混入多种 Number
        when(statisticsMapper.selectNewUserDailyCount(eq(BEGIN), eq(END))).thenReturn(Arrays.asList(
                row("date", java.sql.Date.valueOf("2026-01-10"), "count", BigInteger.valueOf(5)),
                row("date", "2026-01-11", "count", 8)));

        // 调用
        Map<LocalDateTime, Long> result = service.getNewUserDailyCountMap(BEGIN, END);

        // 断言：date → LocalDateTime 起始零点
        assertEquals(2, result.size());
        assertEquals(5L, result.get(LocalDate.of(2026, 1, 10).atStartOfDay()));
        assertEquals(8L, result.get(LocalDate.of(2026, 1, 11).atStartOfDay()));
    }

    @Test
    public void testGetActiveUserDailyCountMap_dateConvert() {
        when(statisticsMapper.selectActiveUserDailyCount(any(), any())).thenReturn(Arrays.asList(
                row("date", "2026-01-10", "count", 3L)));

        Map<LocalDateTime, Long> result = service.getActiveUserDailyCountMap(BEGIN, END);
        assertEquals(3L, result.get(LocalDate.of(2026, 1, 10).atStartOfDay()));
    }

    @Test
    public void testGetPrivateMessageDailyCountMap_dateConvert() {
        when(statisticsMapper.selectPrivateMessageDailyCount(any(), any())).thenReturn(Arrays.asList(
                row("date", "2026-01-10", "count", 7L)));
        assertEquals(7L, service.getPrivateMessageDailyCountMap(BEGIN, END)
                .get(LocalDate.of(2026, 1, 10).atStartOfDay()));
    }

    @Test
    public void testGetGroupMessageDailyCountMap_dateConvert() {
        when(statisticsMapper.selectGroupMessageDailyCount(any(), any())).thenReturn(Arrays.asList(
                row("date", "2026-01-10", "count", 9L)));
        assertEquals(9L, service.getGroupMessageDailyCountMap(BEGIN, END)
                .get(LocalDate.of(2026, 1, 10).atStartOfDay()));
    }

    // ========== 分桶 / 分布 ==========

    @Test
    public void testGetGroupSizeCountMap() {
        when(statisticsMapper.selectGroupSizeDistribution()).thenReturn(Arrays.asList(
                row("range", "1-9 人", "count", BigInteger.valueOf(3)),
                row("range", "10-49 人", "count", 2)));

        Map<String, Long> result = service.getGroupSizeCountMap();
        assertEquals(3L, result.get("1-9 人"));
        assertEquals(2L, result.get("10-49 人"));
    }

    @Test
    public void testGetMessageTypeCountMap() {
        when(statisticsMapper.selectMessageTypeDistribution(BEGIN, END)).thenReturn(Arrays.asList(
                row("type", 101L, "count", BigInteger.valueOf(8)),
                row("type", 102, "count", 1)));

        Map<Integer, Long> result = service.getMessageTypeCountMap(BEGIN, END);
        assertEquals(8L, result.get(101));
        assertEquals(1L, result.get(102));
    }

    @Test
    public void testGetTopSenderCountMap_passesLimit() {
        when(statisticsMapper.selectTopSenders(BEGIN, END, 3)).thenReturn(Arrays.asList(
                row("userId", 1, "messageCount", BigInteger.valueOf(10)),
                row("userId", 2L, "messageCount", 5)));

        Map<Long, Long> result = service.getTopSenderCountMap(BEGIN, END, 3);
        assertEquals(10L, result.get(1L));
        assertEquals(5L, result.get(2L));
    }

    // ========== 工具 ==========

    private static Map<String, Object> row(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

}

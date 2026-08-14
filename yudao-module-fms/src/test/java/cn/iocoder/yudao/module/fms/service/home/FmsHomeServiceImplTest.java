package cn.iocoder.yudao.module.fms.service.home;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeMetricDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.home.vo.FmsHomeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceIndicatorService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetService;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.report.FmsReportCommonService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.ACCOUNT_SET_NOT_INITIALIZED;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.HOME_METRIC_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsHomeServiceImpl.class)
public class FmsHomeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsHomeServiceImpl homeService;
    @MockitoBean
    private FmsAccountSetService accountSetService;
    @MockitoBean
    private FmsClosingPeriodService closingPeriodService;
    @MockitoBean
    private FmsIncomeStatementService incomeStatementService;
    @MockitoBean
    private FmsBalanceSheetService balanceSheetService;
    @MockitoBean
    private FmsReportCommonService reportCommonService;
    @MockitoBean
    private FmsSubjectService subjectService;
    @MockitoBean
    private FmsFinanceIndicatorService financeIndicatorService;

    @Test
    public void testGetHome_success() {
        // mock 数据
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L).setInitialized(true)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(accountSet);
        when(closingPeriodService.getCurrentMonth(1L, accountSet.getStartTime()))
                .thenReturn(YearMonth.of(2026, 8));
        when(reportCommonService.isLineFormula(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class).contains("L"));
        when(incomeStatementService.getIncomeStatement(any(FmsReportListReqVO.class), eq(10L)))
                .thenAnswer(invocation -> buildIncomeStatement(
                        invocation.<FmsReportListReqVO>getArgument(0).getStartMonth()));

        // 调用
        FmsHomeRespVO result = homeService.getHome(1L, 10L);

        // 断言
        assertEquals("2026-08", result.getCurrentMonth());
        assertEquals(12, result.getTrends().size());
        assertEquals(BigDecimal.ZERO, result.getTrends().get(0).getIncome());
        FmsHomeRespVO.Trend currentTrend = result.getTrends().get(11);
        assertEquals(new BigDecimal("100.00"), currentTrend.getIncome());
        assertEquals(new BigDecimal("45.00"), currentTrend.getOperatingCost());
        assertEquals(new BigDecimal("30.00"), currentTrend.getProfit());
        assertEquals(new BigDecimal("12.00"), currentTrend.getExpense());
        assertEquals(new BigDecimal("-13.00"), currentTrend.getOther());
        assertEquals(5, result.getMetrics().size());
        verify(incomeStatementService, times(8)).getIncomeStatement(any(FmsReportListReqVO.class), eq(10L));
    }

    @Test
    public void testGetMetricDetail_success() {
        // mock 数据
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L).setInitialized(true)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(accountSet);
        when(closingPeriodService.getCurrentMonth(1L, accountSet.getStartTime()))
                .thenReturn(YearMonth.of(2026, 8));
        when(reportCommonService.isLineFormula(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class).contains("L"));
        when(incomeStatementService.getIncomeStatement(any(FmsReportListReqVO.class), eq(10L)))
                .thenAnswer(invocation -> buildMetricIncomeStatement(
                        invocation.<FmsReportListReqVO>getArgument(0).getStartMonth()));

        // 调用
        FmsHomeMetricDetailRespVO result = homeService.getMetricDetail(1L, "income", 10L);

        // 断言
        assertEquals("income", result.getKey());
        assertEquals("收入", result.getName());
        assertEquals(12, result.getTrends().size());
        assertEquals(new BigDecimal("100.00"), result.getTrends().get(11).getAmount());
        assertEquals(2, result.getStructure().size());
        assertEquals("5001", result.getStructure().get(0).getSubjectCode());
        assertEquals(new BigDecimal("80.00"), result.getStructure().get(0).getAmount());
        assertEquals("5051", result.getStructure().get(1).getSubjectCode());
        assertEquals(new BigDecimal("20.00"), result.getStructure().get(1).getAmount());
        verify(incomeStatementService, times(9)).getIncomeStatement(any(FmsReportListReqVO.class), eq(10L));
    }

    @Test
    public void testGetMetricDetail_metricInvalid() {
        // mock 数据
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setInitialized(true)
                        .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0)));

        // 调用，并断言异常
        assertServiceException(() -> homeService.getMetricDetail(1L, "unknown", 10L),
                HOME_METRIC_INVALID);
        verify(accountSetService).validateAccountSetReadPermission(1L, 10L);
    }

    @Test
    public void testGetHome_notInitialized() {
        // mock 数据
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setInitialized(false));

        // 调用，并断言异常
        assertServiceException(() -> homeService.getHome(1L, 10L), ACCOUNT_SET_NOT_INITIALIZED);
    }

    @Test
    public void testGetMetricDetail_lineFormula() {
        // mock 数据
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L).setInitialized(true)
                .setStartTime(LocalDateTime.of(2026, 8, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(accountSet);
        when(closingPeriodService.getCurrentMonth(1L, accountSet.getStartTime()))
                .thenReturn(YearMonth.of(2026, 8));
        when(reportCommonService.isLineFormula(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class).contains("L"));
        when(incomeStatementService.getIncomeStatement(any(FmsReportListReqVO.class), eq(10L)))
                .thenReturn(Arrays.asList(
                        new FmsReportItemRespVO().setRowNo(1).setCurrentAmount(new BigDecimal("100.00"))
                                .setFormula("[{\"subjectId\":1444,\"subjectNumber\":\"5001\","
                                        + "\"subjectName\":\"主营业务收入\",\"operator\":\"+\","
                                        + "\"currentAmount\":100.00}]"),
                        new FmsReportItemRespVO().setRowNo(2).setCurrentAmount(new BigDecimal("40.00"))
                                .setFormula("[{\"subjectId\":1453,\"subjectNumber\":\"5401\","
                                        + "\"subjectName\":\"主营业务成本\",\"operator\":\"+\","
                                        + "\"currentAmount\":40.00}]"),
                        buildReportItem(3, "0.00"), buildReportItem(11, "0.00"),
                        buildReportItem(14, "0.00"), buildReportItem(18, "0.00"),
                        new FmsReportItemRespVO().setRowNo(30).setCurrentAmount(new BigDecimal("60.00"))
                                .setFormula("[\"L1-L2\"]")));

        // 调用
        FmsHomeMetricDetailRespVO result = homeService.getMetricDetail(1L, "profit", 10L);

        // 断言
        assertEquals(new BigDecimal("60.00"), result.getTrends().get(11).getAmount());
        assertEquals(1, result.getStructure().size());
        assertEquals("5001", result.getStructure().get(0).getSubjectCode());
        assertEquals(new BigDecimal("100.00"), result.getStructure().get(0).getAmount());
    }

    // ========== 随机对象 ==========

    private List<FmsReportItemRespVO> buildIncomeStatement(String month) {
        if (!"2026-08".equals(month)) {
            return Collections.emptyList();
        }
        return Arrays.asList(buildReportItem(1, "100.00"), buildReportItem(2, "40.00"),
                buildReportItem(3, "5.00"), buildReportItem(11, "3.00"),
                buildReportItem(14, "4.00"), buildReportItem(18, "5.00"),
                buildReportItem(30, "30.00"));
    }

    private FmsReportItemRespVO buildReportItem(Integer rowNo, String amount) {
        return new FmsReportItemRespVO().setRowNo(rowNo).setCurrentAmount(new BigDecimal(amount));
    }

    private List<FmsReportItemRespVO> buildMetricIncomeStatement(String month) {
        if (!"2026-08".equals(month)) {
            return Collections.emptyList();
        }
        return Arrays.asList(new FmsReportItemRespVO().setRowNo(1).setCurrentAmount(new BigDecimal("100.00"))
                .setFormula("[{\"subjectId\":1444,\"subjectNumber\":\"5001\","
                        + "\"subjectName\":\"主营业务收入\",\"operator\":\"+\","
                        + "\"currentAmount\":80.00},{\"subjectId\":1445,"
                        + "\"subjectNumber\":\"5051\",\"subjectName\":\"其他业务收入\","
                        + "\"operator\":\"+\",\"currentAmount\":20.00}]"),
                buildReportItem(2, "40.00"), buildReportItem(3, "5.00"),
                buildReportItem(11, "3.00"), buildReportItem(14, "4.00"),
                buildReportItem(18, "5.00"), buildReportItem(30, "30.00"));
    }

}

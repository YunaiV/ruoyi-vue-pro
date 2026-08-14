package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingPeriodDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingPeriodMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherStatusEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetService;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@Import(FmsClosingPeriodServiceImpl.class)
public class FmsClosingPeriodServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsClosingPeriodServiceImpl closingPeriodService;
    @Resource
    private FmsClosingPeriodMapper closingPeriodMapper;
    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsInitialBalanceService initialBalanceService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsVoucherService voucherService;
    @MockBean
    private FmsBalanceSheetService balanceSheetService;
    @MockBean
    private FmsIncomeStatementService incomeStatementService;

    @Test
    public void testGetCurrentMonth_noClosingPeriod() {
        // 准备参数
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 0, 0);

        // 调用，并断言
        assertEquals(YearMonth.of(2026, 3), closingPeriodService.getCurrentMonth(1L, startTime));
    }

    @Test
    public void testGetCurrentMonth_hasClosingPeriod() {
        // mock 数据
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 6, 30, 0, 0)).setAccountSetId(1L));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 7, 31, 0, 0)).setAccountSetId(1L));

        // 调用，并断言
        assertEquals(YearMonth.of(2026, 8), closingPeriodService.getCurrentMonth(
                1L, LocalDateTime.of(2026, 3, 1, 0, 0)));
    }

    @Test
    public void testGetCurrentMonth() {
        // mock 数据
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        closingPeriodMapper.insert(buildClosingPeriod(1L, YearMonth.of(2026, 8)));

        // 调用，并断言
        assertEquals("2026-08", closingPeriodService.getCurrentMonth(1L, 10L));
    }

    @Test
    public void testGetClosingOverview_excludeClosingVoucherFromPendingCount() {
        // mock 数据
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true));
        when(voucherService.getVoucherListByPeriod(eq(1L), any(), any())).thenReturn(Arrays.asList(
                new FmsVoucherDO().setId(11L).setVoucherWordId(1L).setVoucherNumber(1)
                        .setStatus(FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()),
                new FmsVoucherDO().setId(12L).setVoucherWordId(1L).setVoucherNumber(2)
                        .setStatus(FmsVoucherStatusEnum.PENDING_REVIEW.getStatus()),
                new FmsVoucherDO().setId(13L).setVoucherWordId(1L).setVoucherNumber(3)
                        .setStatus(FmsVoucherStatusEnum.APPROVED.getStatus())));
        when(closingVoucherService.getClosingVoucherListByPeriod(eq(1L), any(), any()))
                .thenReturn(Collections.singletonList(new FmsClosingVoucherDO().setVoucherId(12L)));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Collections.emptyList());
        when(balanceSheetService.getBalanceSheet(any(), eq(10L))).thenReturn(Collections.emptyList());
        when(financeParameterService.getFinanceParameter(1L, 10L))
                .thenReturn(new FmsFinanceParameterDO().setVoucherReviewRequired(true));

        // 调用
        FmsClosingOverviewRespVO overview = closingPeriodService.getClosingOverview(
                buildClosingQueryReqVO(), 10L);

        // 断言
        assertEquals(1L, overview.getPendingVoucherCount());
        assertEquals(3L, overview.getVoucherCount());
    }

    @Test
    public void testGetClosingOverview_allChecksPassed() {
        // mock 数据
        mockClosePeriodChecks();

        // 调用
        FmsClosingOverviewRespVO overview = closingPeriodService.getClosingOverview(
                buildClosingQueryReqVO(), 10L);

        // 断言
        assertEquals(true, overview.getInitialBalanceBalanced());
        assertEquals(true, overview.getVoucherNumberContinuous());
        assertEquals(true, overview.getProfitLossVoucherGenerated());
        assertEquals(true, overview.getIncomeStatementBalanced());
        assertEquals(0, overview.getIncomeStatementUnmappedSubjectCount());
        assertEquals(true, overview.getBalanceSheetProfitLossTransferred());
        assertEquals(true, overview.getBalanceSheetBalanced());
        assertEquals(0, overview.getBalanceSheetUnmappedSubjectCount());
        assertEquals(true, overview.getCanClose());
    }

    @Test
    public void testClosePeriod_notCurrentPeriod() {
        // mock 数据
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        closingPeriodMapper.insert(buildClosingPeriod(1L, YearMonth.of(2026, 8)));

        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-07");

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.closePeriod(reqVO, 10L),
                CLOSING_NOT_CURRENT_PERIOD);
    }

    @Test
    public void testClosePeriod() {
        // mock 数据
        mockClosePeriodChecks();

        // 准备参数
        FmsClosingQueryReqVO reqVO = buildClosingQueryReqVO();

        // 调用
        closingPeriodService.closePeriod(reqVO, 10L);

        // 断言
        assertNotNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59, 59)));
        verify(closingVoucherService).updateClosingVoucherClosedByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 8, 1, 0, 0)), true);
    }

    @Test
    public void testClosePeriod_toTargetMonth() {
        // mock 数据
        mockClosePeriodChecks();

        // 准备参数：当前期间为 2026-08，结账目标为 2026-09
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO()
                .setAccountSetId(1L).setMonth("2026-09");

        // 调用
        closingPeriodService.closePeriod(reqVO, 10L);

        // 断言：目标期间之前的每个月都已登记结账
        assertNotNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTimeUtils.endOfMonth(
                        LocalDateTime.of(2026, 8, 1, 0, 0))));
        assertNotNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTimeUtils.endOfMonth(
                        LocalDateTime.of(2026, 9, 1, 0, 0))));
        verify(closingVoucherService).updateClosingVoucherClosedByPeriod(1L,
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 9, 1, 0, 0)), true);
    }

    @Test
    public void testClosePeriod_initialBalanceUnbalanced() {
        // mock 数据
        mockClosePeriodChecks();
        when(initialBalanceService.getTrialBalance(1L, 10L))
                .thenReturn(new FmsTrialBalanceRespVO().setBalanced(false));

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.closePeriod(buildClosingQueryReqVO(), 10L),
                CLOSING_INITIAL_BALANCE_UNBALANCED);
        assertPeriodNotClosed();
    }

    @Test
    public void testClosePeriod_voucherNumberDiscontinuous() {
        // mock 数据
        mockClosePeriodChecks();
        when(voucherService.getVoucherListByPeriod(eq(1L), any(), any())).thenReturn(java.util.Arrays.asList(
                new FmsVoucherDO().setVoucherWordId(1L).setVoucherNumber(1),
                new FmsVoucherDO().setVoucherWordId(1L).setVoucherNumber(3)));

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.closePeriod(buildClosingQueryReqVO(), 10L),
                CLOSING_VOUCHER_NUMBER_DISCONTINUOUS);
        assertPeriodNotClosed();
    }

    @Test
    public void testClosePeriod_profitLossActivityWithoutClosingVoucher() {
        // mock 数据
        mockClosePeriodChecks();
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.singletonList(
                new FmsSubjectDO().setId(1L).setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType())));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Collections.singletonList(
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(1L)
                        .setPeriodDebitAmount(new BigDecimal("100.00"))
                        .setPeriodCreditAmount(new BigDecimal("100.00"))));

        // 调用，并断言异常：损益净额为零，也不能绕过结转凭证检查
        assertServiceException(() -> closingPeriodService.closePeriod(buildClosingQueryReqVO(), 10L),
                CLOSING_PROFIT_LOSS_VOUCHER_MISSING);
        assertPeriodNotClosed();
    }

    @Test
    public void testClosePeriod_incomeStatementUnmappedSubject() {
        // mock 数据
        mockClosePeriodChecks();
        when(incomeStatementService.checkIncomeStatement(any(), eq(10L)))
                .thenReturn(new FmsIncomeStatementCheckRespVO().setBalanced(true)
                        .setUnmappedSubjects(Collections.singletonList(
                                new FmsIncomeStatementCheckRespVO.UnmappedSubject().setId(1L))));

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.closePeriod(buildClosingQueryReqVO(), 10L),
                CLOSING_REPORT_SUBJECT_UNMAPPED);
        assertPeriodNotClosed();
    }

    @Test
    public void testClosePeriod_balanceSheetUnbalanced() {
        // mock 数据
        mockClosePeriodChecks();
        when(balanceSheetService.checkBalanceSheet(any(), eq(10L)))
                .thenReturn(new FmsBalanceSheetCheckRespVO().setBalanced(false)
                        .setInitialBalanceBalanced(true).setProfitLossTransferred(true)
                        .setUnmappedSubjects(Collections.emptyList()));

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.closePeriod(buildClosingQueryReqVO(), 10L),
                CLOSING_BALANCE_SHEET_UNBALANCED);
        assertPeriodNotClosed();
    }

    @Test
    public void testCancelClosePeriod() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 7, 31, 0, 0)).setAccountSetId(1L));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 8, 31, 0, 0)).setAccountSetId(1L));
        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        closingPeriodService.cancelClosePeriod(reqVO, 10L);

        // 断言
        assertNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59, 59)));
        assertEquals(YearMonth.of(2026, 7), YearMonth.from(closingPeriodMapper
                .selectLatestByAccountSetId(1L).getClosingTime()));
        verify(closingVoucherService).updateClosingVoucherClosedByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 8, 1, 0, 0)), false);
    }

    @Test
    public void testCancelClosePeriod_targetAfterLatestPeriod() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 7, 31, 0, 0)).setAccountSetId(1L));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 8, 31, 0, 0)).setAccountSetId(1L));

        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-09");

        // 调用，并断言异常
        assertServiceException(() -> closingPeriodService.cancelClosePeriod(reqVO, 10L),
                CLOSING_PERIOD_NOT_CLOSED);
        assertNotNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 7, 1, 0, 0))));
        assertNotNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 8, 1, 0, 0))));
        verify(closingVoucherService, never()).updateClosingVoucherClosedByPeriod(
                any(), any(), any(), eq(false));
    }

    @Test
    public void testCancelClosePeriod_toTargetMonth() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true)
                        .setStartTime(LocalDateTime.of(2026, 7, 1, 0, 0)));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 7, 31, 0, 0)).setAccountSetId(1L));
        closingPeriodMapper.insert(new FmsClosingPeriodDO()
                .setClosingTime(LocalDateTime.of(2026, 8, 31, 0, 0)).setAccountSetId(1L));

        // 准备参数：从最近期间反结账到 2026-07
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO()
                .setAccountSetId(1L).setMonth("2026-07");

        // 调用
        closingPeriodService.cancelClosePeriod(reqVO, 10L);

        // 断言：目标期间及之后的结账均已撤销
        assertNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTimeUtils.endOfMonth(
                        LocalDateTime.of(2026, 7, 1, 0, 0))));
        assertNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTimeUtils.endOfMonth(
                        LocalDateTime.of(2026, 8, 1, 0, 0))));
        verify(closingVoucherService).updateClosingVoucherClosedByPeriod(1L,
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 7, 1, 0, 0)), false);
        verify(closingVoucherService).updateClosingVoucherClosedByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 8, 1, 0, 0)), false);
    }

    // ========== 随机对象 ==========

    private FmsClosingPeriodDO buildClosingPeriod(Long accountSetId, YearMonth currentMonth) {
        YearMonth previousMonth = currentMonth.minusMonths(1);
        return new FmsClosingPeriodDO().setAccountSetId(accountSetId)
                .setClosingTime(previousMonth.atEndOfMonth().atStartOfDay());
    }

    private FmsClosingQueryReqVO buildClosingQueryReqVO() {
        return new FmsClosingQueryReqVO().setAccountSetId(1L).setMonth("2026-08");
    }

    private void mockClosePeriodChecks() {
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L)
                .setInitialized(true).setStartTime(startTime));
        when(financeParameterService.getFinanceParameter(1L, 10L))
                .thenReturn(new FmsFinanceParameterDO().setVoucherReviewRequired(false));
        closingPeriodMapper.insert(buildClosingPeriod(1L, YearMonth.of(2026, 8)));
        when(voucherService.getVoucherListByPeriod(eq(1L), any(), any())).thenReturn(Collections.emptyList());
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Collections.emptyList());
        when(balanceSheetService.getBalanceSheet(any(), eq(10L))).thenReturn(Collections.emptyList());
        when(initialBalanceService.getTrialBalance(1L, 10L))
                .thenReturn(new FmsTrialBalanceRespVO().setBalanced(true));
        when(incomeStatementService.checkIncomeStatement(any(), eq(10L)))
                .thenReturn(new FmsIncomeStatementCheckRespVO().setBalanced(true)
                        .setUnmappedSubjects(Collections.emptyList()));
        when(balanceSheetService.checkBalanceSheet(any(), eq(10L)))
                .thenReturn(new FmsBalanceSheetCheckRespVO().setBalanced(true)
                        .setInitialBalanceBalanced(true).setProfitLossTransferred(true)
                        .setUnmappedSubjects(Collections.emptyList()));
    }

    private void assertPeriodNotClosed() {
        assertNull(closingPeriodMapper.selectByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2026, 8, 1, 0, 0))));
        verify(closingVoucherService, never()).updateClosingVoucherClosedByPeriod(any(), any(), any(), eq(true));
    }

}

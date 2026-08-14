package cn.iocoder.yudao.module.fms.service.report;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectAmountVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.LEDGER_PERIOD_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(FmsReportCommonServiceImpl.class)
public class FmsReportCommonServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsReportCommonServiceImpl reportCommonService;

    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsVoucherService voucherService;

    @Test
    public void testGetSubjectBalanceMap_excludeProfitLossClosingVoucher() {
        // mock 数据
        FmsSubjectDO profitLossRoot = new FmsSubjectDO().setId(100L).setParentId(0L)
                .setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO profitLossSubject = new FmsSubjectDO().setId(101L).setParentId(100L)
                .setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO retainedEarningsSubject = new FmsSubjectDO().setId(201L).setParentId(0L)
                .setType(FmsSubjectTypeEnum.EQUITY.getType());
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(
                Arrays.asList(profitLossRoot, profitLossSubject, retainedEarningsSubject));
        FmsLedgerSubjectBalanceRespVO profitLossBalance = buildProfitLossBalance(100L);
        FmsLedgerSubjectBalanceRespVO profitLossChildBalance = buildProfitLossBalance(101L);
        profitLossBalance.setChildren(Collections.singletonList(profitLossChildBalance));
        FmsLedgerSubjectBalanceRespVO retainedEarningsBalance = new FmsLedgerSubjectBalanceRespVO()
                .setSubjectId(201L).setOpeningDebitAmount(BigDecimal.ZERO)
                .setOpeningCreditAmount(new BigDecimal("10.00"))
                .setPeriodDebitAmount(new BigDecimal("30.00")).setPeriodCreditAmount(BigDecimal.ZERO)
                .setYearDebitAmount(new BigDecimal("30.00")).setYearCreditAmount(new BigDecimal("10.00"))
                .setEndingDebitAmount(new BigDecimal("20.00")).setEndingCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(profitLossBalance, retainedEarningsBalance));

        LocalDateTime startTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 1, 0, 0);
        FmsClosingVoucherDO julyClosingVoucher = new FmsClosingVoucherDO().setVoucherId(1L)
                .setVoucherTime(LocalDateTime.of(2026, 7, 31, 0, 0));
        FmsClosingVoucherDO augustClosingVoucher = new FmsClosingVoucherDO().setVoucherId(2L)
                .setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0));
        when(closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                1L, FmsClosingTypeEnum.PROFIT_LOSS.getType()))
                .thenReturn(new FmsClosingSchemeDO().setId(11L));
        when(closingVoucherService.getClosingVoucherListByClosingIdAndPeriod(11L, null, endTime))
                .thenReturn(Arrays.asList(julyClosingVoucher, augustClosingVoucher));
        when(voucherService.getVoucherSubjectAmountList(Collections.singletonList(1L)))
                .thenReturn(Arrays.asList(buildSubjectAmount(101L, "10.00", "0.00"),
                        buildSubjectAmount(201L, "0.00", "10.00")));
        when(voucherService.getVoucherSubjectAmountList(Collections.singletonList(2L)))
                .thenReturn(Arrays.asList(buildSubjectAmount(101L, "0.00", "30.00"),
                        buildSubjectAmount(201L, "30.00", "0.00")));
        when(voucherService.getVoucherSubjectAmountList(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(buildSubjectAmount(101L, "10.00", "30.00"),
                        buildSubjectAmount(201L, "30.00", "10.00")));

        // 调用
        Map<Long, FmsLedgerSubjectBalanceRespVO> result = reportCommonService.getSubjectBalanceMap(
                1L, YearMonth.of(2026, 8), YearMonth.of(2026, 8), 10L);

        // 断言
        assertProfitLossBalance(result.get(100L));
        assertProfitLossBalance(result.get(101L));
        assertEquals(new BigDecimal("10.00"), result.get(201L).getOpeningCreditAmount());
        assertEquals(new BigDecimal("30.00"), result.get(201L).getPeriodDebitAmount());
        assertEquals(new BigDecimal("20.00"), result.get(201L).getEndingDebitAmount());
    }

    @Test
    public void testGetPeriodType_periodInvalid() {
        // 调用，并断言异常
        assertServiceException(() -> reportCommonService.getPeriodType(202609, 202608),
                LEDGER_PERIOD_INVALID);
    }

    @Test
    public void testCalculateBalanceAmount_subjectBalanceByFirstChild() {
        // mock 数据
        FmsSubjectDO parent = new FmsSubjectDO().setId(100L).setBalanceDirection(1);
        FmsSubjectDO debitChild = new FmsSubjectDO().setId(101L).setParentId(100L).setBalanceDirection(1);
        FmsSubjectDO creditChild = new FmsSubjectDO().setId(102L).setParentId(100L).setBalanceDirection(1);
        Map<Long, FmsSubjectDO> subjectMap = new LinkedHashMap<>();
        subjectMap.put(parent.getId(), parent);
        subjectMap.put(debitChild.getId(), debitChild);
        subjectMap.put(creditChild.getId(), creditChild);
        Map<Long, FmsLedgerSubjectBalanceRespVO> balanceMap = new LinkedHashMap<>();
        balanceMap.put(parent.getId(), buildBalance(100L, "70.00", "0.00", "80.00", "0.00"));
        balanceMap.put(debitChild.getId(), buildBalance(101L, "100.00", "0.00", "120.00", "0.00"));
        balanceMap.put(creditChild.getId(), buildBalance(102L, "0.00", "30.00", "0.00", "40.00"));

        // 调用
        BigDecimal openingDebitAmount = reportCommonService.calculateBalanceAmount(
                FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule(), parent, balanceMap, subjectMap, true);
        BigDecimal openingCreditAmount = reportCommonService.calculateBalanceAmount(
                FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule(), parent, balanceMap, subjectMap, true);
        BigDecimal closingDebitAmount = reportCommonService.calculateBalanceAmount(
                FmsFormulaRuleEnum.SUBJECT_DEBIT_BALANCE.getRule(), parent, balanceMap, subjectMap, false);
        BigDecimal closingCreditAmount = reportCommonService.calculateBalanceAmount(
                FmsFormulaRuleEnum.SUBJECT_CREDIT_BALANCE.getRule(), parent, balanceMap, subjectMap, false);

        // 断言
        assertAmountEquals("100.00", openingDebitAmount);
        assertAmountEquals("30.00", openingCreditAmount);
        assertAmountEquals("120.00", closingDebitAmount);
        assertAmountEquals("40.00", closingCreditAmount);
    }

    @Test
    public void testCalculateIncomeOccurrenceAmount_netOppositeAmountForPlusOperator() {
        // mock 数据
        FmsSubjectDO subject = new FmsSubjectDO().setId(101L).setBalanceDirection(2);
        FmsLedgerSubjectBalanceRespVO balance = new FmsLedgerSubjectBalanceRespVO()
                .setPeriodDebitAmount(new BigDecimal("20.00")).setPeriodCreditAmount(new BigDecimal("100.00"))
                .setYearDebitAmount(new BigDecimal("30.00")).setYearCreditAmount(new BigDecimal("200.00"));

        // 调用，并断言
        assertAmountEquals("80.00", reportCommonService.calculateIncomeOccurrenceAmount(
                FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule(), subject, balance, true, "+"));
        assertAmountEquals("170.00", reportCommonService.calculateIncomeOccurrenceAmount(
                FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule(), subject, balance, false, "+"));
        assertAmountEquals("100.00", reportCommonService.calculateIncomeOccurrenceAmount(
                FmsFormulaRuleEnum.CREDIT_AMOUNT.getRule(), subject, balance, true, "-"));
    }

    // ========== 随机对象 ==========

    private FmsLedgerSubjectBalanceRespVO buildProfitLossBalance(Long subjectId) {
        return new FmsLedgerSubjectBalanceRespVO().setSubjectId(subjectId)
                .setOpeningDebitAmount(new BigDecimal("10.00")).setOpeningCreditAmount(BigDecimal.ZERO)
                .setPeriodDebitAmount(new BigDecimal("7.00")).setPeriodCreditAmount(new BigDecimal("30.00"))
                .setYearDebitAmount(new BigDecimal("17.00")).setYearCreditAmount(new BigDecimal("30.00"))
                .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("13.00"))
                .setChildren(Collections.emptyList());
    }

    private FmsLedgerSubjectBalanceRespVO buildBalance(Long subjectId, String openingDebitAmount,
            String openingCreditAmount, String endingDebitAmount, String endingCreditAmount) {
        return new FmsLedgerSubjectBalanceRespVO().setSubjectId(subjectId)
                .setOpeningDebitAmount(new BigDecimal(openingDebitAmount))
                .setOpeningCreditAmount(new BigDecimal(openingCreditAmount))
                .setEndingDebitAmount(new BigDecimal(endingDebitAmount))
                .setEndingCreditAmount(new BigDecimal(endingCreditAmount));
    }

    private FmsVoucherSubjectAmountVO buildSubjectAmount(Long subjectId, String debitAmount,
            String creditAmount) {
        return new FmsVoucherSubjectAmountVO().setSubjectId(subjectId)
                .setDebitAmount(new BigDecimal(debitAmount)).setCreditAmount(new BigDecimal(creditAmount));
    }

    private void assertProfitLossBalance(FmsLedgerSubjectBalanceRespVO balance) {
        assertAmountEquals("0.00", balance.getOpeningDebitAmount());
        assertAmountEquals("0.00", balance.getOpeningCreditAmount());
        assertAmountEquals("7.00", balance.getPeriodDebitAmount());
        assertAmountEquals("0.00", balance.getPeriodCreditAmount());
        assertAmountEquals("7.00", balance.getYearDebitAmount());
        assertAmountEquals("0.00", balance.getYearCreditAmount());
        assertAmountEquals("7.00", balance.getEndingDebitAmount());
        assertAmountEquals("0.00", balance.getEndingCreditAmount());
    }

    private void assertAmountEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

}

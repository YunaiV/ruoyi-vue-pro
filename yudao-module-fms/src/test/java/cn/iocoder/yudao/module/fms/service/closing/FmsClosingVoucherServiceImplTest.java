package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingVoucherGenerateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerEntryVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherEntrySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingVoucherMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingVoucherTypeEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_NOT_CURRENT_PERIOD;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_NOT_PERIOD_END;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsClosingVoucherServiceImpl.class)
public class FmsClosingVoucherServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsClosingVoucherServiceImpl closingVoucherService;
    @Resource
    private FmsClosingVoucherMapper closingVoucherMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsClosingPeriodService closingPeriodService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsVoucherService voucherService;
    @MockBean
    private FmsVoucherWordService voucherWordService;
    @MockBean
    private FmsIncomeStatementService incomeStatementService;

    @Test
    public void testGetClosingVoucherIdSet() {
        // mock 数据
        closingVoucherMapper.insert(new FmsClosingVoucherDO()
                .setAccountSetId(1L).setClosingId(11L).setVoucherId(21L));
        closingVoucherMapper.insert(new FmsClosingVoucherDO()
                .setAccountSetId(2L).setClosingId(12L).setVoucherId(22L));

        // 调用，并断言
        assertEquals(Collections.singleton(21L), closingVoucherService.getClosingVoucherIdSet(
                1L, Arrays.asList(21L, 22L, 23L)));
    }

    @Test
    public void testGetClosingVoucherListByClosingIdAndPeriod() {
        // mock 数据
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setAccountSetId(1L)
                .setClosingId(11L).setVoucherId(21L)
                .setVoucherTime(LocalDateTime.of(2026, 7, 31, 0, 0)));
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setAccountSetId(1L)
                .setClosingId(11L).setVoucherId(22L)
                .setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0)));
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setAccountSetId(1L)
                .setClosingId(12L).setVoucherId(23L)
                .setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0)));

        // 调用
        List<FmsClosingVoucherDO> result = closingVoucherService.getClosingVoucherListByClosingIdAndPeriod(
                11L, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 1, 0, 0));

        // 断言
        assertEquals(1, result.size());
        assertEquals(22L, result.get(0).getVoucherId());
    }

    @Test
    public void testGenerateProfitLossVoucher() {
        // mock 数据
        FmsSubjectDO revenueSubject = new FmsSubjectDO().setId(101L).setCode("5001")
                .setName("主营业务收入").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO expenseSubject = new FmsSubjectDO().setId(102L).setCode("5601")
                .setName("销售费用").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO profitSubject = new FmsSubjectDO().setId(103L).setCode("3103")
                .setName("本年利润").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsSubjectDO adjustmentSubject = new FmsSubjectDO().setId(104L).setCode("6000")
                .setName("以前年度损益调整").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO adjustmentClosingSubject = new FmsSubjectDO().setId(105L).setCode("3104")
                .setName("利润分配").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setPeriodDebitAmount(BigDecimal.ZERO).setPeriodCreditAmount(new BigDecimal("11300.00"))
                .setChildren(Collections.emptyList());
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setPeriodDebitAmount(new BigDecimal("2200.00")).setPeriodCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(revenueSubject, expenseSubject, profitSubject,
                        adjustmentSubject, adjustmentClosingSubject));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(revenueBalance, expenseBalance));
        when(voucherService.getNextVoucherNumber(eq(1L), eq(11L), any(), eq(10L))).thenReturn(4);
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(99L);
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setName("结转损益").setPeriodEnd(true)
                .setVoucherWordId(11L).setDigest("期末结转损益")
                .setVoucherType(FmsClosingVoucherTypeEnum.COMBINED_GAIN_AND_LOSS.getType())
                .setPriorYearAdjustmentSubjectId(104L).setAdjustmentClosingSubjectId(105L)
                .setOtherClosingSubjectId(103L).setReverseBalance(true)
                .setType(FmsClosingTypeEnum.PROFIT_LOSS.getType())
                .setAccountSetId(1L).setClosingDay(31);
        when(closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                1L, FmsClosingTypeEnum.PROFIT_LOSS.getType())).thenReturn(closing);

        // 准备参数
        FmsProfitLossGenerateReqVO reqVO = new FmsProfitLossGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        Long voucherId = closingVoucherService.generateProfitLossVoucher(reqVO, 10L);

        // 断言
        assertEquals(99L, voucherId);
        ArgumentCaptor<FmsVoucherSaveReqVO> voucherCaptor = ArgumentCaptor.forClass(FmsVoucherSaveReqVO.class);
        verify(voucherService).createVoucher(voucherCaptor.capture(), eq(10L));
        List<FmsVoucherEntrySaveReqVO> entries = voucherCaptor.getValue().getEntries();
        assertEquals(3, entries.size());
        assertEquals(4, voucherCaptor.getValue().getVoucherNumber());
        assertEquals(new BigDecimal("11300.00"), entries.get(0).getDebitAmount());
        assertEquals(new BigDecimal("2200.00"), entries.get(1).getCreditAmount());
        assertEquals(new BigDecimal("9100.00"), entries.get(2).getCreditAmount());
        assertEquals("期末结转损益", entries.get(0).getDigest());
        List<FmsClosingVoucherDO> closingVouchers = closingVoucherMapper.selectListByPeriod(1L,
                java.time.LocalDateTime.of(2026, 8, 1, 0, 0),
                java.time.LocalDateTime.of(2026, 8, 31, 23, 59, 59));
        assertEquals(1, closingVouchers.size());
        assertEquals(99L, closingVouchers.get(0).getVoucherId());
    }

    @Test
    public void testGenerateProfitLossVoucher_withAuxiliary() {
        // mock 数据
        FmsSubjectDO expenseSubject = new FmsSubjectDO().setId(102L).setCode("5601")
                .setName("销售费用").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType())
                .setAuxiliaryTypeIds(Collections.singletonList(11L));
        FmsSubjectDO profitSubject = new FmsSubjectDO().setId(103L).setCode("3103")
                .setName("本年利润").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsSubjectDO adjustmentSubject = new FmsSubjectDO().setId(104L).setCode("6000")
                .setName("以前年度损益调整").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO adjustmentClosingSubject = new FmsSubjectDO().setId(105L).setCode("3104")
                .setName("利润分配").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setPeriodDebitAmount(new BigDecimal("200.00")).setPeriodCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        FmsLedgerEntryVO ledgerEntry = new FmsLedgerEntryVO().setSubjectId(102L)
                .setDebitAmount(new BigDecimal("200.00")).setCreditAmount(BigDecimal.ZERO)
                .setAuxiliaries(Collections.singletonList(FmsVoucherEntryDO.AuxiliaryItem.builder()
                        .type(1).typeId(11L).itemId(21L).name("回归客户").build()));
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                expenseSubject, profitSubject, adjustmentSubject, adjustmentClosingSubject));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(expenseBalance));
        when(ledgerService.getEntryList(1L, LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0), 10L)).thenReturn(Collections.singletonList(ledgerEntry));
        when(voucherService.getNextVoucherNumber(eq(1L), eq(11L), any(), eq(10L))).thenReturn(4);
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(99L);
        when(closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                1L, FmsClosingTypeEnum.PROFIT_LOSS.getType())).thenReturn(new FmsClosingSchemeDO().setId(1L)
                .setName("结转损益").setPeriodEnd(true).setVoucherWordId(11L).setDigest("期末结转损益")
                .setVoucherType(FmsClosingVoucherTypeEnum.COMBINED_GAIN_AND_LOSS.getType())
                .setPriorYearAdjustmentSubjectId(104L).setAdjustmentClosingSubjectId(105L)
                .setOtherClosingSubjectId(103L).setReverseBalance(true)
                .setType(FmsClosingTypeEnum.PROFIT_LOSS.getType()).setAccountSetId(1L).setClosingDay(31));

        // 准备参数
        FmsProfitLossGenerateReqVO reqVO = new FmsProfitLossGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        closingVoucherService.generateProfitLossVoucher(reqVO, 10L);

        // 断言
        ArgumentCaptor<FmsVoucherSaveReqVO> voucherCaptor = ArgumentCaptor.forClass(FmsVoucherSaveReqVO.class);
        verify(voucherService).createVoucher(voucherCaptor.capture(), eq(10L));
        List<FmsVoucherEntrySaveReqVO> entries = voucherCaptor.getValue().getEntries();
        assertEquals(2, entries.size());
        assertEquals(4, voucherCaptor.getValue().getVoucherNumber());
        assertEquals(102L, entries.get(0).getSubjectId());
        assertEquals(new BigDecimal("200.00"), entries.get(0).getCreditAmount());
        assertEquals(1, entries.get(0).getAuxiliaries().size());
        assertEquals(11L, entries.get(0).getAuxiliaries().get(0).getTypeId());
        assertEquals(21L, entries.get(0).getAuxiliaries().get(0).getItemId());
        assertEquals(103L, entries.get(1).getSubjectId());
        assertEquals(new BigDecimal("200.00"), entries.get(1).getDebitAmount());
    }

    @Test
    public void testGenerateProfitLossVoucher_separateGainAndLoss() {
        // mock 数据
        FmsSubjectDO revenueSubject = new FmsSubjectDO().setId(101L).setCode("5001")
                .setName("主营业务收入").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO expenseSubject = new FmsSubjectDO().setId(102L).setCode("5601")
                .setName("销售费用").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO profitSubject = new FmsSubjectDO().setId(103L).setCode("3103")
                .setName("本年利润").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsSubjectDO adjustmentSubject = new FmsSubjectDO().setId(104L).setCode("6000")
                .setName("以前年度损益调整").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO adjustmentClosingSubject = new FmsSubjectDO().setId(105L).setCode("3104")
                .setName("利润分配").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setPeriodDebitAmount(BigDecimal.ZERO).setPeriodCreditAmount(new BigDecimal("11300.00"))
                .setChildren(Collections.emptyList());
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setPeriodDebitAmount(new BigDecimal("2200.00")).setPeriodCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                revenueSubject, expenseSubject, profitSubject,
                adjustmentSubject, adjustmentClosingSubject));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(revenueBalance, expenseBalance));
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(99L, 100L);
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setName("结转损益").setPeriodEnd(true)
                .setVoucherWordId(11L).setDigest("结转损益")
                .setVoucherType(FmsClosingVoucherTypeEnum.SEPARATE_GAIN_AND_LOSS.getType())
                .setPriorYearAdjustmentSubjectId(104L).setAdjustmentClosingSubjectId(105L)
                .setOtherClosingSubjectId(103L).setReverseBalance(true)
                .setType(FmsClosingTypeEnum.PROFIT_LOSS.getType())
                .setAccountSetId(1L).setClosingDay(30);
        when(closingSchemeService.getClosingSchemeByAccountSetIdAndType(
                1L, FmsClosingTypeEnum.PROFIT_LOSS.getType())).thenReturn(closing);

        // 准备参数
        FmsProfitLossGenerateReqVO reqVO = new FmsProfitLossGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        Long voucherId = closingVoucherService.generateProfitLossVoucher(reqVO, 10L);

        // 断言
        assertEquals(99L, voucherId);
        ArgumentCaptor<FmsVoucherSaveReqVO> voucherCaptor = ArgumentCaptor.forClass(FmsVoucherSaveReqVO.class);
        verify(voucherService, times(2)).createVoucher(voucherCaptor.capture(), eq(10L));
        List<FmsVoucherSaveReqVO> vouchers = voucherCaptor.getAllValues();
        assertEquals(2, vouchers.size());
        assertEquals(2, vouchers.get(0).getEntries().size());
        assertEquals(new BigDecimal("11300.00"), vouchers.get(0).getEntries().get(0).getDebitAmount());
        assertEquals(new BigDecimal("11300.00"), vouchers.get(0).getEntries().get(1).getCreditAmount());
        assertEquals(2, vouchers.get(1).getEntries().size());
        assertEquals(new BigDecimal("2200.00"), vouchers.get(1).getEntries().get(0).getCreditAmount());
        assertEquals(new BigDecimal("2200.00"), vouchers.get(1).getEntries().get(1).getDebitAmount());
        assertEquals(LocalDateTime.of(2026, 8, 30, 0, 0), vouchers.get(0).getVoucherTime());
        assertEquals(2, closingVoucherMapper.selectListByPeriod(1L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59, 59)).size());
    }

    @Test
    public void testGenerateClosingSchemeVoucher_yearBeginBalance() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setName("结转本月房租").setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.YEAR_BEGIN.getType()).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setAccountSetId(1L)
                .setSubjectRules(Arrays.asList(
                        FmsClosingSchemeDO.SubjectRule.builder().subjectId(102L).digest("结转本月房租")
                                .direction(FmsDebitCreditDirectionEnum.DEBIT.getType())
                                .amountRatio(new BigDecimal("100")).build(),
                        FmsClosingSchemeDO.SubjectRule.builder().subjectId(103L).digest("结转本月房租")
                                .direction(FmsDebitCreditDirectionEnum.CREDIT.getType())
                                .amountRatio(new BigDecimal("100")).build()));
        when(closingSchemeService.validateClosingSchemeExists(1L, closing.getId())).thenReturn(closing);
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setClosingId(closing.getId())
                .setVoucherId(77L).setVoucherTime(LocalDateTime.of(2026, 8, 31, 0, 0))
                .setAmount(new BigDecimal("1200.00")).setAccountSetId(1L));
        FmsLedgerSubjectBalanceRespVO sourceBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setOpeningDebitAmount(new BigDecimal("100.00"))
                .setOpeningCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("1200.00"))
                .setEndingCreditAmount(BigDecimal.ZERO).setChildren(Collections.emptyList());
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(sourceBalance));
        when(voucherService.getNextVoucherNumber(eq(1L), eq(11L), any(), eq(10L))).thenReturn(4);
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(88L);
        doAnswer(invocation -> {
            assertEquals(0, closingVoucherMapper.selectListByClosingId(closing.getId()).size());
            return null;
        }).when(voucherService).deleteVoucherList(1L, Collections.singletonList(77L), 10L);

        // 准备参数
        FmsClosingSchemeGenerateReqVO reqVO = new FmsClosingSchemeGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(closing.getId());
        reqVO.setMonth("2026-08");

        // 调用
        Long voucherId = closingVoucherService.generateClosingSchemeVoucher(reqVO, 10L);

        // 断言
        assertEquals(88L, voucherId);
        ArgumentCaptor<FmsVoucherSaveReqVO> voucherCaptor = ArgumentCaptor.forClass(FmsVoucherSaveReqVO.class);
        verify(voucherService).createVoucher(voucherCaptor.capture(), eq(10L));
        verify(voucherService).deleteVoucherList(1L, Collections.singletonList(77L), 10L);
        assertEquals(4, voucherCaptor.getValue().getVoucherNumber());
        assertEquals(new BigDecimal("100.00"),
                voucherCaptor.getValue().getEntries().get(0).getDebitAmount());
        assertEquals(new BigDecimal("100.00"),
                voucherCaptor.getValue().getEntries().get(1).getCreditAmount());
        List<FmsClosingVoucherDO> closingVouchers = closingVoucherMapper.selectListByClosingId(closing.getId());
        assertEquals(1, closingVouchers.size());
        assertEquals(88L, closingVouchers.get(0).getVoucherId());
        assertEquals(0, new BigDecimal("100.00").compareTo(closingVouchers.get(0).getAmount()));
    }

    @Test
    public void testGenerateClosingSchemeVoucher_nonPeriodEnd() {
        // mock 数据
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType()).setPeriodEnd(false);
        when(closingSchemeService.validateClosingSchemeExists(1L, closing.getId())).thenReturn(closing);
        // 准备参数
        FmsClosingSchemeGenerateReqVO reqVO = new FmsClosingSchemeGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(closing.getId());
        reqVO.setMonth("2026-08");

        // 调用，并断言
        assertServiceException(() -> closingVoucherService.generateClosingSchemeVoucher(reqVO, 10L),
                CLOSING_SCHEME_NOT_PERIOD_END);
    }

    @Test
    public void testGenerateClosingSchemeVoucher_localTax() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setName("计提地税").setPeriodEnd(true)
                .setVoucherWordId(11L).setType(FmsClosingTypeEnum.LOCAL_TAX.getType())
                .setAccountSetId(1L).setSubjectRules(Arrays.asList(
                        buildSubjectRule(301L, "计提教育费附加", 1, "3"),
                        buildSubjectRule(302L, "计提教育费附加", 2, "3"),
                        buildSubjectRule(303L, "计提城建费", 1, "7"),
                        buildSubjectRule(304L, "计提城建费", 2, "7"),
                        buildSubjectRule(305L, "计提地方教育费附加", 1, "2"),
                        buildSubjectRule(306L, "计提地方教育费附加", 2, "2")));
        when(closingSchemeService.validateClosingSchemeExists(1L, closing.getId())).thenReturn(closing);
        List<FmsSubjectDO> subjects = Arrays.asList(
                new FmsSubjectDO().setId(201L).setCode("222101").setName("应交增值税"),
                new FmsSubjectDO().setId(202L).setCode("222121").setName("应交消费税"));
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Arrays.asList(
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(201L)
                        .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("1000.00"))
                        .setChildren(Collections.emptyList()),
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(202L)
                        .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("500.00"))
                        .setChildren(Collections.emptyList())));
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(88L);

        // 准备参数
        FmsClosingSchemeGenerateReqVO reqVO = new FmsClosingSchemeGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(closing.getId());
        reqVO.setMonth("2026-08");

        // 调用
        Long voucherId = closingVoucherService.generateClosingSchemeVoucher(reqVO, 10L);

        // 断言
        assertEquals(88L, voucherId);
        ArgumentCaptor<FmsVoucherSaveReqVO> voucherCaptor = ArgumentCaptor.forClass(FmsVoucherSaveReqVO.class);
        verify(voucherService).createVoucher(voucherCaptor.capture(), eq(10L));
        List<FmsVoucherEntrySaveReqVO> entries = voucherCaptor.getValue().getEntries();
        assertEquals(new BigDecimal("45.00"), entries.get(0).getDebitAmount());
        assertEquals(new BigDecimal("105.00"), entries.get(2).getDebitAmount());
        assertEquals(new BigDecimal("30.00"), entries.get(4).getDebitAmount());
        assertEquals(0, new BigDecimal("180.00").compareTo(
                closingVoucherMapper.selectListByClosingId(closing.getId()).get(0).getAmount()));
    }

    @Test
    public void testGenerateClosingVoucherList_skipSchemeWithoutBalance() {
        // mock 数据
        List<FmsClosingSchemeDO.SubjectRule> subjectRules = Arrays.asList(
                FmsClosingSchemeDO.SubjectRule.builder().subjectId(102L).digest("期末结转")
                        .direction(FmsDebitCreditDirectionEnum.DEBIT.getType())
                        .amountRatio(new BigDecimal("100")).build(),
                FmsClosingSchemeDO.SubjectRule.builder().subjectId(103L).digest("期末结转")
                        .direction(FmsDebitCreditDirectionEnum.CREDIT.getType())
                        .amountRatio(new BigDecimal("100")).build());
        FmsClosingSchemeDO firstClosing = new FmsClosingSchemeDO().setId(1L)
                .setName("结转本月房租").setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType()).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setAccountSetId(1L)
                .setSubjectRules(subjectRules);
        FmsClosingSchemeDO secondClosing = new FmsClosingSchemeDO().setId(2L)
                .setName("结转本月物业费").setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType()).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setAccountSetId(1L)
                .setSubjectRules(subjectRules);
        when(closingSchemeService.validateClosingSchemeExists(1L, firstClosing.getId())).thenReturn(firstClosing);
        when(closingSchemeService.validateClosingSchemeExists(1L, secondClosing.getId())).thenReturn(secondClosing);
        FmsLedgerSubjectBalanceRespVO sourceBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setEndingDebitAmount(new BigDecimal("1200.00"))
                .setEndingCreditAmount(BigDecimal.ZERO).setChildren(Collections.emptyList());
        FmsLedgerSubjectBalanceRespVO zeroBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setEndingDebitAmount(BigDecimal.ZERO)
                .setEndingCreditAmount(BigDecimal.ZERO).setChildren(Collections.emptyList());
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(sourceBalance), Collections.singletonList(zeroBalance));
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(88L);
        LocalDateTime voucherTime = LocalDateTime.of(2026, 8, 31, 0, 0);
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setClosingId(firstClosing.getId())
                .setVoucherId(801L).setVoucherTime(voucherTime).setAmount(new BigDecimal("1200.00"))
                .setClosed(false).setAccountSetId(1L));
        closingVoucherMapper.insert(new FmsClosingVoucherDO().setClosingId(secondClosing.getId())
                .setVoucherId(802L).setVoucherTime(voucherTime).setAmount(new BigDecimal("1200.00"))
                .setClosed(false).setAccountSetId(1L));

        // 准备参数
        FmsClosingVoucherGenerateReqVO reqVO = new FmsClosingVoucherGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setIds(Arrays.asList(firstClosing.getId(), secondClosing.getId()));
        reqVO.setMonth("2026-08");

        // 调用
        List<Long> voucherIds = closingVoucherService.generateClosingVoucherList(reqVO, 10L);

        // 断言
        assertEquals(Collections.singletonList(88L), voucherIds);
        assertEquals(1, closingVoucherMapper.selectListByClosingId(firstClosing.getId()).size());
        assertEquals(0, closingVoucherMapper.selectListByClosingId(secondClosing.getId()).size());
        verify(voucherService).deleteVoucherList(1L, Arrays.asList(801L, 802L), 10L);
        verify(voucherService, times(1)).createVoucher(any(), eq(10L));
    }

    @Test
    public void testGenerateClosingVoucherList_nonPeriodEnd() {
        // mock 数据
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(
                new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setId(1L)
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType()).setPeriodEnd(false);
        when(closingSchemeService.validateClosingSchemeExists(1L, closing.getId())).thenReturn(closing);
        // 准备参数
        FmsClosingVoucherGenerateReqVO reqVO = new FmsClosingVoucherGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setIds(Collections.singletonList(closing.getId()));
        reqVO.setMonth("2026-08");

        // 调用，并断言
        assertServiceException(() -> closingVoucherService.generateClosingVoucherList(reqVO, 10L),
                CLOSING_SCHEME_NOT_PERIOD_END);
    }

    @Test
    public void testGenerateClosingVoucherList_rollbackWhenLaterSchemeFails() {
        // mock 数据
        List<FmsClosingSchemeDO.SubjectRule> subjectRules = Arrays.asList(
                FmsClosingSchemeDO.SubjectRule.builder().subjectId(102L).digest("期末结转")
                        .direction(FmsDebitCreditDirectionEnum.DEBIT.getType())
                        .amountRatio(new BigDecimal("100")).build(),
                FmsClosingSchemeDO.SubjectRule.builder().subjectId(103L).digest("期末结转")
                        .direction(FmsDebitCreditDirectionEnum.CREDIT.getType())
                        .amountRatio(new BigDecimal("100")).build());
        FmsClosingSchemeDO firstClosing = new FmsClosingSchemeDO().setId(1L)
                .setName("结转本月房租").setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType()).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setAccountSetId(1L)
                .setSubjectRules(subjectRules);
        FmsClosingSchemeDO secondClosing = new FmsClosingSchemeDO().setId(2L)
                .setName("结转本月物业费").setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType()).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.REGULAR.getType()).setAccountSetId(1L)
                .setSubjectRules(subjectRules);
        when(closingSchemeService.validateClosingSchemeExists(1L, firstClosing.getId())).thenReturn(firstClosing);
        when(closingSchemeService.validateClosingSchemeExists(1L, secondClosing.getId())).thenReturn(secondClosing);
        FmsLedgerSubjectBalanceRespVO sourceBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setEndingDebitAmount(new BigDecimal("1200.00"))
                .setEndingCreditAmount(BigDecimal.ZERO).setChildren(Collections.emptyList());
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(sourceBalance));
        when(voucherService.createVoucher(any(), eq(10L))).thenReturn(88L)
                .thenThrow(new IllegalStateException("第二个方案生成失败"));

        // 准备参数
        FmsClosingVoucherGenerateReqVO reqVO = new FmsClosingVoucherGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setIds(Arrays.asList(firstClosing.getId(), secondClosing.getId()));
        reqVO.setMonth("2026-08");

        // 调用，并断言
        assertThrows(IllegalStateException.class,
                () -> closingVoucherService.generateClosingVoucherList(reqVO, 10L));
        assertEquals(0, closingVoucherMapper.selectListByClosingId(firstClosing.getId()).size());
        assertEquals(0, closingVoucherMapper.selectListByClosingId(secondClosing.getId()).size());
    }

    @Test
    public void testGenerateProfitLossVoucher_notCurrentPeriod() {
        // mock 数据
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(new FmsAccountSetDO().setId(1L).setInitialized(true).setStartTime(startTime));
        when(closingPeriodService.getCurrentMonth(1L, startTime)).thenReturn(YearMonth.of(2026, 8));

        // 准备参数
        FmsProfitLossGenerateReqVO reqVO = new FmsProfitLossGenerateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-09");

        // 调用，并断言
        assertServiceException(() -> closingVoucherService.generateProfitLossVoucher(reqVO, 10L),
                CLOSING_NOT_CURRENT_PERIOD);
    }

    private FmsClosingSchemeDO.SubjectRule buildSubjectRule(
            Long subjectId, String digest, Integer direction, String amountRatio) {
        return FmsClosingSchemeDO.SubjectRule.builder().subjectId(subjectId).digest(digest)
                .direction(direction).amountRatio(new BigDecimal(amountRatio)).build();
    }

    // ========== 随机对象 ==========

}

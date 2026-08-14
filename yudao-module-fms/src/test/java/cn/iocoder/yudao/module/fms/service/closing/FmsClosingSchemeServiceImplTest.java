package cn.iocoder.yudao.module.fms.service.closing;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossSettingsSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsSpecialClosingSettingsSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingSchemeMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTypeEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingVoucherTypeEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_IN_USE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_RATIO_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsClosingSchemeServiceImpl.class)
public class FmsClosingSchemeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsClosingSchemeServiceImpl closingSchemeService;
    @Resource
    private FmsClosingSchemeMapper closingSchemeMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsVoucherWordService voucherWordService;
    @MockBean
    private FmsIncomeStatementService incomeStatementService;

    @BeforeEach
    public void before() {
        when(financeParameterService.getFinanceParameter(1L)).thenReturn(new FmsFinanceParameterDO()
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE));
        when(financeParameterService.convertStandardSubjectCode(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testGetClosingSchemeCountByVoucherWordId() {
        // mock 数据
        closingSchemeMapper.insert(new FmsClosingSchemeDO().setName("结转损益")
                .setAccountSetId(1L).setVoucherWordId(11L));
        closingSchemeMapper.insert(new FmsClosingSchemeDO().setName("结转损益")
                .setAccountSetId(2L).setVoucherWordId(11L));

        // 调用，并断言
        assertEquals(1L, closingSchemeService.getClosingSchemeCountByVoucherWordId(1L, 11L));
        assertEquals(0L, closingSchemeService.getClosingSchemeCountByVoucherWordId(1L, 12L));
    }

    @Test
    public void testSaveProfitLossSettings() {
        // mock 数据
        FmsSubjectDO adjustmentSubject = new FmsSubjectDO().setId(101L).setCode("6000")
                .setName("以前年度损益调整").setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        FmsSubjectDO adjustmentClosingSubject = new FmsSubjectDO().setId(102L).setCode("3104")
                .setName("利润分配").setType(FmsSubjectTypeEnum.EQUITY.getType());
        FmsSubjectDO otherClosingSubject = new FmsSubjectDO().setId(103L).setCode("3103")
                .setName("本年利润").setType(FmsSubjectTypeEnum.EQUITY.getType());
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                adjustmentSubject, adjustmentClosingSubject, otherClosingSubject));

        // 准备参数
        FmsProfitLossSettingsSaveReqVO reqVO = new FmsProfitLossSettingsSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setVoucherWordId(11L);
        reqVO.setDigest("期末结转损益");
        reqVO.setVoucherType(FmsClosingVoucherTypeEnum.SEPARATE_GAIN_AND_LOSS.getType());
        reqVO.setPriorYearAdjustmentSubjectId(101L);
        reqVO.setAdjustmentClosingSubjectId(102L);
        reqVO.setOtherClosingSubjectId(103L);
        reqVO.setReverseBalance(true);
        reqVO.setClosingDay(28);

        // 调用
        Long id = closingSchemeService.saveProfitLossSettings(reqVO, 10L);

        // 断言
        FmsClosingSchemeDO closing = closingSchemeMapper.selectById(id);
        assertEquals("期末结转损益", closing.getDigest());
        assertEquals(FmsClosingVoucherTypeEnum.SEPARATE_GAIN_AND_LOSS.getType(), closing.getVoucherType());
        assertEquals(101L, closing.getPriorYearAdjustmentSubjectId());
        assertEquals(102L, closing.getAdjustmentClosingSubjectId());
        assertEquals(103L, closing.getOtherClosingSubjectId());
        assertEquals(28, closing.getClosingDay());
        verify(voucherWordService).validateVoucherWordExists(1L, 11L);
    }

    @Test
    public void testCreateClosingScheme() {
        // mock 数据
        FmsSubjectDO sourceSubject = new FmsSubjectDO().setId(101L).setCode("6602")
                .setName("管理费用");
        FmsSubjectDO debitSubject = new FmsSubjectDO().setId(102L).setCode("660201")
                .setName("房租");
        FmsSubjectDO creditSubject = new FmsSubjectDO().setId(103L).setCode("2701")
                .setName("长期待摊费用");
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(sourceSubject, debitSubject, creditSubject));

        // 准备参数
        FmsClosingSchemeSaveReqVO reqVO = buildClosingSchemeSaveReqVO();

        // 调用
        Long id = closingSchemeService.createClosingScheme(reqVO, 10L);

        // 断言
        FmsClosingSchemeDO closing = closingSchemeMapper.selectById(id);
        assertEquals("结转本月房租", closing.getName());
        assertEquals(FmsClosingTypeEnum.REGULAR.getType(), closing.getType());
        assertEquals(2, closing.getSubjectRules().size());
        assertEquals("660201", closing.getSubjectRules().get(0).getSubjectCode());
        assertEquals("2701", closing.getSubjectRules().get(1).getSubjectCode());
    }

    @Test
    public void testUpdateClosingScheme_ratioInvalid() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setName("原结转方案")
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType());
        closingSchemeMapper.insert(closing);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setCode("6602"),
                new FmsSubjectDO().setId(102L).setCode("660201"),
                new FmsSubjectDO().setId(103L).setCode("2701")));

        // 准备参数
        FmsClosingSchemeSaveReqVO reqVO = buildClosingSchemeSaveReqVO();
        reqVO.setId(closing.getId());
        reqVO.setName("非法比例方案");
        reqVO.getSubjects().get(0).setAmountRatio(new BigDecimal("99"));

        // 调用，并断言异常
        assertServiceException(() -> closingSchemeService.updateClosingScheme(reqVO, 10L),
                CLOSING_SCHEME_RATIO_INVALID);
        assertEquals("原结转方案", closingSchemeMapper.selectById(closing.getId()).getName());
    }

    @Test
    public void testDeleteClosingScheme_inUse() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setName("已生成凭证方案")
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType());
        closingSchemeMapper.insert(closing);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(closingVoucherService.getClosingVoucherCountByClosingId(closing.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> closingSchemeService.deleteClosingScheme(1L, closing.getId(), 10L),
                CLOSING_SCHEME_IN_USE);
        assertNotNull(closingSchemeMapper.selectById(closing.getId()));
    }

    @Test
    public void testUpdateSpecialClosingSettings() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setName("计提附加税")
                .setAccountSetId(1L).setVoucherWordId(11L)
                .setType(FmsClosingTypeEnum.LOCAL_TAX.getType());
        closingSchemeMapper.insert(closing);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Arrays.asList(
                new FmsSubjectDO().setId(101L).setCode("540303"),
                new FmsSubjectDO().setId(102L).setCode("222113")));

        // 准备参数
        FmsSpecialClosingSettingsSaveReqVO reqVO = new FmsSpecialClosingSettingsSaveReqVO();
        reqVO.setId(closing.getId());
        reqVO.setAccountSetId(1L);
        reqVO.setVoucherWordId(12L);
        FmsSpecialClosingSettingsSaveReqVO.SubjectRule debitRule =
                new FmsSpecialClosingSettingsSaveReqVO.SubjectRule();
        debitRule.setSubjectId(101L);
        debitRule.setDigest("计提附加税");
        debitRule.setDirection(FmsDebitCreditDirectionEnum.DEBIT.getType());
        debitRule.setAmountRatio(new BigDecimal("5"));
        FmsSpecialClosingSettingsSaveReqVO.SubjectRule creditRule =
                new FmsSpecialClosingSettingsSaveReqVO.SubjectRule();
        creditRule.setSubjectId(102L);
        creditRule.setDigest("计提附加税");
        creditRule.setDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        creditRule.setAmountRatio(new BigDecimal("5"));
        reqVO.setSubjects(Arrays.asList(debitRule, creditRule));

        // 调用
        closingSchemeService.updateSpecialClosingSettings(reqVO, 10L);

        // 断言
        FmsClosingSchemeDO actual = closingSchemeMapper.selectById(closing.getId());
        assertEquals(12L, actual.getVoucherWordId());
        assertEquals("540303", actual.getSubjectRules().get(0).getSubjectCode());
        assertEquals("222113", actual.getSubjectRules().get(1).getSubjectCode());
        verify(voucherWordService).validateVoucherWordExists(1L, 12L);
    }

    @Test
    public void testInitializeDefaultClosingSchemes() {
        // mock 数据
        List<FmsSubjectDO> subjects = buildSpecialClosingSubjects();
        subjects.add(new FmsSubjectDO().setId(401L).setCode("6000"));
        subjects.add(new FmsSubjectDO().setId(402L).setCode("310415"));
        subjects.add(new FmsSubjectDO().setId(403L).setCode("3103"));
        subjects.add(new FmsSubjectDO().setId(404L).setCode("222101"));
        subjects.add(new FmsSubjectDO().setId(405L).setCode("222121"));
        when(voucherWordService.getVoucherWordList(1L)).thenReturn(Collections.singletonList(
                new FmsVoucherWordDO().setId(11L).setDefaultStatus(true)));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);

        // 调用
        closingSchemeService.initializeDefaultClosingSchemes(1L, 10L);
        closingSchemeService.initializeDefaultClosingSchemes(1L, 10L);

        // 断言
        List<FmsClosingSchemeDO> closings = closingSchemeMapper.selectListByAccountSetId(1L);
        assertEquals(4, closings.size());
        FmsClosingSchemeDO profitLossClosing = CollUtil.findOne(closings,
                item -> item.getType().equals(FmsClosingTypeEnum.PROFIT_LOSS.getType()));
        assertNotNull(profitLossClosing);
        assertEquals(11L, profitLossClosing.getVoucherWordId());
        assertEquals(401L, profitLossClosing.getPriorYearAdjustmentSubjectId());
        assertEquals(402L, profitLossClosing.getAdjustmentClosingSubjectId());
        assertEquals(403L, profitLossClosing.getOtherClosingSubjectId());
        assertEquals(FmsClosingTypeEnum.PROFIT_LOSS.getDigest(), profitLossClosing.getDigest());
        assertEquals(FmsClosingVoucherTypeEnum.COMBINED_GAIN_AND_LOSS.getType(),
                profitLossClosing.getVoucherType());
        assertEquals(FmsClosingTypeEnum.UNPAID_VAT.getDigest(), CollUtil.findOne(closings,
                item -> item.getType().equals(FmsClosingTypeEnum.UNPAID_VAT.getType())).getDigest());
        assertEquals(FmsClosingTypeEnum.LOCAL_TAX.getDigest(), CollUtil.findOne(closings,
                item -> item.getType().equals(FmsClosingTypeEnum.LOCAL_TAX.getType())).getDigest());
        assertEquals(FmsClosingTypeEnum.INCOME_TAX.getDigest(), CollUtil.findOne(closings,
                item -> item.getType().equals(FmsClosingTypeEnum.INCOME_TAX.getType())).getDigest());
    }

    @Test
    public void testGetClosingSchemeList_notInitializeOnQuery() {
        // mock 数据
        List<FmsSubjectDO> subjects = buildSpecialClosingSubjects();
        FmsSubjectDO vatSubject = new FmsSubjectDO().setId(201L).setCode("222101")
                .setName("应交增值税");
        FmsSubjectDO consumptionTaxSubject = new FmsSubjectDO().setId(202L).setCode("222121")
                .setName("应交消费税");
        subjects.add(vatSubject);
        subjects.add(consumptionTaxSubject);
        subjects.add(new FmsSubjectDO().setId(203L).setCode("6000"));
        subjects.add(new FmsSubjectDO().setId(204L).setCode("310415"));
        subjects.add(new FmsSubjectDO().setId(205L).setCode("3103"));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(voucherWordService.getVoucherWordList(1L)).thenReturn(Collections.singletonList(
                new FmsVoucherWordDO().setId(11L).setDefaultStatus(true)));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Arrays.asList(
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(201L)
                        .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("1000.00"))
                        .setChildren(Collections.emptyList()),
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(202L)
                        .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("500.00"))
                        .setChildren(Collections.emptyList())));
        when(incomeStatementService.getIncomeStatement(any(), eq(10L))).thenReturn(Arrays.asList(
                new FmsReportItemRespVO().setRowNo(30).setYearAmount(new BigDecimal("10000.00")),
                new FmsReportItemRespVO().setRowNo(30).setYearAmount(new BigDecimal("20000.00")),
                new FmsReportItemRespVO().setRowNo(31).setYearAmount(new BigDecimal("1000.00"))));
        closingSchemeService.initializeDefaultClosingSchemes(1L, 10L);
        Long closingCount = closingSchemeMapper.selectCount();

        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        List<FmsClosingSchemeRespVO> results = closingSchemeService.getClosingSchemeList(reqVO, 10L);

        // 断言
        assertEquals(4, results.size());
        FmsClosingSchemeRespVO unpaidVatClosing = CollUtil.findOne(results,
                item -> item.getType().equals(FmsClosingTypeEnum.UNPAID_VAT.getType()));
        FmsClosingSchemeRespVO localTaxClosing = CollUtil.findOne(results,
                item -> item.getType().equals(FmsClosingTypeEnum.LOCAL_TAX.getType()));
        FmsClosingSchemeRespVO incomeTaxClosing = CollUtil.findOne(results,
                item -> item.getType().equals(FmsClosingTypeEnum.INCOME_TAX.getType()));
        assertEquals(new BigDecimal("1000.00"), unpaidVatClosing.getBalance());
        assertEquals(new BigDecimal("180.00"), localTaxClosing.getBalance());
        assertEquals(new BigDecimal("1500.00"), incomeTaxClosing.getBalance());
        assertEquals(6, localTaxClosing.getSubjects().size());
        assertEquals(new BigDecimal("25"), incomeTaxClosing.getSubjects().get(0).getAmountRatio());
        assertEquals(closingCount, closingSchemeMapper.selectCount());
    }

    @Test
    public void testGetClosingSchemeList_yearBeginBalance() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setName("年初余额结转")
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType()).setPeriodEnd(true)
                .setSubjectId(101L).setFormulaRule(FmsFormulaRuleEnum.DEBIT_BALANCE.getRule())
                .setTimeType(FmsClosingTimeTypeEnum.YEAR_BEGIN.getType());
        closingSchemeMapper.insert(closing);
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.singletonList(
                new FmsSubjectDO().setId(101L).setCode("1001")));
        FmsLedgerSubjectBalanceRespVO currentBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setOpeningDebitAmount(new BigDecimal("150.00")).setOpeningCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("160.00")).setEndingCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        FmsLedgerSubjectBalanceRespVO yearBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setOpeningDebitAmount(new BigDecimal("100.00")).setOpeningCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("160.00")).setEndingCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(currentBalance), Collections.singletonList(yearBalance));
        when(closingVoucherService.getClosingVoucherListByPeriod(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        List<FmsClosingSchemeRespVO> results = closingSchemeService.getClosingSchemeList(reqVO, 10L);

        // 断言
        assertEquals(1, results.size());
        assertEquals(new BigDecimal("100.00"), results.get(0).getBalance());
    }

    @Test
    public void testGetClosingSchemeList_sumGeneratedVoucherAmounts() {
        // mock 数据
        FmsClosingSchemeDO closing = new FmsClosingSchemeDO().setName("结转损益")
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.PROFIT_LOSS.getType()).setPeriodEnd(true);
        closingSchemeMapper.insert(closing);
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Collections.emptyList());
        when(closingVoucherService.getClosingVoucherListByPeriod(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(
                        new FmsClosingVoucherDO().setClosingId(closing.getId()).setVoucherId(801L)
                                .setAmount(new BigDecimal("400.00")),
                        new FmsClosingVoucherDO().setClosingId(closing.getId()).setVoucherId(802L)
                                .setAmount(new BigDecimal("600.00"))));

        // 调用
        List<FmsClosingSchemeRespVO> results = closingSchemeService.getClosingSchemeList(
                new FmsClosingQueryReqVO().setAccountSetId(1L).setMonth("2026-08"), 10L);

        // 断言
        assertEquals(1, results.size());
        assertEquals(new BigDecimal("1000.00"), results.get(0).getBalance());
        assertEquals(Arrays.asList(801L, 802L), results.get(0).getVoucherIds());
    }

    @Test
    public void testGetClosingSchemeList_filterNonPeriodEndRegularScheme() {
        // mock 数据
        closingSchemeMapper.insert(new FmsClosingSchemeDO().setName("非期末自动结转方案")
                .setAccountSetId(1L).setType(FmsClosingTypeEnum.REGULAR.getType()).setPeriodEnd(false));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        // 准备参数
        FmsClosingQueryReqVO reqVO = new FmsClosingQueryReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setMonth("2026-08");

        // 调用
        List<FmsClosingSchemeRespVO> results = closingSchemeService.getClosingSchemeList(reqVO, 10L);

        // 断言
        assertEquals(0, results.size());
    }

    // ========== 随机对象 ==========

    private FmsClosingSchemeSaveReqVO buildClosingSchemeSaveReqVO() {
        FmsClosingSchemeSaveReqVO reqVO = new FmsClosingSchemeSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setName("结转本月房租");
        reqVO.setPeriodEnd(true);
        reqVO.setSubjectId(101L);
        reqVO.setFormulaRule(FmsFormulaRuleEnum.BALANCE.getRule());
        reqVO.setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType());
        reqVO.setVoucherWordId(11L);
        FmsClosingSchemeSaveReqVO.SubjectRule debitRule = new FmsClosingSchemeSaveReqVO.SubjectRule();
        debitRule.setSubjectId(102L);
        debitRule.setDigest("结转本月房租");
        debitRule.setDirection(FmsDebitCreditDirectionEnum.DEBIT.getType());
        debitRule.setAmountRatio(new BigDecimal("100"));
        FmsClosingSchemeSaveReqVO.SubjectRule creditRule = new FmsClosingSchemeSaveReqVO.SubjectRule();
        creditRule.setSubjectId(103L);
        creditRule.setDigest("结转本月房租");
        creditRule.setDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        creditRule.setAmountRatio(new BigDecimal("100"));
        reqVO.setSubjects(Arrays.asList(debitRule, creditRule));
        return reqVO;
    }

    private List<FmsSubjectDO> buildSpecialClosingSubjects() {
        return new ArrayList<>(Arrays.asList(
                new FmsSubjectDO().setId(301L).setCode("22210104"),
                new FmsSubjectDO().setId(302L).setCode("222102"),
                new FmsSubjectDO().setId(303L).setCode("540310"),
                new FmsSubjectDO().setId(304L).setCode("222113"),
                new FmsSubjectDO().setId(305L).setCode("540303"),
                new FmsSubjectDO().setId(306L).setCode("222117"),
                new FmsSubjectDO().setId(307L).setCode("540313"),
                new FmsSubjectDO().setId(308L).setCode("222114"),
                new FmsSubjectDO().setId(309L).setCode("5801"),
                new FmsSubjectDO().setId(310L).setCode("222111")));
    }

}

package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.balance.FmsBalanceSheetConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.balance.FmsBalanceSheetReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.balance.FmsBalanceSheetConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.balance.FmsBalanceSheetReportMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingOverviewRespVO;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({FmsBalanceSheetServiceImpl.class, FmsReportCommonServiceImpl.class})
public class FmsBalanceSheetServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsBalanceSheetServiceImpl balanceSheetService;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;
    @Resource
    private FmsBalanceSheetConfigMapper balanceSheetConfigMapper;
    @Resource
    private FmsBalanceSheetReportMapper balanceSheetReportMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsInitialBalanceService initialBalanceService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsClosingPeriodService closingPeriodService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsVoucherService voucherService;

    private FmsSubjectDO cashSubject;

    @BeforeEach
    public void before() {
        cashSubject = new FmsSubjectDO().setId(103L).setCode("1001").setName("库存现金")
                .setBalanceDirection(1).setLevel(1).setAccountSetId(1L);
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(accountSet);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(accountSet);
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Collections.singletonList(cashSubject));
        when(financeParameterService.getFinanceParameter(1L)).thenReturn(new FmsFinanceParameterDO()
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE));
        when(financeParameterService.convertStandardSubjectCode(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testGetBalanceSheet_pairRowsAndLineFormula() {
        // mock 数据
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("流动资产：").setRowNo(0)
                .setFormula("[]").setEditable(false).setSort(0).setRowId(0)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("流动负债：").setRowNo(0)
                .setFormula("[]").setEditable(false).setSort(32).setRowId(0)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("货币资金").setRowNo(1)
                .setFormula("[{\"operator\":\"+\",\"rules\":0,\"subjectNumber\":\"1001\"}]")
                .setEditable(true).setSort(1).setRowId(1)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("资产总计").setRowNo(30)
                .setFormula("[\"L1\"]").setEditable(false).setSort(2).setRowId(2)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(1));
        FmsLedgerSubjectBalanceRespVO balance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(103L)
                .setSubjectCode("1001").setOpeningDebitAmount(BigDecimal.ZERO)
                .setOpeningCreditAmount(new BigDecimal("100.00"))
                .setEndingDebitAmount(new BigDecimal("900.00")).setEndingCreditAmount(BigDecimal.ZERO)
                .setChildren(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(balance));

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsBalanceSheetRowRespVO> result = balanceSheetService.getBalanceSheet(reqVO, 10L);

        // 断言
        assertEquals(3, result.size());
        assertNotNull(CollUtil.getFirst(result).getAssetName());
        assertNotNull(CollUtil.getFirst(result).getLiabilityName());
        assertEquals(new BigDecimal("900.00"), result.get(1).getAssetClosingAmount());
        assertEquals(new BigDecimal("900.00"), result.get(2).getAssetClosingAmount());
    }

    @Test
    public void testUpdateBalanceSheetFormula() {
        // mock 数据
        FmsBalanceSheetConfigDO config = new FmsBalanceSheetConfigDO().setName("货币资金").setRowNo(1)
                .setFormula("[]").setEditable(true).setSort(1).setAccountSetId(1L).setLevel(2).setRowId(1);
        balanceSheetConfigMapper.insert(config);
        FmsBalanceSheetReportDO report = new FmsBalanceSheetReportDO().setName("货币资金").setRowNo(1)
                .setFormula("[]").setEditable(true).setSort(1).setAccountSetId(1L).setLevel(2).setRowId(1)
                .setFromPeriod(202608).setToPeriod(202608).setType(1)
                .setOpeningAmount(BigDecimal.ZERO).setClosingAmount(BigDecimal.ZERO).setSettled(false);
        balanceSheetReportMapper.insert(report);

        // 准备参数
        FmsReportFormulaUpdateReqVO.Formula formulaReqVO = new FmsReportFormulaUpdateReqVO.Formula();
        formulaReqVO.setSubjectId(cashSubject.getId());
        formulaReqVO.setOperator("-");
        formulaReqVO.setRules(1);
        FmsReportFormulaUpdateReqVO reqVO = new FmsReportFormulaUpdateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(report.getId());
        reqVO.setFormulas(Collections.singletonList(formulaReqVO));

        // 调用
        balanceSheetService.updateBalanceSheetFormula(reqVO, 10L);

        // 断言
        FmsBalanceSheetConfigDO updateConfig = balanceSheetConfigMapper.selectById(config.getId());
        List<FmsReportFormulaRespVO> formulas = JsonUtils.parseArray(updateConfig.getFormula(), FmsReportFormulaRespVO.class);
        assertEquals(1, formulas.size());
        assertEquals(cashSubject.getId(), CollUtil.getFirst(formulas).getSubjectId());
        assertEquals(cashSubject.getCode(), CollUtil.getFirst(formulas).getSubjectNumber());
        assertEquals(cashSubject.getName(), CollUtil.getFirst(formulas).getSubjectName());
        assertEquals("-", CollUtil.getFirst(formulas).getOperator());
        assertEquals(1, CollUtil.getFirst(formulas).getRules());
        assertEquals(updateConfig.getFormula(), balanceSheetReportMapper.selectById(report.getId()).getFormula());
    }

    @Test
    public void testCheckBalanceSheet() {
        // mock 数据
        FmsSubjectDO liabilitySubject = new FmsSubjectDO().setId(104L).setCode("2001").setName("短期借款")
                .setBalanceDirection(2).setLevel(1).setAccountSetId(1L)
                .setType(FmsSubjectTypeEnum.LIABILITY.getType());
        cashSubject.setType(FmsSubjectTypeEnum.ASSET.getType());
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(cashSubject, liabilitySubject));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("货币资金").setRowNo(1)
                .setFormula("[{\"operator\":\"+\",\"rules\":0,\"subjectNumber\":\"1001\"}]")
                .setEditable(true).setSort(1).setRowId(1)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("资产总计").setRowNo(30)
                .setFormula("[\"L1\"]").setEditable(false).setSort(2).setRowId(2)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("负债和所有者权益总计").setRowNo(53)
                .setFormula("[]").setEditable(false).setSort(33).setRowId(3)
                .setType(FmsReportTypeEnum.BALANCE_SHEET.getType()).setLevel(1));
        FmsLedgerSubjectBalanceRespVO cashBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(103L)
                .setSubjectCode("1001").setOpeningDebitAmount(new BigDecimal("100.00"))
                .setOpeningCreditAmount(BigDecimal.ZERO).setEndingDebitAmount(new BigDecimal("110.00"))
                .setEndingCreditAmount(BigDecimal.ZERO).setChildren(Collections.emptyList());
        FmsLedgerSubjectBalanceRespVO liabilityBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(104L)
                .setSubjectCode("2001").setOpeningDebitAmount(BigDecimal.ZERO)
                .setOpeningCreditAmount(new BigDecimal("90.00")).setEndingDebitAmount(BigDecimal.ZERO)
                .setEndingCreditAmount(new BigDecimal("95.00")).setChildren(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(cashBalance, liabilityBalance));
        FmsTrialBalanceRespVO trialBalance = new FmsTrialBalanceRespVO();
        trialBalance.setBalanced(false);
        when(initialBalanceService.getTrialBalance(1L, 10L)).thenReturn(trialBalance);
        when(closingPeriodService.getClosingOverview(any(), eq(10L)))
                .thenReturn(new FmsClosingOverviewRespVO().setProfitLossBalance(new BigDecimal("20.00")));

        // 调用
        FmsBalanceSheetCheckRespVO result = balanceSheetService.checkBalanceSheet(buildQueryReqVO(), 10L);

        // 断言
        assertFalse(result.getBalanced());
        assertFalse(result.getInitialBalanceBalanced());
        assertFalse(result.getProfitLossTransferred());
        assertEquals(new BigDecimal("100.00"), result.getOpeningDifferenceAmount());
        assertEquals(new BigDecimal("110.00"), result.getClosingDifferenceAmount());
        assertEquals(1, result.getUnmappedSubjects().size());
        assertEquals(liabilitySubject.getId(), CollUtil.getFirst(result.getUnmappedSubjects()).getId());
    }

    @Test
    public void testCheckBalanceSheet_capitalizedExpenseWithExpandedCodeRule() {
        // mock 数据
        FmsSubjectDO capitalizedExpenseSubject = new FmsSubjectDO().setId(104L).setCode("40404")
                .setName("资本化支出").setBalanceDirection(1).setLevel(1).setAccountSetId(1L)
                .setType(FmsSubjectTypeEnum.ASSET.getType());
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Collections.singletonList(capitalizedExpenseSubject));
        when(financeParameterService.getFinanceParameter(1L)).thenReturn(new FmsFinanceParameterDO()
                .setSubjectCodeRule("5-2-2-2"));
        when(financeParameterService.convertStandardSubjectCode("4404", "5-2-2-2"))
                .thenReturn("40404");
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(Collections.singletonList(
                new FmsLedgerSubjectBalanceRespVO().setSubjectId(104L).setSubjectCode("40404")
                        .setOpeningDebitAmount(new BigDecimal("100.00")).setOpeningCreditAmount(BigDecimal.ZERO)
                        .setEndingDebitAmount(new BigDecimal("100.00")).setEndingCreditAmount(BigDecimal.ZERO)
                        .setChildren(Collections.emptyList())));
        when(initialBalanceService.getTrialBalance(1L, 10L))
                .thenReturn(new FmsTrialBalanceRespVO().setBalanced(true));
        when(closingPeriodService.getClosingOverview(any(), eq(10L)))
                .thenReturn(new FmsClosingOverviewRespVO().setProfitLossBalance(BigDecimal.ZERO));

        // 调用
        FmsBalanceSheetCheckRespVO result = balanceSheetService.checkBalanceSheet(buildQueryReqVO(), 10L);

        // 断言
        assertEquals(0, result.getUnmappedSubjects().size());
    }

    // ========== 随机对象 ==========

    private FmsReportListReqVO buildQueryReqVO() {
        FmsReportListReqVO reqVO = new FmsReportListReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setStartMonth("2026-08");
        reqVO.setEndMonth("2026-08");
        return reqVO;
    }

}

package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.income.FmsIncomeStatementConfigDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.income.FmsIncomeStatementReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.income.FmsIncomeStatementConfigMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.income.FmsIncomeStatementReportMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
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

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_CONFIG_NOT_EDITABLE;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.REPORT_FORMULA_RULE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({FmsIncomeStatementServiceImpl.class, FmsReportCommonServiceImpl.class})
public class FmsIncomeStatementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsIncomeStatementServiceImpl incomeStatementService;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;
    @Resource
    private FmsIncomeStatementConfigMapper incomeStatementConfigMapper;
    @Resource
    private FmsIncomeStatementReportMapper incomeStatementReportMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsBalanceSheetService balanceSheetService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsVoucherService voucherService;

    private FmsSubjectDO revenueSubject;
    private FmsSubjectDO expenseSubject;

    @BeforeEach
    public void before() {
        revenueSubject = new FmsSubjectDO().setId(101L).setCode("5001").setName("主营业务收入")
                .setBalanceDirection(2).setLevel(1).setAccountSetId(1L);
        expenseSubject = new FmsSubjectDO().setId(102L).setCode("5601").setName("销售费用")
                .setBalanceDirection(1).setLevel(1).setAccountSetId(1L);
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(accountSet);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(accountSet);
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(revenueSubject, expenseSubject));
    }

    @Test
    public void testGetIncomeStatement() {
        // mock 数据
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("一、营业收入").setRowNo(1)
                .setFormula("[{\"operator\":\"+\",\"rules\":6,\"subjectNumber\":\"5001\"}]")
                .setEditable(true).setSort(0).setType(FmsReportTypeEnum.INCOME_STATEMENT.getType())
                .setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("减：销售费用").setRowNo(2)
                .setFormula("[{\"operator\":\"+\",\"rules\":5,\"subjectNumber\":\"5601\"}]")
                .setEditable(true).setSort(1).setType(FmsReportTypeEnum.INCOME_STATEMENT.getType())
                .setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("二、营业利润").setRowNo(3)
                .setFormula("[\"L1-L2\"]").setEditable(false).setSort(2)
                .setType(FmsReportTypeEnum.INCOME_STATEMENT.getType()).setLevel(1));
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setSubjectCode("5001").setPeriodDebitAmount(BigDecimal.ZERO)
                .setPeriodCreditAmount(new BigDecimal("1000.00")).setYearDebitAmount(BigDecimal.ZERO)
                .setYearCreditAmount(new BigDecimal("3000.00"));
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setSubjectCode("5601").setPeriodDebitAmount(new BigDecimal("200.00"))
                .setPeriodCreditAmount(BigDecimal.ZERO).setYearDebitAmount(new BigDecimal("500.00"))
                .setYearCreditAmount(BigDecimal.ZERO);
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(revenueBalance, expenseBalance));

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = incomeStatementService.getIncomeStatement(reqVO, 10L);

        // 断言
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("1000.00"), result.get(0).getCurrentAmount());
        assertEquals(new BigDecimal("500.00"), result.get(1).getYearAmount());
        assertEquals(new BigDecimal("800.00"), result.get(2).getCurrentAmount());
        assertEquals(new BigDecimal("2500.00"), result.get(2).getYearAmount());
    }

    @Test
    public void testGetIncomeStatement_subjectCodeChanged() {
        // mock 数据
        incomeStatementConfigMapper.insert(new FmsIncomeStatementConfigDO().setName("一、营业收入")
                .setRowNo(1).setFormula("[{\"subjectId\":101,\"subjectName\":\"主营业务收入\","
                        + "\"subjectNumber\":\"5001\",\"operator\":\"+\",\"rules\":6}]")
                .setEditable(true).setSort(0).setAccountSetId(1L).setLevel(1));
        revenueSubject.setCode("50010");
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setSubjectCode("50010").setPeriodDebitAmount(BigDecimal.ZERO)
                .setPeriodCreditAmount(new BigDecimal("1000.00")).setYearDebitAmount(BigDecimal.ZERO)
                .setYearCreditAmount(new BigDecimal("3000.00"));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(revenueBalance));

        // 调用
        List<FmsReportItemRespVO> result = incomeStatementService.getIncomeStatement(buildQueryReqVO(), 10L);

        // 断言
        assertEquals(new BigDecimal("1000.00"), CollUtil.getFirst(result).getCurrentAmount());
        List<FmsReportFormulaRespVO> formulas = JsonUtils.parseArray(
                CollUtil.getFirst(result).getFormula(), FmsReportFormulaRespVO.class);
        assertEquals("50010", CollUtil.getFirst(formulas).getSubjectNumber());
    }

    @Test
    public void testGetIncomeStatement_formulaSnapshot() {
        // mock 数据
        String revenueFormula = "[{\"subjectId\":101,\"subjectName\":\"主营业务收入\","
                + "\"subjectNumber\":\"5001\",\"operator\":\"+\",\"rules\":6}]";
        String expenseFormula = "[{\"subjectId\":102,\"subjectName\":\"销售费用\","
                + "\"subjectNumber\":\"5601\",\"operator\":\"+\",\"rules\":5}]";
        FmsIncomeStatementConfigDO config = new FmsIncomeStatementConfigDO().setName("一、营业收入")
                .setRowNo(1).setFormula(revenueFormula).setEditable(true).setSort(1)
                .setAccountSetId(1L).setLevel(1);
        incomeStatementConfigMapper.insert(config);
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setPeriodDebitAmount(BigDecimal.ZERO).setPeriodCreditAmount(new BigDecimal("1000.00"))
                .setYearDebitAmount(BigDecimal.ZERO).setYearCreditAmount(new BigDecimal("3000.00"));
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setPeriodDebitAmount(new BigDecimal("200.00")).setPeriodCreditAmount(BigDecimal.ZERO)
                .setYearDebitAmount(new BigDecimal("500.00")).setYearCreditAmount(BigDecimal.ZERO);
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Arrays.asList(revenueBalance, expenseBalance));

        // 调用
        List<FmsReportItemRespVO> augustResult = incomeStatementService.getIncomeStatement(
                buildQueryReqVO("2026-08", "2026-08"), 10L);
        incomeStatementConfigMapper.updateById(new FmsIncomeStatementConfigDO().setId(config.getId())
                .setFormula(expenseFormula));
        List<FmsReportItemRespVO> augustResultAfterUpdate = incomeStatementService.getIncomeStatement(
                buildQueryReqVO("2026-08", "2026-08"), 10L);
        List<FmsReportItemRespVO> septemberResult = incomeStatementService.getIncomeStatement(
                buildQueryReqVO("2026-09", "2026-09"), 10L);

        // 断言
        assertEquals(new BigDecimal("1000.00"), CollUtil.getFirst(augustResult).getCurrentAmount());
        assertEquals(new BigDecimal("1000.00"), CollUtil.getFirst(augustResultAfterUpdate).getCurrentAmount());
        assertEquals(new BigDecimal("200.00"), CollUtil.getFirst(septemberResult).getCurrentAmount());
        assertEquals(revenueFormula, CollUtil.getFirst(incomeStatementReportMapper.selectListByPeriod(
                1L, 202608, 202608, 1)).getFormula());
        assertEquals(expenseFormula, CollUtil.getFirst(incomeStatementReportMapper.selectListByPeriod(
                1L, 202609, 202609, 1)).getFormula());
    }

    @Test
    public void testUpdateIncomeStatementFormula() {
        // mock 数据
        String oldFormula = "[]";
        FmsIncomeStatementConfigDO config = new FmsIncomeStatementConfigDO().setName("一、营业收入")
                .setRowNo(1).setFormula(oldFormula).setEditable(true).setSort(1)
                .setAccountSetId(1L).setLevel(1);
        incomeStatementConfigMapper.insert(config);
        FmsIncomeStatementReportDO currentReport = new FmsIncomeStatementReportDO().setName("一、营业收入")
                .setRowNo(1).setFormula(oldFormula).setEditable(true).setSort(1).setAccountSetId(1L).setLevel(1)
                .setFromPeriod(202608).setToPeriod(202608).setType(1)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO).setSettled(false);
        incomeStatementReportMapper.insert(currentReport);
        FmsIncomeStatementReportDO historyReport = new FmsIncomeStatementReportDO().setName("一、营业收入")
                .setRowNo(1).setFormula(oldFormula).setEditable(true).setSort(1).setAccountSetId(1L).setLevel(1)
                .setFromPeriod(202607).setToPeriod(202607).setType(1)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO).setSettled(false);
        incomeStatementReportMapper.insert(historyReport);

        // 准备参数
        FmsReportFormulaUpdateReqVO.Formula formulaReqVO = new FmsReportFormulaUpdateReqVO.Formula();
        formulaReqVO.setSubjectId(revenueSubject.getId());
        formulaReqVO.setOperator("+");
        formulaReqVO.setRules(6);
        FmsReportFormulaUpdateReqVO reqVO = new FmsReportFormulaUpdateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(currentReport.getId());
        reqVO.setFormulas(Collections.singletonList(formulaReqVO));

        // 调用
        incomeStatementService.updateIncomeStatementFormula(reqVO, 10L);

        // 断言
        String formula = incomeStatementReportMapper.selectById(currentReport.getId()).getFormula();
        assertEquals(oldFormula, incomeStatementConfigMapper.selectById(config.getId()).getFormula());
        assertTrue(formula.contains("\"subjectId\":101"));
        assertEquals(oldFormula, incomeStatementReportMapper.selectById(historyReport.getId()).getFormula());
    }

    @Test
    public void testUpdateIncomeStatementFormula_ruleInvalid() {
        // mock 数据
        FmsIncomeStatementConfigDO config = new FmsIncomeStatementConfigDO().setName("一、营业收入")
                .setRowNo(1).setFormula("[]").setEditable(true).setSort(1).setAccountSetId(1L).setLevel(1);
        incomeStatementConfigMapper.insert(config);
        FmsIncomeStatementReportDO report = new FmsIncomeStatementReportDO().setName("一、营业收入")
                .setRowNo(1).setFormula("[]").setEditable(true).setSort(1).setAccountSetId(1L).setLevel(1)
                .setFromPeriod(202608).setToPeriod(202608).setType(1)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO).setSettled(false);
        incomeStatementReportMapper.insert(report);

        // 准备参数
        FmsReportFormulaUpdateReqVO.Formula formulaReqVO = new FmsReportFormulaUpdateReqVO.Formula();
        formulaReqVO.setSubjectId(revenueSubject.getId());
        formulaReqVO.setOperator("+");
        formulaReqVO.setRules(0);
        FmsReportFormulaUpdateReqVO reqVO = new FmsReportFormulaUpdateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(report.getId());
        reqVO.setFormulas(Collections.singletonList(formulaReqVO));

        // 调用，并断言
        assertServiceException(() -> incomeStatementService.updateIncomeStatementFormula(reqVO, 10L),
                REPORT_FORMULA_RULE_INVALID);
    }

    @Test
    public void testUpdateIncomeStatementFormula_notEditable() {
        // mock 数据
        FmsIncomeStatementReportDO report = new FmsIncomeStatementReportDO().setName("二、营业利润")
                .setRowNo(21).setFormula("[\"L1-L2\"]").setEditable(false).setSort(20)
                .setAccountSetId(1L).setLevel(1).setFromPeriod(202608).setToPeriod(202608).setType(1)
                .setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO).setSettled(false);
        incomeStatementReportMapper.insert(report);

        // 准备参数
        FmsReportFormulaUpdateReqVO reqVO = new FmsReportFormulaUpdateReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setId(report.getId());
        reqVO.setFormulas(Collections.emptyList());

        // 调用，并断言
        assertServiceException(() -> incomeStatementService.updateIncomeStatementFormula(reqVO, 10L),
                REPORT_CONFIG_NOT_EDITABLE);
    }

    @Test
    public void testCheckIncomeStatement() {
        // mock 数据
        FmsSubjectDO skippedSubject = new FmsSubjectDO().setId(105L).setCode("6001")
                .setName("以前年度损益调整").setBalanceDirection(1).setLevel(1).setAccountSetId(1L)
                .setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        revenueSubject.setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        expenseSubject.setType(FmsSubjectTypeEnum.PROFIT_LOSS.getType());
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Arrays.asList(revenueSubject, expenseSubject, skippedSubject));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("四、净利润").setRowNo(32)
                .setFormula("[{\"operator\":\"+\",\"rules\":6,\"subjectNumber\":\"5001\"}]")
                .setEditable(false).setSort(0).setType(FmsReportTypeEnum.INCOME_STATEMENT.getType())
                .setLevel(1));
        FmsLedgerSubjectBalanceRespVO revenueBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(101L)
                .setSubjectCode("5001").setPeriodDebitAmount(BigDecimal.ZERO)
                .setPeriodCreditAmount(new BigDecimal("1000.00")).setYearDebitAmount(BigDecimal.ZERO)
                .setYearCreditAmount(new BigDecimal("3000.00"));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(revenueBalance));
        when(balanceSheetService.getBalanceSheet(any(), eq(10L))).thenReturn(Collections.singletonList(
                new FmsBalanceSheetRowRespVO().setLiabilityRowNo(51)
                        .setLiabilityOpeningAmount(BigDecimal.ZERO)
                        .setLiabilityClosingAmount(new BigDecimal("3000.00"))));

        // 调用
        FmsIncomeStatementCheckRespVO result = incomeStatementService.checkIncomeStatement(
                buildQueryReqVO(), 10L);

        // 断言
        assertTrue(result.getBalanced());
        assertEquals(new BigDecimal("0.00"), result.getDifferenceAmount());
        assertEquals(1, result.getUnmappedSubjects().size());
        assertEquals(expenseSubject.getId(), CollUtil.getFirst(result.getUnmappedSubjects()).getId());
    }

    // ========== 随机对象 ==========

    private FmsReportListReqVO buildQueryReqVO() {
        return buildQueryReqVO("2026-08", "2026-08");
    }

    private FmsReportListReqVO buildQueryReqVO(String startMonth, String endMonth) {
        FmsReportListReqVO reqVO = new FmsReportListReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setStartMonth(startMonth);
        reqVO.setEndMonth(endMonth);
        return reqVO;
    }

}

package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingPeriodService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({FmsBalanceSheetServiceImpl.class, FmsIncomeStatementServiceImpl.class,
        FmsCashFlowStatementServiceImpl.class, FmsReportCommonServiceImpl.class})
public class FmsReportTemplateServiceTest extends BaseDbUnitTest {

    @Resource
    private DataSource dataSource;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;
    @Resource
    private FmsBalanceSheetServiceImpl balanceSheetService;
    @Resource
    private FmsIncomeStatementServiceImpl incomeStatementService;
    @Resource
    private FmsCashFlowStatementServiceImpl cashFlowStatementService;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;
    @MockBean
    private FmsInitialBalanceService initialBalanceService;
    @MockBean
    private FmsClosingPeriodService closingPeriodService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;
    @MockBean
    private FmsClosingVoucherService closingVoucherService;
    @MockBean
    private FmsVoucherService voucherService;

    private final List<FmsSubjectDO> subjects = new ArrayList<>();
    private final List<FmsLedgerSubjectBalanceRespVO> balances = new ArrayList<>();

    @BeforeEach
    public void before() {
        // 初始化报表模板
        initializeTemplates();
        // mock 账套、科目和账簿余额
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L).setInitialized(true)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(accountSet);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);
        when(ledgerService.getSubjectBalanceList(any(), eq(10L))).thenReturn(balances);
    }

    @Test
    public void testInitializeTemplates_repeatExecution() {
        // mock 数据：模拟用户修改已有报表公式
        FmsReportTemplateDO template = CollUtil.findOne(
                reportTemplateMapper.selectListByType(FmsReportTypeEnum.INCOME_STATEMENT.getType()),
                item -> Integer.valueOf(21).equals(item.getRowNo()));
        reportTemplateMapper.updateById(new FmsReportTemplateDO().setId(template.getId()).setFormula("[\"L1\"]"));

        // 调用
        initializeTemplates();

        // 断言
        assertEquals(58, reportTemplateMapper.selectListByType(FmsReportTypeEnum.BALANCE_SHEET.getType()).size());
        assertEquals(31, reportTemplateMapper.selectListByType(FmsReportTypeEnum.INCOME_STATEMENT.getType()).size());
        assertEquals(25, reportTemplateMapper.selectListByType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).size());
        assertEquals(21, reportTemplateMapper.selectListByType(FmsReportTypeEnum.CASH_FLOW_ADJUSTMENT.getType()).size());
        assertEquals(135, reportTemplateMapper.selectCount());
        assertEquals("[\"L1\"]", reportTemplateMapper.selectById(template.getId()).getFormula());
    }

    @Test
    public void testInitializeTemplates_preserveExistingFormulas() {
        // mock 数据：模拟旧版报表公式
        List<FmsReportTemplateDO> templates = new ArrayList<>();
        for (FmsReportTemplateDO template : reportTemplateMapper.selectList()) {
            String oldFormula;
            if (FmsReportTypeEnum.BALANCE_SHEET.getType().equals(template.getType()) && (template.getRowNo() == 9
                    || template.getRowNo() == 24 || template.getRowNo() == 25)) {
                oldFormula = template.getFormula().replace("\"operator\":\"-\"", "\"operator\":\"+\"");
            } else if (FmsReportTypeEnum.INCOME_STATEMENT.getType().equals(template.getType())
                    && template.getRowNo() == 21) {
                oldFormula = "[\"L1-L2-L3-L11-L14-L18\"]";
            } else if (FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType().equals(template.getType())
                    && template.getRowNo() == 19) {
                oldFormula = "[\"L14+L15-L17-L18\"]";
            } else {
                continue;
            }
            templates.add(template);
            reportTemplateMapper.updateById(new FmsReportTemplateDO().setId(template.getId()).setFormula(oldFormula));
        }

        // 调用
        initializeTemplates();

        // 断言
        assertEquals(5, templates.size());
        for (FmsReportTemplateDO template : templates) {
            assertEquals(template.getFormula(), reportTemplateMapper.selectById(template.getId()).getFormula());
        }
        assertEquals(135, reportTemplateMapper.selectCount());
    }

    @Test
    public void testInitializeTemplates_preserveLegacySubjectIds() {
        // mock 数据：模拟使用历史科目编号的公式
        FmsReportTemplateDO template = CollUtil.findOne(
                reportTemplateMapper.selectListByType(FmsReportTypeEnum.BALANCE_SHEET.getType()),
                item -> Integer.valueOf(25).equals(item.getRowNo()));
        String oldFormula = "[{\"operator\":\"+\",\"rules\":0,\"subjectId\":1371,"
                + "\"subjectName\":\"无形资产\",\"subjectNumber\":\"1701\"},"
                + "{\"operator\":\"+\",\"rules\":0,\"subjectId\":1372,"
                + "\"subjectName\":\"累计摊销\",\"subjectNumber\":\"1702\"}]";
        reportTemplateMapper.updateById(new FmsReportTemplateDO().setId(template.getId()).setFormula(oldFormula));

        // 调用
        initializeTemplates();

        // 断言
        assertEquals(template.getFormula(), reportTemplateMapper.selectById(template.getId()).getFormula());
    }

    @Test
    public void testGetBalanceSheet_capitalContribution() {
        // mock 数据：投入资本 100 元
        mockSubjectBalance("1001", 1, 0, 100, 100, 0);
        mockSubjectBalance("3001", 2, 0, -100, 0, 100);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsBalanceSheetRowRespVO> balanceSheet = balanceSheetService.getBalanceSheet(reqVO, 10L);
        List<FmsReportItemRespVO> cashFlow = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertAssetAmount(balanceSheet, 30, 100);
        FmsBalanceSheetRowRespVO total = CollUtil.findOne(balanceSheet,
                row -> Integer.valueOf(53).equals(row.getLiabilityRowNo()));
        assertNotNull(total);
        assertAmount(100, total.getLiabilityClosingAmount());
        assertItemAmount(cashFlow, 15, 100);
        assertItemAmount(cashFlow, 22, 100);
    }

    @Test
    public void testGetBalanceSheet_inventoryAndContraAccounts() {
        // mock 数据：存货、固定资产、无形资产及其备抵科目
        mockSubjectBalance("1403", 1, 0, 30, 30, 0);
        mockSubjectBalance("1407", 2, 0, -5, 0, 5);
        mockSubjectBalance("1621", 1, 0, 100, 100, 0);
        mockSubjectBalance("1622", 2, 0, -20, 0, 20);
        mockSubjectBalance("1701", 1, 0, 100, 100, 0);
        mockSubjectBalance("1702", 2, 0, -10, 0, 10);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsBalanceSheetRowRespVO> result = balanceSheetService.getBalanceSheet(reqVO, 10L);

        // 断言
        assertAssetAmount(result, 9, 25);
        assertAssetAmount(result, 24, 80);
        assertAssetAmount(result, 25, 90);
        assertAssetAmount(result, 30, 195);
    }

    @Test
    public void testGetCashFlowStatement_creditSale() {
        // mock 数据：赊销 100 元并结转利润，尚未收款
        mockSubjectBalance("1001", 1, 0, 0, 0, 0);
        mockSubjectBalance("1122", 1, 0, 100, 100, 0);
        mockSubjectBalance("5001", 2, 0, 0, 0, 100);
        mockSubjectBalance("3103", 2, 0, -100, 0, 100);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> incomeItems = incomeStatementService.getIncomeStatement(reqVO, 10L);
        List<FmsReportItemRespVO> result = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertItemAmount(incomeItems, 1, 100);
        assertItemAmount(result, 1, 0);
        assertItemAmount(result, 6, 0);
        assertItemAmount(result, 20, 0);
        assertItemAmount(result, 22, 0);
    }

    @Test
    public void testGetCashFlowStatement_purchaseInventory() {
        // mock 数据：期初现金 100 元，支付 40 元购入原材料
        mockSubjectBalance("1001", 1, 100, 60, 0, 40);
        mockSubjectBalance("1403", 1, 0, 40, 40, 0);
        mockSubjectBalance("3001", 2, -100, -100, 0, 0);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertItemAmount(result, 3, 40);
        assertItemAmount(result, 6, 0);
        assertItemAmount(result, 20, -40);
        assertItemAmount(result, 22, 60);
    }

    @Test
    public void testGetCashFlowStatement_payWagesAndIncomeTax() {
        // mock 数据：计提并支付工资 30 元、所得税 10 元，期末应付款为零
        mockSubjectBalance("1001", 1, 100, 60, 0, 40);
        mockSubjectBalance("3001", 2, -100, -100, 0, 0);
        mockSubjectBalance("3103", 2, 0, 40, 40, 0);
        mockSubjectBalance("5602", 1, 0, 0, 30, 0);
        mockSubjectBalance("5801", 1, 0, 0, 10, 0);
        mockSubjectBalance("2211", 2, 0, 0, 30, 30);
        mockSubjectBalance("2221", 2, 0, 0, 10, 10);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertItemAmount(result, 4, 30);
        assertItemAmount(result, 5, 10);
        assertItemAmount(result, 6, 0);
        assertItemAmount(result, 20, -40);
        assertItemAmount(result, 22, 60);
    }

    @Test
    public void testGetCashFlowStatement_repayLoan() {
        // mock 数据：期初现金 100 元，偿还借款 30 元
        mockSubjectBalance("1001", 1, 100, 70, 0, 30);
        mockSubjectBalance("2001", 2, -100, -70, 30, 0);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertItemAmount(result, 16, 30);
        assertItemAmount(result, 19, -30);
        assertItemAmount(result, 6, 0);
        assertItemAmount(result, 22, 70);
    }

    @Test
    public void testGetIncomeStatement_investmentIncomeAndNonOperatingExpense() {
        // mock 数据：投资收益 20 元，营业外支出 5 元
        mockSubjectBalance("5111", 2, 0, -20, 0, 20);
        mockSubjectBalance("5711", 1, 0, 5, 5, 0);

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = incomeStatementService.getIncomeStatement(reqVO, 10L);

        // 断言
        assertItemAmount(result, 20, 20);
        assertItemAmount(result, 21, 20);
        assertItemAmount(result, 24, 5);
        assertItemAmount(result, 32, 15);
    }

    /**
     * 执行报表模板升级脚本
     */
    private void initializeTemplates() {
        new ResourceDatabasePopulator(new FileSystemResource("../sql/mysql/upgrade/20260926-fms-report-template.sql"))
                .execute(dataSource);
    }

    /**
     * 模拟科目余额和发生额：余额借方为正、贷方为负，发生额已扣除损益结转数据
     */
    private void mockSubjectBalance(String code, int direction, int opening, int closing, int debit, int credit) {
        Long id = Long.valueOf(code);
        subjects.add(new FmsSubjectDO().setId(id).setAccountSetId(1L).setCode(code).setName(code)
                .setLevel(1).setBalanceDirection(direction));
        balances.add(new FmsLedgerSubjectBalanceRespVO().setSubjectId(id).setSubjectCode(code)
                .setOpeningDebitAmount(BigDecimal.valueOf(Math.max(opening, 0)))
                .setOpeningCreditAmount(BigDecimal.valueOf(Math.max(-opening, 0)))
                .setEndingDebitAmount(BigDecimal.valueOf(Math.max(closing, 0)))
                .setEndingCreditAmount(BigDecimal.valueOf(Math.max(-closing, 0)))
                .setPeriodDebitAmount(BigDecimal.valueOf(debit)).setPeriodCreditAmount(BigDecimal.valueOf(credit))
                .setYearDebitAmount(BigDecimal.valueOf(debit)).setYearCreditAmount(BigDecimal.valueOf(credit))
                .setChildren(Collections.emptyList()));
    }

    /**
     * 构造 2026 年 8 月的报表查询参数
     */
    private FmsReportListReqVO buildQueryReqVO() {
        FmsReportListReqVO reqVO = new FmsReportListReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setStartMonth("2026-08");
        reqVO.setEndMonth("2026-08");
        return reqVO;
    }

    /**
     * 断言报表指定行的本期和本年金额
     */
    private void assertItemAmount(List<FmsReportItemRespVO> rows, int rowNo, int expected) {
        FmsReportItemRespVO item = CollUtil.findOne(rows, row -> Integer.valueOf(rowNo).equals(row.getRowNo()));
        assertNotNull(item);
        assertAmount(expected, item.getCurrentAmount());
        assertAmount(expected, item.getYearAmount());
    }

    /**
     * 断言资产负债表指定行的资产期末余额
     */
    private void assertAssetAmount(List<FmsBalanceSheetRowRespVO> rows, int rowNo, int expected) {
        FmsBalanceSheetRowRespVO item = CollUtil.findOne(rows,
                row -> Integer.valueOf(rowNo).equals(row.getAssetRowNo()));
        assertNotNull(item);
        assertAmount(expected, item.getAssetClosingAmount());
    }

    /**
     * 按数值比较金额，忽略 BigDecimal 的小数位数差异
     */
    private void assertAmount(int expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual), "实际金额：" + actual);
    }

}

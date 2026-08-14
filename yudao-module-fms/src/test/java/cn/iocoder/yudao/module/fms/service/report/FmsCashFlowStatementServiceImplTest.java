package cn.iocoder.yudao.module.fms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowStatementUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.FmsReportTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowExtendDataDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow.FmsCashFlowStatementReportDO;
import cn.iocoder.yudao.module.fms.dal.mysql.report.FmsReportTemplateMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowExtendDataMapper;
import cn.iocoder.yudao.module.fms.dal.mysql.report.cashflow.FmsCashFlowStatementReportMapper;
import cn.iocoder.yudao.module.fms.enums.report.FmsReportTypeEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingSchemeService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({FmsCashFlowStatementServiceImpl.class, FmsReportCommonServiceImpl.class})
public class FmsCashFlowStatementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsCashFlowStatementServiceImpl cashFlowStatementService;
    @Resource
    private FmsReportTemplateMapper reportTemplateMapper;
    @Resource
    private FmsCashFlowExtendDataMapper cashFlowExtendDataMapper;
    @Resource
    private FmsCashFlowStatementReportMapper cashFlowStatementReportMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsLedgerService ledgerService;
    @MockBean
    private FmsBalanceSheetService balanceSheetService;
    @MockBean
    private FmsIncomeStatementService incomeStatementService;
    @MockBean
    private FmsClosingSchemeService closingSchemeService;

    private FmsSubjectDO expenseSubject;

    @BeforeEach
    public void before() {
        expenseSubject = new FmsSubjectDO().setId(102L).setCode("5601").setName("销售费用")
                .setBalanceDirection(1).setLevel(1).setAccountSetId(1L);
        FmsAccountSetDO accountSet = new FmsAccountSetDO().setId(1L)
                .setStartTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L)).thenReturn(accountSet);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L)).thenReturn(accountSet);
        when(subjectService.getSubjectList(1L, null, 10L))
                .thenReturn(Collections.singletonList(expenseSubject));
    }

    @Test
    public void testGetCashFlowStatement_crossReportFormula() {
        // mock 数据
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("销售商品收到的现金").setRowNo(1)
                .setFormula("[\"IN1\"]").setEditable(false).setSort(0)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("支付其他经营活动现金").setRowNo(6)
                .setFormula("[]").setEditable(false).setSort(1)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("经营活动现金流量净额").setRowNo(7)
                .setFormula("[\"L1-L6\"]").setEditable(false).setSort(2)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("现金净增加额").setRowNo(20)
                .setFormula("[\"L7\"]").setEditable(false).setSort(3)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("期初现金余额").setRowNo(21)
                .setFormula("[\"BA[1,1]\"]").setEditable(false).setSort(4)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("期末现金余额").setRowNo(22)
                .setFormula("[\"L20+L21\"]").setEditable(false).setSort(5)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(1));
        Map<Integer, FmsReportItemRespVO> balanceLineMap = new HashMap<>();
        balanceLineMap.put(FmsBalanceSheetServiceImpl.MONETARY_CASH_ROW_NO, new FmsReportItemRespVO()
                .setRowNo(FmsBalanceSheetServiceImpl.MONETARY_CASH_ROW_NO)
                .setOpeningAmount(new BigDecimal("100.00")).setClosingAmount(new BigDecimal("1100.00")));
        when(balanceSheetService.getBalanceSheetLineMap(any(), any(), any(), eq(10L)))
                .thenReturn(balanceLineMap);
        when(incomeStatementService.getIncomeStatement(any(), eq(10L)))
                .thenReturn(Collections.singletonList(new FmsReportItemRespVO().setRowNo(1)
                        .setCurrentAmount(new BigDecimal("1000.00"))
                        .setYearAmount(new BigDecimal("1000.00"))));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.emptyList());

        // 准备参数
        FmsReportListReqVO reqVO = buildQueryReqVO();

        // 调用
        List<FmsReportItemRespVO> result = cashFlowStatementService.getCashFlowStatement(reqVO, 10L);

        // 断言
        assertEquals(6, result.size());
        assertEquals(new BigDecimal("1000.00"), result.get(0).getCurrentAmount());
        assertEquals(new BigDecimal("1100.00"), result.get(5).getCurrentAmount());
        assertEquals(new BigDecimal("1100.00"), result.get(5).getYearAmount());
        assertEquals(6, cashFlowStatementReportMapper.selectListByPeriod(
                1L, 202608, 202608, 1).size());
    }

    @Test
    public void testUpdateCashFlowStatement() {
        // mock 数据
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("经营活动现金流入").setRowNo(1)
                .setFormula("[\"IN1\"]").setEditable(false).setSort(0)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("经营活动现金流量净额").setRowNo(2)
                .setFormula("[\"L1\"]").setEditable(false).setSort(1)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setLevel(1));
        when(balanceSheetService.getBalanceSheetLineMap(any(), any(), any(), eq(10L)))
                .thenReturn(Collections.emptyMap());
        when(incomeStatementService.getIncomeStatement(any(), eq(10L)))
                .thenReturn(Collections.singletonList(new FmsReportItemRespVO().setRowNo(1)
                        .setCurrentAmount(new BigDecimal("100.00"))
                        .setYearAmount(new BigDecimal("150.00"))));
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.emptyList());

        // 准备参数
        FmsReportListReqVO listReqVO = buildQueryReqVO();
        List<FmsReportItemRespVO> reports = cashFlowStatementService.getCashFlowStatement(
                listReqVO, 10L);
        FmsCashFlowStatementUpdateReqVO updateReqVO = new FmsCashFlowStatementUpdateReqVO()
                .setAccountSetId(1L).setStartMonth("2026-08").setEndMonth("2026-08")
                .setItems(Collections.singletonList(new FmsCashFlowStatementUpdateReqVO.Item()
                        .setId(reports.get(0).getId()).setCurrentAmount(new BigDecimal("80.00"))
                        .setYearAmount(new BigDecimal("200.00"))));

        // 调用
        cashFlowStatementService.updateCashFlowStatement(updateReqVO, 10L);
        reports = cashFlowStatementService.getCashFlowStatement(listReqVO, 10L);

        // 断言保存金额和后续行次联动
        assertEquals(new BigDecimal("80.00"), reports.get(0).getCurrentAmount());
        assertEquals(new BigDecimal("200.00"), reports.get(0).getYearAmount());
        assertEquals(new BigDecimal("80.00"), reports.get(1).getCurrentAmount());
        assertEquals(new BigDecimal("200.00"), reports.get(1).getYearAmount());

        // 调用：金额清零后重新按公式计算
        updateReqVO.getItems().get(0).setCurrentAmount(BigDecimal.ZERO).setYearAmount(BigDecimal.ZERO);
        cashFlowStatementService.updateCashFlowStatement(updateReqVO, 10L);
        reports = cashFlowStatementService.getCashFlowStatement(listReqVO, 10L);

        // 断言
        assertEquals(new BigDecimal("100.00"), reports.get(0).getCurrentAmount());
        assertEquals(new BigDecimal("150.00"), reports.get(0).getYearAmount());
    }

    @Test
    public void testGetCashFlowAdjustmentListAndStatement() {
        // mock 数据
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("支付给职工的工资").setRowNo(1)
                .setFormula("[{\"operator\":\"+\",\"rules\":5,\"subjectNumber\":\"5601\"}]")
                .setEditable(true).setSort(0)
                .setType(FmsReportTypeEnum.CASH_FLOW_ADJUSTMENT.getType()).setCategory(1).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("支付给职工的其他福利费").setRowNo(2)
                .setFormula("[]").setEditable(true).setSort(1)
                .setType(FmsReportTypeEnum.CASH_FLOW_ADJUSTMENT.getType()).setCategory(1).setLevel(1));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("支付给职工以及为职工支付的现金")
                .setRowNo(4).setFormula("[\"L1+L2\"]").setEditable(false).setSort(2)
                .setType(FmsReportTypeEnum.CASH_FLOW_ADJUSTMENT.getType()).setCategory(1).setLevel(2));
        reportTemplateMapper.insert(new FmsReportTemplateDO().setName("支付的职工薪酬").setRowNo(4)
                .setFormula("[\"EX4\"]").setEditable(false).setSort(0)
                .setType(FmsReportTypeEnum.CASH_FLOW_STATEMENT.getType()).setCategory(1).setLevel(2));
        FmsLedgerSubjectBalanceRespVO expenseBalance = new FmsLedgerSubjectBalanceRespVO().setSubjectId(102L)
                .setSubjectCode("5601").setPeriodDebitAmount(new BigDecimal("200.00"))
                .setPeriodCreditAmount(new BigDecimal("20.00")).setYearDebitAmount(new BigDecimal("500.00"))
                .setYearCreditAmount(new BigDecimal("50.00")).setChildren(Collections.emptyList());
        when(ledgerService.getSubjectBalanceList(any(), eq(10L)))
                .thenReturn(Collections.singletonList(expenseBalance));
        when(balanceSheetService.getBalanceSheetLineMap(any(), any(), any(), eq(10L)))
                .thenReturn(Collections.emptyMap());
        when(incomeStatementService.getIncomeStatement(any(), eq(10L)))
                .thenReturn(Collections.emptyList());

        // 调用
        List<FmsCashFlowAdjustmentRespVO> adjustments = cashFlowStatementService.getCashFlowAdjustmentList(
                buildQueryReqVO(), 10L);

        // 断言
        assertEquals(3, adjustments.size());
        assertEquals(new BigDecimal("200.00"), adjustments.get(0).getCurrentAmount());
        assertEquals(new BigDecimal("500.00"), adjustments.get(0).getYearAmount());
        assertEquals(new BigDecimal("200.00"), adjustments.get(2).getCurrentAmount());

        // 准备参数
        FmsCashFlowExtendDataDO manualData = cashFlowExtendDataMapper.selectListByPeriod(
                1L, 202608, 202608, 1, 1).get(1);
        cashFlowStatementService.getCashFlowStatement(buildQueryReqVO(), 10L);
        FmsCashFlowStatementReportDO adjustedReport = CollUtil.getFirst(
                cashFlowStatementReportMapper.selectListByPeriod(1L, 202608, 202608, 1));
        cashFlowStatementReportMapper.updateById(new FmsCashFlowStatementReportDO().setId(adjustedReport.getId())
                .setCurrentAmount(new BigDecimal("999.00")).setYearAmount(new BigDecimal("999.00")));
        FmsCashFlowAdjustmentUpdateReqVO.Item itemReqVO = new FmsCashFlowAdjustmentUpdateReqVO.Item();
        itemReqVO.setId(manualData.getId());
        itemReqVO.setCurrentAmount(new BigDecimal("30.00"));
        itemReqVO.setYearAmount(new BigDecimal("60.00"));
        FmsCashFlowAdjustmentUpdateReqVO updateReqVO = new FmsCashFlowAdjustmentUpdateReqVO();
        updateReqVO.setAccountSetId(1L);
        updateReqVO.setItems(Collections.singletonList(itemReqVO));

        // 调用
        cashFlowStatementService.updateCashFlowAdjustment(updateReqVO, 10L);
        adjustedReport = cashFlowStatementReportMapper.selectById(adjustedReport.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(adjustedReport.getCurrentAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(adjustedReport.getYearAmount()));
        adjustments = cashFlowStatementService.getCashFlowAdjustmentList(buildQueryReqVO(), 10L);
        List<FmsReportItemRespVO> statement = cashFlowStatementService.getCashFlowStatement(
                buildQueryReqVO(), 10L);

        // 断言
        assertEquals(new BigDecimal("230.00"), adjustments.get(2).getCurrentAmount());
        assertEquals(new BigDecimal("560.00"), adjustments.get(2).getYearAmount());
        assertEquals(new BigDecimal("230.00"), CollUtil.getFirst(statement).getCurrentAmount());
        assertEquals(new BigDecimal("560.00"), CollUtil.getFirst(statement).getYearAmount());
    }

    @Test
    public void testCheckCashFlowStatement() {
        // mock 数据
        FmsBalanceSheetCheckRespVO balanceCheck = new FmsBalanceSheetCheckRespVO()
                .setBalanced(true).setInitialBalanceBalanced(true).setProfitLossTransferred(true)
                .setOpeningDifferenceAmount(BigDecimal.ZERO).setClosingDifferenceAmount(BigDecimal.ZERO)
                .setUnmappedSubjects(Collections.emptyList());
        when(balanceSheetService.checkBalanceSheet(any(), eq(10L))).thenReturn(balanceCheck);

        // 调用
        FmsCashFlowCheckRespVO result = cashFlowStatementService.checkCashFlowStatement(
                buildQueryReqVO(), 10L);

        // 断言
        assertTrue(result.getBalanced());
        assertTrue(result.getInitialBalanceBalanced());
        assertTrue(result.getProfitLossTransferred());
        assertTrue(result.getBalanceSheetReady());
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

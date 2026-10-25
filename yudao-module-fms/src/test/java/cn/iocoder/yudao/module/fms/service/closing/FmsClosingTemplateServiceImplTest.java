package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingTemplateDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.closing.FmsClosingTemplateMapper;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTemplateCategoryEnum;
import cn.iocoder.yudao.module.fms.enums.closing.FmsClosingTimeTypeEnum;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.report.FmsFormulaRuleEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsFinanceParameterService;
import cn.iocoder.yudao.module.fms.service.config.FmsSubjectService;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_SCHEME_RATIO_INVALID;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsClosingTemplateServiceImpl.class)
public class FmsClosingTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsClosingTemplateServiceImpl closingTemplateService;
    @Resource
    private FmsClosingTemplateMapper closingTemplateMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private FmsSubjectService subjectService;
    @MockBean
    private FmsFinanceParameterService financeParameterService;

    @BeforeEach
    public void before() {
        when(financeParameterService.getFinanceParameter(any())).thenReturn(new FmsFinanceParameterDO()
                .setSubjectCodeRule(FmsFinanceParameterDO.DEFAULT_SUBJECT_CODE_RULE));
        when(financeParameterService.convertStandardSubjectCode(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testInitializeClosingTemplates() {
        // mock 方法
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(buildPresetSubjects());

        // 调用
        closingTemplateService.initializeClosingTemplates(1L, 10L);
        closingTemplateService.initializeClosingTemplates(1L, 10L);

        // 断言
        List<FmsClosingTemplateDO> templates = closingTemplateMapper.selectListByAccountSetId(1L);
        assertEquals(21, templates.size());
        assertEquals(4L, templates.stream().map(FmsClosingTemplateDO::getCategory).distinct().count());
        assertEquals(21L, templates.stream().map(FmsClosingTemplateDO::getPresetCode).distinct().count());
    }

    @Test
    public void testInitializeClosingTemplates_accountSetIsolation() {
        // mock 方法
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(buildPresetSubjects());
        when(subjectService.getSubjectList(2L, null, 10L)).thenReturn(buildPresetSubjects());

        // 调用
        closingTemplateService.initializeClosingTemplates(1L, 10L);
        closingTemplateService.initializeClosingTemplates(2L, 10L);

        // 断言
        assertEquals(21, closingTemplateMapper.selectListByAccountSetId(1L).size());
        assertEquals(21, closingTemplateMapper.selectListByAccountSetId(2L).size());
    }

    @Test
    public void testInitializeClosingTemplates_subjectNotExists() {
        // mock 方法
        List<FmsSubjectDO> subjects = buildPresetSubjects();
        subjects.remove(0);
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);

        // 调用，并断言
        assertServiceException(() -> closingTemplateService.initializeClosingTemplates(1L, 10L),
                CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS, "daily-travel-reimbursement", "1001");
        assertEquals(0, closingTemplateMapper.selectCount());
    }

    /**
     * 电话费记入销售费用-通讯费，见 https://github.com/YunaiV/ruoyi-vue-pro/issues/1326
     */
    @Test
    public void testInitializeClosingTemplates_presetSubjects() {
        // mock 方法
        List<FmsSubjectDO> subjects = buildPresetSubjects();
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);

        // 调用
        closingTemplateService.initializeClosingTemplates(1L, 10L);

        // 断言：报销电话费，借记销售费用-通讯费，而不是办公用品、差旅费
        List<FmsClosingTemplateDO> templates = closingTemplateMapper.selectListByAccountSetId(1L);
        List<FmsClosingTemplateDO.SubjectRule> telephoneRules = findTemplate(templates, "daily-telephone-expense")
                .getSubjectRules();
        assertEquals(2, telephoneRules.size());
        FmsSubjectDO telephoneSubject = findSubject(subjects, telephoneRules.get(0).getSubjectId());
        assertEquals("通讯费", telephoneSubject.getName());
        assertSubjectRule(telephoneRules.get(0), findSubject(subjects, "560109"), FmsDebitCreditDirectionEnum.DEBIT, "100");
        assertSubjectRule(telephoneRules.get(1), findSubject(subjects, "1001"), FmsDebitCreditDirectionEnum.CREDIT, "100");
        // 断言：报销差旅费，仍借记差旅费
        List<FmsClosingTemplateDO.SubjectRule> travelRules = findTemplate(templates, "daily-travel-reimbursement")
                .getSubjectRules();
        assertEquals(2, travelRules.size());
        assertSubjectRule(travelRules.get(0), findSubject(subjects, "560107"), FmsDebitCreditDirectionEnum.DEBIT, "100");
        assertSubjectRule(travelRules.get(1), findSubject(subjects, "1001"), FmsDebitCreditDirectionEnum.CREDIT, "100");
        // 断言：支付房租水电费、报销业务招待费用的科目不变
        List<FmsClosingTemplateDO.SubjectRule> rentRules = findTemplate(templates, "daily-rent-utilities")
                .getSubjectRules();
        assertEquals(2, rentRules.size());
        assertSubjectRule(rentRules.get(0), findSubject(subjects, "560102"), FmsDebitCreditDirectionEnum.DEBIT, "100");
        assertSubjectRule(rentRules.get(1), findSubject(subjects, "1001"), FmsDebitCreditDirectionEnum.CREDIT, "100");
        List<FmsClosingTemplateDO.SubjectRule> entertainmentRules = findTemplate(templates, "daily-business-entertainment")
                .getSubjectRules();
        assertEquals(2, entertainmentRules.size());
        assertSubjectRule(entertainmentRules.get(0), findSubject(subjects, "1001"), FmsDebitCreditDirectionEnum.DEBIT, "100");
        assertSubjectRule(entertainmentRules.get(1), findSubject(subjects, "1002"), FmsDebitCreditDirectionEnum.CREDIT, "100");
        // 断言：计提工资，仍按销售费用-工资 15%、管理费用-工资 85% 分摊
        List<FmsClosingTemplateDO.SubjectRule> payrollRules = findTemplate(templates, "transfer-accrue-payroll")
                .getSubjectRules();
        assertEquals(3, payrollRules.size());
        assertSubjectRule(payrollRules.get(0), findSubject(subjects, "560110"), FmsDebitCreditDirectionEnum.DEBIT, "15");
        assertSubjectRule(payrollRules.get(1), findSubject(subjects, "560209"), FmsDebitCreditDirectionEnum.DEBIT, "85");
        assertSubjectRule(payrollRules.get(2), findSubject(subjects, "2211"), FmsDebitCreditDirectionEnum.CREDIT, "100");
        // 断言：办公用品不再被任何预置模板引用
        assertEquals(0L, closingTemplateService.getClosingTemplateCountBySubjectIds(1L,
                singletonList(findSubject(subjects, "560101").getId())));
    }

    @Test
    public void testInitializeClosingTemplates_telephoneSubjectNotExists() {
        // mock 方法：账套缺少通讯费科目
        List<FmsSubjectDO> subjects = buildPresetSubjects();
        subjects.removeIf(subject -> "560109".equals(subject.getCode()));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(subjects);

        // 调用，并断言：不会回退到办公用品、差旅费
        assertServiceException(() -> closingTemplateService.initializeClosingTemplates(1L, 10L),
                CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS, "daily-telephone-expense", "560109");
        assertEquals(0, closingTemplateMapper.selectCount());
    }

    @Test
    public void testGetClosingTemplateList() {
        // mock 数据
        closingTemplateMapper.insert(new FmsClosingTemplateDO().setAccountSetId(1L)
                .setName("账套一模板").setCategory(FmsClosingTemplateCategoryEnum.DAILY_EXPENSE.getCategory()));
        closingTemplateMapper.insert(new FmsClosingTemplateDO().setAccountSetId(2L)
                .setName("账套二模板").setCategory(FmsClosingTemplateCategoryEnum.DAILY_EXPENSE.getCategory()));
        when(accountSetService.validateAccountSetReadPermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));

        // 调用
        List<?> templates = closingTemplateService.getClosingTemplateList(1L, 10L);

        // 断言
        assertEquals(1, templates.size());
        verify(accountSetService).validateAccountSetReadPermission(1L, 10L);
    }

    @Test
    public void testCreateClosingTemplate() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(buildClosingTemplateSubjects());

        // 准备参数
        FmsClosingTemplateSaveReqVO reqVO = buildClosingTemplateSaveReqVO();

        // 调用
        Long id = closingTemplateService.createClosingTemplate(reqVO, 10L);

        // 断言
        FmsClosingTemplateDO template = closingTemplateMapper.selectById(id);
        assertEquals("结转本月房租", template.getName());
        assertEquals(2, template.getSubjectRules().size());
        assertEquals("560102", template.getSubjectRules().get(0).getSubjectCode());
        assertEquals("1001", template.getSubjectRules().get(1).getSubjectCode());
        verify(accountSetService).validateAccountSetWritePermission(1L, 10L);
    }

    @Test
    public void testCreateClosingTemplate_ratioInvalid() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(buildClosingTemplateSubjects());

        // 准备参数
        FmsClosingTemplateSaveReqVO reqVO = buildClosingTemplateSaveReqVO();
        reqVO.getSubjects().get(0).setAmountRatio(new BigDecimal("99"));

        // 调用，并断言异常
        assertServiceException(() -> closingTemplateService.createClosingTemplate(reqVO, 10L),
                CLOSING_SCHEME_RATIO_INVALID);
        assertEquals(0, closingTemplateMapper.selectCount());
    }

    @Test
    public void testUpdateClosingTemplate() {
        // mock 数据
        FmsClosingTemplateDO template = new FmsClosingTemplateDO().setAccountSetId(1L)
                .setName("原模板").setCategory(FmsClosingTemplateCategoryEnum.DAILY_EXPENSE.getCategory());
        closingTemplateMapper.insert(template);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));
        when(subjectService.getSubjectList(1L, null, 10L)).thenReturn(buildClosingTemplateSubjects());

        // 准备参数
        FmsClosingTemplateSaveReqVO reqVO = buildClosingTemplateSaveReqVO();
        reqVO.setId(template.getId());
        reqVO.setName("修改后模板");

        // 调用
        closingTemplateService.updateClosingTemplate(reqVO, 10L);

        // 断言
        assertEquals("修改后模板", closingTemplateMapper.selectById(template.getId()).getName());
    }

    @Test
    public void testUpdateClosingTemplate_notExists() {
        // mock 数据
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));

        // 准备参数
        FmsClosingTemplateSaveReqVO reqVO = buildClosingTemplateSaveReqVO();
        reqVO.setId(999L);

        // 调用，并断言异常
        assertServiceException(() -> closingTemplateService.updateClosingTemplate(reqVO, 10L),
                CLOSING_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteClosingTemplate() {
        // mock 数据
        FmsClosingTemplateDO template = new FmsClosingTemplateDO().setAccountSetId(1L)
                .setName("待删除模板").setCategory(FmsClosingTemplateCategoryEnum.DAILY_EXPENSE.getCategory());
        closingTemplateMapper.insert(template);
        when(accountSetService.validateAccountSetWritePermission(1L, 10L))
                .thenReturn(new FmsAccountSetDO().setId(1L));

        // 调用
        closingTemplateService.deleteClosingTemplate(1L, template.getId(), 10L);

        // 断言
        assertNull(closingTemplateMapper.selectById(template.getId()));
        verify(accountSetService).validateAccountSetWritePermission(1L, 10L);
    }

    // ========== 随机对象 ==========

    /**
     * 构建预置模板引用的科目，编号、编码、名称与系统科目模板一致
     */
    private List<FmsSubjectDO> buildPresetSubjects() {
        return new ArrayList<>(Arrays.asList(
                buildSubject(1331L, "1001", "库存现金"),
                buildSubject(1332L, "1002", "银行存款"),
                buildSubject(1340L, "1122", "应收账款"),
                buildSubject(1344L, "1221", "其他应收款"),
                buildSubject(1349L, "1405", "库存商品"),
                buildSubject(1377L, "2202", "应付账款"),
                buildSubject(1379L, "2211", "应付职工薪酬"),
                buildSubject(1382L, "22210101", "进项税额"),
                buildSubject(1388L, "22210107", "销项税额"),
                buildSubject(1392L, "222102", "未交增值税"),
                buildSubject(1401L, "222111", "应交所得税"),
                buildSubject(1407L, "222117", "应交城市维护建设税"),
                buildSubject(1444L, "5001", "主营业务收入"),
                buildSubject(1469L, "560101", "办公用品"),
                buildSubject(1470L, "560102", "房租"),
                buildSubject(1475L, "560107", "差旅费"),
                buildSubject(1477L, "560109", "通讯费"),
                buildSubject(1478L, "560110", "工资"),
                buildSubject(1495L, "560209", "工资"),
                buildSubject(1505L, "560302", "利息"),
                buildSubject(1506L, "560303", "手续费")));
    }

    private static FmsSubjectDO buildSubject(Long id, String code, String name) {
        return new FmsSubjectDO().setId(id).setCode(code).setName(name);
    }

    private static FmsSubjectDO findSubject(List<FmsSubjectDO> subjects, String code) {
        return subjects.stream().filter(subject -> code.equals(subject.getCode()))
                .findFirst().orElseThrow(() -> new AssertionError("科目不存在：" + code));
    }

    private static FmsSubjectDO findSubject(List<FmsSubjectDO> subjects, Long id) {
        return subjects.stream().filter(subject -> id.equals(subject.getId()))
                .findFirst().orElseThrow(() -> new AssertionError("科目不存在：" + id));
    }

    private static FmsClosingTemplateDO findTemplate(List<FmsClosingTemplateDO> templates, String presetCode) {
        return templates.stream().filter(template -> presetCode.equals(template.getPresetCode()))
                .findFirst().orElseThrow(() -> new AssertionError("预置模板不存在：" + presetCode));
    }

    private static void assertSubjectRule(FmsClosingTemplateDO.SubjectRule rule, FmsSubjectDO subject,
                                          FmsDebitCreditDirectionEnum direction, String amountRatio) {
        assertEquals(subject.getId(), rule.getSubjectId());
        assertEquals(subject.getCode(), rule.getSubjectCode());
        assertEquals(direction.getType(), rule.getDirection());
        assertEquals(0, new BigDecimal(amountRatio).compareTo(rule.getAmountRatio()));
    }

    private List<FmsSubjectDO> buildClosingTemplateSubjects() {
        return Arrays.asList(
                new FmsSubjectDO().setId(101L).setCode("560102").setName("房租"),
                new FmsSubjectDO().setId(102L).setCode("1001").setName("库存现金"));
    }

    private FmsClosingTemplateSaveReqVO buildClosingTemplateSaveReqVO() {
        FmsClosingTemplateSaveReqVO reqVO = new FmsClosingTemplateSaveReqVO();
        reqVO.setAccountSetId(1L);
        reqVO.setName("结转本月房租");
        reqVO.setCategory(FmsClosingTemplateCategoryEnum.DAILY_EXPENSE.getCategory());
        reqVO.setPeriodEnd(true);
        reqVO.setFormulaRule(FmsFormulaRuleEnum.BALANCE.getRule());
        reqVO.setTimeType(FmsClosingTimeTypeEnum.PERIOD_END.getType());
        reqVO.setSort(10);
        FmsClosingTemplateSaveReqVO.SubjectRule debitRule = new FmsClosingTemplateSaveReqVO.SubjectRule();
        debitRule.setSubjectId(101L);
        debitRule.setDigest("结转本月房租");
        debitRule.setDirection(FmsDebitCreditDirectionEnum.DEBIT.getType());
        debitRule.setAmountRatio(new BigDecimal("100"));
        FmsClosingTemplateSaveReqVO.SubjectRule creditRule = new FmsClosingTemplateSaveReqVO.SubjectRule();
        creditRule.setSubjectId(102L);
        creditRule.setDigest("结转本月房租");
        creditRule.setDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        creditRule.setAmountRatio(new BigDecimal("100"));
        reqVO.setSubjects(Arrays.asList(debitRule, creditRule));
        return reqVO;
    }

}

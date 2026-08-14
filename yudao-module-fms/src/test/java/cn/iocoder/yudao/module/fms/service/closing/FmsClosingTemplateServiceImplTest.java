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

    private List<FmsSubjectDO> buildPresetSubjects() {
        List<String> subjectCodes = Arrays.asList("1001", "1002", "1122", "1221", "1405", "2202", "2211",
                "22210101", "22210107", "222102", "222111", "222117", "5001", "560101", "560102",
                "560107", "560110", "560209", "560302", "560303");
        List<FmsSubjectDO> subjects = new ArrayList<>();
        for (int index = 0; index < subjectCodes.size(); index++) {
            subjects.add(new FmsSubjectDO().setId((long) index + 1).setCode(subjectCodes.get(index))
                    .setName("科目" + index));
        }
        return subjects;
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

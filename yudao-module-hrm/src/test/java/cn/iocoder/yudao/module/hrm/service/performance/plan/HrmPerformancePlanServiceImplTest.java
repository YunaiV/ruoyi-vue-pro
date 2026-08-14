package cn.iocoder.yudao.module.hrm.service.performance.plan;

import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import cn.iocoder.yudao.module.hrm.service.performance.config.HrmPerformanceAssessmentTemplateService;
import cn.iocoder.yudao.module.hrm.service.performance.config.HrmPerformanceResultTemplateService;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentBatchReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.HandlerStage;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ResultConfig;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ReviewStage;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.Scope;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.plan.HrmPerformancePlanMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanScopeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewScoringTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PLAN_STATUS_NOT_ALLOW_DELETE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_ACTION_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformancePlanServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformancePlanServiceImpl.class)
public class HrmPerformancePlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmPerformancePlanServiceImpl planService;

    @Resource
    private HrmPerformancePlanMapper planMapper;

    @MockitoBean
    private HrmPerformanceAssessmentService assessmentService;
    @MockitoBean
    private HrmPerformanceAssessmentTemplateService assessmentTemplateService;
    @MockitoBean
    private HrmPerformanceResultTemplateService resultTemplateService;
    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private DeptApi deptApi;

    @Test
    public void testCreatePlan_success() {
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO = buildPlanSaveReqVO("季度绩效计划", 200L);
        CollUtil.getFirst(reqVO.getAssessmentConfig().getDimensions()).setName("计划专用业绩");

        // 调用
        Long planId = planService.createPerformancePlan(reqVO);

        // 断言
        HrmPerformancePlanDO plan = planMapper.selectById(planId);
        assertEquals(HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), plan.getStatus());
        assertEquals(HrmPerformanceStageTypeEnum.NOT_STARTED.getType(), plan.getStageType());
        assertNull(plan.getOperationType());
        assertEquals(true, plan.getResultAudit());
        assertEquals(true, plan.getResultConfirmation());
        assertEquals("季度考核模板", plan.getAssessmentConfig().getName());
        assertEquals("业绩",
                CollUtil.getFirst(plan.getAssessmentConfig().getDimensions()).getName());
        assertEquals("季度结果模板", plan.getResultConfig().getName());
        verify(assessmentTemplateService).validatePerformanceAssessmentTemplateEnabled(
                reqVO.getAssessmentTemplateId());
        verify(resultTemplateService).validatePerformanceResultTemplateEnabled(
                reqVO.getResultTemplateId());
        verify(assessmentService).addPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(planId, actualPlan.getId())), eq(Collections.singletonList(200L)));
    }

    @Test
    public void testCreatePlan_scopeIntersectionNoMatch() {
        // mock 方法
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.emptyList());
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO = buildPlanSaveReqVO("考评范围交集为空", 200L);
        HrmPerformancePlanSaveReqVO.Scope employmentScope = new HrmPerformancePlanSaveReqVO.Scope()
                .setType(HrmPerformancePlanScopeTypeEnum.EMPLOYMENT.getType())
                .setEmployeeType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setEmployeeStatuses(Collections.singletonList(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        reqVO.setScopes(Arrays.asList(CollUtil.getFirst(reqVO.getScopes()), employmentScope));

        // 调用
        Long planId = planService.createPerformancePlan(reqVO);

        // 断言
        verify(assessmentService).addPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(planId, actualPlan.getId())), eq(Collections.emptyList()));
    }

    @Test
    public void testUpdatePlan_success() {
        // mock 数据
        HrmPerformancePlanDO dbPlan = buildPlan(
                "季度绩效计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(dbPlan);
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO =
                buildPlanSaveReqVO("年度绩效计划", 200L, 201L).setId(dbPlan.getId());

        // 调用
        planService.updatePerformancePlan(reqVO);

        // 断言
        HrmPerformancePlanDO plan = planMapper.selectById(dbPlan.getId());
        assertEquals(reqVO.getName(), plan.getName());
        assertEquals(dbPlan.getStatus(), plan.getStatus());
        verify(resultTemplateService).validatePerformanceResultTemplateExists(reqVO.getResultTemplateId());
        verify(assessmentService).syncPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())
                        && Objects.equals(plan.getName(), actualPlan.getName())),
                eq(Arrays.asList(200L, 201L)));
    }

    @Test
    public void testUpdatePlan_changeAssessmentTemplate() {
        // mock 数据
        HrmPerformancePlanDO dbPlan = buildPlan(
                "季度绩效计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(dbPlan);
        HrmPerformanceAssessmentTemplateDO newTemplate = buildAssessmentTemplate().setId(101L);
        when(assessmentTemplateService.validatePerformanceAssessmentTemplateEnabled(newTemplate.getId()))
                .thenReturn(newTemplate);
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO =
                buildPlanSaveReqVO("年度绩效计划", 200L).setId(dbPlan.getId())
                        .setAssessmentTemplateId(newTemplate.getId());

        // 调用
        planService.updatePerformancePlan(reqVO);

        // 断言
        HrmPerformancePlanDO plan = planMapper.selectById(dbPlan.getId());
        assertEquals(newTemplate.getId(), plan.getAssessmentTemplateId());
        verify(assessmentTemplateService).validatePerformanceAssessmentTemplateEnabled(newTemplate.getId());
    }

    @Test
    public void testUpdatePlan_changeResultTemplate() {
        // mock 数据
        HrmPerformancePlanDO dbPlan = buildPlan(
                "季度绩效计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(dbPlan);
        HrmPerformanceResultTemplateDO newTemplate = buildResultTemplate().setId(201L);
        when(resultTemplateService.validatePerformanceResultTemplateEnabled(newTemplate.getId()))
                .thenReturn(newTemplate);
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO =
                buildPlanSaveReqVO("年度绩效计划", 200L).setId(dbPlan.getId())
                        .setResultTemplateId(newTemplate.getId());

        // 调用
        planService.updatePerformancePlan(reqVO);

        // 断言
        HrmPerformancePlanDO plan = planMapper.selectById(dbPlan.getId());
        assertEquals(newTemplate.getId(), plan.getResultTemplateId());
        verify(resultTemplateService).validatePerformanceResultTemplateEnabled(newTemplate.getId());
    }

    @Test
    public void testUpdatePlan_keepsExcludedEmployees() {
        // mock 数据
        HrmPerformancePlanDO dbPlan = buildPlan(
                "季度绩效计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L, 201L);
        dbPlan.setScopes(Arrays.asList(
                CollUtil.getFirst(dbPlan.getScopes()),
                new Scope().setType(HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType())
                        .setEmployeeIds(Collections.singletonList(200L))));
        planMapper.insert(dbPlan);
        // 准备参数
        HrmPerformancePlanSaveReqVO reqVO =
                buildPlanSaveReqVO("年度绩效计划", 200L, 201L).setId(dbPlan.getId());

        // 调用
        planService.updatePerformancePlan(reqVO);

        // 断言
        HrmPerformancePlanDO plan = planMapper.selectById(dbPlan.getId());
        Scope excludedScope = CollUtil.findOne(plan.getScopes(), scope -> Objects.equals(
                scope.getType(), HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType()));
        assertEquals(Collections.singletonList(200L), excludedScope.getEmployeeIds());
        verify(assessmentService).syncPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())),
                eq(Collections.singletonList(201L)));
    }

    @Test
    public void testDeletePlan_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "已归档计划", HrmPerformancePlanStatusEnum.ARCHIVED.getStatus(), 200L);
        planMapper.insert(plan);

        // 调用
        planService.deletePerformancePlan(plan.getId());

        // 断言
        assertNull(planMapper.selectById(plan.getId()));
        verify(assessmentService).deletePerformanceAssessmentListByPlanId(plan.getId());
    }

    @Test
    public void testDeletePlan_running() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "进行中计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        planMapper.insert(plan);

        // 调用，并断言异常
        assertServiceException(() -> planService.deletePerformancePlan(plan.getId()),
                PERFORMANCE_PLAN_STATUS_NOT_ALLOW_DELETE);
        verify(assessmentService, never()).deletePerformanceAssessmentListByPlanId(plan.getId());
    }

    @Test
    public void testAddAssessments_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待添加员工计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(plan);
        // 准备参数
        HrmPerformanceAssessmentBatchReqVO reqVO = new HrmPerformanceAssessmentBatchReqVO()
                .setPlanId(plan.getId()).setEmployeeIds(Collections.singletonList(201L));

        // 调用
        planService.addPerformancePlanEmployees(reqVO);

        // 断言
        verify(employeeService).validateEmployeeListExists(reqVO.getEmployeeIds());
        verify(assessmentService).addPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())), eq(reqVO.getEmployeeIds()));
    }

    @Test
    public void testAddAssessments_persistsIncludedEmployeeForStart() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待添加范围外员工计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(plan);
        when(assessmentService.startPerformanceAssessmentList(any(HrmPerformancePlanDO.class)))
                .thenReturn(HrmPerformanceStageTypeEnum.EXECUTING.getType());
        HrmPerformanceAssessmentBatchReqVO reqVO = new HrmPerformanceAssessmentBatchReqVO()
                .setPlanId(plan.getId()).setEmployeeIds(Collections.singletonList(201L));

        // 调用
        planService.addPerformancePlanEmployees(reqVO);

        // 断言额外员工已写入普通员工范围，不依赖当前考核明细反推范围
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        Scope employeeScope = CollUtil.findOne(updatedPlan.getScopes(), scope -> Objects.equals(
                scope.getType(), HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType()));
        assertEquals(Arrays.asList(200L, 201L), employeeScope.getEmployeeIds());
        clearInvocations(assessmentService);

        // 再次调用启动计划，并断言范围包含原范围和额外员工
        planService.startPerformancePlan(plan.getId());
        verify(assessmentService).addPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())), eq(Arrays.asList(200L, 201L)));
    }

    @Test
    public void testRemoveAssessments_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待移除员工计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L, 201L);
        planMapper.insert(plan);
        // 准备参数
        HrmPerformanceAssessmentBatchReqVO reqVO = new HrmPerformanceAssessmentBatchReqVO()
                .setPlanId(plan.getId()).setEmployeeIds(Collections.singletonList(200L));

        // 调用
        planService.removePerformancePlanEmployees(reqVO);

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        Scope excludedScope = CollUtil.findOne(updatedPlan.getScopes(), scope -> Objects.equals(
                scope.getType(), HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType()));
        assertEquals(reqVO.getEmployeeIds(), excludedScope.getEmployeeIds());
        verify(assessmentService).deletePerformanceAssessmentList(plan.getId(), reqVO.getEmployeeIds());
    }

    @Test
    public void testRemoveAssessments_emptyEmployeeIds() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待移除员工计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(plan);
        // 准备参数
        HrmPerformanceAssessmentBatchReqVO reqVO = new HrmPerformanceAssessmentBatchReqVO()
                .setPlanId(plan.getId()).setEmployeeIds(Collections.emptyList());

        // 调用
        planService.removePerformancePlanEmployees(reqVO);

        // 断言
        verify(assessmentService, never()).deletePerformanceAssessmentList(
                eq(plan.getId()), any());
    }

    @Test
    public void testStartPlan_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待启动计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(plan);
        when(assessmentService.startPerformanceAssessmentList(any(HrmPerformancePlanDO.class)))
                .thenReturn(HrmPerformanceStageTypeEnum.EXECUTING.getType());

        // 调用
        planService.startPerformancePlan(plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformancePlanStatusEnum.RUNNING.getStatus(), updatedPlan.getStatus());
        assertEquals(HrmPerformanceStageTypeEnum.EXECUTING.getType(), updatedPlan.getStageType());
        assertEquals(HrmPerformancePlanOperationTypeEnum.START_SCORING.getType(),
                updatedPlan.getOperationType());
        verify(assessmentService).addPerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())), eq(Collections.singletonList(200L)));
    }

    @Test
    public void testOpenScoring_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待开启评分计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        planMapper.insert(plan);
        when(assessmentService.openPerformanceAssessmentScoring(any(HrmPerformancePlanDO.class)))
                .thenReturn(HrmPerformanceStageTypeEnum.SELF_SCORE.getType());

        // 调用
        planService.openPerformancePlanScoring(plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformanceStageTypeEnum.SELF_SCORE.getType(), updatedPlan.getStageType());
        assertNull(updatedPlan.getOperationType());
    }

    @Test
    public void testStartPerformancePlanInterview_withResultConfirmation() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待发起面谈计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        plan.setResultConfirmation(true);
        planMapper.insert(plan);
        when(assessmentService.startPerformanceAssessmentInterview(any(HrmPerformancePlanDO.class)))
                .thenReturn(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());

        // 调用
        planService.startPerformancePlanInterview(plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), updatedPlan.getStageType());
        assertNull(updatedPlan.getOperationType());
    }

    @Test
    public void testStartPerformancePlanInterview_withoutResultConfirmation() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "无需结果确认计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        plan.setResultConfirmation(false);
        planMapper.insert(plan);
        when(assessmentService.startPerformanceAssessmentInterview(any(HrmPerformancePlanDO.class)))
                .thenReturn(HrmPerformanceStageTypeEnum.END.getType());

        // 调用
        planService.startPerformancePlanInterview(plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), updatedPlan.getStageType());
        assertEquals(HrmPerformancePlanOperationTypeEnum.ARCHIVE.getType(),
                updatedPlan.getOperationType());
    }

    @Test
    public void testArchivePlan_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待归档计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        planMapper.insert(plan);

        // 调用
        planService.archivePerformancePlan(plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus(), updatedPlan.getStatus());
        assertEquals(HrmPerformanceStageTypeEnum.ARCHIVED.getType(), updatedPlan.getStageType());
        assertNull(updatedPlan.getOperationType());
        verify(assessmentService).archivePerformanceAssessmentList(argThat(actualPlan ->
                Objects.equals(plan.getId(), actualPlan.getId())));
    }

    @Test
    public void testTerminatePlan_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "待终止计划", HrmPerformancePlanStatusEnum.RUNNING.getStatus(), 200L);
        planMapper.insert(plan);

        // 调用
        planService.terminatePerformancePlan(1L, plan.getId());

        // 断言
        HrmPerformancePlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(HrmPerformancePlanStatusEnum.TERMINATED.getStatus(), updatedPlan.getStatus());
        assertNull(updatedPlan.getOperationType());
        assertNotNull(updatedPlan.getTerminateTime());
        verify(assessmentService).terminatePerformanceAssessmentListByPlanId(plan.getId(), null);
    }

    @Test
    public void testTerminatePlan_notRunning() {
        // mock 数据
        HrmPerformancePlanDO plan = buildPlan(
                "未开始计划", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(), 200L);
        planMapper.insert(plan);

        // 调用，并断言异常
        assertServiceException(() -> planService.terminatePerformancePlan(1L, plan.getId()),
                PERFORMANCE_STAGE_ACTION_INVALID);
        verify(assessmentService, never()).terminatePerformanceAssessmentListByPlanId(any(), any());
    }

    @Test
    public void testGetPlanStatusCount() {
        // mock 数据
        planMapper.insert(buildPlan(
                "研发季度考核", HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus()));
        planMapper.insert(buildPlan(
                "研发月度考核", HrmPerformancePlanStatusEnum.RUNNING.getStatus()));
        // 测试 name 不匹配
        planMapper.insert(buildPlan(
                "销售季度考核", HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()));
        // 准备参数
        HrmPerformancePlanPageReqVO reqVO = new HrmPerformancePlanPageReqVO();
        reqVO.setName("研发");
        reqVO.setStatus(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());

        // 调用
        Map<Integer, Long> countMap = planService.getPerformancePlanStatusCount(reqVO);

        // 断言
        assertEquals(2, countMap.size());
        assertEquals(1L, countMap.get(HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus()));
        assertEquals(1L, countMap.get(HrmPerformancePlanStatusEnum.RUNNING.getStatus()));
    }

    // ========== 随机对象 ==========

    private HrmPerformancePlanSaveReqVO buildPlanSaveReqVO(String name, Long... employeeIds) {
        HrmPerformanceAssessmentTemplateDO template = buildAssessmentTemplate();
        when(assessmentTemplateService.validatePerformanceAssessmentTemplateExists(template.getId()))
                .thenReturn(template);
        when(assessmentTemplateService.validatePerformanceAssessmentTemplateEnabled(template.getId()))
                .thenReturn(template);
        HrmPerformanceResultTemplateDO resultTemplate = buildResultTemplate();
        when(resultTemplateService.validatePerformanceResultTemplateExists(resultTemplate.getId()))
                .thenReturn(resultTemplate);
        when(resultTemplateService.validatePerformanceResultTemplateEnabled(resultTemplate.getId()))
                .thenReturn(resultTemplate);
        HrmPerformancePlanSaveReqVO.Scope scope = new HrmPerformancePlanSaveReqVO.Scope()
                .setType(HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType())
                .setEmployeeIds(Arrays.asList(employeeIds));
        HrmPerformancePlanSaveReqVO reqVO = new HrmPerformancePlanSaveReqVO();
        reqVO.setName(name);
        reqVO.setAssessmentTemplateId(template.getId());
        reqVO.setAssessmentConfig(buildAssessmentTemplateSaveConfig());
        reqVO.setResultTemplateId(resultTemplate.getId());
        reqVO.setResultConfig(buildResultSaveConfig(resultTemplate.getName()));
        reqVO.setResultAuditStages(Collections.singletonList(buildHandlerStage()));
        reqVO.setAppealStages(Collections.singletonList(buildHandlerStage()));
        reqVO.setAppealTimeoutDays(2);
        reqVO.setAppealTimeoutAction(1);
        reqVO.setScopes(Collections.singletonList(scope));
        fillPlanProcessConfig(reqVO);
        return reqVO;
    }

    private HrmPerformancePlanDO buildPlan(String name, Integer status, Long... employeeIds) {
        Scope scope = new Scope().setType(HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType())
                .setEmployeeIds(Arrays.asList(employeeIds));
        return HrmPerformancePlanDO.builder().name(name).assessmentTemplateId(100L)
                .assessmentConfig(buildAssessmentTemplateConfig())
                .scopes(Collections.singletonList(scope)).reviewStages(buildReviewStages())
                .resultTemplateId(200L).resultConfig(buildResultConfig("季度结果模板"))
                .resultAudit(true).resultConfirmation(true)
                .appealTimeoutDays(2).appealTimeoutAction(1)
                .status(status).stageType(HrmPerformanceStageTypeEnum.NOT_STARTED.getType()).build();
    }

    private static void fillPlanProcessConfig(HrmPerformancePlanSaveReqVO reqVO) {
        reqVO.setCycleType(HrmPerformanceCycleTypeEnum.MONTH.getType());
        reqVO.setQuotaSettingType(HrmPerformanceQuotaSettingTypeEnum.SYSTEM.getType());
        reqVO.setTargetConfirmation(false);
        reqVO.setReviewStages(buildReviewSaveStages());
        reqVO.setResultAudit(true);
        reqVO.setResultConfirmation(true);
        reqVO.setSyncToSalary(false);
    }

    private static List<ReviewStage> buildReviewStages() {
        ReviewStage selfStage = new ReviewStage();
        selfStage.setName("员工自评");
        selfStage.setRater(new HandlerStage().setType(HrmPerformanceRaterTypeEnum.SELF.getType()));
        selfStage.setWeight(BigDecimal.valueOf(30));
        selfStage.setScoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType());
        selfStage.setVisibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        selfStage.setRequiredSetting(false);
        selfStage.setRejectAuthority(false);

        ReviewStage superiorStage = new ReviewStage();
        superiorStage.setName("直属上级评分");
        superiorStage.setRater(new HandlerStage()
                .setType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType()).setLevel(1));
        superiorStage.setWeight(BigDecimal.valueOf(70));
        superiorStage.setScoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType());
        superiorStage.setVisibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        superiorStage.setRequiredSetting(true);
        superiorStage.setRejectAuthority(true);
        return Arrays.asList(selfStage, superiorStage);
    }

    private static List<HrmPerformancePlanSaveReqVO.ReviewStage> buildReviewSaveStages() {
        HrmPerformancePlanSaveReqVO.ReviewStage selfStage =
                new HrmPerformancePlanSaveReqVO.ReviewStage();
        selfStage.setName("员工自评");
        selfStage.setRater(new HrmPerformancePlanSaveReqVO.HandlerStage()
                .setType(HrmPerformanceRaterTypeEnum.SELF.getType()));
        selfStage.setWeight(BigDecimal.valueOf(30));
        selfStage.setScoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType());
        selfStage.setVisibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        selfStage.setRequiredSetting(false);
        selfStage.setRejectAuthority(false);

        HrmPerformancePlanSaveReqVO.ReviewStage superiorStage =
                new HrmPerformancePlanSaveReqVO.ReviewStage();
        superiorStage.setName("直属上级评分");
        superiorStage.setRater(new HrmPerformancePlanSaveReqVO.HandlerStage()
                .setType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType()).setLevel(1));
        superiorStage.setWeight(BigDecimal.valueOf(70));
        superiorStage.setScoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType());
        superiorStage.setVisibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        superiorStage.setRequiredSetting(true);
        superiorStage.setRejectAuthority(true);
        return Arrays.asList(selfStage, superiorStage);
    }

    private static HrmPerformanceAssessmentTemplateDO buildAssessmentTemplate() {
        HrmPerformanceAssessmentTemplateDO.AssessmentConfig config =
                buildAssessmentTemplateConfig();
        return HrmPerformanceAssessmentTemplateDO.builder().id(100L).name(config.getName())
                .scoreCalculation(config.getScoreCalculation())
                .upperLimitType(config.getUpperLimitType())
                .upperLimitScore(config.getUpperLimitScore())
                .dimensions(config.getDimensions()).build();
    }

    private static HrmPerformanceAssessmentTemplateDO.AssessmentConfig buildAssessmentTemplateConfig() {
        HrmPerformanceAssessmentTemplateDO.Quota quota =
                HrmPerformanceAssessmentTemplateDO.Quota.builder()
                        .name("目标达成率").standard("按完成质量评分")
                        .weight(BigDecimal.valueOf(100)).scoreType(1).build();
        HrmPerformanceAssessmentTemplateDO.Dimension dimension =
                HrmPerformanceAssessmentTemplateDO.Dimension.builder()
                        .name("业绩").quotaType(1).weight(BigDecimal.valueOf(100))
                        .allowEdit(false).quotas(Collections.singletonList(quota)).build();
        return HrmPerformanceAssessmentTemplateDO.AssessmentConfig.builder()
                .name("季度考核模板").scoreCalculation(1).upperLimitType(1)
                .upperLimitScore(BigDecimal.valueOf(100))
                .dimensions(Collections.singletonList(dimension)).build();
    }

    private static HrmPerformancePlanSaveReqVO.AssessmentConfig buildAssessmentTemplateSaveConfig() {
        return BeanUtils.toBean(buildAssessmentTemplateConfig(),
                HrmPerformancePlanSaveReqVO.AssessmentConfig.class);
    }

    private static HrmPerformanceResultTemplateDO buildResultTemplate() {
        return HrmPerformanceResultTemplateDO.builder().id(200L).name("季度结果模板")
                .levels(buildResultLevels()).build();
    }

    private static ResultConfig buildResultConfig(String name) {
        return ResultConfig.builder().name(name).levels(buildResultLevels()).build();
    }

    private static HrmPerformancePlanSaveReqVO.ResultConfig buildResultSaveConfig(String name) {
        return BeanUtils.toBean(buildResultConfig(name), HrmPerformancePlanSaveReqVO.ResultConfig.class);
    }

    private static HrmPerformancePlanSaveReqVO.HandlerStage buildHandlerStage() {
        return new HrmPerformancePlanSaveReqVO.HandlerStage()
                .setType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType()).setLevel(1);
    }

    private static List<Level> buildResultLevels() {
        return Arrays.asList(
                Level.builder().name("A").minScore(BigDecimal.valueOf(90))
                        .maxScore(BigDecimal.valueOf(100))
                        .coefficient(BigDecimal.valueOf(1.2)).build(),
                Level.builder().name("B").minScore(BigDecimal.valueOf(80))
                        .maxScore(BigDecimal.valueOf(89.99))
                        .coefficient(BigDecimal.ONE).build(),
                Level.builder().name("C").minScore(BigDecimal.ZERO)
                        .maxScore(BigDecimal.valueOf(79.99))
                        .coefficient(BigDecimal.valueOf(0.8)).build());
    }

}

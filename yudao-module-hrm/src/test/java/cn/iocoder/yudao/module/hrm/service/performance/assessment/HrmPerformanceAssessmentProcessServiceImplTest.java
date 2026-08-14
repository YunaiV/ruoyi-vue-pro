package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceAppealReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceConfirmReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceHandleStageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.HandlerStage;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ResultConfig;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ReviewStage;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaScoreMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentStageMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.plan.HrmPerformancePlanMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealTimeoutActionEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceConfirmationResultEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceResultAuditStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewScoringTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_HANDLER_USER_NOT_BOUND;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_TARGET_CONFIRM_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants.PERFORMANCE_PROCESS_RESULT;
import static cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants.PERFORMANCE_TASK_PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformanceAssessmentProcessServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceAssessmentProcessServiceImpl.class)
public class HrmPerformanceAssessmentProcessServiceImplTest extends BaseDbUnitTest {

    private static final Long EMPLOYEE_ID = 200L;
    private static final Long EMPLOYEE_USER_ID = 1001L;
    private static final Long HANDLER_USER_ID = 2001L;
    private static final Long LEADER_EMPLOYEE_ID = 201L;

    @Resource
    private HrmPerformanceAssessmentProcessServiceImpl processService;

    @Resource
    private HrmPerformanceAssessmentMapper assessmentMapper;
    @Resource
    private HrmPerformanceAssessmentStageMapper assessmentStageMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaMapper assessmentQuotaMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaScoreMapper assessmentQuotaScoreMapper;
    @Resource
    private HrmPerformancePlanMapper planMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private DeptApi deptApi;
    @MockitoBean
    private HrmPerformancePlanService planService;
    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;
    @MockitoBean
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Test
    public void testInitializeAssessmentStages_unboundEmployee_fail() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan()
                .setReviewStages(Collections.singletonList(ReviewStage.builder()
                        .name("员工自评")
                        .rater(HandlerStage.builder()
                                .type(HrmPerformanceRaterTypeEnum.SELF.getType()).build())
                        .weight(new BigDecimal("100"))
                        .scoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType())
                        .visibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent())
                        .requiredSetting(false).rejectAuthority(false).build()))
                .setResultAudit(false).setResultConfirmation(false);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.NOT_STARTED.getType());
        assessmentMapper.insert(assessment);
        when(employeeService.validateEmployeeExists(EMPLOYEE_ID)).thenReturn(
                HrmEmployeeDO.builder().id(EMPLOYEE_ID).name("测试员工").userId(null).build());

        when(employeeService.getEmployeeMap(Collections.singleton(EMPLOYEE_ID))).thenReturn(
                Collections.singletonMap(EMPLOYEE_ID,
                        HrmEmployeeDO.builder().id(EMPLOYEE_ID).name("测试员工").userId(null).build()));

        // 调用、断言
        assertServiceException(() -> processService.initializeAssessmentStages(plan, assessment),
                PERFORMANCE_STAGE_HANDLER_USER_NOT_BOUND, "测试员工");
        assertTrue(assessmentStageMapper.selectListByAssessmentId(assessment.getId()).isEmpty());
    }

    @Test
    public void testInitializeAssessmentStages_sameHandler_mergesStages() {
        // mock 数据
        HandlerStage superior = HandlerStage.builder()
                .type(HrmPerformanceRaterTypeEnum.SUPERIOR.getType()).level(1).build();
        HandlerStage specified = HandlerStage.builder()
                .type(HrmPerformanceRaterTypeEnum.SPECIFIED.getType())
                .employeeId(LEADER_EMPLOYEE_ID).build();
        HrmPerformancePlanDO plan = randomPlan()
                .setReviewStages(Arrays.asList(
                        ReviewStage.builder().name("上级评分").rater(superior)
                                .weight(new BigDecimal("40"))
                                .scoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType())
                                .visibleContent(HrmPerformanceReviewVisibleContentEnum.SELF.getContent())
                                .requiredSetting(true).rejectAuthority(true).build(),
                        ReviewStage.builder().name("指定员工评分").rater(specified)
                                .weight(new BigDecimal("60"))
                                .scoringType(HrmPerformanceReviewScoringTypeEnum.QUOTA.getType())
                                .visibleContent(HrmPerformanceReviewVisibleContentEnum.ALL.getContent())
                                .requiredSetting(false).rejectAuthority(false).build()))
                .setResultAudit(true).setResultAuditStages(Arrays.asList(superior, specified))
                .setResultConfirmation(true).setAppealStages(Arrays.asList(superior, specified));
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.NOT_STARTED.getType());
        assessmentMapper.insert(assessment);
        HrmEmployeeDO employee = HrmEmployeeDO.builder().id(EMPLOYEE_ID)
                .name("测试员工").leaderEmployeeId(LEADER_EMPLOYEE_ID).build();
        HrmEmployeeDO leader = HrmEmployeeDO.builder().id(LEADER_EMPLOYEE_ID)
                .name("测试上级").build();
        when(employeeService.validateEmployeeExists(EMPLOYEE_ID)).thenReturn(employee);
        when(employeeService.getEmployee(LEADER_EMPLOYEE_ID)).thenReturn(leader);
        employee.setUserId(EMPLOYEE_USER_ID);
        leader.setUserId(HANDLER_USER_ID);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(EMPLOYEE_ID, employee);
        employeeMap.put(LEADER_EMPLOYEE_ID, leader);
        when(employeeService.getEmployeeMap(any())).thenReturn(employeeMap);

        // 调用
        processService.initializeAssessmentStages(plan, assessment);

        // 断言
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentId(assessment.getId());
        assertEquals(5, stages.size());
        HrmPerformanceAssessmentStageDO reviewStage = stages.get(0);
        assertEquals(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(), reviewStage.getType());
        assertEquals(LEADER_EMPLOYEE_ID, reviewStage.getHandlerEmployeeId());
        assertEquals(new BigDecimal("100.00"), reviewStage.getWeight());
        assertEquals("上级评分", reviewStage.getName());
        assertEquals(HrmPerformanceReviewVisibleContentEnum.SELF.getContent(),
                reviewStage.getVisibleContent());
        assertTrue(reviewStage.getRequiredSetting());
        assertTrue(reviewStage.getRejectAuthority());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(), stages.get(1).getType());
        assertEquals(LEADER_EMPLOYEE_ID, stages.get(1).getHandlerEmployeeId());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), stages.get(2).getType());
        assertEquals(HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), stages.get(3).getType());
        assertEquals(LEADER_EMPLOYEE_ID, stages.get(3).getHandlerEmployeeId());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), stages.get(4).getType());
    }

    @Test
    public void testConfirmTarget_success() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType());
        assessmentMapper.insert(assessment);
        assessmentStageMapper.insert(randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(),
                "目标确认", HANDLER_USER_ID, 1,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        mockHandlerEmployee(HANDLER_USER_ID);
        HrmPortalPerformanceConfirmReqVO reqVO = new HrmPortalPerformanceConfirmReqVO();
        reqVO.setAssessmentId(assessment.getId());
        reqVO.setPass(HrmPerformanceConfirmationResultEnum.PASS.getResult());
        reqVO.setComment("目标确认通过");

        // 调用
        processService.confirmTarget(HANDLER_USER_ID, reqVO);

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceStageTypeEnum.EXECUTING.getType(), updated.getStageType());
        assertEquals(HrmPerformanceConfirmationResultEnum.PASS.getResult(),
                updated.getTargetConfirmationResult());
        assertEquals("目标确认通过", updated.getTargetConfirmationComment());
    }

    @Test
    public void testConfirmTarget_otherUser() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType());
        assessmentMapper.insert(assessment);
        assessmentStageMapper.insert(randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(),
                "目标确认", HANDLER_USER_ID, 1,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        mockHandlerEmployee(HANDLER_USER_ID);
        // 准备参数
        HrmPortalPerformanceConfirmReqVO reqVO = new HrmPortalPerformanceConfirmReqVO();
        reqVO.setAssessmentId(assessment.getId());
        reqVO.setPass(HrmPerformanceConfirmationResultEnum.PASS.getResult());
        reqVO.setComment("目标确认通过");

        // 调用，并断言异常
        assertServiceException(() -> processService.confirmTarget(3001L, reqVO),
                PERFORMANCE_TARGET_CONFIRM_NO_PERMISSION);
    }

    @Test
    public void testConfirmTarget_rejectReopensFillQuotaStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO fillStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(),
                "指标填写", EMPLOYEE_ID, 1,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(fillStage);
        HrmPerformanceAssessmentStageDO targetStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(),
                "目标确认", HANDLER_USER_ID, 2,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        assessmentStageMapper.insert(targetStage);
        mockEmployee();
        mockHandlerEmployee(HANDLER_USER_ID);
        HrmPortalPerformanceConfirmReqVO reqVO = new HrmPortalPerformanceConfirmReqVO();
        reqVO.setAssessmentId(assessment.getId());
        reqVO.setPass(HrmPerformanceConfirmationResultEnum.REJECT.getResult());
        reqVO.setComment("指标需要调整");

        // 调用
        processService.confirmTarget(HANDLER_USER_ID, reqVO);

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(), updated.getStageType());
        assertEquals(fillStage.getSort(), updated.getStageSort());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fillStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus(),
                assessmentStageMapper.selectById(targetStage.getId()).getStatus());
    }

    @Test
    public void testHandleResultAudit_sequentialPass() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO firstStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核（第1级）", HANDLER_USER_ID, 3,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        assessmentStageMapper.insert(firstStage);
        HrmPerformanceAssessmentStageDO secondStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核（第2级）", 2002L, 4,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(secondStage);
        HrmPerformanceAssessmentStageDO resultConfirmStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", EMPLOYEE_USER_ID, 5,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(resultConfirmStage);
        mockHandlerEmployee(HANDLER_USER_ID);
        mockHandlerEmployee(2002L);

        // 调用
        processService.handleResultAudit(HANDLER_USER_ID,
                buildHandleReqVO(assessment.getId(), firstStage.getId(),
                        HrmPerformanceConfirmationResultEnum.PASS.getResult()));
        processService.handleResultAudit(2002L,
                buildHandleReqVO(assessment.getId(), secondStage.getId(),
                        HrmPerformanceConfirmationResultEnum.PASS.getResult()));

        // 断言
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(firstStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(secondStage.getId()).getStatus());
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceResultAuditStatusEnum.PASS.getStatus(), updated.getResultAuditStatus());
        assertNotNull(updated.getResultAuditTime());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), updated.getStageType());
        assertEquals(resultConfirmStage.getSort(), updated.getStageSort());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus(),
                assessmentStageMapper.selectById(resultConfirmStage.getId()).getStatus());
        verify(planService).updatePerformancePlanStageTypeAndOperationType(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType());
    }

    @Test
    public void testGetMyResultConfirmationTaskPage_onlyPendingStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        planMapper.insert(plan);
        HrmPerformanceAssessmentDO pendingAssessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        assessmentMapper.insert(pendingAssessment);
        assessmentStageMapper.insert(randomStage(
                pendingAssessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", HANDLER_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        HrmPerformancePlanDO waitingPlan = randomPlan();
        planMapper.insert(waitingPlan);
        HrmPerformanceAssessmentDO waitingAssessment = randomAssessment(
                waitingPlan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        assessmentMapper.insert(waitingAssessment);
        assessmentStageMapper.insert(randomStage(
                waitingAssessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", HANDLER_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
        mockHandlerEmployee(HANDLER_USER_ID);

        // 准备参数
        HrmPortalPerformanceTaskPageReqVO reqVO = new HrmPortalPerformanceTaskPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setStageStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());

        // 调用
        PageResult<HrmPerformanceAssessmentStageDO> result =
                processService.getMyResultConfirmationTaskPage(HANDLER_USER_ID, reqVO);

        // 断言
        assertEquals(1, result.getList().size());
        assertEquals(pendingAssessment.getId(), CollUtil.getFirst(result.getList()).getAssessmentId());
    }

    @Test
    public void testNotifyPendingStage_accountRebound_success() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        HrmPerformanceAssessmentStageDO stage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核", HANDLER_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        when(employeeService.getEmployee(HANDLER_USER_ID)).thenReturn(
                new HrmEmployeeDO().setId(HANDLER_USER_ID).setUserId(3001L));
        when(planService.getPerformancePlan(plan.getId())).thenReturn(plan);

        // 调用
        processService.notifyPendingStage(assessment, stage);

        // 断言
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(captor.capture());
        assertEquals(3001L, captor.getValue().getUserId());
        assertEquals(PERFORMANCE_TASK_PENDING, captor.getValue().getTemplateCode());
        assertEquals(plan.getName(), captor.getValue().getTemplateParams().get("planName"));
        assertEquals(stage.getName(), captor.getValue().getTemplateParams().get("stageName"));
        assertEquals("/hrm/portal/performance/assessment",
                captor.getValue().getTemplateParams().get("route"));
    }

    @Test
    public void testNotifyProcessResult_accountRebound_success() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        when(employeeService.getEmployeeListByIds(Collections.singleton(EMPLOYEE_ID)))
                .thenReturn(Collections.singletonList(
                        new HrmEmployeeDO().setId(EMPLOYEE_ID).setUserId(3001L)));
        when(planService.getPerformancePlan(plan.getId())).thenReturn(plan);

        // 调用
        processService.notifyProcessResult(assessment, Collections.singleton(EMPLOYEE_ID),
                "结果审核", "已驳回，请重新评分");

        // 断言
        ArgumentCaptor<NotifySendSingleToUserReqDTO> captor =
                ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(captor.capture());
        assertEquals(3001L, captor.getValue().getUserId());
        assertEquals(PERFORMANCE_PROCESS_RESULT, captor.getValue().getTemplateCode());
        assertEquals(plan.getName(), captor.getValue().getTemplateParams().get("planName"));
        assertEquals("结果审核", captor.getValue().getTemplateParams().get("actionName"));
        assertEquals("已驳回，请重新评分", captor.getValue().getTemplateParams().get("result"));
        assertEquals("/hrm/portal/performance/assessment",
                captor.getValue().getTemplateParams().get("route"));
    }

    @Test
    public void testGetMyTaskCountMap_success() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        planMapper.insert(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.SELF_SCORE.getType());
        assessmentMapper.insert(assessment);
        assessmentStageMapper.insert(randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.SELF_SCORE.getType(),
                "员工自评", HANDLER_USER_ID, 1,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        assessmentStageMapper.insert(randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(),
                "目标确认", HANDLER_USER_ID, 2,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
        mockHandlerEmployee(HANDLER_USER_ID);

        // 调用
        Map<Integer, Map<Integer, Long>> countMap =
                processService.getMyTaskCountMap(HANDLER_USER_ID, null);

        // 断言
        assertEquals(1L, countMap.get(HrmPerformanceStageTypeEnum.SELF_SCORE.getType())
                .get(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        assertEquals(1L, countMap.get(HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType())
                .get(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
    }

    @Test
    public void testValidateTaskStage_completedStage() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                randomLongId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO stage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核", HANDLER_USER_ID, 3,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(stage);
        mockHandlerEmployee(HANDLER_USER_ID);

        // 调用
        HrmPerformanceAssessmentStageDO result = processService.validateTaskStage(
                HANDLER_USER_ID, assessment.getId(), stage.getId());

        // 断言
        assertEquals(stage.getId(), result.getId());
    }

    @Test
    public void testValidateTaskStage_otherUser() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                randomLongId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO stage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核", HANDLER_USER_ID, 3,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(stage);
        mockHandlerEmployee(HANDLER_USER_ID);

        // 调用，并断言异常
        assertServiceException(() -> processService.validateTaskStage(
                3001L, assessment.getId(), stage.getId()), PERFORMANCE_STAGE_NO_PERMISSION);
    }

    @Test
    public void testStartResultAudit_withoutAuditWaitsForInterview() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        plan.setResultAudit(false);
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO resultConfirmStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", EMPLOYEE_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(resultConfirmStage);

        // 调用
        processService.startResultAudit(assessment.getId());

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceResultAuditStatusEnum.PASS.getStatus(), updated.getResultAuditStatus());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), updated.getStageType());
        assertEquals(resultConfirmStage.getSort(), updated.getStageSort());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus(),
                assessmentStageMapper.selectById(resultConfirmStage.getId()).getStatus());
        verify(planService).updatePerformancePlanStageTypeAndOperationType(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType());
    }

    @Test
    public void testStartResultAudit_preservesProcessedAuditStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        plan.setResultAudit(true);
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO processedStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "一级结果审核", 2001L, 4,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(processedStage);
        HrmPerformanceAssessmentStageDO resumeStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "二级结果审核", HANDLER_USER_ID, 5,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(resumeStage);
        when(employeeService.getEmployee(HANDLER_USER_ID))
                .thenReturn(new HrmEmployeeDO().setId(HANDLER_USER_ID).setUserId(HANDLER_USER_ID));

        // 调用
        HrmPortalPerformanceProcessRespVO result = processService.startResultAudit(assessment.getId());

        // 断言
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(processedStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(resumeStage.getId()).getStatus());
        assertEquals(resumeStage.getId(), result.getNextStageId());
    }

    @Test
    public void testStartResultAudit_withoutAuditAndResultConfirmationCompletesEndStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        plan.setResultAudit(false).setResultConfirmation(false);
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO endStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.END.getType(),
                "结束", null, 4, HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(endStage);

        // 调用
        HrmPortalPerformanceProcessRespVO processResult = processService.startResultAudit(assessment.getId());

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus(), updated.getProcessStatus());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), updated.getStageType());
        assertEquals(endStage.getSort(), updated.getStageSort());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(endStage.getId()).getStatus());
        assertNull(processResult.getNextStageId());
        verify(planService).updatePerformancePlanStageTypeAndOperationType(
                plan.getId(), HrmPerformanceStageTypeEnum.END.getType(),
                HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType());
    }

    @Test
    public void testHandleResultAudit_afterAppealReactivatesResultConfirmation() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO auditStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果复核", HANDLER_USER_ID, 3,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        assessmentStageMapper.insert(auditStage);
        HrmPerformanceAssessmentStageDO resultConfirmStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", EMPLOYEE_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.APPEALED.getStatus());
        assessmentStageMapper.insert(resultConfirmStage);
        mockHandlerEmployee(HANDLER_USER_ID);

        // 调用
        processService.handleResultAudit(HANDLER_USER_ID,
                buildHandleReqVO(assessment.getId(), auditStage.getId(),
                        HrmPerformanceConfirmationResultEnum.PASS.getResult()));

        // 断言：申诉前已经发起绩效面谈，重评完成后无需再次发起
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), updated.getStageType());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(resultConfirmStage.getId()).getStatus());
        verify(planService).updatePerformancePlanStageTypeAndOperationType(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), null);
    }

    @Test
    public void testHandleResultAudit_rejectReopensReviewStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        assessment.setScore(new BigDecimal("88.00")).setResultLevel("B").setCoefficient(BigDecimal.ONE);
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentStageDO firstReviewStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                "员工自评", EMPLOYEE_ID, 1,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(firstReviewStage);
        HrmPerformanceAssessmentStageDO middleReviewStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                "直属上级评分", HANDLER_USER_ID, 2,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(middleReviewStage);
        HrmPerformanceAssessmentStageDO lastReviewStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                "部门负责人评分", 2003L, 3,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.insert(lastReviewStage);
        HrmPerformanceAssessmentStageDO auditStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                "结果审核", 2002L, 4, HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        assessmentStageMapper.insert(auditStage);
        HrmPerformanceAssessmentQuotaDO quota = HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(assessment.getId()).dimensionId(1L)
                .preset(true).name("目标达成率")
                .finalScore(new BigDecimal("88.00")).build();
        assessmentQuotaMapper.insert(quota);
        assessmentQuotaScoreMapper.insert(new HrmPerformanceAssessmentQuotaScoreDO()
                .setAssessmentStageId(firstReviewStage.getId()).setAssessmentQuotaId(quota.getId())
                .setScore(new BigDecimal("80.00")));
        assessmentQuotaScoreMapper.insert(new HrmPerformanceAssessmentQuotaScoreDO()
                .setAssessmentStageId(middleReviewStage.getId()).setAssessmentQuotaId(quota.getId())
                .setScore(new BigDecimal("88.00")));
        assessmentQuotaScoreMapper.insert(new HrmPerformanceAssessmentQuotaScoreDO()
                .setAssessmentStageId(lastReviewStage.getId()).setAssessmentQuotaId(quota.getId())
                .setScore(new BigDecimal("92.00")));
        HrmPortalPerformanceHandleStageReqVO reqVO = buildHandleReqVO(assessment.getId(), auditStage.getId(),
                HrmPerformanceConfirmationResultEnum.REJECT.getResult());
        reqVO.setReviewStageIds(Arrays.asList(firstReviewStage.getId(), lastReviewStage.getId()));
        mockHandlerEmployee(2002L);

        // 调用
        processService.handleResultAudit(2002L, reqVO);

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(), updated.getStageType());
        assertNull(updated.getScore());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(firstReviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(middleReviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(lastReviewStage.getId()).getStatus());
        assertNull(assessmentQuotaMapper.selectById(quota.getId()).getFinalScore());
        assertEquals(0, assessmentQuotaScoreMapper.selectListByAssessmentStageIds(
                Arrays.asList(firstReviewStage.getId(), lastReviewStage.getId())).size());
        assertEquals(1, assessmentQuotaScoreMapper.selectListByAssessmentStageIds(
                Collections.singleton(middleReviewStage.getId())).size());
    }

    @Test
    public void testConfirmResult_completesEndStage() {
        // mock 数据
        HrmPerformancePlanDO plan = randomPlan();
        mockPlan(plan);
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        assessmentMapper.insert(assessment);
        assessmentStageMapper.insert(randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", EMPLOYEE_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        HrmPerformanceAssessmentStageDO appealStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                "申诉确认", HANDLER_USER_ID, 5,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(appealStage);
        HrmPerformanceAssessmentStageDO endStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.END.getType(),
                "结束", null, 6, HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(endStage);
        mockEmployee();
        HrmPortalPerformanceConfirmReqVO reqVO = new HrmPortalPerformanceConfirmReqVO();
        reqVO.setAssessmentId(assessment.getId());
        reqVO.setPass(HrmPerformanceConfirmationResultEnum.PASS.getResult());
        reqVO.setComment("认可绩效结果");

        // 调用
        processService.confirmResult(EMPLOYEE_USER_ID, reqVO);

        // 断言
        HrmPerformanceAssessmentDO updated = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus(), updated.getProcessStatus());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), updated.getStageType());
        assertEquals(endStage.getSort(), updated.getStageSort());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(endStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(appealStage.getId()).getStatus());
    }

    @Test
    public void testHandleAppeal_approveReopensReviewStage() {
        // mock 数据
        AppealFixture fixture = randomAppealFixture();
        mockPlan(fixture.plan);
        assessmentMapper.insert(fixture.assessment);
        assessmentStageMapper.insert(fixture.reviewStage);
        assessmentStageMapper.insert(fixture.resultConfirmStage);
        assessmentStageMapper.insert(fixture.appealStage);
        assessmentStageMapper.insert(fixture.endStage);
        mockEmployee();
        mockHandlerEmployee(HANDLER_USER_ID);
        HrmPortalPerformanceAppealReqVO appealReqVO = new HrmPortalPerformanceAppealReqVO();
        appealReqVO.setAssessmentId(fixture.assessment.getId());
        appealReqVO.setAppealReason("评分结果需要复核");
        appealReqVO.setReviewStageIds(Collections.singletonList(fixture.reviewStage.getId()));

        // mock 数据：先提交申诉
        processService.submitAppeal(EMPLOYEE_USER_ID, appealReqVO);
        HrmPerformanceAssessmentStageDO appealStage = assessmentStageMapper.selectById(fixture.appealStage.getId());

        // 调用
        processService.handleAppeal(HANDLER_USER_ID,
                buildHandleReqVO(fixture.assessment.getId(), appealStage.getId(),
                        HrmPerformanceConfirmationResultEnum.PASS.getResult()));

        // 断言
        HrmPerformanceAssessmentDO updated =
                assessmentMapper.selectById(fixture.assessment.getId());
        assertEquals(HrmPerformanceAppealStatusEnum.PASS.getStatus(), updated.getAppealStatus());
        assertEquals(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(), updated.getStageType());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
    }

    @Test
    public void testHandleAppeal_rejectCompletesEndStage() {
        // mock 数据
        AppealFixture fixture = randomAppealFixture();
        mockPlan(fixture.plan);
        assessmentMapper.insert(fixture.assessment);
        assessmentStageMapper.insert(fixture.reviewStage);
        assessmentStageMapper.insert(fixture.resultConfirmStage);
        assessmentStageMapper.insert(fixture.appealStage);
        fixture.endStage.setSort(7);
        HrmPerformanceAssessmentStageDO laterAppealStage = randomStage(
                fixture.assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                "申诉确认（第2级）", 2002L, 6,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.insert(laterAppealStage);
        assessmentStageMapper.insert(fixture.endStage);
        mockEmployee();
        mockHandlerEmployee(HANDLER_USER_ID);
        HrmPortalPerformanceAppealReqVO appealReqVO = new HrmPortalPerformanceAppealReqVO();
        appealReqVO.setAssessmentId(fixture.assessment.getId());
        appealReqVO.setAppealReason("评分结果需要复核");
        appealReqVO.setReviewStageIds(Collections.singletonList(fixture.reviewStage.getId()));

        // mock 数据：先提交申诉
        processService.submitAppeal(EMPLOYEE_USER_ID, appealReqVO);

        // 调用
        processService.handleAppeal(HANDLER_USER_ID,
                buildHandleReqVO(fixture.assessment.getId(), fixture.appealStage.getId(),
                        HrmPerformanceConfirmationResultEnum.REJECT.getResult()));

        // 断言
        HrmPerformanceAssessmentDO updated =
                assessmentMapper.selectById(fixture.assessment.getId());
        assertEquals(HrmPerformanceAppealStatusEnum.REJECT.getStatus(), updated.getAppealStatus());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), updated.getStageType());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(fixture.endStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(laterAppealStage.getId()).getStatus());
    }

    @Test
    public void testProcessAppealTimeout_reject() {
        // mock 数据
        AppealFixture fixture = randomAppealFixture();
        mockPlan(fixture.plan);
        assessmentMapper.insert(fixture.assessment);
        assessmentStageMapper.insert(fixture.reviewStage);
        assessmentStageMapper.insert(fixture.resultConfirmStage);
        assessmentStageMapper.insert(fixture.appealStage);
        assessmentStageMapper.insert(fixture.endStage);
        mockEmployee();
        fixture.appealStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                .setDeadlineTime(LocalDateTime.now().minusMinutes(1));
        assessmentStageMapper.updateById(fixture.appealStage);
        fixture.assessment.setStageType(HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())
                .setAppealStatus(HrmPerformanceAppealStatusEnum.PENDING.getStatus());
        assessmentMapper.updateById(fixture.assessment);
        // 调用
        List<HrmPerformanceAssessmentStageDO> timeoutStages =
                processService.getAppealTimeoutStageList(LocalDateTime.now());
        assertEquals(1, timeoutStages.size());
        assertTrue(processService.processAppealTimeout(fixture.appealStage.getId()));
        assertFalse(processService.processAppealTimeout(fixture.appealStage.getId()));

        // 断言
        HrmPerformanceAssessmentDO updated =
                assessmentMapper.selectById(fixture.assessment.getId());
        assertEquals(HrmPerformanceAppealStatusEnum.REJECT.getStatus(), updated.getAppealStatus());
        assertEquals(HrmPerformanceStageTypeEnum.END.getType(), updated.getStageType());
        HrmPerformanceAssessmentStageDO updatedStage =
                assessmentStageMapper.selectById(fixture.appealStage.getId());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus(), updatedStage.getStatus());
        assertEquals("申诉确认超期，系统自动驳回", updatedStage.getRejectReason());
    }

    // ========== 随机对象 ==========

    private AppealFixture randomAppealFixture() {
        HrmPerformancePlanDO plan = randomPlan();
        plan.setAppealTimeoutAction(HrmPerformanceAppealTimeoutActionEnum.REJECT.getAction());
        HrmPerformanceAssessmentDO assessment = randomAssessment(
                plan.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        assessment.setScore(new BigDecimal("88.00")).setResultLevel("B").setCoefficient(BigDecimal.ONE);
        HrmPerformanceAssessmentStageDO reviewStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                "直属上级评分", HANDLER_USER_ID, 2,
                HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        HrmPerformanceAssessmentStageDO resultConfirmStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                "结果确认", EMPLOYEE_USER_ID, 4,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        HrmPerformanceAssessmentStageDO appealStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                "申诉确认", HANDLER_USER_ID, 5,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        HrmPerformanceAssessmentStageDO endStage = randomStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.END.getType(),
                "结束", null, 6, HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        return new AppealFixture(plan, assessment, reviewStage, resultConfirmStage, appealStage, endStage);
    }

    private HrmPerformancePlanDO randomPlan() {
        return randomPojo(HrmPerformancePlanDO.class, plan -> plan
                .setId(randomLongId()).setOperationType(null)
                .setCycleType(HrmPerformanceCycleTypeEnum.MONTH.getType())
                .setCycle("2026-07").setQuarter(null)
                .setAssessmentTemplateId(randomLongId()).setAssessmentConfig(buildAssessmentConfig())
                .setScopes(Collections.emptyList()).setQuotaSettingType(null)
                .setTargetConfirmation(false).setTargetConfirmationStage(null)
                .setReviewStages(Collections.emptyList())
                .setResultAudit(true).setResultAuditStages(Collections.emptyList())
                .setResultConfirmation(true).setAppealStages(Collections.emptyList())
                .setAppealTimeoutDays(2)
                .setAppealTimeoutAction(HrmPerformanceAppealTimeoutActionEnum.REJECT.getAction())
                .setResultTemplateId(randomLongId()).setResultConfig(buildResultConfig())
                .setSyncToSalary(false).setPaidForMonth(null)
                .setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.EXECUTING.getType()));
    }

    private HrmPerformanceAssessmentDO randomAssessment(Long planId, Integer stageType) {
        return randomPojo(HrmPerformanceAssessmentDO.class, assessment -> assessment
                .setId(randomLongId()).setPlanId(planId).setEmployeeId(EMPLOYEE_ID)
                .setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                .setStageType(stageType).setStageSort(null)
                .setScore(null).setResultLevel(null).setCoefficient(null)
                .setTargetConfirmationResult(null).setTargetConfirmationComment(null)
                .setTargetConfirmationTime(null).setSelfComment(null).setReviewerComment(null)
                .setResultComment(null).setResultConfirmationTime(null)
                .setResultAuditStatus(null).setResultAuditTime(null).setResultAuditReason(null)
                .setAppealReason(null).setAppealFileUrls(Collections.emptyList()).setAppealSubmitTime(null)
                .setAppealStatus(HrmPerformanceAppealStatusEnum.NONE.getStatus())
                .setAppealTime(null).setAppealComment(null).setArchiveTime(null));
    }

    private HrmPerformanceAssessmentStageDO randomStage(
            Long assessmentId, Integer type, String name, Long handlerEmployeeId,
            Integer sort, Integer status) {
        HrmPerformanceAssessmentStageDO stage = randomPojo(HrmPerformanceAssessmentStageDO.class,
                item -> item.setId(randomLongId()).setAssessmentId(assessmentId)
                        .setType(type).setName(name)
                        .setHandlerEmployeeId(handlerEmployeeId)
                        .setRaterType(null).setWeight(null).setScoringType(null).setVisibleContent(null)
                        .setRequiredSetting(false).setRejectAuthority(false)
                        .setSort(sort).setStatus(status)
                        .setScore(null).setResultLevel(null).setComment(null).setRejectReason(null)
                        .setSubmitTime(null).setDeadlineTime(null));
        if (HrmPerformanceStageTypeEnum.OTHER_SCORE.getType().equals(type)) {
            stage.setRaterType(HrmPerformanceRaterTypeEnum.SPECIFIED.getType())
                    .setWeight(new BigDecimal("100"));
        }
        return stage;
    }

    private HrmPortalPerformanceHandleStageReqVO buildHandleReqVO(
            Long assessmentId, Long stageId, Integer pass) {
        HrmPortalPerformanceHandleStageReqVO reqVO = new HrmPortalPerformanceHandleStageReqVO();
        reqVO.setAssessmentId(assessmentId);
        reqVO.setStageId(stageId);
        reqVO.setPass(pass);
        reqVO.setComment(HrmPerformanceConfirmationResultEnum.PASS.getResult().equals(pass)
                ? "审核通过" : "退回重新评分");
        return reqVO;
    }

    private void mockEmployee() {
        HrmEmployeeDO employee = randomPojo(HrmEmployeeDO.class,
                item -> item.setId(EMPLOYEE_ID).setName("测试员工").setUserId(EMPLOYEE_USER_ID));
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(employee);
        when(employeeService.getEmployeeByUserId(EMPLOYEE_USER_ID)).thenReturn(employee);
    }

    private void mockHandlerEmployee(Long userId) {
        when(employeeService.getEmployeeByUserId(userId))
                .thenReturn(new HrmEmployeeDO().setId(userId).setUserId(userId));
    }

    private void mockPlan(HrmPerformancePlanDO plan) {
        when(planService.validatePerformancePlanExists(plan.getId())).thenReturn(plan);
        when(planService.getPerformancePlan(plan.getId())).thenReturn(plan);
    }

    private HrmPerformanceAssessmentTemplateDO.AssessmentConfig buildAssessmentConfig() {
        return HrmPerformanceAssessmentTemplateDO.AssessmentConfig.builder()
                .name("季度考核模板").scoreCalculation(1).upperLimitType(1)
                .upperLimitScore(new BigDecimal("100")).dimensions(Collections.singletonList(
                        HrmPerformanceAssessmentTemplateDO.Dimension.builder()
                                .name("业绩").quotaType(1).weight(new BigDecimal("100"))
                                .allowEdit(false).quotas(Collections.singletonList(
                                        HrmPerformanceAssessmentTemplateDO.Quota.builder()
                                                .name("目标达成率").weight(new BigDecimal("100"))
                                                .scoreType(1).build()))
                                .build()))
                .build();
    }

    private ResultConfig buildResultConfig() {
        Level level = new Level().setName("A").setMinScore(BigDecimal.ZERO)
                .setMaxScore(new BigDecimal("100")).setCoefficient(BigDecimal.ONE);
        return new ResultConfig().setName("默认结果模板").setLevels(Collections.singletonList(level));
    }

    private static final class AppealFixture {

        private final HrmPerformancePlanDO plan;
        private final HrmPerformanceAssessmentDO assessment;
        private final HrmPerformanceAssessmentStageDO reviewStage;
        private final HrmPerformanceAssessmentStageDO resultConfirmStage;
        private final HrmPerformanceAssessmentStageDO appealStage;
        private final HrmPerformanceAssessmentStageDO endStage;

        private AppealFixture(HrmPerformancePlanDO plan,
                              HrmPerformanceAssessmentDO assessment,
                              HrmPerformanceAssessmentStageDO reviewStage,
                              HrmPerformanceAssessmentStageDO resultConfirmStage,
                              HrmPerformanceAssessmentStageDO appealStage,
                              HrmPerformanceAssessmentStageDO endStage) {
            this.plan = plan;
            this.assessment = assessment;
            this.reviewStage = reviewStage;
            this.resultConfirmStage = resultConfirmStage;
            this.appealStage = appealStage;
            this.endStage = endStage;
        }
    }

}

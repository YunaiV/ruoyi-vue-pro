package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceProcessRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentActionRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformanceAssessmentQueryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@ExtendWith(MockitoExtension.class)
public class HrmPerformanceAssessmentQueryServiceImplTest {

    private static final Long ASSESSMENT_ID = 1L;
    private static final Long PLAN_ID = 2L;
    private static final Long EMPLOYEE_ID = 3L;
    private static final Long HANDLER_EMPLOYEE_ID = 4L;
    private static final Long HANDLER_USER_ID = 5L;
    private static final Long DIMENSION_ID = 6L;
    private static final Long QUOTA_ID = 7L;
    private static final Long STAGE_ID = 8L;

    @InjectMocks
    private HrmPerformanceAssessmentQueryServiceImpl assessmentQueryService;

    @Mock
    private HrmPerformanceAssessmentService assessmentService;
    @Mock
    private HrmPerformancePlanService planService;
    @Mock
    private HrmEmployeeService employeeService;
    @Mock
    private DeptApi deptApi;
    @Mock
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Test
    public void testBuildAssessmentRespVO_enrichesRelatedData() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.SELF_SCORE.getType());
        HrmPerformanceAssessmentStageDO stage = buildStage(
                HrmPerformanceStageTypeEnum.SELF_SCORE.getType(),
                HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        mockRelatedData(stage);

        // 调用
        HrmPerformanceAssessmentRespVO respVO = assessmentQueryService.getPerformanceAssessmentRespVO(assessment);

        // 断言
        assertEquals("季度绩效", respVO.getName());
        assertEquals("张三", respVO.getEmployeeName());
        assertEquals("研发部", respVO.getDeptName());
        assertEquals("业绩", respVO.getQuotas().get(0).getDimensionName());
        assertEquals("李经理", respVO.getStages().get(0).getHandlerName());
        assertEquals(new BigDecimal("90"), respVO.getStages().get(0)
                .getQuotaScoreList().get(0).getScore());
        assertFalse(respVO.getStages().get(0).getCanHandle());
    }

    @Test
    public void testBuildProcessAssessmentRespVO_marksCurrentHandlerAndAppealStages() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType());
        HrmPerformanceAssessmentStageDO stage = buildStage(
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                HrmPerformanceReviewVisibleContentEnum.ALL.getContent());
        mockRelatedData(stage);
        HrmPerformanceAssessmentAppealRecordDO appealRecord =
                new HrmPerformanceAssessmentAppealRecordDO().setAssessmentId(ASSESSMENT_ID)
                        .setStageId(99L)
                        .setStatus(HrmPerformanceAppealRecordStatusEnum.NOT_PROCESSED.getStatus());
        when(assessmentService.getPerformanceAssessmentAppealRecordList(any(), any()))
                .thenReturn(Collections.singletonList(appealRecord));
        when(employeeService.getEmployeeByUserId(HANDLER_USER_ID)).thenReturn(
                HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).userId(HANDLER_USER_ID).build());

        // 调用
        HrmPerformanceAssessmentRespVO respVO =
                assessmentQueryService.getPerformanceAssessmentProcessRespVO(assessment, HANDLER_USER_ID);

        // 断言
        assertEquals(STAGE_ID, respVO.getCurrentStage().getId());
        assertTrue(respVO.getCurrentStage().getCanHandle());
        assertFalse(respVO.getCurrentStage().getCanScore());
        assertEquals(Collections.singletonList(99L), respVO.getAppealReviewStageIds());
    }

    @Test
    public void testBuildReviewTaskRespVO_hidesOtherReviewContent() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.SELF_SCORE.getType());
        assessment.setSelfComment("历史自评").setReviewerComment("历史他评");
        HrmPerformanceAssessmentStageDO stage = buildStage(
                HrmPerformanceStageTypeEnum.SELF_SCORE.getType(),
                HrmPerformanceReviewVisibleContentEnum.SELF.getContent());
        mockRelatedData(stage);
        when(employeeService.getEmployeeByUserId(HANDLER_USER_ID)).thenReturn(
                HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).userId(HANDLER_USER_ID).build());

        // 调用
        HrmPerformanceAssessmentRespVO respVO =
                assessmentQueryService.getPerformanceAssessmentTaskRespVO(
                        assessment, STAGE_ID, HANDLER_USER_ID);

        // 断言
        assertEquals(STAGE_ID, respVO.getCurrentReviewStage().getId());
        assertTrue(respVO.getCurrentReviewStage().getCanScore());
        assertEquals(1, respVO.getReviewStages().size());
        assertNull(respVO.getSelfComment());
        assertNull(respVO.getReviewerComment());
        assertNull(respVO.getQuotas().get(0).getSelfScore());
        assertNull(respVO.getQuotas().get(0).getReviewerScore());
        assertNull(respVO.getQuotas().get(0).getFinalScore());
    }

    @Test
    public void testBuildProcessAssessmentRespVO_completedSelfVisibility_hidesOtherReviewContent() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        assessment.setSelfComment("历史自评").setReviewerComment("历史他评");
        HrmPerformanceAssessmentStageDO stage = buildStage(
                HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                HrmPerformanceReviewVisibleContentEnum.SELF.getContent())
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        mockRelatedData(stage);
        when(employeeService.getEmployeeByUserId(HANDLER_USER_ID)).thenReturn(
                HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).userId(HANDLER_USER_ID).build());

        // 调用
        HrmPerformanceAssessmentRespVO respVO =
                assessmentQueryService.getPerformanceAssessmentProcessRespVO(assessment, HANDLER_USER_ID);

        // 断言
        assertEquals(1, respVO.getReviewStages().size());
        assertEquals(STAGE_ID, respVO.getReviewStages().get(0).getId());
        assertNull(respVO.getSelfComment());
        assertNull(respVO.getReviewerComment());
        assertNull(respVO.getQuotas().get(0).getReviewerScore());
        assertNull(respVO.getQuotas().get(0).getFinalScore());
    }

    @Test
    public void testGetProcessRecordList_mergeAndSort() {
        // mock 数据
        LocalDateTime beginTime = LocalDateTime.of(2026, 7, 1, 9, 0);
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        HrmPerformanceAssessmentActionRecordDO quotaAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.FILL_QUOTA, null,
                HrmPerformanceAssessmentActionTypeEnum.FILL_QUOTA.getTitle(),
                "填写并提交了绩效指标", beginTime);
        HrmPerformanceAssessmentActionRecordDO targetAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.CONFIRM_TARGET, HANDLER_EMPLOYEE_ID,
                "确认绩效目标", "确认了绩效目标，意见：目标合理", beginTime.plusDays(1));
        HrmPerformanceAssessmentActionRecordDO scoreAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.SCORE, HANDLER_EMPLOYEE_ID,
                "提交绩效评分", "填写并提交了评分，阶段得分：88", beginTime.plusDays(2));
        HrmPerformanceAssessmentActionRecordDO appealAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.SUBMIT_APPEAL, null,
                "提交绩效申诉", "提交了绩效申诉，原因：申请复核", beginTime.plusDays(3));
        HrmPerformanceAssessmentActionRecordDO terminateAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.TERMINATE, null,
                "终止绩效考核", "终止了绩效考核", beginTime.plusDays(5));
        when(assessmentActionRecordService.getPerformanceAssessmentActionRecordList(ASSESSMENT_ID))
                .thenReturn(Arrays.asList(terminateAction, scoreAction, quotaAction, appealAction, targetAction));
        when(employeeService.getEmployeeMap(any())).thenReturn(
                Collections.singletonMap(HANDLER_EMPLOYEE_ID,
                        HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).name("李经理").build()));

        // 调用
        List<HrmPerformanceProcessRecordRespVO> records =
                assessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment);

        // 断言
        assertEquals(5, records.size());
        assertEquals("提交绩效指标", records.get(0).getTitle());
        assertEquals("确认绩效目标", records.get(1).getTitle());
        assertEquals("提交绩效评分", records.get(2).getTitle());
        assertEquals("提交绩效申诉", records.get(3).getTitle());
        assertEquals("终止绩效考核", records.get(4).getTitle());
        assertEquals("ACTION", records.get(1).getSource());
        assertEquals("李经理", records.get(1).getOperatorName());
    }

    @Test
    public void testGetProcessRecordList_containsReviewRejectReason() {
        // mock 数据
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 1, 9, 0);
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        HrmPerformanceAssessmentActionRecordDO rejectAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.REJECT_SCORE, HANDLER_EMPLOYEE_ID,
                "驳回绩效评分", "驳回到【直属上级评分】，原因：评分依据不完整", createTime);
        when(assessmentActionRecordService.getPerformanceAssessmentActionRecordList(ASSESSMENT_ID))
                .thenReturn(Collections.singletonList(rejectAction));
        when(employeeService.getEmployeeMap(any())).thenReturn(
                Collections.singletonMap(HANDLER_EMPLOYEE_ID,
                        HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).name("李经理").build()));

        // 调用
        List<HrmPerformanceProcessRecordRespVO> records =
                assessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment);

        // 断言
        assertEquals(1, records.size());
        assertEquals("驳回绩效评分", records.get(0).getTitle());
        assertTrue(records.get(0).getContent().contains("原因：评分依据不完整"));
    }

    @Test
    public void testGetProcessRecordList_keepsRepeatedActionsAndFiles() {
        // mock 数据
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 1, 9, 0);
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessment.setCreateTime(createTime);
        HrmPerformanceAssessmentActionRecordDO scoreAction =
                new HrmPerformanceAssessmentActionRecordDO()
                        .setAssessmentId(ASSESSMENT_ID).setStageId(STAGE_ID)
                        .setEmployeeId(HANDLER_EMPLOYEE_ID)
                        .setType(HrmPerformanceAssessmentActionTypeEnum.SCORE.getType())
                        .setTitle("提交绩效评分").setContent("阶段得分：88")
                        .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        scoreAction.setCreateTime(createTime.plusDays(1));
        HrmPerformanceAssessmentActionRecordDO rejectAction =
                new HrmPerformanceAssessmentActionRecordDO()
                        .setAssessmentId(ASSESSMENT_ID).setStageId(STAGE_ID)
                        .setEmployeeId(HANDLER_EMPLOYEE_ID)
                        .setType(HrmPerformanceAssessmentActionTypeEnum.REJECT_SCORE.getType())
                        .setTitle("驳回绩效评分").setContent("评分依据不完整")
                        .setFileUrls(Collections.singletonList("https://example.com/evidence.pdf"))
                        .setStatus(HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus());
        rejectAction.setCreateTime(createTime.plusDays(2));
        when(assessmentActionRecordService.getPerformanceAssessmentActionRecordList(ASSESSMENT_ID))
                .thenReturn(Arrays.asList(scoreAction, rejectAction));
        when(employeeService.getEmployeeMap(any())).thenReturn(
                Collections.singletonMap(HANDLER_EMPLOYEE_ID,
                        HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).name("李经理").build()));

        // 调用
        List<HrmPerformanceProcessRecordRespVO> records =
                assessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment);

        // 断言
        assertEquals(2, records.size());
        assertEquals("提交绩效评分", records.get(0).getTitle());
        assertEquals("驳回绩效评分", records.get(1).getTitle());
        assertEquals("ACTION", records.get(0).getSource());
        assertEquals("李经理", records.get(1).getOperatorName());
        assertEquals(Collections.singletonList("https://example.com/evidence.pdf"),
                records.get(1).getFileUrls());
    }

    @Test
    public void testGetProcessRecordList_selfVisibility_hidesOtherReviewerScoreAction() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildAssessment(
                HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        HrmPerformanceAssessmentStageDO viewerStage = buildStage(
                HrmPerformanceStageTypeEnum.OTHER_SCORE.getType(),
                HrmPerformanceReviewVisibleContentEnum.SELF.getContent())
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        HrmPerformanceAssessmentActionRecordDO ownScoreAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.SCORE, HANDLER_EMPLOYEE_ID,
                "提交绩效评分", "阶段得分：88", LocalDateTime.now().minusDays(1));
        HrmPerformanceAssessmentActionRecordDO otherScoreAction = buildActionRecord(
                HrmPerformanceAssessmentActionTypeEnum.SCORE, 99L,
                "提交绩效评分", "阶段得分：96，评语：表现优秀", LocalDateTime.now());
        when(assessmentActionRecordService.getPerformanceAssessmentActionRecordList(ASSESSMENT_ID))
                .thenReturn(Arrays.asList(ownScoreAction, otherScoreAction));
        when(employeeService.getEmployeeByUserId(HANDLER_USER_ID)).thenReturn(
                HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).userId(HANDLER_USER_ID).build());
        when(assessmentService.getPerformanceAssessmentStageList(any()))
                .thenReturn(Collections.singletonList(viewerStage));
        when(employeeService.getEmployeeMap(any())).thenReturn(Collections.singletonMap(
                HANDLER_EMPLOYEE_ID,
                HrmEmployeeDO.builder().id(HANDLER_EMPLOYEE_ID).name("李经理").build()));

        // 调用
        List<HrmPerformanceProcessRecordRespVO> records = assessmentQueryService
                .getPerformanceAssessmentProcessRecordList(assessment, HANDLER_USER_ID);

        // 断言
        assertEquals(1, records.size());
        assertEquals("阶段得分：88", records.get(0).getContent());
        assertEquals("李经理", records.get(0).getOperatorName());
    }

    private HrmPerformanceAssessmentActionRecordDO buildActionRecord(
            HrmPerformanceAssessmentActionTypeEnum type, Long employeeId,
            String title, String content, LocalDateTime createTime) {
        HrmPerformanceAssessmentActionRecordDO actionRecord = new HrmPerformanceAssessmentActionRecordDO()
                .setAssessmentId(ASSESSMENT_ID).setEmployeeId(employeeId)
                .setType(type.getType()).setTitle(title).setContent(content);
        actionRecord.setCreateTime(createTime);
        return actionRecord;
    }

    private void mockRelatedData(HrmPerformanceAssessmentStageDO stage) {
        HrmPerformancePlanDO plan = HrmPerformancePlanDO.builder().id(PLAN_ID).name("季度绩效").build();
        when(planService.getPerformancePlanMap(any())).thenReturn(Collections.singletonMap(PLAN_ID, plan));
        HrmPerformanceAssessmentDimensionDO dimension = new HrmPerformanceAssessmentDimensionDO()
                .setId(DIMENSION_ID).setAssessmentId(ASSESSMENT_ID).setName("业绩")
                .setWeight(new BigDecimal("100")).setAllowEdit(false);
        when(assessmentService.getPerformanceAssessmentDimensionList(any()))
                .thenReturn(Collections.singletonList(dimension));
        HrmPerformanceAssessmentQuotaDO quota = HrmPerformanceAssessmentQuotaDO.builder()
                .id(QUOTA_ID).assessmentId(ASSESSMENT_ID).dimensionId(DIMENSION_ID)
                .name("目标达成率").selfScore(new BigDecimal("80"))
                .reviewerScore(new BigDecimal("90")).finalScore(new BigDecimal("86")).build();
        when(assessmentService.getPerformanceAssessmentQuotaList(any()))
                .thenReturn(Collections.singletonList(quota));
        when(assessmentService.getPerformanceAssessmentStageList(any()))
                .thenReturn(Collections.singletonList(stage));
        HrmPerformanceAssessmentQuotaScoreDO quotaScore =
                new HrmPerformanceAssessmentQuotaScoreDO().setAssessmentStageId(STAGE_ID)
                        .setAssessmentQuotaId(QUOTA_ID).setScore(new BigDecimal("90"));
        when(assessmentService.getPerformanceAssessmentQuotaScoreList(any()))
                .thenReturn(Collections.singletonList(quotaScore));
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(EMPLOYEE_ID, HrmEmployeeDO.builder()
                .id(EMPLOYEE_ID).name("张三").deptId(10L).build());
        HrmEmployeeDO handler = HrmEmployeeDO.builder()
                .id(HANDLER_EMPLOYEE_ID).name("李经理").userId(HANDLER_USER_ID).build();
        employeeMap.put(HANDLER_EMPLOYEE_ID, handler);
        when(employeeService.getEmployeeMap(any())).thenReturn(employeeMap);
        DeptRespDTO dept = new DeptRespDTO();
        dept.setId(10L);
        dept.setName("研发部");
        when(deptApi.getDeptMap(any())).thenReturn(Collections.singletonMap(10L, dept));
    }

    private HrmPerformanceAssessmentDO buildAssessment(Integer stageType) {
        return HrmPerformanceAssessmentDO.builder().id(ASSESSMENT_ID).planId(PLAN_ID)
                .employeeId(EMPLOYEE_ID).stageType(stageType).build();
    }

    private HrmPerformanceAssessmentStageDO buildStage(Integer type, Integer visibleContent) {
        return new HrmPerformanceAssessmentStageDO().setId(STAGE_ID).setAssessmentId(ASSESSMENT_ID)
                .setType(type).setName("处理节点").setHandlerEmployeeId(HANDLER_EMPLOYEE_ID)
                .setVisibleContent(visibleContent)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
    }

}

package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentQuotaScoreRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceQuotaSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceReviewRejectReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScorePreviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScoreReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ResultConfig;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentDimensionMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaScoreMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentStageMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.plan.HrmPerformancePlanMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.config.HrmPerformanceResultTemplateMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaScoreTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceUpperLimitTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_QUOTA_ACTUAL_VALUE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_QUOTA_TARGET_VALUE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_LEVEL_NOT_MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformanceAssessmentReviewServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceAssessmentReviewServiceImpl.class)
public class HrmPerformanceAssessmentReviewServiceImplTest extends BaseDbUnitTest {

    private static final Long QUOTA_TEMPLATE_ID = 100L;
    private static final Long QUOTA_ASSESSMENT_ID = 300L;

    @Resource
    private HrmPerformanceAssessmentReviewServiceImpl reviewService;

    @Resource
    private HrmPerformancePlanMapper planMapper;
    @Resource
    private HrmPerformanceResultTemplateMapper resultTemplateMapper;
    @Resource
    private HrmPerformanceAssessmentMapper assessmentMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaMapper assessmentQuotaMapper;
    @Resource
    private HrmPerformanceAssessmentDimensionMapper assessmentDimensionMapper;
    @Resource
    private HrmPerformanceAssessmentStageMapper assessmentStageMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaScoreMapper assessmentQuotaScoreMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private HrmPerformanceAssessmentProcessService processService;
    @MockitoBean
    private HrmPerformancePlanService planService;
    @MockitoBean
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Test
    public void testGetMyFillQuotaTaskPage_success() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        HrmPerformanceAssessmentStageDO fillStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId()).setName("员工填写")
                .setType(HrmPerformanceStageTypeEnum.FILL_QUOTA.getType())
                .setHandlerEmployeeId(200L)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(0);
        assessmentStageMapper.insert(fillStage);

        // 准备参数
        HrmPortalPerformanceTaskPageReqVO reqVO = buildPendingTaskPageReqVO();

        // 调用
        PageResult<HrmPerformanceAssessmentStageDO> result =
                reviewService.getMyFillQuotaTaskPage(1001L, reqVO);

        // 断言
        assertEquals(1, result.getList().size());
        assertEquals(fillStage.getId(), result.getList().get(0).getId());
    }

    @Test
    public void testGetMyReviewTaskPage_success() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        when(planService.getPerformancePlanList(any()))
                .thenReturn(Collections.singletonList(fixture.plan));

        // 准备参数
        HrmPortalPerformanceTaskPageReqVO reqVO = buildPendingTaskPageReqVO();

        // 调用
        PageResult<HrmPerformanceAssessmentStageDO> result =
                reviewService.getMyReviewTaskPage(1001L, reqVO);

        // 断言
        assertEquals(1, result.getList().size());
        assertEquals(fixture.reviewStage.getId(), result.getList().get(0).getId());
    }

    @Test
    public void testGetMyReviewTaskPage_accountRebound_success() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        when(employeeService.getEmployeeByUserId(2002L)).thenReturn(
                HrmEmployeeDO.builder().id(200L).userId(2002L).build());
        when(planService.getPerformancePlanList(any()))
                .thenReturn(Collections.singletonList(fixture.plan));

        // 准备参数
        HrmPortalPerformanceTaskPageReqVO reqVO = buildPendingTaskPageReqVO();

        // 调用
        PageResult<HrmPerformanceAssessmentStageDO> result =
                reviewService.getMyReviewTaskPage(2002L, reqVO);

        // 断言
        assertEquals(1, result.getList().size());
        assertEquals(fixture.reviewStage.getId(), result.getList().get(0).getId());
    }

    @Test
    public void testPreviewScore_readOnly_success() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        quotaReqVO.setId(fixture.quota.getId());
        quotaReqVO.setSelfScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO();
        reqVO.setAssessmentId(fixture.assessment.getId());
        reqVO.setReviewStageId(fixture.reviewStage.getId());
        reqVO.setQuotas(Collections.singletonList(quotaReqVO));

        // 调用
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);

        // 断言
        assertEquals(new BigDecimal("80.00"), preview.getStageScore());
        assertEquals("B", preview.getStageResultLevel());
        assertEquals(new BigDecimal("80.00"), preview.getCumulativeScore());
        assertEquals("B", preview.getCumulativeResultLevel());
        assertTrue(assessmentQuotaScoreMapper.selectListByAssessmentStageIds(
                Collections.singleton(fixture.reviewStage.getId())).isEmpty());
        HrmPerformanceAssessmentQuotaDO quota = assessmentQuotaMapper.selectById(fixture.quota.getId());
        assertEquals(new BigDecimal("0.00"), quota.getSelfScore());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
    }

    @Test
    public void testPreviewScore_otherReviewer() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO();
        reqVO.setAssessmentId(fixture.assessment.getId());
        reqVO.setReviewStageId(fixture.reviewStage.getId());

        // 调用，并断言异常
        assertServiceException(() -> reviewService.previewScore(2002L, reqVO),
                PERFORMANCE_REVIEW_STAGE_NO_PERMISSION);
    }

    @Test
    public void testPreviewScore_resultTemplateChanged_usePlanSnapshot() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        fixture.resultTemplate.setLevels(Collections.singletonList(
                Level.builder().name("S").minScore(BigDecimal.ZERO)
                        .maxScore(new BigDecimal("100")).coefficient(new BigDecimal("2")).build()));
        resultTemplateMapper.updateById(fixture.resultTemplate);
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        quotaReqVO.setId(fixture.quota.getId());
        quotaReqVO.setSelfScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO();
        reqVO.setAssessmentId(fixture.assessment.getId());
        reqVO.setReviewStageId(fixture.reviewStage.getId());
        reqVO.setQuotas(Collections.singletonList(quotaReqVO));

        // 调用
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);

        // 断言
        assertEquals("B", preview.getStageResultLevel());
        assertEquals("B", preview.getCumulativeResultLevel());
    }

    @Test
    public void testPreviewScore_multipleStages_hidesCumulativeLevel() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        fixture.reviewStage.setWeight(new BigDecimal("50"));
        assessmentStageMapper.updateById(fixture.reviewStage);
        assessmentStageMapper.insert(new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId()).setName("直属上级评分")
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType())
                .setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType()).setHandlerEmployeeId(201L)
                .setWeight(new BigDecimal("50"))
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()).setSort(2));
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(fixture.quota.getId()).setSelfScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO()
                .setAssessmentId(fixture.assessment.getId()).setReviewStageId(fixture.reviewStage.getId())
                .setQuotas(Collections.singletonList(quotaReqVO));

        // 调用
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);

        // 断言
        assertEquals(new BigDecimal("40.00"), preview.getCumulativeScore());
        assertNull(preview.getCumulativeResultLevel());
    }

    @Test
    public void testScoreAssessment_resultLevelNotMatch() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        fixture.plan.setResultConfig(ResultConfig.builder().name("不完整结果模板")
                .levels(Collections.singletonList(Level.builder().name("A")
                        .minScore(new BigDecimal("90")).maxScore(new BigDecimal("100"))
                        .coefficient(BigDecimal.ONE).build())).build());
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(fixture.quota.getId()).setSelfScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO()
                .setAssessmentId(fixture.assessment.getId()).setReviewStageId(fixture.reviewStage.getId())
                .setQuotas(Collections.singletonList(quotaReqVO));

        // 调用，并断言未命中等级时不保存空结果
        assertServiceException(() -> reviewService.scoreAssessment(1001L, reqVO),
                PERFORMANCE_RESULT_LEVEL_NOT_MATCH);
    }

    @Test
    public void testScoreAssessment_multipleReviewer_refreshPartialScore() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        fixture.assessment.setStageType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.updateById(fixture.assessment);
        fixture.quota.setSelfScore(new BigDecimal("99")).setReviewerScore(new BigDecimal("60"));
        assessmentQuotaMapper.updateById(fixture.quota);
        fixture.reviewStage.setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType())
                .setName("上级评分").setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType())
                .setHandlerEmployeeId(201L).setWeight(new BigDecimal("20"))
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()).setSort(1);
        assessmentStageMapper.updateById(fixture.reviewStage);
        assessmentQuotaScoreMapper.insert(new HrmPerformanceAssessmentQuotaScoreDO()
                .setAssessmentStageId(fixture.reviewStage.getId())
                .setAssessmentQuotaId(fixture.quota.getId()).setScore(new BigDecimal("60")));
        HrmPerformanceAssessmentStageDO currentStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("部门负责人评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.DEPT_LEADER.getType())
                .setHandlerEmployeeId(200L).setWeight(new BigDecimal("30"))
                .setRequiredSetting(false).setRejectAuthority(false)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(2);
        assessmentStageMapper.insert(currentStage);
        HrmPerformanceAssessmentStageDO nextStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("指定评分人评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.SPECIFIED.getType())
                .setHandlerEmployeeId(202L).setWeight(new BigDecimal("50"))
                .setRequiredSetting(false).setRejectAuthority(false)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()).setSort(3);
        assessmentStageMapper.insert(nextStage);
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        quotaReqVO.setId(fixture.quota.getId());
        quotaReqVO.setReviewerScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO();
        reqVO.setAssessmentId(fixture.assessment.getId());
        reqVO.setReviewStageId(currentStage.getId());
        reqVO.setQuotas(Collections.singletonList(quotaReqVO));

        // 调用
        HrmPortalPerformanceProcessRespVO result = reviewService.scoreAssessment(1001L, reqVO);

        // 断言
        assertEquals(fixture.assessment.getId(), result.getId());
        HrmPerformanceAssessmentQuotaDO quota = assessmentQuotaMapper.selectById(fixture.quota.getId());
        assertNull(quota.getSelfScore());
        assertEquals(new BigDecimal("72.00"), quota.getReviewerScore());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(nextStage.getId()).getStatus());
    }

    @Test
    public void testScoreAssessment_multiplePendingStage_scoreInParallel() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        HrmPerformanceAssessmentStageDO otherReviewStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("直属上级评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType())
                .setHandlerEmployeeId(200L).setWeight(new BigDecimal("50"))
                .setRequiredSetting(false).setRejectAuthority(false)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(2);
        assessmentStageMapper.insert(otherReviewStage);
        HrmPortalPerformanceQuotaSaveReqVO quotaReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        quotaReqVO.setId(fixture.quota.getId());
        quotaReqVO.setReviewerScore(new BigDecimal("80"));
        HrmPortalPerformanceScoreReqVO reqVO = new HrmPortalPerformanceScoreReqVO();
        reqVO.setAssessmentId(fixture.assessment.getId());
        reqVO.setReviewStageId(otherReviewStage.getId());
        reqVO.setQuotas(Collections.singletonList(quotaReqVO));

        // 调用
        reviewService.scoreAssessment(1001L, reqVO);

        // 断言
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(otherReviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceStageTypeEnum.SELF_SCORE.getType(),
                assessmentMapper.selectById(fixture.assessment.getId()).getStageType());
    }

    @Test
    public void testEnsureAssessmentQuotaList_success() {
        // 准备参数
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("100"));
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();

        // 调用
        reviewService.ensureAssessmentQuotaList(plan, assessment);

        // 断言
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        HrmPerformanceAssessmentDimensionDO dimension =
                assessmentDimensionMapper.selectListByAssessmentId(assessment.getId()).get(0);
        assertEquals(1, quotas.size());
        HrmPerformanceAssessmentQuotaDO quota = quotas.get(0);
        assertEquals(dimension.getId(), quota.getDimensionId());
        assertTrue(quota.getPreset());
        assertEquals("目标达成率", quota.getName());
        assertEquals(new BigDecimal("100.00"), quota.getWeight());
        assertEquals(new BigDecimal("0.00"), quota.getSelfScore());
        assertEquals(new BigDecimal("0.00"), quota.getReviewerScore());
        assertEquals(new BigDecimal("0.00"), quota.getFinalScore());
    }

    @Test
    public void testEnsureAssessmentQuotaList_existingQuota() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();
        HrmPerformanceAssessmentQuotaDO existingQuota = HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(assessment.getId()).dimensionId(1L).preset(true)
                .name("存量指标").weight(new BigDecimal("100")).build();
        assessmentQuotaMapper.insert(existingQuota);
        // 准备参数
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("100"));

        // 调用
        reviewService.ensureAssessmentQuotaList(plan, assessment);

        // 断言
        assertEquals(1L, assessmentQuotaMapper.selectCount());
        assertEquals("存量指标", assessmentQuotaMapper.selectById(existingQuota.getId()).getName());
    }

    @Test
    public void testReplaceAssessmentQuotaList_success() {
        // mock 数据
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("60"));
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();
        reviewService.ensureAssessmentQuotaList(plan, assessment);
        HrmPerformanceAssessmentQuotaDO presetQuota =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()).get(0);
        Long dimensionId = presetQuota.getDimensionId();
        assessmentQuotaMapper.insert(HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(assessment.getId()).dimensionId(dimensionId).preset(false)
                .name("旧自定义指标").standard("旧标准").weight(new BigDecimal("40"))
                .scoreType(1).sort(2).build());
        // 准备参数
        HrmPortalPerformanceQuotaSaveReqVO presetReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        presetReqVO.setId(presetQuota.getId());
        HrmPortalPerformanceQuotaSaveReqVO customReqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        customReqVO.setDimensionId(dimensionId);
        customReqVO.setName(" 新自定义指标 ");
        customReqVO.setStandard(" 新标准 ");
        customReqVO.setWeight(new BigDecimal("40"));
        customReqVO.setScoreType(1);

        // 调用
        reviewService.replaceAssessmentQuotaList(plan, assessment,
                Arrays.asList(presetReqVO, customReqVO));

        // 断言
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        assertEquals(2, quotas.size());
        HrmPerformanceAssessmentQuotaDO customQuota = quotas.stream()
                .filter(quota -> !quota.getPreset()).findFirst().orElse(null);
        assertNotNull(customQuota);
        assertEquals(dimensionId, customQuota.getDimensionId());
        assertEquals("新自定义指标", customQuota.getName());
        assertEquals("新标准", customQuota.getStandard());
        assertEquals(new BigDecimal("40.00"), customQuota.getWeight());
    }

    @Test
    public void testUpdateAssessmentQuotaList_incompleteIds() {
        // mock 数据
        HrmPerformanceAssessmentQuotaDO firstQuota = HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(QUOTA_ASSESSMENT_ID).dimensionId(1L)
                .preset(true).name("指标一").build();
        assessmentQuotaMapper.insert(firstQuota);
        assessmentQuotaMapper.insert(HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(QUOTA_ASSESSMENT_ID).dimensionId(1L)
                .preset(true).name("指标二").build());
        // 准备参数
        HrmPortalPerformanceQuotaSaveReqVO reqVO = new HrmPortalPerformanceQuotaSaveReqVO();
        reqVO.setId(firstQuota.getId());

        // 调用，并断言异常
        assertServiceException(() -> reviewService.updateAssessmentQuotaList(
                QUOTA_ASSESSMENT_ID, Collections.singletonList(reqVO)), PERFORMANCE_DATA_ILLEGAL);
    }

    // ==================== 完成率指标 ====================

    @Test
    public void testPreviewScore_mixedQuota_completionRateCalculatedByServer() {
        // mock 数据：手工指标 50% + 完成率指标 50%
        PreviewFixture fixture = createPreviewFixture();
        fixture.quota.setWeight(new BigDecimal("50"));
        assessmentQuotaMapper.updateById(fixture.quota);
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "销售额完成率", new BigDecimal("50"), "200", null);
        // 准备参数：完成率指标传入的分数不被采用
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, fixture.reviewStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setSelfScore(new BigDecimal("80")),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId()).setActualValue("150")
                        .setSelfScore(new BigDecimal("100")).setFinalScore(new BigDecimal("100")));

        // 调用
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);

        // 断言：完成率得分 = 150 / 200 × 100 = 75；阶段得分 = 80 × 50% + 75 × 50%
        assertEquals(new BigDecimal("77.50"), preview.getStageScore());
        assertEquals(new BigDecimal("75.00"), findQuotaScore(preview, rateQuota.getId()));
        assertEquals(new BigDecimal("80"), findQuotaScore(preview, fixture.quota.getId()));
        // 试算不保存实际值
        assertNull(assessmentQuotaMapper.selectById(rateQuota.getId()).getActualValue());
    }

    @Test
    public void testScoreAssessment_completionRate_entryStageSaveActualValue() {
        // mock 数据：统一评分上限 90，作为完成率满分
        PreviewFixture fixture = createPreviewFixture();
        fixture.plan.getAssessmentConfig().setUpperLimitScore(new BigDecimal("90"));
        fixture.quota.setWeight(new BigDecimal("40"));
        assessmentQuotaMapper.updateById(fixture.quota);
        HrmPerformanceAssessmentQuotaDO partialQuota = insertCompletionRateQuota(
                fixture, "回款完成率", new BigDecimal("30"), "3", null);
        HrmPerformanceAssessmentQuotaDO exceededQuota = insertCompletionRateQuota(
                fixture, "新客完成率", new BigDecimal("30"), "3", null);
        // 准备参数
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, fixture.reviewStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setSelfScore(new BigDecimal("90")),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(partialQuota.getId()).setActualValue(" 1 "),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(exceededQuota.getId()).setActualValue("5"));

        // 调用
        reviewService.scoreAssessment(1001L, reqVO);

        // 断言：1 / 3 × 90 = 30.00；超额完成按上限 90 封顶
        List<HrmPerformanceAssessmentQuotaScoreDO> scores = assessmentQuotaScoreMapper
                .selectListByAssessmentStageIds(Collections.singleton(fixture.reviewStage.getId()));
        assertEquals(new BigDecimal("30.00"), findQuotaScore(scores, partialQuota.getId()));
        assertEquals(new BigDecimal("90.00"), findQuotaScore(scores, exceededQuota.getId()));
        // 阶段得分 = 90 × 40% + 30 × 30% + 90 × 30%
        assertEquals(new BigDecimal("72.00"),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getScore());
        assertEquals("1", assessmentQuotaMapper.selectById(partialQuota.getId()).getActualValue());
        assertEquals("5", assessmentQuotaMapper.selectById(exceededQuota.getId()).getActualValue());
    }

    @Test
    public void testScoreAssessment_completionRate_actualValueInvalid() {
        // mock 数据
        PreviewFixture fixture = createPreviewFixture();
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "销售额完成率", new BigDecimal("50"), "100", null);
        for (String actualValue : Arrays.asList(null, "", "-1", "95%", "1e2", "约 100")) {
            // 准备参数
            HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, fixture.reviewStage,
                    new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                            .setSelfScore(new BigDecimal("80")),
                    new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId())
                            .setActualValue(actualValue).setSelfScore(new BigDecimal("100")));

            // 调用，并断言异常
            assertServiceException(() -> reviewService.scoreAssessment(1001L, reqVO),
                    PERFORMANCE_QUOTA_ACTUAL_VALUE_INVALID, "销售额完成率");
        }
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
    }

    @Test
    public void testPreviewScore_completionRate_targetValueInvalid() {
        // mock 数据：目标值不合法时无法计算
        PreviewFixture fixture = createPreviewFixture();
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "销售额完成率", new BigDecimal("50"), "0", null);
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, fixture.reviewStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setSelfScore(new BigDecimal("80")),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId()).setActualValue("10"));

        // 调用，并断言异常
        assertServiceException(() -> reviewService.previewScore(1001L, reqVO),
                PERFORMANCE_QUOTA_TARGET_VALUE_INVALID, "销售额完成率");
    }

    @Test
    public void testScoreAssessment_completionRate_laterStageUseFrozenActualValue() {
        // mock 数据：首个评分阶段已录入实际值 100
        PreviewFixture fixture = createPreviewFixture();
        fixture.quota.setWeight(new BigDecimal("50"));
        assessmentQuotaMapper.updateById(fixture.quota);
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "销售额完成率", new BigDecimal("50"), "200", "100");
        fixture.reviewStage.setWeight(new BigDecimal("50"))
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.updateById(fixture.reviewStage);
        assessmentQuotaScoreMapper.insertBatch(Arrays.asList(
                new HrmPerformanceAssessmentQuotaScoreDO().setAssessmentStageId(fixture.reviewStage.getId())
                        .setAssessmentQuotaId(fixture.quota.getId()).setScore(new BigDecimal("80")),
                new HrmPerformanceAssessmentQuotaScoreDO().setAssessmentStageId(fixture.reviewStage.getId())
                        .setAssessmentQuotaId(rateQuota.getId()).setScore(new BigDecimal("50"))));
        fixture.assessment.setStageType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.updateById(fixture.assessment);
        HrmPerformanceAssessmentStageDO currentStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("上级评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType())
                .setHandlerEmployeeId(200L).setWeight(new BigDecimal("50"))
                .setRequiredSetting(false).setRejectAuthority(false)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(2);
        assessmentStageMapper.insert(currentStage);
        // 准备参数：后续阶段尝试改写实际值和完成率分数
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, currentStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setReviewerScore(new BigDecimal("70")),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId()).setActualValue("200")
                        .setReviewerScore(new BigDecimal("100")));

        // 调用：试算与提交均使用冻结的实际值
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);
        reviewService.scoreAssessment(1001L, reqVO);

        // 断言
        assertEquals(new BigDecimal("50.00"), findQuotaScore(preview, rateQuota.getId()));
        assertEquals(new BigDecimal("60.00"), preview.getStageScore());
        List<HrmPerformanceAssessmentQuotaScoreDO> scores = assessmentQuotaScoreMapper
                .selectListByAssessmentStageIds(Collections.singleton(currentStage.getId()));
        assertEquals(new BigDecimal("50.00"), findQuotaScore(scores, rateQuota.getId()));
        HrmPerformanceAssessmentQuotaDO quota = assessmentQuotaMapper.selectById(rateQuota.getId());
        assertEquals("100", quota.getActualValue());
        assertEquals(new BigDecimal("50.00"), quota.getFinalScore());
    }

    @Test
    public void testPreviewScore_lastStage_cumulativeScoreMatchesFinish() {
        // 调用：最后一个评分阶段，手工指标 80.01；指标汇总 80.005 需先保留两位小数再计算总分
        assertLastStageCumulativeScoreMatchesFinish(new BigDecimal("80.01"));
    }

    @Test
    public void testPreviewScore_lastStage_manualScoreMoreThanTwoDecimals() {
        // 调用：手工指标 80.009 按保存精度舍入为 80.01，试算与完成评分后保存的结果一致
        BigDecimal manualScore = assertLastStageCumulativeScoreMatchesFinish(new BigDecimal("80.009"));

        // 断言：保存的手工指标得分
        assertEquals(new BigDecimal("80.01"), manualScore);
    }

    @Test
    public void testScoreAssessment_sameSortQueuedStage_activateAndReject() {
        // mock 数据：全部重评后排队的阶段与首个阶段 sort 相同、编号更大
        PreviewFixture fixture = createPreviewFixture();
        fixture.quota.setWeight(new BigDecimal("50"));
        assessmentQuotaMapper.updateById(fixture.quota);
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "回款完成率", new BigDecimal("50"), "100", "20");
        fixture.reviewStage.setWeight(new BigDecimal("50"));
        assessmentStageMapper.updateById(fixture.reviewStage);
        HrmPerformanceAssessmentStageDO queuedStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("上级评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType())
                .setHandlerEmployeeId(200L).setWeight(new BigDecimal("50"))
                .setRequiredSetting(false).setRejectAuthority(true)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus())
                .setSort(fixture.reviewStage.getSort());
        assessmentStageMapper.insert(queuedStage);
        // 准备参数：首个阶段评分并录入实际值
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, fixture.reviewStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setSelfScore(new BigDecimal("80")),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId()).setActualValue("80"));

        // 调用
        reviewService.scoreAssessment(1001L, reqVO);

        // 断言：排队阶段被激活，实际值已在首个阶段保存
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(queuedStage.getId()).getStatus());
        assertEquals("80", assessmentQuotaMapper.selectById(rateQuota.getId()).getActualValue());

        // 调用：排队阶段驳回，退回 sort 相同、编号更小的已完成阶段
        HrmPortalPerformanceReviewRejectReqVO rejectReqVO = new HrmPortalPerformanceReviewRejectReqVO();
        rejectReqVO.setAssessmentId(fixture.assessment.getId());
        rejectReqVO.setReviewStageId(queuedStage.getId());
        rejectReqVO.setReason("实际值需要修正");
        reviewService.rejectReviewStage(1001L, rejectReqVO);

        // 断言：首个阶段重新待处理，排队阶段回到未处理
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(),
                assessmentStageMapper.selectById(fixture.reviewStage.getId()).getStatus());
        assertEquals(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus(),
                assessmentStageMapper.selectById(queuedStage.getId()).getStatus());
    }

    @Test
    public void testReplaceAssessmentQuotaList_lockedDimensionIgnorePresetTargetValue() {
        // mock 数据：不允许编辑的维度中，预置完成率指标使用模板默认目标值
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("100"));
        HrmPerformanceAssessmentTemplateDO.Dimension dimension = plan.getAssessmentConfig().getDimensions().get(0);
        dimension.setAllowEdit(false).getQuotas().get(0)
                .setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType()).setTargetValue("100");
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();
        reviewService.ensureAssessmentQuotaList(plan, assessment);
        HrmPerformanceAssessmentQuotaDO presetQuota =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()).get(0);
        // 准备参数：尝试改写目标值
        HrmPortalPerformanceQuotaSaveReqVO presetReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(presetQuota.getId()).setTargetValue("120");

        // 调用
        reviewService.replaceAssessmentQuotaList(plan, assessment, Collections.singletonList(presetReqVO));

        // 断言：目标值保持模板默认值
        assertEquals("100", assessmentQuotaMapper.selectById(presetQuota.getId()).getTargetValue());
    }

    @Test
    public void testEnsureAssessmentQuotaList_copyTemplateTargetValue() {
        // 准备参数
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("100"));
        plan.getAssessmentConfig().getDimensions().get(0).getQuotas().get(0)
                .setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType()).setTargetValue("100");
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();

        // 调用
        reviewService.ensureAssessmentQuotaList(plan, assessment);

        // 断言
        HrmPerformanceAssessmentQuotaDO quota = assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()).get(0);
        assertEquals(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType(), quota.getScoreType());
        assertEquals("100", quota.getTargetValue());
    }

    @Test
    public void testReplaceAssessmentQuotaList_presetTargetValueAndCompletionRate() {
        // mock 数据：预置完成率指标带模板默认目标值
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("60"));
        plan.getAssessmentConfig().getDimensions().get(0).getQuotas().get(0)
                .setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType()).setTargetValue("100");
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();
        reviewService.ensureAssessmentQuotaList(plan, assessment);
        HrmPerformanceAssessmentQuotaDO presetQuota =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()).get(0);
        // 准备参数：预置指标只补目标值，尝试修改的其他字段不生效
        HrmPortalPerformanceQuotaSaveReqVO presetReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(presetQuota.getId()).setTargetValue(" 120 ").setName("篡改名称")
                .setWeight(new BigDecimal("10")).setScoreType(1);
        HrmPortalPerformanceQuotaSaveReqVO customReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setDimensionId(presetQuota.getDimensionId()).setName("回款完成率").setStandard("按回款计算")
                .setWeight(new BigDecimal("40")).setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType())
                .setTargetValue("50").setActualValue("30");

        // 调用
        reviewService.replaceAssessmentQuotaList(plan, assessment, Arrays.asList(presetReqVO, customReqVO));

        // 断言
        HrmPerformanceAssessmentQuotaDO updatedPreset = assessmentQuotaMapper.selectById(presetQuota.getId());
        assertEquals("120", updatedPreset.getTargetValue());
        assertEquals("目标达成率", updatedPreset.getName());
        assertEquals(new BigDecimal("60.00"), updatedPreset.getWeight());
        assertEquals(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType(), updatedPreset.getScoreType());
        HrmPerformanceAssessmentQuotaDO customQuota = CollUtil.findOne(
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()), quota -> !quota.getPreset());
        assertEquals("50", customQuota.getTargetValue());
        // 完成率指标的实际值只在首个评分阶段录入
        assertNull(customQuota.getActualValue());
    }

    @Test
    public void testReplaceAssessmentQuotaList_completionRateTargetValueInvalid() {
        // mock 数据
        HrmPerformancePlanDO plan = buildQuotaPlan(new BigDecimal("60"));
        plan.getAssessmentConfig().getDimensions().get(0).getQuotas().get(0)
                .setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType()).setTargetValue("100");
        HrmPerformanceAssessmentDO assessment = buildQuotaAssessment();
        reviewService.ensureAssessmentQuotaList(plan, assessment);
        HrmPerformanceAssessmentQuotaDO presetQuota =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId()).get(0);
        HrmPortalPerformanceQuotaSaveReqVO customReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setDimensionId(presetQuota.getDimensionId()).setName("回款完成率").setStandard("按回款计算")
                .setWeight(new BigDecimal("40")).setScoreType(1);

        // 调用，并断言：清空预置完成率指标的目标值
        HrmPortalPerformanceQuotaSaveReqVO clearedPresetReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(presetQuota.getId()).setTargetValue(" ");
        assertServiceException(() -> reviewService.replaceAssessmentQuotaList(plan, assessment,
                Arrays.asList(clearedPresetReqVO, customReqVO)), PERFORMANCE_QUOTA_TARGET_VALUE_INVALID, "目标达成率");
        // 调用，并断言：自定义完成率指标的目标值不是数字
        HrmPortalPerformanceQuotaSaveReqVO presetReqVO = new HrmPortalPerformanceQuotaSaveReqVO()
                .setId(presetQuota.getId()).setTargetValue("100");
        customReqVO.setScoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType()).setTargetValue("五十");
        assertServiceException(() -> reviewService.replaceAssessmentQuotaList(plan, assessment,
                Arrays.asList(presetReqVO, customReqVO)), PERFORMANCE_QUOTA_TARGET_VALUE_INVALID, "回款完成率");
        // 调用，并断言：未知评分类型
        customReqVO.setScoreType(99).setTargetValue("50");
        assertServiceException(() -> reviewService.replaceAssessmentQuotaList(plan, assessment,
                Arrays.asList(presetReqVO, customReqVO)), PERFORMANCE_DATA_ILLEGAL);
        assertEquals("100", assessmentQuotaMapper.selectById(presetQuota.getId()).getTargetValue());
    }

    /**
     * 两个 50% 评分阶段、两个 50% 指标：首个阶段已完成（手工 80、完成率 80），在最后一个阶段提交手工评分，
     * 断言试算的累计分与等级和完成评分后保存的结果一致，均为 80.01
     *
     * @param currentManualScore 最后一个阶段的手工评分
     * @return 最后一个阶段保存的手工指标得分
     */
    private BigDecimal assertLastStageCumulativeScoreMatchesFinish(
            BigDecimal currentManualScore) {
        // mock 数据：首个评分阶段已完成，手工指标 80、完成率指标 80（实际值 160 / 目标值 200）
        PreviewFixture fixture = createPreviewFixture();
        fixture.quota.setWeight(new BigDecimal("50"));
        assessmentQuotaMapper.updateById(fixture.quota);
        HrmPerformanceAssessmentQuotaDO rateQuota = insertCompletionRateQuota(
                fixture, "销售额完成率", new BigDecimal("50"), "200", "160");
        fixture.reviewStage.setWeight(new BigDecimal("50"))
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.updateById(fixture.reviewStage);
        assessmentQuotaScoreMapper.insertBatch(Arrays.asList(
                new HrmPerformanceAssessmentQuotaScoreDO().setAssessmentStageId(fixture.reviewStage.getId())
                        .setAssessmentQuotaId(fixture.quota.getId()).setScore(new BigDecimal("80")),
                new HrmPerformanceAssessmentQuotaScoreDO().setAssessmentStageId(fixture.reviewStage.getId())
                        .setAssessmentQuotaId(rateQuota.getId()).setScore(new BigDecimal("80"))));
        fixture.assessment.setStageType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType());
        assessmentMapper.updateById(fixture.assessment);
        HrmPerformanceAssessmentStageDO currentStage = new HrmPerformanceAssessmentStageDO()
                .setAssessmentId(fixture.assessment.getId())
                .setType(HrmPerformanceStageTypeEnum.OTHER_SCORE.getType()).setName("上级评分")
                .setRaterType(HrmPerformanceRaterTypeEnum.SUPERIOR.getType())
                .setHandlerEmployeeId(200L).setWeight(new BigDecimal("50"))
                .setRequiredSetting(false).setRejectAuthority(false)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(2);
        assessmentStageMapper.insert(currentStage);
        // 准备参数
        HrmPortalPerformanceScoreReqVO reqVO = buildScoreReqVO(fixture, currentStage,
                new HrmPortalPerformanceQuotaSaveReqVO().setId(fixture.quota.getId())
                        .setReviewerScore(currentManualScore),
                new HrmPortalPerformanceQuotaSaveReqVO().setId(rateQuota.getId()));

        // 调用
        HrmPortalPerformanceScorePreviewRespVO preview = reviewService.previewScore(1001L, reqVO);
        reviewService.scoreAssessment(1001L, reqVO);

        // 断言：试算的累计分和等级与完成评分后保存的结果一致
        HrmPerformanceAssessmentDO persisted = assessmentMapper.selectById(fixture.assessment.getId());
        assertEquals(new BigDecimal("80.01"), preview.getCumulativeScore());
        assertEquals(new BigDecimal("80.01"), persisted.getScore());
        assertEquals(persisted.getResultLevel(), preview.getCumulativeResultLevel());
        return findQuotaScore(assessmentQuotaScoreMapper.selectListByAssessmentStageIds(
                Collections.singleton(currentStage.getId())), fixture.quota.getId());
    }

    private HrmPerformanceAssessmentQuotaDO insertCompletionRateQuota(PreviewFixture fixture, String name,
                                                                    BigDecimal weight, String targetValue,
                                                                    String actualValue) {
        HrmPerformanceAssessmentQuotaDO quota = HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(fixture.assessment.getId()).dimensionId(1L).preset(true).name(name)
                .weight(weight).scoreType(HrmPerformanceQuotaScoreTypeEnum.COMPLETION_RATE.getType())
                .targetValue(targetValue).actualValue(actualValue)
                .selfScore(BigDecimal.ZERO).reviewerScore(BigDecimal.ZERO).finalScore(BigDecimal.ZERO).build();
        assessmentQuotaMapper.insert(quota);
        return quota;
    }

    private HrmPortalPerformanceScoreReqVO buildScoreReqVO(PreviewFixture fixture,
                                                          HrmPerformanceAssessmentStageDO stage,
                                                          HrmPortalPerformanceQuotaSaveReqVO... quotas) {
        return new HrmPortalPerformanceScoreReqVO().setAssessmentId(fixture.assessment.getId())
                .setReviewStageId(stage.getId()).setQuotas(Arrays.asList(quotas));
    }

    private BigDecimal findQuotaScore(HrmPortalPerformanceScorePreviewRespVO preview, Long quotaId) {
        HrmPerformanceAssessmentQuotaScoreRespVO score = CollUtil.findOne(preview.getQuotaScores(),
                item -> quotaId.equals(item.getAssessmentQuotaId()));
        return score == null ? null : score.getScore();
    }

    private BigDecimal findQuotaScore(List<HrmPerformanceAssessmentQuotaScoreDO> scores, Long quotaId) {
        HrmPerformanceAssessmentQuotaScoreDO score = CollUtil.findOne(scores,
                item -> quotaId.equals(item.getAssessmentQuotaId()));
        return score == null ? null : score.getScore();
    }

    private HrmPortalPerformanceTaskPageReqVO buildPendingTaskPageReqVO() {
        HrmPortalPerformanceTaskPageReqVO reqVO = new HrmPortalPerformanceTaskPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setStageStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        return reqVO;
    }

    private PreviewFixture createPreviewFixture() {
        HrmPerformanceResultTemplateDO resultTemplate = HrmPerformanceResultTemplateDO.builder()
                .name("季度结果模板").levels(buildResultConfig().getLevels()).build();
        resultTemplateMapper.insert(resultTemplate);
        HrmPerformancePlanDO plan = HrmPerformancePlanDO.builder()
                .name("季度绩效计划").assessmentTemplateId(100L)
                .assessmentConfig(HrmPerformanceAssessmentTemplateDO.AssessmentConfig.builder()
                        .name("季度考核模板").upperLimitType(HrmPerformanceUpperLimitTypeEnum.UNIFIED.getType())
                        .upperLimitScore(new BigDecimal("100")).dimensions(Collections.emptyList()).build())
                .scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .resultTemplateId(resultTemplate.getId()).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).stageType(HrmPerformanceStageTypeEnum.SELF_SCORE.getType()).build();
        planMapper.insert(plan);
        when(planService.validatePerformancePlanExists(plan.getId())).thenReturn(plan);
        HrmPerformanceAssessmentDO assessment = HrmPerformanceAssessmentDO.builder()
                .planId(plan.getId()).employeeId(200L).status(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .stageType(HrmPerformanceStageTypeEnum.SELF_SCORE.getType()).build();
        assessmentMapper.insert(assessment);
        when(employeeService.getEmployeeByUserId(1001L)).thenReturn(
                HrmEmployeeDO.builder().id(200L).userId(1001L).build());
        HrmPerformanceAssessmentQuotaDO quota = HrmPerformanceAssessmentQuotaDO.builder()
                .assessmentId(assessment.getId()).dimensionId(1L)
                .preset(true).name("目标达成率")
                .weight(new BigDecimal("100"))
                .selfScore(BigDecimal.ZERO).reviewerScore(BigDecimal.ZERO).finalScore(BigDecimal.ZERO).build();
        assessmentQuotaMapper.insert(quota);
        HrmPerformanceAssessmentStageDO reviewStage = new HrmPerformanceAssessmentStageDO();
        reviewStage.setAssessmentId(assessment.getId()).setName("员工自评")
                .setType(HrmPerformanceStageTypeEnum.SELF_SCORE.getType())
                .setRaterType(HrmPerformanceRaterTypeEnum.SELF.getType()).setHandlerEmployeeId(200L)
                .setWeight(new BigDecimal("100")).setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()).setSort(1);
        assessmentStageMapper.insert(reviewStage);
        return new PreviewFixture(resultTemplate, plan, assessment, quota, reviewStage);
    }

    private ResultConfig buildResultConfig() {
        return ResultConfig.builder().name("季度结果模板").levels(Arrays.asList(
                Level.builder().name("A").minScore(BigDecimal.valueOf(90))
                        .maxScore(BigDecimal.valueOf(100)).coefficient(BigDecimal.valueOf(1.2)).build(),
                Level.builder().name("B").minScore(BigDecimal.valueOf(80))
                        .maxScore(BigDecimal.valueOf(89.99)).coefficient(BigDecimal.ONE).build(),
                Level.builder().name("C").minScore(BigDecimal.ZERO)
                        .maxScore(BigDecimal.valueOf(79.99)).coefficient(BigDecimal.valueOf(0.8)).build()
        )).build();
    }

    private HrmPerformancePlanDO buildQuotaPlan(BigDecimal weight) {
        HrmPerformanceAssessmentTemplateDO.Quota quota = HrmPerformanceAssessmentTemplateDO.Quota.builder()
                .name("目标达成率").illustrate("目标达成情况").standard("按完成质量评分")
                .weight(weight).scoreType(1).build();
        HrmPerformanceAssessmentTemplateDO.Dimension dimension =
                HrmPerformanceAssessmentTemplateDO.Dimension.builder()
                        .name("业绩").quotaType(1).weight(new BigDecimal("100"))
                        .allowEdit(true).quotas(Collections.singletonList(quota)).build();
        HrmPerformanceAssessmentTemplateDO.AssessmentConfig config =
                HrmPerformanceAssessmentTemplateDO.AssessmentConfig.builder()
                        .name("季度考核").scoreCalculation(1).upperLimitType(1)
                        .upperLimitScore(new BigDecimal("100"))
                        .dimensions(Collections.singletonList(dimension)).build();
        return HrmPerformancePlanDO.builder().id(10L).assessmentTemplateId(QUOTA_TEMPLATE_ID)
                .assessmentConfig(config).scopes(Collections.emptyList())
                .reviewStages(Collections.emptyList()).build();
    }

    private HrmPerformanceAssessmentDO buildQuotaAssessment() {
        return HrmPerformanceAssessmentDO.builder().id(QUOTA_ASSESSMENT_ID)
                .planId(10L).employeeId(20L).build();
    }

    private static final class PreviewFixture {

        private final HrmPerformanceResultTemplateDO resultTemplate;
        private final HrmPerformancePlanDO plan;
        private final HrmPerformanceAssessmentDO assessment;
        private final HrmPerformanceAssessmentQuotaDO quota;
        private final HrmPerformanceAssessmentStageDO reviewStage;

        private PreviewFixture(HrmPerformanceResultTemplateDO resultTemplate, HrmPerformancePlanDO plan,
                               HrmPerformanceAssessmentDO assessment,
                               HrmPerformanceAssessmentQuotaDO quota,
                               HrmPerformanceAssessmentStageDO reviewStage) {
            this.resultTemplate = resultTemplate;
            this.plan = plan;
            this.assessment = assessment;
            this.quota = quota;
            this.reviewStage = reviewStage;
        }

    }

}

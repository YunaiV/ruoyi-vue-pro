package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ResultConfig;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentAppealRecordMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.plan.HrmPerformancePlanMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * {@link HrmPerformanceAssessmentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceAssessmentServiceImpl.class)
public class HrmPerformanceAssessmentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmPerformanceAssessmentServiceImpl assessmentService;

    @Resource
    private HrmPerformancePlanMapper planMapper;
    @Resource
    private HrmPerformanceAssessmentMapper assessmentMapper;
    @Resource
    private HrmPerformanceAssessmentAppealRecordMapper assessmentAppealRecordMapper;
    @Resource
    private HrmEmployeeMapper employeeMapper;

    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private HrmPerformanceAssessmentProcessService assessmentProcessService;
    @MockBean
    private HrmPerformanceAssessmentReviewService assessmentReviewService;
    @MockBean
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Test
    public void testTerminateAssessmentListByPlanId_success() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .processStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                .stageType(HrmPerformanceStageTypeEnum.SELF_SCORE.getType()).build();
        assessmentMapper.insert(assessment);

        // 调用
        assessmentService.terminatePerformanceAssessmentListByPlanId(1L, 300L);

        // 断言
        HrmPerformanceAssessmentDO updatedAssessment = assessmentMapper.selectById(assessment.getId());
        assertEquals(HrmPerformancePlanStatusEnum.TERMINATED.getStatus(), updatedAssessment.getStatus());
        assertEquals(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus(),
                updatedAssessment.getProcessStatus());
        assertEquals(HrmPerformanceStageTypeEnum.SELF_SCORE.getType(), updatedAssessment.getStageType());
        verify(assessmentActionRecordService).createPerformanceAssessmentActionRecord(
                300L, assessment.getId(), null, HrmPerformanceAssessmentActionTypeEnum.TERMINATE,
                null, null);
    }

    @Test
    public void testGetAssessmentMap_success() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment1 = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L).status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(assessment1);
        HrmPerformanceAssessmentDO assessment2 = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(200L).status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(assessment2);

        // 调用
        Map<Long, HrmPerformanceAssessmentDO> assessmentMap = assessmentService.getPerformanceAssessmentMap(
                Arrays.asList(assessment1.getId(), assessment2.getId()));

        // 断言
        assertEquals(2, assessmentMap.size());
        assertEquals(assessment1.getId(), assessmentMap.get(assessment1.getId()).getId());
        assertEquals(assessment2.getId(), assessmentMap.get(assessment2.getId()).getId());
    }

    @Test
    public void testGetAssessmentPage_resultLevelEmpty() {
        // mock 数据
        HrmPerformanceAssessmentDO unclassifiedAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L).status(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .resultLevel(null).build();
        assessmentMapper.insert(unclassifiedAssessment);
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(200L).status(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .resultLevel("A").build());

        // 准备参数
        HrmPerformanceAssessmentPageReqVO reqVO = new HrmPerformanceAssessmentPageReqVO();
        reqVO.setPlanId(1L);
        reqVO.setResultLevelEmpty(true);

        // 调用
        PageResult<HrmPerformanceAssessmentDO> pageResult = assessmentService.getPerformanceAssessmentPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(unclassifiedAssessment.getId(), pageResult.getList().get(0).getId());
        assertNull(pageResult.getList().get(0).getResultLevel());
    }

    @Test
    public void testGetPortalAssessmentPage() {
        // mock 数据
        HrmPerformancePlanDO runningPlan = HrmPerformancePlanDO.builder()
                .name("研发季度绩效").scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        planMapper.insert(runningPlan);
        HrmPerformancePlanDO archivedPlan = HrmPerformancePlanDO.builder()
                .name("研发季度绩效归档").scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        planMapper.insert(archivedPlan);
        HrmPerformanceAssessmentDO runningAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(runningPlan.getId()).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(runningAssessment);
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(archivedPlan.getId()).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build());

        // 准备参数
        HrmPortalPerformanceAssessmentPageReqVO reqVO = new HrmPortalPerformanceAssessmentPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setSearch("研发季度");
        reqVO.setArchived(false);

        // 调用
        PageResult<HrmPerformanceAssessmentDO> pageResult =
                assessmentService.getPortalPerformanceAssessmentPage(reqVO, 100L);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(runningAssessment.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetAssessmentStatusCountMapByEmployeeId() {
        // mock 数据
        HrmPerformancePlanDO runningPlan = HrmPerformancePlanDO.builder()
                .name("研发季度绩效").scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        planMapper.insert(runningPlan);
        HrmPerformancePlanDO archivedPlan = HrmPerformancePlanDO.builder()
                .name("研发季度绩效归档").scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        planMapper.insert(archivedPlan);
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(runningPlan.getId()).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(archivedPlan.getId()).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build());

        // 调用
        Map<Integer, Long> countMap =
                assessmentService.getPerformanceAssessmentStatusCountMapByEmployeeId(100L, "研发季度");

        // 断言
        assertEquals(1L, countMap.get(HrmPerformancePlanStatusEnum.RUNNING.getStatus()));
        assertEquals(1L, countMap.get(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()));
    }

    @Test
    public void testGetArchiveEmployeePage_distinctAndSearch() {
        // mock 数据
        HrmEmployeeDO matchedEmployee = HrmEmployeeDO.builder()
                .name("研发员工").jobNumber("DEV-001").build();
        employeeMapper.insert(matchedEmployee);
        HrmEmployeeDO unmatchedEmployee = HrmEmployeeDO.builder()
                .name("销售员工").jobNumber("SALE-001").build();
        employeeMapper.insert(unmatchedEmployee);
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(matchedEmployee.getId())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(2L).employeeId(matchedEmployee.getId())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(3L).employeeId(unmatchedEmployee.getId())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build());

        // 准备参数
        HrmPerformanceArchiveEmployeePageReqVO reqVO = new HrmPerformanceArchiveEmployeePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setSearch("DEV-001");

        // 调用
        PageResult<HrmEmployeeDO> pageResult =
                assessmentService.getPerformanceArchiveEmployeePage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(matchedEmployee.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetPerformanceAssessmentArchive() {
        // mock 数据
        HrmPerformanceAssessmentDO archivedAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        assessmentMapper.insert(archivedAssessment);
        HrmPerformanceAssessmentDO runningAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(2L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(runningAssessment);

        // 调用，并断言
        assertEquals(archivedAssessment.getId(),
                assessmentService.getPerformanceAssessmentArchive(archivedAssessment.getId()).getId());
        assertNull(assessmentService.getPerformanceAssessmentArchive(runningAssessment.getId()));
        assertNull(assessmentService.getPerformanceAssessmentArchive(9999L));
    }

    @Test
    public void testDeleteArchiveRecordsByEmployeeIds_onlyDeletesArchivedRecords() {
        // mock 数据
        HrmPerformanceAssessmentDO archivedAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        assessmentMapper.insert(archivedAssessment);
        HrmPerformanceAssessmentDO runningAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(2L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(runningAssessment);

        // 调用
        assessmentService.deletePerformanceArchiveRecordsByEmployeeIds(Collections.singletonList(100L));

        // 断言
        assertNull(assessmentMapper.selectById(archivedAssessment.getId()));
        assertEquals(runningAssessment.getId(), assessmentMapper.selectById(runningAssessment.getId()).getId());
    }

    @Test
    public void testDeleteArchiveRecords_deletesAppealRecords() {
        // mock 数据
        HrmPerformanceAssessmentDO assessment = HrmPerformanceAssessmentDO.builder()
                .planId(1L).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        assessmentMapper.insert(assessment);
        HrmPerformanceAssessmentAppealRecordDO appealRecord = new HrmPerformanceAssessmentAppealRecordDO()
                .setAssessmentId(assessment.getId()).setStageId(1L).setStatus(0);
        assessmentAppealRecordMapper.insert(appealRecord);
        HrmPerformanceAssessmentDO runningAssessment = HrmPerformanceAssessmentDO.builder()
                .planId(2L).employeeId(200L)
                .status(HrmPerformancePlanStatusEnum.RUNNING.getStatus()).build();
        assessmentMapper.insert(runningAssessment);

        // 调用
        assessmentService.deletePerformanceArchiveRecords(Arrays.asList(
                assessment.getId(), runningAssessment.getId()));

        // 断言
        assertEquals(0, assessmentAppealRecordMapper.selectListByAssessmentIdAndStatus(
                assessment.getId(), null).size());
        assertEquals(runningAssessment.getId(),
                assessmentMapper.selectById(runningAssessment.getId()).getId());
    }

    @Test
    public void testGetArchivedEmployeeCoefficientMap_success() {
        // mock 数据
        HrmPerformancePlanDO julyPlan = HrmPerformancePlanDO.builder()
                .name("2026-07 绩效考核").paidForMonth("2026-07")
                .scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        planMapper.insert(julyPlan);
        HrmPerformancePlanDO oldJulyPlan = HrmPerformancePlanDO.builder()
                .name("2026-07 绩效考核（旧）").paidForMonth("2026-07")
                .scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        planMapper.insert(oldJulyPlan);
        HrmPerformancePlanDO augustPlan = HrmPerformancePlanDO.builder()
                .name("2026-08 绩效考核").paidForMonth("2026-08")
                .scopes(Collections.emptyList()).reviewStages(Collections.emptyList())
                .assessmentTemplateId(1L).assessmentConfig(buildAssessmentConfig())
                .resultTemplateId(1L).resultConfig(buildResultConfig())
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus()).build();
        planMapper.insert(augustPlan);
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(oldJulyPlan.getId()).employeeId(100L)
                .status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                .coefficient(new BigDecimal("0.80")).archiveTime(LocalDateTime.of(2026, 7, 10, 9, 0)).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(julyPlan.getId()).employeeId(100L).status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                .coefficient(new BigDecimal("1.20")).archiveTime(LocalDateTime.of(2026, 7, 31, 18, 0)).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(julyPlan.getId()).employeeId(200L).status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                .archiveTime(LocalDateTime.of(2026, 7, 31, 18, 0)).build());
        assessmentMapper.insert(HrmPerformanceAssessmentDO.builder()
                .planId(augustPlan.getId()).employeeId(100L).status(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                .coefficient(new BigDecimal("2.00")).archiveTime(LocalDateTime.of(2026, 8, 31, 18, 0)).build());

        // 调用
        Map<Long, BigDecimal> coefficientMap = assessmentService.getPerformanceArchiveEmployeeCoefficientMap(
                Arrays.asList(julyPlan.getId(), oldJulyPlan.getId()), Arrays.asList(100L, 200L));

        // 断言
        assertEquals(2, coefficientMap.size());
        assertEquals(new BigDecimal("1.20"), coefficientMap.get(100L));
        assertEquals(new BigDecimal("1.00"), coefficientMap.get(200L));
    }

    @Test
    public void testGetArchivedEmployeeCoefficientMap_emptyEmployeeIds() {
        // 调用
        Map<Long, BigDecimal> coefficientMap = assessmentService.getPerformanceArchiveEmployeeCoefficientMap(
                Collections.singletonList(1L), Collections.emptyList());

        // 断言
        assertTrue(coefficientMap.isEmpty());
    }

    // ========== 随机对象 ==========

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

}

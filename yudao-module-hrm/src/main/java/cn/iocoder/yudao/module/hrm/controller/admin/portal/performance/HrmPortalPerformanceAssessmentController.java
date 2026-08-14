package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceProcessRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceTaskCountRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceAppealReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceConfirmReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceHandleStageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceFillQuotaReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceReviewRejectReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScorePreviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScoreReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentProcessService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentQueryService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentReviewService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端绩效考核")
@RestController
@RequestMapping("/hrm/portal/performance/assessment")
@Validated
public class HrmPortalPerformanceAssessmentController {

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmPerformanceAssessmentService performanceAssessmentService;
    @Resource
    private HrmPerformanceAssessmentProcessService performanceAssessmentProcessService;
    @Resource
    private HrmPerformanceAssessmentReviewService performanceAssessmentReviewService;
    @Resource
    private HrmPerformanceAssessmentQueryService performanceAssessmentQueryService;

    @GetMapping("/page")
    @Operation(summary = "获得我的绩效分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPortalPerformanceAssessmentRespVO>> getPerformanceAssessmentPage(
            @Valid HrmPortalPerformanceAssessmentPageReqVO reqVO) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        PageResult<HrmPerformanceAssessmentDO> pageResult =
                performanceAssessmentService.getPortalPerformanceAssessmentPage(reqVO, employee.getId());
        // 拼接 VO
        return success(buildPortalAssessmentRespVOPage(pageResult));
    }

    @GetMapping("/task-count")
    @Operation(summary = "获得我的绩效任务数量")
    @Parameter(name = "search", description = "员工姓名或工号", example = "张三")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalPerformanceTaskCountRespVO> getPerformanceAssessmentTaskCount(
            @RequestParam(value = "search", required = false) String search) {
        Long loginUserId = getLoginUserId();
        employeeService.validateEmployeeBySelf(loginUserId);
        Map<Integer, Map<Integer, Long>> taskCountMap =
                performanceAssessmentProcessService.getMyTaskCountMap(loginUserId, search);
        // 拼接 VO
        return success(buildTaskCountRespVO(taskCountMap));
    }

    @GetMapping("/fill-quota-task-page")
    @Operation(summary = "获得我的绩效指标填写任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentFillQuotaTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentReviewService.getMyFillQuotaTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/target-confirmation-task-page")
    @Operation(summary = "获得我的绩效目标确认任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentTargetConfirmationTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentProcessService.getMyTargetConfirmationTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/review-task-page")
    @Operation(summary = "获得我的绩效评分任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentReviewTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentReviewService.getMyReviewTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/result-audit-task-page")
    @Operation(summary = "获得我的绩效结果审核任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentResultAuditTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentProcessService.getMyResultAuditTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/result-confirmation-task-page")
    @Operation(summary = "获得我的绩效结果确认任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentResultConfirmationTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentProcessService.getMyResultConfirmationTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/appeal-task-page")
    @Operation(summary = "获得我的绩效申诉处理任务分页")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentAppealTaskPage(
            @Valid HrmPortalPerformanceTaskPageReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        PageResult<HrmPerformanceAssessmentStageDO> pageResult =
                performanceAssessmentProcessService.getMyAppealTaskPage(loginUserId, reqVO);
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentTaskRespVOPage(pageResult, loginUserId));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的员工绩效考核明细")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessment(
            @RequestParam("id") Long id,
            @RequestParam(value = "stageId", required = false) Long stageId) {
        Long loginUserId = getLoginUserId();
        HrmPerformanceAssessmentDO assessment = validateReadablePerformanceAssessment(loginUserId, id, stageId);
        // 拼接 VO
        if (stageId != null) {
            return success(performanceAssessmentQueryService
                    .getPerformanceAssessmentTaskRespVO(assessment, stageId, loginUserId));
        }
        return success(performanceAssessmentQueryService
                .getPerformanceAssessmentProcessRespVO(assessment, loginUserId));
    }

    @GetMapping("/process-record-list")
    @Operation(summary = "获得我的绩效流程记录")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmPerformanceProcessRecordRespVO>> getPerformanceAssessmentProcessRecordList(
            @RequestParam("id") Long id,
            @RequestParam(value = "stageId", required = false) Long stageId) {
        Long loginUserId = getLoginUserId();
        HrmPerformanceAssessmentDO assessment = validateReadablePerformanceAssessment(loginUserId, id, stageId);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment, loginUserId));
    }

    @PutMapping("/fill-quota")
    @Operation(summary = "填写我的绩效指标")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> fillPerformanceAssessmentQuota(
            @Valid @RequestBody HrmPortalPerformanceFillQuotaReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        validateOwnPerformanceAssessment(loginUserId, reqVO.getAssessmentId());
        performanceAssessmentReviewService.fillQuota(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/confirm-target")
    @Operation(summary = "确认我的绩效目标")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> confirmPerformanceAssessmentTarget(
            @Valid @RequestBody HrmPortalPerformanceConfirmReqVO reqVO) {
        performanceAssessmentProcessService.confirmTarget(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/score-preview")
    @Operation(summary = "试算绩效评分")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<HrmPortalPerformanceScorePreviewRespVO> previewPerformanceAssessmentScore(
            @Valid @RequestBody HrmPortalPerformanceScoreReqVO reqVO) {
        return success(performanceAssessmentReviewService.previewScore(getLoginUserId(), reqVO));
    }

    @PutMapping("/score")
    @Operation(summary = "提交绩效自评或他评")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<HrmPortalPerformanceProcessRespVO> scorePerformanceAssessment(
            @Valid @RequestBody HrmPortalPerformanceScoreReqVO reqVO) {
        return success(performanceAssessmentReviewService.scoreAssessment(getLoginUserId(), reqVO));
    }

    @PutMapping("/reject-review-stage")
    @Operation(summary = "驳回上一绩效评分阶段")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> rejectPerformanceAssessmentReviewStage(
            @Valid @RequestBody HrmPortalPerformanceReviewRejectReqVO reqVO) {
        performanceAssessmentReviewService.rejectReviewStage(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/handle-result-audit")
    @Operation(summary = "处理绩效结果审核")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> handlePerformanceAssessmentResultAudit(
            @Valid @RequestBody HrmPortalPerformanceHandleStageReqVO reqVO) {
        performanceAssessmentProcessService.handleResultAudit(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/confirm-result")
    @Operation(summary = "确认我的绩效结果")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> confirmPerformanceAssessmentResult(
            @Valid @RequestBody HrmPortalPerformanceConfirmReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        validateOwnPerformanceAssessment(loginUserId, reqVO.getAssessmentId());
        performanceAssessmentProcessService.confirmResult(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/submit-appeal")
    @Operation(summary = "提交我的绩效申诉")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<HrmPortalPerformanceProcessRespVO> submitPerformanceAssessmentAppeal(
            @Valid @RequestBody HrmPortalPerformanceAppealReqVO reqVO) {
        Long loginUserId = getLoginUserId();
        validateOwnPerformanceAssessment(loginUserId, reqVO.getAssessmentId());
        return success(performanceAssessmentProcessService.submitAppeal(loginUserId, reqVO));
    }

    @PutMapping("/handle-appeal")
    @Operation(summary = "处理绩效申诉")
    @PreAuthorize("@ss.hasPermission('hrm:portal:performance:action')")
    public CommonResult<Boolean> handlePerformanceAssessmentAppeal(
            @Valid @RequestBody HrmPortalPerformanceHandleStageReqVO reqVO) {
        performanceAssessmentProcessService.handleAppeal(getLoginUserId(), reqVO);
        return success(true);
    }

    private HrmPerformanceAssessmentDO validateOwnPerformanceAssessment(Long loginUserId, Long assessmentId) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(loginUserId);
        return performanceAssessmentService.validatePerformanceAssessmentByEmployeeId(assessmentId, employee.getId());
    }

    private HrmPerformanceAssessmentDO validateReadablePerformanceAssessment(
            Long loginUserId, Long assessmentId, Long stageId) {
        if (stageId == null) {
            return validateOwnPerformanceAssessment(loginUserId, assessmentId);
        }
        performanceAssessmentProcessService.validateTaskStage(loginUserId, assessmentId, stageId);
        return performanceAssessmentService.getPerformanceAssessment(assessmentId);
    }

    // ==================== 拼接 VO ====================

    private PageResult<HrmPortalPerformanceAssessmentRespVO> buildPortalAssessmentRespVOPage(
            PageResult<HrmPerformanceAssessmentDO> pageResult) {
        PageResult<HrmPerformanceAssessmentRespVO> respVOPage =
                performanceAssessmentQueryService.getPerformanceAssessmentRespVOPage(pageResult);
        return new PageResult<>(
                BeanUtils.toBean(respVOPage.getList(), HrmPortalPerformanceAssessmentRespVO.class),
                respVOPage.getTotal());
    }

    private HrmPortalPerformanceTaskCountRespVO buildTaskCountRespVO(
            Map<Integer, Map<Integer, Long>> taskCountMap) {
        Integer pendingStatus = HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus();
        Integer completedStatus = HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus();
        // 按流程类型和处理状态组装员工端各页签的待办、已办数量
        return new HrmPortalPerformanceTaskCountRespVO()
                .setFillPendingCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(), pendingStatus))
                .setFillCompletedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(), completedStatus))
                .setTargetPendingCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(), pendingStatus))
                .setTargetCompletedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(), completedStatus))
                .setReviewPendingCount(getTaskCount(taskCountMap, HrmPerformanceStageTypeEnum.REVIEW_TYPES, pendingStatus))
                .setReviewCompletedCount(getTaskCount(taskCountMap, HrmPerformanceStageTypeEnum.REVIEW_TYPES, completedStatus))
                .setResultAuditPendingCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(), pendingStatus))
                .setResultAuditCompletedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(), completedStatus))
                .setResultConfirmationPendingCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), pendingStatus))
                .setResultConfirmationCompletedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), completedStatus))
                .setResultConfirmationAppealedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(),
                        HrmPerformanceAssessmentStageStatusEnum.APPEALED.getStatus()))
                .setAppealPendingCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), pendingStatus))
                .setAppealCompletedCount(getTaskCount(
                        taskCountMap, HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), completedStatus));
    }

    private long getTaskCount(Map<Integer, Map<Integer, Long>> countMap, Integer type, Integer status) {
        return countMap.getOrDefault(type, Collections.emptyMap()).getOrDefault(status, 0L);
    }

    @SuppressWarnings("SameParameterValue")
    private long getTaskCount(Map<Integer, Map<Integer, Long>> countMap,
                              Collection<Integer> types, Integer status) {
        long count = 0L;
        for (Integer type : types) {
            count += getTaskCount(countMap, type, status);
        }
        return count;
    }

}

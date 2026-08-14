package cn.iocoder.yudao.module.hrm.controller.admin.performance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchivePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentBatchReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceProcessRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentProcessService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentQueryService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertLinkedSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工绩效考核")
@RestController
@RequestMapping("/hrm/performance/assessment")
@Validated
public class HrmPerformanceAssessmentController {

    @Resource
    private HrmPerformanceAssessmentService performanceAssessmentService;
    @Resource
    private HrmPerformanceAssessmentQueryService performanceAssessmentQueryService;
    @Resource
    private HrmPerformancePlanService performancePlanService;
    @Resource
    private HrmEmployeeService employeeService;

    @PostMapping("/create-list")
    @Operation(summary = "添加员工绩效考核")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:update')")
    public CommonResult<Boolean> addPerformancePlanEmployees(
            @Valid @RequestBody HrmPerformanceAssessmentBatchReqVO reqVO) {
        performancePlanService.addPerformancePlanEmployees(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "移除员工绩效考核")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:update')")
    public CommonResult<Boolean> removePerformancePlanEmployees(
            @Valid @RequestBody HrmPerformanceAssessmentBatchReqVO reqVO) {
        performancePlanService.removePerformancePlanEmployees(reqVO);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得员工绩效考核分页")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentPage(
            @Validated HrmPerformanceAssessmentPageReqVO reqVO) {
        PageResult<HrmPerformanceAssessmentDO> pageResult =
                performanceAssessmentService.getPerformanceAssessmentPage(reqVO);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentRespVOPage(pageResult));
    }

    @GetMapping("/unassigned-employee-id-list")
    @Operation(summary = "获得未加入指定绩效计划的员工编号列表")
    @Parameter(name = "planId", description = "绩效计划编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:query')")
    public CommonResult<List<Long>> getPerformancePlanUnassignedEmployeeIdList(
            @RequestParam("planId") Long planId) {
        performancePlanService.validatePerformancePlanExists(planId);
        // 1. 查询已经参与计划的员工
        List<HrmPerformanceAssessmentDO> assessments =
                performanceAssessmentService.getPerformanceAssessmentListByPlanId(planId);
        Set<Long> assignedEmployeeIds = convertSet(assessments, HrmPerformanceAssessmentDO::getEmployeeId);
        // 2. 筛选尚未参与计划的员工
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(new HrmEmployeeListReqVO());
        return success(convertList(employees, HrmEmployeeDO::getId,
                employee -> !assignedEmployeeIds.contains(employee.getId())));
    }

    @GetMapping("/get")
    @Operation(summary = "获得员工绩效考核")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:query')")
    public CommonResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessment(@RequestParam("id") Long id) {
        HrmPerformanceAssessmentDO assessment = performanceAssessmentService.getPerformanceAssessment(id);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentProcessRespVO(
                assessment, getLoginUserId()));
    }

    @GetMapping("/process-record-list")
    @Operation(summary = "获得绩效流程记录列表")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:plan:query')")
    public CommonResult<List<HrmPerformanceProcessRecordRespVO>> getPerformanceAssessmentProcessRecordList(
            @RequestParam("id") Long id) {
        HrmPerformanceAssessmentDO assessment = performanceAssessmentService.getPerformanceAssessment(id);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment));
    }

    @GetMapping("/archive-page")
    @Operation(summary = "获得绩效归档分页")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentRespVO>> getPerformanceAssessmentArchivePage(
            @Validated HrmPerformanceArchivePageReqVO reqVO) {
        PageResult<HrmPerformanceAssessmentDO> pageResult =
                performanceAssessmentService.getPerformanceAssessmentArchivePage(reqVO);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentRespVOPage(pageResult));
    }

    @GetMapping("/archive-employee-page")
    @Operation(summary = "获得员工绩效档案分页")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:query')")
    public CommonResult<PageResult<HrmPerformanceArchiveEmployeeRespVO>> getPerformanceArchiveEmployeePage(
            @Validated HrmPerformanceArchiveEmployeePageReqVO reqVO) {
        PageResult<HrmEmployeeDO> pageResult =
                performanceAssessmentService.getPerformanceArchiveEmployeePage(reqVO);
        return success(performanceAssessmentQueryService.getPerformanceArchiveEmployeeRespVOPage(pageResult));
    }

    @GetMapping("/archive-get")
    @Operation(summary = "获得绩效归档详情")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:query')")
    public CommonResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentArchive(@RequestParam("id") Long id) {
        HrmPerformanceAssessmentDO assessment = performanceAssessmentService.getPerformanceAssessmentArchive(id);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentRespVO(assessment));
    }

    @GetMapping("/archive-process-record-list")
    @Operation(summary = "获得绩效归档流程记录列表")
    @Parameter(name = "id", description = "员工绩效考核编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:query')")
    public CommonResult<List<HrmPerformanceProcessRecordRespVO>> getPerformanceAssessmentArchiveProcessRecordList(
            @RequestParam("id") Long id) {
        HrmPerformanceAssessmentDO assessment = performanceAssessmentService.getPerformanceAssessmentArchive(id);
        return success(performanceAssessmentQueryService.getPerformanceAssessmentProcessRecordList(assessment));
    }

    @GetMapping("/archive-plan-simple-list")
    @Operation(summary = "获得绩效归档计划精简列表")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:query')")
    public CommonResult<List<HrmPerformancePlanRespVO>> getPerformanceArchivePlanSimpleList() {
        // 1. 查询存在归档考核的绩效计划
        List<HrmPerformanceAssessmentDO> assessments =
                performanceAssessmentService.getPerformanceAssessmentListByStatus(
                        HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        Set<Long> planIds = convertLinkedSet(assessments, HrmPerformanceAssessmentDO::getPlanId);
        List<HrmPerformancePlanDO> plans = performancePlanService.getPerformancePlanList(planIds);
        // 2. 转换响应
        return success(convertList(plans, plan -> new HrmPerformancePlanRespVO()
                .setId(plan.getId()).setName(plan.getName())));
    }

    @DeleteMapping("/archive-delete")
    @Operation(summary = "删除绩效归档记录")
    @Parameter(name = "ids", description = "绩效归档记录编号列表", required = true, example = "1024,1025")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:delete')")
    public CommonResult<Boolean> deletePerformanceArchiveRecords(
            @RequestParam("ids") @NotEmpty(message = "归档记录不能为空") List<Long> ids) {
        performanceAssessmentService.deletePerformanceArchiveRecords(ids);
        return success(true);
    }

    @DeleteMapping("/archive-employee-delete")
    @Operation(summary = "删除员工的全部绩效档案")
    @Parameter(name = "employeeIds", description = "员工编号列表", required = true, example = "1024,1025")
    @PreAuthorize("@ss.hasPermission('hrm:performance:archive:delete')")
    public CommonResult<Boolean> deletePerformanceArchiveEmployeeRecords(
            @RequestParam("employeeIds") @NotEmpty(message = "员工不能为空") List<Long> employeeIds) {
        performanceAssessmentService.deletePerformanceArchiveRecordsByEmployeeIds(employeeIds);
        return success(true);
    }

}

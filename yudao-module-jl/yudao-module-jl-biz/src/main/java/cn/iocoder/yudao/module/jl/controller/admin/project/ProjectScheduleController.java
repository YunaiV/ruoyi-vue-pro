package cn.iocoder.yudao.module.jl.controller.admin.project;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSchedule;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectScheduleMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectScheduleService;

@Tag(name = "管理后台 - 项目安排单")
@RestController
@RequestMapping("/jl/project-schedule")
@Validated
public class ProjectScheduleController {

    @Resource
    private ProjectScheduleService projectScheduleService;

    @Resource
    private ProjectScheduleMapper projectScheduleMapper;

//    @PostMapping("/create")
//    @Operation(summary = "创建项目安排单")
//    @PreAuthorize("@ss.hasPermission('jl:project-schedule:create')")
//    public CommonResult<Long> createProjectSchedule(@Valid @RequestBody ProjectScheduleCreateReqVO createReqVO) {
//        return success(projectScheduleService.createProjectSchedule(createReqVO));
//    }

    @PostMapping("/save")
    @Operation(summary = "保存项目安排单")
    @PreAuthorize("@ss.hasPermission('jl:project-schedule:create')")
    public CommonResult<Long> createProjectSchedule(@Valid @RequestBody ProjectScheduleSaveReqVO saveReqVO) {
        return success(projectScheduleService.saveProjectSchedule(saveReqVO));
    }

//    @PutMapping("/update")
//    @Operation(summary = "更新项目安排单")
//    @PreAuthorize("@ss.hasPermission('jl:project-schedule:update')")
//    public CommonResult<Boolean> updateProjectSchedule(@Valid @RequestBody ProjectScheduleUpdateReqVO updateReqVO) {
//        projectScheduleService.updateProjectSchedule(updateReqVO);
//        return success(true);
//    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目安排单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-schedule:delete')")
    public CommonResult<Boolean> deleteProjectSchedule(@RequestParam("id") Long id) {
        projectScheduleService.deleteProjectSchedule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目安排单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-schedule:query')")
    public CommonResult<ProjectScheduleRespVO> getProjectSchedule(@RequestParam("id") Long id) {
            Optional<ProjectSchedule> projectSchedule = projectScheduleService.getProjectSchedule(id);
        return success(projectSchedule.map(projectScheduleMapper::toDto).orElseThrow(() -> exception(PROJECT_SCHEDULE_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目安排单列表")
    @PreAuthorize("@ss.hasPermission('jl:project-schedule:query')")
    public CommonResult<PageResult<ProjectScheduleRespVO>> getProjectSchedulePage(@Valid ProjectSchedulePageReqVO pageVO, @Valid ProjectSchedulePageOrder orderV0) {
        PageResult<ProjectSchedule> pageResult = projectScheduleService.getProjectSchedulePage(pageVO, orderV0);
        return success(projectScheduleMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目安排单 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-schedule:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectScheduleExcel(@Valid ProjectScheduleExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectSchedule> list = projectScheduleService.getProjectScheduleList(exportReqVO);
        // 导出 Excel
        List<ProjectScheduleExcelVO> excelData = projectScheduleMapper.toExcelList(list);
        ExcelUtils.write(response, "项目安排单.xls", "数据", ProjectScheduleExcelVO.class, excelData);
    }

}

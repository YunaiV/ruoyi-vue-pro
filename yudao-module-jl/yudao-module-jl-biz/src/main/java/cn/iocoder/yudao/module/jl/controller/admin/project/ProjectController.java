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
import cn.iocoder.yudao.module.jl.entity.project.Project;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectService;

@Tag(name = "管理后台 - 项目管理")
@RestController
@RequestMapping("/jl/project")
@Validated
public class ProjectController {

    @Resource
    private ProjectService projectService;

    @Resource
    private ProjectMapper projectMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目管理")
    @PreAuthorize("@ss.hasPermission('jl:project:create')")
    public CommonResult<Long> createProject(@Valid @RequestBody ProjectCreateReqVO createReqVO) {
        return success(projectService.createProject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目管理")
    @PreAuthorize("@ss.hasPermission('jl:project:update')")
    public CommonResult<Boolean> updateProject(@Valid @RequestBody ProjectUpdateReqVO updateReqVO) {
        projectService.updateProject(updateReqVO);
        return success(true);
    }

    @PutMapping("/current-schedule")
    @Operation(summary = "设置当前的主安排单")
    @PreAuthorize("@ss.hasPermission('jl:project:update')")
    public CommonResult<Boolean> setCurrentSchedule(@Valid @RequestBody ProjectSetCurrentScheduleReqVO updateReqVO) {
        projectService.setProjectCurrentSchedule(updateReqVO.getProjectId(), updateReqVO.getScheduleId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        projectService.deleteProject(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project:query')")
    public CommonResult<ProjectRespVO> getProject(@RequestParam("id") Long id) {
            Optional<Project> project = projectService.getProject(id);
        return success(project.map(projectMapper::toDto).orElseThrow(() -> exception(PROJECT_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目管理列表")
    @PreAuthorize("@ss.hasPermission('jl:project:query')")
    public CommonResult<PageResult<ProjectRespVO>> getProjectPage(@Valid ProjectPageReqVO pageVO, @Valid ProjectPageOrder orderV0) {
        PageResult<Project> pageResult = projectService.getProjectPage(pageVO, orderV0);
        return success(projectMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目管理 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectExcel(@Valid ProjectExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<Project> list = projectService.getProjectList(exportReqVO);
        // 导出 Excel
        List<ProjectExcelVO> excelData = projectMapper.toExcelList(list);
        ExcelUtils.write(response, "项目管理.xls", "数据", ProjectExcelVO.class, excelData);
    }

}

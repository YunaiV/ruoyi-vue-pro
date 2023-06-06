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
import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectCategoryMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectCategoryService;

@Tag(name = "管理后台 - 项目的实验名目")
@RestController
@RequestMapping("/jl/project-category")
@Validated
public class ProjectCategoryController {

    @Resource
    private ProjectCategoryService projectCategoryService;

    @Resource
    private ProjectCategoryMapper projectCategoryMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目的实验名目")
    @PreAuthorize("@ss.hasPermission('jl:project-category:create')")
    public CommonResult<Long> createProjectCategory(@Valid @RequestBody ProjectCategoryCreateReqVO createReqVO) {
        return success(projectCategoryService.createProjectCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目的实验名目")
    @PreAuthorize("@ss.hasPermission('jl:project-category:update')")
    public CommonResult<Boolean> updateProjectCategory(@Valid @RequestBody ProjectCategoryUpdateReqVO updateReqVO) {
        projectCategoryService.updateProjectCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目的实验名目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-category:delete')")
    public CommonResult<Boolean> deleteProjectCategory(@RequestParam("id") Long id) {
        projectCategoryService.deleteProjectCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目的实验名目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-category:query')")
    public CommonResult<ProjectCategoryRespVO> getProjectCategory(@RequestParam("id") Long id) {
            Optional<ProjectCategory> projectCategory = projectCategoryService.getProjectCategory(id);
        return success(projectCategory.map(projectCategoryMapper::toDto).orElseThrow(() -> exception(PROJECT_CATEGORY_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目的实验名目列表")
    @PreAuthorize("@ss.hasPermission('jl:project-category:query')")
    public CommonResult<PageResult<ProjectCategoryRespVO>> getProjectCategoryPage(@Valid ProjectCategoryPageReqVO pageVO, @Valid ProjectCategoryPageOrder orderV0) {
        PageResult<ProjectCategory> pageResult = projectCategoryService.getProjectCategoryPage(pageVO, orderV0);
        return success(projectCategoryMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目的实验名目 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-category:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectCategoryExcel(@Valid ProjectCategoryExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectCategory> list = projectCategoryService.getProjectCategoryList(exportReqVO);
        // 导出 Excel
        List<ProjectCategoryExcelVO> excelData = projectCategoryMapper.toExcelList(list);
        ExcelUtils.write(response, "项目的实验名目.xls", "数据", ProjectCategoryExcelVO.class, excelData);
    }

}

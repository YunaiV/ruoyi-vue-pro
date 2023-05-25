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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import cn.iocoder.yudao.module.jl.convert.project.ProjectBaseConvert;
import cn.iocoder.yudao.module.jl.service.project.ProjectBaseService;

@Tag(name = "管理后台 - 项目管理")
@RestController
@RequestMapping("/jl/project-base")
@Validated
public class ProjectBaseController {

    @Resource
    private ProjectBaseService projectBaseService;

    @PostMapping("/create")
    @Operation(summary = "创建项目管理")
    @PreAuthorize("@ss.hasPermission('jl:project-base:create')")
    public CommonResult<Long> createProjectBase(@Valid @RequestBody ProjectBaseCreateReqVO createReqVO) {
        return success(projectBaseService.createProjectBase(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目管理")
    @PreAuthorize("@ss.hasPermission('jl:project-base:update')")
    public CommonResult<Boolean> updateProjectBase(@Valid @RequestBody ProjectBaseUpdateReqVO updateReqVO) {
        projectBaseService.updateProjectBase(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-base:delete')")
    public CommonResult<Boolean> deleteProjectBase(@RequestParam("id") Long id) {
        projectBaseService.deleteProjectBase(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-base:query')")
    public CommonResult<ProjectBaseRespVO> getProjectBase(@RequestParam("id") Long id) {
        ProjectBaseDO projectBase = projectBaseService.getProjectBase(id);
        return success(ProjectBaseConvert.INSTANCE.convert(projectBase));
    }

    @GetMapping("/list")
    @Operation(summary = "获得项目管理列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:project-base:query')")
    public CommonResult<List<ProjectBaseRespVO>> getProjectBaseList(@RequestParam("ids") Collection<Long> ids) {
        List<ProjectBaseDO> list = projectBaseService.getProjectBaseList(ids);
        return success(ProjectBaseConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目管理分页")
    @PreAuthorize("@ss.hasPermission('jl:project-base:query')")
    public CommonResult<PageResult<ProjectBaseRespVO>> getProjectBasePage(@Valid ProjectBasePageReqVO pageVO) {
        PageResult<ProjectBaseDO> pageResult = projectBaseService.getProjectBasePage(pageVO);
        return success(ProjectBaseConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目管理 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-base:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectBaseExcel(@Valid ProjectBaseExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ProjectBaseDO> list = projectBaseService.getProjectBaseList(exportReqVO);
        // 导出 Excel
        List<ProjectBaseExcelVO> datas = ProjectBaseConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "项目管理.xls", "数据", ProjectBaseExcelVO.class, datas);
    }

}

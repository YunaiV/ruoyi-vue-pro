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
import cn.iocoder.yudao.module.jl.entity.project.ProjectConstract;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectConstractMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectConstractService;

@Tag(name = "管理后台 - 项目合同")
@RestController
@RequestMapping("/jl/project-constract")
@Validated
public class ProjectConstractController {

    @Resource
    private ProjectConstractService projectConstractService;

    @Resource
    private ProjectConstractMapper projectConstractMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目合同")
    @PreAuthorize("@ss.hasPermission('jl:project-constract:create')")
    public CommonResult<Long> createProjectConstract(@Valid @RequestBody ProjectConstractCreateReqVO createReqVO) {
        return success(projectConstractService.createProjectConstract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目合同")
    @PreAuthorize("@ss.hasPermission('jl:project-constract:update')")
    public CommonResult<Boolean> updateProjectConstract(@Valid @RequestBody ProjectConstractUpdateReqVO updateReqVO) {
        projectConstractService.updateProjectConstract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-constract:delete')")
    public CommonResult<Boolean> deleteProjectConstract(@RequestParam("id") Long id) {
        projectConstractService.deleteProjectConstract(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-constract:query')")
    public CommonResult<ProjectConstractRespVO> getProjectConstract(@RequestParam("id") Long id) {
            Optional<ProjectConstract> projectConstract = projectConstractService.getProjectConstract(id);
        return success(projectConstract.map(projectConstractMapper::toDto).orElseThrow(() -> exception(PROJECT_CONSTRACT_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目合同列表")
    @PreAuthorize("@ss.hasPermission('jl:project-constract:query')")
    public CommonResult<PageResult<ProjectConstractRespVO>> getProjectConstractPage(@Valid ProjectConstractPageReqVO pageVO, @Valid ProjectConstractPageOrder orderV0) {
        PageResult<ProjectConstract> pageResult = projectConstractService.getProjectConstractPage(pageVO, orderV0);
        return success(projectConstractMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目合同 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-constract:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectConstractExcel(@Valid ProjectConstractExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectConstract> list = projectConstractService.getProjectConstractList(exportReqVO);
        // 导出 Excel
        List<ProjectConstractExcelVO> excelData = projectConstractMapper.toExcelList(list);
        ExcelUtils.write(response, "项目合同.xls", "数据", ProjectConstractExcelVO.class, excelData);
    }

}

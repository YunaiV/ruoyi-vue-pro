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
import cn.iocoder.yudao.module.jl.entity.project.ProjectFund;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectFundMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectFundService;

@Tag(name = "管理后台 - 项目款项")
@RestController
@RequestMapping("/jl/project-fund")
@Validated
public class ProjectFundController {

    @Resource
    private ProjectFundService projectFundService;

    @Resource
    private ProjectFundMapper projectFundMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目款项")
    @PreAuthorize("@ss.hasPermission('jl:project-fund:create')")
    public CommonResult<Long> createProjectFund(@Valid @RequestBody ProjectFundCreateReqVO createReqVO) {
        return success(projectFundService.createProjectFund(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目款项")
    @PreAuthorize("@ss.hasPermission('jl:project-fund:update')")
    public CommonResult<Boolean> updateProjectFund(@Valid @RequestBody ProjectFundUpdateReqVO updateReqVO) {
        projectFundService.updateProjectFund(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目款项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-fund:delete')")
    public CommonResult<Boolean> deleteProjectFund(@RequestParam("id") Long id) {
        projectFundService.deleteProjectFund(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目款项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-fund:query')")
    public CommonResult<ProjectFundRespVO> getProjectFund(@RequestParam("id") Long id) {
            Optional<ProjectFund> projectFund = projectFundService.getProjectFund(id);
        return success(projectFund.map(projectFundMapper::toDto).orElseThrow(() -> exception(PROJECT_FUND_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目款项列表")
    @PreAuthorize("@ss.hasPermission('jl:project-fund:query')")
    public CommonResult<PageResult<ProjectFundRespVO>> getProjectFundPage(@Valid ProjectFundPageReqVO pageVO, @Valid ProjectFundPageOrder orderV0) {
        PageResult<ProjectFund> pageResult = projectFundService.getProjectFundPage(pageVO, orderV0);
        return success(projectFundMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目款项 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-fund:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectFundExcel(@Valid ProjectFundExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectFund> list = projectFundService.getProjectFundList(exportReqVO);
        // 导出 Excel
        List<ProjectFundExcelVO> excelData = projectFundMapper.toExcelList(list);
        ExcelUtils.write(response, "项目款项.xls", "数据", ProjectFundExcelVO.class, excelData);
    }

}

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
import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectChargeitemMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectChargeitemService;

@Tag(name = "管理后台 - 项目中的实验名目的收费项")
@RestController
@RequestMapping("/jl/project-chargeitem")
@Validated
public class ProjectChargeitemController {

    @Resource
    private ProjectChargeitemService projectChargeitemService;

    @Resource
    private ProjectChargeitemMapper projectChargeitemMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目中的实验名目的收费项")
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:create')")
    public CommonResult<Long> createProjectChargeitem(@Valid @RequestBody ProjectChargeitemCreateReqVO createReqVO) {
        return success(projectChargeitemService.createProjectChargeitem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目中的实验名目的收费项")
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:update')")
    public CommonResult<Boolean> updateProjectChargeitem(@Valid @RequestBody ProjectChargeitemUpdateReqVO updateReqVO) {
        projectChargeitemService.updateProjectChargeitem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目中的实验名目的收费项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:delete')")
    public CommonResult<Boolean> deleteProjectChargeitem(@RequestParam("id") Long id) {
        projectChargeitemService.deleteProjectChargeitem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目中的实验名目的收费项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:query')")
    public CommonResult<ProjectChargeitemRespVO> getProjectChargeitem(@RequestParam("id") Long id) {
            Optional<ProjectChargeitem> projectChargeitem = projectChargeitemService.getProjectChargeitem(id);
        return success(projectChargeitem.map(projectChargeitemMapper::toDto).orElseThrow(() -> exception(PROJECT_CHARGEITEM_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目中的实验名目的收费项列表")
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:query')")
    public CommonResult<PageResult<ProjectChargeitemRespVO>> getProjectChargeitemPage(@Valid ProjectChargeitemPageReqVO pageVO, @Valid ProjectChargeitemPageOrder orderV0) {
        PageResult<ProjectChargeitem> pageResult = projectChargeitemService.getProjectChargeitemPage(pageVO, orderV0);
        return success(projectChargeitemMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目中的实验名目的收费项 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-chargeitem:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectChargeitemExcel(@Valid ProjectChargeitemExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectChargeitem> list = projectChargeitemService.getProjectChargeitemList(exportReqVO);
        // 导出 Excel
        List<ProjectChargeitemExcelVO> excelData = projectChargeitemMapper.toExcelList(list);
        ExcelUtils.write(response, "项目中的实验名目的收费项.xls", "数据", ProjectChargeitemExcelVO.class, excelData);
    }

}

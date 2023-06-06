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
import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectSupplyMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectSupplyService;

@Tag(name = "管理后台 - 项目中的实验名目的物资项")
@RestController
@RequestMapping("/jl/project-supply")
@Validated
public class ProjectSupplyController {

    @Resource
    private ProjectSupplyService projectSupplyService;

    @Resource
    private ProjectSupplyMapper projectSupplyMapper;

    @PostMapping("/create")
    @Operation(summary = "创建项目中的实验名目的物资项")
    @PreAuthorize("@ss.hasPermission('jl:project-supply:create')")
    public CommonResult<Long> createProjectSupply(@Valid @RequestBody ProjectSupplyCreateReqVO createReqVO) {
        return success(projectSupplyService.createProjectSupply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目中的实验名目的物资项")
    @PreAuthorize("@ss.hasPermission('jl:project-supply:update')")
    public CommonResult<Boolean> updateProjectSupply(@Valid @RequestBody ProjectSupplyUpdateReqVO updateReqVO) {
        projectSupplyService.updateProjectSupply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目中的实验名目的物资项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-supply:delete')")
    public CommonResult<Boolean> deleteProjectSupply(@RequestParam("id") Long id) {
        projectSupplyService.deleteProjectSupply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目中的实验名目的物资项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-supply:query')")
    public CommonResult<ProjectSupplyRespVO> getProjectSupply(@RequestParam("id") Long id) {
            Optional<ProjectSupply> projectSupply = projectSupplyService.getProjectSupply(id);
        return success(projectSupply.map(projectSupplyMapper::toDto).orElseThrow(() -> exception(PROJECT_SUPPLY_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目中的实验名目的物资项列表")
    @PreAuthorize("@ss.hasPermission('jl:project-supply:query')")
    public CommonResult<PageResult<ProjectSupplyRespVO>> getProjectSupplyPage(@Valid ProjectSupplyPageReqVO pageVO, @Valid ProjectSupplyPageOrder orderV0) {
        PageResult<ProjectSupply> pageResult = projectSupplyService.getProjectSupplyPage(pageVO, orderV0);
        return success(projectSupplyMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目中的实验名目的物资项 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-supply:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectSupplyExcel(@Valid ProjectSupplyExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectSupply> list = projectSupplyService.getProjectSupplyList(exportReqVO);
        // 导出 Excel
        List<ProjectSupplyExcelVO> excelData = projectSupplyMapper.toExcelList(list);
        ExcelUtils.write(response, "项目中的实验名目的物资项.xls", "数据", ProjectSupplyExcelVO.class, excelData);
    }

}

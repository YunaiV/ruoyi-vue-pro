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
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import cn.iocoder.yudao.module.jl.mapper.project.ProjectQuoteMapper;
import cn.iocoder.yudao.module.jl.service.project.ProjectQuoteService;

@Tag(name = "管理后台 - 项目报价")
@RestController
@RequestMapping("/jl/project-quote")
@Validated
public class ProjectQuoteController {

    @Resource
    private ProjectQuoteService projectQuoteService;

    @Resource
    private ProjectQuoteMapper projectQuoteMapper;

//    @PostMapping("/create")
//    @Operation(summary = "创建项目报价")
//    @PreAuthorize("@ss.hasPermission('jl:project-quote:create')")
//    public CommonResult<Long> createProjectQuote(@Valid @RequestBody ProjectQuoteCreateReqVO createReqVO) {
//        return success(projectQuoteService.createProjectQuote(createReqVO));
//    }

    @PostMapping("/save")
    @Operation(summary = "保存项目报价")
    @PreAuthorize("@ss.hasPermission('jl:project-quote:create')")
    public CommonResult<Long> saveProjectQuote(@Valid @RequestBody ProjectQuoteSaveReqVO saveReqVO) {
        return success(projectQuoteService.saveProjectQuote(saveReqVO));
    }

//    @PutMapping("/update")
//    @Operation(summary = "更新项目报价")
//    @PreAuthorize("@ss.hasPermission('jl:project-quote:update')")
//    public CommonResult<Boolean> updateProjectQuote(@Valid @RequestBody ProjectQuoteUpdateReqVO updateReqVO) {
//        projectQuoteService.updateProjectQuote(updateReqVO);
//        return success(true);
//    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除项目报价")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:project-quote:delete')")
    public CommonResult<Boolean> deleteProjectQuote(@RequestParam("id") Long id) {
        projectQuoteService.deleteProjectQuote(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得项目报价")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:project-quote:query')")
    public CommonResult<ProjectQuoteRespVO> getProjectQuote(@RequestParam("id") Long id) {
            Optional<ProjectQuote> projectQuote = projectQuoteService.getProjectQuote(id);
        return success(projectQuote.map(projectQuoteMapper::toDto).orElseThrow(() -> exception(PROJECT_QUOTE_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得项目报价列表")
    @PreAuthorize("@ss.hasPermission('jl:project-quote:query')")
    public CommonResult<PageResult<ProjectQuoteRespVO>> getProjectQuotePage(@Valid ProjectQuotePageReqVO pageVO, @Valid ProjectQuotePageOrder orderV0) {
        PageResult<ProjectQuote> pageResult = projectQuoteService.getProjectQuotePage(pageVO, orderV0);
        return success(projectQuoteMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目报价 Excel")
    @PreAuthorize("@ss.hasPermission('jl:project-quote:export')")
    @OperateLog(type = EXPORT)
    public void exportProjectQuoteExcel(@Valid ProjectQuoteExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<ProjectQuote> list = projectQuoteService.getProjectQuoteList(exportReqVO);
        // 导出 Excel
        List<ProjectQuoteExcelVO> excelData = projectQuoteMapper.toExcelList(list);
        ExcelUtils.write(response, "项目报价.xls", "数据", ProjectQuoteExcelVO.class, excelData);
    }

}

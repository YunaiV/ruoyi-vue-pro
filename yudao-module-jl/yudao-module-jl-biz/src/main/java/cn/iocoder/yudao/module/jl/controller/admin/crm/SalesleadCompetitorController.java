package cn.iocoder.yudao.module.jl.controller.admin.crm;

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

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCompetitorMapper;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadCompetitorService;

@Tag(name = "管理后台 - 销售线索中竞争对手的报价")
@RestController
@RequestMapping("/jl/saleslead-competitor")
@Validated
public class SalesleadCompetitorController {

    @Resource
    private SalesleadCompetitorService salesleadCompetitorService;

    @Resource
    private SalesleadCompetitorMapper salesleadCompetitorMapper;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中竞争对手的报价")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:create')")
    public CommonResult<Long> createSalesleadCompetitor(@Valid @RequestBody SalesleadCompetitorCreateReqVO createReqVO) {
        return success(salesleadCompetitorService.createSalesleadCompetitor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中竞争对手的报价")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:update')")
    public CommonResult<Boolean> updateSalesleadCompetitor(@Valid @RequestBody SalesleadCompetitorUpdateReqVO updateReqVO) {
        salesleadCompetitorService.updateSalesleadCompetitor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除销售线索中竞争对手的报价")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:delete')")
    public CommonResult<Boolean> deleteSalesleadCompetitor(@RequestParam("id") Long id) {
        salesleadCompetitorService.deleteSalesleadCompetitor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得销售线索中竞争对手的报价")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:query')")
    public CommonResult<SalesleadCompetitorRespVO> getSalesleadCompetitor(@RequestParam("id") Long id) {
            Optional<SalesleadCompetitor> salesleadCompetitor = salesleadCompetitorService.getSalesleadCompetitor(id);
        return success(salesleadCompetitor.map(salesleadCompetitorMapper::toDto).orElseThrow(() -> exception(SALESLEAD_COMPETITOR_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得销售线索中竞争对手的报价列表")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:query')")
    public CommonResult<PageResult<SalesleadCompetitorRespVO>> getSalesleadCompetitorPage(@Valid SalesleadCompetitorPageReqVO pageVO, @Valid SalesleadCompetitorPageOrder orderV0) {
        PageResult<SalesleadCompetitor> pageResult = salesleadCompetitorService.getSalesleadCompetitorPage(pageVO, orderV0);
        return success(salesleadCompetitorMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中竞争对手的报价 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-competitor:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadCompetitorExcel(@Valid SalesleadCompetitorExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<SalesleadCompetitor> list = salesleadCompetitorService.getSalesleadCompetitorList(exportReqVO);
        // 导出 Excel
        List<SalesleadCompetitorExcelVO> excelData = salesleadCompetitorMapper.toExcelList(list);
        ExcelUtils.write(response, "销售线索中竞争对手的报价.xls", "数据", SalesleadCompetitorExcelVO.class, excelData);
    }

}

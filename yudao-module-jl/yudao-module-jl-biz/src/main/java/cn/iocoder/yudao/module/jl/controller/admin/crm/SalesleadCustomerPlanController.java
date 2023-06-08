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
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadCustomerPlanMapper;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadCustomerPlanService;

@Tag(name = "管理后台 - 销售线索中的客户方案")
@RestController
@RequestMapping("/jl/saleslead-customer-plan")
@Validated
public class SalesleadCustomerPlanController {

    @Resource
    private SalesleadCustomerPlanService salesleadCustomerPlanService;

    @Resource
    private SalesleadCustomerPlanMapper salesleadCustomerPlanMapper;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中的客户方案")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:create')")
    public CommonResult<Long> createSalesleadCustomerPlan(@Valid @RequestBody SalesleadCustomerPlanCreateReqVO createReqVO) {
        return success(salesleadCustomerPlanService.createSalesleadCustomerPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中的客户方案")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:update')")
    public CommonResult<Boolean> updateSalesleadCustomerPlan(@Valid @RequestBody SalesleadCustomerPlanUpdateReqVO updateReqVO) {
        salesleadCustomerPlanService.updateSalesleadCustomerPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除销售线索中的客户方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:delete')")
    public CommonResult<Boolean> deleteSalesleadCustomerPlan(@RequestParam("id") Long id) {
        salesleadCustomerPlanService.deleteSalesleadCustomerPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得销售线索中的客户方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:query')")
    public CommonResult<SalesleadCustomerPlanRespVO> getSalesleadCustomerPlan(@RequestParam("id") Long id) {
            Optional<SalesleadCustomerPlan> salesleadCustomerPlan = salesleadCustomerPlanService.getSalesleadCustomerPlan(id);
        return success(salesleadCustomerPlan.map(salesleadCustomerPlanMapper::toDto).orElseThrow(() -> exception(SALESLEAD_CUSTOMER_PLAN_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得销售线索中的客户方案列表")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:query')")
    public CommonResult<PageResult<SalesleadCustomerPlanRespVO>> getSalesleadCustomerPlanPage(@Valid SalesleadCustomerPlanPageReqVO pageVO, @Valid SalesleadCustomerPlanPageOrder orderV0) {
        PageResult<SalesleadCustomerPlan> pageResult = salesleadCustomerPlanService.getSalesleadCustomerPlanPage(pageVO, orderV0);
        return success(salesleadCustomerPlanMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中的客户方案 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-customer-plan:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadCustomerPlanExcel(@Valid SalesleadCustomerPlanExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<SalesleadCustomerPlan> list = salesleadCustomerPlanService.getSalesleadCustomerPlanList(exportReqVO);
        // 导出 Excel
        List<SalesleadCustomerPlanExcelVO> excelData = salesleadCustomerPlanMapper.toExcelList(list);
        ExcelUtils.write(response, "销售线索中的客户方案.xls", "数据", SalesleadCustomerPlanExcelVO.class, excelData);
    }

}

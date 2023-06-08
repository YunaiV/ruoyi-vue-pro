package cn.iocoder.yudao.module.jl.controller.admin.crm;

import cn.iocoder.yudao.module.jl.service.crm.CustomerService;
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
import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadMapper;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadService;

@Tag(name = "管理后台 - 销售线索")
@RestController
@RequestMapping("/jl/saleslead")
@Validated
public class SalesleadController {

    @Resource
    private SalesleadService salesleadService;

    @Resource
    private CustomerService customerService;

    @Resource
    private SalesleadMapper salesleadMapper;

//    @PostMapping("/create")
//    @Operation(summary = "创建销售线索")
//    @PreAuthorize("@ss.hasPermission('jl:saleslead:create')")
//    public CommonResult<Long> createSaleslead(@Valid @RequestBody SalesleadCreateReqVO createReqVO) {
//        Long salesLeadId = salesleadService.createSaleslead(createReqVO);
//
//        // 给客户添加最近的销售线索
//        customerService.bindLastSaleslead(createReqVO.getCustomerId(), salesLeadId);
//        return success(salesLeadId);
//    }

    @PutMapping("/save")
    @Operation(summary = "保存销售线索")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:update')")
    public CommonResult<Boolean> updateSaleslead(@Valid @RequestBody SalesleadUpdateReqVO updateReqVO) {
        salesleadService.updateSaleslead(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除销售线索")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead:delete')")
    public CommonResult<Boolean> deleteSaleslead(@RequestParam("id") Long id) {
        salesleadService.deleteSaleslead(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得销售线索")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<SalesleadRespVO> getSaleslead(@RequestParam("id") Long id) {
            Optional<Saleslead> saleslead = salesleadService.getSaleslead(id);
        return success(saleslead.map(salesleadMapper::toDto).orElseThrow(() -> exception(SALESLEAD_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得销售线索列表")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<PageResult<SalesleadRespVO>> getSalesleadPage(@Valid SalesleadPageReqVO pageVO, @Valid SalesleadPageOrder orderV0) {
        PageResult<Saleslead> pageResult = salesleadService.getSalesleadPage(pageVO, orderV0);
        return success(salesleadMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadExcel(@Valid SalesleadExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<Saleslead> list = salesleadService.getSalesleadList(exportReqVO);
        // 导出 Excel
        List<SalesleadExcelVO> excelData = salesleadMapper.toExcelList(list);
        ExcelUtils.write(response, "销售线索.xls", "数据", SalesleadExcelVO.class, excelData);
    }

}

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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;
import cn.iocoder.yudao.module.jl.convert.crm.SalesleadConvert;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadService;

@Tag(name = "管理后台 - 销售线索")
@RestController
@RequestMapping("/jl/saleslead")
@Validated
public class SalesleadController {

    @Resource
    private SalesleadService salesleadService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:create')")
    public CommonResult<Long> createSaleslead(@Valid @RequestBody SalesleadCreateReqVO createReqVO) {
        return success(salesleadService.createSaleslead(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:update')")
    public CommonResult<Boolean> updateSaleslead(@Valid @RequestBody SalesleadUpdateReqVO updateReqVO) {
        salesleadService.updateSaleslead(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead:delete')")
    public CommonResult<Boolean> deleteSaleslead(@RequestParam("id") Long id) {
        salesleadService.deleteSaleslead(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<SalesleadRespVO> getSaleslead(@RequestParam("id") Long id) {
        SalesleadDO saleslead = salesleadService.getSaleslead(id);
        return success(SalesleadConvert.INSTANCE.convert(saleslead));
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<List<SalesleadRespVO>> getSalesleadList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesleadDO> list = salesleadService.getSalesleadList(ids);
        return success(SalesleadConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索分页")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<PageResult<SalesleadRespVO>> getSalesleadPage(@Valid SalesleadPageReqVO pageVO) {
        PageResult<SalesleadDO> pageResult = salesleadService.getSalesleadPage(pageVO);
        return success(SalesleadConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadExcel(@Valid SalesleadExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SalesleadDO> list = salesleadService.getSalesleadList(exportReqVO);
        // 导出 Excel
        List<SalesleadExcelVO> datas = SalesleadConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索.xls", "数据", SalesleadExcelVO.class, datas);
    }

}

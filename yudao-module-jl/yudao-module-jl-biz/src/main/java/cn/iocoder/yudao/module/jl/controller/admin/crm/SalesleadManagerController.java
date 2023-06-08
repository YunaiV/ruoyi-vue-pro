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
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadManager;
import cn.iocoder.yudao.module.jl.mapper.crm.SalesleadManagerMapper;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadManagerService;

@Tag(name = "管理后台 - 销售线索中的项目售前支持人员")
@RestController
@RequestMapping("/jl/saleslead-manager")
@Validated
public class SalesleadManagerController {

    @Resource
    private SalesleadManagerService salesleadManagerService;

    @Resource
    private SalesleadManagerMapper salesleadManagerMapper;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索中的项目售前支持人员")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:create')")
    public CommonResult<Long> createSalesleadManager(@Valid @RequestBody SalesleadManagerCreateReqVO createReqVO) {
        return success(salesleadManagerService.createSalesleadManager(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索中的项目售前支持人员")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:update')")
    public CommonResult<Boolean> updateSalesleadManager(@Valid @RequestBody SalesleadManagerUpdateReqVO updateReqVO) {
        salesleadManagerService.updateSalesleadManager(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除销售线索中的项目售前支持人员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:delete')")
    public CommonResult<Boolean> deleteSalesleadManager(@RequestParam("id") Long id) {
        salesleadManagerService.deleteSalesleadManager(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得销售线索中的项目售前支持人员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:query')")
    public CommonResult<SalesleadManagerRespVO> getSalesleadManager(@RequestParam("id") Long id) {
            Optional<SalesleadManager> salesleadManager = salesleadManagerService.getSalesleadManager(id);
        return success(salesleadManager.map(salesleadManagerMapper::toDto).orElseThrow(() -> exception(SALESLEAD_MANAGER_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得销售线索中的项目售前支持人员列表")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:query')")
    public CommonResult<PageResult<SalesleadManagerRespVO>> getSalesleadManagerPage(@Valid SalesleadManagerPageReqVO pageVO, @Valid SalesleadManagerPageOrder orderV0) {
        PageResult<SalesleadManager> pageResult = salesleadManagerService.getSalesleadManagerPage(pageVO, orderV0);
        return success(salesleadManagerMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索中的项目售前支持人员 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead-manager:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadManagerExcel(@Valid SalesleadManagerExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<SalesleadManager> list = salesleadManagerService.getSalesleadManagerList(exportReqVO);
        // 导出 Excel
        List<SalesleadManagerExcelVO> excelData = salesleadManagerMapper.toExcelList(list);
        ExcelUtils.write(response, "销售线索中的项目售前支持人员.xls", "数据", SalesleadManagerExcelVO.class, excelData);
    }

}

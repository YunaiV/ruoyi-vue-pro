package cn.iocoder.yudao.module.jl.controller.admin.laboratory;

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

import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.Supply;
import cn.iocoder.yudao.module.jl.mapper.laboratory.SupplyMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.SupplyService;

@Tag(name = "管理后台 - 实验物资")
@RestController
@RequestMapping("/jl/supply")
@Validated
public class SupplyController {

    @Resource
    private SupplyService supplyService;

    @Resource
    private SupplyMapper supplyMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验物资")
    @PreAuthorize("@ss.hasPermission('jl:supply:create')")
    public CommonResult<Long> createSupply(@Valid @RequestBody SupplyCreateReqVO createReqVO) {
        return success(supplyService.createSupply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验物资")
    @PreAuthorize("@ss.hasPermission('jl:supply:update')")
    public CommonResult<Boolean> updateSupply(@Valid @RequestBody SupplyUpdateReqVO updateReqVO) {
        supplyService.updateSupply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验物资")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:supply:delete')")
    public CommonResult<Boolean> deleteSupply(@RequestParam("id") Long id) {
        supplyService.deleteSupply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验物资")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:supply:query')")
    public CommonResult<SupplyRespVO> getSupply(@RequestParam("id") Long id) {
            Optional<Supply> supply = supplyService.getSupply(id);
        return success(supply.map(supplyMapper::toDto).orElseThrow(() -> exception(SUPPLY_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验物资列表")
    @PreAuthorize("@ss.hasPermission('jl:supply:query')")
    public CommonResult<PageResult<SupplyRespVO>> getSupplyPage(@Valid SupplyPageReqVO pageVO, @Valid SupplyPageOrder orderV0) {
        PageResult<Supply> pageResult = supplyService.getSupplyPage(pageVO, orderV0);
        return success(supplyMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验物资 Excel")
    @PreAuthorize("@ss.hasPermission('jl:supply:export')")
    @OperateLog(type = EXPORT)
    public void exportSupplyExcel(@Valid SupplyExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<Supply> list = supplyService.getSupplyList(exportReqVO);
        // 导出 Excel
        List<SupplyExcelVO> excelData = supplyMapper.toExcelList(list);
        ExcelUtils.write(response, "实验物资.xls", "数据", SupplyExcelVO.class, excelData);
    }

}

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
import cn.iocoder.yudao.module.jl.entity.laboratory.LabSupply;
import cn.iocoder.yudao.module.jl.mapper.laboratory.LabSupplyMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.LabSupplyService;

@Tag(name = "管理后台 - 实验物资")
@RestController
@RequestMapping("/jl/lab-supply")
@Validated
public class LabSupplyController {

    @Resource
    private LabSupplyService labSupplyService;

    @Resource
    private LabSupplyMapper labSupplyMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验物资")
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:create')")
    public CommonResult<Long> createLabSupply(@Valid @RequestBody LabSupplyCreateReqVO createReqVO) {
        return success(labSupplyService.createLabSupply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验物资")
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:update')")
    public CommonResult<Boolean> updateLabSupply(@Valid @RequestBody LabSupplyUpdateReqVO updateReqVO) {
        labSupplyService.updateLabSupply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验物资")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:delete')")
    public CommonResult<Boolean> deleteLabSupply(@RequestParam("id") Long id) {
        labSupplyService.deleteLabSupply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验物资")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:query')")
    public CommonResult<LabSupplyRespVO> getLabSupply(@RequestParam("id") Long id) {
            Optional<LabSupply> labSupply = labSupplyService.getLabSupply(id);
        return success(labSupply.map(labSupplyMapper::toDto).orElseThrow(() -> exception(LAB_SUPPLY_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验物资列表")
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:query')")
    public CommonResult<PageResult<LabSupplyRespVO>> getLabSupplyPage(@Valid LabSupplyPageReqVO pageVO, @Valid LabSupplyPageOrder orderV0) {
        PageResult<LabSupply> pageResult = labSupplyService.getLabSupplyPage(pageVO, orderV0);
        return success(labSupplyMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验物资 Excel")
    @PreAuthorize("@ss.hasPermission('jl:lab-supply:export')")
    @OperateLog(type = EXPORT)
    public void exportLabSupplyExcel(@Valid LabSupplyExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<LabSupply> list = labSupplyService.getLabSupplyList(exportReqVO);
        // 导出 Excel
        List<LabSupplyExcelVO> excelData = labSupplyMapper.toExcelList(list);
        ExcelUtils.write(response, "实验物资.xls", "数据", LabSupplyExcelVO.class, excelData);
    }

}

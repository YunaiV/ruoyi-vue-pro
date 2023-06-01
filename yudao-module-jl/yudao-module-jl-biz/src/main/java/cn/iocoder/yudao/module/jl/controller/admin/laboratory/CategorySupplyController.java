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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySupplyMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.CategorySupplyService;

@Tag(name = "管理后台 - 实验名目的物资")
@RestController
@RequestMapping("/jl/category-supply")
@Validated
public class CategorySupplyController {

    @Resource
    private CategorySupplyService categorySupplyService;

    @Resource
    private CategorySupplyMapper categorySupplyMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验名目的物资")
    @PreAuthorize("@ss.hasPermission('jl:category-supply:create')")
    public CommonResult<Long> createCategorySupply(@Valid @RequestBody CategorySupplyCreateReqVO createReqVO) {
        return success(categorySupplyService.createCategorySupply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验名目的物资")
    @PreAuthorize("@ss.hasPermission('jl:category-supply:update')")
    public CommonResult<Boolean> updateCategorySupply(@Valid @RequestBody CategorySupplyUpdateReqVO updateReqVO) {
        categorySupplyService.updateCategorySupply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验名目的物资")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:category-supply:delete')")
    public CommonResult<Boolean> deleteCategorySupply(@RequestParam("id") Long id) {
        categorySupplyService.deleteCategorySupply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验名目的物资")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:category-supply:query')")
    public CommonResult<CategorySupplyRespVO> getCategorySupply(@RequestParam("id") Long id) {
            Optional<CategorySupply> categorySupply = categorySupplyService.getCategorySupply(id);
        return success(categorySupply.map(categorySupplyMapper::toDto).orElseThrow(() -> exception(CATEGORY_SUPPLY_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验名目的物资列表")
    @PreAuthorize("@ss.hasPermission('jl:category-supply:query')")
    public CommonResult<PageResult<CategorySupplyRespVO>> getCategorySupplyPage(@Valid CategorySupplyPageReqVO pageVO, @Valid CategorySupplyPageOrder orderV0) {
        PageResult<CategorySupply> pageResult = categorySupplyService.getCategorySupplyPage(pageVO, orderV0);
        return success(categorySupplyMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验名目的物资 Excel")
    @PreAuthorize("@ss.hasPermission('jl:category-supply:export')")
    @OperateLog(type = EXPORT)
    public void exportCategorySupplyExcel(@Valid CategorySupplyExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<CategorySupply> list = categorySupplyService.getCategorySupplyList(exportReqVO);
        // 导出 Excel
        List<CategorySupplyExcelVO> excelData = categorySupplyMapper.toExcelList(list);
        ExcelUtils.write(response, "实验名目的物资.xls", "数据", CategorySupplyExcelVO.class, excelData);
    }

}

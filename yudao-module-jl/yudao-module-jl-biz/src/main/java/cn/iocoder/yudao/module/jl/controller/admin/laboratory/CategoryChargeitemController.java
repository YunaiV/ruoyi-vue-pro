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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import cn.iocoder.yudao.module.jl.mapper.laboratory.CategoryChargeitemMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.CategoryChargeitemService;

@Tag(name = "管理后台 - 实验名目的收费项")
@RestController
@RequestMapping("/jl/category-chargeitem")
@Validated
public class CategoryChargeitemController {

    @Resource
    private CategoryChargeitemService categoryChargeitemService;

    @Resource
    private CategoryChargeitemMapper categoryChargeitemMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验名目的收费项")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:create')")
    public CommonResult<Long> createCategoryChargeitem(@Valid @RequestBody CategoryChargeitemCreateReqVO createReqVO) {
        return success(categoryChargeitemService.createCategoryChargeitem(createReqVO));
    }

    @PostMapping("/save")
    @Operation(summary = "全量保存")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:create')")
    public CommonResult<Boolean> saveCategoryChargeitem(@Valid @RequestBody CategoryChargeItemSaveReqVO saveReqVO) {
        return success(categoryChargeitemService.saveCategoryChargeItem(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验名目的收费项")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:update')")
    public CommonResult<Boolean> updateCategoryChargeitem(@Valid @RequestBody CategoryChargeitemUpdateReqVO updateReqVO) {
        categoryChargeitemService.updateCategoryChargeitem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验名目的收费项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:delete')")
    public CommonResult<Boolean> deleteCategoryChargeitem(@RequestParam("id") Long id) {
        categoryChargeitemService.deleteCategoryChargeitem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验名目的收费项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:query')")
    public CommonResult<CategoryChargeitemRespVO> getCategoryChargeitem(@RequestParam("id") Long id) {
            Optional<CategoryChargeitem> categoryChargeitem = categoryChargeitemService.getCategoryChargeitem(id);
        return success(categoryChargeitem.map(categoryChargeitemMapper::toDto).orElseThrow(() -> exception(CATEGORY_CHARGEITEM_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验名目的收费项列表")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:query')")
    public CommonResult<PageResult<CategoryChargeitemRespVO>> getCategoryChargeitemPage(@Valid CategoryChargeitemPageReqVO pageVO, @Valid CategoryChargeitemPageOrder orderV0) {
        PageResult<CategoryChargeitem> pageResult = categoryChargeitemService.getCategoryChargeitemPage(pageVO, orderV0);
        return success(categoryChargeitemMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验名目的收费项 Excel")
    @PreAuthorize("@ss.hasPermission('jl:category-chargeitem:export')")
    @OperateLog(type = EXPORT)
    public void exportCategoryChargeitemExcel(@Valid CategoryChargeitemExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<CategoryChargeitem> list = categoryChargeitemService.getCategoryChargeitemList(exportReqVO);
        // 导出 Excel
        List<CategoryChargeitemExcelVO> excelData = categoryChargeitemMapper.toExcelList(list);
        ExcelUtils.write(response, "实验名目的收费项.xls", "数据", CategoryChargeitemExcelVO.class, excelData);
    }

}

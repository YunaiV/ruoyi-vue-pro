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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryReference;
import cn.iocoder.yudao.module.jl.mapper.laboratory.CategoryReferenceMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.CategoryReferenceService;

@Tag(name = "管理后台 - 实验名目的参考资料")
@RestController
@RequestMapping("/jl/category-reference")
@Validated
public class CategoryReferenceController {

    @Resource
    private CategoryReferenceService categoryReferenceService;

    @Resource
    private CategoryReferenceMapper categoryReferenceMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验名目的参考资料")
    @PreAuthorize("@ss.hasPermission('jl:category-reference:create')")
    public CommonResult<Long> createCategoryReference(@Valid @RequestBody CategoryReferenceCreateReqVO createReqVO) {
        return success(categoryReferenceService.createCategoryReference(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验名目的参考资料")
    @PreAuthorize("@ss.hasPermission('jl:category-reference:update')")
    public CommonResult<Boolean> updateCategoryReference(@Valid @RequestBody CategoryReferenceUpdateReqVO updateReqVO) {
        categoryReferenceService.updateCategoryReference(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验名目的参考资料")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:category-reference:delete')")
    public CommonResult<Boolean> deleteCategoryReference(@RequestParam("id") Long id) {
        categoryReferenceService.deleteCategoryReference(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验名目的参考资料")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:category-reference:query')")
    public CommonResult<CategoryReferenceRespVO> getCategoryReference(@RequestParam("id") Long id) {
            Optional<CategoryReference> categoryReference = categoryReferenceService.getCategoryReference(id);
        return success(categoryReference.map(categoryReferenceMapper::toDto).orElseThrow(() -> exception(CATEGORY_REFERENCE_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验名目的参考资料列表")
    @PreAuthorize("@ss.hasPermission('jl:category-reference:query')")
    public CommonResult<PageResult<CategoryReferenceRespVO>> getCategoryReferencePage(@Valid CategoryReferencePageReqVO pageVO, @Valid CategoryReferencePageOrder orderV0) {
        PageResult<CategoryReference> pageResult = categoryReferenceService.getCategoryReferencePage(pageVO, orderV0);
        return success(categoryReferenceMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验名目的参考资料 Excel")
    @PreAuthorize("@ss.hasPermission('jl:category-reference:export')")
    @OperateLog(type = EXPORT)
    public void exportCategoryReferenceExcel(@Valid CategoryReferenceExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<CategoryReference> list = categoryReferenceService.getCategoryReferenceList(exportReqVO);
        // 导出 Excel
        List<CategoryReferenceExcelVO> excelData = categoryReferenceMapper.toExcelList(list);
        ExcelUtils.write(response, "实验名目的参考资料.xls", "数据", CategoryReferenceExcelVO.class, excelData);
    }

}

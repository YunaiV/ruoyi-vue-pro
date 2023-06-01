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
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import cn.iocoder.yudao.module.jl.mapper.laboratory.CategorySopMapper;
import cn.iocoder.yudao.module.jl.service.laboratory.CategorySopService;

@Tag(name = "管理后台 - 实验名目的操作SOP")
@RestController
@RequestMapping("/jl/category-sop")
@Validated
public class CategorySopController {

    @Resource
    private CategorySopService categorySopService;

    @Resource
    private CategorySopMapper categorySopMapper;

    @PostMapping("/create")
    @Operation(summary = "创建实验名目的操作SOP")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:create')")
    public CommonResult<Long> createCategorySop(@Valid @RequestBody CategorySopCreateReqVO createReqVO) {
        return success(categorySopService.createCategorySop(createReqVO));
    }

    @PostMapping("/save")
    @Operation(summary = "全量保存")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:create')")
    public CommonResult<Boolean> saveCategorySop(@Valid @RequestBody CategorySopSaveReqVO saveReqVO) {
        return success(categorySopService.saveAllCategorySop(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新实验名目的操作SOP")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:update')")
    public CommonResult<Boolean> updateCategorySop(@Valid @RequestBody CategorySopUpdateReqVO updateReqVO) {
        categorySopService.updateCategorySop(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除实验名目的操作SOP")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:category-sop:delete')")
    public CommonResult<Boolean> deleteCategorySop(@RequestParam("id") Long id) {
        categorySopService.deleteCategorySop(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得实验名目的操作SOP")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:query')")
    public CommonResult<CategorySopRespVO> getCategorySop(@RequestParam("id") Long id) {
            Optional<CategorySop> categorySop = categorySopService.getCategorySop(id);
        return success(categorySop.map(categorySopMapper::toDto).orElseThrow(() -> exception(CATEGORY_SOP_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得实验名目的操作SOP列表")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:query')")
    public CommonResult<PageResult<CategorySopRespVO>> getCategorySopPage(@Valid CategorySopPageReqVO pageVO, @Valid CategorySopPageOrder orderV0) {
        PageResult<CategorySop> pageResult = categorySopService.getCategorySopPage(pageVO, orderV0);
        return success(categorySopMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出实验名目的操作SOP Excel")
    @PreAuthorize("@ss.hasPermission('jl:category-sop:export')")
    @OperateLog(type = EXPORT)
    public void exportCategorySopExcel(@Valid CategorySopExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<CategorySop> list = categorySopService.getCategorySopList(exportReqVO);
        // 导出 Excel
        List<CategorySopExcelVO> excelData = categorySopMapper.toExcelList(list);
        ExcelUtils.write(response, "实验名目的操作SOP.xls", "数据", CategorySopExcelVO.class, excelData);
    }

}

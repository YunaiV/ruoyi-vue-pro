package cn.iocoder.yudao.module.product.controller.admin.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.product.convert.category.CategoryConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.product.service.category.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 商品分类")
@RestController
@RequestMapping("/product/category")
@Validated
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @PostMapping("/create")
    @ApiOperation("创建商品分类")
    @PreAuthorize("@ss.hasPermission('product:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody CategoryCreateReqVO createReqVO) {
        return success(categoryService.createCategory(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新商品分类")
    @PreAuthorize("@ss.hasPermission('product:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody CategoryUpdateReqVO updateReqVO) {
        categoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除商品分类")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得商品分类")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:category:query')")
    public CommonResult<CategoryRespVO> getCategory(@RequestParam("id") Long id) {
        CategoryDO category = categoryService.getCategory(id);
        return success(CategoryConvert.INSTANCE.convert(category));
    }

    @GetMapping("/listByIds")
    @ApiOperation("获得商品分类列表")
    @PreAuthorize("@ss.hasPermission('product:category:query')")
    public CommonResult<List<CategoryRespVO>> getCategoryList(@RequestParam("ids") Collection<Long> ids) {
        List<CategoryDO> list = categoryService.getCategoryList(ids);
        return success(CategoryConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/listByQuery")
    @ApiOperation("获得商品分类列表")
    @PreAuthorize("@ss.hasPermission('product:category:query')")
    public CommonResult<List<CategoryRespVO>> listByQuery(@Valid CategoryTreeListReqVO treeListReqVO) {
        List<CategoryDO> list = categoryService.getCategoryTreeList(treeListReqVO);
        list.sort(Comparator.comparing(CategoryDO::getSort));
        return success(CategoryConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得商品分类分页")
    @PreAuthorize("@ss.hasPermission('product:category:query')")
    public CommonResult<PageResult<CategoryRespVO>> getCategoryPage(@Valid CategoryPageReqVO pageVO) {
        PageResult<CategoryDO> pageResult = categoryService.getCategoryPage(pageVO);
        return success(CategoryConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出商品分类 Excel")
    @PreAuthorize("@ss.hasPermission('product:category:export')")
    @OperateLog(type = EXPORT)
    public void exportCategoryExcel(@Valid CategoryExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CategoryDO> list = categoryService.getCategoryList(exportReqVO);
        // 导出 Excel
        List<CategoryExcelVO> datas = CategoryConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商品分类.xls", "数据", CategoryExcelVO.class, datas);
    }

}

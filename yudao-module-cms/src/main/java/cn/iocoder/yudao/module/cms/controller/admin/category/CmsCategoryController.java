package cn.iocoder.yudao.module.cms.controller.admin.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.cms.convert.category.CmsCategoryConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;
import cn.iocoder.yudao.module.cms.service.category.CmsCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - CMS Category Management")
@RestController
@RequestMapping("/cms/admin/categories")
@Validated
public class CmsCategoryController {

    @Resource
    private CmsCategoryService categoryService;

    @Resource
    private CmsCategoryConvert categoryConvert;

    @PostMapping("/create")
    @Operation(summary = "Create a new category")
    @PreAuthorize("@ss.hasPermission('cms:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody CmsCategoryCreateReqVO createReqVO) {
        Long categoryId = categoryService.createCategory(createReqVO);
        return success(categoryId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update an existing category")
    @PreAuthorize("@ss.hasPermission('cms:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody CmsCategoryUpdateReqVO updateReqVO) {
        categoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a category by ID")
    @Parameter(name = "id", description = "Category ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get a category by ID")
    @Parameter(name = "id", description = "Category ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:category:query')")
    public CommonResult<CmsCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        CmsCategoryDO category = categoryService.getCategory(id);
        return success(categoryConvert.convert(category));
    }

    @GetMapping("/page")
    @Operation(summary = "Get category page")
    @PreAuthorize("@ss.hasPermission('cms:category:query')")
    public CommonResult<PageResult<CmsCategoryRespVO>> getCategoryPage(@Valid CmsCategoryPageReqVO pageVO) {
        PageResult<CmsCategoryDO> pageResult = categoryService.getCategoryPage(pageVO);
        return success(categoryConvert.convertPage(pageResult));
    }

    @GetMapping("/list-simple")
    @Operation(summary = "Get simple category list (for dropdowns, etc.)")
    @PreAuthorize("@ss.hasPermission('cms:category:query')")
    public CommonResult<List<CmsCategorySimpleRespVO>> getSimpleCategoryList(CmsCategoryPageReqVO reqVO) {
        // Pass the reqVO if it contains filters like parentId for specific lists
        List<CmsCategoryDO> list = categoryService.getCategoryList(reqVO);
        return success(categoryConvert.convertListSimple(list));
    }
}

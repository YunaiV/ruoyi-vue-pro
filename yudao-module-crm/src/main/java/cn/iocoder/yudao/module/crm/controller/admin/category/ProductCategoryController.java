package cn.iocoder.yudao.module.crm.controller.admin.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategoryPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategoryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.crm.service.category.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 我的商品分类 Controller（学习用）
 * 
 * CI/CD 测试说明：
 * 本文件用于测试企业级开发流程，验证从代码提交到自动部署的完整流程
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 我的商品分类（学习）")
@RestController
@RequestMapping("/crm/my-category")
@Validated
public class ProductCategoryController {

    @Resource
    private ProductCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建商品分类")
    @PreAuthorize("@ss.hasPermission('crm:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody ProductCategorySaveReqVO createReqVO) {
        Long categoryId = categoryService.createCategory(createReqVO);
        return success(categoryId);
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品分类")
    @PreAuthorize("@ss.hasPermission('crm:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody ProductCategorySaveReqVO updateReqVO) {
        categoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('crm:category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商品分类详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('crm:category:query')")
    public CommonResult<ProductCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        ProductCategoryDO category = categoryService.getCategory(id);
        return success(BeanUtils.toBean(category, ProductCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品分类分页列表")
    @PreAuthorize("@ss.hasPermission('crm:category:query')")
    public CommonResult<PageResult<ProductCategoryRespVO>> getCategoryPage(@Valid ProductCategoryPageReqVO pageReqVO) {
        PageResult<ProductCategoryDO> pageResult = categoryService.getCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductCategoryRespVO.class));
    }

    @GetMapping("/list-by-parent-id")
    @Operation(summary = "根据父分类ID查询子分类列表")
    @Parameter(name = "parentId", description = "父分类ID", required = true, example = "0")
    public CommonResult<List<ProductCategoryRespVO>> getCategoryListByParentId(@RequestParam("parentId") Long parentId) {
        List<ProductCategoryDO> list = categoryService.getCategoryListByParentId(parentId);
        return success(BeanUtils.toBean(list, ProductCategoryRespVO.class));
    }

}

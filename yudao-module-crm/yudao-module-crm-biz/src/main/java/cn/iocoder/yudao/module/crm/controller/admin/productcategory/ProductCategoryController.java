package cn.iocoder.yudao.module.crm.controller.admin.productcategory;

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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.productcategory.ProductCategoryDO;
import cn.iocoder.yudao.module.crm.convert.productcategory.ProductCategoryConvert;
import cn.iocoder.yudao.module.crm.service.productcategory.ProductCategoryService;

@Tag(name = "管理后台 - 产品分类")
@RestController
@RequestMapping("/crm/product-category")
@Validated
public class ProductCategoryController {

    @Resource
    private ProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品分类")
    @PreAuthorize("@ss.hasPermission('crm:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody ProductCategoryCreateReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品分类")
    @PreAuthorize("@ss.hasPermission('crm:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody ProductCategoryUpdateReqVO updateReqVO) {
        productCategoryService.updateProductCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:product-category:delete')")
    public CommonResult<Boolean> deleteProductCategory(@RequestParam("id") Long id) {
        productCategoryService.deleteProductCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:product-category:query')")
    public CommonResult<ProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        ProductCategoryDO productCategory = productCategoryService.getProductCategory(id);
        return success(ProductCategoryConvert.INSTANCE.convert(productCategory));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产品分类列表")
    @PreAuthorize("@ss.hasPermission('crm:product-category:query')")
    public CommonResult<List<ProductCategoryRespVO>> getProductCategoryList(@Valid ProductCategoryListReqVO treeListReqVO) {
        List<ProductCategoryDO> list = productCategoryService.getProductCategoryList(treeListReqVO);
        return success(ProductCategoryConvert.INSTANCE.convertList(list));
    }

}

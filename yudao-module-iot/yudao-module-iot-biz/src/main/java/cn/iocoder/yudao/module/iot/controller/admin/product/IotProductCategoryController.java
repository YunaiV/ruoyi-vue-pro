package cn.iocoder.yudao.module.iot.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategorySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 产品分类")
@RestController
@RequestMapping("/iot/product-category")
@Validated
public class IotProductCategoryController {

    @Resource
    private IotProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建IoT 产品分类")
    @PreAuthorize("@ss.hasPermission('iot:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody IotProductCategorySaveReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新IoT 产品分类")
    @PreAuthorize("@ss.hasPermission('iot:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody IotProductCategorySaveReqVO updateReqVO) {
        productCategoryService.updateProductCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IoT 产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product-category:delete')")
    public CommonResult<Boolean> deleteProductCategory(@RequestParam("id") Long id) {
        productCategoryService.deleteProductCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得IoT 产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<IotProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        IotProductCategoryDO productCategory = productCategoryService.getProductCategory(id);
        return success(BeanUtils.toBean(productCategory, IotProductCategoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得IoT 产品分类分页")
    @PreAuthorize("@ss.hasPermission('iot:product-category:query')")
    public CommonResult<PageResult<IotProductCategoryRespVO>> getProductCategoryPage(@Valid IotProductCategoryPageReqVO pageReqVO) {
        PageResult<IotProductCategoryDO> pageResult = productCategoryService.getProductCategoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotProductCategoryRespVO.class));
    }

}
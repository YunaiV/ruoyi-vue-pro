package cn.iocoder.yudao.module.crm.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryListReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.service.product.CrmProductCategoryService;
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

@Tag(name = "管理后台 - CRM 产品分类")
@RestController
@RequestMapping("/crm/product-category")
@Validated
public class CrmProductCategoryController {

    @Resource
    private CrmProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品分类")
    @PreAuthorize("@ss.hasPermission('crm:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody CrmProductCategoryCreateReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品分类")
    @PreAuthorize("@ss.hasPermission('crm:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody CrmProductCategoryCreateReqVO updateReqVO) {
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
    public CommonResult<CrmProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        CrmProductCategoryDO category = productCategoryService.getProductCategory(id);
        return success(BeanUtils.toBean(category, CrmProductCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产品分类列表")
    @PreAuthorize("@ss.hasPermission('crm:product-category:query')")
    public CommonResult<List<CrmProductCategoryRespVO>> getProductCategoryList(@Valid CrmProductCategoryListReqVO listReqVO) {
        List<CrmProductCategoryDO> list = productCategoryService.getProductCategoryList(listReqVO);
        return success(BeanUtils.toBean(list, CrmProductCategoryRespVO.class));
    }

}

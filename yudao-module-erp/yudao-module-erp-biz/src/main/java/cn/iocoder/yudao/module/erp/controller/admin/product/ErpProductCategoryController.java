package cn.iocoder.yudao.module.erp.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.category.ErpProductCategoryListReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.category.ErpProductCategoryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.category.ErpProductCategorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - ERP 产品分类")
@RestController
@RequestMapping("/erp/product-category")
@Validated
public class ErpProductCategoryController {

    @Resource
    private ErpProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建ERP 产品分类")
    @PreAuthorize("@ss.hasPermission('erp:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody ErpProductCategorySaveReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP 产品分类")
    @PreAuthorize("@ss.hasPermission('erp:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody ErpProductCategorySaveReqVO updateReqVO) {
        productCategoryService.updateProductCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:product-category:delete')")
    public CommonResult<Boolean> deleteProductCategory(@RequestParam("id") Long id) {
        productCategoryService.deleteProductCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP 产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:product-category:query')")
    public CommonResult<ErpProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        ErpProductCategoryDO productCategory = productCategoryService.getProductCategory(id);
        return success(BeanUtils.toBean(productCategory, ErpProductCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得ERP 产品分类列表")
    @PreAuthorize("@ss.hasPermission('erp:product-category:query')")
    public CommonResult<List<ErpProductCategoryRespVO>> getProductCategoryList(@Valid ErpProductCategoryListReqVO listReqVO) {
        List<ErpProductCategoryDO> list = productCategoryService.getProductCategoryList(listReqVO);
        return success(BeanUtils.toBean(list, ErpProductCategoryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 产品分类 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product-category:export')")
    @OperateLog(type = EXPORT)
    public void exportProductCategoryExcel(@Valid ErpProductCategoryListReqVO listReqVO,
              HttpServletResponse response) throws IOException {
        List<ErpProductCategoryDO> list = productCategoryService.getProductCategoryList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "ERP 产品分类.xls", "数据", ErpProductCategoryRespVO.class,
                        BeanUtils.toBean(list, ErpProductCategoryRespVO.class));
    }

}
package cn.iocoder.yudao.module.iot.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 产品")
@RestController
@RequestMapping("/iot/product")
@Validated
public class IotProductController {

    @Resource
    private IotProductService productService;
    @Resource
    private IotProductCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('iot:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody IotProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody IotProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新产品状态")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "status", description = "状态", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProductStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        productService.updateProductStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProduct(@RequestParam("id") Long id) {
        IotProductDO product = productService.getProduct(id);
        if (product == null) {
            return success(null);
        }
        // 拼接数据
        IotProductCategoryDO category = categoryService.getProductCategory(product.getCategoryId());
        return success(BeanUtils.toBean(product, IotProductRespVO.class, bean -> {
            if (category != null) {
                bean.setCategoryName(category.getName());
            }
        }));
    }

    @GetMapping("/get-by-key")
    @Operation(summary = "通过 ProductKey 获得产品")
    @Parameter(name = "productKey", description = "产品Key", required = true, example = "abc123")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProductByKey(@RequestParam("productKey") String productKey) {
        IotProductDO product = productService.getProductByProductKey(productKey);
        if (product == null) {
            return success(null);
        }
        // 拼接数据
        IotProductCategoryDO category = categoryService.getProductCategory(product.getCategoryId());
        return success(BeanUtils.toBean(product, IotProductRespVO.class, bean -> {
            if (category != null) {
                bean.setCategoryName(category.getName());
            }
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<PageResult<IotProductRespVO>> getProductPage(@Valid IotProductPageReqVO pageReqVO) {
        PageResult<IotProductDO> pageResult = productService.getProductPage(pageReqVO);
        // 拼接数据
        Map<Long, IotProductCategoryDO> categoryMap = categoryService.getProductCategoryMap(
                convertList(pageResult.getList(), IotProductDO::getCategoryId));
        return success(BeanUtils.toBean(pageResult, IotProductRespVO.class, bean -> {
            MapUtils.findAndThen(categoryMap, bean.getCategoryId(),
                    category -> bean.setCategoryName(category.getName()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('iot:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid IotProductPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        CommonResult<PageResult<IotProductRespVO>> result = getProductPage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品.xls", "数据", IotProductRespVO.class,
                result.getData().getList());
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取产品的精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<IotProductRespVO>> getProductSimpleList() {
        List<IotProductDO> list = productService.getProductList();
        return success(convertList(list, product -> // 只返回 id、name 字段
                new IotProductRespVO().setId(product.getId()).setName(product.getName())
                        .setDeviceType(product.getDeviceType()).setLocationType(product.getLocationType())));
    }

}
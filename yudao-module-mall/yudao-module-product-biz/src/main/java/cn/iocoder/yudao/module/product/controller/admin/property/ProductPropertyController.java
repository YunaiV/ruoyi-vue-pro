package cn.iocoder.yudao.module.product.controller.admin.property;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.*;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 规格名称")
@RestController
@RequestMapping("/product/property")
@Validated
public class ProductPropertyController {

    @Resource
    private ProductPropertyService productPropertyService;

    @PostMapping("/create")
    @Operation(summary = "创建规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:create')")
    public CommonResult<Long> createProperty(@Valid @RequestBody ProductPropertyCreateReqVO createReqVO) {
        return success(productPropertyService.createProperty(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:update')")
    public CommonResult<Boolean> updateProperty(@Valid @RequestBody ProductPropertyUpdateReqVO updateReqVO) {
        productPropertyService.updateProperty(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除规格名称")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('product:property:delete')")
    public CommonResult<Boolean> deleteProperty(@RequestParam("id") Long id) {
        productPropertyService.deleteProperty(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得规格名称")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<ProductPropertyRespVO> getProperty(@RequestParam("id") Long id) {
        return success(productPropertyService.getProperty(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获得规格名称列表")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<ProductPropertyRespVO>> getPropertyList(@Valid ProductPropertyListReqVO listReqVO) {
        return success(productPropertyService.getPropertyList(listReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得规格名称分页")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<PageResult<ProductPropertyRespVO>> getPropertyPage(@Valid ProductPropertyPageReqVO pageVO) {
        return success(productPropertyService.getPropertyPage(pageVO));
    }

    @GetMapping("/listAndValue")
    @Operation(summary = "获得规格名称列表")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<ProductPropertyAndValueRespVO>> getPropertyAndValueList(@Valid ProductPropertyListReqVO listReqVO) {
        return success(productPropertyService.getPropertyAndValueList(listReqVO));
    }

}

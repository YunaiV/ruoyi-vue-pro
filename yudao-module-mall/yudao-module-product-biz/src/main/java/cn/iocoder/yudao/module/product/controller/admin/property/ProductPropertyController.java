package cn.iocoder.yudao.module.product.controller.admin.property;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.*;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 规格名称")
@RestController
@RequestMapping("/product/property")
@Validated
public class ProductPropertyController {

    @Resource
    private ProductPropertyService productPropertyService;

    @PostMapping("/create")
    @ApiOperation("创建规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:create')")
    public CommonResult<Long> createProperty(@Valid @RequestBody ProductPropertyCreateReqVO createReqVO) {
        return success(productPropertyService.createProperty(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:update')")
    public CommonResult<Boolean> updateProperty(@Valid @RequestBody ProductPropertyUpdateReqVO updateReqVO) {
        productPropertyService.updateProperty(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:property:delete')")
    public CommonResult<Boolean> deleteProperty(@RequestParam("id") Long id) {
        productPropertyService.deleteProperty(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<ProductPropertyRespVO> getProperty(@RequestParam("id") Long id) {
        return success(productPropertyService.getProperty(id));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格名称列表")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<ProductPropertyRespVO>> getPropertyList(@Valid ProductPropertyListReqVO listReqVO) {
        return success(productPropertyService.getPropertyList(listReqVO));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格名称分页")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<PageResult<ProductPropertyRespVO>> getPropertyPage(@Valid ProductPropertyPageReqVO pageVO) {
        return success(productPropertyService.getPropertyPage(pageVO));
    }

    @GetMapping("/listAndValue")
    @ApiOperation("获得规格名称列表")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<ProductPropertyAndValueRespVO>> getPropertyAndValueList(@Valid ProductPropertyListReqVO listReqVO) {
        return success(productPropertyService.getPropertyAndValueList(listReqVO));
    }

}

package cn.iocoder.yudao.module.product.controller.admin.property;

import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

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

import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.convert.property.ProductPropertyConvert;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;

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
        return success(productPropertyService.getPropertyResp(id));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格名称列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<ProductPropertyRespVO>> getPropertyList(@RequestParam("ids") Collection<Long> ids) {
        List<ProductPropertyDO> list = productPropertyService.getPropertyList(ids);
        return success(ProductPropertyConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格名称分页")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<PageResult<ProductPropertyRespVO>> getPropertyPage(@Valid ProductPropertyPageReqVO pageVO) {
        return success(productPropertyService.getPropertyListPage(pageVO));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出规格名称 Excel")
    @PreAuthorize("@ss.hasPermission('product:property:export')")
    @OperateLog(type = EXPORT)
    public void exportPropertyExcel(@Valid ProductPropertyExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ProductPropertyDO> list = productPropertyService.getPropertyList(exportReqVO);
        // 导出 Excel
        List<ProductPropertyExcelVO> datas = ProductPropertyConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "规格名称.xls", "数据", ProductPropertyExcelVO.class, datas);
    }

}

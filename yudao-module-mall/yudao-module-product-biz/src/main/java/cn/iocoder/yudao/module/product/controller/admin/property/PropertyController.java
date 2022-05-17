package cn.iocoder.yudao.module.product.controller.admin.property;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

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

import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.property.PropertyDO;
import cn.iocoder.yudao.module.product.convert.property.PropertyConvert;
import cn.iocoder.yudao.module.product.service.property.PropertyService;

@Api(tags = "管理后台 - 规格名称")
@RestController
@RequestMapping("/product/property")
@Validated
public class PropertyController {

    @Resource
    private PropertyService propertyService;

    @PostMapping("/create")
    @ApiOperation("创建规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:create')")
    public CommonResult<Long> createProperty(@Valid @RequestBody PropertyCreateReqVO createReqVO) {
        return success(propertyService.createProperty(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新规格名称")
    @PreAuthorize("@ss.hasPermission('product:property:update')")
    public CommonResult<Boolean> updateProperty(@Valid @RequestBody PropertyUpdateReqVO updateReqVO) {
        propertyService.updateProperty(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:property:delete')")
    public CommonResult<Boolean> deleteProperty(@RequestParam("id") Long id) {
        propertyService.deleteProperty(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<PropertyRespVO> getProperty(@RequestParam("id") Long id) {
        PropertyDO property = propertyService.getProperty(id);
        return success(PropertyConvert.INSTANCE.convert(property));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格名称列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<List<PropertyRespVO>> getPropertyList(@RequestParam("ids") Collection<Long> ids) {
        List<PropertyDO> list = propertyService.getPropertyList(ids);
        return success(PropertyConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格名称分页")
    @PreAuthorize("@ss.hasPermission('product:property:query')")
    public CommonResult<PageResult<PropertyRespVO>> getPropertyPage(@Valid PropertyPageReqVO pageVO) {
        PageResult<PropertyDO> pageResult = propertyService.getPropertyPage(pageVO);
        return success(PropertyConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出规格名称 Excel")
    @PreAuthorize("@ss.hasPermission('product:property:export')")
    @OperateLog(type = EXPORT)
    public void exportPropertyExcel(@Valid PropertyExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PropertyDO> list = propertyService.getPropertyList(exportReqVO);
        // 导出 Excel
        List<PropertyExcelVO> datas = PropertyConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "规格名称.xls", "数据", PropertyExcelVO.class, datas);
    }

}

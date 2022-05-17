package cn.iocoder.yudao.module.product.controller.admin.propertyvalue;

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

import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.PropertyValueDO;
import cn.iocoder.yudao.module.product.convert.propertyvalue.PropertyValueConvert;
import cn.iocoder.yudao.module.product.service.propertyvalue.PropertyValueService;

@Api(tags = "管理后台 - 规格值")
@RestController
@RequestMapping("/product/property-value")
@Validated
public class PropertyValueController {

    @Resource
    private PropertyValueService propertyValueService;

    @PostMapping("/create")
    @ApiOperation("创建规格值")
    @PreAuthorize("@ss.hasPermission('product:property-value:create')")
    public CommonResult<Integer> createPropertyValue(@Valid @RequestBody PropertyValueCreateReqVO createReqVO) {
        return success(propertyValueService.createPropertyValue(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新规格值")
    @PreAuthorize("@ss.hasPermission('product:property-value:update')")
    public CommonResult<Boolean> updatePropertyValue(@Valid @RequestBody PropertyValueUpdateReqVO updateReqVO) {
        propertyValueService.updatePropertyValue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除规格值")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:property-value:delete')")
    public CommonResult<Boolean> deletePropertyValue(@RequestParam("id") Integer id) {
        propertyValueService.deletePropertyValue(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得规格值")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:property-value:query')")
    public CommonResult<PropertyValueRespVO> getPropertyValue(@RequestParam("id") Integer id) {
        PropertyValueDO propertyValue = propertyValueService.getPropertyValue(id);
        return success(PropertyValueConvert.INSTANCE.convert(propertyValue));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格值列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:property-value:query')")
    public CommonResult<List<PropertyValueRespVO>> getPropertyValueList(@RequestParam("ids") Collection<Integer> ids) {
        List<PropertyValueDO> list = propertyValueService.getPropertyValueList(ids);
        return success(PropertyValueConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格值分页")
    @PreAuthorize("@ss.hasPermission('product:property-value:query')")
    public CommonResult<PageResult<PropertyValueRespVO>> getPropertyValuePage(@Valid PropertyValuePageReqVO pageVO) {
        PageResult<PropertyValueDO> pageResult = propertyValueService.getPropertyValuePage(pageVO);
        return success(PropertyValueConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出规格值 Excel")
    @PreAuthorize("@ss.hasPermission('product:property-value:export')")
    @OperateLog(type = EXPORT)
    public void exportPropertyValueExcel(@Valid PropertyValueExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PropertyValueDO> list = propertyValueService.getPropertyValueList(exportReqVO);
        // 导出 Excel
        List<PropertyValueExcelVO> datas = PropertyValueConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "规格值.xls", "数据", PropertyValueExcelVO.class, datas);
    }

}

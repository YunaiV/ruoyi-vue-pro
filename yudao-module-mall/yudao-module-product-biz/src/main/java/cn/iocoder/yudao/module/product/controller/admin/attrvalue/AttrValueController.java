package cn.iocoder.yudao.module.product.controller.admin.attrvalue;

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

import cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrvalue.AttrValueDO;
import cn.iocoder.yudao.module.product.convert.attrvalue.AttrValueConvert;
import cn.iocoder.yudao.module.product.service.attrvalue.AttrValueService;

@Api(tags = "管理后台 - 规格值")
@RestController
@RequestMapping("/product/attr-value")
@Validated
public class AttrValueController {

    @Resource
    private AttrValueService attrValueService;

    @PostMapping("/create")
    @ApiOperation("创建规格值")
    @PreAuthorize("@ss.hasPermission('product:attr-value:create')")
    public CommonResult<Integer> createAttrValue(@Valid @RequestBody AttrValueCreateReqVO createReqVO) {
        return success(attrValueService.createAttrValue(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新规格值")
    @PreAuthorize("@ss.hasPermission('product:attr-value:update')")
    public CommonResult<Boolean> updateAttrValue(@Valid @RequestBody AttrValueUpdateReqVO updateReqVO) {
        attrValueService.updateAttrValue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除规格值")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:attr-value:delete')")
    public CommonResult<Boolean> deleteAttrValue(@RequestParam("id") Integer id) {
        attrValueService.deleteAttrValue(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得规格值")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:attr-value:query')")
    public CommonResult<AttrValueRespVO> getAttrValue(@RequestParam("id") Integer id) {
        AttrValueDO attrValue = attrValueService.getAttrValue(id);
        return success(AttrValueConvert.INSTANCE.convert(attrValue));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格值列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:attr-value:query')")
    public CommonResult<List<AttrValueRespVO>> getAttrValueList(@RequestParam("ids") Collection<Integer> ids) {
        List<AttrValueDO> list = attrValueService.getAttrValueList(ids);
        return success(AttrValueConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格值分页")
    @PreAuthorize("@ss.hasPermission('product:attr-value:query')")
    public CommonResult<PageResult<AttrValueRespVO>> getAttrValuePage(@Valid AttrValuePageReqVO pageVO) {
        PageResult<AttrValueDO> pageResult = attrValueService.getAttrValuePage(pageVO);
        return success(AttrValueConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出规格值 Excel")
    @PreAuthorize("@ss.hasPermission('product:attr-value:export')")
    @OperateLog(type = EXPORT)
    public void exportAttrValueExcel(@Valid AttrValueExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<AttrValueDO> list = attrValueService.getAttrValueList(exportReqVO);
        // 导出 Excel
        List<AttrValueExcelVO> datas = AttrValueConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "规格值.xls", "数据", AttrValueExcelVO.class, datas);
    }

}

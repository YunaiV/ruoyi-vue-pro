package cn.iocoder.yudao.module.product.controller.admin.attrkey;

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

import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;
import cn.iocoder.yudao.module.product.convert.attrkey.AttrKeyConvert;
import cn.iocoder.yudao.module.product.service.attrkey.AttrKeyService;

@Api(tags = "管理后台 - 规格名称")
@RestController
@RequestMapping("/product/attr-key")
@Validated
public class AttrKeyController {

    @Resource
    private AttrKeyService attrKeyService;

    @PostMapping("/create")
    @ApiOperation("创建规格名称")
    @PreAuthorize("@ss.hasPermission('product:attr-key:create')")
    public CommonResult<Integer> createAttrKey(@Valid @RequestBody AttrKeyCreateReqVO createReqVO) {
        return success(attrKeyService.createAttrKey(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新规格名称")
    @PreAuthorize("@ss.hasPermission('product:attr-key:update')")
    public CommonResult<Boolean> updateAttrKey(@Valid @RequestBody AttrKeyUpdateReqVO updateReqVO) {
        attrKeyService.updateAttrKey(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:attr-key:delete')")
    public CommonResult<Boolean> deleteAttrKey(@RequestParam("id") Integer id) {
        attrKeyService.deleteAttrKey(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得规格名称")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:attr-key:query')")
    public CommonResult<AttrKeyRespVO> getAttrKey(@RequestParam("id") Integer id) {
        AttrKeyDO attrKey = attrKeyService.getAttrKey(id);
        return success(AttrKeyConvert.INSTANCE.convert(attrKey));
    }

    @GetMapping("/list")
    @ApiOperation("获得规格名称列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:attr-key:query')")
    public CommonResult<List<AttrKeyRespVO>> getAttrKeyList(@RequestParam("ids") Collection<Integer> ids) {
        List<AttrKeyDO> list = attrKeyService.getAttrKeyList(ids);
        return success(AttrKeyConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得规格名称分页")
    @PreAuthorize("@ss.hasPermission('product:attr-key:query')")
    public CommonResult<PageResult<AttrKeyRespVO>> getAttrKeyPage(@Valid AttrKeyPageReqVO pageVO) {
        PageResult<AttrKeyDO> pageResult = attrKeyService.getAttrKeyPage(pageVO);
        return success(AttrKeyConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出规格名称 Excel")
    @PreAuthorize("@ss.hasPermission('product:attr-key:export')")
    @OperateLog(type = EXPORT)
    public void exportAttrKeyExcel(@Valid AttrKeyExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<AttrKeyDO> list = attrKeyService.getAttrKeyList(exportReqVO);
        // 导出 Excel
        List<AttrKeyExcelVO> datas = AttrKeyConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "规格名称.xls", "数据", AttrKeyExcelVO.class, datas);
    }

}

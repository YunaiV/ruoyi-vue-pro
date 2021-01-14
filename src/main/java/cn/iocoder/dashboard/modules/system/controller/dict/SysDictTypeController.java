package cn.iocoder.dashboard.modules.system.controller.dict;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.*;
import cn.iocoder.dashboard.modules.system.convert.dict.SysDictTypeConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "字典类型 API")
@RestController
@RequestMapping("/system/dict-type")
public class SysDictTypeController {

    @Resource
    private SysDictTypeService dictTypeService;

    @ApiOperation("/获得字典类型的分页列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    public CommonResult<PageResult<SysDictTypeRespVO>> pageDictTypes(@Validated SysDictTypePageReqVO reqVO) {
        return success(SysDictTypeConvert.INSTANCE.convertPage(dictTypeService.pageDictTypes(reqVO)));
    }

    @ApiOperation("/查询字典类型详细")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @GetMapping(value = "/get")
//    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    public CommonResult<SysDictTypeRespVO> getDictType(@RequestParam("id") Long id) {
        return success(SysDictTypeConvert.INSTANCE.convert(dictTypeService.getDictType(id)));
    }

    @ApiOperation("新增字典类型")
    @PostMapping("/create")
//    @PreAuthorize("@ss.hasPermi('system:dict:add')")
//    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    public CommonResult<Long> createDictType(@Validated @RequestBody SysDictTypeCreateReqVO reqVO) {
        Long dictTypeId = dictTypeService.createDictType(reqVO);
        return success(dictTypeId);
    }

    @ApiOperation("修改字典类型")
    @PostMapping("update")
//    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
//    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateDictType(@Validated @RequestBody SysDictTypeUpdateReqVO reqVO) {
        dictTypeService.updateDictType(reqVO);
        return success(true);
    }

    @ApiOperation("删除字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PostMapping("/delete")
//    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    public CommonResult<Boolean> deleteDictType(Long id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }


    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得全部字典类型列表", notes = "包括开启 + 禁用的字典类型，主要用于前端的下拉选项")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<SysDictTypeSimpleRespVO>> listSimpleDictTypes() {
        List<SysDictTypeDO> list = dictTypeService.listDictTypes();
        return success(SysDictTypeConvert.INSTANCE.convertList(list));
    }

    @ApiOperation("导出数据类型")
    @GetMapping("/export")
//    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    public void export(HttpServletResponse response, @Validated SysDictTypeExportReqVO reqVO) throws IOException {
        List<SysDictTypeDO> list = dictTypeService.listDictTypes(reqVO);
        List<SysDictTypeExcelVO> excelTypeList = SysDictTypeConvert.INSTANCE.convertList02(list);
        // 输出
        ExcelUtils.write(response, "字典类型.xls", "类型列表",
                SysDictTypeExcelVO.class, excelTypeList);
    }

}

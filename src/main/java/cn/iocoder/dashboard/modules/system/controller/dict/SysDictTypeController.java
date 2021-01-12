package cn.iocoder.dashboard.modules.system.controller.dict;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeRespVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.dict.SysDictTypeConvert;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    @ApiImplicitParam(name = "id", value = "编号", readOnly = true, example = "1024")
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
    @ApiImplicitParam(name = "id", value = "编号", readOnly = true, example = "1024")
    @PostMapping("/delete")
//    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    public CommonResult<Boolean> deleteDictType(Long id) {
        dictTypeService.deleteDictType(id);
        return success(true);
    }


//    /**
//     * 获取字典选择框列表
//     */
//    @GetMapping("/optionselect")
//    public AjaxResult optionselect() {
//        List<SysDictType> dictTypes = dictTypeService.selectDictTypeAll();
//        return AjaxResult.success(dictTypes);
//    }

    //
//    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:dict:export')")
//    @GetMapping("/export")
//    public AjaxResult export(SysDictType dictType) {
//        List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
//        ExcelUtil<SysDictType> util = new ExcelUtil<SysDictType>(SysDictType.class);
//        return util.exportExcel(list, "字典类型");
//    }

}

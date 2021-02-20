package cn.iocoder.dashboard.modules.system.controller.dict;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.*;
import cn.iocoder.dashboard.modules.system.convert.dict.SysDictDataConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictDataService;
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

@Api(tags = "字典数据")
@RestController
@RequestMapping("/system/dict-data")
public class SysDictDataController {

    @Resource
    private SysDictDataService dictDataService;

    @ApiOperation(value = "获得全部字典数据列表", notes = "一般用于管理后台缓存字典数据在本地")
    @GetMapping("/list-all-simple")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<SysDictDataSimpleVO>> listSimpleDictDatas() {
        List<SysDictDataDO> list = dictDataService.listDictDatas();
        return success(SysDictDataConvert.INSTANCE.convertList(list));
    }

    @ApiOperation("/获得字典类型的分页列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    public CommonResult<PageResult<SysDictDataRespVO>> pageDictTypes(@Validated SysDictDataPageReqVO reqVO) {
        return success(SysDictDataConvert.INSTANCE.convertPage(dictDataService.pageDictDatas(reqVO)));
    }

    @ApiOperation("/查询字典数据详细")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @GetMapping(value = "/get")
//    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    public CommonResult<SysDictDataRespVO> getDictData(@RequestParam("id") Long id) {
        return success(SysDictDataConvert.INSTANCE.convert(dictDataService.getDictData(id)));
    }

    @ApiOperation("新增字典数据")
    @PostMapping("/create")
//    @PreAuthorize("@ss.hasPermi('system:dict:add')")
//    @Log(title = "字典数据", businessData = BusinessData.INSERT)
    public CommonResult<Long> createDictData(@Validated @RequestBody SysDictDataCreateReqVO reqVO) {
        Long dictDataId = dictDataService.createDictData(reqVO);
        return success(dictDataId);
    }

    @ApiOperation("修改字典数据")
    @PostMapping("update")
//    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
//    @Log(title = "字典数据", businessData = BusinessData.UPDATE)
    public CommonResult<Boolean> updateDictData(@Validated @RequestBody SysDictDataUpdateReqVO reqVO) {
        dictDataService.updateDictData(reqVO);
        return success(true);
    }

    @ApiOperation("删除字典数据")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PostMapping("/delete")
//    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    public CommonResult<Boolean> deleteDictData(Long id) {
        dictDataService.deleteDictData(id);
        return success(true);
    }

    @ApiOperation("导出字典数据")
    @GetMapping("/export")
//    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    public void export(HttpServletResponse response, @Validated SysDictDataExportReqVO reqVO) throws IOException {
        List<SysDictDataDO> list = dictDataService.listDictDatas(reqVO);
        List<SysDictDataExcelVO> excelDataList = SysDictDataConvert.INSTANCE.convertList02(list);
        // 输出
        ExcelUtils.write(response, "字典数据.xls", "数据列表",
                SysDictDataExcelVO.class, excelDataList);
    }

}

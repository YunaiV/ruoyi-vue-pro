package cn.iocoder.dashboard.modules.system.controller.dict;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDataDictSimpleVO;
import cn.iocoder.dashboard.modules.system.convert.dict.SysDictDataConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "字典数据 API")
@RestController
@RequestMapping("/system/dict-data")
public class SysDictDataController {

    @Resource
    private SysDictDataService dictDataService;

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得全部字典数据列表", notes = "一般用于管理后台缓存字典数据在本地")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<SysDataDictSimpleVO>> listSimpleDictDatas() {
        List<SysDictDataDO> list = dictDataService.listDictDatas();
        return success(SysDictDataConvert.INSTANCE.convertList(list));
    }

//    @PreAuthorize("@ss.hasPermi('system:dict:list')")
//    @GetMapping("/list")
//    public TableDataInfo list(SysDictData dictData) {
//        startPage();
//        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
//        return getDataTable(list);
//    }
//
//    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:dict:export')")
//    @GetMapping("/export")
//    public AjaxResult export(SysDictData dictData) {
//        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
//        ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
//        return util.exportExcel(list, "字典数据");
//    }
//
//    /**
//     * 查询字典数据详细
//     */
//    @PreAuthorize("@ss.hasPermi('system:dict:query')")
//    @GetMapping(value = "/{dictCode}")
//    public AjaxResult getInfo(@PathVariable Long dictCode) {
//        return AjaxResult.success(dictDataService.selectDictDataById(dictCode));
//    }
//
//    /**
//     * 根据字典类型查询字典数据信息
//     */
//    @GetMapping(value = "/type/{dictType}")
//    public AjaxResult dictType(@PathVariable String dictType) {
//        List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
//        if (StringUtils.isNull(data)) {
//            data = new ArrayList<SysDictData>();
//        }
//        return AjaxResult.success(data);
//    }
//
//    /**
//     * 新增字典类型
//     */
//    @PreAuthorize("@ss.hasPermi('system:dict:add')")
//    @Log(title = "字典数据", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody SysDictData dict) {
//        dict.setCreateBy(SecurityUtils.getUsername());
//        return toAjax(dictDataService.insertDictData(dict));
//    }
//
//    /**
//     * 修改保存字典类型
//     */
//    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
//    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@Validated @RequestBody SysDictData dict) {
//        dict.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(dictDataService.updateDictData(dict));
//    }
//
//    /**
//     * 删除字典类型
//     */
//    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
//    @Log(title = "字典类型", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{dictCodes}")
//    public AjaxResult remove(@PathVariable Long[] dictCodes) {
//        return toAjax(dictDataService.deleteDictDataByIds(dictCodes));
//    }

}

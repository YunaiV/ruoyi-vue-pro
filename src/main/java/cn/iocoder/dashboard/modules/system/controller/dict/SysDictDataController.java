package cn.iocoder.dashboard.modules.system.controller.dict;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.SysDataDictSimpleVO;
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

@Api(tags = "数据字典 API")
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

}

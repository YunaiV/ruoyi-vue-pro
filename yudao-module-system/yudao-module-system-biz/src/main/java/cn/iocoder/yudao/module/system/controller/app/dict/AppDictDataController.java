package cn.iocoder.yudao.module.system.controller.app.dict;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.DictDataExportReqVO;
import cn.iocoder.yudao.module.system.controller.app.dict.vo.AppDictDataRespVO;
import cn.iocoder.yudao.module.system.convert.dict.DictDataConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 字典数据")
@RestController
@RequestMapping("/system/dict-data")
@Validated
public class AppDictDataController {

    @Resource
    private DictDataService dictDataService;

    // TODO @疯狂：暂时不用 path 参数哈；主要考虑一些中间件支持的一般，例如说链路追踪之类的；还是作为一个参数噶；
    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典数据信息")
    public CommonResult<List<AppDictDataRespVO>> getDictDataList(@PathVariable String dictType) {
        List<DictDataDO> list = dictDataService.getDictDataList(new DictDataExportReqVO().setDictType(dictType));
        return success(DictDataConvert.INSTANCE.convertList03(list));
    }

}

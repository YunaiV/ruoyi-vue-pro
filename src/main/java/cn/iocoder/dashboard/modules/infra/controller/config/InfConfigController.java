package cn.iocoder.dashboard.modules.infra.controller.config;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.dashboard.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import cn.iocoder.dashboard.modules.infra.controller.config.vo.*;
import cn.iocoder.dashboard.modules.infra.convert.config.InfConfigConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.dashboard.modules.infra.service.config.InfConfigService;
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
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.CONFIG_GET_VALUE_ERROR_IF_SENSITIVE;

@Api(tags = "参数配置")
@RestController
@RequestMapping("/infra/config")
public class InfConfigController {

    @Resource
    private InfConfigService configService;

    @ApiOperation("获取参数配置分页")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('infra:config:list')")
    public CommonResult<PageResult<InfConfigRespVO>> getConfigPage(@Validated InfConfigPageReqVO reqVO) {
        PageResult<InfConfigDO> page = configService.getConfigPage(reqVO);
        return success(InfConfigConvert.INSTANCE.convertPage(page));
    }

    @ApiOperation("导出参数配置")
    @GetMapping("/export")
//    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('infra:config:export')")
    public void exportSysConfig(HttpServletResponse response, @Validated InfConfigExportReqVO reqVO) throws IOException {
        List<InfConfigDO> list = configService.getConfigList(reqVO);
        // 拼接数据
        List<InfConfigExcelVO> excelDataList = InfConfigConvert.INSTANCE.convertList(list);
        // 输出
        ExcelUtils.write(response, "参数配置.xls", "配置列表",
                InfConfigExcelVO.class, excelDataList);
    }

    @ApiOperation("获得参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @GetMapping(value = "/get")
//    @PreAuthorize("@ss.hasPermi('infra:config:query')")
    public CommonResult<InfConfigRespVO> getConfig(@RequestParam("id") Long id) {
        return success(InfConfigConvert.INSTANCE.convert(configService.getConfig(id)));
    }

    @ApiOperation(value = "根据参数键名查询参数值", notes = "敏感配置，不允许返回给前端")
    @ApiImplicitParam(name = "key", value = "参数键", required = true, example = "yunai.biz.username", dataTypeClass = String.class)
    @GetMapping(value = "/get-value-by-key")
    public CommonResult<String> getConfigKey(@RequestParam("key") String key) {
        InfConfigDO config = configService.getConfigByKey(key);
        if (config == null) {
            return null;
        }
        if (config.getSensitive()) {
            throw ServiceExceptionUtil.exception(CONFIG_GET_VALUE_ERROR_IF_SENSITIVE);
        }
        return success(config.getValue());
    }

    @ApiOperation("新增参数配置")
    @PostMapping("/create")
//    @PreAuthorize("@ss.hasPermi('infra:config:add')")
//    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @Idempotent(timeout = 60, keyResolver = ExpressionIdempotentKeyResolver.class, keyArg = "#reqVO.key")
    public CommonResult<Long> createConfig(@Validated @RequestBody InfConfigCreateReqVO reqVO) {
        return success(configService.createConfig(reqVO));
    }

    @ApiOperation("修改参数配置")
    @PutMapping("/update")
//    @PreAuthorize("@ss.hasPermi('infra:config:edit')")
//    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @Idempotent(timeout = 60)
    public CommonResult<Boolean> updateConfig(@Validated @RequestBody InfConfigUpdateReqVO reqVO) {
        configService.updateConfig(reqVO);
        return success(true);
    }

    @ApiOperation("删除参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @DeleteMapping("/delete")
//    @PreAuthorize("@ss.hasPermi('infra:config:remove')")
//    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        configService.deleteConfig(id);
        return success(true);
    }

}

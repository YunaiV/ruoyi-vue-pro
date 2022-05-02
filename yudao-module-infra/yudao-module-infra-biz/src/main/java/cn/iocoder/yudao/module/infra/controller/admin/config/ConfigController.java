package cn.iocoder.yudao.module.infra.controller.admin.config;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.*;
import cn.iocoder.yudao.module.infra.convert.config.ConfigConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 参数配置")
@RestController
@RequestMapping("/infra/config")
@Validated
public class ConfigController {

    @Resource
    private ConfigService configService;

    @PostMapping("/create")
    @ApiOperation("创建参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody ConfigCreateReqVO reqVO) {
        return success(configService.createConfig(reqVO));
    }

    @PutMapping("/update")
    @ApiOperation("修改参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody ConfigUpdateReqVO reqVO) {
        configService.updateConfig(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        configService.deleteConfig(id);
        return success(true);
    }

    @GetMapping(value = "/get")
    @ApiOperation("获得参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<ConfigRespVO> getConfig(@RequestParam("id") Long id) {
        return success(ConfigConvert.INSTANCE.convert(configService.getConfig(id)));
    }

    @GetMapping(value = "/get-value-by-key")
    @ApiOperation(value = "根据参数键名查询参数值", notes = "不可见的配置，不允许返回给前端")
    @ApiImplicitParam(name = "key", value = "参数键", required = true, example = "yunai.biz.username", dataTypeClass = String.class)
    public CommonResult<String> getConfigKey(@RequestParam("key") String key) {
        ConfigDO config = configService.getConfigByKey(key);
        if (config == null) {
            return null;
        }
        if (config.getVisible()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.CONFIG_GET_VALUE_ERROR_IF_VISIBLE);
        }
        return success(config.getValue());
    }

    @GetMapping("/page")
    @ApiOperation("获取参数配置分页")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<PageResult<ConfigRespVO>> getConfigPage(@Valid ConfigPageReqVO reqVO) {
        PageResult<ConfigDO> page = configService.getConfigPage(reqVO);
        return success(ConfigConvert.INSTANCE.convertPage(page));
    }

    @GetMapping("/export")
    @ApiOperation("导出参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:export')")
    @OperateLog(type = EXPORT)
    public void exportSysConfig(@Valid ConfigExportReqVO reqVO,
                                HttpServletResponse response) throws IOException {
        List<ConfigDO> list = configService.getConfigList(reqVO);
        // 拼接数据
        List<ConfigExcelVO> datas = ConfigConvert.INSTANCE.convertList(list);
        // 输出
        ExcelUtils.write(response, "参数配置.xls", "数据", ConfigExcelVO.class, datas);
    }

}

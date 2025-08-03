package cn.iocoder.yudao.module.iot.controller.admin.plugin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugin.IotPluginConfigDO;
import cn.iocoder.yudao.module.iot.service.plugin.IotPluginConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 插件配置")
@RestController
@RequestMapping("/iot/plugin-config")
@Validated
public class PluginConfigController {

    @Resource
    private IotPluginConfigService pluginConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建插件配置")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:create')")
    public CommonResult<Long> createPluginConfig(@Valid @RequestBody PluginConfigSaveReqVO createReqVO) {
        return success(pluginConfigService.createPluginConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插件配置")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:update')")
    public CommonResult<Boolean> updatePluginConfig(@Valid @RequestBody PluginConfigSaveReqVO updateReqVO) {
        pluginConfigService.updatePluginConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插件配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:delete')")
    public CommonResult<Boolean> deletePluginConfig(@RequestParam("id") Long id) {
        pluginConfigService.deletePluginConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插件配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:query')")
    public CommonResult<PluginConfigRespVO> getPluginConfig(@RequestParam("id") Long id) {
        IotPluginConfigDO pluginConfig = pluginConfigService.getPluginConfig(id);
        return success(BeanUtils.toBean(pluginConfig, PluginConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得插件配置分页")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:query')")
    public CommonResult<PageResult<PluginConfigRespVO>> getPluginConfigPage(@Valid PluginConfigPageReqVO pageReqVO) {
        PageResult<IotPluginConfigDO> pageResult = pluginConfigService.getPluginConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PluginConfigRespVO.class));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "上传插件文件")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:update')")
    public CommonResult<Boolean> uploadFile(@Valid PluginConfigImportReqVO reqVO) {
        pluginConfigService.uploadFile(reqVO.getId(), reqVO.getFile());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改插件状态")
    @PreAuthorize("@ss.hasPermission('iot:plugin-config:update')")
    public CommonResult<Boolean> updatePluginConfigStatus(@Valid @RequestBody PluginConfigStatusReqVO reqVO) {
        pluginConfigService.updatePluginStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

}
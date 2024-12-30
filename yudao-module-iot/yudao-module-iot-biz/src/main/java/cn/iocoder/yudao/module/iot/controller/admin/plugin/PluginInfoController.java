package cn.iocoder.yudao.module.iot.controller.admin.plugin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoImportReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.PluginInfoSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.service.plugin.PluginInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 插件信息")
@RestController
@RequestMapping("/iot/plugin-info")
@Validated
public class PluginInfoController {

    @Resource
    private PluginInfoService pluginInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建插件信息")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:create')")
    public CommonResult<Long> createPluginInfo(@Valid @RequestBody PluginInfoSaveReqVO createReqVO) {
        return success(pluginInfoService.createPluginInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插件信息")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:update')")
    public CommonResult<Boolean> updatePluginInfo(@Valid @RequestBody PluginInfoSaveReqVO updateReqVO) {
        pluginInfoService.updatePluginInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插件信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:delete')")
    public CommonResult<Boolean> deletePluginInfo(@RequestParam("id") Long id) {
        pluginInfoService.deletePluginInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插件信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:query')")
    public CommonResult<PluginInfoRespVO> getPluginInfo(@RequestParam("id") Long id) {
        PluginInfoDO pluginInfo = pluginInfoService.getPluginInfo(id);
        return success(BeanUtils.toBean(pluginInfo, PluginInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得插件信息分页")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:query')")
    public CommonResult<PageResult<PluginInfoRespVO>> getPluginInfoPage(@Valid PluginInfoPageReqVO pageReqVO) {
        PageResult<PluginInfoDO> pageResult = pluginInfoService.getPluginInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PluginInfoRespVO.class));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "上传插件文件")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:update')")
    public CommonResult<Boolean> uploadFile(@Valid PluginInfoImportReqVO reqVO) {
        pluginInfoService.uploadFile(reqVO.getId(), reqVO.getFile());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改插件状态")
    @PreAuthorize("@ss.hasPermission('iot:plugin-info:update')")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody PluginInfoSaveReqVO reqVO) {
        pluginInfoService.updatePluginStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

}
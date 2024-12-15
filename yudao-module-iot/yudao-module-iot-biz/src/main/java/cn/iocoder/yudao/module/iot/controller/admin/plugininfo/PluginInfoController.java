package cn.iocoder.yudao.module.iot.controller.admin.plugininfo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.PluginInfoPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.PluginInfoRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.PluginInfoSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.service.plugininfo.PluginInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_IS_EMPTY;

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

    @RequestMapping(value = "/update-jar",
            method = {RequestMethod.POST, RequestMethod.PUT}) // 解决 uni-app 不支持 Put 上传文件的问题
    @Operation(summary = "上传Jar包")
    public CommonResult<Boolean> uploadJar(
            @RequestParam("id") Long id,
            @RequestParam("jar") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        pluginInfoService.uploadJar(id, file);
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
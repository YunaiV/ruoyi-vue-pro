package cn.iocoder.yudao.module.iot.controller.admin.plugininstance;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininstance.PluginInstanceDO;
import cn.iocoder.yudao.module.iot.service.plugininstance.PluginInstanceService;

@Tag(name = "管理后台 - IoT 插件实例")
@RestController
@RequestMapping("/iot/plugin-instance")
@Validated
public class PluginInstanceController {

    @Resource
    private PluginInstanceService pluginInstanceService;

    @PostMapping("/create")
    @Operation(summary = "创建IoT 插件实例")
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:create')")
    public CommonResult<Long> createPluginInstance(@Valid @RequestBody PluginInstanceSaveReqVO createReqVO) {
        return success(pluginInstanceService.createPluginInstance(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新IoT 插件实例")
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:update')")
    public CommonResult<Boolean> updatePluginInstance(@Valid @RequestBody PluginInstanceSaveReqVO updateReqVO) {
        pluginInstanceService.updatePluginInstance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IoT 插件实例")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:delete')")
    public CommonResult<Boolean> deletePluginInstance(@RequestParam("id") Long id) {
        pluginInstanceService.deletePluginInstance(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得IoT 插件实例")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:query')")
    public CommonResult<PluginInstanceRespVO> getPluginInstance(@RequestParam("id") Long id) {
        PluginInstanceDO pluginInstance = pluginInstanceService.getPluginInstance(id);
        return success(BeanUtils.toBean(pluginInstance, PluginInstanceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得IoT 插件实例分页")
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:query')")
    public CommonResult<PageResult<PluginInstanceRespVO>> getPluginInstancePage(@Valid PluginInstancePageReqVO pageReqVO) {
        PageResult<PluginInstanceDO> pageResult = pluginInstanceService.getPluginInstancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PluginInstanceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出IoT 插件实例 Excel")
    @PreAuthorize("@ss.hasPermission('iot:plugin-instance:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPluginInstanceExcel(@Valid PluginInstancePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PluginInstanceDO> list = pluginInstanceService.getPluginInstancePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "IoT 插件实例.xls", "数据", PluginInstanceRespVO.class,
                        BeanUtils.toBean(list, PluginInstanceRespVO.class));
    }

}
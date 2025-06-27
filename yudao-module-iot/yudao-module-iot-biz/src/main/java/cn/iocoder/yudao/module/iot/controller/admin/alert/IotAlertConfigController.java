package cn.iocoder.yudao.module.iot.controller.admin.alert;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
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

import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertConfigService;

@Tag(name = "管理后台 - IoT 告警配置")
@RestController
@RequestMapping("/iot/alert-config")
@Validated
public class IotAlertConfigController {

    @Resource
    private IotAlertConfigService alertConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建告警配置")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:create')")
    public CommonResult<Long> createAlertConfig(@Valid @RequestBody IotAlertConfigSaveReqVO createReqVO) {
        return success(alertConfigService.createAlertConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新告警配置")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:update')")
    public CommonResult<Boolean> updateAlertConfig(@Valid @RequestBody IotAlertConfigSaveReqVO updateReqVO) {
        alertConfigService.updateAlertConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alert-config:delete')")
    public CommonResult<Boolean> deleteAlertConfig(@RequestParam("id") Long id) {
        alertConfigService.deleteAlertConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:query')")
    public CommonResult<IotAlertConfigRespVO> getAlertConfig(@RequestParam("id") Long id) {
        IotAlertConfigDO alertConfig = alertConfigService.getAlertConfig(id);
        return success(BeanUtils.toBean(alertConfig, IotAlertConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警配置分页")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:query')")
    public CommonResult<PageResult<IotAlertConfigRespVO>> getAlertConfigPage(@Valid IotAlertConfigPageReqVO pageReqVO) {
        PageResult<IotAlertConfigDO> pageResult = alertConfigService.getAlertConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotAlertConfigRespVO.class));
    }

}
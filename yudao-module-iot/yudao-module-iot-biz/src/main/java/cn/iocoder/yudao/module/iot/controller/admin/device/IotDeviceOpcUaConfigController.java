package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT 设备 OPC UA 连接配置")
@RestController
@RequestMapping("/iot/device-opc-ua-config")
@Validated
public class IotDeviceOpcUaConfigController {

    @Resource
    private IotDeviceOpcUaConfigService opcUaConfigService;

    @PostMapping("/save")
    @Operation(summary = "保存设备 OPC UA 连接配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> saveDeviceOpcUaConfig(@Valid @RequestBody IotDeviceOpcUaConfigSaveReqVO saveReqVO) {
        opcUaConfigService.saveDeviceOpcUaConfig(saveReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 OPC UA 连接配置")
    @Parameter(name = "id", description = "编号", example = "1024")
    @Parameter(name = "deviceId", description = "设备编号", example = "2048")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceOpcUaConfigRespVO> getDeviceOpcUaConfig(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "deviceId", required = false) Long deviceId) {
        IotDeviceOpcUaConfigDO opcUaConfig = null;
        if (id != null) {
            opcUaConfig = opcUaConfigService.getDeviceOpcUaConfig(id);
        } else if (deviceId != null) {
            opcUaConfig = opcUaConfigService.getDeviceOpcUaConfigByDeviceId(deviceId);
        }
        return success(BeanUtils.toBean(opcUaConfig, IotDeviceOpcUaConfigRespVO.class));
    }

}

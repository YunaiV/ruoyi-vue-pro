package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备 Modbus 连接配置")
@RestController
@RequestMapping("/iot/device-modbus-config")
@Validated
public class IotDeviceModbusConfigController {

    @Resource
    private IotDeviceModbusConfigService modbusConfigService;

    @PostMapping("/save")
    @Operation(summary = "保存设备 Modbus 连接配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> saveDeviceModbusConfig(@Valid @RequestBody IotDeviceModbusConfigSaveReqVO saveReqVO) {
        modbusConfigService.saveDeviceModbusConfig(saveReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 Modbus 连接配置")
    @Parameter(name = "id", description = "编号", example = "1024")
    @Parameter(name = "deviceId", description = "设备编号", example = "2048")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceModbusConfigRespVO> getDeviceModbusConfig(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "deviceId", required = false) Long deviceId) {
        IotDeviceModbusConfigDO modbusConfig = null;
        if (id != null) {
            modbusConfig = modbusConfigService.getDeviceModbusConfig(id);
        } else if (deviceId != null) {
            modbusConfig = modbusConfigService.getDeviceModbusConfigByDeviceId(deviceId);
        }
        return success(BeanUtils.toBean(modbusConfig, IotDeviceModbusConfigRespVO.class));
    }

}

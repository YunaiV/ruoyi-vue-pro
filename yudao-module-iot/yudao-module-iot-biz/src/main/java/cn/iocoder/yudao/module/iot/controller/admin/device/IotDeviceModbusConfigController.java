package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备 Modbus 连接配置")
@RestController
@RequestMapping("/iot/device-modbus-config")
@Validated
public class IotDeviceModbusConfigController {

    @Resource
    private IotDeviceModbusConfigService modbusConfigService;

    // TODO @AI：create 和 update 合并成 save 接口；
    @PostMapping("/create")
    @Operation(summary = "创建设备 Modbus 连接配置")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:create')")
    public CommonResult<Long> createModbusConfig(@Valid @RequestBody IotDeviceModbusConfigSaveReqVO createReqVO) {
        return success(modbusConfigService.createModbusConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 Modbus 连接配置")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:update')")
    public CommonResult<Boolean> updateModbusConfig(@Valid @RequestBody IotDeviceModbusConfigSaveReqVO updateReqVO) {
        modbusConfigService.updateModbusConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 Modbus 连接配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:delete')")
    public CommonResult<Boolean> deleteModbusConfig(@RequestParam("id") Long id) {
        modbusConfigService.deleteModbusConfig(id);
        return success(true);
    }

    // TODO @AI：这个接口改造，支持 id 或者 deviceId；二选一查询；
    @GetMapping("/get")
    @Operation(summary = "获得设备 Modbus 连接配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:query')")
    public CommonResult<IotDeviceModbusConfigRespVO> getModbusConfig(@RequestParam("id") Long id) {
        IotDeviceModbusConfigDO modbusConfig = modbusConfigService.getModbusConfig(id);
        return success(BeanUtils.toBean(modbusConfig, IotDeviceModbusConfigRespVO.class));
    }

    // TODO @AI：合并到 getModbusConfig 接口里；
    @GetMapping("/get-by-device-id")
    @Operation(summary = "根据设备编号获得 Modbus 连接配置")
    @Parameter(name = "deviceId", description = "设备编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:query')")
    public CommonResult<IotDeviceModbusConfigRespVO> getModbusConfigByDeviceId(@RequestParam("deviceId") Long deviceId) {
        IotDeviceModbusConfigDO modbusConfig = modbusConfigService.getModbusConfigByDeviceId(deviceId);
        return success(BeanUtils.toBean(modbusConfig, IotDeviceModbusConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 Modbus 连接配置分页")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-config:query')")
    public CommonResult<PageResult<IotDeviceModbusConfigRespVO>> getModbusConfigPage(@Valid IotDeviceModbusConfigPageReqVO pageReqVO) {
        PageResult<IotDeviceModbusConfigDO> pageResult = modbusConfigService.getModbusConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceModbusConfigRespVO.class));
    }

}

package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备 Modbus 点位配置")
@RestController
@RequestMapping("/iot/device-modbus-point")
@Validated
public class IotDeviceModbusPointController {

    @Resource
    private IotDeviceModbusPointService modbusPointService;

    @PostMapping("/create")
    @Operation(summary = "创建设备 Modbus 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Long> createDeviceModbusPoint(@Valid @RequestBody IotDeviceModbusPointSaveReqVO createReqVO) {
        return success(modbusPointService.createDeviceModbusPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 Modbus 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceModbusPoint(@Valid @RequestBody IotDeviceModbusPointSaveReqVO updateReqVO) {
        modbusPointService.updateDeviceModbusPoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 Modbus 点位配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> deleteDeviceModbusPoint(@RequestParam("id") Long id) {
        modbusPointService.deleteDeviceModbusPoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 Modbus 点位配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceModbusPointRespVO> getDeviceModbusPoint(@RequestParam("id") Long id) {
        IotDeviceModbusPointDO modbusPoint = modbusPointService.getDeviceModbusPoint(id);
        return success(BeanUtils.toBean(modbusPoint, IotDeviceModbusPointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 Modbus 点位配置分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceModbusPointRespVO>> getDeviceModbusPointPage(@Valid IotDeviceModbusPointPageReqVO pageReqVO) {
        PageResult<IotDeviceModbusPointDO> pageResult = modbusPointService.getDeviceModbusPointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceModbusPointRespVO.class));
    }

}

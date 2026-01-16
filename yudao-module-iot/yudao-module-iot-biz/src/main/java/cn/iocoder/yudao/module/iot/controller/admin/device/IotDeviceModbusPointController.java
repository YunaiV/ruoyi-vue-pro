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
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:create')")
    public CommonResult<Long> createModbusPoint(@Valid @RequestBody IotDeviceModbusPointSaveReqVO createReqVO) {
        return success(modbusPointService.createModbusPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 Modbus 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:update')")
    public CommonResult<Boolean> updateModbusPoint(@Valid @RequestBody IotDeviceModbusPointSaveReqVO updateReqVO) {
        modbusPointService.updateModbusPoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 Modbus 点位配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:delete')")
    public CommonResult<Boolean> deleteModbusPoint(@RequestParam("id") Long id) {
        modbusPointService.deleteModbusPoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 Modbus 点位配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:query')")
    public CommonResult<IotDeviceModbusPointRespVO> getModbusPoint(@RequestParam("id") Long id) {
        IotDeviceModbusPointDO modbusPoint = modbusPointService.getModbusPoint(id);
        return success(BeanUtils.toBean(modbusPoint, IotDeviceModbusPointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 Modbus 点位配置分页")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:query')")
    public CommonResult<PageResult<IotDeviceModbusPointRespVO>> getModbusPointPage(@Valid IotDeviceModbusPointPageReqVO pageReqVO) {
        PageResult<IotDeviceModbusPointDO> pageResult = modbusPointService.getModbusPointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceModbusPointRespVO.class));
    }

    // TODO @AI：应该用不上这个接口？只需要 getModbusPointPage 分页
    @GetMapping("/list-by-device-id")
    @Operation(summary = "根据设备编号获得 Modbus 点位配置列表")
    @Parameter(name = "deviceId", description = "设备编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-modbus-point:query')")
    public CommonResult<List<IotDeviceModbusPointRespVO>> getModbusPointListByDeviceId(@RequestParam("deviceId") Long deviceId) {
        List<IotDeviceModbusPointDO> list = modbusPointService.getModbusPointListByDeviceId(deviceId);
        return success(BeanUtils.toBean(list, IotDeviceModbusPointRespVO.class));
    }

}

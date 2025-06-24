package cn.iocoder.yudao.module.huawei.smarthome.controller.app.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceControlReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceGetReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.service.device.HuaweiDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户APP - 华为智能家居 - 设备管理")
@RestController
@RequestMapping("/huawei/smarthome/device")
@Validated
public class AppHuaweiDeviceController {

    @Resource
    private HuaweiDeviceService huaweiDeviceService;

    @GetMapping("/list-by-space")
    @ApiOperation("查询指定空间下设备信息")
    public CommonResult<DeviceListRespDTO> listDevicesBySpace(
            @ApiParam(value = "空间 ID", required = true, example = "space-uuid-123")
            @RequestParam("spaceId") String spaceId) {
        try {
            DeviceListBySpaceReqDTO reqDTO = new DeviceListBySpaceReqDTO(spaceId);
            DeviceListRespDTO respDTO = huaweiDeviceService.listDevicesBySpace(reqDTO);
            return success(respDTO);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API查询空间下设备列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list-by-ids")
    @ApiOperation("查询指定设备信息 (批量)")
    public CommonResult<DeviceListRespDTO> getDevices(
            @ApiParam(value = "设备 ID 列表", required = true, example = "device-uuid-1,device-uuid-2")
            @RequestParam("deviceIds") List<String> deviceIds) {
        try {
            DeviceGetReqDTO reqDTO = new DeviceGetReqDTO(deviceIds);
            DeviceListRespDTO respDTO = huaweiDeviceService.getDevices(reqDTO);
            return success(respDTO);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API查询指定设备信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/{deviceId}/control")
    @ApiOperation("控制设备")
    public CommonResult<?> controlDevice(
            @ApiParam(value = "设备 ID", required = true, example = "device-uuid-123")
            @PathVariable("deviceId") String deviceId,
            @Valid @RequestBody DeviceControlReqDTO controlReqDTO) {
        // 确保路径中的deviceId与请求体中的deviceId一致或以此为准
        if (!deviceId.equals(controlReqDTO.getDeviceId())) {
            // 或者根据项目规范决定如何处理，例如抛出参数错误异常
            // 这里简单地以路径参数为准覆盖请求体中的（如果设计允许请求体也包含deviceId）
             controlReqDTO.setDeviceId(deviceId);
        }
        try {
            huaweiDeviceService.controlDevice(controlReqDTO);
            return success(null);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API控制设备失败: " + e.getMessage());
        }
    }
}

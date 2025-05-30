package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import cn.iocoder.yudao.module.iot.service.plugin.IotPluginInstanceService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * * 设备数据 Upstream 上行 API 实现类
 */
@RestController
@Validated
@Primary // 保证优先匹配，因为 yudao-module-iot-net-component-core 也有 IotDeviceUpstreamApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceUpstreamApiImpl implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;

    // ========== 设备相关 ==========

    @Override
    public CommonResult<Boolean> updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO) {
        deviceUpstreamService.updateDeviceState(updateReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO) {
        deviceUpstreamService.reportDeviceProperty(reportReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO) {
        deviceUpstreamService.reportDeviceEvent(reportReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> registerDevice(IotDeviceRegisterReqDTO registerReqDTO) {
        deviceUpstreamService.registerDevice(registerReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> registerSubDevice(IotDeviceRegisterSubReqDTO registerReqDTO) {
        deviceUpstreamService.registerSubDevice(registerReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> addDeviceTopology(IotDeviceTopologyAddReqDTO addReqDTO) {
        deviceUpstreamService.addDeviceTopology(addReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        boolean result = deviceUpstreamService.authenticateEmqxConnection(authReqDTO);
        return success(result);
    }

}
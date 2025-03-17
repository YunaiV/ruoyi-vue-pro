package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceUpstreamService;
import cn.iocoder.yudao.module.iot.service.plugin.IotPluginInstanceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * * 设备数据 Upstream 上行 API 实现类
 */
@RestController
@Validated
public class IoTDeviceUpstreamApiImpl implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;
    @Resource
    private IotPluginInstanceService pluginInstanceService;

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

    // ========== 插件相关 ==========

    @Override
    public CommonResult<Boolean> heartbeatPluginInstance(IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO) {
        pluginInstanceService.heartbeatPluginInstance(heartbeatReqDTO);
        return success(true);
    }

}
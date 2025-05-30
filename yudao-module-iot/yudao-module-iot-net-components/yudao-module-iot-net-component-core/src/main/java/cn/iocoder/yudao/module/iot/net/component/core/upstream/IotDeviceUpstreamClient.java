package cn.iocoder.yudao.module.iot.net.component.core.upstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
/**
 * 设备数据 Upstream 上行客户端
 * <p>
 * 直接调用 IotDeviceUpstreamApi 接口
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamClient implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    public CommonResult<Boolean> updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO) {
        return deviceUpstreamApi.updateDeviceState(updateReqDTO);
    }

    @Override
    public CommonResult<Boolean> reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO) {
        return deviceUpstreamApi.reportDeviceEvent(reportReqDTO);
    }

    @Override
    public CommonResult<Boolean> registerDevice(IotDeviceRegisterReqDTO registerReqDTO) {
        return deviceUpstreamApi.registerDevice(registerReqDTO);
    }

    @Override
    public CommonResult<Boolean> registerSubDevice(IotDeviceRegisterSubReqDTO registerReqDTO) {
        return deviceUpstreamApi.registerSubDevice(registerReqDTO);
    }

    @Override
    public CommonResult<Boolean> addDeviceTopology(IotDeviceTopologyAddReqDTO addReqDTO) {
        return deviceUpstreamApi.addDeviceTopology(addReqDTO);
    }

    @Override
    public CommonResult<Boolean> authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        return deviceUpstreamApi.authenticateEmqxConnection(authReqDTO);
    }

    @Override
    public CommonResult<Boolean> reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO) {
        return deviceUpstreamApi.reportDeviceProperty(reportReqDTO);
    }

}

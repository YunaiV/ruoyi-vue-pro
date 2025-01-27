package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import cn.iocoder.yudao.module.iot.service.device.upstream.IotDeviceUpstreamService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 *  * 设备数据 Upstream 上行 API 实现类
 */
@RestController
@Validated
public class IoTDeviceUpstreamApiImpl implements IotDeviceUpstreamApi {

    @Resource
    private IotDeviceUpstreamService deviceUpstreamService;

    @Override
    public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        deviceUpstreamService.updateDeviceStatus(updateReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        deviceUpstreamService.reportDevicePropertyData(reportReqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        deviceUpstreamService.reportDeviceEventData(reportReqDTO);
        return success(true);
    }

}
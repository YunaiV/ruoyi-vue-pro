package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 设备数据 API 实现类
 */
@RestController
@Validated
public class DeviceDataApiImpl implements DeviceDataApi {

    @Resource
    private IotDevicePropertyDataService deviceDataService;

    @Override
    public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        return success(true);
    }

    @Override
    public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        deviceDataService.saveDeviceData(reportReqDTO);
        return success(true);
    }

}
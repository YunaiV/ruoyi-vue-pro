package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 设备数据 API 实现类
 */
@Service
@Validated
public class DeviceDataApiImpl implements DeviceDataApi {

    @Resource
    private IotDevicePropertyDataService deviceDataService;

    @Override
    public void saveDeviceData(String productKey, String deviceName, String message) {
        deviceDataService.saveDeviceData(productKey, deviceName, message);
    }

}
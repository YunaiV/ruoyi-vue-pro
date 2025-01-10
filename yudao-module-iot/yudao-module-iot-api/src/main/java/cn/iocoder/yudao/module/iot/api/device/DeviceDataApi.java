package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.module.iot.api.device.dto.DeviceDataCreateReqDTO;
import jakarta.validation.Valid;

/**
 * 设备数据 API
 *
 * @author haohao
 */
public interface DeviceDataApi {

    /**
     * 保存设备数据
     *
     * @param createDTO 设备数据
     */
    void saveDeviceData(@Valid DeviceDataCreateReqDTO createDTO);

}
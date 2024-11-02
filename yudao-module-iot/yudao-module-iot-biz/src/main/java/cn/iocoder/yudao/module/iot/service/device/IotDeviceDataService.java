package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceDataService {


    /**
     * 保存设备数据
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @param message     消息
     */
    void saveDeviceData(String productKey, String deviceName, String message);

    /**
     * 获得设备属性最新数据
     *
     * @param deviceId 设备编号
     * @return 设备属性最新数据
     */
    List<IotDeviceDataDO> getDevicePropertiesLatestData(@Valid IotDeviceDataReqVO deviceId);
}
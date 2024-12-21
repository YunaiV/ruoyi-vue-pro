package cn.iocoder.yudao.module.iot.api;

/**
 * 设备数据 API
 */
public interface DeviceDataApi {

    /**
     * 保存设备数据
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @param message    消息
     */
    void saveDeviceData(String productKey, String deviceName, String message);

}
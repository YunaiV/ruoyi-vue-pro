package cn.iocoder.yudao.module.iot.api.device;

/**
 * 设备数据 API
 *
 * @author haohao
 */
public interface DeviceDataApi {

    // TODO @haohao：最好搞成 dto 哈！
    /**
     * 保存设备数据
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @param message    消息
     */
    void saveDeviceData(String productKey, String deviceName, String message);

}
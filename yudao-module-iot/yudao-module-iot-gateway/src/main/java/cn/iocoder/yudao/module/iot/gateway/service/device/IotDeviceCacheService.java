package cn.iocoder.yudao.module.iot.gateway.service.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备缓存 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceCacheService {

    /**
     * 设备信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class DeviceInfo {
        /**
         * 设备编号
         */
        private Long deviceId;
        /**
         * 产品标识
         */
        private String productKey;
        /**
         * 设备名称
         */
        private String deviceName;
        /**
         * 设备密钥
         */
        private String deviceKey;
        /**
         * 租户编号
         */
        private Long tenantId;
    }

    /**
     * 根据 productKey 和 deviceName 获取设备信息
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 设备信息，如果不存在返回 null
     */
    DeviceInfo getDeviceInfo(String productKey, String deviceName);

    /**
     * 根据 deviceId 获取设备信息
     *
     * @param deviceId 设备编号
     * @return 设备信息，如果不存在返回 null
     */
    DeviceInfo getDeviceInfoById(Long deviceId);

    /**
     * 清除设备缓存
     *
     * @param deviceId 设备编号
     */
    void evictDeviceCache(Long deviceId);

    /**
     * 清除设备缓存
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     */
    void evictDeviceCache(String productKey, String deviceName);

}
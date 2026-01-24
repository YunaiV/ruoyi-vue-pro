package cn.iocoder.yudao.module.iot.core.topic.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 子设备动态注册 Response DTO
 * <p>
 * 用于 thing.sub.register 响应的设备信息
 *
 * @author 芋道源码
 * @see <a href="http://help.aliyun.com/zh/marketplace/dynamic-registration-of-sub-devices">阿里云 - 动态注册子设备</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotSubDeviceRegisterRespDTO {

    /**
     * 子设备 ProductKey
     */
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    private String deviceName;

    /**
     * 分配的 DeviceSecret
     */
    private String deviceSecret;

}

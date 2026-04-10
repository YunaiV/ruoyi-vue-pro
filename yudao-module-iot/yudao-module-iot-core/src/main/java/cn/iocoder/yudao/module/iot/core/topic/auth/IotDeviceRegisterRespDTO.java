package cn.iocoder.yudao.module.iot.core.topic.auth;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备动态注册 Response DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#DEVICE_REGISTER} 响应的设备信息
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceRegisterRespDTO {

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
    private String deviceSecret;

}

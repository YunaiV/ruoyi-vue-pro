package cn.iocoder.yudao.module.iot.core.topic.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * IoT 子设备动态注册 Request DTO
 * <p>
 * 用于 thing.sub.register 消息的 params 数组元素
 *
 * @author 芋道源码
 * @see <a href="http://help.aliyun.com/zh/marketplace/dynamic-registration-of-sub-devices">阿里云 - 动态注册子设备</a>
 */
@Data
public class IotSubDeviceRegisterReqDTO {

    /**
     * 子设备 ProductKey
     */
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

}

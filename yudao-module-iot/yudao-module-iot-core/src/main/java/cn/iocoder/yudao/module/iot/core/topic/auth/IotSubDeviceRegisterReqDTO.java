package cn.iocoder.yudao.module.iot.core.topic.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// TODO @AI：不用带 device 关键字；
// TODO @AI：挂个阿里云的链接，https://help.aliyun.com/zh/iot/user-guide/register-devices 的「子设备的 MQTT 动态注册」小节
/**
 * IoT 子设备动态注册 Request DTO
 * <p>
 * 用于 thing.sub.register 消息的 params 数组元素
 *
 * @author 芋道源码
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

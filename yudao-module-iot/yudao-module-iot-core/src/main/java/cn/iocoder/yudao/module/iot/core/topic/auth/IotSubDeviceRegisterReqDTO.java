package cn.iocoder.yudao.module.iot.core.topic.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * IoT 子设备动态注册 Request DTO
 * <p>
 * 用于 thing.auth.register.sub 消息的 params 数组元素
 *
 * 特殊：网关子设备的动态注册，必须已经创建好该网关子设备（不然哪来的 {@link #deviceName} 字段）。更多的好处，是设备不用提前烧录 deviceSecret 密钥。
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/register-devices">阿里云 - 动态注册子设备</a>
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

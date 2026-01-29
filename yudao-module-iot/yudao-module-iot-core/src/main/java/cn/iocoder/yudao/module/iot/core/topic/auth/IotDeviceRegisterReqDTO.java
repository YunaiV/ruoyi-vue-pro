package cn.iocoder.yudao.module.iot.core.topic.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * IoT 设备动态注册 Request DTO
 * <p>
 * 用于直连设备/网关的一型一密动态注册：使用 productSecret 验证，返回 deviceSecret
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">阿里云 - 一型一密</a>
 */
@Data
public class IotDeviceRegisterReqDTO {

    /**
     * 产品标识
     */
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    /**
     * 设备名称
     */
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

    /**
     * 产品密钥
     */
    @NotEmpty(message = "产品密钥不能为空")
    private String productSecret;

}

package cn.iocoder.yudao.module.iot.core.topic.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * IoT 设备动态注册 Request DTO
 * <p>
 * 用于直连设备/网关的一型一密动态注册：使用 ProductSecret 验证签名，返回 DeviceSecret
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

    // TODO @AI：可以去掉 random 字段；
    /**
     * 随机数，用于签名
     */
    @NotEmpty(message = "随机数不能为空")
    private String random;

    // TODO @AI：看起来，是直接带 productSecret 阿里云上，你在检查下！
    /**
     * 签名
     */
    @NotEmpty(message = "签名不能为空")
    private String sign;

}

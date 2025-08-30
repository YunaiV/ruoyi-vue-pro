package cn.iocoder.yudao.module.iot.core.biz.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * IoT 设备认证 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceAuthReqDTO {

    /**
     * 客户端 ID
     */
    @NotEmpty(message = "客户端 ID 不能为空")
    private String clientId;

    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    private String password;

}

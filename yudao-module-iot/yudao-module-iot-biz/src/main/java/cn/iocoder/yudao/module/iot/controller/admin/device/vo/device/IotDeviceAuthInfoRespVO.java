package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备认证信息 Response VO")
@Data
public class IotDeviceAuthInfoRespVO {

    @Schema(description = "客户端 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "product123.device001")
    @NotBlank(message = "客户端 ID 不能为空")
    private String clientId;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "device001&product123")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1a2b3c4d5e6f7890abcdef1234567890")
    @NotBlank(message = "密码不能为空")
    private String password;

}
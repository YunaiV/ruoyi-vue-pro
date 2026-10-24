package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机确认扫码登录 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 手机确认扫码登录 Request VO")
@Data
public class AuthQrcodeConfirmReqVO {

    @Schema(description = "二维码编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "二维码编号不能为空")
    @Pattern(regexp = "[a-f0-9]{32}", message = "二维码编号格式不正确")
    private String qrcodeId;

}

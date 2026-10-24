package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 电脑轮询领取扫码登录结果 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 电脑轮询扫码登录 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthQrcodePollReqVO extends AuthQrcodeConfirmReqVO {

    @Schema(description = "生成二维码时获得的领取凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "领取凭证不能为空")
    @Pattern(regexp = "[a-f0-9]{32}", message = "领取凭证格式不正确")
    @ToString.Exclude
    private String secret;

}

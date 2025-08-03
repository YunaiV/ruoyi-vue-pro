package cn.iocoder.yudao.module.member.controller.app.user.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Schema(description = "用户 APP - 重置密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMemberUserResetPasswordReqVO {

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "buzhidao")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @Schema(description = "手机验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "手机验证码不能为空")
    @Length(min = 4, max = 6, message = "手机验证码长度为 4-6 位")
    @Pattern(regexp = "^[0-9]+$", message = "手机验证码必须都是数字")
    private String code;

    @Schema(description = "手机号",requiredMode = Schema.RequiredMode.REQUIRED,example = "15878962356")
    @NotBlank(message = "手机号不能为空")
    @Mobile
    private String mobile;

}

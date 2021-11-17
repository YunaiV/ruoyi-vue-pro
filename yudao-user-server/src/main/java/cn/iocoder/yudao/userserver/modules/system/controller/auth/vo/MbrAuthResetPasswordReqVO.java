package cn.iocoder.yudao.userserver.modules.system.controller.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@ApiModel("重置密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbrAuthResetPasswordReqVO {

    /**
     * 修改密码校验规则
     */
    public interface updatePasswordValidView {
    }

    /**
     * 忘记密码校验规则
     */
    public interface resetPasswordValidView {
    }

    @ApiModelProperty(value = "用户旧密码", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空",groups = updatePasswordValidView.class)
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "buzhidao")
    @NotEmpty(message = "新密码不能为空",groups = {updatePasswordValidView.class,resetPasswordValidView.class})
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @ApiModelProperty(value = "手机验证码", required = true, example = "1024")
    @NotEmpty(message = "手机验证码不能为空",groups = resetPasswordValidView.class)
    @Length(min = 4, max = 6, message = "手机验证码长度为 4-6 位")
    @Pattern(regexp = "^[0-9]+$", message = "手机验证码必须都是数字")
    private String code;
}

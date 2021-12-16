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

@ApiModel("修改密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbrAuthUpdatePasswordReqVO {

    @ApiModelProperty(value = "用户旧密码", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "buzhidao")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;
}

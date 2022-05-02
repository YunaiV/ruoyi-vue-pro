package cn.iocoder.yudao.module.system.controller.admin.user.vo.profile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@ApiModel("管理后台 - 用户个人中心更新密码 Request VO")
@Data
public class UserProfileUpdatePasswordReqVO {

    @ApiModelProperty(value = "旧密码", required = true, example = "123456")
    @NotEmpty(message = "旧密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "654321")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String newPassword;

}

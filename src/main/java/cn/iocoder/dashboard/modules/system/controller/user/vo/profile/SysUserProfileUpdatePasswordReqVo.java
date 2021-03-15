package cn.iocoder.dashboard.modules.system.controller.user.vo.profile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 类名称：SysUserUpdatePersonalPasswordReqVo
 * ***********************
 * <p>
 * 类描述：更新用户个人密码
 *
 * @author deng on 2021/3/15 22:04
 */
@ApiModel("用户个人中心更新密码 Response VO")
@Data
public class SysUserProfileUpdatePasswordReqVo {

    @ApiModelProperty(value = "用户编号", required = true, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @ApiModelProperty(value = "旧密码", required = true, example = "123456")
    @NotEmpty(message = "旧密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "654321")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String newPassword;
}
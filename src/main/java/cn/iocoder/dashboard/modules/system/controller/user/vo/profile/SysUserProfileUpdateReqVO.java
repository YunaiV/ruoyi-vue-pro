package cn.iocoder.dashboard.modules.system.controller.user.vo.profile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ApiModel("用户个人信息更新 Request VO")
@Data
public class SysUserProfileUpdateReqVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋艿")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickname;

    @ApiModelProperty(value = "用户邮箱", example = "yudao@iocoder.cn")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @ApiModelProperty(value = "手机号码", example = "15601691300")
    @Pattern(regexp = "^1(3|4|5|7|8)\\d{9}$",message = "手机号码格式错误")
    private String mobile;

    @ApiModelProperty(value = "用户性别", example = "1", notes = "参见 SysSexEnum 枚举类")
    private Integer sex;

    @ApiModelProperty(value = "用户头像", example = "http://www.iocoder.cn/xxx.png")
    private String avatar;

}

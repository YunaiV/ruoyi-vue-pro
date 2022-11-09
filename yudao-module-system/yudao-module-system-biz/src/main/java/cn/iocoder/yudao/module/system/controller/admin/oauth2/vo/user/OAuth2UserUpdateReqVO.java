package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@ApiModel("管理后台 - OAuth2 更新用户基本信息 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserUpdateReqVO {

    @ApiModelProperty(value = "用户昵称", required = true, example = "芋艿")
    @Size(max = 30, message = "用户昵称长度不能超过 30 个字符")
    private String nickname;

    @ApiModelProperty(value = "用户邮箱", example = "yudao@iocoder.cn")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @ApiModelProperty(value = "手机号码", example = "15601691300")
    @Length(min = 11, max = 11, message = "手机号长度必须 11 位")
    private String mobile;

    @ApiModelProperty(value = "用户性别", example = "1", notes = "参见 SexEnum 枚举类")
    private Integer sex;

}

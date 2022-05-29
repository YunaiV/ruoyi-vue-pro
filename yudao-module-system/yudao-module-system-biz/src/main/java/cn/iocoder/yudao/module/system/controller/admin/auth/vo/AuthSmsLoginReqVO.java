package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@ApiModel("管理后台 - 短信验证码的呢老姑 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSmsLoginReqVO {

    @ApiModelProperty(value = "手机号", required = true, example = "yudaoyuanma")
    @NotEmpty(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号格式错误，仅支持大陆手机号")
    @Pattern(regexp = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$", message = "账号格式为数字以及字母")
    private String mobile;



    @ApiModelProperty(value = "短信验证码", required = true, example = "1024", notes = "验证码开启时，需要传递")
    @NotEmpty(message = "验证码不能为空", groups = CodeEnableGroup.class)
    private String code;

    /**
     * 开启验证码的 Group
     */
    public interface CodeEnableGroup {}

}

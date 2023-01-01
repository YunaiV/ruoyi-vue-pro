package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@ApiModel("管理后台 - 短信验证码的登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSmsLoginReqVO {

    @ApiModelProperty(value = "手机号", required = true, example = "yudaoyuanma")
    @NotEmpty(message = "手机号不能为空")
    @Mobile
    private String mobile;

    @ApiModelProperty(value = "短信验证码", required = true, example = "1024")
    @NotEmpty(message = "验证码不能为空")
    private String code;

}

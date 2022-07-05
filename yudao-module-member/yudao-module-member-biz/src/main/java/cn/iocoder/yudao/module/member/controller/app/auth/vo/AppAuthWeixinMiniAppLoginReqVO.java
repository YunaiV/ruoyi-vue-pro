package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@ApiModel("用户 APP - 微信小程序手机登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniAppLoginReqVO {

    @ApiModelProperty(value = "手机 code", required = true, example = "hello", notes = "小程序通过 wx.getPhoneNumber 方法获得")
    @NotEmpty(message = "手机 code 不能为空")
    private String phoneCode;

    @ApiModelProperty(value = "登录 code", required = true, example = "word", notes = "小程序通过 wx.login 方法获得")
    @NotEmpty(message = "登录 code 不能为空")
    private String loginCode;

}

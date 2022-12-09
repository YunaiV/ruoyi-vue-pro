package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Schema(title = "用户 APP - 微信小程序手机登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniAppLoginReqVO {

    @Schema(title = "手机 code", required = true, example = "hello", description = "小程序通过 wx.getPhoneNumber 方法获得")
    @NotEmpty(message = "手机 code 不能为空")
    private String phoneCode;

    @Schema(title = "登录 code", required = true, example = "word", description = "小程序通过 wx.login 方法获得")
    @NotEmpty(message = "登录 code 不能为空")
    private String loginCode;

}

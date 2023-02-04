package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Schema(description = "用户 APP - 微信小程序手机登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniAppLoginReqVO {

    @Schema(description = "手机 code,小程序通过 wx.getPhoneNumber 方法获得", required = true, example = "hello")
    @NotEmpty(message = "手机 code 不能为空")
    private String phoneCode;

    @Schema(description = "登录 code,小程序通过 wx.login 方法获得", required = true, example = "word")
    @NotEmpty(message = "登录 code 不能为空")
    private String loginCode;

}

package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "用户 APP - 微信小程序手机登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthWeixinMiniAppLoginReqVO {

    @Schema(description = "手机 code，小程序通过 wx.getPhoneNumber 方法获得", requiredMode = Schema.RequiredMode.REQUIRED, example = "hello")
    @NotEmpty(message = "手机 code 不能为空")
    private String phoneCode;

    @Schema(description = "登录 code，小程序通过 wx.login 方法获得", requiredMode = Schema.RequiredMode.REQUIRED, example = "word")
    @NotEmpty(message = "登录 code 不能为空")
    private String loginCode;

    @Schema(description = "state", requiredMode = Schema.RequiredMode.REQUIRED, example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    @NotEmpty(message = "state 不能为空")
    private String state;

}

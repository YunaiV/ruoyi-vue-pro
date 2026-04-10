package cn.iocoder.yudao.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "用户 APP - 基于微信小程序的授权码，修改手机 Request VO")
@Data
public class AppMemberUserUpdateMobileByWeixinReqVO {

    @Schema(description = "手机 code，小程序通过 wx.getPhoneNumber 方法获得",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "hello")
    @NotEmpty(message = "手机 code 不能为空")
    private String code;

}

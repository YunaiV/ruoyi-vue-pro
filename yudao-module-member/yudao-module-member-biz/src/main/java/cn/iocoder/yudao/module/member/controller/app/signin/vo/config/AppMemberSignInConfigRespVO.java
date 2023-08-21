package cn.iocoder.yudao.module.member.controller.app.signin.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 签到规则 Response VO")
@Data
public class AppMemberSignInConfigRespVO {

    @Schema(description = "签到第 x 天", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

}

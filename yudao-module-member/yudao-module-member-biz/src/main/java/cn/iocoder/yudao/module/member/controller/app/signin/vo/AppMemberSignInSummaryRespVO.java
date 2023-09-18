package cn.iocoder.yudao.module.member.controller.app.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户签到统计信息 Response VO")
@Data
public class AppMemberSignInSummaryRespVO {

    @Schema(description = "持续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer continuousDay;

    @Schema(description = "总签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer totalDay;

    @Schema(description = "当天是否签到", requiredMode = Schema.RequiredMode.REQUIRED,example = "true")
    private Boolean todaySignIn ;

}

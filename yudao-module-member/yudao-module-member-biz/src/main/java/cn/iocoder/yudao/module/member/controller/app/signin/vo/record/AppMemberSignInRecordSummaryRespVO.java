package cn.iocoder.yudao.module.member.controller.app.signin.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 个人签到统计 Response VO")
@Data
public class AppMemberSignInRecordSummaryRespVO {

    @Schema(description = "总签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer totalDay;

    @Schema(description = "连续签到第 x 天", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer continuousDay;

    @Schema(description = "今天是否已签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean todaySignIn;

}

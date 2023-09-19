package cn.iocoder.yudao.module.member.controller.app.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户签到积分 Response VO")
@Data
public class AppMemberSignInRecordRespVO {

    @Schema(description = "第几天签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer day;

    @Schema(description = "签到的分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "签到时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

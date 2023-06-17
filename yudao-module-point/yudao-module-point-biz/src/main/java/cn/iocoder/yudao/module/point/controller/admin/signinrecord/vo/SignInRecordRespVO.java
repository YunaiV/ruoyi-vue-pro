package cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户签到积分 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInRecordRespVO extends SignInRecordBaseVO {

    @Schema(description = "签到自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    private Long id;

    @Schema(description = "签到时间")
    private LocalDateTime createTime;

}

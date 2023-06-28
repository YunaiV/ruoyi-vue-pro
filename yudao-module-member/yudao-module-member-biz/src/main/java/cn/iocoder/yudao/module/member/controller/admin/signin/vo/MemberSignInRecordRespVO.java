package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户签到积分 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignInRecordRespVO extends MemberSignInRecordBaseVO {

    @Schema(description = "签到自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    private Long id;

    @Schema(description = "签到时间")
    private LocalDateTime createTime;

}

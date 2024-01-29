package cn.iocoder.yudao.module.member.controller.admin.signin.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到记录 Response VO")
@Data
public class MemberSignInRecordRespVO {

    @Schema(description = "签到自增 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    private Long id;

    @Schema(description = "签到用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "6507")
    private Long userId;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "第几天签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer day;

    @Schema(description = "签到的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "签到时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}

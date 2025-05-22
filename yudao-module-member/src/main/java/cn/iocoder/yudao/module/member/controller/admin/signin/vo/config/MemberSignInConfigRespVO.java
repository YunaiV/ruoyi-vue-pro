package cn.iocoder.yudao.module.member.controller.admin.signin.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到规则 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignInConfigRespVO extends MemberSignInConfigBaseVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20937")
    private Integer id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

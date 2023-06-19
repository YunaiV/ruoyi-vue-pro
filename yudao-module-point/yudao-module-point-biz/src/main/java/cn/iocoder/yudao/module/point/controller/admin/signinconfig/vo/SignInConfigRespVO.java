package cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分签到规则 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInConfigRespVO extends SignInConfigBaseVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20937")
    private Integer id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}

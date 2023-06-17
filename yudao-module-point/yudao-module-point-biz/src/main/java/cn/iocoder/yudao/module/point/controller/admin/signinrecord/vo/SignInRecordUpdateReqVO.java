package cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 用户签到积分更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SignInRecordUpdateReqVO extends SignInRecordBaseVO {

    @Schema(description = "签到自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    @NotNull(message = "签到自增id不能为空")
    private Long id;

}

package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 用户签到积分更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberSignInRecordUpdateReqVO extends MemberSignInRecordBaseVO {

    @Schema(description = "签到自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11903")
    @NotNull(message = "签到自增id不能为空")
    private Long id;

}

package cn.iocoder.yudao.module.member.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 用户修改余额 Request VO")
@Data
@ToString(callSuper = true)
public class MemberUserUpdateBalanceReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "变动余额，正数为增加，负数为减少", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "变动余额不能为空")
    private Integer balance;

}

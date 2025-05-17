package cn.iocoder.yudao.module.member.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 用户修改积分 Request VO")
@Data
@ToString(callSuper = true)
public class MemberUserUpdatePointReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "变动积分，正数为增加，负数为减少", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "变动积分不能为空")
    private Integer point;

}

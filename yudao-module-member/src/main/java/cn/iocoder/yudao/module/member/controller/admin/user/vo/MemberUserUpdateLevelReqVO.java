package cn.iocoder.yudao.module.member.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 用户修改等级 Request VO")
@Data
@ToString(callSuper = true)
public class MemberUserUpdateLevelReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    /**
     * 取消用户等级时，值为空
     */
    @Schema(description = "用户等级编号", example = "1")
    private Long levelId;

    @Schema(description = "修改原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "推广需要")
    @NotBlank(message = "修改原因不能为空")
    private String reason;

}

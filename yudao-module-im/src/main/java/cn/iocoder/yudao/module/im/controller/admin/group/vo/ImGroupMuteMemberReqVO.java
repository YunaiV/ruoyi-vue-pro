package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 成员禁言 Request VO")
@Data
public class ImGroupMuteMemberReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "群编号不能为空")
    private Long groupId;

    @Schema(description = "被禁言的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    // TODO @AI：是不是改成禁言结束时间更好？因为禁言时长可能会比较大，单位秒不太好理解。
    @Schema(description = "禁言时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "600")
    @NotNull(message = "禁言时长不能为空")
    @Min(value = 1, message = "禁言时长至少 1 秒")
    private Integer mutedSeconds;

}

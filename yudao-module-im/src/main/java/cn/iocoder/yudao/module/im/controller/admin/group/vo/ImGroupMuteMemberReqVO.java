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
    private Long id;

    @Schema(description = "被禁言的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "禁言时长（秒），0 表示永久禁言", requiredMode = Schema.RequiredMode.REQUIRED, example = "600")
    @NotNull(message = "禁言时长不能为空")
    @Min(value = 0, message = "禁言时长不能小于 0 秒")
    private Integer mutedSeconds;

}

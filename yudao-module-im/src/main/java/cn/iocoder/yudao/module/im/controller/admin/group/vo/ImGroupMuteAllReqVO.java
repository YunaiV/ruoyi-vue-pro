package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 全群禁言 / 取消 Request VO")
@Data
public class ImGroupMuteAllReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "是否全群禁言", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否全群禁言不能为空")
    private Boolean mutedAll;

}

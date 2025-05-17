package cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 客服会话置顶 Request VO")
@Data
public class KeFuConversationUpdatePinnedReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23202")
    @NotNull(message = "会话编号不能为空")
    private Long id;

    @Schema(description = "管理端置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "管理端置顶不能为空")
    private Boolean adminPinned;

}
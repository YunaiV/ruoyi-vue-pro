package cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 我的对话修改 Request VO")
@Data
public class Ai1ChatConversationUpdateMyReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体舰队的航速")
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过 50 个字符")
    private String title;

}

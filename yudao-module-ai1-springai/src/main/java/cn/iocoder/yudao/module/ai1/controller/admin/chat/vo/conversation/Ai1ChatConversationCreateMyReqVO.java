package cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI1 我的对话创建 Request VO")
@Data
public class Ai1ChatConversationCreateMyReqVO {

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Agent 编号不能为空")
    private Long agentId;

}

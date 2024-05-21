package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - AI 聊天消息发送 Request VO")
@Data
public class AiChatMessageSendReqVO {

    @Schema(description = "聊天对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "聊天对话编号不能为空")
    private Long conversationId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "帮我写个 Java 算法")
    @NotEmpty(message = "聊天内容不能为空")
    private String content;

    @Schema(description = "是否携带上下文", example = "true")
    private Boolean useContext;

}

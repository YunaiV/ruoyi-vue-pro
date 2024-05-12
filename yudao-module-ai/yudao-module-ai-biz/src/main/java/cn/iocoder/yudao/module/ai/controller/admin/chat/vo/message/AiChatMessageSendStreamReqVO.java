package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天消息发送 Request VO")
@Data
public class AiChatMessageSendStreamReqVO {

    @Schema(description = "提问的 messageId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "提问的 messageId 不能为空")
    private Long id;

}

package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天会话 Response VO")
@Data
public class AiChatConversationListReqVO {

    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是一个标题")
    private String title;

}

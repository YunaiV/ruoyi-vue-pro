package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 聊天消息 Add Response VO")
@Data
public class AiChatMessageAddRespVO {

    @Schema(description = "用户信息")
    private AiChatMessageRespVO userMessage;

    @Schema(description = "系统信息")
    private AiChatMessageRespVO systemMessage;
}

package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 聊天消息发送 Response VO")
@Data
public class AiChatMessageSendRespVO {

    @Schema(description = "发送消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private Message send;

    @Schema(description = "接收消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private Message receive;

    @Schema(description = "消息")
    @Data
    public static class Message {

        @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "role")
        private String type; // 参见 MessageType 枚举类

        @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，你好啊")
        private String content;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime createTime;

    }

}

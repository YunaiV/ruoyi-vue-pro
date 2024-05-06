package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天消息 Response VO")
@Data
public class AiChatMessageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long conversationId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "role")
    private String type; // 参见 MessageType 枚举类

    @Schema(description = "用户编号", example = "4096")
    private Long userId; // 仅当 user 发送时非空

    @Schema(description = "角色编号", example = "888")
    private Long roleId; // 仅当 assistant 回复时非空

    @Schema(description = "模型标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-3.5-turbo")
    private String model; // 参见 AiOpenAiModelEnum 枚举类

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long modelId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，你好啊")
    private String content;

    @Schema(description = "消耗 Token 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer tokens;

}

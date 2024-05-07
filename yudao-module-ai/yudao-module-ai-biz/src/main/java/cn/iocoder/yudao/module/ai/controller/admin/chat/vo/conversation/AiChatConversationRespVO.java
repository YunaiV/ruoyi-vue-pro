package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - AI 聊天会话 Response VO")
@Data
public class AiChatConversationRespVO {

    @Schema(description = "会话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long userId;

    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是一个标题")
    private String title;

    @Schema(description = "是否置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean pinned;

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Long roleId;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long modelId;

    @Schema(description = "模型标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "ERNIE-Bot-turbo-0922")
    private String model;

    @Schema(description = "温度参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.8")
    private Double temperature;

    @Schema(description = "单条回复的最大 Token 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private Integer maxTokens;

    @Schema(description = "上下文的最大 Message 数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer maxContexts;

}

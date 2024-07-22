package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天对话更新【我的】 Request VO")
@Data
public class AiChatConversationUpdateMyReqVO {

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "对话编号不能为空")
    private Long id;

    @Schema(description = "对话标题", example = "我是一个标题")
    private String title;

    @Schema(description = "是否置顶", example = "true")
    private Boolean pinned;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "角色设定", example = "一个快乐的程序员")
    private String systemMessage;

    @Schema(description = "温度参数", example = "0.8")
    private Double temperature;

    @Schema(description = "单条回复的最大 Token 数量", example = "4096")
    private Integer maxTokens;

    @Schema(description = "上下文的最大 Message 数量", example = "10")
    private Integer maxContexts;

}

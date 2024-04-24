package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 看板 message list req
 *
 * @author fansili
 * @time 2024/4/24 17:28
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class ChatMessageListRes {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "聊天ID，关联到特定的会话或对话")
    private Long chatConversationId;

    @Schema(description = "角色ID，用于标识发送消息的用户或系统的身份")
    private Long userId;

    @Schema(description = "消息具体内容，存储用户的发言或者系统响应的文字信息")
    private String message;

    @Schema(description = "消息类型，枚举值可能包括'system'(系统消息)、'user'(用户消息)和'assistant'(助手消息)")
    private String messageType;

    @Schema(description = "在生成消息时采用的Top-K采样大小")
    private Double topK;

    @Schema(description = "Top-P核采样方法的概率阈值")
    private Double topP;

    @Schema(description = "温度参数，用于调整生成回复的随机性和多样性程度，")
    private Double temperature;

}

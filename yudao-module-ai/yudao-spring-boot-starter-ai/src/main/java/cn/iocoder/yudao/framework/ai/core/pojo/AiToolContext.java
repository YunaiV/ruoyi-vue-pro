package cn.iocoder.yudao.framework.ai.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;

/**
 * 工具上下文参数 DTO，让AI工具可以处理当前用户的相关信息
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiToolContext {
    public static final String CONTEXT_KEY = "AI_TOOL_CONTEXT";
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 聊天模型
     */
    private StreamingChatModel chatModel;

    /**
     * 关联的聊天角色Id
     */
    private Long roleId;

    /**
     * 会话Id
     */
    private Long conversationId;
}
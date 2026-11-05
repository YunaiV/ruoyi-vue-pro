package cn.iocoder.yudao.module.ai.framework.ai.core.model.grok;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Grok {@link ChatModel} 实现类
 *
 * 兼容 OpenAI 接口，复用 OpenAiChatModel，参见 {@link OpenAiDelegateChatModel}
 */
@Slf4j
public class GrokChatModel extends OpenAiDelegateChatModel {

    public static final String BASE_URL = "https://api.x.ai/v1";
    public static final String COMPLETE_PATH = "/v1/chat/completions";
    public static final String MODEL_DEFAULT = "grok-4.3";

    public GrokChatModel(ChatModel openAiChatModel) {
        super(openAiChatModel);
    }

}

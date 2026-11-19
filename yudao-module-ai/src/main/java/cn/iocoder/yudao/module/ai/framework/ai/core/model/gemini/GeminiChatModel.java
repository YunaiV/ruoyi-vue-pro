package cn.iocoder.yudao.module.ai.framework.ai.core.model.gemini;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

/**
 * 谷歌 Gemini {@link ChatModel} 实现类，基于 Google AI Studio 提供的 <a href="https://ai.google.dev/gemini-api/docs/openai">OpenAI 兼容方案</a>
 *
 * 兼容 OpenAI 接口，复用 OpenAiChatModel，参见 {@link OpenAiDelegateChatModel}
 *
 * @author 芋道源码
 */
@Slf4j
public class GeminiChatModel extends OpenAiDelegateChatModel {

    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai/";
    public static final String COMPLETE_PATH = "/chat/completions";
    public static final String MODEL_DEFAULT = "gemini-2.5-flash";

    public GeminiChatModel(ChatModel openAiChatModel) {
        super(openAiChatModel);
    }

}

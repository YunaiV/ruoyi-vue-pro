package cn.iocoder.yudao.module.ai.framework.ai.core.model.grok;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * Grok {@link ChatModel} 实现类
 *
 *
 */
@Slf4j
@RequiredArgsConstructor
public class GrokChatModel implements ChatModel {

    public static final String BASE_URL = "https://api.x.ai";
    public static final String COMPLETE_PATH = "/v1/chat/completions";
    public static final String MODEL_DEFAULT = "grok-4-fast-reasoning";

    /**
     * 兼容 OpenAI 接口，进行复用
     */
    private final ChatModel openAiChatModel;

    @Override
    public ChatResponse call(Prompt prompt) {
        return openAiChatModel.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return openAiChatModel.stream(prompt);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return openAiChatModel.getDefaultOptions();
    }

}

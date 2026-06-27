package cn.iocoder.yudao.module.ai.framework.ai.core.model.moonshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import reactor.core.publisher.Flux;

/**
 * 月之暗面 {@link ChatModel} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class MoonshotChatModel implements ChatModel {

    public static final String BASE_URL = "https://api.moonshot.cn";

    public static final String COMPLETE_PATH = "/v1/chat/completions";

    public static final String MODEL_DEFAULT = "kimi-k2.6";

    /**
     * 兼容 OpenAI 接口，复用 DeepSeek 客户端
     */
    private final DeepSeekChatModel deepSeekChatModel;

    @Override
    public ChatResponse call(Prompt prompt) {
        return deepSeekChatModel.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return deepSeekChatModel.stream(prompt);
    }

    @Override
    public ChatOptions getOptions() {
        return deepSeekChatModel.getOptions();
    }

    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public ChatOptions getDefaultOptions() {
        return getOptions();
    }

}

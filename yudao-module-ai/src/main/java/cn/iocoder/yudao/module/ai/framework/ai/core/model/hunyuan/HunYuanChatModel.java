package cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * 腾讯混元 {@link ChatModel} 实现类
 *
 * 基于 <a href="https://cloud.tencent.com/document/product/1823/132252">TokenHub OpenAI 兼容接口</a> 实现
 *
 * @author fansili
 */
@Slf4j
@RequiredArgsConstructor
public class HunYuanChatModel implements ChatModel {

    public static final String BASE_URL = "https://tokenhub.tencentmaas.com";
    public static final String COMPLETE_PATH = "/v1/chat/completions";

    public static final String MODEL_DEFAULT = "hy3-preview";

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
    public ChatOptions getOptions() {
        return openAiChatModel.getOptions();
    }

    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public ChatOptions getDefaultOptions() {
        return getOptions();
    }

}

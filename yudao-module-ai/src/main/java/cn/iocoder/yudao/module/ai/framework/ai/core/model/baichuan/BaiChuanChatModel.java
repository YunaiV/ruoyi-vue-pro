package cn.iocoder.yudao.module.ai.framework.ai.core.model.baichuan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Flux;

/**
 * 百川 {@link ChatModel} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class BaiChuanChatModel implements ChatModel {

    public static final String BASE_URL = "https://api.baichuan-ai.com";

    public static final String MODEL_DEFAULT = "Baichuan4-Turbo";

    /**
     * 兼容 OpenAI 接口，进行复用
     */
    private final OpenAiChatModel openAiChatModel;

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

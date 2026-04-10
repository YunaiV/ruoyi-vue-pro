package cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Flux;

/**
 * 硅基流动 {@link ChatModel} 实现类
 *
 * 1. API 文档：<a href="https://docs.siliconflow.cn/cn/api-reference/chat-completions/chat-completions">API 文档</a>
 *
 * @author fansili
 */
@Slf4j
@RequiredArgsConstructor
public class SiliconFlowChatModel implements ChatModel {

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

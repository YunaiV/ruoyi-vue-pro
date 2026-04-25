package cn.iocoder.yudao.module.ai.framework.ai.core.model.runpod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * Runpod {@link ChatModel} 实现类
 *
 * <p>Runpod 暴露与 OpenAI 完全兼容的端点：
 * {@code https://api.runpod.ai/v2/{endpointId}/openai/v1}</p>
 *
 * <p>因此直接复用 {@link org.springframework.ai.openai.OpenAiChatModel} 作为底层实现，
 * 无需自定义 HTTP 客户端，支持 call（同步）和 stream（流式）两种调用方式。</p>
 *
 * <p>环境变量：
 * <ul>
 *   <li>{@code RUNPOD_API_KEY}（必需）</li>
 *   <li>{@code RUNPOD_BASE_URL}（可选，默认 {@value #DEFAULT_BASE_URL}）</li>
 *   <li>{@code RUNPOD_MODEL_ID}（可选，默认 {@value #DEFAULT_ENDPOINT_ID}，用于 URL 路径）</li>
 * </ul>
 * </p>
 *
 * @author deepay
 */
@Slf4j
@RequiredArgsConstructor
public class RunpodChatModel implements ChatModel {

    /**
     * Runpod API 默认 base URL
     */
    public static final String DEFAULT_BASE_URL = "https://api.runpod.ai";

    /**
     * 默认 Runpod 端点 ID（URL 路径中使用）
     */
    public static final String DEFAULT_ENDPOINT_ID = "qwen3-32b-awq";

    /**
     * 默认实际模型名称（发送给 OpenAI 接口的 model 参数）
     */
    public static final String DEFAULT_MODEL_NAME = "Qwen/Qwen3-32B-AWQ";

    /**
     * Runpod OpenAI 兼容端点的路径后缀
     */
    public static final String OPENAI_COMPAT_PATH = "/openai/v1";

    /**
     * 复用 OpenAI 兼容接口
     */
    private final ChatModel openAiChatModel;

    // TODO @deepay：Runpod 图片生成（imageModel）预留，待 PR4 实现。
    //   参考：npm @runpod/ai-sdk-provider → runpod.imageModel('google/nano-banana-2')

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

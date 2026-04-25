package cn.iocoder.yudao.module.ai.framework.ai.core.model.vllm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * vLLM {@link ChatModel} 实现类
 *
 * <p>vLLM 启动后暴露与 OpenAI 完全兼容的 HTTP 服务端点：
 * <ul>
 *   <li>聊天：{@code POST {baseUrl}/v1/chat/completions}</li>
 *   <li>补全：{@code POST {baseUrl}/v1/completions}</li>
 *   <li>嵌入：{@code POST {baseUrl}/v1/embeddings}</li>
 * </ul>
 * 因此直接复用 {@link org.springframework.ai.openai.OpenAiChatModel} 作为底层实现，
 * 无需任何自定义 HTTP 客户端。</p>
 *
 * <p>典型启动命令：
 * <pre>{@code
 * vllm serve NousResearch/Meta-Llama-3-8B-Instruct \
 *   --dtype auto \
 *   --api-key token-abc123
 * }</pre>
 * 或通过 Docker 镜像。启动后默认监听 {@code http://localhost:8000}。
 * </p>
 *
 * <p>配置示例（application-local.yaml）：
 * <pre>{@code
 * yudao:
 *   ai:
 *     vllm:
 *       enable: true
 *       base-url: http://localhost:8000   # vLLM 服务地址
 *       api-key: token-abc123             # --api-key 参数对应的值
 *       model: NousResearch/Meta-Llama-3-8B-Instruct
 * }</pre>
 * </p>
 *
 * <p>vLLM 专属扩展参数（如 {@code top_k}）可通过
 * {@link org.springframework.ai.openai.OpenAiChatOptions} 的 {@code additionalOptions}
 * 字段传入，例如：{@code extra_body={"top_k": 50}}。</p>
 *
 * @author deepay
 */
@Slf4j
@RequiredArgsConstructor
public class VllmChatModel implements ChatModel {

    /**
     * vLLM 本地服务默认地址
     */
    public static final String DEFAULT_BASE_URL = "http://localhost:8000";

    /**
     * OpenAI 兼容的聊天补全路径（vLLM 与 OpenAI 相同）
     */
    public static final String COMPLETIONS_PATH = "/v1/chat/completions";

    /**
     * 复用 OpenAI 兼容接口
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

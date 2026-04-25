package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.vllm.VllmChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link VllmChatModel} 单元测试
 *
 * <p>使用 Mockito mock 底层 {@link ChatModel}，
 * 验证 VllmChatModel 正确委托调用，无需真实 vLLM 服务。</p>
 *
 * <p>集成测试（{@code @Disabled}）需本地已运行：
 * <pre>{@code
 * vllm serve NousResearch/Meta-Llama-3-8B-Instruct \
 *   --dtype auto \
 *   --api-key token-abc123
 * }</pre>
 * 或设置环境变量 {@code VLLM_BASE_URL} / {@code VLLM_API_KEY} / {@code VLLM_MODEL}。
 * </p>
 *
 * @author deepay
 */
public class VllmChatModelTests {

    // ========== Mock 单元测试（不需要真实 vLLM 服务）==========

    @Test
    public void testCall_delegatesToOpenAiChatModel() {
        // 1. mock 底层 ChatModel
        ChatModel mockInner = mock(ChatModel.class);
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockInner.call(any(Prompt.class))).thenReturn(mockResponse);

        // 2. 构建 VllmChatModel
        VllmChatModel vllmModel = new VllmChatModel(mockInner);

        // 3. 调用
        Prompt prompt = new Prompt(List.of(new UserMessage("Hello from vLLM test")));
        ChatResponse response = vllmModel.call(prompt);

        // 4. 验证
        assertSame(mockResponse, response);
        verify(mockInner, times(1)).call(prompt);
    }

    @Test
    public void testStream_delegatesToOpenAiChatModel() {
        ChatModel mockInner = mock(ChatModel.class);
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockInner.call(any(Prompt.class))).thenReturn(mockResponse);

        VllmChatModel vllmModel = new VllmChatModel(mockInner);

        Prompt prompt = new Prompt(List.of(new UserMessage("stream test")));
        assertDoesNotThrow(() -> vllmModel.stream(prompt));
    }

    @Test
    public void testGetDefaultOptions_delegatesToInner() {
        ChatModel mockInner = mock(ChatModel.class);
        when(mockInner.getDefaultOptions()).thenReturn(
                OpenAiChatOptions.builder().model("meta-llama/Meta-Llama-3-8B-Instruct").build());

        VllmChatModel vllmModel = new VllmChatModel(mockInner);

        assertNotNull(vllmModel.getDefaultOptions());
        verify(mockInner, times(1)).getDefaultOptions();
    }

    @Test
    public void testConstants() {
        assertEquals("http://localhost:8000", VllmChatModel.DEFAULT_BASE_URL);
        assertEquals("/v1/chat/completions", VllmChatModel.COMPLETIONS_PATH);
    }

    // ========== 集成测试（需要本地 vLLM 服务，默认跳过）==========

    @Test
    @Disabled("需要本地运行 vLLM 服务才能执行：vllm serve {model} --api-key token-abc123")
    public void testCall_integration() {
        String baseUrl = System.getenv().getOrDefault("VLLM_BASE_URL", VllmChatModel.DEFAULT_BASE_URL);
        String apiKey  = System.getenv().getOrDefault("VLLM_API_KEY", "token-abc123");
        String model   = System.getenv().getOrDefault("VLLM_MODEL", "NousResearch/Meta-Llama-3-8B-Instruct");

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .maxTokens(128)
                        .temperature(0.7)
                        .build())
                .build();

        VllmChatModel vllmModel = new VllmChatModel(openAiChatModel);

        Prompt prompt = new Prompt(List.of(
                new SystemMessage("You are a helpful assistant."),
                new UserMessage("What is vLLM? Reply in one sentence.")));

        ChatResponse response = vllmModel.call(prompt);
        System.out.println("[VllmChatModelTests] integration response: " + response);
        assertNotNull(response);
    }

}

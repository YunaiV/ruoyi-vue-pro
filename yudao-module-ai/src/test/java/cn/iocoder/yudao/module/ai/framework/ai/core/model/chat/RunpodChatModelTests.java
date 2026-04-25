package cn.iocoder.yudao.module.ai.framework.ai.core.model.chat;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.runpod.RunpodChatModel;
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
 * {@link RunpodChatModel} 单元测试
 *
 * <p>使用 Mockito mock 底层 {@link ChatModel}（即 OpenAiChatModel），
 * 验证 RunpodChatModel 正确委托调用，无需真实 Runpod API Key。</p>
 *
 * @author deepay
 */
public class RunpodChatModelTests {

    // ========== Mock 单元测试（不需要真实 API Key）==========

    @Test
    public void testCall_delegatesToOpenAiChatModel() {
        // 1. mock 底层 ChatModel（OpenAiChatModel）
        ChatModel mockInner = mock(ChatModel.class);
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockInner.call(any(Prompt.class))).thenReturn(mockResponse);

        // 2. 构建 RunpodChatModel
        RunpodChatModel runpodModel = new RunpodChatModel(mockInner);

        // 3. 调用
        Prompt prompt = new Prompt(List.of(new UserMessage("What is Runpod?")));
        ChatResponse response = runpodModel.call(prompt);

        // 4. 验证：call 委托给了内层模型，且返回结果正确
        assertSame(mockResponse, response);
        verify(mockInner, times(1)).call(prompt);
    }

    @Test
    public void testStream_delegatesToOpenAiChatModel() {
        // 1. mock 底层 ChatModel
        ChatModel mockInner = mock(ChatModel.class);
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockInner.call(any(Prompt.class))).thenReturn(mockResponse);

        // 2. 构建 RunpodChatModel
        RunpodChatModel runpodModel = new RunpodChatModel(mockInner);

        // 3. 调用 stream（RunpodChatModel.stream 委托给 openAiChatModel.stream）
        Prompt prompt = new Prompt(List.of(new UserMessage("What is Runpod?")));
        // stream 在有真实 OpenAiChatModel 时才完整验证；此处只验证无异常
        assertDoesNotThrow(() -> runpodModel.stream(prompt));
    }

    @Test
    public void testGetDefaultOptions_returnsNonNull() {
        ChatModel mockInner = mock(ChatModel.class);
        when(mockInner.getDefaultOptions()).thenReturn(
                OpenAiChatOptions.builder().model(RunpodChatModel.DEFAULT_MODEL_NAME).build());

        RunpodChatModel runpodModel = new RunpodChatModel(mockInner);

        assertNotNull(runpodModel.getDefaultOptions());
        verify(mockInner, times(1)).getDefaultOptions();
    }

    @Test
    public void testConstants() {
        assertEquals("https://api.runpod.ai", RunpodChatModel.DEFAULT_BASE_URL);
        assertEquals("qwen3-32b-awq", RunpodChatModel.DEFAULT_ENDPOINT_ID);
        assertEquals("Qwen/Qwen3-32B-AWQ", RunpodChatModel.DEFAULT_MODEL_NAME);
        assertEquals("/openai/v1", RunpodChatModel.OPENAI_COMPAT_PATH);
    }

    // ========== 集成测试（需要真实 Runpod API Key，默认跳过）==========

    @Test
    @Disabled("需要真实 RUNPOD_API_KEY 才能运行")
    public void testCall_integration() {
        // 真实调用：https://api.runpod.ai/v2/qwen3-32b-awq/openai/v1
        String apiKey = System.getenv("RUNPOD_API_KEY");
        String baseUrl = System.getenv().getOrDefault("RUNPOD_BASE_URL", RunpodChatModel.DEFAULT_BASE_URL);
        String endpointId = System.getenv().getOrDefault("RUNPOD_MODEL_ID", RunpodChatModel.DEFAULT_ENDPOINT_ID);

        String openAiBaseUrl = baseUrl + "/v2/" + endpointId + RunpodChatModel.OPENAI_COMPAT_PATH;
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(openAiBaseUrl)
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(RunpodChatModel.DEFAULT_MODEL_NAME)
                        .maxTokens(256)
                        .temperature(0.7)
                        .build())
                .build();

        RunpodChatModel runpodModel = new RunpodChatModel(openAiChatModel);

        Prompt prompt = new Prompt(List.of(
                new SystemMessage("You are a helpful assistant."),
                new UserMessage("What is Runpod? Answer in one sentence.")));

        ChatResponse response = runpodModel.call(prompt);
        System.out.println("[RunpodChatModelTests] integration response: " + response);
        assertNotNull(response);
    }

}

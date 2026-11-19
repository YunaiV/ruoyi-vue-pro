package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.grok.GrokChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.services.async.chat.ChatCompletionServiceAsync;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AiChatUtils} 的单元测试类
 *
 * 直接构造真实的 Spring AI 对象，不启动 Spring 容器，不请求外部模型
 *
 * @author 芋道源码
 */
public class AiChatUtilsTest {

    /**
     * 自定义型号：不在 {@link AiChatUtils#TONG_YI_MULTI_MODELS} 名单中
     */
    private static final String CUSTOM_MODEL = "my-qwen-vl";
    /**
     * 名单内的型号
     */
    private static final String LISTED_MODEL = "qwen3-vl-plus";

    private static final String OPENAI = AiPlatformEnum.OPENAI.getPlatform();

    // ========== buildChatOptions ==========

    @Test
    public void testBuildChatOptions_tongYiCustomModelWithMedia() {
        // 调用
        DashScopeChatOptions options = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                CUSTOM_MODEL, 0.7, 1024, null, null, true);
        // 断言：含 Media 走多模态接口
        assertEquals(Boolean.TRUE, options.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_tongYiCustomModelTextOnly() {
        // 调用
        DashScopeChatOptions options = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                CUSTOM_MODEL, 0.7, 1024, null, null, false);
        // 断言：纯文本保持原通道
        assertEquals(Boolean.FALSE, options.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_tongYiListedModelTextOnly() {
        // 调用：原有重载，默认不含 Media
        DashScopeChatOptions options = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                LISTED_MODEL, 0.7, 1024);
        // 断言：名单内型号纯文本仍按名单走多模态接口，与改动前一致
        assertEquals(Boolean.TRUE, options.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_openAiStreamUsage() {
        // 调用
        OpenAiChatOptions openAiOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.OPENAI,
                "gpt-test", 0.7, 1024);
        OpenAiChatOptions geminiOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-test", 0.7, 1024);
        OpenAiChatOptions grokOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GROK,
                "grok-test", 0.7, 1024);
        // 断言：仅 OpenAI 开启流式用量，复用 OpenAI 客户端的厂商显式关闭
        // （Spring AI 2.x 流式总是发送 stream_options，未设置时默认 include_usage = true）
        assertTrue(openAiOptions.getStreamOptions().includeUsage());
        assertFalse(geminiOptions.getStreamOptions().includeUsage());
        assertFalse(grokOptions.getStreamOptions().includeUsage());
    }

    @Test
    public void testBuildChatOptions_streamUsageRequestBody() throws Exception {
        // 调用：经 ChatClient 发起流式请求，捕获 OpenAI SDK 实际序列化的请求体
        Map<?, ?> openAiBody = captureOpenAiStreamRequestBody(AiPlatformEnum.OPENAI, "gpt-test");
        Map<?, ?> grokBody = captureOpenAiStreamRequestBody(AiPlatformEnum.GROK, "grok-test");
        // 断言：OpenAI 请求 include_usage = true；Grok 请求 include_usage = false，而不是未设置时默认的 true
        assertEquals(Boolean.TRUE, ((Map<?, ?>) openAiBody.get("stream_options")).get("include_usage"));
        assertEquals(Boolean.FALSE, ((Map<?, ?>) grokBody.get("stream_options")).get("include_usage"));
        assertEquals("grok-test", grokBody.get("model"));
    }

    private static Map<?, ?> captureOpenAiStreamRequestBody(AiPlatformEnum platform, String model) throws Exception {
        // mock OpenAI SDK 的异步客户端：只捕获请求，不发起网络调用
        OpenAIClientAsync clientAsync = mock(OpenAIClientAsync.class, RETURNS_DEEP_STUBS);
        ChatCompletionServiceAsync completions = clientAsync.chat().completions();
        when(completions.createStreaming(any(ChatCompletionCreateParams.class), any()))
                .thenThrow(new IllegalStateException("仅捕获请求"));
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiClient(mock(OpenAIClient.class))
                .openAiClientAsync(clientAsync).build();
        ChatModel chatModel = platform == AiPlatformEnum.GROK ? new GrokChatModel(openAiChatModel) : openAiChatModel;

        // 调用：与 AiChatMessageServiceImpl 一样经 ChatClient 流式调用；捕获请求后的失败忽略
        Prompt prompt = new Prompt("你好", AiChatUtils.buildChatOptions(platform, model, 0.7, 1024));
        ChatClient.create(chatModel).prompt(prompt).stream().chatResponse()
                .onErrorResume(ex -> Flux.empty()).blockLast();
        // 捕获请求
        ArgumentCaptor<ChatCompletionCreateParams> paramsCaptor = ArgumentCaptor.forClass(ChatCompletionCreateParams.class);
        verify(completions).createStreaming(paramsCaptor.capture(), any());
        return JsonUtils.parseObject(com.openai.core.ObjectMappers.jsonMapper()
                .writeValueAsString(paramsCaptor.getValue()._body()), Map.class);
    }

    // ========== buildChatOptions：思考参数 ==========

    @Test
    public void testBuildChatOptions_tongYiNullOptionsKeepsDefault() {
        // 调用：未配置思考
        DashScopeChatOptions options = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                CUSTOM_MODEL, 0.7, 1024, null, null, false, null);
        // 断言：与原有行为一致，固定开启思考、不发预算
        assertEquals(Boolean.TRUE, options.getEnableThinking());
        assertNull(options.getThinkingBudget());
    }

    @Test
    public void testBuildChatOptions_tongYiOptions() {
        // 准备参数
        Map<String, Object> offReasoningOptions = new LinkedHashMap<>();
        offReasoningOptions.put("enable_thinking", false);
        Map<String, Object> budgetReasoningOptions = new LinkedHashMap<>();
        budgetReasoningOptions.put("enable_thinking", true);
        budgetReasoningOptions.put("thinking_budget", 8192);

        // 调用
        DashScopeChatOptions offOptions = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                CUSTOM_MODEL, 0.7, 1024, null, null, false, offReasoningOptions);
        DashScopeChatOptions budgetOptions = (DashScopeChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.ALIBABA,
                CUSTOM_MODEL, 0.7, 1024, null, null, true, budgetReasoningOptions);
        // 断言：映射到原生字段；多模态判断不受影响
        assertEquals(Boolean.FALSE, offOptions.getEnableThinking());
        assertNull(offOptions.getThinkingBudget());
        assertEquals(Boolean.FALSE, offOptions.getMultiModel());
        assertEquals(Boolean.TRUE, budgetOptions.getEnableThinking());
        assertEquals(8192, budgetOptions.getThinkingBudget());
        assertEquals(Boolean.TRUE, budgetOptions.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_openAiNullOptionsUnchanged() {
        // 调用
        OpenAiChatOptions options = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.OPENAI,
                "gpt-test", 0.7, 1024, null, null, false, null);
        // 断言：不发送思考参数
        assertNull(options.getReasoningEffort());
        assertTrue(options.getExtraBody() == null || options.getExtraBody().isEmpty());
        assertTrue(options.getStreamOptions().includeUsage());
    }

    @Test
    public void testBuildChatOptions_openAiOptions() {
        // 准备参数
        Map<String, Object> levelOptions = new LinkedHashMap<>();
        levelOptions.put("reasoning_effort", "high");
        levelOptions.put("reasoning", MapUtil.of("enabled", true));

        // 调用
        OpenAiChatOptions openAiOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.OPENAI,
                "gpt-test", 0.7, 1024, null, null, false, levelOptions);
        OpenAiChatOptions grokOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GROK,
                "grok-test", 0.7, 1024, null, null, false, levelOptions);
        // 断言：reasoning_effort 写强类型字段，且不重复出现在 extraBody；其余键只追加到 extraBody
        assertEquals("high", openAiOptions.getReasoningEffort());
        assertFalse(openAiOptions.getExtraBody().containsKey("reasoning_effort"));
        assertEquals(MapUtil.of("enabled", true), openAiOptions.getExtraBody().get("reasoning"));
        assertEquals(0.7, openAiOptions.getTemperature());
        // 断言：streamUsage 规则不变，仅 OpenAI 开启，复用 OpenAI 客户端的厂商显式关闭
        assertTrue(openAiOptions.getStreamOptions().includeUsage());
        assertFalse(grokOptions.getStreamOptions().includeUsage());
        assertEquals("high", grokOptions.getReasoningEffort());
        // 断言：调用方的思考参数没有被修改
        assertEquals(2, levelOptions.size());
    }

    @Test
    public void testBuildChatOptions_openAiOnlyEffortNoExtraBody() {
        // 准备参数
        Map<String, Object> levelOptions = new LinkedHashMap<>();
        levelOptions.put("reasoning_effort", "low");

        // 调用
        OpenAiChatOptions options = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-test", 0.7, 1024, null, null, false, levelOptions);
        // 断言
        assertEquals("low", options.getReasoningEffort());
        assertTrue(options.getExtraBody() == null || options.getExtraBody().isEmpty());
    }

    @Test
    public void testBuildChatOptions_deepSeekOptionsIgnored() {
        // 准备参数
        Map<String, Object> levelOptions = new LinkedHashMap<>();
        levelOptions.put("thinking", MapUtil.of("type", "disabled"));

        // 调用
        DeepSeekChatOptions withReasoning = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.DEEPSEEK,
                "deepseek-test", 0.7, 1024, null, null, false, levelOptions);
        DeepSeekChatOptions withoutReasoning = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.DEEPSEEK,
                "deepseek-test", 0.7, 1024);
        // 断言：DeepSeek 族的思考参数装在客户端 HTTP 层，options 与未配置时一致
        assertEquals(withoutReasoning.getModel(), withReasoning.getModel());
        assertEquals(withoutReasoning.getTemperature(), withReasoning.getTemperature());
        assertEquals(withoutReasoning.getMaxTokens(), withReasoning.getMaxTokens());
    }

    @Test
    public void testBuildChatOptions_miMo() {
        // 调用
        DeepSeekChatOptions options = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.XIAOMI,
                "mimo-v2.6-flash", 0.7, 1024);
        // 断言：复用 DeepSeek 客户端的选项
        assertEquals("mimo-v2.6-flash", options.getModel());
        assertEquals(0.7, options.getTemperature());
        assertEquals(1024, options.getMaxTokens());
    }

}

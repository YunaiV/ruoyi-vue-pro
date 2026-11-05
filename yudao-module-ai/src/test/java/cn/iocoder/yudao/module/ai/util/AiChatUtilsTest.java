package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.grok.GrokChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiChatModel;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.services.async.chat.ChatCompletionServiceAsync;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// DONE @AI：单测名字不对，对齐下；AiUtilsTest 才对；已合并进 AiUtilsTest，删除 AiUtilsUsageTest
// DONE @AI：代码风格，要对齐 system user test 噢；已按「准备参数 / 调用 / 断言」步骤注释对齐，纯单元测试不启动 Spring 容器
/**
 * {@link AiChatUtils} 的单元测试类
 *
 * 直接构造真实的 Spring AI 对象，不启动 Spring 容器，不请求外部模型
 *
 * @author 芋道源码
 */
public class AiChatUtilsTest {

    /**
     * 通义的自定义型号
     */
    private static final String CUSTOM_MODEL = "my-qwen-vl";

    private static final String OPENAI = AiPlatformEnum.OPENAI.getPlatform();

    // ========== buildChatOptions ==========

    @Test
    public void testBuildChatOptions_tongYiOpenAiCompatible() {
        // 调用：含 Media、纯文本各一次
        OpenAiChatOptions mediaOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, true);
        OpenAiChatOptions textOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, false);
        // 断言：走 OpenAI 兼容接口，图片随消息发送，是否含 Media 不影响 options
        assertEquals(CUSTOM_MODEL, mediaOptions.getModel());
        assertEquals(0.7, mediaOptions.getTemperature());
        assertEquals(1024, mediaOptions.getMaxTokens());
        assertEquals(mediaOptions.getExtraBody(), textOptions.getExtraBody());
        // 断言：不设置 streamOptions，由 Spring AI 默认发送 include_usage = true
        assertNull(mediaOptions.getStreamOptions());
    }

    @Test
    public void testBuildChatOptions_openAiStreamUsage() {
        // 调用
        OpenAiChatOptions openAiOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.OPENAI,
                "gpt-test", 0.7, 1024);
        ChatOptions geminiOptions = AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-test", 0.7, 1024);
        OpenAiChatOptions grokOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GROK,
                "grok-test", 0.7, 1024);
        // 断言：仅 OpenAI 开启流式用量，复用 OpenAI 客户端的 Grok 显式关闭；Gemini 走 Google GenAI 原生客户端
        assertTrue(openAiOptions.getStreamOptions().includeUsage());
        assertInstanceOf(GoogleGenAiChatOptions.class, geminiOptions);
        assertFalse(grokOptions.getStreamOptions().includeUsage());
    }

    @Test
    public void testBuildChatOptions_streamUsageRequestBody() throws Exception {
        // 调用：经 ChatClient 发起流式请求，捕获 OpenAI SDK 实际序列化的请求体
        Map<?, ?> openAiBody = captureOpenAiStreamRequestBody(AiPlatformEnum.OPENAI, "gpt-test");
        Map<?, ?> grokBody = captureOpenAiStreamRequestBody(AiPlatformEnum.GROK, "grok-test");
        Map<?, ?> tongYiBody = captureOpenAiStreamRequestBody(AiPlatformEnum.TONG_YI, CUSTOM_MODEL);
        // 断言：OpenAI 请求带 include_usage = true；Grok 请求带 include_usage = false，
        // 不是 Spring AI 2.x 未设置 stream_options 时默认发送的 true
        assertEquals(Boolean.TRUE, ((Map<?, ?>) openAiBody.get("stream_options")).get("include_usage"));
        assertEquals(Boolean.FALSE, ((Map<?, ?>) grokBody.get("stream_options")).get("include_usage"));
        // 断言：通义请求带默认的 include_usage = true，不带 include_obfuscation；enable_thinking 在请求体顶层
        Map<?, ?> tongYiStreamOptions = (Map<?, ?>) tongYiBody.get("stream_options");
        assertEquals(Boolean.TRUE, tongYiStreamOptions.get("include_usage"));
        assertFalse(tongYiStreamOptions.containsKey("include_obfuscation"));
        assertEquals(Boolean.TRUE, tongYiBody.get("enable_thinking"));
        assertEquals(CUSTOM_MODEL, tongYiBody.get("model"));
    }

    /**
     * 使用 mock 的 OpenAI SDK 异步客户端发起流式请求，捕获请求后直接失败，不请求外部模型
     *
     * @param platform 平台；与生产一致，OpenAI、Grok、通义分别由 {@link OpenAiDelegateChatModel}、{@link GrokChatModel}、{@link TongYiChatModel} 包装 {@link OpenAiChatModel}
     * @param model 模型标志
     * @return OpenAI SDK 序列化后的请求体
     */
    private static Map<?, ?> captureOpenAiStreamRequestBody(AiPlatformEnum platform, String model) throws Exception {
        // mock OpenAI SDK 的异步客户端
        OpenAIClientAsync clientAsync = mock(OpenAIClientAsync.class, RETURNS_DEEP_STUBS);
        ChatCompletionServiceAsync completions = clientAsync.chat().completions();
        when(completions.createStreaming(any(ChatCompletionCreateParams.class), any()))
                .thenThrow(new IllegalStateException("仅捕获请求"));
        ChatModel chatModel = OpenAiChatModel.builder().openAiClient(mock(OpenAIClient.class))
                .openAiClientAsync(clientAsync).build();
        if (platform == AiPlatformEnum.OPENAI) {
            chatModel = new OpenAiDelegateChatModel(chatModel);
        } else if (platform == AiPlatformEnum.GROK) {
            chatModel = new GrokChatModel(chatModel);
        } else if (platform == AiPlatformEnum.TONG_YI) {
            chatModel = new TongYiChatModel(chatModel);
        }

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
        OpenAiChatOptions options = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, false, null);
        // 断言：与原有行为一致，固定开启思考、不发预算
        assertEquals(MapUtil.of("enable_thinking", true), options.getExtraBody());
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
        OpenAiChatOptions offOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, false, offReasoningOptions);
        OpenAiChatOptions budgetOptions = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, true, budgetReasoningOptions);
        // 断言：追加到 extraBody，关闭思考不被默认开启覆盖；关闭时不发预算
        assertEquals(offReasoningOptions, offOptions.getExtraBody());
        assertEquals(budgetReasoningOptions, budgetOptions.getExtraBody());
        // 断言：调用方的思考参数没有被修改
        assertEquals(1, offReasoningOptions.size());
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
        // 断言：streamUsage 规则不变，仅 OpenAI 开启，Grok 显式关闭
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
        OpenAiChatOptions options = (OpenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GROK,
                "grok-test", 0.7, 1024, null, null, false, levelOptions);
        // 断言
        assertEquals("low", options.getReasoningEffort());
        assertTrue(options.getExtraBody() == null || options.getExtraBody().isEmpty());
    }

    @Test
    public void testBuildChatOptions_geminiNullOptionsUnchanged() {
        // 调用：未配置思考
        GoogleGenAiChatOptions options = (GoogleGenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-test", 0.7, 1024, null, null, false, null);
        // 断言：不发送思考参数，其它参数与原有行为一致
        assertNull(options.getThinkingLevel());
        assertNull(options.getThinkingBudget());
        assertEquals("gemini-test", options.getModel());
        assertEquals(0.7, options.getTemperature());
        assertEquals(1024, options.getMaxOutputTokens());
    }

    @Test
    public void testBuildChatOptions_geminiOptions() {
        // 准备参数
        Map<String, Object> highOptions = new LinkedHashMap<>();
        highOptions.put("reasoning_effort", "high");
        Map<String, Object> noneOptions = new LinkedHashMap<>();
        noneOptions.put("reasoning_effort", "none");
        Map<String, Object> budgetOptions = new LinkedHashMap<>();
        budgetOptions.put("thinking_budget", 2048);

        // 调用：thinkingLevel 只适用于 Gemini 3 及以上，none 只适用于更早的模型（由 AiReasoningUtils 按模型校验）
        GoogleGenAiChatOptions high = (GoogleGenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-3-flash-preview", 0.7, 1024, null, null, false, highOptions);
        GoogleGenAiChatOptions none = (GoogleGenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-2.5-flash", 0.7, 1024, null, null, false, noneOptions);
        GoogleGenAiChatOptions budget = (GoogleGenAiChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.GEMINI,
                "gemini-2.5-flash", 0.7, 1024, null, null, false, budgetOptions);
        // 断言：reasoning_effort 映射到 thinkingLevel，none 映射为 thinkingBudget = 0；thinking_budget 映射到 thinkingBudget
        assertEquals(GoogleGenAiThinkingLevel.HIGH, high.getThinkingLevel());
        assertNull(high.getThinkingBudget());
        assertNull(none.getThinkingLevel());
        assertEquals(0, none.getThinkingBudget());
        assertNull(budget.getThinkingLevel());
        assertEquals(2048, budget.getThinkingBudget());
    }

    @Test
    public void testBuildChatOptions_deepSeekOptionsIgnored() {
        // 准备参数
        Map<String, Object> levelOptions = new LinkedHashMap<>();
        levelOptions.put("thinking", MapUtil.of("type", "disabled"));

        // 调用
        DeepSeekChatOptions withReasoning = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.DEEP_SEEK,
                "deepseek-test", 0.7, 1024, null, null, false, levelOptions);
        DeepSeekChatOptions withoutReasoning = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.DEEP_SEEK,
                "deepseek-test", 0.7, 1024);
        // 断言：DeepSeek 族的思考参数装在客户端 HTTP 层，options 与未配置时一致
        assertEquals(withoutReasoning.getModel(), withReasoning.getModel());
        assertEquals(withoutReasoning.getTemperature(), withReasoning.getTemperature());
        assertEquals(withoutReasoning.getMaxTokens(), withReasoning.getMaxTokens());
    }

    @Test
    public void testBuildChatOptions_miMo() {
        // 调用
        DeepSeekChatOptions options = (DeepSeekChatOptions) AiChatUtils.buildChatOptions(AiPlatformEnum.MI_MO,
                "mimo-v2.6-flash", 0.7, 1024);
        // 断言：复用 DeepSeek 客户端的选项
        assertEquals("mimo-v2.6-flash", options.getModel());
        assertEquals(0.7, options.getTemperature());
        assertEquals(1024, options.getMaxTokens());
    }

}

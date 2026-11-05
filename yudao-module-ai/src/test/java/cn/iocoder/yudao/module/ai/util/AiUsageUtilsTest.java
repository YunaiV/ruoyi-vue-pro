package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiUsageUtils.UsageSnapshot;
import com.anthropic.core.ObjectMappers;
import com.anthropic.models.messages.MessageDeltaUsage;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.openai.models.completions.CompletionUsage;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiUsageUtils} 的单元测试类
 *
 * 直接构造真实的 Spring AI 对象，不启动 Spring 容器，不请求外部模型
 *
 * @author 芋道源码
 */
public class AiUsageUtilsTest {

    private static final String ANTHROPIC = AiPlatformEnum.ANTHROPIC.getPlatform();
    private static final String OPENAI = AiPlatformEnum.OPENAI.getPlatform();
    private static final String GEMINI = AiPlatformEnum.GEMINI.getPlatform();

    // ========== extractUsage ==========

    @Test
    public void testExtractUsage_nullResponse() {
        // 调用，并断言
        assertUsage(AiUsageUtils.extractUsage(null, OPENAI), null, null);
    }

    @Test
    public void testExtractUsage_emptyUsageIsUnknown() {
        // 调用，并断言：默认 ChatResponseMetadata 的 usage 即为 EmptyUsage
        assertUsage(AiUsageUtils.extractUsage(new ChatResponse(Collections.emptyList()), OPENAI), null, null);
        // 调用，并断言：显式 EmptyUsage
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new EmptyUsage()), OPENAI), null, null);
    }

    @Test
    public void testExtractUsage_realZeroKept() {
        // 准备参数
        ChatResponse response = buildResponse(new DefaultUsage(0, 0, 0));

        // 调用
        UsageSnapshot usage = AiUsageUtils.extractUsage(response, OPENAI);
        // 断言：厂商明确返回 0，保留 0
        assertUsage(usage, 0L, 0L);
    }

    @Test
    public void testExtractUsage_providedTotalIgnored() {
        // 调用，并断言：标准 getter，厂商给的 total 与 input + output 不同，不读取，只取输入、输出
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 999)), OPENAI), 10L, 5L);
        // 调用，并断言：OpenAI 官方 SDK 用量，同样归一
        CompletionUsage nativeUsage = CompletionUsage.builder().promptTokens(10).completionTokens(5).totalTokens(999).build();
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 999, nativeUsage)), OPENAI),
                10L, 5L);
    }

    @Test
    public void testExtractUsage_deepSeekNativeNullNotZero() {
        // 准备参数：原生 prompt_tokens 为 null，框架 DefaultUsage 会转成 0（Spring AI 2.x 的 OpenAI 官方 SDK 用量字段必填，改用 DeepSeek 原生 record）
        DeepSeekApi.Usage nativeUsage = new DeepSeekApi.Usage(6, null, null);

        // 调用，并断言：输入保持未知
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(null, 6, null, nativeUsage)), OPENAI),
                null, 6L);
    }

    @Test
    public void testExtractUsage_anthropicWithCache() {
        // 准备参数：Spring AI 2.x 的 Anthropic 用量，promptTokens 只含基础输入，缓存读取 40、缓存写入 30 由 Usage 单独返回
        Usage anthropicUsage = new DefaultUsage(100, 20, 120, null, 40L, 30L);

        // 调用
        UsageSnapshot usage = AiUsageUtils.extractUsage(buildResponse(anthropicUsage), ANTHROPIC);
        // 断言：完整输入 = 100 + 40 + 30；合计不重复加缓存
        assertUsage(usage, 170L, 20L);
    }

    @Test
    public void testExtractUsage_anthropicSyncUsage() throws Exception {
        // 准备参数：同步时原生用量为完整的 Message Usage
        com.anthropic.models.messages.Usage withCache = parseAnthropic(
                "{\"input_tokens\":10,\"output_tokens\":5,\"cache_read_input_tokens\":2,\"cache_creation_input_tokens\":3}",
                com.anthropic.models.messages.Usage.class);
        com.anthropic.models.messages.Usage withoutCache = parseAnthropic(
                "{\"input_tokens\":100,\"output_tokens\":20}", com.anthropic.models.messages.Usage.class);

        // 调用，并断言：完整输入 = 10 + 2 + 3
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 15, withCache, 2L, 3L)), ANTHROPIC),
                15L, 5L);
        // 调用，并断言：完整 Usage 未返回缓存字段，视为没有缓存分量
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(100, 20, 120, withoutCache, null, null)), ANTHROPIC),
                100L, 20L);
    }

    @Test
    public void testExtractUsage_anthropicStreamDeltaWithoutCacheIsUnknown() throws Exception {
        // 准备参数：message_delta 只返回 output_tokens；Spring AI 2.0.1 流式只从 message_delta 读取缓存，
        // message_start 中的缓存读取、写入被丢弃，getter 为 null，promptTokens 只是 message_start 的基础输入
        MessageDeltaUsage deltaUsage = parseAnthropic("{\"output_tokens\":5}", MessageDeltaUsage.class);

        // 调用，并断言：无法确认缓存分量，完整输入未知，不能把基础输入当成完整输入；输出保留
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 15, deltaUsage, null, null)), ANTHROPIC),
                null, 5L);
    }

    @Test
    public void testExtractUsage_anthropicStreamDeltaWithCache() throws Exception {
        // 准备参数：message_delta 返回了缓存字段
        MessageDeltaUsage deltaUsage = parseAnthropic(
                "{\"input_tokens\":10,\"output_tokens\":5,\"cache_read_input_tokens\":2,\"cache_creation_input_tokens\":3}",
                MessageDeltaUsage.class);

        // 调用，并断言：完整输入 = 10 + 2 + 3
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 15, deltaUsage, 2L, 3L)), ANTHROPIC),
                15L, 5L);
        // 调用，并断言：缓存字段明确为 0，完整输入即基础输入
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 15, deltaUsage, 0L, 0L)), ANTHROPIC),
                10L, 5L);
    }

    @Test
    public void testExtractUsage_anthropicWithoutNativeUsageAndCache() {
        // 调用，并断言：无原生用量，且缓存字段未返回，无法确认缓存分量，完整输入未知；输出取 getter
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(100, 20, 120, null, null, null)), ANTHROPIC),
                null, 20L);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5)), ANTHROPIC), null, 5L);
        // 调用，并断言：只返回其中一个缓存字段，同样未知
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 15, null, 2L, null)), ANTHROPIC),
                null, 5L);
    }

    @Test
    public void testExtractUsage_anthropicUnknownBase() {
        // 准备参数：基础输入未知（getter 返回 null），但有缓存分量
        Usage anthropicUsage = new NullPromptUsage(20, 40L, 30L);

        // 调用，并断言：完整输入仍未知，不做估算
        assertUsage(AiUsageUtils.extractUsage(buildResponse(anthropicUsage), ANTHROPIC), null, 20L);
    }

    @Test
    public void testExtractUsage_partialNativeMap() {
        // 调用，并断言：只有输出 KEY，输入保持未知，不能退回 getter 当成 0
        Map<String, Object> outputOnly = MapUtil.of("completion_tokens", 7);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(null, 7, null, outputOnly)), OPENAI),
                null, 7L);
        // 调用，并断言：输入 KEY 明确为 null
        Map<String, Object> explicitNull = new HashMap<>();
        explicitNull.put("prompt_tokens", null);
        explicitNull.put("completion_tokens", 3);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(null, 3, null, explicitNull)), OPENAI),
                null, 3L);
        // 调用，并断言：只有输入 KEY
        Map<String, Object> inputOnly = MapUtil.of("input_tokens", 9);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(9, null, null, inputOnly)), OPENAI),
                9L, null);
    }

    @Test
    public void testExtractUsage_anthropicNativeUsageIgnored() {
        // 准备参数：原生用量（例如流式 message_delta）与标准 getter 不一致
        Map<String, Object> nativeUsage = MapUtil.<String, Object>builder()
                .put("input_tokens", 0).put("output_tokens", 8)
                .put("cache_read_input_tokens", 0).put("cache_creation_input_tokens", 0).build();

        // 调用，并断言：Spring AI 2.x 已在 getter 合并 message_start 的输入，以 getter 为准：50 + 10 + 5
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(50, 8, 58, nativeUsage, 10L, 5L)), ANTHROPIC),
                65L, 8L);
    }

    @Test
    public void testExtractUsage_unknownNativeRecordUsesGetter() {
        // 准备参数：未知厂商的原生 record
        CustomNativeUsage nativeUsage = new CustomNativeUsage(10, Optional.of(3), Optional.empty(), new HashMap<>());

        // 调用，并断言：使用标准 getter
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 2, null, nativeUsage)), OPENAI),
                10L, 2L);
    }

    @Test
    public void testExtractUsage_geminiThoughtsCountedAsOutput() {
        // 准备参数：Gemini 原生客户端的用量，思考 Token 单独返回
        GenerateContentResponseUsageMetadata nativeUsage = GenerateContentResponseUsageMetadata.builder()
                .promptTokenCount(10).candidatesTokenCount(5).thoughtsTokenCount(7).totalTokenCount(22).build();
        GoogleGenAiUsage geminiUsage = GoogleGenAiUsage.from(nativeUsage);

        // 调用，并断言：标准 getter 的 completionTokens 不含思考
        assertEquals(5, geminiUsage.getCompletionTokens());
        // 调用，并断言：输出 = 5 + 7，与 OpenAI 兼容层的 completion_tokens 一致
        assertUsage(AiUsageUtils.extractUsage(buildResponse(geminiUsage), GEMINI), 10L, 12L);
    }

    @Test
    public void testExtractUsage_geminiToolUsePromptCountedAsInput() {
        // 准备参数：Gemini 原生用量明确返回工具提示 Token，total 25 = 10 + 3 + 5 + 7
        GenerateContentResponseUsageMetadata nativeUsage = GenerateContentResponseUsageMetadata.builder()
                .promptTokenCount(10).toolUsePromptTokenCount(3).candidatesTokenCount(5).thoughtsTokenCount(7)
                .totalTokenCount(25).build();
        GoogleGenAiUsage geminiUsage = GoogleGenAiUsage.from(nativeUsage);

        // 调用，并断言：标准 getter 的 promptTokens 不含工具提示
        assertEquals(10, geminiUsage.getPromptTokens());
        // 调用，并断言：工具提示计入输入（10 + 3），不计入输出（5 + 7），输入 + 输出 = 厂商合计
        assertUsage(AiUsageUtils.extractUsage(buildResponse(geminiUsage), GEMINI), 13L, 12L);
        // 调用，并断言：基础输入未返回时，只有工具提示不能当成完整输入，输入未知
        GenerateContentResponseUsageMetadata withoutPrompt = GenerateContentResponseUsageMetadata.builder()
                .toolUsePromptTokenCount(3).candidatesTokenCount(5).build();
        assertUsage(AiUsageUtils.extractUsage(buildResponse(GoogleGenAiUsage.from(withoutPrompt)), GEMINI), null, 5L);
    }

    @Test
    public void testExtractUsage_geminiPartialNativeNullNotZero() {
        // 准备参数：只返回输入，输出、思考都未返回
        GenerateContentResponseUsageMetadata nativeUsage = GenerateContentResponseUsageMetadata.builder()
                .promptTokenCount(10).build();

        // 调用，并断言：输出保持未知，不当成 0
        assertUsage(AiUsageUtils.extractUsage(buildResponse(GoogleGenAiUsage.from(nativeUsage)), GEMINI), 10L, null);
    }

    @Test
    public void testExtractUsage_geminiWithoutUsageMetadata() {
        // 准备参数：响应没有 usageMetadata 时，Spring AI 2.x 返回全 0 的 GoogleGenAiUsage
        GoogleGenAiUsage geminiUsage = GoogleGenAiUsage.from(null);

        // 调用，并断言：不是有效用量，输入、输出未知
        assertFalse(AiUsageUtils.isUsagePresent(geminiUsage));
        assertUsage(AiUsageUtils.extractUsage(buildResponse(geminiUsage), GEMINI), null, null);
    }

    @Test
    public void testExtractUsage_geminiToolRoundsAggregated() {
        // 准备参数：带工具续调用两轮，每轮 prompt 10、candidates 5、thoughts 7、toolUsePrompt 3、total 25；
        // ChatClient 累加为原生用量为 null 的 DefaultUsage(20, 10, 50)，思考、工具提示 Token 只剩在合计中
        Usage aggregatedUsage = new DefaultUsage(20, 10, 50);

        // 调用，并断言：差值 20 混有工具提示输入 6、思考输出 14，无法拆分，输入、输出都未知，不把 Σprompt 当成完整输入
        assertUsage(AiUsageUtils.extractUsage(buildResponse(aggregatedUsage), GEMINI), null, null);
        // 调用，并断言：差值即使全是思考，也无法从累加用量确认，输入、输出都未知
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(20, 10, 44)), GEMINI), null, null);
        // 调用，并断言：合计小于「输入 + 输出」，数据不一致，输入、输出都未知
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(20, 10, 25)), GEMINI), null, null);
        // 调用，并断言：合计 = 输入 + 输出，没有工具提示、思考分量，输入、输出确定
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(20, 10, 30)), GEMINI), 20L, 10L);
        // 调用，并断言：其它平台仍不读取合计
        assertUsage(AiUsageUtils.extractUsage(buildResponse(aggregatedUsage), OPENAI), 20L, 10L);
    }

    @Test
    public void testExtractUsage_openAiStreamPlaceholderIsUnknown() {
        // 准备参数：Spring AI 2.x 的 OpenAI 流式正文 chunk 没有 usage 时，用只含三项 0 的 CompletionUsage 占位；
        // 厂商只返回三项 0 时，SDK 解析结果与占位完全相同，同样按未知处理（无法区分的边界）
        CompletionUsage placeholder = CompletionUsage.builder().promptTokens(0).completionTokens(0).totalTokens(0).build();
        Usage placeholderUsage = new DefaultUsage(0, 0, 0, placeholder);

        // 调用，并断言：不是厂商明确返回的用量，输入、输出未知；复用 OpenAI 客户端的 Grok 同样
        assertFalse(AiUsageUtils.isUsagePresent(placeholderUsage));
        assertUsage(AiUsageUtils.extractUsage(buildResponse(placeholderUsage), OPENAI), null, null);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(placeholderUsage), AiPlatformEnum.GROK.getPlatform()),
                null, null);
        // 调用，并断言：厂商明确返回的用量，输出为 0 也保留
        CompletionUsage realUsage = CompletionUsage.builder().promptTokens(3).completionTokens(0).totalTokens(3).build();
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(3, 0, 3, realUsage)), OPENAI), 3L, 0L);
        // 调用，并断言：厂商明确返回三项 0，且带 details，与占位不相等，保留 0
        CompletionUsage zeroWithDetails = CompletionUsage.builder().promptTokens(0).completionTokens(0).totalTokens(0)
                .promptTokensDetails(CompletionUsage.PromptTokensDetails.builder().cachedTokens(0).build()).build();
        assertTrue(AiUsageUtils.isUsagePresent(new DefaultUsage(0, 0, 0, zeroWithDetails)));
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(0, 0, 0, zeroWithDetails)), OPENAI), 0L, 0L);
    }

    @Test
    public void testIsUsagePresent() {
        // 调用，并断言：null、EmptyUsage 无效
        assertFalse(AiUsageUtils.isUsagePresent(null));
        assertFalse(AiUsageUtils.isUsagePresent(new EmptyUsage()));
        // 调用，并断言：厂商明确返回 0，有效
        assertTrue(AiUsageUtils.isUsagePresent(new DefaultUsage(0, 0, 0)));
        // 调用，并断言：Gemini 有 usageMetadata，有效
        assertTrue(AiUsageUtils.isUsagePresent(GoogleGenAiUsage.from(
                GenerateContentResponseUsageMetadata.builder().promptTokenCount(1).build())));
    }

    // ========== extractResponseMetadata ==========

    @Test
    public void testExtractResponseMetadata_null() {
        // 调用，并断言
        assertNull(AiUsageUtils.extractResponseMetadata(null));
    }

    @Test
    public void testExtractResponseMetadata_full() {
        // 准备参数
        CompletionUsage nativeUsage = CompletionUsage.builder().promptTokens(10).completionTokens(5).totalTokens(15)
                .promptTokensDetails(CompletionUsage.PromptTokensDetails.builder().cachedTokens(4).build()).build();
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .id("resp-1").model("gpt-test")
                .usage(new DefaultUsage(10, 5, 15, nativeUsage))
                .rateLimit(new TestRateLimit())
                .promptMetadata(PromptMetadata.of(PromptMetadata.PromptFilterMetadata.from(0, MapUtil.of("hate", "safe"))))
                .keyValue("created", 123L)
                .keyValue("apiKey", "sk-vendor-metadata")
                .build();
        ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.builder()
                .finishReason("STOP").contentFilter("violence")
                .metadata("role", "ASSISTANT")
                .metadata("reasoningContent", "推理正文不应保存")
                .build();
        ChatResponse response = new ChatResponse(Collections.singletonList(
                new Generation(new AssistantMessage("生成正文不应保存"), generationMetadata)), metadata);

        // 调用
        Map<String, Object> result = AiUsageUtils.extractResponseMetadata(response);

        // 断言：响应级独立 getter
        assertEquals("resp-1", result.get("id"));
        assertEquals("gpt-test", result.get("model"));
        // 断言：usage 标准 getter + 原生用量
        Map<String, Object> usage = asMap(result.get("usage"));
        assertEquals(DefaultUsage.class.getName(), usage.get("type"));
        assertEquals(false, usage.get("empty"));
        assertEquals(10, usage.get("promptTokens"));
        assertEquals(5, usage.get("completionTokens"));
        assertEquals(15, usage.get("totalTokens"));
        // 断言：原生用量为 OpenAI 官方 SDK 对象，使用 SDK 自带的序列化
        Map<String, Object> nativeUsageMap = asMap(usage.get("nativeUsage"));
        assertEquals(10, nativeUsageMap.get("prompt_tokens"));
        assertEquals(5, nativeUsageMap.get("completion_tokens"));
        assertEquals(4, asMap(nativeUsageMap.get("prompt_tokens_details")).get("cached_tokens"));
        // 断言：rateLimit
        Map<String, Object> rateLimit = asMap(result.get("rateLimit"));
        assertEquals(100L, rateLimit.get("requestsLimit"));
        assertEquals(99L, rateLimit.get("requestsRemaining"));
        assertEquals("PT1S", rateLimit.get("requestsReset"));
        assertEquals(1000L, rateLimit.get("tokensLimit"));
        assertEquals(900L, rateLimit.get("tokensRemaining"));
        assertEquals("PT1M", rateLimit.get("tokensReset"));
        // 断言：promptMetadata
        List<Map<String, Object>> promptMetadata = asList(result.get("promptMetadata"));
        assertEquals(1, promptMetadata.size());
        assertEquals(0, promptMetadata.get(0).get("promptIndex"));
        assertEquals("safe", asMap(promptMetadata.get(0).get("contentFilterMetadata")).get("hate"));
        // 断言：扩展属性，厂商 metadata 原样保存
        Map<String, Object> extensions = asMap(result.get("extensions"));
        assertEquals(123L, ((Number) extensions.get("created")).longValue());
        assertEquals("sk-vendor-metadata", extensions.get("apiKey"));
        // 断言：generation
        List<Map<String, Object>> generations = asList(result.get("generations"));
        assertEquals(1, generations.size());
        assertEquals(0, generations.get(0).get("index"));
        assertEquals("STOP", generations.get(0).get("finishReason"));
        assertEquals(ListUtil.of("violence"), generations.get(0).get("contentFilters"));
        Map<String, Object> generationMap = asMap(generations.get(0).get("metadata"));
        assertEquals("ASSISTANT", generationMap.get("role"));
        assertFalse(generationMap.containsKey("reasoningContent"));
        // 断言：不保存生成正文、推理正文；无快照错误
        String text = String.valueOf(result);
        assertFalse(text.contains("生成正文不应保存"));
        assertFalse(text.contains("推理正文不应保存"));
        assertFalse(result.containsKey(AiUsageUtils.SNAPSHOT_ERRORS_KEY));
    }

    @Test
    public void testExtractResponseMetadata_emptyUsage() {
        // 调用
        Map<String, Object> result = AiUsageUtils.extractResponseMetadata(buildResponse(new EmptyUsage()));

        // 断言：标记为 EmptyUsage
        assertEquals(true, asMap(result.get("usage")).get("empty"));
        // 断言：默认 EmptyRateLimit 不保存
        assertNull(result.get("rateLimit"));
    }

    @Test
    public void testExtractResponseMetadata_geminiNativeUsage() {
        // 准备参数：Gemini 原生用量为 Google GenAI SDK 对象
        GenerateContentResponseUsageMetadata nativeUsage = GenerateContentResponseUsageMetadata.builder()
                .promptTokenCount(10).candidatesTokenCount(5).thoughtsTokenCount(7).totalTokenCount(22).build();

        // 调用
        Map<String, Object> result = AiUsageUtils.extractResponseMetadata(buildResponse(GoogleGenAiUsage.from(nativeUsage)));

        // 断言：使用 SDK 自带的序列化，原生字段完整保留；无快照错误
        Map<String, Object> usage = asMap(result.get("usage"));
        assertEquals(GoogleGenAiUsage.class.getName(), usage.get("type"));
        Map<String, Object> nativeUsageMap = asMap(usage.get("nativeUsage"));
        assertEquals(10, nativeUsageMap.get("promptTokenCount"));
        assertEquals(5, nativeUsageMap.get("candidatesTokenCount"));
        assertEquals(7, nativeUsageMap.get("thoughtsTokenCount"));
        assertFalse(result.containsKey(AiUsageUtils.SNAPSHOT_ERRORS_KEY));
    }

    @Test
    public void testExtractResponseMetadata_nativeRecordOptionalNestedMapDetached() {
        // 准备参数：原生用量为 record，含 Optional 与可变嵌套 Map
        Map<String, Object> nested = new HashMap<>();
        nested.put("audio_tokens", 1);
        Map<String, Object> details = new HashMap<>();
        details.put("reasoning_tokens", 7);
        details.put("nested", nested);
        CustomNativeUsage nativeUsage = new CustomNativeUsage(10, Optional.of(3), Optional.empty(), details);
        ChatResponse response = buildResponse(new DefaultUsage(10, 2, null, nativeUsage));

        // 调用
        Map<String, Object> result = AiUsageUtils.extractResponseMetadata(response);
        // 快照后修改原对象
        details.put("reasoning_tokens", 999);
        details.put("added", true);
        nested.put("audio_tokens", 888);

        // 断言：原生字段完整保留，Optional 展开，快照与原对象脱钩
        Map<String, Object> nativeUsageMap = asMap(asMap(result.get("usage")).get("nativeUsage"));
        assertEquals(10, nativeUsageMap.get("inputTokens"));
        assertEquals(3, nativeUsageMap.get("cachedTokens"));
        assertTrue(nativeUsageMap.containsKey("missingTokens"));
        assertNull(nativeUsageMap.get("missingTokens"));
        Map<String, Object> detailsSnapshot = asMap(nativeUsageMap.get("details"));
        assertEquals(7, detailsSnapshot.get("reasoning_tokens"));
        assertFalse(detailsSnapshot.containsKey("added"));
        assertEquals(1, asMap(detailsSnapshot.get("nested")).get("audio_tokens"));
    }

    @Test
    public void testExtractResponseMetadata_partialFailure() {
        // 准备参数：一个扩展属性序列化时抛出异常
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .id("resp-2").model("m")
                .usage(new DefaultUsage(1, 2))
                .keyValue("broken", new BrokenValue())
                .keyValue("ok", "fine")
                .build();
        ChatResponse response = new ChatResponse(Collections.singletonList(new Generation(new AssistantMessage("x"),
                ChatGenerationMetadata.builder().finishReason("STOP").build())), metadata);

        // 调用
        Map<String, Object> result = AiUsageUtils.extractResponseMetadata(response);

        // 断言：其余字段正常
        assertEquals("resp-2", result.get("id"));
        assertEquals(1, asMap(result.get("usage")).get("promptTokens"));
        Map<String, Object> extensions = asMap(result.get("extensions"));
        assertEquals("fine", extensions.get("ok"));
        assertFalse(extensions.containsKey("broken"));
        assertEquals("STOP", asList(result.get("generations")).get(0).get("finishReason"));
        // 断言：错误记录路径与异常信息
        List<Map<String, Object>> errors = asList(result.get(AiUsageUtils.SNAPSHOT_ERRORS_KEY));
        assertEquals(1, errors.size());
        assertEquals(2, errors.get(0).size());
        assertEquals("extensions.broken", errors.get(0).get("path"));
        assertTrue(String.valueOf(errors.get(0).get("error")).contains(BrokenValue.MESSAGE));
    }


    // ========== 辅助方法 ==========

    private static ChatResponse buildResponse(Usage usage) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().id("id").model("model").usage(usage).build();
        return new ChatResponse(Collections.singletonList(new Generation(new AssistantMessage("content"))), metadata);
    }

    /**
     * 使用 Anthropic SDK 自带的 JsonMapper，按真实响应 JSON 构造 SDK 用量对象（与 SDK 解析 SSE 一致，不经过 builder 的必填校验）
     */
    private static <T> T parseAnthropic(String json, Class<T> clazz) throws Exception {
        return ObjectMappers.jsonMapper().readValue(json, clazz);
    }

    private static void assertUsage(UsageSnapshot usage, Long input, Long output) {
        assertNotNull(usage);
        assertEquals(input, usage.getInputTokens());
        assertEquals(output, usage.getOutputTokens());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        assertInstanceOf(Map.class, value);
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object value) {
        assertInstanceOf(List.class, value);
        return (List<Map<String, Object>>) value;
    }

    /**
     * 模拟未知厂商的原生用量 record（Spring AI 各厂商原生用量均为 record，需覆盖 record 转换）
     */
    public record CustomNativeUsage(Integer inputTokens, Optional<Integer> cachedTokens,
                                    Optional<Integer> missingTokens, Map<String, Object> details) {
    }

    /**
     * 基础输入未知的用量：{@link DefaultUsage} 会把 null 转成 0，所以单独实现
     */
    private record NullPromptUsage(Integer completionTokens, Long cacheReadInputTokens,
                                   Long cacheWriteInputTokens) implements Usage {

        @Override
        public Integer getPromptTokens() {
            return null;
        }

        @Override
        public Integer getCompletionTokens() {
            return completionTokens;
        }

        @Override
        public Object getNativeUsage() {
            return null;
        }

        @Override
        public Long getCacheReadInputTokens() {
            return cacheReadInputTokens;
        }

        @Override
        public Long getCacheWriteInputTokens() {
            return cacheWriteInputTokens;
        }

    }

    /**
     * 序列化时抛出异常的扩展属性
     */
    public static class BrokenValue {

        public static final String MESSAGE = "broken-value-getter";

        public String getValue() {
            throw new IllegalStateException(MESSAGE);
        }

    }

    private static class TestRateLimit implements RateLimit {

        @Override
        public Long getRequestsLimit() {
            return 100L;
        }

        @Override
        public Long getRequestsRemaining() {
            return 99L;
        }

        @Override
        public Duration getRequestsReset() {
            return Duration.ofSeconds(1);
        }

        @Override
        public Long getTokensLimit() {
            return 1000L;
        }

        @Override
        public Long getTokensRemaining() {
            return 900L;
        }

        @Override
        public Duration getTokensReset() {
            return Duration.ofMinutes(1);
        }

    }

}

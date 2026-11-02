package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiUsageUtils.UsageSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.api.AnthropicApi;
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
import org.springframework.ai.openai.api.OpenAiApi;

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
        // 调用，并断言：OpenAI 原生 record，同样归一
        OpenAiApi.Usage nativeUsage = new OpenAiApi.Usage(5, 10, 999);
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5, 999, nativeUsage)), OPENAI),
                10L, 5L);
    }

    @Test
    public void testExtractUsage_openAiNativeNullNotZero() {
        // 准备参数：原生 prompt_tokens 为 null，框架 DefaultUsage 会转成 0
        OpenAiApi.Usage nativeUsage = new OpenAiApi.Usage(6, null, null);

        // 调用，并断言：输入保持未知
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(null, 6, null, nativeUsage)), OPENAI),
                null, 6L);
    }

    @Test
    public void testExtractUsage_anthropicRecordWithCache() {
        // 准备参数：参数顺序为 inputTokens, outputTokens, cacheCreationInputTokens, cacheReadInputTokens
        AnthropicApi.Usage nativeUsage = new AnthropicApi.Usage(100, 20, 30, 40);

        // 调用
        UsageSnapshot usage = AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(100, 20, 120, nativeUsage)), ANTHROPIC);
        // 断言：完整输入 = 100 + 40 + 30；合计不重复加缓存
        assertUsage(usage, 170L, 20L);
    }

    @Test
    public void testExtractUsage_anthropicRecordWithoutCacheFields() {
        // 准备参数
        AnthropicApi.Usage nativeUsage = new AnthropicApi.Usage(100, 20, null, null);

        // 调用，并断言：缓存字段未返回，视为没有缓存分量
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(100, 20, 120, nativeUsage)), ANTHROPIC),
                100L, 20L);
    }

    @Test
    public void testExtractUsage_anthropicRecordUnknownBase() {
        // 准备参数：基础 input_tokens 未知，但有缓存分量
        AnthropicApi.Usage nativeUsage = new AnthropicApi.Usage(null, 20, 30, 40);

        // 调用，并断言：完整输入仍未知，不做估算
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(0, 20, 20, nativeUsage)), ANTHROPIC),
                null, 20L);
    }

    @Test
    public void testExtractUsage_anthropicWithoutNativeUsage() {
        // 调用，并断言：框架聚合后丢失原生用量，无法确认缓存部分，输入未知，输出取 getter
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 5)), ANTHROPIC), null, 5L);
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
    public void testExtractUsage_anthropicNativeMap() {
        // 调用，并断言：Map 原生用量同样归一缓存
        Map<String, Object> nativeUsage = MapUtil.<String, Object>builder()
                .put("input_tokens", 50).put("output_tokens", 8)
                .put("cache_read_input_tokens", 10).put("cache_creation_input_tokens", 5).build();
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(50, 8, 58, nativeUsage)), ANTHROPIC),
                65L, 8L);
        // 调用，并断言：基础输入缺失，输入未知，输出保留
        Map<String, Object> missingBase = MapUtil.<String, Object>builder()
                .put("output_tokens", 8).put("cache_read_input_tokens", 10).build();
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(0, 8, 8, missingBase)), ANTHROPIC),
                null, 8L);
    }

    @Test
    public void testExtractUsage_unknownNativeRecordUsesGetter() {
        // 准备参数：未知厂商的原生 record
        CustomNativeUsage nativeUsage = new CustomNativeUsage(10, Optional.of(3), Optional.empty(), new HashMap<>());

        // 调用，并断言：使用标准 getter
        assertUsage(AiUsageUtils.extractUsage(buildResponse(new DefaultUsage(10, 2, null, nativeUsage)), OPENAI),
                10L, 2L);
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
        OpenAiApi.Usage nativeUsage = new OpenAiApi.Usage(5, 10, 15,
                new OpenAiApi.Usage.PromptTokensDetails(null, 4), null);
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

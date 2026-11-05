package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import com.google.genai.JsonSerializable;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.openai.models.completions.CompletionUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;

import java.util.*;
import java.util.function.Supplier;

/**
 * AI Token 用量与响应元数据的工具类
 *
 * 1. {@link #extractUsage(ChatResponse, String)}：从单个 {@link ChatResponse} 提取标准用量快照，未知为 null，不做估算
 * 2. {@link #extractResponseMetadata(ChatResponse)}：把响应元数据转换为与 SDK 对象脱钩的 JSON 值
 *
 * 无状态，不负责流式 chunk 之间的合并，由请求日志按请求维护
 *
 * @author 芋道源码
 */
public class AiUsageUtils {

    // DONE @AI：字段注释；几个都写下；（已为下面的 KEY 补充字段注释）
    /**
     * 原生用量 Map 中输入 Token 的候选 KEY，按优先级排列：OpenAI 兼容为 prompt_tokens，Anthropic、通义为 input_tokens，及其驼峰写法
     */
    private static final List<String> NATIVE_INPUT_KEYS = ListUtil.of("prompt_tokens", "input_tokens", "promptTokens", "inputTokens");
    /**
     * 原生用量 Map 中输出 Token 的候选 KEY，按优先级排列：OpenAI 兼容为 completion_tokens，Anthropic、通义为 output_tokens，及其驼峰写法
     */
    private static final List<String> NATIVE_OUTPUT_KEYS = ListUtil.of("completion_tokens", "output_tokens", "completionTokens", "outputTokens");

    /**
     * 标准用量快照
     *
     * 字段为 null 表示未知；0 表示厂商明确返回 0。合计不在这里保存，由使用方按「完整输入 + 输出」计算
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageSnapshot {

        /**
         * 完整输入 Token（Anthropic 已包含缓存读取、缓存写入）
         */
        private Long inputTokens;
        /**
         * 输出 Token
         */
        private Long outputTokens;

        /**
         * 创建未知用量
         *
         * @return 输入、输出都为 null 的快照
         */
        public static UsageSnapshot unknown() {
            return new UsageSnapshot();
        }

    }

    /**
     * 从单个 {@link ChatResponse} 提取标准用量快照，不做估算，不合并流式 chunk
     *
     * 1. usage 为 null 或 {@link EmptyUsage} 时为未知，不是 0
     * 2. 优先读取原生用量，原生字段为 null 时保持未知
     * 3. Anthropic 完整输入 = input_tokens + cache_read_input_tokens + cache_creation_input_tokens，基础输入未知时仍未知。
     *    Spring AI 2.x 的 Anthropic 用量中，promptTokens 只含基础输入，缓存读取、写入由 Usage 单独返回；无法确认缓存分量时也未知
     * 4. 不使用厂商返回的合计计算用量，缓存、推理等分量不重复计入
     * 5. Gemini 完整输入 = promptTokenCount + toolUsePromptTokenCount，输出 = candidatesTokenCount + thoughtsTokenCount，
     *    与 OpenAI 兼容层的 completion_tokens 一致。带工具续调用时，ChatClient 累加多轮用量后不再保留工具提示、思考分量，
     *    只在可确认时返回，参见 {@link #isGeminiAccumulatedUsageComplete}
     *
     * @param response 模型响应，可以为 null
     * @param platform 平台，参见 {@link AiPlatformEnum#getPlatform()}
     * @return 用量快照，不会返回 null
     */
    public static UsageSnapshot extractUsage(ChatResponse response, String platform) {
        // 1. 无有效 usage，用量未知
        Usage usage = response != null && response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        if (!isUsagePresent(usage)) {
            return UsageSnapshot.unknown();
        }
        Object nativeUsage = Opt.ofTry(usage::getNativeUsage).get();

        // 2. Anthropic：缓存读取、写入归一到完整输入（Spring AI 2.x 的原生用量是官方 SDK 对象，流式时不含输入，以标准 getter 为准）
        if (StrUtil.equals(platform, AiPlatformEnum.ANTHROPIC.getPlatform())) {
            return new UsageSnapshot(sumAnthropicInput(Convert.toLong(Opt.ofTry(usage::getPromptTokens).get()),
                            Opt.ofTry(usage::getCacheReadInputTokens).get(),
                            Opt.ofTry(usage::getCacheWriteInputTokens).get(),
                            nativeUsage instanceof com.anthropic.models.messages.Usage),
                    Convert.toLong(Opt.ofTry(usage::getCompletionTokens).get()));
        }

        // 3. 已知厂商的原生 record，保留 null 语义（OpenAI 官方 SDK 的用量字段必填，走第 5 步的标准 getter）
        if (nativeUsage instanceof DeepSeekApi.Usage) {
            DeepSeekApi.Usage deepSeekUsage = (DeepSeekApi.Usage) nativeUsage;
            return new UsageSnapshot(Convert.toLong(deepSeekUsage.promptTokens()), Convert.toLong(deepSeekUsage.completionTokens()));
        }
        if (nativeUsage instanceof DashScopeApiSpec.TokenUsage) {
            DashScopeApiSpec.TokenUsage dashScopeUsage = (DashScopeApiSpec.TokenUsage) nativeUsage;
            return new UsageSnapshot(Convert.toLong(dashScopeUsage.inputTokens()), Convert.toLong(dashScopeUsage.outputTokens()));
        }
        if (nativeUsage instanceof GenerateContentResponseUsageMetadata) {
            // Gemini 原生客户端：工具提示、思考 Token 单独返回，标准 getter 的 promptTokens、completionTokens 不含，这里分别计入输入、输出
            GenerateContentResponseUsageMetadata geminiUsage = (GenerateContentResponseUsageMetadata) nativeUsage;
            return new UsageSnapshot(sumGeminiInput(geminiUsage.promptTokenCount().orElse(null), geminiUsage.toolUsePromptTokenCount().orElse(null)),
                    sumGeminiOutput(geminiUsage.candidatesTokenCount().orElse(null), geminiUsage.thoughtsTokenCount().orElse(null)));
        }
        if (StrUtil.equals(platform, AiPlatformEnum.GEMINI.getPlatform()) && nativeUsage == null) {
            // Gemini 多轮累加用量：Spring AI 2.x 的 ChatClient 带工具续调用时，把各轮用量累加为原生用量为 null 的 DefaultUsage
            return isGeminiAccumulatedUsageComplete(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens())
                    ? new UsageSnapshot(Convert.toLong(usage.getPromptTokens()), Convert.toLong(usage.getCompletionTokens()))
                    : UsageSnapshot.unknown();
        }

        // 4. Map 原生用量，含任一标准 KEY 时以 Map 为准，缺少的一项保持未知
        if (nativeUsage instanceof Map && containsAnyStandardKey((Map<?, ?>) nativeUsage)) {
            Map<?, ?> nativeMap = (Map<?, ?>) nativeUsage;
            return new UsageSnapshot(getFirstLong(nativeMap, NATIVE_INPUT_KEYS), getFirstLong(nativeMap, NATIVE_OUTPUT_KEYS));
        }

        // 5. 无原生用量或未知类型，使用标准 getter
        return new UsageSnapshot(Convert.toLong(Opt.ofTry(usage::getPromptTokens).get()),
                Convert.toLong(Opt.ofTry(usage::getCompletionTokens).get()));
    }

    /**
     * 是否为有效用量
     *
     * 1. null、{@link EmptyUsage} 无效
     * 2. Spring AI 2.x 的 Gemini 在响应没有 usageMetadata 时，仍返回全 0 的 {@link GoogleGenAiUsage}（原生用量为 null），
     *    不是厂商明确返回的 0，同样无效
     * 3. Spring AI 2.x 的 OpenAI（及复用其客户端的 Grok 等）流式 chunk 没有 usage 时，用只含三项 0 的 {@link CompletionUsage} 占位，
     *    同样无效。厂商明确返回的 usage 带 prompt_tokens_details 等字段时，与占位不相等，仍然有效；
     *    只返回三项 0 时，SDK 解析结果与占位完全相同，无法区分，按未知处理
     *
     * @param usage 用量
     * @return 是否有效
     */
    public static boolean isUsagePresent(Usage usage) {
        if (usage == null || usage instanceof EmptyUsage) {
            return false;
        }
        Object nativeUsage = Opt.ofTry(usage::getNativeUsage).get();
        if (usage instanceof GoogleGenAiUsage && nativeUsage == null) {
            return false;
        }
        return !OPENAI_STREAM_PLACEHOLDER_USAGE.equals(nativeUsage);
    }

    /**
     * Spring AI 2.x 的 OpenAI 流式 chunk 没有 usage 时的占位用量，与 OpenAiChatModel 的构建方式一致
     *
     * {@link CompletionUsage#equals(Object)} 比较全部字段（含 details、额外属性），只有完全相同才视为占位
     */
    private static final CompletionUsage OPENAI_STREAM_PLACEHOLDER_USAGE = CompletionUsage.builder()
            .promptTokens(0).completionTokens(0).totalTokens(0).build();

    /**
     * Gemini 多轮累加用量是否可确认
     *
     * 每轮的 totalTokenCount = promptTokenCount + toolUsePromptTokenCount + candidatesTokenCount + thoughtsTokenCount，
     * 累加后只剩 Σprompt、Σcandidates、合计，工具提示（输入）与思考（输出）混在「合计 - Σprompt - Σcandidates」的差值中，无法拆分。
     * 因此差值为 0 时，输入、输出都可确认；差值不为 0、或合计缺失时，输入、输出都未知，不把 Σprompt 当成完整输入，也不估算差值
     *
     * @param promptTokens     累加的 promptTokenCount
     * @param completionTokens 累加的 candidatesTokenCount
     * @param totalTokens      累加合计
     * @return 是否可确认
     */
    private static boolean isGeminiAccumulatedUsageComplete(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        return promptTokens != null && completionTokens != null && totalTokens != null
                && totalTokens == promptTokens + completionTokens;
    }

    /**
     * 计算 Gemini 完整输入 Token
     *
     * @param promptTokens        基础输入，null 时完整输入未知
     * @param toolUsePromptTokens 工具提示输入，null 表示未返回（Gemini 不返回值为 0 的字段）
     * @return 完整输入 Token
     */
    private static Long sumGeminiInput(Integer promptTokens, Integer toolUsePromptTokens) {
        if (promptTokens == null) {
            return null;
        }
        return (long) promptTokens + ObjUtil.defaultIfNull(toolUsePromptTokens, 0);
    }

    /**
     * 计算 Gemini 输出 Token
     *
     * @param candidatesTokens 候选输出，null 表示未返回
     * @param thoughtsTokens   思考输出，null 表示未返回
     * @return 输出 Token；两者都未返回时为 null
     */
    private static Long sumGeminiOutput(Integer candidatesTokens, Integer thoughtsTokens) {
        if (candidatesTokens == null && thoughtsTokens == null) {
            return null;
        }
        return (long) ObjUtil.defaultIfNull(candidatesTokens, 0) + ObjUtil.defaultIfNull(thoughtsTokens, 0);
    }

    /**
     * 计算 Anthropic 完整输入 Token
     *
     * 1. 同步：原生用量是完整的 Message Usage，缓存字段未返回表示没有缓存分量
     * 2. 流式（原生用量为 message_delta 的 MessageDeltaUsage）、或无原生用量：缓存字段都返回才可计算，否则完整输入未知。
     *    原因：Spring AI 2.0.1 流式时只从 message_delta 读取缓存，message_start 中的缓存读取、写入被丢弃，不能当成 0
     *
     * @param inputTokens         基础输入，null 时完整输入未知
     * @param cacheReadTokens     缓存读取
     * @param cacheCreationTokens 缓存写入
     * @param completeUsage       是否为完整的 Message Usage
     * @return 完整输入 Token
     */
    private static Long sumAnthropicInput(Long inputTokens, Long cacheReadTokens, Long cacheCreationTokens,
                                          boolean completeUsage) {
        if (inputTokens == null) {
            return null;
        }
        if (!completeUsage && (cacheReadTokens == null || cacheCreationTokens == null)) {
            return null;
        }
        return inputTokens + ObjUtil.defaultIfNull(cacheReadTokens, 0L) + ObjUtil.defaultIfNull(cacheCreationTokens, 0L);
    }

    /**
     * 原生用量 Map 是否包含任一输入、输出 KEY
     *
     * @param map 原生用量
     * @return 是否包含
     */
    private static boolean containsAnyStandardKey(Map<?, ?> map) {
        return CollUtil.containsAny(map.keySet(), NATIVE_INPUT_KEYS)
                || CollUtil.containsAny(map.keySet(), NATIVE_OUTPUT_KEYS);
    }

    /**
     * 按 KEY 优先级，读取第一个存在的 KEY 的 Token 数
     *
     * @param map  原生用量
     * @param keys 候选 KEY，按优先级排列
     * @return Token 数；KEY 都不存在或值为 null 时为 null
     */
    private static Long getFirstLong(Map<?, ?> map, List<String> keys) {
        String key = CollUtil.findOne(keys, map::containsKey);
        return key != null ? Convert.toLong(map.get(key)) : null;
    }

    // ========== 响应元数据 ==========

    /**
     * usage 快照的 KEY：流式时有效 usage 整体替换
     */
    public static final String USAGE_KEY = "usage";
    /**
     * generation 列表快照的 KEY：流式时按 {@link #GENERATION_INDEX_KEY} 合并
     */
    public static final String GENERATIONS_KEY = "generations";
    /**
     * generation 下标的 KEY
     */
    public static final String GENERATION_INDEX_KEY = "index";
    /**
     * 快照失败记录的 KEY
     */
    public static final String SNAPSHOT_ERRORS_KEY = "snapshotErrors";

    /**
     * generation 扩展属性中排除的 KEY：属于生成正文，已在业务表保存
     */
    private static final Set<String> EXCLUDED_GENERATION_METADATA_KEYS = SetUtils.asSet("reasoningContent");

    /**
     * 将 {@link ChatResponse} 的元数据转换为 JSON 值快照
     *
     * 包含 id、model、usage（含 nativeUsage）、rateLimit、promptMetadata、扩展属性，及每个 generation 的
     * finishReason、contentFilters、扩展属性；不包含 prompt、生成正文。
     * 返回值仅为 Map / List / 基础类型，与 SDK 对象脱钩；某部分转换失败时跳过，并记录到 {@link #SNAPSHOT_ERRORS_KEY}
     *
     * @param response 模型响应，可以为 null
     * @return 元数据快照；response 为 null 时返回 null
     */
    public static Map<String, Object> extractResponseMetadata(ChatResponse response) {
        if (response == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        // 1. 响应级元数据：每部分独立转换，单个失败不影响其它
        ChatResponseMetadata metadata = response.getMetadata();
        if (metadata != null) {
            putSafely(result, errors, "id", metadata::getId, "id");
            putSafely(result, errors, "model", metadata::getModel, "model");
            putSafely(result, errors, USAGE_KEY, () -> buildUsageSnapshot(metadata.getUsage(), errors), USAGE_KEY);
            putSafely(result, errors, "rateLimit", () -> buildRateLimitSnapshot(metadata.getRateLimit()), "rateLimit");
            putSafely(result, errors, "promptMetadata",
                    () -> buildPromptMetadataSnapshot(metadata.getPromptMetadata(), errors), "promptMetadata");
            putSafely(result, errors, "extensions",
                    () -> buildEntrySnapshot(metadata.entrySet(), Collections.emptySet(), "extensions", errors), "extensions");
        }
        // 2. 每个 generation 的元数据
        putSafely(result, errors, GENERATIONS_KEY, () -> buildGenerationsSnapshot(response.getResults(), errors), GENERATIONS_KEY);
        // 3. 记录转换失败的部分
        if (!errors.isEmpty()) {
            result.put(SNAPSHOT_ERRORS_KEY, errors);
        }
        return result;
    }

    /**
     * 构建 usage 快照，包含标准 getter 与原生用量
     *
     * @param usage  用量
     * @param errors 转换失败记录
     * @return usage 快照
     */
    private static Map<String, Object> buildUsageSnapshot(Usage usage, List<Map<String, Object>> errors) {
        if (usage == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        // 1. 类型，及是否为 EmptyUsage（其 getter 返回的 0 不代表真实用量）
        result.put("type", usage.getClass().getName());
        result.put("empty", usage instanceof EmptyUsage);
        // 2. 标准 getter、原生用量逐项转换
        putSafely(result, errors, "promptTokens", usage::getPromptTokens, "usage.promptTokens");
        putSafely(result, errors, "completionTokens", usage::getCompletionTokens, "usage.completionTokens");
        putSafely(result, errors, "totalTokens", usage::getTotalTokens, "usage.totalTokens");
        putSafely(result, errors, "cacheReadInputTokens", usage::getCacheReadInputTokens, "usage.cacheReadInputTokens");
        putSafely(result, errors, "cacheWriteInputTokens", usage::getCacheWriteInputTokens, "usage.cacheWriteInputTokens");
        putSafely(result, errors, "nativeUsage", () -> toJsonValue(usage.getNativeUsage()), "usage.nativeUsage");
        return result;
    }

    /**
     * 构建限流快照，{@link EmptyRateLimit} 不保存
     *
     * @param rateLimit 限流信息
     * @return 限流快照
     */
    private static Map<String, Object> buildRateLimitSnapshot(RateLimit rateLimit) {
        if (rateLimit == null || rateLimit instanceof EmptyRateLimit) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", rateLimit.getClass().getName());
        result.put("requestsLimit", rateLimit.getRequestsLimit());
        result.put("requestsRemaining", rateLimit.getRequestsRemaining());
        result.put("requestsReset", StrUtil.toStringOrNull(rateLimit.getRequestsReset()));
        result.put("tokensLimit", rateLimit.getTokensLimit());
        result.put("tokensRemaining", rateLimit.getTokensRemaining());
        result.put("tokensReset", StrUtil.toStringOrNull(rateLimit.getTokensReset()));
        return result;
    }

    /**
     * 构建 prompt 内容过滤元数据快照
     *
     * @param promptMetadata prompt 元数据
     * @param errors         转换失败记录
     * @return 每个 prompt 的过滤结果
     */
    private static List<Map<String, Object>> buildPromptMetadataSnapshot(PromptMetadata promptMetadata,
                                                                        List<Map<String, Object>> errors) {
        if (promptMetadata == null) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        int index = 0;
        for (PromptMetadata.PromptFilterMetadata filterMetadata : promptMetadata) {
            // 逐个 prompt 转换过滤结果，失败时记录带下标的路径
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("promptIndex", filterMetadata.getPromptIndex());
            putSafely(item, errors, "contentFilterMetadata", () -> toJsonValue(filterMetadata.getContentFilterMetadata()),
                    "promptMetadata[" + index + "].contentFilterMetadata");
            result.add(item);
            index++;
        }
        return result;
    }

    /**
     * 构建 generation 元数据快照，不包含生成正文
     *
     * @param generations generation 列表
     * @param errors      转换失败记录
     * @return 每个 generation 的 finishReason、contentFilters、扩展属性
     */
    private static List<Map<String, Object>> buildGenerationsSnapshot(List<Generation> generations,
                                                                     List<Map<String, Object>> errors) {
        if (generations == null) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>(generations.size());
        for (int i = 0; i < generations.size(); i++) {
            // 1. 记录下标，便于流式按 index 合并
            Generation generation = generations.get(i);
            ChatGenerationMetadata metadata = generation != null ? generation.getMetadata() : null;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put(GENERATION_INDEX_KEY, i);
            // 2. 逐项转换元数据，扩展属性排除推理正文
            if (metadata != null) {
                String path = GENERATIONS_KEY + "[" + i + "]";
                putSafely(item, errors, "finishReason", metadata::getFinishReason, path + ".finishReason");
                putSafely(item, errors, "contentFilters", () -> toJsonValue(metadata.getContentFilters()),
                        path + ".contentFilters");
                putSafely(item, errors, "metadata", () -> buildEntrySnapshot(metadata.entrySet(),
                        EXCLUDED_GENERATION_METADATA_KEYS, path + ".metadata", errors), path + ".metadata");
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 构建扩展属性快照，按 KEY 排序
     *
     * @param entries      扩展属性
     * @param excludedKeys 排除的 KEY
     * @param path         失败记录的路径前缀
     * @param errors       转换失败记录
     * @return 扩展属性快照
     */
    private static Map<String, Object> buildEntrySnapshot(Set<Map.Entry<String, Object>> entries, Set<String> excludedKeys,
                                                          String path, List<Map<String, Object>> errors) {
        if (entries == null) {
            return null;
        }
        // 1. 按 KEY 排序，保证快照稳定
        Map<String, Object> result = new TreeMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            if (key == null || excludedKeys.contains(key)) {
                continue;
            }
            // 2. 逐个 KEY 转换，单个失败不影响其它
            putSafely(result, errors, key, () -> toJsonValue(entry.getValue()), path + "." + key);
        }
        return new LinkedHashMap<>(result);
    }

    /**
     * 将 SDK 对象转换为 Map / List / 基础类型，与原对象脱钩
     *
     * @param value SDK 对象
     * @return JSON 值
     */
    private static Object toJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        // 1. 官方 SDK 对象：基于 Jackson 2 注解（JsonField 等），用 SDK 自带的序列化，再读回成 Map / List
        String sdkJson = toSdkJson(value);
        if (sdkJson != null) {
            return JsonUtils.parseObject(sdkJson, Object.class);
        }
        // 2. 其它对象：Jackson 3 已内置 Optional 等 JDK 8 类型的支持
        return JsonUtils.getObjectMapper().convertValue(value, Object.class);
    }

    /**
     * 使用官方 SDK 自带的序列化，把 SDK 对象转换为 JSON 字符串
     *
     * Spring AI 2.x 的 OpenAI、Anthropic、Google GenAI 基于官方 SDK，原生用量等元数据是 SDK 对象
     *
     * @param value 对象
     * @return JSON 字符串；不是这几个 SDK 的对象时返回 null
     */
    private static String toSdkJson(Object value) {
        if (value instanceof JsonSerializable) {
            return ((JsonSerializable) value).toJson();
        }
        String className = value.getClass().getName();
        try {
            if (className.startsWith("com.openai.")) {
                return com.openai.core.ObjectMappers.jsonMapper().writeValueAsString(value);
            }
            if (className.startsWith("com.anthropic.")) {
                return com.anthropic.core.ObjectMappers.jsonMapper().writeValueAsString(value);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("SDK 对象序列化失败：" + className, ex);
        }
        return null;
    }

    /**
     * 执行 supplier 并写入 target；失败时跳过，并记录路径与异常信息
     *
     * @param target   写入目标
     * @param errors   转换失败记录
     * @param key      写入的 KEY
     * @param supplier 值的提供者
     * @param path     失败记录的路径
     */
    private static void putSafely(Map<String, Object> target, List<Map<String, Object>> errors,
                                  String key, Supplier<?> supplier, String path) {
        try {
            target.put(key, supplier.get());
        } catch (Exception ex) {
            errors.add(MapUtil.builder(new LinkedHashMap<String, Object>())
                    .put("path", path).put("error", ExceptionUtil.getMessage(ex)).build());
        }
    }

}

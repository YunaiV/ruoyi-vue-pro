package cn.iocoder.yudao.module.ai1.framework.ai.core.llm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * AI1 LLM 模型工厂
 *
 * 按 {@link Ai1ProviderRuntime} 程序化构建 Spring AI 模型：
 * 1. 对话模型：OpenAiChatModel（OpenAI 兼容协议，覆盖 DeepSeek、GLM、Ollama(/v1) 等）
 * 2. 嵌入模型：OpenAiEmbeddingModel（向量化）
 *
 * 模型按 providerId:modelId 缓存复用（编号全局唯一，无需再区分租户），缓存使用 Guava Cache 按容量淘汰最久未访问的模型；
 * Provider、模型的配置变更时，由对应 Service 调用 {@link #evictByProviderId}、{@link #evictByModelId} 显式失效
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1LlmModelFactory {

    /**
     * 附属 Header 会话占位符：构建模型时按当前对话替换
     */
    public static final String SESSION_PLACEHOLDER = "{session}";
    // TODO @AI：这个是不是不用噢？直接 id 是不是就 ok 了？
    /**
     * 会话占位符替换值的前缀，完整值为「前缀 + 对话编号」
     */
    private static final String SESSION_VALUE_PREFIX = "ai1-conversation-";
    /**
     * 缓存上限，达到后逐出最久未访问的模型
     */
    private static final int CACHE_MAX = 128;
    /**
     * 缓存 key 分隔符
     */
    private static final String KEY_SEPARATOR = ":";

    /**
     * 对话模型缓存：providerId:modelId:conversationKey → ChatModel
     */
    private final Cache<String, OpenAiChatModel> chatModelCache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX).build();
    /**
     * 嵌入模型缓存：providerId:modelId → EmbeddingModel
     */
    private final Cache<String, OpenAiEmbeddingModel> embeddingModelCache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX).build();

    /**
     * 获取（或构建）对话模型，并包装为 ChatClient
     *
     * @param runtime        模型运行时快照
     * @param conversationId 对话编号，用于替换附属 Header 中的 {session} 占位符，可为空
     * @return ChatClient
     */
    public ChatClient buildChatClient(Ai1ProviderRuntime runtime, Long conversationId) {
        return ChatClient.builder(getOrCreateChatModel(runtime, conversationId)).build();
    }

    /**
     * 获取（或构建）嵌入模型
     *
     * @param runtime 模型运行时快照
     * @return 嵌入模型
     */
    public EmbeddingModel getOrCreateEmbeddingModel(Ai1ProviderRuntime runtime) {
        // TODO DONE @AI：key 是不是抽个方法出来？
        // 缓存 key 统一由 buildCacheKey 构建
        return embeddingModelCache.asMap().computeIfAbsent(buildCacheKey(runtime), key -> {
            OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                    .baseUrl(normalizeBaseUrl(runtime.getBaseUrl()))
                    .apiKey(runtime.getApiKey())
                    .model(runtime.getModel())
                    .customHeaders(buildHeaders(runtime.getHeaders(), null))
                    .build();
            log.debug("[getOrCreateEmbeddingModel][providerId({}) modelId({}) 嵌入模型构建完成]", runtime.getProviderId(), runtime.getModelId());
            return OpenAiEmbeddingModel.builder().options(options).build();
        });
    }

    /**
     * 失效指定 Provider 下的全部模型缓存（Provider 的地址、密钥、Header、状态变更或删除时调用）
     *
     * @param providerId Provider 编号
     */
    public void evictByProviderId(Long providerId) {
        String prefix = providerId + KEY_SEPARATOR;
        evict(key -> key.startsWith(prefix));
    }

    /**
     * 失效指定模型的缓存（模型标识、类型、状态变更或删除时调用）
     *
     * @param modelId 模型编号
     */
    public void evictByModelId(Long modelId) {
        String modelIdText = String.valueOf(modelId);
        evict(key -> modelIdText.equals(StrUtil.split(key, KEY_SEPARATOR).get(1)));
    }

    // TODO DONE @AI：java.util.function. 多了？
    /**
     * 按 key 条件失效对话模型、嵌入模型缓存
     *
     * @param keyMatcher key 匹配条件
     */
    private void evict(Predicate<String> keyMatcher) {
        chatModelCache.asMap().keySet().removeIf(keyMatcher);
        embeddingModelCache.asMap().keySet().removeIf(keyMatcher);
    }

    /**
     * 获取（或构建）对话模型
     *
     * 仅当附属 Header 使用 {session} 占位符（需按对话隔离）时，才把对话编号纳入缓存 key；
     * 否则同一模型跨对话复用，避免每个对话都构建一个模型实例
     */
    // TODO DONE @AI：方法内注释，这样更好理解；1. 2. 这种；
    private OpenAiChatModel getOrCreateChatModel(Ai1ProviderRuntime runtime, Long conversationId) {
        // 1. 计算缓存 key：Header 含 {session} 占位符时按对话隔离，否则跨对话共享
        String conversationKey = usesSessionHeader(runtime.getHeaders()) && conversationId != null
                ? String.valueOf(conversationId) : "";
        String cacheKey = buildCacheKey(runtime) + KEY_SEPARATOR + conversationKey;

        // 2. 命中缓存直接复用；未命中时按运行时快照构建 OpenAI 兼容对话模型并缓存
        return chatModelCache.asMap().computeIfAbsent(cacheKey, key -> {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .baseUrl(normalizeBaseUrl(runtime.getBaseUrl()))
                    .apiKey(runtime.getApiKey())
                    .model(runtime.getModel())
                    .customHeaders(buildHeaders(runtime.getHeaders(), conversationId))
                    .build();
            log.debug("[getOrCreateChatModel][providerId({}) modelId({}) 对话模型构建完成]", runtime.getProviderId(), runtime.getModelId());
            return OpenAiChatModel.builder().options(options).build();
        });
    }

    /**
     * 构建模型缓存 key：providerId:modelId:providerUpdateTime:modelUpdateTime
     *
     * 带上供应商、模型的更新时间：其他节点修改配置后，本节点拿到的运行时快照更新时间变化，自然命中不到旧缓存，无需广播失效
     *
     * @param runtime 模型运行时快照
     * @return 缓存 key
     */
    private static String buildCacheKey(Ai1ProviderRuntime runtime) {
        return runtime.getProviderId() + KEY_SEPARATOR + runtime.getModelId()
                + KEY_SEPARATOR + runtime.getProviderUpdateTime()
                + KEY_SEPARATOR + runtime.getModelUpdateTime();
    }

    // TODO DONE @AI： http://host:11434 注释风格；
    /**
     * 归一化接口地址：去掉结尾斜杠；裸地址（无路径）自动补 /v1，
     * 保证连通测试、模型拉取与实际对话访问同一个 OpenAI 兼容端点
     *
     * 例如说：Ollama 的 <code>http://127.0.0.1:11434</code> 归一化为 <code>http://127.0.0.1:11434/v1</code>
     *
     * @param baseUrl 接口地址
     * @return 归一化后的地址
     */
    public static String normalizeBaseUrl(String baseUrl) {
        String url = StrUtil.removeSuffix(StrUtil.trim(baseUrl), "/");
        try {
            String path = URI.create(url).getPath();
            if (StrUtil.isBlank(path) || "/".equals(path)) {
                url = url + "/v1";
            }
        } catch (Exception e) {
            // 非法 URL 按原值使用，交由后续请求报错
            log.warn("[normalizeBaseUrl][接口地址({}) 解析失败，按原值使用]", url, e);
        }
        return url;
    }

    /**
     * 附属 Header 是否使用 {session} 占位符
     */
    private static boolean usesSessionHeader(List<Map<String, String>> headers) {
        // TODO DONE @AI：findone 是不是就行了，不用判空了？
        // CollUtil.findOne 对空集合返回 null，无需额外判空
        return CollUtil.findOne(headers, header -> StrUtil.contains(header.get("value"), SESSION_PLACEHOLDER)) != null;
    }

    /**
     * 构建请求 Header：{session} 占位符替换为「ai1-conversation-对话编号」；对话编号为空时，跳过带占位符的 Header
     */
    private static Map<String, String> buildHeaders(List<Map<String, String>> headers, Long conversationId) {
        Map<String, String> result = new LinkedHashMap<>();
        if (CollUtil.isEmpty(headers)) {
            return result;
        }
        for (Map<String, String> header : headers) {
            String key = header.get("key");
            String value = header.get("value");
            if (StrUtil.isBlank(key)) {
                continue;
            }
            if (StrUtil.contains(value, SESSION_PLACEHOLDER)) {
                if (conversationId == null) {
                    continue;
                }
                value = value.replace(SESSION_PLACEHOLDER, SESSION_VALUE_PREFIX + conversationId);
            }
            result.put(key.trim(), StrUtil.nullToEmpty(value));
        }
        return result;
    }

}

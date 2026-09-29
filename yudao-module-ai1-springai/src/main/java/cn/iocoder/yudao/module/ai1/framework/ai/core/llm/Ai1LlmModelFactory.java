package cn.iocoder.yudao.module.ai1.framework.ai.core.llm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.service.provider.bo.Ai1ProviderRuntime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

// TODO @AI：guava 简化？
/**
 * AI1 LLM 模型工厂
 *
 * 按 {@link Ai1ProviderRuntime} 程序化构建 Spring AI 模型：
 * 1. 对话模型：OpenAiChatModel（OpenAI 兼容协议，覆盖 DeepSeek、GLM、Ollama(/v1) 等）
 * 2. 嵌入模型：OpenAiEmbeddingModel（向量化）
 *
 * 模型按 providerId:modelId 缓存复用（编号全局唯一，无需再区分租户）；Provider、模型的配置变更时，
 * 由对应 Service 调用 {@link #evictByProviderId}、{@link #evictByModelId} 显式失效
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1LlmModelFactory {

    /**
     * 附属 Header 会话占位符：构建模型时按当前会话编号替换
     */
    public static final String SESSION_PLACEHOLDER = "{session}";
    /**
     * 缓存上限，达到后逐出最久未访问的模型
     */
    private static final int CACHE_MAX = 128;
    /**
     * 缓存 key 分隔符
     */
    private static final String KEY_SEPARATOR = ":";

    /**
     * 对话模型缓存：providerId:modelId:sessionKey → ChatModel
     */
    private final Map<String, OpenAiChatModel> chatModelCache = Collections.synchronizedMap(new LruCache<>());
    /**
     * 嵌入模型缓存：providerId:modelId → EmbeddingModel
     */
    private final Map<String, OpenAiEmbeddingModel> embeddingModelCache = Collections.synchronizedMap(new LruCache<>());

    /**
     * 获取（或构建）对话模型，并包装为 ChatClient
     *
     * @param runtime   模型运行时快照
     * @param sessionId 会话标识，用于替换附属 Header 中的 {session} 占位符，可为空
     * @return ChatClient
     */
    public ChatClient buildChatClient(Ai1ProviderRuntime runtime, String sessionId) {
        return ChatClient.builder(getOrCreateChatModel(runtime, sessionId)).build();
    }

    /**
     * 获取（或构建）嵌入模型
     *
     * @param runtime 模型运行时快照
     * @return 嵌入模型
     */
    public EmbeddingModel getOrCreateEmbeddingModel(Ai1ProviderRuntime runtime) {
        // TODO @AI：key 是不是抽个方法出来？
        String key = runtime.getProviderId() + KEY_SEPARATOR + runtime.getModelId();
        OpenAiEmbeddingModel cached = embeddingModelCache.get(key);
        if (cached != null) {
            return cached;
        }
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .baseUrl(normalizeBaseUrl(runtime.getBaseUrl()))
                .apiKey(runtime.getApiKey())
                .model(runtime.getModel())
                .customHeaders(buildHeaders(runtime.getHeaders(), null))
                .build();
        OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder().options(options).build();
        embeddingModelCache.put(key, model);
        log.debug("[getOrCreateEmbeddingModel][providerId({}) modelId({}) 嵌入模型构建完成]", runtime.getProviderId(), runtime.getModelId());
        return model;
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

    // TODO @AI：java.util.function. 多了？
    private void evict(java.util.function.Predicate<String> keyMatcher) {
        synchronized (chatModelCache) {
            chatModelCache.keySet().removeIf(keyMatcher);
        }
        synchronized (embeddingModelCache) {
            embeddingModelCache.keySet().removeIf(keyMatcher);
        }
    }

    /**
     * 获取（或构建）对话模型
     *
     * 仅当附属 Header 使用 {session} 占位符（需按会话隔离）时，才把 sessionId 纳入缓存 key；
     * 否则同一模型跨会话复用，避免每个对话都构建一个模型实例
     */
    // TODO @AI：方法内注释，这样更好理解；1. 2. 这种；
    private OpenAiChatModel getOrCreateChatModel(Ai1ProviderRuntime runtime, String sessionId) {
        String sessionKey = usesSessionHeader(runtime.getHeaders()) ? StrUtil.nullToEmpty(sessionId) : "";
        String key = runtime.getProviderId() + KEY_SEPARATOR + runtime.getModelId() + KEY_SEPARATOR + sessionKey;
        OpenAiChatModel cached = chatModelCache.get(key);
        if (cached != null) {
            return cached;
        }
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .baseUrl(normalizeBaseUrl(runtime.getBaseUrl()))
                .apiKey(runtime.getApiKey())
                .model(runtime.getModel())
                .customHeaders(buildHeaders(runtime.getHeaders(), sessionId))
                .build();
        OpenAiChatModel model = OpenAiChatModel.builder().options(options).build();
        chatModelCache.put(key, model);
        log.debug("[getOrCreateChatModel][providerId({}) modelId({}) 对话模型构建完成]", runtime.getProviderId(), runtime.getModelId());
        return model;
    }

    // TODO @AI： http://host:11434 注释风格；
    /**
     * 归一化接口地址：去掉结尾斜杠；裸地址（无路径，如 Ollama 的 http://host:11434）自动补 /v1，
     * 保证连通测试、模型拉取与实际对话访问同一个 OpenAI 兼容端点
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
        // TODO @AI：findone 是不是就行了，不用判空了？
        return CollUtil.isNotEmpty(headers)
                && CollUtil.findOne(headers, header -> StrUtil.contains(header.get("value"), SESSION_PLACEHOLDER)) != null;
    }

    /**
     * 构建请求 Header：{session} 占位符按会话编号替换；会话编号为空时，跳过带占位符的 Header
     */
    private static Map<String, String> buildHeaders(List<Map<String, String>> headers, String sessionId) {
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
                if (StrUtil.isBlank(sessionId)) {
                    continue;
                }
                value = value.replace(SESSION_PLACEHOLDER, sessionId);
            }
            result.put(key.trim(), StrUtil.nullToEmpty(value));
        }
        return result;
    }

    // TODO @AI：guava 可以简化下么？
    /**
     * 简易 LRU 缓存：达到上限后逐出最久未访问的项
     */
    private static class LruCache<K, V> extends LinkedHashMap<K, V> {

        LruCache() {
            super(16, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_MAX;
        }

    }

}

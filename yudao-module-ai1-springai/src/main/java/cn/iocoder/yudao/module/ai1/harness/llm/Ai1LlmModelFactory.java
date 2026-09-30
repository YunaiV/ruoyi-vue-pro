package cn.iocoder.yudao.module.ai1.harness.llm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
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
 * 按 {@link Ai1ModelRespBO} 程序化构建 Spring AI 模型：
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
     * 附属 Header 对话占位符：构建模型时替换为当前对话编号
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
    private final Cache<String, OpenAiChatModel> chatModelCache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX).build();
    /**
     * 嵌入模型缓存：providerId:modelId → EmbeddingModel
     */
    private final Cache<String, OpenAiEmbeddingModel> embeddingModelCache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX).build();

    /**
     * 获取（或构建）对话模型，并包装为 ChatClient
     *
     * @param model        模型运行时快照
     * @param sessionId 对话编号，用于替换附属 Header 中的 {session} 占位符，可为空
     * @return ChatClient
     */
    public ChatClient buildChatClient(Ai1ModelRespBO model, Long sessionId) {
        return ChatClient.builder(getOrCreateChatModel(model, sessionId)).build();
    }

    /**
     * 获取（或构建）嵌入模型
     *
     * @param model 模型运行时快照
     * @return 嵌入模型
     */
    public EmbeddingModel getOrCreateEmbeddingModel(Ai1ModelRespBO model) {
        return embeddingModelCache.asMap().computeIfAbsent(buildCacheKey(model), key -> {
            OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                    .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                    .apiKey(model.getApiKey())
                    .model(model.getModel())
                    .customHeaders(buildHeaders(model.getHeaders(), null))
                    .build();
            log.debug("[getOrCreateEmbeddingModel][providerId({}) modelId({}) 嵌入模型构建完成]", model.getProviderId(), model.getModelId());
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
    private OpenAiChatModel getOrCreateChatModel(Ai1ModelRespBO model, Long sessionId) {
        // 1. 计算缓存 key：Header 含 {session} 占位符时按对话隔离，否则跨对话共享
        String sessionKey = usesSessionHeader(model.getHeaders()) && sessionId != null
                ? String.valueOf(sessionId) : "";
        String cacheKey = buildCacheKey(model) + KEY_SEPARATOR + sessionKey;

        // 2. 命中缓存直接复用；未命中时按运行时快照构建 OpenAI 兼容对话模型并缓存
        return chatModelCache.asMap().computeIfAbsent(cacheKey, key -> {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .baseUrl(normalizeBaseUrl(model.getBaseUrl()))
                    .apiKey(model.getApiKey())
                    .model(model.getModel())
                    .customHeaders(buildHeaders(model.getHeaders(), sessionId))
                    .build();
            log.debug("[getOrCreateChatModel][providerId({}) modelId({}) 对话模型构建完成]", model.getProviderId(), model.getModelId());
            return OpenAiChatModel.builder().options(options).build();
        });
    }

    /**
     * 构建模型缓存 key：providerId:modelId
     *
     * @param model 模型运行时快照
     * @return 缓存 key
     */
    private static String buildCacheKey(Ai1ModelRespBO model) {
        return model.getProviderId() + KEY_SEPARATOR + model.getModelId();
    }

    /**
     * 归一化接口地址：去掉结尾斜杠；裸地址（无路径）自动补 /v1，
     * 保证连通测试、模型拉取与实际对话访问同一个 OpenAI 兼容端点
     *
     * 例如说：Ollama 的 <a href="http://127.0.0.1:11434">http://127.0.0.1:11434</a>
     * 归一化为 <a href="http://127.0.0.1:11434/v1">http://127.0.0.1:11434/v1</a>
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
        return CollUtil.findOne(headers, header -> StrUtil.contains(header.get("value"), SESSION_PLACEHOLDER)) != null;
    }

    /**
     * 构建请求 Header：{session} 占位符替换为对话编号；对话编号为空时，跳过带占位符的 Header
     */
    private static Map<String, String> buildHeaders(List<Map<String, String>> headers, Long sessionId) {
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
                if (sessionId == null) {
                    continue;
                }
                value = value.replace(SESSION_PLACEHOLDER, String.valueOf(sessionId));
            }
            result.put(key.trim(), StrUtil.nullToEmpty(value));
        }
        return result;
    }

}

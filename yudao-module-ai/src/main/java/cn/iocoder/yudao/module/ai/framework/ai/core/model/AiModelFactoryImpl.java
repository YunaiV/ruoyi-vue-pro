package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.config.AiAutoConfiguration;
import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.moonshot.MoonshotChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.stepfun.StepFunChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.yiyan.YiYanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.zhipu.ZhiPuChatModel;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingAutoConfiguration;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeImageAutoConfiguration;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import io.micrometer.observation.ObservationRegistry;
import io.milvus.client.MilvusServiceClient;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.SneakyThrows;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiChatAutoConfiguration;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration;
import org.springframework.ai.model.stabilityai.autoconfigure.StabilityAiImageAutoConfiguration;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.*;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientConnectionDetails;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientProperties;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.RedisClient;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * AI Model 模型工厂的实现类
 *
 * @author 芋道源码
 */
public class AiModelFactoryImpl implements AiModelFactory {

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String rawApiKey, String rawUrl) {
        final String apiKey = resolveSpringPlaceholders(rawApiKey);
        final String url = resolveSpringPlaceholders(rawUrl);
        String cacheKey = buildClientCacheKey(ChatModel.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<ChatModel>) () -> {
            // noinspection EnhancedSwitchMigration
            switch (platform) {
                case TONG_YI:
                    return buildTongYiChatModel(apiKey);
                case YI_YAN:
                    return buildYiYanChatModel(apiKey);
                case DEEP_SEEK:
                    return buildDeepSeekChatModel(apiKey);
                case DOU_BAO:
                    return buildDouBaoChatModel(apiKey);
                case HUN_YUAN:
                    return buildHunYuanChatModel(apiKey, url);
                case SILICON_FLOW:
                    return buildSiliconFlowChatModel(apiKey);
                case ZHI_PU:
                    return buildZhiPuChatModel(apiKey, url);
                case MINI_MAX:
                    return buildMiniMaxChatModel(apiKey, url);
                case MOONSHOT:
                    return buildMoonshotChatModel(apiKey, url);
                case STEP_FUN:
                    return buildStepFunChatModel(apiKey, url);
                case XING_HUO:
                    return buildXingHuoChatModel(apiKey);
                case BAI_CHUAN:
                    return buildBaiChuanChatModel(apiKey);
                case OPENAI:
                    return buildOpenAiChatModel(apiKey, url);
                case AZURE_OPENAI:
                    return buildAzureOpenAiChatModel(apiKey, url);
                case ANTHROPIC:
                    return buildAnthropicChatModel(apiKey, url);
                case GEMINI:
                    return buildGeminiChatModel(apiKey, url);
                case OLLAMA:
                    return buildOllamaChatModel(url);
                case GROK:
                    return buildGrokChatModel(apiKey, url);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public ChatModel getDefaultChatModel(AiPlatformEnum platform) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtil.getBean(DashScopeChatModel.class);
            case YI_YAN:
                return SpringUtil.getBean(YiYanChatModel.class);
            case DEEP_SEEK:
                return SpringUtil.getBean(DeepSeekChatModel.class);
            case DOU_BAO:
                return SpringUtil.getBean(DouBaoChatModel.class);
            case HUN_YUAN:
                return SpringUtil.getBean(HunYuanChatModel.class);
            case SILICON_FLOW:
                return SpringUtil.getBean(SiliconFlowChatModel.class);
            case ZHI_PU:
                return SpringUtil.getBean(ZhiPuChatModel.class);
            case MINI_MAX:
                return SpringUtil.getBean(MiniMaxChatModel.class);
            case MOONSHOT:
                return SpringUtil.getBean(MoonshotChatModel.class);
            case STEP_FUN:
                return SpringUtil.getBean(StepFunChatModel.class);
            case XING_HUO:
                return SpringUtil.getBean(XingHuoChatModel.class);
            case BAI_CHUAN:
                return SpringUtil.getBean(BaiChuanChatModel.class);
            case OPENAI:
                return SpringUtil.getBean(OpenAiChatModel.class);
            case ANTHROPIC:
                return SpringUtil.getBean(AnthropicChatModel.class);
            case GEMINI:
                return SpringUtil.getBean(GoogleGenAiChatModel.class);
            case OLLAMA:
                return SpringUtil.getBean(OllamaChatModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getDefaultImageModel(AiPlatformEnum platform) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtil.getBean(DashScopeImageModel.class);
            case SILICON_FLOW:
                return SpringUtil.getBean(SiliconFlowImageModel.class);
            case OPENAI:
                return SpringUtil.getBean(OpenAiImageModel.class);
            case STABLE_DIFFUSION:
                return SpringUtil.getBean(StabilityAiImageModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getOrCreateImageModel(AiPlatformEnum platform, String rawApiKey, String rawUrl) {
        String apiKey = resolveSpringPlaceholders(rawApiKey);
        String url = resolveSpringPlaceholders(rawUrl);
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return buildTongYiImagesModel(apiKey);
            case OPENAI:
                return buildOpenAiImageModel(apiKey, url);
            case SILICON_FLOW:
                return buildSiliconFlowImageModel(apiKey, url);
            case STABLE_DIFFUSION:
                return buildStabilityAiImageModel(apiKey, url);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public MidjourneyApi getOrCreateMidjourneyApi(String rawApiKey, String rawUrl) {
        final String apiKey = resolveSpringPlaceholders(rawApiKey);
        final String url = resolveSpringPlaceholders(rawUrl);
        String cacheKey = buildClientCacheKey(MidjourneyApi.class, AiPlatformEnum.MIDJOURNEY.getPlatform(),
                apiKey, url);
        return Singleton.get(cacheKey, (Func0<MidjourneyApi>) () -> {
            YudaoAiProperties.Midjourney properties = SpringUtil.getBean(YudaoAiProperties.class)
                    .getMidjourney();
            return new MidjourneyApi(url, apiKey, properties.getNotifyUrl());
        });
    }

    @Override
    public SunoApi getOrCreateSunoApi(String rawApiKey, String rawUrl) {
        final String apiKey = resolveSpringPlaceholders(rawApiKey);
        final String url = resolveSpringPlaceholders(rawUrl);
        String cacheKey = buildClientCacheKey(SunoApi.class, AiPlatformEnum.SUNO.getPlatform(), apiKey, url);
        return Singleton.get(cacheKey, (Func0<SunoApi>) () -> new SunoApi(url));
    }

    @Override
    @SuppressWarnings("EnhancedSwitchMigration")
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String rawApiKey, String rawUrl, String model) {
        final String apiKey = resolveSpringPlaceholders(rawApiKey);
        final String url = resolveSpringPlaceholders(rawUrl);
        String cacheKey = buildClientCacheKey(EmbeddingModel.class, platform, apiKey, url, model);
        return Singleton.get(cacheKey, (Func0<EmbeddingModel>) () -> {
            switch (platform) {
                case TONG_YI:
                    return buildTongYiEmbeddingModel(apiKey, model);
                case OPENAI:
                    return buildOpenAiEmbeddingModel(apiKey, url, model);
                case AZURE_OPENAI:
                    return buildAzureOpenAiEmbeddingModel(apiKey, url, model);
                case OLLAMA:
                    return buildOllamaEmbeddingModel(url, model);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public VectorStore getOrCreateVectorStore(Class<? extends VectorStore> type,
                                              EmbeddingModel embeddingModel,
                                              Map<String, Class<?>> metadataFields) {
        String cacheKey = buildClientCacheKey(VectorStore.class, embeddingModel, type);
        return Singleton.get(cacheKey, (Func0<VectorStore>) () -> {
            if (type == SimpleVectorStore.class) {
                return buildSimpleVectorStore(embeddingModel);
            }
            if (type == QdrantVectorStore.class) {
                return buildQdrantVectorStore(embeddingModel);
            }
            if (type == RedisVectorStore.class) {
                return buildRedisVectorStore(embeddingModel, metadataFields);
            }
            if (type == MilvusVectorStore.class) {
                return buildMilvusVectorStore(embeddingModel);
            }
            throw new IllegalArgumentException(StrUtil.format("未知类型({})", type));
        });
    }

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }

    private static String resolveSpringPlaceholders(String value) {
        // yml 配置的占位符由 Spring 自动解析；DB 里保存的 ${xxx} 需要在这里手动解析。
        return AiUtils.resolveSpringPlaceholders(value);
    }

    // ========== 各种创建 spring-ai 客户端的方法 ==========

    /**
     * 可参考 {@link DashScopeChatAutoConfiguration} 的 dashscopeChatModel 方法
     */
    private static DashScopeChatModel buildTongYiChatModel(String key) {
        return AiAutoConfiguration.buildTongYiChatModel(key);
    }

    /**
     * 可参考 {@link DashScopeImageAutoConfiguration} 的 dashScopeImageModel 方法
     */
    private static DashScopeImageModel buildTongYiImagesModel(String key) {
        return AiAutoConfiguration.buildTongYiImagesModel(key);
    }

    private ChatModel buildYiYanChatModel(String apiKey) {
        YudaoAiProperties.YiYan properties = new YudaoAiProperties.YiYan()
                .setApiKey(apiKey);
        return new AiAutoConfiguration().buildYiYanChatClient(properties);
    }

    /**
     * 可参考 {@link DeepSeekChatAutoConfiguration} 的 deepSeekChatModel 方法
     */
    private static DeepSeekChatModel buildDeepSeekChatModel(String apiKey) {
        DeepSeekApi deepSeekApi = DeepSeekApi.builder().apiKey(apiKey).build();
        DeepSeekChatOptions options = DeepSeekChatOptions.builder().model(DeepSeekApi.DEFAULT_CHAT_MODEL)
                .temperature(0.7).build();
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .options(options)
                .build();
    }

    /**
     * 可参考 {@link AiAutoConfiguration#douBaoChatClient(YudaoAiProperties)}
     */
    private ChatModel buildDouBaoChatModel(String apiKey) {
        YudaoAiProperties.DouBao properties = new YudaoAiProperties.DouBao()
                .setApiKey(apiKey);
        return new AiAutoConfiguration().buildDouBaoChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#hunYuanChatClient(YudaoAiProperties)}
     */
    private ChatModel buildHunYuanChatModel(String apiKey, String url) {
        YudaoAiProperties.HunYuan properties = new YudaoAiProperties.HunYuan()
                .setBaseUrl(url).setApiKey(apiKey);
        return new AiAutoConfiguration().buildHunYuanChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#siliconFlowChatClient(YudaoAiProperties)}
     */
    private ChatModel buildSiliconFlowChatModel(String apiKey) {
        YudaoAiProperties.SiliconFlow properties = new YudaoAiProperties.SiliconFlow()
                .setApiKey(apiKey);
        return new AiAutoConfiguration().buildSiliconFlowChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#zhiPuChatClient(YudaoAiProperties)}
     */
    private ZhiPuChatModel buildZhiPuChatModel(String apiKey, String url) {
        YudaoAiProperties.ZhiPu properties = new YudaoAiProperties.ZhiPu()
                .setBaseUrl(url).setApiKey(apiKey);
        return new AiAutoConfiguration().buildZhiPuChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#miniMaxChatClient(YudaoAiProperties)}
     */
    private MiniMaxChatModel buildMiniMaxChatModel(String apiKey, String url) {
        YudaoAiProperties.MiniMax properties = new YudaoAiProperties.MiniMax()
                .setBaseUrl(url).setApiKey(apiKey);
        return new AiAutoConfiguration().buildMiniMaxChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#moonshotChatClient(YudaoAiProperties)}
     */
    private MoonshotChatModel buildMoonshotChatModel(String apiKey, String url) {
        YudaoAiProperties.Moonshot properties = new YudaoAiProperties.Moonshot()
                .setBaseUrl(url).setApiKey(apiKey);
        return new AiAutoConfiguration().buildMoonshotChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#stepFunChatClient(YudaoAiProperties)}
     */
    private StepFunChatModel buildStepFunChatModel(String apiKey, String url) {
        YudaoAiProperties.StepFun properties = new YudaoAiProperties.StepFun()
                .setBaseUrl(url).setApiKey(apiKey);
        return new AiAutoConfiguration().buildStepFunChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#xingHuoChatClient(YudaoAiProperties)}
     */
    private static XingHuoChatModel buildXingHuoChatModel(String apiKey) {
        YudaoAiProperties.XingHuo properties = new YudaoAiProperties.XingHuo()
                .setApiKey(apiKey).setModel(XingHuoChatModel.MODEL_DEFAULT);
        return new AiAutoConfiguration().buildXingHuoChatClient(properties);
    }

    /**
     * 可参考 {@link AiAutoConfiguration#baiChuanChatClient(YudaoAiProperties)}
     */
    private BaiChuanChatModel buildBaiChuanChatModel(String apiKey) {
        YudaoAiProperties.BaiChuan properties = new YudaoAiProperties.BaiChuan()
                .setApiKey(apiKey);
        return new AiAutoConfiguration().buildBaiChuanChatClient(properties);
    }

    /**
     * 可参考 {@link OpenAiChatAutoConfiguration} 的 openAiChatModel 方法
     */
    private static OpenAiChatModel buildOpenAiChatModel(String openAiToken, String url) {
        return OpenAiChatModel.builder()
                .options(buildOpenAiChatOptions(openAiToken, url).build())
                .build();
    }

    private static OpenAiChatModel buildAzureOpenAiChatModel(String openAiToken, String url) {
        return OpenAiChatModel.builder()
                .options(buildOpenAiChatOptions(openAiToken, url)
                        .azure(true)
                        .build())
                .build();
    }

    private static OpenAiChatOptions.Builder buildOpenAiChatOptions(String apiKey, String url) {
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder().apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            optionsBuilder.baseUrl(url);
        }
        return optionsBuilder;
    }

    /**
     * 可参考 {@link AnthropicChatAutoConfiguration} 的 anthropicApi 方法
     */
    private static AnthropicChatModel buildAnthropicChatModel(String apiKey, String url) {
        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder().apiKey(apiKey);
        if (StrUtil.isNotEmpty(url)) {
            optionsBuilder.baseUrl(url);
        }
        return AnthropicChatModel.builder()
                .options(optionsBuilder.build())
                .build();
    }

    /**
     * 可参考 {@link GoogleGenAiChatAutoConfiguration} 的 googleGenAiChatModel 方法
     */
    private static GoogleGenAiChatModel buildGeminiChatModel(String apiKey, String url) {
        Client.Builder clientBuilder = Client.builder().apiKey(apiKey);
        if (StrUtil.isNotBlank(url)) {
            clientBuilder.httpOptions(HttpOptions.builder()
                    .baseUrl(url)
                    // TeamOrouter 的 Gemini 原生协议使用 Authorization Bearer 鉴权
                    .headers(Collections.singletonMap("Authorization", "Bearer " + apiKey))
                    .build());
        }
        return GoogleGenAiChatModel.builder()
                .genAiClient(clientBuilder.build())
                .options(GoogleGenAiChatOptions.builder()
                        .model("gemini-2.5-flash")
                        .build())
                .toolCallingManager(SpringUtil.getBean(ToolCallingManager.class))
                .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                .observationRegistry(SpringUtil.getBean(ObservationRegistry.class))
                .build();
    }

    /**
     * 可参考 {@link OpenAiImageAutoConfiguration} 的 openAiImageModel 方法
     */
    private OpenAiImageModel buildOpenAiImageModel(String openAiToken, String url) {
        OpenAiImageOptions.Builder optionsBuilder = OpenAiImageOptions.builder().apiKey(openAiToken);
        if (StrUtil.isNotEmpty(url)) {
            optionsBuilder.baseUrl(url);
        }
        return OpenAiImageModel.builder()
                .options(optionsBuilder.build())
                .build();
    }

    /**
     * 创建 SiliconFlowImageModel 对象
     */
    private SiliconFlowImageModel buildSiliconFlowImageModel(String apiToken, String url) {
        url = StrUtil.blankToDefault(url, SiliconFlowApiConstants.DEFAULT_BASE_URL);
        SiliconFlowImageApi openAiApi = new SiliconFlowImageApi(url, apiToken);
        return new SiliconFlowImageModel(openAiApi);
    }

    /**
     * 可参考 {@link OllamaChatAutoConfiguration} 的 ollamaChatModel 方法
     */
    private static OllamaChatModel buildOllamaChatModel(String url) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(url).build();
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .build();
    }

    /**
     * 可参考 {@link StabilityAiImageAutoConfiguration} 的 stabilityAiImageModel 方法
     */
    private StabilityAiImageModel buildStabilityAiImageModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, StabilityAiApi.DEFAULT_BASE_URL);
        StabilityAiApi stabilityAiApi = new StabilityAiApi(apiKey, StabilityAiApi.DEFAULT_IMAGE_MODEL, url);
        return new StabilityAiImageModel(stabilityAiApi);
    }

    private ChatModel buildGrokChatModel(String apiKey,String url) {
        YudaoAiProperties.Grok properties = new YudaoAiProperties.Grok()
                .setBaseUrl(url)
                .setApiKey(apiKey);
        return new AiAutoConfiguration().buildGrokChatClient(properties);
    }

    // ========== 各种创建 EmbeddingModel 的方法 ==========

    /**
     * 可参考 {@link DashScopeEmbeddingAutoConfiguration} 的 DashScopeEmbeddingModel 方法
     */
    private DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model) {
        return AiAutoConfiguration.buildTongYiEmbeddingModel(apiKey, model);
    }

    private OllamaEmbeddingModel buildOllamaEmbeddingModel(String url, String model) {
        OllamaApi ollamaApi = OllamaApi.builder().baseUrl(url).build();
        OllamaEmbeddingOptions ollamaOptions = OllamaEmbeddingOptions.builder().model(model).build();
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .options(ollamaOptions)
                .build();
    }

    /**
     * 可参考 {@link OpenAiEmbeddingAutoConfiguration} 的 openAiEmbeddingModel 方法
     */
    private OpenAiEmbeddingModel buildOpenAiEmbeddingModel(String openAiToken, String url, String model) {
        OpenAiEmbeddingOptions.Builder optionsBuilder = OpenAiEmbeddingOptions.builder()
                .apiKey(openAiToken)
                .model(model);
        if (StrUtil.isNotEmpty(url)) {
            optionsBuilder.baseUrl(url);
        }
        return OpenAiEmbeddingModel.builder()
                .metadataMode(MetadataMode.EMBED)
                .options(optionsBuilder.build())
                .build();
    }

    private OpenAiEmbeddingModel buildAzureOpenAiEmbeddingModel(String openAiToken, String url, String model) {
        OpenAiEmbeddingOptions.Builder optionsBuilder = OpenAiEmbeddingOptions.builder()
                .apiKey(openAiToken)
                .model(model)
                .deploymentName(model)
                .azure(true);
        if (StrUtil.isNotEmpty(url)) {
            optionsBuilder.baseUrl(url);
        }
        return OpenAiEmbeddingModel.builder()
                .metadataMode(MetadataMode.EMBED)
                .options(optionsBuilder.build())
                .build();
    }

    // ========== 各种创建 VectorStore 的方法 ==========

    /**
     * 注意：仅适合本地测试使用，生产建议还是使用 Qdrant、Milvus 等
     */
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private SimpleVectorStore buildSimpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 启动加载
        File file = new File(StrUtil.format("{}/vector_store/simple_{}.json",
                FileUtil.getUserHomePath(), embeddingModel.getClass().getSimpleName()));
        if (!file.exists()) {
            FileUtil.mkParentDirs(file);
            file.createNewFile();
        } else if (file.length() > 0) {
            vectorStore.load(file);
        }
        // 定时持久化，每分钟一次
        Timer timer = new Timer("SimpleVectorStoreTimer-" + file.getAbsolutePath());
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                vectorStore.save(file);
            }

        }, Duration.ofMinutes(1).toMillis(), Duration.ofMinutes(1).toMillis());
        // 关闭时，进行持久化
        RuntimeUtil.addShutdownHook(() -> vectorStore.save(file));
        return vectorStore;
    }

    /**
     * 参考 {@link QdrantVectorStoreAutoConfiguration} 的 vectorStore 方法
     */
    @SneakyThrows
    private QdrantVectorStore buildQdrantVectorStore(EmbeddingModel embeddingModel) {
        QdrantVectorStoreAutoConfiguration configuration = new QdrantVectorStoreAutoConfiguration();
        QdrantVectorStoreProperties properties = SpringUtil.getBean(QdrantVectorStoreProperties.class);
        // 参考 QdrantVectorStoreAutoConfiguration 实现，创建 QdrantClient 对象
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                properties.getHost(), properties.getPort(), properties.isUseTls());
        if (StrUtil.isNotEmpty(properties.getApiKey())) {
            grpcClientBuilder.withApiKey(properties.getApiKey());
        }
        QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build());
        // 创建 QdrantVectorStore 对象
        QdrantVectorStore vectorStore = configuration.vectorStore(embeddingModel, properties, qdrantClient,
                getObservationRegistry(), getCustomObservationConvention(), getBatchingStrategy());
        // 初始化索引
        vectorStore.afterPropertiesSet();
        return vectorStore;
    }

    /**
     * 参考 {@link RedisVectorStoreAutoConfiguration} 的 vectorStore 方法
     */
    private RedisVectorStore buildRedisVectorStore(EmbeddingModel embeddingModel,
                                                   Map<String, Class<?>> metadataFields) {
        // 创建 RedisClient 对象
        RedisClient redisClient = buildRedisClient();
        // 创建 RedisVectorStoreProperties 对象
        RedisVectorStoreProperties properties = SpringUtil.getBean(RedisVectorStoreProperties.class);
        RedisVectorStore redisVectorStore = RedisVectorStore.builder(redisClient, embeddingModel)
                .indexName(properties.getIndexName()).prefix(properties.getPrefix())
                .initializeSchema(properties.isInitializeSchema())
                .metadataFields(convertList(metadataFields.entrySet(), entry -> {
                    String fieldName = entry.getKey();
                    Class<?> fieldType = entry.getValue();
                    if (Number.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.numeric(fieldName);
                    }
                    if (Boolean.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.tag(fieldName);
                    }
                    return RedisVectorStore.MetadataField.text(fieldName);
                }))
                .observationRegistry(getObservationRegistry().getObject())
                .customObservationConvention(getCustomObservationConvention().getObject())
                .batchingStrategy(getBatchingStrategy())
                .build();
        // 初始化索引
        redisVectorStore.afterPropertiesSet();
        return redisVectorStore;
    }

    private RedisClient buildRedisClient() {
        DataRedisProperties redisProperties = SpringUtil.getBean(DataRedisProperties.class);
        Assert.isNull(redisProperties.getCluster(), "RedisVectorStore 暂不支持 Redis Cluster 模式");
        Assert.isNull(redisProperties.getSentinel(), "RedisVectorStore 暂不支持 Redis Sentinel 模式");
        Assert.isNull(redisProperties.getMasterreplica(), "RedisVectorStore 暂不支持 Redis Master-Replica 模式");
        if (StrUtil.isNotEmpty(redisProperties.getUrl())) {
            return RedisClient.create(redisProperties.getUrl());
        }
        DefaultJedisClientConfig.Builder clientConfigBuilder = DefaultJedisClientConfig.builder()
                .ssl(redisProperties.getSsl().isEnabled())
                .database(redisProperties.getDatabase());
        if (StrUtil.isNotEmpty(redisProperties.getUsername())) {
            clientConfigBuilder.user(redisProperties.getUsername());
        }
        if (StrUtil.isNotEmpty(redisProperties.getPassword())) {
            clientConfigBuilder.password(redisProperties.getPassword());
        }
        if (StrUtil.isNotEmpty(redisProperties.getClientName())) {
            clientConfigBuilder.clientName(redisProperties.getClientName());
        }
        if (redisProperties.getTimeout() != null) {
            clientConfigBuilder.socketTimeoutMillis(toMillis(redisProperties.getTimeout()));
        }
        if (redisProperties.getConnectTimeout() != null) {
            clientConfigBuilder.connectionTimeoutMillis(toMillis(redisProperties.getConnectTimeout()));
        }
        JedisClientConfig clientConfig = clientConfigBuilder.build();
        return RedisClient.builder()
                .hostAndPort(new HostAndPort(redisProperties.getHost(), redisProperties.getPort()))
                .clientConfig(clientConfig)
                .build();
    }

    private static int toMillis(Duration duration) {
        return Math.toIntExact(duration.toMillis());
    }

    /**
     * 参考 {@link MilvusVectorStoreAutoConfiguration} 的 vectorStore 方法
     */
    @SneakyThrows
    private MilvusVectorStore buildMilvusVectorStore(EmbeddingModel embeddingModel) {
        MilvusVectorStoreAutoConfiguration configuration = new MilvusVectorStoreAutoConfiguration();
        // 获取配置属性
        MilvusVectorStoreProperties serverProperties = SpringUtil.getBean(MilvusVectorStoreProperties.class);
        MilvusServiceClientProperties clientProperties = SpringUtil.getBean(MilvusServiceClientProperties.class);

        // 创建 MilvusServiceClient 对象
        MilvusServiceClient milvusClient = configuration.milvusClient(serverProperties, clientProperties,
                new MilvusServiceClientConnectionDetails() {

                    @Override
                    public String getHost() {
                        return clientProperties.getHost();
                    }

                    @Override
                    public int getPort() {
                        return clientProperties.getPort();
                    }

                }
        );
        // 创建 MilvusVectorStore 对象
        MilvusVectorStore vectorStore = configuration.vectorStore(milvusClient, embeddingModel, serverProperties,
                getBatchingStrategy(), getObservationRegistry(), getCustomObservationConvention());

        // 初始化索引
        vectorStore.afterPropertiesSet();
        return vectorStore;
    }

    private static ObjectProvider<ObservationRegistry> getObservationRegistry() {
        return new ObjectProvider<>() {

            @Override
            public ObservationRegistry getObject() throws BeansException {
                return SpringUtil.getBean(ObservationRegistry.class);
            }

        };
    }

    private static ObjectProvider<VectorStoreObservationConvention> getCustomObservationConvention() {
        return new ObjectProvider<>() {

            @Override
            public VectorStoreObservationConvention getObject() throws BeansException {
                return new DefaultVectorStoreObservationConvention();
            }

        };
    }

    private static BatchingStrategy getBatchingStrategy() {
        return SpringUtil.getBean(BatchingStrategy.class);
    }

    private static ObjectProvider<EmbeddingModelObservationConvention> getEmbeddingModelObservationConvention() {
        return new ObjectProvider<>() {

            @Override
            public EmbeddingModelObservationConvention getObject() throws BeansException {
                return SpringUtil.getBean(EmbeddingModelObservationConvention.class);
            }

        };
    }

}

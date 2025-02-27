package cn.iocoder.yudao.framework.ai.core.factory;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.ai.config.YudaoAiAutoConfiguration;
import cn.iocoder.yudao.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.deepseek.DeepSeekChatModel;
import cn.iocoder.yudao.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.azure.ai.openai.OpenAIClientBuilder;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiChatProperties;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiConnectionProperties;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanAutoConfiguration;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiAutoConfiguration;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiConnectionProperties;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * AI Model 模型工厂的实现类
 *
 * @author 芋道源码
 */
public class AiModelFactoryImpl implements AiModelFactory {

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(ChatModel.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<ChatModel>) () -> {
            //noinspection EnhancedSwitchMigration
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
                case XING_HUO:
                    return buildXingHuoChatModel(apiKey);
                case OPENAI:
                    return buildOpenAiChatModel(apiKey, url);
                case AZURE_OPENAI:
                    return buildAzureOpenAiChatModel(apiKey, url);
                case OLLAMA:
                    return buildOllamaChatModel(url);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public ChatModel getDefaultChatModel(AiPlatformEnum platform) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtil.getBean(DashScopeChatModel.class);
            case YI_YAN:
                return SpringUtil.getBean(QianFanChatModel.class);
            case DEEP_SEEK:
                return SpringUtil.getBean(DeepSeekChatModel.class);
            case DOU_BAO:
                return SpringUtil.getBean(DouBaoChatModel.class);
            case HUN_YUAN:
                return SpringUtil.getBean(HunYuanChatModel.class);
            case SILICON_FLOW:
                return SpringUtil.getBean(SiliconFlowChatModel.class);
            case ZHI_PU:
                return SpringUtil.getBean(ZhiPuAiChatModel.class);
            case XING_HUO:
                return SpringUtil.getBean(XingHuoChatModel.class);
            case OPENAI:
                return SpringUtil.getBean(OpenAiChatModel.class);
            case AZURE_OPENAI:
                return SpringUtil.getBean(AzureOpenAiChatModel.class);
            case OLLAMA:
                return SpringUtil.getBean(OllamaChatModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getDefaultImageModel(AiPlatformEnum platform) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtil.getBean(DashScopeImageModel.class);
            case YI_YAN:
                return SpringUtil.getBean(QianFanImageModel.class);
            case ZHI_PU:
                return SpringUtil.getBean(ZhiPuAiImageModel.class);
            case OPENAI:
                return SpringUtil.getBean(OpenAiImageModel.class);
            case STABLE_DIFFUSION:
                return SpringUtil.getBean(StabilityAiImageModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getOrCreateImageModel(AiPlatformEnum platform, String apiKey, String url) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return buildTongYiImagesModel(apiKey);
            case YI_YAN:
                return buildQianFanImageModel(apiKey);
            case ZHI_PU:
                return buildZhiPuAiImageModel(apiKey, url);
            case OPENAI:
                return buildOpenAiImageModel(apiKey, url);
            case STABLE_DIFFUSION:
                return buildStabilityAiImageModel(apiKey, url);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public MidjourneyApi getOrCreateMidjourneyApi(String apiKey, String url) {
        String cacheKey = buildClientCacheKey(MidjourneyApi.class, AiPlatformEnum.MIDJOURNEY.getPlatform(), apiKey, url);
        return Singleton.get(cacheKey, (Func0<MidjourneyApi>) () -> {
            YudaoAiProperties.MidjourneyProperties properties = SpringUtil.getBean(YudaoAiProperties.class).getMidjourney();
            return new MidjourneyApi(url, apiKey, properties.getNotifyUrl());
        });
    }

    @Override
    public SunoApi getOrCreateSunoApi(String apiKey, String url) {
        String cacheKey = buildClientCacheKey(SunoApi.class, AiPlatformEnum.SUNO.getPlatform(), apiKey, url);
        return Singleton.get(cacheKey, (Func0<SunoApi>) () -> new SunoApi(url));
    }

    @Override
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model) {
        String cacheKey = buildClientCacheKey(EmbeddingModel.class, platform, apiKey, url, model);
        return Singleton.get(cacheKey, (Func0<EmbeddingModel>) () -> {
            switch (platform) {
                case TONG_YI:
                    return buildTongYiEmbeddingModel(apiKey, model);
                case OLLAMA:
                    return buildOllamaEmbeddingModel(url, model);
                // TODO @芋艿：各个平台的向量化能力；
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public VectorStore getOrCreateVectorStore(EmbeddingModel embeddingModel) {
//        String cacheKey = buildClientCacheKey(VectorStore.class, platform, apiKey, url);
        String cacheKey = buildClientCacheKey(VectorStore.class, embeddingModel);
        return Singleton.get(cacheKey, (Func0<VectorStore>) () -> {
            // TODO @芋艿：先临时使用 store
            return SimpleVectorStore.builder(embeddingModel).build();
            // TODO @芋艿：@xin：后续看看，是不是切到阿里云之类的
//            String prefix = StrUtil.format("{}#{}:", platform.getPlatform(), apiKey);
//            var config = RedisVectorStore.RedisVectorStoreConfig.builder()
//                    .withIndexName(cacheKey)
//                    .withPrefix(prefix)
//                    .withMetadataFields(new RedisVectorStore.MetadataField("knowledgeId", Schema.FieldType.NUMERIC))
//                    .build();
//            RedisProperties redisProperties = SpringUtils.getBean(RedisProperties.class);
//            RedisVectorStore redisVectorStore = new RedisVectorStore(config, embeddingModel,
//                    new JedisPooled(redisProperties.getHost(), redisProperties.getPort()),
//                    true);
//            redisVectorStore.afterPropertiesSet();
//            return redisVectorStore;
        });
    }

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }

    // ========== 各种创建 spring-ai 客户端的方法 ==========

    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashscopeChatModel 方法
     */
    private static DashScopeChatModel buildTongYiChatModel(String key) {
        DashScopeApi dashScopeApi = new DashScopeApi(key);
        return new DashScopeChatModel(dashScopeApi);
    }

    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashScopeImageModel 方法
     */
    private static DashScopeImageModel buildTongYiImagesModel(String key) {
        DashScopeImageApi dashScopeImageApi = new DashScopeImageApi(key);
        return new DashScopeImageModel(dashScopeImageApi);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration} 的 qianFanChatModel 方法
     */
    private static QianFanChatModel buildYiYanChatModel(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        return new QianFanChatModel(qianFanApi);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration} 的 qianFanImageModel 方法
     */
    private QianFanImageModel buildQianFanImageModel(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        QianFanImageApi qianFanApi = new QianFanImageApi(appKey, secretKey);
        return new QianFanImageModel(qianFanApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#deepSeekChatModel(YudaoAiProperties)}
     */
    private static DeepSeekChatModel buildDeepSeekChatModel(String apiKey) {
        YudaoAiProperties.DeepSeekProperties properties = new YudaoAiProperties.DeepSeekProperties()
                .setApiKey(apiKey);
        return new YudaoAiAutoConfiguration().buildDeepSeekChatModel(properties);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#douBaoChatClient(YudaoAiProperties)}
     */
    private ChatModel buildDouBaoChatModel(String apiKey) {
        YudaoAiProperties.DouBaoProperties properties = new YudaoAiProperties.DouBaoProperties()
                .setApiKey(apiKey);
        return new YudaoAiAutoConfiguration().buildDouBaoChatClient(properties);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#hunYuanChatClient(YudaoAiProperties)}
     */
    private ChatModel buildHunYuanChatModel(String apiKey, String url) {
        YudaoAiProperties.HunYuanProperties properties = new YudaoAiProperties.HunYuanProperties()
                .setBaseUrl(url).setApiKey(apiKey);
        return new YudaoAiAutoConfiguration().buildHunYuanChatClient(properties);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#siliconFlowChatClient(YudaoAiProperties)}
     */
    private ChatModel buildSiliconFlowChatModel(String apiKey) {
        YudaoAiProperties.SiliconFlowProperties properties = new YudaoAiProperties.SiliconFlowProperties()
               .setApiKey(apiKey);
        return new YudaoAiAutoConfiguration().buildSiliconFlowChatClient(properties);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration} 的 zhiPuAiChatModel 方法
     */
    private ZhiPuAiChatModel buildZhiPuChatModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, ZhiPuAiConnectionProperties.DEFAULT_BASE_URL);
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(url, apiKey);
        return new ZhiPuAiChatModel(zhiPuAiApi);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration} 的 zhiPuAiImageModel 方法
     */
    private ZhiPuAiImageModel buildZhiPuAiImageModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, ZhiPuAiConnectionProperties.DEFAULT_BASE_URL);
        ZhiPuAiImageApi zhiPuAiApi = new ZhiPuAiImageApi(url, apiKey, RestClient.builder());
        return new ZhiPuAiImageModel(zhiPuAiApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#xingHuoChatClient(YudaoAiProperties)}
     */
    private static XingHuoChatModel buildXingHuoChatModel(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "XingHuoChatClient 的密钥需要 (appKey|secretKey) 格式");
        YudaoAiProperties.XingHuoProperties properties = new YudaoAiProperties.XingHuoProperties()
                .setAppKey(keys.get(0)).setSecretKey(keys.get(1));
        return new YudaoAiAutoConfiguration().buildXingHuoChatClient(properties);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration} 的 openAiChatModel 方法
     */
    private static OpenAiChatModel buildOpenAiChatModel(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(openAiToken).build();
        return OpenAiChatModel.builder().openAiApi(openAiApi).build();
    }

    // TODO @芋艿：手头暂时没密钥，使用建议再测试下
    /**
     * 可参考 {@link AzureOpenAiAutoConfiguration}
     */
    private static AzureOpenAiChatModel buildAzureOpenAiChatModel(String apiKey, String url) {
        AzureOpenAiAutoConfiguration azureOpenAiAutoConfiguration = new AzureOpenAiAutoConfiguration();
        // 创建 OpenAIClient 对象
        AzureOpenAiConnectionProperties connectionProperties = new AzureOpenAiConnectionProperties();
        connectionProperties.setApiKey(apiKey);
        connectionProperties.setEndpoint(url);
        OpenAIClientBuilder openAIClient = azureOpenAiAutoConfiguration.openAIClientBuilder(connectionProperties, null);
        // 获取 AzureOpenAiChatProperties 对象
        AzureOpenAiChatProperties chatProperties = SpringUtil.getBean(AzureOpenAiChatProperties.class);
        return azureOpenAiAutoConfiguration.azureOpenAiChatModel(openAIClient, chatProperties,
                null, null, null);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration} 的 openAiImageModel 方法
     */
    private OpenAiImageModel buildOpenAiImageModel(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiImageApi openAiApi = OpenAiImageApi.builder().baseUrl(url).apiKey(openAiToken).build();
        return new OpenAiImageModel(openAiApi);
    }

    /**
     * 可参考 {@link OllamaAutoConfiguration} 的 ollamaApi 方法
     */
    private static OllamaChatModel buildOllamaChatModel(String url) {
        OllamaApi ollamaApi = new OllamaApi(url);
        return OllamaChatModel.builder().ollamaApi(ollamaApi).build();
    }

    private StabilityAiImageModel buildStabilityAiImageModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, StabilityAiApi.DEFAULT_BASE_URL);
        StabilityAiApi stabilityAiApi = new StabilityAiApi(apiKey, StabilityAiApi.DEFAULT_IMAGE_MODEL, url);
        return new StabilityAiImageModel(stabilityAiApi);
    }

    // ========== 各种创建 EmbeddingModel 的方法 ==========

    // TODO @芋艿：需要测试下
    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashscopeEmbeddingModel 方法
     */
    private DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model) {
        DashScopeApi dashScopeApi = new DashScopeApi(apiKey);
        DashScopeEmbeddingOptions dashScopeEmbeddingOptions = DashScopeEmbeddingOptions.builder().withModel(model).build();
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, dashScopeEmbeddingOptions);
    }

    private OllamaEmbeddingModel buildOllamaEmbeddingModel(String url, String model) {
        OllamaApi ollamaApi = new OllamaApi(url);
        OllamaOptions ollamaOptions = OllamaOptions.builder().model(model).build();
        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).defaultOptions(ollamaOptions).build();
    }

}

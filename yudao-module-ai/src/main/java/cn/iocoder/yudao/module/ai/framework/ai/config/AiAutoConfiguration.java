package cn.iocoder.yudao.module.ai.framework.ai.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.grok.GrokChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.mimo.MiMoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax.MiniMaxChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.moonshot.MoonshotChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.stepfun.StepFunChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi.TongYiChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.yiyan.YiYanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.zhipu.ZhiPuChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.reasoning.AiRequestBodyPatcher;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.baidu.AiBaiduWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.bocha.AiBoChaWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.deepseek.AiDeepSeekWebSearchClient;
import cn.iocoder.yudao.module.ai.tool.function.DirectoryListToolFunction;
import cn.iocoder.yudao.module.ai.tool.function.UserProfileQueryToolFunction;
import cn.iocoder.yudao.module.ai.tool.function.WeatherQueryToolFunction;
import cn.iocoder.yudao.module.ai.tool.method.PersonService;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientProperties;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 芋道 AI 自动配置
 *
 * @author fansili
 */
@Configuration
@EnableConfigurationProperties({ YudaoAiProperties.class,
        QdrantVectorStoreProperties.class, // 解析 Qdrant 配置
        RedisVectorStoreProperties.class, // 解析 Redis 配置
        MilvusVectorStoreProperties.class, MilvusServiceClientProperties.class // 解析 Milvus 配置
})
@Slf4j
public class AiAutoConfiguration {

    @Bean
    public AiModelFactory aiModelFactory() {
        return new AiModelFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        // 特殊：兜底有 ObservationRegistry Bean，避免相关的 ChatModel 创建报错。相关 issue：https://t.zsxq.com/CuPu4
        return ObservationRegistry.NOOP;
    }

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.ai.dashscope.api-key")
    public TongYiChatModel tongYiChatModel(@Value("${spring.ai.dashscope.api-key}") String apiKey) {
        return buildTongYiChatModel(apiKey);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.ai.dashscope.api-key")
    public DashScopeImageModel dashScopeImageModel(@Value("${spring.ai.dashscope.api-key}") String apiKey,
                                                   ObservationRegistry observationRegistry) {
        return buildTongYiImagesModel(apiKey, observationRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.ai.dashscope.api-key")
    public DashScopeEmbeddingModel dashScopeEmbeddingModel(@Value("${spring.ai.dashscope.api-key}") String apiKey,
                                                           ObservationRegistry observationRegistry) {
        return buildTongYiEmbeddingModel(apiKey, null, observationRegistry);
    }

    /**
     * 创建通义千问的 ChatModel
     *
     * 特殊：spring-ai-alibaba-dashscope 2.0.0-M1.1 的 DashScopeChatModel 与 Spring AI 2.0.x 二进制不兼容，
     * 对话改走百炼的 OpenAI 兼容接口，参见 {@link TongYiChatModel}；图片、向量仍使用 DashScope 原生客户端
     */
    public static TongYiChatModel buildTongYiChatModel(String apiKey) {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(TongYiChatModel.BASE_URL)
                        .apiKey(apiKey)
                        .model(TongYiChatModel.MODEL_DEFAULT)
                        .temperature(0.7)
                        .build())
                .build();
        return new TongYiChatModel(openAiChatModel);
    }

    public static DashScopeImageModel buildTongYiImagesModel(String apiKey) {
        return buildTongYiImagesModel(apiKey, getObservationRegistry());
    }

    private static DashScopeImageModel buildTongYiImagesModel(String apiKey, ObservationRegistry observationRegistry) {
        DashScopeImageApi dashScopeImageApi = DashScopeImageApi.builder().apiKey(apiKey).build();
        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .model(DashScopeImageApi.DEFAULT_IMAGE_MODEL)
                .build();
        return new DashScopeImageModel(dashScopeImageApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE,
                observationRegistry);
    }

    public static DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model) {
        return buildTongYiEmbeddingModel(apiKey, model, getObservationRegistry());
    }

    private static DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model,
                                                                     ObservationRegistry observationRegistry) {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(apiKey).build();
        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder()
                .model(StrUtil.blankToDefault(model, DashScopeApi.DEFAULT_EMBEDDING_MODEL))
                .build();
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, options, RetryUtils.DEFAULT_RETRY_TEMPLATE,
                observationRegistry);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.bytedance.chat.enable", havingValue = "true")
    public DouBaoChatModel douBaoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getBytedance().getChat();
        return buildDouBaoChatClient(properties);
    }

    public DouBaoChatModel buildDouBaoChatClient(YudaoAiProperties.Chat properties) {
        return buildDouBaoChatClient(properties, null);
    }

    public DouBaoChatModel buildDouBaoChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(DouBaoChatModel.MODEL_DEFAULT);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(DouBaoChatModel.BASE_URL)
                        .completionsPath(DouBaoChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey()), patcher)
                        .build())
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.siliconflow.chat.enable", havingValue = "true")
    public SiliconFlowChatModel siliconFlowChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getSiliconflow().getChat();
        return buildSiliconFlowChatClient(properties);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(YudaoAiProperties.Chat properties) {
        return buildSiliconFlowChatClient(properties, null);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(YudaoAiProperties.Chat properties,
                                                           AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(SiliconFlowApiConstants.MODEL_DEFAULT);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                        .apiKey(properties.getApiKey()), patcher)
                        .build())
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new SiliconFlowChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.tencent.chat.enable", havingValue = "true")
    public HunYuanChatModel hunYuanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getTencent().getChat();
        return buildHunYuanChatClient(properties);
    }

    public HunYuanChatModel buildHunYuanChatClient(YudaoAiProperties.Chat properties) {
        return buildHunYuanChatClient(properties, null);
    }

    public HunYuanChatModel buildHunYuanChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(HunYuanChatModel.MODEL_DEFAULT);
        }
        if (StrUtil.isEmpty(properties.getBaseUrl())) {
            properties.setBaseUrl(HunYuanChatModel.BASE_URL);
        }
        // 创建 DeepSeekChatModel、HunYuanChatModel 对象
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(properties.getBaseUrl())
                        .completionsPath(HunYuanChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey()), patcher)
                        .build())
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new HunYuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.iflytek.chat.enable", havingValue = "true")
    public XingHuoChatModel xingHuoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getIflytek().getChat();
        return buildXingHuoChatClient(properties);
    }

    public XingHuoChatModel buildXingHuoChatClient(YudaoAiProperties.Chat properties) {
        return buildXingHuoChatClient(properties, null);
    }

    public XingHuoChatModel buildXingHuoChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(XingHuoChatModel.MODEL_DEFAULT);
        }
        return XingHuoChatModel.builder()
                .apiKey(properties.getApiKey())
                .patcher(patcher)
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.baichuan.chat.enable", havingValue = "true")
    public BaiChuanChatModel baiChuanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getBaichuan().getChat();
        return buildBaiChuanChatClient(properties);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(YudaoAiProperties.Chat properties) {
        return buildBaiChuanChatClient(properties, null);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(BaiChuanChatModel.MODEL_DEFAULT);
        }
        DeepSeekChatModel deepSeekChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(BaiChuanChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .completionsPath(BaiChuanChatModel.COMPLETE_PATH), patcher)
                        .build())
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new BaiChuanChatModel(deepSeekChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.baidu.chat.enable", havingValue = "true")
    public YiYanChatModel yiYanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getBaidu().getChat();
        return buildYiYanChatClient(properties);
    }

    public YiYanChatModel buildYiYanChatClient(YudaoAiProperties.Chat properties) {
        return buildYiYanChatClient(properties, null);
    }

    public YiYanChatModel buildYiYanChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(YiYanChatModel.MODEL_DEFAULT);
        }
        return new YiYanChatModel(buildDeepSeekCompatibleChatModel(
                StrUtil.blankToDefault(properties.getBaseUrl(), YiYanChatModel.BASE_URL),
                null, properties.getApiKey(), properties.getModel(), properties.getTemperature(),
                properties.getMaxTokens(), properties.getTopP(), patcher));
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.zhipu.chat.enable", havingValue = "true")
    public ZhiPuChatModel zhiPuChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getZhipu().getChat();
        return buildZhiPuChatClient(properties);
    }

    public ZhiPuChatModel buildZhiPuChatClient(YudaoAiProperties.Chat properties) {
        return buildZhiPuChatClient(properties, null);
    }

    public ZhiPuChatModel buildZhiPuChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(ZhiPuChatModel.MODEL_DEFAULT);
        }
        DeepSeekChatModel deepSeekChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                        .baseUrl(StrUtil.blankToDefault(properties.getBaseUrl(), ZhiPuChatModel.BASE_URL))
                        .apiKey(properties.getApiKey()), patcher)
                        .build())
                .options(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new ZhiPuChatModel(deepSeekChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.minimax.chat.enable", havingValue = "true")
    public MiniMaxChatModel miniMaxChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getMinimax().getChat();
        return buildMiniMaxChatClient(properties);
    }

    public MiniMaxChatModel buildMiniMaxChatClient(YudaoAiProperties.Chat properties) {
        return buildMiniMaxChatClient(properties, null);
    }

    public MiniMaxChatModel buildMiniMaxChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(MiniMaxChatModel.MODEL_DEFAULT);
        }
        return new MiniMaxChatModel(buildDeepSeekCompatibleChatModel(
                StrUtil.blankToDefault(properties.getBaseUrl(), MiniMaxChatModel.BASE_URL),
                null, properties.getApiKey(), properties.getModel(), properties.getTemperature(),
                properties.getMaxTokens(), properties.getTopP(), patcher));
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.moonshot.chat.enable", havingValue = "true")
    public MoonshotChatModel moonshotChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getMoonshot().getChat();
        return buildMoonshotChatClient(properties);
    }

    public MoonshotChatModel buildMoonshotChatClient(YudaoAiProperties.Chat properties) {
        return buildMoonshotChatClient(properties, null);
    }

    public MoonshotChatModel buildMoonshotChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(MoonshotChatModel.MODEL_DEFAULT);
        }
        return new MoonshotChatModel(buildDeepSeekCompatibleChatModel(
                StrUtil.blankToDefault(properties.getBaseUrl(), MoonshotChatModel.BASE_URL),
                MoonshotChatModel.COMPLETE_PATH, properties.getApiKey(), properties.getModel(),
                properties.getTemperature(), properties.getMaxTokens(), properties.getTopP(), patcher));
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.stepfun.chat.enable", havingValue = "true")
    public StepFunChatModel stepFunChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getStepfun().getChat();
        return buildStepFunChatClient(properties);
    }

    public StepFunChatModel buildStepFunChatClient(YudaoAiProperties.Chat properties) {
        return buildStepFunChatClient(properties, null);
    }

    public StepFunChatModel buildStepFunChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(StepFunChatModel.MODEL_DEFAULT);
        }
        return new StepFunChatModel(buildDeepSeekCompatibleChatModel(
                StrUtil.blankToDefault(properties.getBaseUrl(), StepFunChatModel.BASE_URL),
                StepFunChatModel.COMPLETE_PATH, properties.getApiKey(), properties.getModel(),
                properties.getTemperature(), properties.getMaxTokens(), properties.getTopP(), patcher));
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.xiaomi.chat.enable", havingValue = "true")
    public MiMoChatModel miMoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getXiaomi().getChat();
        return buildMiMoChatClient(properties);
    }

    public MiMoChatModel buildMiMoChatClient(YudaoAiProperties.Chat properties) {
        return buildMiMoChatClient(properties, null);
    }

    public MiMoChatModel buildMiMoChatClient(YudaoAiProperties.Chat properties, AiRequestBodyPatcher patcher) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(MiMoChatModel.MODEL_DEFAULT);
        }
        return new MiMoChatModel(buildDeepSeekCompatibleChatModel(
                StrUtil.blankToDefault(properties.getBaseUrl(), MiMoChatModel.BASE_URL),
                MiMoChatModel.COMPLETE_PATH, properties.getApiKey(), properties.getModel(),
                properties.getTemperature(), properties.getMaxTokens(), properties.getTopP(), patcher));
    }

    private static DeepSeekChatModel buildDeepSeekCompatibleChatModel(String baseUrl, String completionsPath,
                                                                     String apiKey, String model,
                                                                     Double temperature, Integer maxTokens,
                                                                     Double topP, AiRequestBodyPatcher patcher) {
        DeepSeekApi.Builder apiBuilder = DeepSeekApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey);
        if (StrUtil.isNotEmpty(completionsPath)) {
            apiBuilder.completionsPath(completionsPath);
        }
        AiRequestBodyPatcher.configure(apiBuilder, patcher);
        return DeepSeekChatModel.builder()
                .deepSeekApi(apiBuilder.build())
                .options(DeepSeekChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .topP(topP)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.image.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.MidjourneyImage config = yudaoAiProperties.getMidjourney().getImage();
        return new MidjourneyApi(config.getBaseUrl(), config.getApiKey(), config.getNotifyUrl());
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.grok.chat.enable", havingValue = "true")
    public GrokChatModel grokChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Chat properties = yudaoAiProperties.getGrok().getChat();
        return buildGrokChatClient(properties);
    }

    public GrokChatModel buildGrokChatClient(YudaoAiProperties.Chat properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GrokChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(buildOpenAiBaseUrl(StrUtil.blankToDefault(properties.getBaseUrl(), GrokChatModel.BASE_URL),
                                GrokChatModel.COMPLETE_PATH))
                        .apiKey(properties.getApiKey())
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .build();
        return new GrokChatModel(openAiChatModel);
    }

    /**
     * 获得 OpenAI 官方 SDK 的 baseUrl
     *
     * OpenAI 官方 SDK 的请求路径固定为 baseUrl + /chat/completions，所以由原 baseUrl + completionsPath 换算
     *
     * @param baseUrl         原 baseUrl
     * @param completionsPath 原 completionsPath，以 /chat/completions 结尾
     * @return baseUrl
     */
    private static String buildOpenAiBaseUrl(String baseUrl, String completionsPath) {
        return StrUtil.removeSuffix(StrUtil.removeSuffix(baseUrl, "/") + completionsPath, "/chat/completions");
    }

    // ========== RAG 相关 ==========

    @Bean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy();
    }

    private static ObservationRegistry getObservationRegistry() {
        return SpringUtil.getBean(ObservationRegistry.class);
    }

    // ========== Web Search 相关 ==========

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.web-search.enable", havingValue = "true")
    public AiWebSearchClient webSearchClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.WebSearch properties = yudaoAiProperties.getWebSearch();
        switch (properties.getPlatform()) {
            case BOCHA:
                return new AiBoChaWebSearchClient(properties.getApiKey());
            case BAIDU:
                return new AiBaiduWebSearchClient(properties.getApiKey());
            default:
                return new AiDeepSeekWebSearchClient(properties.getApiKey());
        }
    }

    // ========== MCP & 工具相关 ==========

    /**
     * 参考自 <a href="https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html">MCP Server Boot Starter</>
     */
    @Bean
    public List<ToolCallback> toolCallbacks(PersonService personService) {
        return List.of(ToolCallbacks.from(personService));
    }

    @Bean
    public ToolCallback directoryListToolCallback(DirectoryListToolFunction function) {
        return FunctionToolCallback.builder("directory_list", function)
                .description("列出指定目录的文件列表")
                .inputType(DirectoryListToolFunction.Request.class).build();
    }

    @Bean
    public ToolCallback weatherQueryToolCallback(WeatherQueryToolFunction function) {
        return FunctionToolCallback.builder("weather_query", function)
                .description("查询指定城市的天气信息")
                .inputType(WeatherQueryToolFunction.Request.class).build();
    }

    @Bean
    public ToolCallback userProfileQueryToolCallback(UserProfileQueryToolFunction function) {
        return FunctionToolCallback.builder("user_profile_query", function)
                .description("用户信息查询")
                .inputType(UserProfileQueryToolFunction.Request.class).build();
    }

}

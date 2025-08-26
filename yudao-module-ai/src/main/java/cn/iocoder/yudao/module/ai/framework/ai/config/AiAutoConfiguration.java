package cn.iocoder.yudao.module.ai.framework.ai.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.baichuan.BaiChuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.gemini.GeminiChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.hunyuan.HunYuanChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.bocha.AiBoChaWebSearchClient;
import cn.iocoder.yudao.module.ai.tool.method.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientProperties;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.gemini.enable", havingValue = "true")
    public GeminiChatModel geminiChatModel(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Gemini properties = yudaoAiProperties.getGemini();
        return buildGeminiChatClient(properties);
    }

    public GeminiChatModel buildGeminiChatClient(YudaoAiProperties.Gemini properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(GeminiChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(GeminiChatModel.BASE_URL)
                        .completionsPath(GeminiChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new GeminiChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.doubao.enable", havingValue = "true")
    public DouBaoChatModel douBaoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.DouBao properties = yudaoAiProperties.getDoubao();
        return buildDouBaoChatClient(properties);
    }

    public DouBaoChatModel buildDouBaoChatClient(YudaoAiProperties.DouBao properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(DouBaoChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(DouBaoChatModel.BASE_URL)
                        .completionsPath(DouBaoChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.siliconflow.enable", havingValue = "true")
    public SiliconFlowChatModel siliconFlowChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.SiliconFlow properties = yudaoAiProperties.getSiliconflow();
        return buildSiliconFlowChatClient(properties);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(YudaoAiProperties.SiliconFlow properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(SiliconFlowApiConstants.MODEL_DEFAULT);
        }
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new SiliconFlowChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.hunyuan.enable", havingValue = "true")
    public HunYuanChatModel hunYuanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.HunYuan properties = yudaoAiProperties.getHunyuan();
        return buildHunYuanChatClient(properties);
    }

    public HunYuanChatModel buildHunYuanChatClient(YudaoAiProperties.HunYuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(HunYuanChatModel.MODEL_DEFAULT);
        }
        // 特殊：由于混元大模型不提供 deepseek，而是通过知识引擎，所以需要区分下 URL
        if (StrUtil.isEmpty(properties.getBaseUrl())) {
            properties.setBaseUrl(
                    StrUtil.startWithIgnoreCase(properties.getModel(), "deepseek") ? HunYuanChatModel.DEEP_SEEK_BASE_URL
                            : HunYuanChatModel.BASE_URL);
        }
        // 创建 DeepSeekChatModel、HunYuanChatModel 对象
        DeepSeekChatModel openAiChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(properties.getBaseUrl())
                        .completionsPath(HunYuanChatModel.COMPLETE_PATH)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new HunYuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatModel xingHuoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.XingHuo properties = yudaoAiProperties.getXinghuo();
        return buildXingHuoChatClient(properties);
    }

    public XingHuoChatModel buildXingHuoChatClient(YudaoAiProperties.XingHuo properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(XingHuoChatModel.MODEL_DEFAULT);
        }
        OpenAiApi.Builder builder = OpenAiApi.builder()
                .baseUrl(XingHuoChatModel.BASE_URL_V1)
                .apiKey(properties.getAppKey() + ":" + properties.getSecretKey());
        if ("x1".equals(properties.getModel())) {
            builder.baseUrl(XingHuoChatModel.BASE_URL_V2)
                    .completionsPath(XingHuoChatModel.BASE_COMPLETIONS_PATH_V2);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(builder.build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                // TODO @芋艿：星火的 function call 有 bug，会报 ToolResponseMessage must have an id 错误！！！
                .toolCallingManager(getToolCallingManager())
                .build();
        return new XingHuoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.baichuan.enable", havingValue = "true")
    public BaiChuanChatModel baiChuanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.BaiChuan properties = yudaoAiProperties.getBaichuan();
        return buildBaiChuanChatClient(properties);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(YudaoAiProperties.BaiChuan properties) {
        if (StrUtil.isEmpty(properties.getModel())) {
            properties.setModel(BaiChuanChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(BaiChuanChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new BaiChuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.Midjourney config = yudaoAiProperties.getMidjourney();
        return new MidjourneyApi(config.getBaseUrl(), config.getApiKey(), config.getNotifyUrl());
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.suno.enable", havingValue = "true")
    public SunoApi sunoApi(YudaoAiProperties yudaoAiProperties) {
        return new SunoApi(yudaoAiProperties.getSuno().getBaseUrl());
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

    private static ToolCallingManager getToolCallingManager() {
        return SpringUtil.getBean(ToolCallingManager.class);
    }

    // ========== Web Search 相关 ==========

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.web-search.enable", havingValue = "true")
    public AiWebSearchClient webSearchClient(YudaoAiProperties yudaoAiProperties) {
        return new AiBoChaWebSearchClient(yudaoAiProperties.getWebSearch().getApiKey());
    }

    // ========== MCP 相关 ==========

    /**
     * 参考自 <a href="https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html">MCP Server Boot Starter</>
     */
    @Bean
    public List<ToolCallback> toolCallbacks(PersonService personService) {
        return List.of(ToolCallbacks.from(personService));
    }

}
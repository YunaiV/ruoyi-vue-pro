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
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import com.alibaba.cloud.ai.tongyi.TongYiAutoConfiguration;
import com.alibaba.cloud.ai.tongyi.TongYiConnectionProperties;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatModel;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatProperties;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesModel;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.azure.ai.openai.OpenAIClient;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiChatProperties;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiConnectionProperties;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanChatProperties;
import org.springframework.ai.autoconfigure.qianfan.QianFanConnectionProperties;
import org.springframework.ai.autoconfigure.qianfan.QianFanImageProperties;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiAutoConfiguration;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiChatProperties;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiConnectionProperties;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiImageProperties;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
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
                return SpringUtil.getBean(TongYiChatModel.class);
            case YI_YAN:
                return SpringUtil.getBean(QianFanChatModel.class);
            case DEEP_SEEK:
                return SpringUtil.getBean(DeepSeekChatModel.class);
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
                return SpringUtil.getBean(TongYiImagesModel.class);
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

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }

    // ========== 各种创建 spring-ai 客户端的方法 ==========

    /**
     * 可参考 {@link TongYiAutoConfiguration#tongYiChatClient(Generation, TongYiChatProperties, TongYiConnectionProperties)}
     */
    private static TongYiChatModel buildTongYiChatModel(String key) {
        com.alibaba.dashscope.aigc.generation.Generation generation = SpringUtil.getBean(Generation.class);
        TongYiChatProperties chatOptions = SpringUtil.getBean(TongYiChatProperties.class);
        // TODO @芋艿：貌似 apiKey 是全局唯一的？？？得测试下
        // TODO @芋艿：貌似阿里云不是增量返回的
        // 该 issue 进行跟进中 https://github.com/alibaba/spring-cloud-alibaba/issues/3790
        TongYiConnectionProperties connectionProperties = new TongYiConnectionProperties();
        connectionProperties.setApiKey(key);
        return new TongYiAutoConfiguration().tongYiChatClient(generation, chatOptions, connectionProperties);
    }

    private static TongYiImagesModel buildTongYiImagesModel(String key) {
        ImageSynthesis imageSynthesis = SpringUtil.getBean(ImageSynthesis.class);
        TongYiImagesProperties imagesOptions = SpringUtil.getBean(TongYiImagesProperties.class);
        TongYiConnectionProperties connectionProperties = new TongYiConnectionProperties();
        connectionProperties.setApiKey(key);
        return new TongYiAutoConfiguration().tongYiImagesClient(imageSynthesis, imagesOptions, connectionProperties);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration#qianFanChatModel(QianFanConnectionProperties, QianFanChatProperties, RestClient.Builder, RetryTemplate, ResponseErrorHandler)}
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
     * 可参考 {@link QianFanAutoConfiguration#qianFanImageModel(QianFanConnectionProperties, QianFanImageProperties, RestClient.Builder, RetryTemplate, ResponseErrorHandler)}
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
        return new DeepSeekChatModel(apiKey);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration#zhiPuAiChatModel(
     * ZhiPuAiConnectionProperties, ZhiPuAiChatProperties, RestClient.Builder, List, FunctionCallbackContext, RetryTemplate, ResponseErrorHandler)}
     */
    private ZhiPuAiChatModel buildZhiPuChatModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, ZhiPuAiConnectionProperties.DEFAULT_BASE_URL);
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(url, apiKey);
        return new ZhiPuAiChatModel(zhiPuAiApi);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration#zhiPuAiImageModel(
     * ZhiPuAiConnectionProperties, ZhiPuAiImageProperties, RestClient.Builder, RetryTemplate, ResponseErrorHandler)}
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
        Assert.equals(keys.size(), 3, "XingHuoChatClient 的密钥需要 (appid|appKey|secretKey) 格式");
        String appKey = keys.get(1);
        String secretKey = keys.get(2);
        return new XingHuoChatModel(appKey, secretKey);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration}
     */
    private static OpenAiChatModel buildOpenAiChatModel(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, ApiUtils.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = new OpenAiApi(url, openAiToken);
        return new OpenAiChatModel(openAiApi);
    }

    /**
     * 可参考 {@link AzureOpenAiAutoConfiguration}
     */
    private static AzureOpenAiChatModel buildAzureOpenAiChatModel(String apiKey, String url) {
        AzureOpenAiAutoConfiguration azureOpenAiAutoConfiguration = new AzureOpenAiAutoConfiguration();
        // 创建 OpenAIClient 对象
        AzureOpenAiConnectionProperties connectionProperties = new AzureOpenAiConnectionProperties();
        connectionProperties.setApiKey(apiKey);
        connectionProperties.setEndpoint(url);
        OpenAIClient openAIClient = azureOpenAiAutoConfiguration.openAIClient(connectionProperties);
        // 获取 AzureOpenAiChatProperties 对象
        AzureOpenAiChatProperties chatProperties = SpringUtil.getBean(AzureOpenAiChatProperties.class);
        return azureOpenAiAutoConfiguration.azureOpenAiChatModel(openAIClient, chatProperties, null, null);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration}
     */
    private OpenAiImageModel buildOpenAiImageModel(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, ApiUtils.DEFAULT_BASE_URL);
        OpenAiImageApi openAiApi = new OpenAiImageApi(url, openAiToken, RestClient.builder());
        return new OpenAiImageModel(openAiApi);
    }

    /**
     * 可参考 {@link OllamaAutoConfiguration}
     */
    private static OllamaChatModel buildOllamaChatModel(String url) {
        OllamaApi ollamaApi = new OllamaApi(url);
        return new OllamaChatModel(ollamaApi);
    }

    private StabilityAiImageModel buildStabilityAiImageModel(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, StabilityAiApi.DEFAULT_BASE_URL);
        StabilityAiApi stabilityAiApi = new StabilityAiApi(apiKey, StabilityAiApi.DEFAULT_IMAGE_MODEL, url);
        return new StabilityAiImageModel(stabilityAiApi);
    }

}

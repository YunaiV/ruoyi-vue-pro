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
import cn.iocoder.yudao.framework.ai.core.model.deepseek.DeepSeekChatClient;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
import com.alibaba.cloud.ai.tongyi.TongYiAutoConfiguration;
import com.alibaba.cloud.ai.tongyi.TongYiConnectionProperties;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatModel;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanChatProperties;
import org.springframework.ai.autoconfigure.qianfan.QianFanConnectionProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * AI 客户端工厂的实现类
 *
 * @author 芋道源码
 */
public class AiClientFactoryImpl implements AiClientFactory {

    @Override
    public ChatModel getOrCreateChatClient(AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(ChatModel.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<ChatModel>) () -> {
            //noinspection EnhancedSwitchMigration
            switch (platform) {
                case OPENAI:
                    return buildOpenAiChatClient(apiKey, url);
                case OLLAMA:
                    return buildOllamaChatClient(url);
                case YI_YAN:
                    return buildYiYanChatClient(apiKey);
                case XING_HUO:
                    return buildXingHuoChatClient(apiKey);
                case QIAN_WEN:
                    return buildQianWenChatClient(apiKey);
                case DEEP_SEEK:
                    return buildDeepSeekChatClient(apiKey);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public ChatModel getDefaultChatClient(AiPlatformEnum platform) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return SpringUtil.getBean(OpenAiChatModel.class);
            case OLLAMA:
                return SpringUtil.getBean(OllamaChatModel.class);
            case YI_YAN:
                return SpringUtil.getBean(QianFanChatModel.class);
            case XING_HUO:
                return SpringUtil.getBean(XingHuoChatClient.class);
            case QIAN_WEN:
                return SpringUtil.getBean(TongYiChatModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getDefaultImageClient(AiPlatformEnum platform) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return SpringUtil.getBean(OpenAiImageModel.class);
            case STABLE_DIFFUSION:
                return SpringUtil.getBean(StabilityAiImageModel.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageModel getOrCreateImageClient(AiPlatformEnum platform, String apiKey, String url) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return buildOpenAiImageClient(apiKey, url);
            case STABLE_DIFFUSION:
                return buildStabilityAiImageClient(apiKey, url);
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
     * 可参考 {@link OpenAiAutoConfiguration}
     */
    private static OpenAiChatModel buildOpenAiChatClient(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, ApiUtils.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = new OpenAiApi(url, openAiToken);
        return new OpenAiChatModel(openAiApi);
    }

    /**
     * 可参考 {@link OllamaAutoConfiguration}
     */
    private static OllamaChatModel buildOllamaChatClient(String url) {
        OllamaApi ollamaApi = new OllamaApi(url);
        return new OllamaChatModel(ollamaApi);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration#qianFanChatModel(QianFanConnectionProperties, QianFanChatProperties, RestClient.Builder, RetryTemplate, ResponseErrorHandler)}
     */
    private static QianFanChatModel buildYiYanChatClient(String key) {
        // TODO @xin：貌似目前设置，request 势必会报错；看看能不能有办法，参考 buildQianWenChatClient，调用 QianFanAutoConfiguration#qianFanChatModel初始化，当然 key 要用自己的哈
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        return new QianFanChatModel(qianFanApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#xingHuoChatClient(YudaoAiProperties)}
     */
    private static XingHuoChatClient buildXingHuoChatClient(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 3, "XingHuoChatClient 的密钥需要 (appid|appKey|secretKey) 格式");
        String appKey = keys.get(1);
        String secretKey = keys.get(2);
        return new XingHuoChatClient(appKey, secretKey);
    }

    private static DeepSeekChatClient buildDeepSeekChatClient(String apiKey) {
        return new DeepSeekChatClient(apiKey);
    }

    /**
     * 可参考 {@link TongYiAutoConfiguration#tongYiChatClient(Generation, TongYiChatProperties, TongYiConnectionProperties)}
     */
    private static TongYiChatModel buildQianWenChatClient(String key) {
        com.alibaba.dashscope.aigc.generation.Generation generation = SpringUtil.getBean(Generation.class);
        TongYiChatProperties chatOptions = SpringUtil.getBean(TongYiChatProperties.class);
        // TODO @xin：貌似 apiKey 是全局唯一的？？？得测试下
        // TODO @xin：貌似阿里云不是增量返回的
        TongYiConnectionProperties connectionProperties = new TongYiConnectionProperties();
        connectionProperties.setApiKey(key);
        return new TongYiAutoConfiguration().tongYiChatClient(generation, chatOptions, connectionProperties);
    }

    private OpenAiImageModel buildOpenAiImageClient(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, ApiUtils.DEFAULT_BASE_URL);
        OpenAiImageApi openAiApi = new OpenAiImageApi(url, openAiToken, RestClient.builder());
        return new OpenAiImageModel(openAiApi);
    }

    private StabilityAiImageModel buildStabilityAiImageClient(String apiKey, String url) {
        url = StrUtil.blankToDefault(url, StabilityAiApi.DEFAULT_BASE_URL);
        StabilityAiApi stabilityAiApi = new StabilityAiApi(apiKey, StabilityAiApi.DEFAULT_IMAGE_MODEL, url);
        return new StabilityAiImageModel(stabilityAiApi);
    }

}

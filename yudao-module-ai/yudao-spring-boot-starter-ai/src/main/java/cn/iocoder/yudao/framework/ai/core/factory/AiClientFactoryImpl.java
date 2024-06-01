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
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenChatClient;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenChatModal;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenOptions;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.api.QianWenApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoApi;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.YiYanChatClient;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.YiYanChatOptions;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanApi;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

/**
 * AI 客户端工厂的实现类
 *
 * @author 芋道源码
 */
public class AiClientFactoryImpl implements AiClientFactory {

    @Override
    public StreamingChatClient getOrCreateStreamingChatClient(AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(StreamingChatClient.class, platform, apiKey, url);
        return Singleton.get(cacheKey, (Func0<StreamingChatClient>) () -> {
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
//                case GEMIR:
//                    return buildGoogleGemir(apiKey);
                default:
                    throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
            }
        });
    }

    @Override
    public StreamingChatClient getDefaultStreamingChatClient(AiPlatformEnum platform) {
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return SpringUtil.getBean(OpenAiChatClient.class);
            case OLLAMA:
                return SpringUtil.getBean(OllamaChatClient.class);
            case YI_YAN:
                return SpringUtil.getBean(YiYanChatClient.class);
            case XING_HUO:
                return SpringUtil.getBean(XingHuoChatClient.class);
            case QIAN_WEN:
                return SpringUtil.getBean(QianWenChatClient.class);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    @Override
    public ImageClient getDefaultImageClient(AiPlatformEnum platform) {
        switch (platform) {
            case OPEN_AI_DALL:
                return SpringUtil.getBean(OpenAiImageClient.class);

        }
        return null;
    }

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtil.isEmpty(params)) {
            return clazz.getName();
        }
        return StrUtil.format("{}#{}", clazz.getName(), ArrayUtil.join(params, "_"));
    }

    @Override
    public ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens) {
        Float temperatureF = temperature != null ? temperature.floatValue() : null;
        //noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                return OpenAiChatOptions.builder().withModel(model).withTemperature(temperatureF).withMaxTokens(maxTokens).build();
            case OLLAMA:
                return OllamaOptions.create().withModel(model).withTemperature(temperatureF).withNumPredict(maxTokens);
            case YI_YAN:
                // TODO @fan：增加一个 model
                return new YiYanChatOptions().setTemperature(temperatureF).setMaxOutputTokens(maxTokens);
            case XING_HUO:
                return new XingHuoOptions().setChatModel(XingHuoChatModel.valueOfModel(model)).setTemperature(temperatureF)
                        .setMaxTokens(maxTokens);
            case QIAN_WEN:
                // TODO @fan:增加 model、temperature 参数
                return new QianWenOptions().setMaxTokens(maxTokens);
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    // ========== 各种创建 spring-ai 客户端的方法 ==========

    /**
     * 可参考 {@link OpenAiAutoConfiguration}
     */
    private static OpenAiChatClient buildOpenAiChatClient(String openAiToken, String url) {
        url = StrUtil.blankToDefault(url, ApiUtils.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = new OpenAiApi(url, openAiToken);
        return new OpenAiChatClient(openAiApi);
    }

    /**
     * 可参考 {@link OllamaAutoConfiguration}
     */
    private static OllamaChatClient buildOllamaChatClient(String url) {
        OllamaApi ollamaApi = new OllamaApi(url);
        return new OllamaChatClient(ollamaApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#yiYanChatClient(YudaoAiProperties)}
     */
    private static YiYanChatClient buildYiYanChatClient(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys.get(0);
        String secretKey = keys.get(1);
        YiYanApi yiYanApi = new YiYanApi(appKey, secretKey, YiYanApi.DEFAULT_CHAT_MODEL, 0);
        return new YiYanChatClient(yiYanApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#xingHuoChatClient(YudaoAiProperties)}
     */
    private static XingHuoChatClient buildXingHuoChatClient(String key) {
        List<String> keys = StrUtil.split(key, '|');
        Assert.equals(keys.size(), 3, "XingHuoChatClient 的密钥需要 (appid|appKey|secretKey) 格式");
        String appId = keys.get(0);
        String appKey = keys.get(1);
        String secretKey = keys.get(2);
        XingHuoApi xingHuoApi = new XingHuoApi(appId, appKey, secretKey);
        return new XingHuoChatClient(xingHuoApi);
    }

    /**
     * 可参考 {@link YudaoAiAutoConfiguration#qianWenChatClient(YudaoAiProperties)}
     */
    private static QianWenChatClient buildQianWenChatClient(String key) {
        QianWenApi qianWenApi = new QianWenApi(key, QianWenChatModal.QWEN_72B_CHAT);
        return new QianWenChatClient(qianWenApi);
    }


//    private static VertexAiGeminiChatClient buildGoogleGemir(String key) {
//        List<String> keys = StrUtil.split(key, '|');
//        Assert.equals(keys.size(), 2, "VertexAiGeminiChatClient 的密钥需要 (projectId|location) 格式");
//        VertexAI vertexApi =  new VertexAI(keys.get(0), keys.get(1));
//        return new VertexAiGeminiChatClient(vertexApi);
//    }

}

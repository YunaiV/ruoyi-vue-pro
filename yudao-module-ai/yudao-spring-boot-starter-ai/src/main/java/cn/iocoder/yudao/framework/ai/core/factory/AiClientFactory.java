package cn.iocoder.yudao.framework.ai.core.factory;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;

/**
 * AI 客户端工厂的接口类
 *
 * @author fansili
 */
public interface AiClientFactory {

    /**
     * 基于指定配置，获得 ChatModel 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL
     * @return ChatModel 对象
     */
    ChatModel getOrCreateChatClient(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 基于默认配置，获得 ChatModel 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return ChatModel 对象
     */
    ChatModel getDefaultChatClient(AiPlatformEnum platform);

    /**
     * 基于默认配置，获得 ImageClient 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return ImageClient 对象
     */
    ImageModel getDefaultImageClient(AiPlatformEnum platform);

    /**
     * 基于指定配置，获得 ImageClient 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL
     * @return ImageClient 对象
     */
    ImageModel getOrCreateImageClient(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 基于指定配置，获得 MidjourneyApi 对象
     *
     * 如果不存在，则进行创建
     *
     * @param apiKey API KEY
     * @param url API URL
     * @return MidjourneyApi 对象
     */
    MidjourneyApi getOrCreateMidjourneyApi(String apiKey, String url);

    /**
     * 基于指定配置，获得 SunoApi 对象
     *
     * 如果不存在，则进行创建
     *
     * @param apiKey API KEY
     * @param url API URL
     * @return SunoApi 对象
     */
    SunoApi getOrCreateSunoApi(String apiKey, String url);

}

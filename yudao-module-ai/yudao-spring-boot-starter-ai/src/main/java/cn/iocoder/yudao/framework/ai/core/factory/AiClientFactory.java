package cn.iocoder.yudao.framework.ai.core.factory;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.image.ImageClient;

/**
 * AI 客户端工厂的接口类
 *
 * @author fansili
 */
public interface AiClientFactory {

    /**
     * 基于指定配置，获得 StreamingChatClient 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL
     * @return StreamingChatClient 对象
     */
    StreamingChatClient getOrCreateStreamingChatClient(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 基于默认配置，获得 StreamingChatClient 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return StreamingChatClient 对象
     */
    StreamingChatClient getDefaultStreamingChatClient(AiPlatformEnum platform);

    /**
     * 基于默认配置，获得 ImageClient 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return ImageClient 对象
     */
    ImageClient getDefaultImageClient(AiPlatformEnum platform);

    /**
     * 创建 Chat 参数
     *
     * @param platform 平台
     * @param model 模型
     * @param temperature 温度
     * @param maxTokens 生成的最大 Token
     * @return Chat 参数
     */
    ChatOptions buildChatOptions(AiPlatformEnum platform, String model, Double temperature, Integer maxTokens);

}

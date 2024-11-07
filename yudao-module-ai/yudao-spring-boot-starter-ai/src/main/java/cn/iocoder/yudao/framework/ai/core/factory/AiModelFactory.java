package cn.iocoder.yudao.framework.ai.core.factory;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * AI Model 模型工厂的接口类
 *
 * @author fansili
 */
public interface AiModelFactory {

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
    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 基于默认配置，获得 ChatModel 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return ChatModel 对象
     */
    ChatModel getDefaultChatModel(AiPlatformEnum platform);

    /**
     * 基于默认配置，获得 ImageModel 对象
     *
     * 默认配置，指的是在 application.yaml 配置文件中的 spring.ai 相关的配置
     *
     * @param platform 平台
     * @return ImageModel 对象
     */
    ImageModel getDefaultImageModel(AiPlatformEnum platform);

    /**
     * 基于指定配置，获得 ImageModel 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL
     * @return ImageModel 对象
     */
    ImageModel getOrCreateImageModel(AiPlatformEnum platform, String apiKey, String url);

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

    /**
     * 基于指定配置，获得 EmbeddingModel 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey   API KEY
     * @param url      API URL
     * @return ChatModel 对象
     */
    EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url);

    /**
     * 基于指定配置，获得 VectorStore 对象
     * <p>
     * 如果不存在，则进行创建
     *
     * @param embeddingModel 嵌入模型
     * @param platform       平台
     * @param apiKey         API KEY
     * @param url            API URL
     * @return VectorStore 对象
     */
    VectorStore getOrCreateVectorStore(EmbeddingModel embeddingModel, AiPlatformEnum platform, String apiKey, String url);

}

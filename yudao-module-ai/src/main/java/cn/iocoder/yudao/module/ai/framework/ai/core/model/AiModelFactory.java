package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import com.volcengine.ark.runtime.service.ArkService;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Map;

/**
 * AI Model 模型工厂的接口类
 *
 * @author fansili
 */
public interface AiModelFactory {

    /**
     * 基于指定配置和思考参数，获得 ChatModel 对象
     *
     * 如果不存在，则进行创建。思考参数为 null、或平台不走 HTTP 层补丁时，按平台、API KEY、URL 缓存；
     * 否则参数通过 AiRequestBodyPatcher 绑定在客户端实例的 HTTP 层，缓存键追加参数的规范化哈希
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL
     * @param reasoningOptions 思考参数，可为 null
     * @return ChatModel 对象
     */
    ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url, Map<String, Object> reasoningOptions);

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
     * @param model 模型标识（例如说：Stability AI 需要拼接到请求路径中）
     * @return ImageModel 对象
     */
    ImageModel getOrCreateImageModel(AiPlatformEnum platform, String apiKey, String url, String model);

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
     * 基于指定配置，获得火山方舟 ArkService 对象（豆包 Seedream 绘图）
     *
     * @param apiKey API Key
     * @param url API URL，允许空
     * @return ArkService 对象
     */
    ArkService getOrCreateArkService(String apiKey, String url);

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
     * 基于指定配置，获得语音合成的 AiSpeechModel 对象：阿里云（通义）、MiniMax、字节跳动（豆包）、百度、腾讯云
     *
     * 如果不存在，则进行创建。模型在创建时绑定，请求不能切换
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL，允许空（使用平台默认地址）
     * @param model 模型标识
     * @return AiSpeechModel 对象
     */
    AiSpeechModel getOrCreateTextToSpeechModel(AiPlatformEnum platform, String apiKey, String url, String model);

    /**
     * 基于指定配置，获得腾讯云语音合成的 TencentTtsApi 对象：按任务编号查询未在时限内完成的任务
     *
     * 如果不存在，则进行创建
     *
     * @param apiKey 凭证，格式为 SecretId|SecretKey
     * @param url API URL，允许空
     * @return TencentTtsApi 对象
     */
    TencentTtsApi getOrCreateTencentTtsApi(String apiKey, String url);

    /**
     * 基于指定配置，获得 OcrModel 对象：通义、硅基流动、腾讯云、百度、火山引擎
     *
     * 如果不存在，则进行创建。按平台、API KEY、URL、模型标识缓存，配置变更后不会复用旧凭证
     *
     * @param platform 平台
     * @param apiKey API KEY
     * @param url API URL，允许空（使用平台默认地址）
     * @param model 模型标识或服务代码
     * @return OcrModel 对象
     */
    OcrModel getOrCreateOcrModel(AiPlatformEnum platform, String apiKey, String url, String model);

    /**
     * 基于指定配置，获得 EmbeddingModel 对象
     *
     * 如果不存在，则进行创建
     *
     * @param platform 平台
     * @param apiKey   API KEY
     * @param url      API URL
     * @param model     模型
     * @return ChatModel 对象
     */
    EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model);

    /**
     * 基于指定配置，获得 VectorStore 对象
     *
     * 如果不存在，则进行创建
     *
     * @param type           向量存储类型
     * @param embeddingModel 向量模型
     * @param metadataFields 元数据字段
     * @return VectorStore 对象
     */
    VectorStore getOrCreateVectorStore(Class<? extends VectorStore> type,
                                       EmbeddingModel embeddingModel,
                                       Map<String, Class<?>> metadataFields);

}

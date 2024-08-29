package cn.iocoder.yudao.framework.ai.core.factory;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * AI Vector 模型工厂的接口类
 * @author xiaoxin
 */
public interface AiVectorStoreFactory {


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

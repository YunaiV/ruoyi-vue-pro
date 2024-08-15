package cn.iocoder.yudao.module.ai.service.knowledge;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * AI 嵌入 Service 接口
 *
 * @author xiaoxin
 */
public interface AiEmbeddingService {

    /**
     * 向量化文档
     */
    void embeddingDoc();


    /**
     * 相似查询
     *
     * @param content 查询内容
     */
    List<Document> similaritySearch(String content);
}

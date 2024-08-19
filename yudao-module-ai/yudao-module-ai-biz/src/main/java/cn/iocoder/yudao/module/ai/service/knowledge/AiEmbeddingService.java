package cn.iocoder.yudao.module.ai.service.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

/**
 * AI 嵌入 Service 接口
 *
 * @author xiaoxin
 */
public interface AiEmbeddingService {

    /**
     * 向量化文档并存储
     */
    void add(List<Document> documents);

    /**
     * 相似查询
     *
     * @param request 查询实体
     */
    List<Document> similaritySearch(SearchRequest request);

}

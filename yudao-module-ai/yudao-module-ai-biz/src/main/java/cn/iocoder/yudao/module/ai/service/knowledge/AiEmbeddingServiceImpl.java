package cn.iocoder.yudao.module.ai.service.knowledge;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 嵌入 Service 实现类
 *
 * @author xiaoxin
 */
@Service
public class AiEmbeddingServiceImpl implements AiEmbeddingService {

    @Resource
    private RedisVectorStore vectorStore;

    @Override
    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }
}

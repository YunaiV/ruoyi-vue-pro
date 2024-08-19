package cn.iocoder.yudao.module.ai.service.knowledge;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO @xin：是不是不用 AiEmbeddingServiceImpl，直接 vectorStore 注入到需要的地方就好啦。通过 KnowledgeDocumentService 返回就好。
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
//    @Async
    // TODO xiaoxin 报错先注释
    public void add(List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }

}

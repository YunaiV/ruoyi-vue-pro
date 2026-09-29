package cn.iocoder.yudao.module.ai1.harness.rag;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.loader.LazyFunLoader;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeSearchRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.enums.model.Ai1ModelTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ProviderService;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ProviderRuntime;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.DropCollectionParam;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MODEL_TYPE_NOT_EMBEDDING;

/**
 * AI1 RAG 工具（Spring AI + Milvus）
 *
 * 知识库的向量化、检索、清理与 Advisor 装配：
 * 1. 向量化：分片 → Document（含 documentId、chunkIndex 元数据）→ VectorStore.add（先清理旧向量）
 * 2. 检索：VectorStore.similaritySearch
 * 3. 清理：按 documentId 元数据删除向量；知识库删除时 drop 整个集合
 * 4. Advisor：为对话装配 QuestionAnswerAdvisor，检索上下文自动注入
 *
 * 集合名为 {collectionPrefix}{知识库编号}（编号全局唯一，无需租户前缀）；调用方必须先在当前租户下查出知识库，再传入本工具，
 * 以此保证不会越权访问其他租户的集合
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1RagTool {

    /**
     * 向量存储缓存上限
     */
    private static final int CACHE_MAX = 128;
    /**
     * 向量元数据：文档编号
     */
    private static final String METADATA_DOCUMENT_ID = "documentId";
    /**
     * 向量元数据：分片序号
     */
    private static final String METADATA_CHUNK_INDEX = "chunkIndex";

    @Resource
    private YudaoAi1Properties ai1Properties;

    @Resource
    private Ai1ProviderService providerService;

    @Resource
    private Ai1LlmModelFactory llmModelFactory;

    /**
     * 向量存储缓存：知识库编号 → 向量存储；按访问顺序淘汰，嵌入模型实例变化（配置变更后重建）时自动重建
     */
    private final Cache<Long, CachedVectorStore> vectorStoreCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX).build();
    /**
     * Milvus 客户端：首次使用时创建，全局复用一个连接
     */
    private final LazyFunLoader<MilvusServiceClient> milvusClientLoader = LazyFunLoader.on(this::createMilvusClient);

    /**
     * 文档向量化：分片 → 嵌入 → 写入 Milvus；先清理旧向量再写入，支持重复向量化
     *
     * @param knowledgeBase 知识库（需已配置嵌入模型）
     * @param documentId    文档编号
     * @param content       文档内容
     * @return 分片数量
     */
    public int vectorize(Ai1KnowledgeBaseDO knowledgeBase, Long documentId, String content) {
        // 1. 分片
        List<String> chunks = split(content, knowledgeBase.getChunkSize(), knowledgeBase.getChunkOverlap());
        if (CollUtil.isEmpty(chunks)) {
            throw new IllegalArgumentException("文档无可分片内容");
        }

        // 2. 清理旧向量后写入
        MilvusVectorStore vectorStore = getOrCreateVectorStore(knowledgeBase);
        vectorStore.delete(new FilterExpressionBuilder().eq(METADATA_DOCUMENT_ID, documentId).build());
        List<Document> documents = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put(METADATA_DOCUMENT_ID, documentId);
            metadata.put(METADATA_CHUNK_INDEX, i);
            documents.add(new Document(chunks.get(i), metadata));
        }
        vectorStore.add(documents);
        return chunks.size();
    }

    /**
     * 向量检索：按查询文本召回知识库相关分片
     *
     * @param knowledgeBase 知识库
     * @param query         查询文本
     * @param topK          检索数量，小于等于 0 时使用知识库配置
     * @return 命中分片；文档名称由调用方按文档编号补充
     */
    public List<Ai1KnowledgeSearchRespVO> search(Ai1KnowledgeBaseDO knowledgeBase, String query, Integer topK) {
        // 1. 相似度检索：未指定检索数量时，使用知识库配置
        MilvusVectorStore vectorStore = getOrCreateVectorStore(knowledgeBase);
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query).topK(topK != null && topK > 0 ? topK : getDefaultTopK(knowledgeBase)).build());

        // 2. 转换为命中分片：从向量元数据中还原文档编号、分片序号
        return convertList(documents, document -> new Ai1KnowledgeSearchRespVO().setText(document.getText())
                .setDocumentId(Convert.toLong(document.getMetadata().get(METADATA_DOCUMENT_ID)))
                .setChunkIndex(Convert.toInt(document.getMetadata().get(METADATA_CHUNK_INDEX)))
                .setScore(ObjUtil.defaultIfNull(document.getScore(), 0D)));
    }

    /**
     * 按文档编号清理向量（文档删除、内容变更时调用）；失败时仅记录日志
     *
     * @param knowledgeBase 知识库
     * @param documentIds   文档编号集合
     */
    public void deleteByDocumentIds(Ai1KnowledgeBaseDO knowledgeBase, Collection<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return;
        }
        try {
            getOrCreateVectorStore(knowledgeBase).delete(new FilterExpressionBuilder()
                    .in(METADATA_DOCUMENT_ID, documentIds.toArray()).build());
        } catch (Exception e) {
            log.warn("[deleteByDocumentIds][知识库({}) 文档({}) 向量清理失败]", knowledgeBase.getId(), documentIds, e);
        }
    }

    /**
     * 删除知识库的整个向量集合（知识库删除时调用，避免遗留孤儿集合）；失败时仅记录日志
     *
     * @param knowledgeBaseId 知识库编号
     */
    public void dropCollection(Long knowledgeBaseId) {
        evict(knowledgeBaseId);
        try {
            R<RpcStatus> result = milvusClientLoader.get().dropCollection(DropCollectionParam.newBuilder()
                    .withDatabaseName(ai1Properties.getMilvus().getDatabase())
                    .withCollectionName(buildCollectionName(knowledgeBaseId))
                    .build());
            if (result.getStatus() != R.Status.Success.getCode()) {
                log.warn("[dropCollection][知识库({}) 向量集合删除失败：{}]", knowledgeBaseId, result.getMessage());
            }
        } catch (Exception e) {
            log.warn("[dropCollection][知识库({}) 向量集合删除异常]", knowledgeBaseId, e);
        }
    }

    /**
     * 为知识库构建 RAG Advisor：检索上下文自动注入对话
     *
     * @param knowledgeBase 知识库
     * @return RAG Advisor
     */
    public Advisor buildAdvisor(Ai1KnowledgeBaseDO knowledgeBase) {
        MilvusVectorStore vectorStore = getOrCreateVectorStore(knowledgeBase);
        SearchRequest searchRequest = SearchRequest.builder().topK(getDefaultTopK(knowledgeBase)).build();
        return QuestionAnswerAdvisor.builder(vectorStore).searchRequest(searchRequest).build();
    }

    /**
     * 失效知识库的向量存储缓存
     *
     * 只作用于当前节点；其他节点在嵌入模型变更后，会因嵌入模型实例不一致自然重建
     *
     * @param knowledgeBaseId 知识库编号
     */
    public void evict(Long knowledgeBaseId) {
        vectorStoreCache.invalidate(knowledgeBaseId);
    }

    @PreDestroy
    public void destroy() {
        milvusClientLoader.ifInitialized(MilvusServiceClient::close);
    }

    // ==================== 内部实现：嵌入模型 / 向量存储 / 分片 ====================

    /**
     * 获取（或构建）知识库向量存储：嵌入模型实例不变时复用；Provider、模型配置变更后模型工厂会重建实例，此处随之重建
     */
    private MilvusVectorStore getOrCreateVectorStore(Ai1KnowledgeBaseDO knowledgeBase) {
        // 1. 缓存命中、且嵌入模型实例未变化时，直接复用
        EmbeddingModel embeddingModel = getEmbeddingModel(knowledgeBase);
        CachedVectorStore cached = vectorStoreCache.getIfPresent(knowledgeBase.getId());
        if (cached != null && cached.getEmbeddingModel() == embeddingModel) {
            return cached.getVectorStore();
        }

        // TODO DONE @AI：方法内注释；
        // 2.1 构建向量存储：集合不存在时自动创建
        String collectionName = buildCollectionName(knowledgeBase.getId());
        MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusClientLoader.get(), embeddingModel)
                .databaseName(ai1Properties.getMilvus().getDatabase())
                .collectionName(collectionName)
                .metricType(MetricType.COSINE)
                .indexType(IndexType.FLAT)
                .initializeSchema(true)
                .build();
        // 2.2 手动构建的实例不会触发 Spring 生命周期回调，需主动初始化：建集合、建索引、加载
        try {
            vectorStore.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException("向量集合(" + collectionName + ") 初始化失败：" + e.getMessage(), e);
        }
        // 2.3 放入缓存
        vectorStoreCache.put(knowledgeBase.getId(), new CachedVectorStore()
                .setEmbeddingModel(embeddingModel).setVectorStore(vectorStore));
        log.debug("[getOrCreateVectorStore][知识库({}) 向量存储构建完成]", knowledgeBase.getId());
        return vectorStore;
    }

    /**
     * 解析知识库的嵌入模型：Provider、模型不存在或被禁用时抛出业务异常
     */
    private EmbeddingModel getEmbeddingModel(Ai1KnowledgeBaseDO knowledgeBase) {
        Ai1ProviderRuntime runtime = providerService.getProviderRuntime(
                knowledgeBase.getEmbeddingProviderId(), knowledgeBase.getEmbeddingModelId());
        if (!Ai1ModelTypeEnum.isEmbedding(runtime.getModelType())) {
            throw exception(MODEL_TYPE_NOT_EMBEDDING);
        }
        return llmModelFactory.getOrCreateEmbeddingModel(runtime);
    }

    /**
     * 创建 Milvus 客户端：配置了用户名时，开启鉴权
     */
    private MilvusServiceClient createMilvusClient() {
        YudaoAi1Properties.Milvus milvus = ai1Properties.getMilvus();
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withUri(milvus.getUri())
                .withDatabaseName(milvus.getDatabase());
        if (StrUtil.isNotBlank(milvus.getUsername())) {
            builder.withAuthorization(milvus.getUsername(), milvus.getPassword());
        }
        return new MilvusServiceClient(builder.build());
    }

    /**
     * 构建知识库的向量集合名：一个知识库对应一个集合
     *
     * 不同知识库可能使用不同的嵌入模型，向量维度不同，无法共用一个集合；后续也可能落到不同的向量库，按知识库隔离更便于迁移和整体删除
     */
    private String buildCollectionName(Long knowledgeBaseId) {
        return ai1Properties.getMilvus().getCollectionPrefix() + knowledgeBaseId;
    }

    /**
     * 获得知识库的检索数量：知识库未配置时，使用 yudao.ai1.milvus.default-top-k 配置
     */
    private int getDefaultTopK(Ai1KnowledgeBaseDO knowledgeBase) {
        return knowledgeBase.getTopK() != null && knowledgeBase.getTopK() > 0 ? knowledgeBase.getTopK()
                : ai1Properties.getMilvus().getDefaultTopK();
    }

    /**
     * 文本分片：按 chunkSize、chunkOverlap 做字符级分片
     *
     * @param content      文本
     * @param chunkSize    分片大小
     * @param chunkOverlap 分片重叠，限制在 [0, chunkSize - 1]
     * @return 分片列表
     */
    @SuppressWarnings("MathClampMigration")
    static List<String> split(String content, Integer chunkSize, Integer chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        if (StrUtil.isEmpty(content)) {
            return chunks;
        }
        int size = Math.max(ObjUtil.defaultIfNull(chunkSize, 0), 1);
        int overlap = Math.min(Math.max(ObjUtil.defaultIfNull(chunkOverlap, 0), 0), size - 1);
        int length = content.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + size, length);
            chunks.add(content.substring(start, end));
            if (end >= length) {
                break;
            }
            start = end - overlap;
        }
        return chunks;
    }

    /**
     * 向量存储缓存项
     */
    @Data
    private static class CachedVectorStore {

        /**
         * 构建时使用的嵌入模型实例，用于判断是否需要重建
         */
        private EmbeddingModel embeddingModel;
        /**
         * 向量存储
         */
        private MilvusVectorStore vectorStore;

    }

}

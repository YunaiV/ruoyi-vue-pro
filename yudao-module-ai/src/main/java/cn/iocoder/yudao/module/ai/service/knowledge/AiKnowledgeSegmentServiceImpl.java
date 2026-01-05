package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentProcessRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeSegmentMapper;
import cn.iocoder.yudao.module.ai.enums.AiDocumentSplitStrategyEnum;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import cn.iocoder.yudao.module.ai.service.knowledge.splitter.MarkdownQaSplitter;
import cn.iocoder.yudao.module.ai.service.knowledge.splitter.SemanticTextSplitter;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.springframework.ai.vectorstore.SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL;

/**
 * AI 知识库分片 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeSegmentServiceImpl implements AiKnowledgeSegmentService {

    private static final String VECTOR_STORE_METADATA_KNOWLEDGE_ID = "knowledgeId";
    private static final String VECTOR_STORE_METADATA_DOCUMENT_ID = "documentId";
    private static final String VECTOR_STORE_METADATA_SEGMENT_ID = "segmentId";

    private static final Map<String, Class<?>> VECTOR_STORE_METADATA_TYPES = Map.of(
            VECTOR_STORE_METADATA_KNOWLEDGE_ID, String.class,
            VECTOR_STORE_METADATA_DOCUMENT_ID, String.class,
            VECTOR_STORE_METADATA_SEGMENT_ID, String.class);

    /**
     * Rerank 在向量检索时，检索数量 * 该系数，目的是为了提升 Rerank 的效果
     */
    private static final Integer RERANK_RETRIEVAL_FACTOR = 4;

    @Resource
    private AiKnowledgeSegmentMapper segmentMapper;

    @Resource
    private AiKnowledgeService knowledgeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiKnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private AiModelService modelService;

    @Resource
    private TokenCountEstimator tokenCountEstimator;

    @Autowired(required = false) // 由于 spring.ai.model.rerank 配置项，可以关闭 RerankModel 的功能，所以这里只能不强制注入
    private RerankModel rerankModel;

    @Override
    public PageResult<AiKnowledgeSegmentDO> getKnowledgeSegmentPage(AiKnowledgeSegmentPageReqVO pageReqVO) {
        return segmentMapper.selectPage(pageReqVO);
    }

    @Override
    public void createKnowledgeSegmentBySplitContent(Long documentId, String content) {
        // 1. 校验
        AiKnowledgeDocumentDO documentDO = knowledgeDocumentService.validateKnowledgeDocumentExists(documentId);
        AiKnowledgeDO knowledgeDO = knowledgeService.validateKnowledgeExists(documentDO.getKnowledgeId());
        VectorStore vectorStore = getVectorStoreById(knowledgeDO);

        // 2. 文档切片（使用自动检测策略）
        List<Document> documentSegments = splitContentByStrategy(content, documentDO.getSegmentMaxTokens(),
                AiDocumentSplitStrategyEnum.AUTO, documentDO.getUrl());

        // 3.1 存储切片
        List<AiKnowledgeSegmentDO> segmentDOs = convertList(documentSegments, segment -> {
            if (StrUtil.isEmpty(segment.getText())) {
                return null;
            }
            return new AiKnowledgeSegmentDO().setKnowledgeId(documentDO.getKnowledgeId()).setDocumentId(documentId)
                    .setContent(segment.getText()).setContentLength(segment.getText().length())
                    .setVectorId(AiKnowledgeSegmentDO.VECTOR_ID_EMPTY)
                    .setTokens(tokenCountEstimator.estimate(segment.getText()))
                    .setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        segmentMapper.insertBatch(segmentDOs);
        // 3.2 切片向量化
        for (int i = 0; i < documentSegments.size(); i++) {
            Document segment = documentSegments.get(i);
            AiKnowledgeSegmentDO segmentDO = segmentDOs.get(i);
            writeVectorStore(vectorStore, segmentDO, segment);
        }
    }

    @Override
    public void updateKnowledgeSegment(AiKnowledgeSegmentSaveReqVO reqVO) {
        // 1. 校验
        AiKnowledgeSegmentDO oldSegment = validateKnowledgeSegmentExists(reqVO.getId());

        // 2. 删除向量
        VectorStore vectorStore = getVectorStoreById(oldSegment.getKnowledgeId());
        deleteVectorStore(vectorStore, oldSegment);

        // 3.1 更新切片
        AiKnowledgeSegmentDO newSegment = BeanUtils.toBean(reqVO, AiKnowledgeSegmentDO.class);
        segmentMapper.updateById(newSegment);
        // 3.2 重新向量化，必须开启状态
        if (CommonStatusEnum.isEnable(oldSegment.getStatus())) {
            newSegment.setKnowledgeId(oldSegment.getKnowledgeId()).setDocumentId(oldSegment.getDocumentId());
            writeVectorStore(vectorStore, newSegment, new Document(newSegment.getContent()));
        }
    }

    @Override
    public void deleteKnowledgeSegmentByDocumentId(Long documentId) {
        // 1. 查询需要删除的段落
        List<AiKnowledgeSegmentDO> segments = segmentMapper.selectListByDocumentId(documentId);
        if (CollUtil.isEmpty(segments)) {
            return;
        }

        // 2. 批量删除段落记录
        segmentMapper.deleteByIds(convertList(segments, AiKnowledgeSegmentDO::getId));

        // 3. 删除向量存储中的段落
        VectorStore vectorStore = getVectorStoreById(segments.get(0).getKnowledgeId());
        vectorStore.delete(convertList(segments, AiKnowledgeSegmentDO::getVectorId));
    }

    @Override
    public void updateKnowledgeSegmentStatus(AiKnowledgeSegmentUpdateStatusReqVO reqVO) {
        // 1. 校验
        AiKnowledgeSegmentDO segment = validateKnowledgeSegmentExists(reqVO.getId());

        // 2. 获取知识库向量实例
        VectorStore vectorStore = getVectorStoreById(segment.getKnowledgeId());

        // 3. 更新状态
        segmentMapper.updateById(new AiKnowledgeSegmentDO().setId(reqVO.getId()).setStatus(reqVO.getStatus()));

        // 4. 更新向量
        if (CommonStatusEnum.isEnable(reqVO.getStatus())) {
            writeVectorStore(vectorStore, segment, new Document(segment.getContent()));
        } else {
            deleteVectorStore(vectorStore, segment);
        }
    }

    @Override
    public void reindexKnowledgeSegmentByKnowledgeId(Long knowledgeId) {
        // 1.1 校验知识库存在
        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(knowledgeId);
        // 1.2 获取知识库向量实例
        VectorStore vectorStore = getVectorStoreById(knowledge);

        // 2.1 查询知识库下的所有启用状态的段落
        List<AiKnowledgeSegmentDO> segments = segmentMapper.selectListByKnowledgeIdAndStatus(
                knowledgeId, CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(segments)) {
            return;
        }
        // 2.2 遍历所有段落，重新索引
        for (AiKnowledgeSegmentDO segment : segments) {
            // 删除旧的向量
            deleteVectorStore(vectorStore, segment);
            // 重新创建向量
            writeVectorStore(vectorStore, segment, new Document(segment.getContent()));
        }
        log.info("[reindexKnowledgeSegmentByKnowledgeId][知识库({}) 重新索引完成，共处理 {} 个段落]",
                knowledgeId, segments.size());
    }

    private void writeVectorStore(VectorStore vectorStore, AiKnowledgeSegmentDO segmentDO, Document segment) {
        // 1. 向量存储
        // 为什么要 toString 呢？因为部分 VectorStore 实现，不支持 Long 类型，例如说 QdrantVectorStore
        segment.getMetadata().put(VECTOR_STORE_METADATA_KNOWLEDGE_ID, segmentDO.getKnowledgeId().toString());
        segment.getMetadata().put(VECTOR_STORE_METADATA_DOCUMENT_ID, segmentDO.getDocumentId().toString());
        segment.getMetadata().put(VECTOR_STORE_METADATA_SEGMENT_ID, segmentDO.getId().toString());
        vectorStore.add(List.of(segment));

        // 2. 更新向量 ID
        segmentMapper.updateById(new AiKnowledgeSegmentDO().setId(segmentDO.getId()).setVectorId(segment.getId()));
    }

    private void deleteVectorStore(VectorStore vectorStore, AiKnowledgeSegmentDO segmentDO) {
        // 1. 更新向量 ID
        if (StrUtil.isEmpty(segmentDO.getVectorId())) {
            return;
        }
        segmentMapper.updateById(new AiKnowledgeSegmentDO().setId(segmentDO.getId())
                .setVectorId(AiKnowledgeSegmentDO.VECTOR_ID_EMPTY));

        // 2. 删除向量
        vectorStore.delete(List.of(segmentDO.getVectorId()));
    }

    @Override
    public List<AiKnowledgeSegmentSearchRespBO> searchKnowledgeSegment(AiKnowledgeSegmentSearchReqBO reqBO) {
        // 1. 校验
        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(reqBO.getKnowledgeId());

        // 2. 检索
        List<Document> documents = searchDocument(knowledge, reqBO);
        if (CollUtil.isEmpty(documents)) {
            return ListUtil.empty();
        }

        // 3.1 段落召回
        List<AiKnowledgeSegmentDO> segments = segmentMapper
                .selectListByVectorIds(convertList(documents, Document::getId));
        if (CollUtil.isEmpty(segments)) {
            return ListUtil.empty();
        }
        // 3.2 增加召回次数
        segmentMapper.updateRetrievalCountIncrByIds(convertList(segments, AiKnowledgeSegmentDO::getId));

        // 4. 构建结果
        List<AiKnowledgeSegmentSearchRespBO> result = convertList(segments, segment -> {
            Document document = CollUtil.findOne(documents, // 找到对应的文档
                    doc -> Objects.equals(doc.getId(), segment.getVectorId()));
            if (document == null) {
                return null;
            }
            return BeanUtils.toBean(segment, AiKnowledgeSegmentSearchRespBO.class)
                    .setScore(document.getScore());
        });
        result.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore())); // 按照分数降序排序
        return result;
    }

    /**
     * 基于 Embedding + Rerank Model，检索知识库中的文档
     *
     * @param knowledge 知识库
     * @param reqBO 检索请求
     * @return 文档列表
     */
    private List<Document> searchDocument(AiKnowledgeDO knowledge, AiKnowledgeSegmentSearchReqBO reqBO) {
        VectorStore vectorStore = getVectorStoreById(knowledge);
        Integer topK = ObjUtil.defaultIfNull(reqBO.getTopK(), knowledge.getTopK());
        Double similarityThreshold = ObjUtil.defaultIfNull(reqBO.getSimilarityThreshold(), knowledge.getSimilarityThreshold());

        // 1. 向量检索
        int searchTopK = rerankModel != null ? topK * RERANK_RETRIEVAL_FACTOR : topK;
        double searchSimilarityThreshold = rerankModel != null ? SIMILARITY_THRESHOLD_ACCEPT_ALL : similarityThreshold;
        SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                .query(reqBO.getContent())
                .topK(searchTopK).similarityThreshold(searchSimilarityThreshold)
                .filterExpression(new FilterExpressionBuilder()
                        .eq(VECTOR_STORE_METADATA_KNOWLEDGE_ID, reqBO.getKnowledgeId().toString()).build());
        List<Document> documents = vectorStore.similaritySearch(searchRequestBuilder.build());
        if (CollUtil.isEmpty(documents)) {
            return documents;
        }

        // 2. Rerank 重排序
        if (rerankModel != null) {
            RerankResponse rerankResponse = rerankModel.call(new RerankRequest(reqBO.getContent(), documents,
                    DashScopeRerankOptions.builder().withTopN(topK).build()));
            documents = convertList(rerankResponse.getResults(),
                    documentWithScore -> documentWithScore.getScore() >= similarityThreshold
                            ? documentWithScore.getOutput() : null);
        }
        return documents;
    }

    @Override
    public List<AiKnowledgeSegmentDO> splitContent(String url, Integer segmentMaxTokens) {
        // 1. 读取 URL 内容
        String content = knowledgeDocumentService.readUrl(url);

        // 2.1 自动检测文档类型并选择策略
        AiDocumentSplitStrategyEnum strategy = detectDocumentStrategy(content, url);
        // 2.2 文档切片
        List<Document> documentSegments = splitContentByStrategy(content, segmentMaxTokens, strategy, url);

        // 3. 转换为段落对象
        return convertList(documentSegments, segment -> {
            if (StrUtil.isEmpty(segment.getText())) {
                return null;
            }
            return new AiKnowledgeSegmentDO()
                    .setContent(segment.getText())
                    .setContentLength(segment.getText().length())
                    .setTokens(tokenCountEstimator.estimate(segment.getText()));
        });
    }

    /**
     * 校验段落是否存在
     *
     * @param id 文档编号
     * @return 段落信息
     */
    private AiKnowledgeSegmentDO validateKnowledgeSegmentExists(Long id) {
        AiKnowledgeSegmentDO knowledgeSegment = segmentMapper.selectById(id);
        if (knowledgeSegment == null) {
            throw exception(KNOWLEDGE_SEGMENT_NOT_EXISTS);
        }
        return knowledgeSegment;
    }

    private VectorStore getVectorStoreById(AiKnowledgeDO knowledge) {
        return modelService.getOrCreateVectorStore(knowledge.getEmbeddingModelId(), VECTOR_STORE_METADATA_TYPES);
    }

    private VectorStore getVectorStoreById(Long knowledgeId) {
        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(knowledgeId);
        return getVectorStoreById(knowledge);
    }

    /**
     * 根据策略切分内容
     *
     * @param content 文档内容
     * @param segmentMaxTokens 分段的最大 Token 数
     * @param strategy 切片策略
     * @param url 文档 URL（用于自动检测文件类型）
     * @return 切片后的文档列表
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private List<Document> splitContentByStrategy(String content, Integer segmentMaxTokens,
                                                  AiDocumentSplitStrategyEnum strategy, String url) {
        // 自动检测策略
        if (strategy == AiDocumentSplitStrategyEnum.AUTO) {
            strategy = detectDocumentStrategy(content, url);
            log.info("[splitContentByStrategy][自动检测到文档策略: {}]", strategy.getName());
        }
        // 根据策略切分
        TextSplitter textSplitter;
        switch (strategy) {
            case MARKDOWN_QA:
                textSplitter = new MarkdownQaSplitter(segmentMaxTokens);
                break;
            case SEMANTIC:
                textSplitter = new SemanticTextSplitter(segmentMaxTokens);
                break;
            case PARAGRAPH:
                textSplitter = new SemanticTextSplitter(segmentMaxTokens, 0); // 段落切分，无重叠
                break;
            case TOKEN:
            default:
                textSplitter = buildTokenTextSplitter(segmentMaxTokens);
                break;
        }
        // 执行切分
        return textSplitter.apply(Collections.singletonList(new Document(content)));
    }

    /**
     * 自动检测文档类型并选择切片策略
     *
     * @param content 文档内容
     * @param url 文档 URL
     * @return 推荐的切片策略
     */
    private AiDocumentSplitStrategyEnum detectDocumentStrategy(String content, String url) {
        if (StrUtil.isEmpty(content)) {
            return AiDocumentSplitStrategyEnum.TOKEN;
        }
        // 1. 检测 Markdown QA 格式
        if (isMarkdownQaFormat(content, url)) {
            return AiDocumentSplitStrategyEnum.MARKDOWN_QA;
        }
        // 2. 检测普通 Markdown 文档
        if (isMarkdownDocument(url)) {
            return AiDocumentSplitStrategyEnum.SEMANTIC;
        }
        // 3. 默认使用语义切分（比 Token 切分更智能）
        return AiDocumentSplitStrategyEnum.SEMANTIC;
    }

    /**
     * 检测是否为 Markdown QA 格式
     * 特征：包含多个二级标题（## ）且标题后紧跟答案内容
     */
    private boolean isMarkdownQaFormat(String content, String url) {
        // 文件扩展名判断
        if (StrUtil.isNotEmpty(url) && !url.toLowerCase().endsWith(".md")) {
            return false;
        }

        // 统计二级标题数量
        long h2Count = content.lines()
                .filter(line -> line.trim().startsWith("## "))
                .count();

        // 要求一：至少包含 2 个二级标题才认为是 QA 格式
        if (h2Count < 2) {
            return false;
        }

        // 要求二：检查标题占比（QA 文档标题行数相对较多），如果二级标题占比超过 10%，认为是 QA 格式
        long totalLines = content.lines().count();
        double h2Ratio = (double) h2Count / totalLines;
        return h2Ratio > 0.1;
    }

    /**
     * 检测是否为 Markdown 文档
     */
    private boolean isMarkdownDocument(String url) {
        return StrUtil.endWithAnyIgnoreCase(url, ".md", ".markdown");
    }

    /**
     * 构建基于 Token 的文本切片器（原有逻辑保留）
     */
    private static TextSplitter buildTokenTextSplitter(Integer segmentMaxTokens) {
        return TokenTextSplitter.builder()
                .withChunkSize(segmentMaxTokens)
                .withMinChunkSizeChars(Integer.MAX_VALUE) // 忽略字符的截断
                .withMinChunkLengthToEmbed(1) // 允许的最小有效分段长度
                .withMaxNumChunks(Integer.MAX_VALUE)
                .withKeepSeparator(true) // 保留分隔符
                .build();
    }

    @Override
    public List<AiKnowledgeSegmentProcessRespVO> getKnowledgeSegmentProcessList(List<Long> documentIds) {
        if (CollUtil.isEmpty(documentIds)) {
            return Collections.emptyList();
        }
        return segmentMapper.selectProcessList(documentIds);
    }

    @Override
    public Long createKnowledgeSegment(AiKnowledgeSegmentSaveReqVO createReqVO) {
        // 1.1 校验文档是否存在
        AiKnowledgeDocumentDO document = knowledgeDocumentService
                .validateKnowledgeDocumentExists(createReqVO.getDocumentId());
        // 1.2 获取知识库信息
        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(document.getKnowledgeId());
        // 1.3 校验 token 熟练
        Integer tokens = tokenCountEstimator.estimate(createReqVO.getContent());
        if (tokens > document.getSegmentMaxTokens()) {
            throw exception(KNOWLEDGE_SEGMENT_CONTENT_TOO_LONG, tokens, document.getSegmentMaxTokens());
        }

        // 2. 保存段落
        AiKnowledgeSegmentDO segment = BeanUtils.toBean(createReqVO, AiKnowledgeSegmentDO.class)
                .setKnowledgeId(knowledge.getId()).setDocumentId(document.getId())
                .setContentLength(createReqVO.getContent().length()).setTokens(tokens)
                .setVectorId(AiKnowledgeSegmentDO.VECTOR_ID_EMPTY)
                .setRetrievalCount(0).setStatus(CommonStatusEnum.ENABLE.getStatus());
        segmentMapper.insert(segment);

        // 3. 向量化
        writeVectorStore(getVectorStoreById(knowledge), segment, new Document(segment.getContent()));
        return segment.getId();
    }

    @Override
    public AiKnowledgeSegmentDO getKnowledgeSegment(Long id) {
        return segmentMapper.selectById(id);
    }

    @Override
    public List<AiKnowledgeSegmentDO> getKnowledgeSegmentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return segmentMapper.selectByIds(ids);
    }

}

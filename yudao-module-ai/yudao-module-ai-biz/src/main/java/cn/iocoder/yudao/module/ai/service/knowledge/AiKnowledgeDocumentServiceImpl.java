package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeSegmentMapper;
import cn.iocoder.yudao.module.ai.enums.knowledge.AiKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * AI 知识库-文档 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeDocumentServiceImpl implements AiKnowledgeDocumentService {

    @Resource
    private AiKnowledgeDocumentMapper documentMapper;
    @Resource
    private AiKnowledgeSegmentMapper segmentMapper;

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private TokenCountEstimator tokenCountEstimator;

    @Resource
    private AiApiKeyService apiKeyService;
    @Resource
    private AiKnowledgeService knowledgeService;
    @Resource
    private AiChatModelService chatModelService;


    // TODO 芋艿：需要 review 下，代码格式；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeDocument(AiKnowledgeDocumentCreateReqVO createReqVO) {
        // 1.1 下载文档
        String url = createReqVO.getUrl();
        // 1.2 加载文档
        TikaDocumentReader loader = new TikaDocumentReader(downloadFile(url));
        List<Document> documents = loader.get();
        Document document = CollUtil.getFirst(documents);
        String content = document.getContent();
        Integer tokens = tokenCountEstimator.estimate(content);
        Integer wordCount = content.length();

        // 1.3 文档记录入库
        AiKnowledgeDocumentDO documentDO = BeanUtils.toBean(createReqVO, AiKnowledgeDocumentDO.class)
                .setTokens(tokens).setWordCount(wordCount)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setSliceStatus(AiKnowledgeDocumentStatusEnum.SUCCESS.getStatus());
        documentMapper.insert(documentDO);
        Long documentId = documentDO.getId();
        if (CollUtil.isEmpty(documents)) {
            return documentId;
        }

        // 2.1 文档分段
        List<Document> segments = tokenTextSplitter.apply(documents);
        // 2.2 分段内容入库
        List<AiKnowledgeSegmentDO> segmentDOList = CollectionUtils.convertList(segments,
                segment -> new AiKnowledgeSegmentDO().setContent(segment.getContent()).setDocumentId(documentId).setKnowledgeId(createReqVO.getKnowledgeId()).setVectorId(segment.getId())
                        .setTokens(tokenCountEstimator.estimate(segment.getContent())).setWordCount(segment.getContent().length())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        segmentMapper.insertBatch(segmentDOList);

        // 3.1 document 补充源数据
        segments.forEach(segment -> {
            Map<String, Object> metadata = segment.getMetadata();
            metadata.put(AiKnowledgeSegmentDO.FIELD_KNOWLEDGE_ID, createReqVO.getKnowledgeId());
        });

        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(createReqVO.getKnowledgeId());
        AiChatModelDO model = chatModelService.validateChatModel(knowledge.getModelId());
        // 3.2 获取向量存储实例
        VectorStore vectorStore = apiKeyService.getOrCreateVectorStore(model.getKeyId());
        // 3.3 向量化并存储
        vectorStore.add(segments);
        return documentId;
    }

    private org.springframework.core.io.Resource downloadFile(String url) {
        try {
            byte[] bytes = HttpUtil.downloadBytes(url);
            return new ByteArrayResource(bytes);
        } catch (Exception e) {
            log.error("[downloadFile][url({}) 下载失败]", url, e);
            throw new RuntimeException(e);
        }
    }

}

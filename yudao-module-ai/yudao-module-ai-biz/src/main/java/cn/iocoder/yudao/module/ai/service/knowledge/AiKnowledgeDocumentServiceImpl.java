package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeDocumentMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeSegmentMapper;
import cn.iocoder.yudao.module.ai.enums.knowledge.AiKnowledgeDocumentStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    private TokenCountEstimator TOKEN_COUNT_ESTIMATOR;
    @Resource
    private RedisVectorStore vectorStore;


    // TODO 芋艿：需要 review 下，代码格式；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeDocument(AiKnowledgeDocumentCreateReqVO createReqVO) {
        // 1.1 下载文档
        String url = createReqVO.getUrl();
        TikaDocumentReader loader = new TikaDocumentReader(downloadFile(url));
        // 1.2 加载文档
        List<Document> documents = loader.get();
        Document document = CollUtil.getFirst(documents);
        // TODO @xin：是不是不存在，就抛出异常呀；厚泽 return 呀；
        Integer tokens = Objects.nonNull(document) ? TOKEN_COUNT_ESTIMATOR.estimate(document.getContent()) : 0;
        Integer wordCount = Objects.nonNull(document) ? document.getContent().length() : 0;

        AiKnowledgeDocumentDO documentDO = BeanUtils.toBean(createReqVO, AiKnowledgeDocumentDO.class)
                .setTokens(tokens).setWordCount(wordCount)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setSliceStatus(AiKnowledgeDocumentStatusEnum.SUCCESS.getStatus());
        // 1.2 文档记录入库
        documentMapper.insert(documentDO);
        Long documentId = documentDO.getId();
        if (CollUtil.isEmpty(documents)) {
            return documentId;
        }

        // 2.1 文档分段
        List<Document> segments = tokenTextSplitter.apply(documents);
        // 2.2 分段内容入库
        List<AiKnowledgeSegmentDO> segmentDOList = CollectionUtils.convertList(segments,
                segment -> new AiKnowledgeSegmentDO().setContent(segment.getContent()).setDocumentId(documentId)
                        .setTokens(TOKEN_COUNT_ESTIMATOR.estimate(segment.getContent())).setWordCount(segment.getContent().length())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        segmentMapper.insertBatch(segmentDOList);
        // 3 向量化并存储
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

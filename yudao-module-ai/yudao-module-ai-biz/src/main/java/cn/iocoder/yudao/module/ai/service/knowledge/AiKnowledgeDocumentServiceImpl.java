package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
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
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
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
    private AiEmbeddingService embeddingService;

    private static final JTokkitTokenCountEstimator TOKEN_COUNT_ESTIMATOR = new JTokkitTokenCountEstimator();

    // TODO xiaoxin 临时测试用，后续删
    @Value("classpath:/webapp/test/Fel.pdf")
    private org.springframework.core.io.Resource data;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeDocument(AiKnowledgeDocumentCreateReqVO createReqVO) {

        // TODO xiaoxin 后续从 url 加载
        TikaDocumentReader loader = new TikaDocumentReader(data);
        // 加载文档
        List<Document> documents = loader.get();
        Document document = CollUtil.getFirst(documents);
        // TODO 芋艿 文档层面有没有可能会比较大，这两个字段是否可以从分段表计算得出？
        Integer tokens = Objects.nonNull(document) ? TOKEN_COUNT_ESTIMATOR.estimate(document.getContent()) : 0;
        Integer wordCount = Objects.nonNull(document) ? document.getContent().length() : 0;

        AiKnowledgeDocumentDO documentDO = BeanUtils.toBean(createReqVO, AiKnowledgeDocumentDO.class);
        documentDO.setTokens(tokens).setWordCount(wordCount)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setSliceStatus(AiKnowledgeDocumentStatusEnum.SUCCESS.getStatus());
        // 文档记录入库
        documentMapper.insert(documentDO);
        Long documentId = documentDO.getId();
        if (CollUtil.isEmpty(documents)) {
            return documentId;
        }

        // 文档分段
        List<Document> segments = tokenTextSplitter.apply(documents);

        List<AiKnowledgeSegmentDO> segmentDOList = CollectionUtils.convertList(segments,
                segment -> new AiKnowledgeSegmentDO().setContent(segment.getContent()).setDocumentId(documentId)
                        .setTokens(TOKEN_COUNT_ESTIMATOR.estimate(segment.getContent())).setWordCount(segment.getContent().length())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        // 分段内容入库
        segmentMapper.insertBatch(segmentDOList);

        //向量化并存储
        embeddingService.add(segments);

        return documentId;
    }
}

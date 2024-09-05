package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentSearchReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeSegmentMapper;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.KNOWLEDGE_SEGMENT_NOT_EXISTS;

/**
 * AI 知识库分片 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeSegmentServiceImpl implements AiKnowledgeSegmentService {

    @Resource
    private AiKnowledgeSegmentMapper segmentMapper;

    @Resource
    private AiKnowledgeService knowledgeService;
    @Resource
    private AiChatModelService chatModelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Override
    public PageResult<AiKnowledgeSegmentDO> getKnowledgeSegmentPage(AiKnowledgeSegmentPageReqVO pageReqVO) {
        return segmentMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateKnowledgeSegment(AiKnowledgeSegmentUpdateReqVO reqVO) {
        // 0 校验
        AiKnowledgeSegmentDO oldKnowledgeSegment = validateKnowledgeSegmentExists(reqVO.getId());
        // 2.1 获取知识库向量实例
        VectorStore vectorStore = knowledgeService.getVectorStoreById(oldKnowledgeSegment.getKnowledgeId());
        // 2.2 删除原向量
        vectorStore.delete(List.of(oldKnowledgeSegment.getVectorId()));

        // 2.3 重新向量化
        Document document = new Document(reqVO.getContent());
        document.getMetadata().put(AiKnowledgeSegmentDO.FIELD_KNOWLEDGE_ID, oldKnowledgeSegment.getKnowledgeId());
        vectorStore.add(List.of(document));

        // 2.1 更新段落内容
        AiKnowledgeSegmentDO knowledgeSegment = BeanUtils.toBean(reqVO, AiKnowledgeSegmentDO.class);
        knowledgeSegment.setVectorId(document.getId());
        segmentMapper.updateById(knowledgeSegment);
    }

    @Override
    public void updateKnowledgeSegmentStatus(AiKnowledgeSegmentUpdateStatusReqVO reqVO) {
        // 0 校验
        AiKnowledgeSegmentDO oldKnowledgeSegment = validateKnowledgeSegmentExists(reqVO.getId());
        // 1 获取知识库向量实例
        VectorStore vectorStore = knowledgeService.getVectorStoreById(oldKnowledgeSegment.getKnowledgeId());
        AiKnowledgeSegmentDO knowledgeSegment = BeanUtils.toBean(reqVO, AiKnowledgeSegmentDO.class);

        if (Objects.equals(reqVO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            // 2.1 启用重新向量化
            Document document = new Document(oldKnowledgeSegment.getContent());
            document.getMetadata().put(AiKnowledgeSegmentDO.FIELD_KNOWLEDGE_ID, oldKnowledgeSegment.getKnowledgeId());
            vectorStore.add(List.of(document));
            knowledgeSegment.setVectorId(document.getId());
        } else {
            // 2.2 禁用删除向量
            vectorStore.delete(List.of(oldKnowledgeSegment.getVectorId()));
            knowledgeSegment.setVectorId(null);
        }
        // 3 更新段落状态
        segmentMapper.updateById(knowledgeSegment);
    }

    @Override
    public List<AiKnowledgeSegmentDO> similaritySearch(AiKnowledgeSegmentSearchReqVO reqVO) {
        // 0. 校验
        AiKnowledgeDO knowledge = knowledgeService.validateKnowledgeExists(reqVO.getKnowledgeId());
        AiChatModelDO model = chatModelService.validateChatModel(knowledge.getModelId());

        // 1.1 获取向量存储实例
        VectorStore vectorStore = apiKeyService.getOrCreateVectorStore(model.getKeyId());

        // 1.2 向量检索
        List<Document> documentList = vectorStore.similaritySearch(SearchRequest.query(reqVO.getContent())
                .withTopK(knowledge.getTopK())
                .withSimilarityThreshold(knowledge.getSimilarityThreshold())
                .withFilterExpression(new FilterExpressionBuilder().eq(AiKnowledgeSegmentDO.FIELD_KNOWLEDGE_ID, reqVO.getKnowledgeId()).build()));
        if (CollUtil.isEmpty(documentList)) {
            return ListUtil.empty();
        }
        // 2.1 段落召回
        return segmentMapper.selectList(CollUtil.getFieldValues(documentList, "id", String.class));
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
}

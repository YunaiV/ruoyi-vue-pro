package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
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
        segmentMapper.updateById(BeanUtils.toBean(reqVO, AiKnowledgeSegmentDO.class));
        // TODO @xin 重新向量化
    }

    @Override
    public void updateKnowledgeSegmentStatus(AiKnowledgeSegmentUpdateStatusReqVO reqVO) {
        segmentMapper.updateById(BeanUtils.toBean(reqVO, AiKnowledgeSegmentDO.class));
        // TODO @xin 1.禁用删除向量 2.启用重新向量化
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
                //TODO  @xin 配置提取
                .withTopK(5)
                .withSimilarityThreshold(0.5d)
                .withFilterExpression(new FilterExpressionBuilder().eq(AiKnowledgeSegmentDO.FIELD_KNOWLEDGE_ID, reqVO.getKnowledgeId()).build()));
        if (CollUtil.isEmpty(documentList)) {
            return ListUtil.empty();
        }
        // 2.1 段落召回
        return segmentMapper.selectList(CollUtil.getFieldValues(documentList, "id", String.class));
    }
}

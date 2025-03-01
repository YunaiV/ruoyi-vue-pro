package cn.iocoder.yudao.module.ai.dal.mysql.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentProcessRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Collection;

/**
 * AI 知识库分片 Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiKnowledgeSegmentMapper extends BaseMapperX<AiKnowledgeSegmentDO> {

    default PageResult<AiKnowledgeSegmentDO> selectPage(AiKnowledgeSegmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiKnowledgeSegmentDO>()
                .eq(AiKnowledgeSegmentDO::getDocumentId, reqVO.getDocumentId())
                .eqIfPresent(AiKnowledgeSegmentDO::getStatus, reqVO.getStatus())
                .likeIfPresent(AiKnowledgeSegmentDO::getContent, reqVO.getKeyword())
                .orderByDesc(AiKnowledgeSegmentDO::getId));
    }

    default List<AiKnowledgeSegmentDO> selectListByVectorIds(List<String> vectorIdList) {
        return selectList(new LambdaQueryWrapperX<AiKnowledgeSegmentDO>()
                .in(AiKnowledgeSegmentDO::getVectorId, vectorIdList)
                .orderByDesc(AiKnowledgeSegmentDO::getId));
    }

    default List<AiKnowledgeSegmentDO> selectListByDocumentId(Long documentId) {
        return selectList(new LambdaQueryWrapperX<AiKnowledgeSegmentDO>()
                .eq(AiKnowledgeSegmentDO::getDocumentId, documentId)
                .orderByDesc(AiKnowledgeSegmentDO::getId));
    }

    default List<AiKnowledgeSegmentProcessRespVO> selectProcessList(Collection<Long> documentIds) {
        MPJLambdaWrapper<AiKnowledgeSegmentDO> wrapper = new MPJLambdaWrapperX<AiKnowledgeSegmentDO>()
                .selectAs(AiKnowledgeSegmentDO::getDocumentId, AiKnowledgeSegmentProcessRespVO::getDocumentId)
                .selectCount(AiKnowledgeSegmentDO::getId, "count")
                .select("COUNT(CASE WHEN vector_id > '" + AiKnowledgeSegmentDO.VECTOR_ID_EMPTY
                        + "' THEN 1 ELSE NULL END) AS embeddingCount")
                .in(AiKnowledgeSegmentDO::getDocumentId, documentIds)
                .groupBy(AiKnowledgeSegmentDO::getDocumentId);
        return selectJoinList(AiKnowledgeSegmentProcessRespVO.class, wrapper);
    }

}

package cn.iocoder.yudao.module.ai.dal.mysql.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 知识库-分片 Mapper
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

    default List<AiKnowledgeSegmentDO> selectList(List<String> vectorIdList) {
        return selectList(new LambdaQueryWrapperX<AiKnowledgeSegmentDO>()
                .in(AiKnowledgeSegmentDO::getVectorId, vectorIdList)
                .orderByDesc(AiKnowledgeSegmentDO::getId));
    }
}

package cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentLikeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeDocumentLikeMapper extends BaseMapperX<PmsKnowledgeDocumentLikeDO> {

    default PmsKnowledgeDocumentLikeDO selectByDocumentIdAndUserId(Long documentId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeDocumentLikeDO>()
                .eq(PmsKnowledgeDocumentLikeDO::getDocumentId, documentId)
                .eq(PmsKnowledgeDocumentLikeDO::getUserId, userId));
    }

    default List<PmsKnowledgeDocumentLikeDO> selectListByDocumentId(Long documentId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentLikeDO>()
                .eq(PmsKnowledgeDocumentLikeDO::getDocumentId, documentId)
                .orderByAsc(PmsKnowledgeDocumentLikeDO::getCreateTime)
                .orderByAsc(PmsKnowledgeDocumentLikeDO::getId));
    }

    default void deleteByDocumentIds(Collection<Long> documentIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeDocumentLikeDO>()
                .in(PmsKnowledgeDocumentLikeDO::getDocumentId, documentIds));
    }

}

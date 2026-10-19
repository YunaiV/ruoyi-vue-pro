package cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeDocumentCommentMapper extends BaseMapperX<PmsKnowledgeDocumentCommentDO> {

    default List<PmsKnowledgeDocumentCommentDO> selectListByDocumentId(Long documentId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentCommentDO>()
                .eq(PmsKnowledgeDocumentCommentDO::getDocumentId, documentId)
                .orderByAsc(PmsKnowledgeDocumentCommentDO::getCreateTime)
                .orderByAsc(PmsKnowledgeDocumentCommentDO::getId));
    }

    default void deleteByMainId(Long mainId) {
        delete(PmsKnowledgeDocumentCommentDO::getMainId, mainId);
    }

    default void deleteByDocumentIds(Collection<Long> documentIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeDocumentCommentDO>()
                .in(PmsKnowledgeDocumentCommentDO::getDocumentId, documentIds));
    }

}

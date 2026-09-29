package cn.iocoder.yudao.module.ai1.dal.mysql.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 知识文档 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1KnowledgeDocumentMapper extends BaseMapperX<Ai1KnowledgeDocumentDO> {

    default PageResult<Ai1KnowledgeDocumentDO> selectPage(Ai1KnowledgeDocumentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1KnowledgeDocumentDO>()
                .eq(Ai1KnowledgeDocumentDO::getKnowledgeBaseId, reqVO.getKnowledgeBaseId())
                .likeIfPresent(Ai1KnowledgeDocumentDO::getName, reqVO.getName())
                .eqIfPresent(Ai1KnowledgeDocumentDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1KnowledgeDocumentDO::getId));
    }

    default List<Ai1KnowledgeDocumentDO> selectListByKnowledgeBaseId(Long knowledgeBaseId) {
        return selectList(new LambdaQueryWrapperX<Ai1KnowledgeDocumentDO>()
                .eq(Ai1KnowledgeDocumentDO::getKnowledgeBaseId, knowledgeBaseId)
                .orderByAsc(Ai1KnowledgeDocumentDO::getId));
    }

    default int deleteByKnowledgeBaseIds(Collection<Long> knowledgeBaseIds) {
        return delete(new LambdaQueryWrapperX<Ai1KnowledgeDocumentDO>()
                .in(Ai1KnowledgeDocumentDO::getKnowledgeBaseId, knowledgeBaseIds));
    }

}

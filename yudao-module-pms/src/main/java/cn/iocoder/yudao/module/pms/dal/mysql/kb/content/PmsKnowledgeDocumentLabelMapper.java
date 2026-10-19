package cn.iocoder.yudao.module.pms.dal.mysql.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentLabelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsKnowledgeDocumentLabelMapper extends BaseMapperX<PmsKnowledgeDocumentLabelDO> {

    default List<PmsKnowledgeDocumentLabelDO> selectList() {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeDocumentLabelDO>()
                .orderByAsc(PmsKnowledgeDocumentLabelDO::getCreateTime)
                .orderByAsc(PmsKnowledgeDocumentLabelDO::getId));
    }

}

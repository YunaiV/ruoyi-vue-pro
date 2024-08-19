package cn.iocoder.yudao.module.ai.dal.mysql.knowledge;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 知识库-文档 Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiKnowledgeDocumentMapper extends BaseMapperX<AiKnowledgeDocumentDO> {
}

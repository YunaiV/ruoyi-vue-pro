package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeDocumentCreateReqVO;

/**
 * AI 知识库-文档 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeDocumentService {

    /**
     * 创建文档
     *
     * @param createReqVO 文档创建 Request VO
     * @return 文档编号
     */
    Long createKnowledgeDocument(AiKnowledgeDocumentCreateReqVO createReqVO);

}

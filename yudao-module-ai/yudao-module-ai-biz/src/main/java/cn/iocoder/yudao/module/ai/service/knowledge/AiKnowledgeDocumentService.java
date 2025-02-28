package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.AiKnowledgeDocumentCreateListReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;

import java.util.List;

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

    /**
     * 批量创建文档
     *
     * @param createListReqVO 批量创建 Request VO
     * @return 文档编号列表
     */
    List<Long> createKnowledgeDocumentList(AiKnowledgeDocumentCreateListReqVO createListReqVO);

    /**
     * 获取文档分页
     *
     * @param pageReqVO 分页参数
     * @return 文档分页
     */
    PageResult<AiKnowledgeDocumentDO> getKnowledgeDocumentPage(AiKnowledgeDocumentPageReqVO pageReqVO);

    /**
     * 获取文档详情
     *
     * @param id 文档编号
     * @return 文档详情
     */
    AiKnowledgeDocumentDO getKnowledgeDocument(Long id);

    /**
     * 更新文档
     *
     * @param reqVO 更新信息
     */
    void updateKnowledgeDocument(AiKnowledgeDocumentUpdateReqVO reqVO);

    /**
     * 校验文档是否存在
     *
     * @param id 文档编号
     * @return 文档信息
     */
    AiKnowledgeDocumentDO validateKnowledgeDocumentExists(Long id);

    /**
     * 读取 URL 内容
     *
     * @param url URL
     * @return 内容
     */
    String readUrl(String url);

}

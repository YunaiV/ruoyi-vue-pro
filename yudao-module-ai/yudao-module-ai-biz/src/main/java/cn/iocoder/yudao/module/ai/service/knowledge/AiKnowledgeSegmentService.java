package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;

/**
 * AI 知识库段落 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeSegmentService {

    /**
     * 获取段落分页
     *
     * @param pageReqVO 分页查询
     * @return 文档分页
     */
    PageResult<AiKnowledgeSegmentDO> getKnowledgeSegmentPage(AiKnowledgeSegmentPageReqVO pageReqVO);

    /**
     * 更新段落的内容
     *
     * @param reqVO 更新内容
     */
    void updateKnowledgeSegment(AiKnowledgeSegmentUpdateReqVO reqVO);

    /**
     * 更新段落的状态
     *
     * @param reqVO 更新内容
     */
    void updateKnowledgeSegmentStatus(AiKnowledgeSegmentUpdateStatusReqVO reqVO);

}

package cn.iocoder.yudao.module.ai.service.knowledge;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.AiKnowledgeUpdateMyReqVO;

/**
 * AI 知识库-基础信息 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeBaseService {

    /**
     * 创建【我的】知识库
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 编号
     */
    Long createKnowledgeMy(AiKnowledgeCreateMyReqVO createReqVO, Long userId);


    /**
     * 创建【我的】知识库
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateKnowledgeMy(AiKnowledgeUpdateMyReqVO updateReqVO, Long userId);

}

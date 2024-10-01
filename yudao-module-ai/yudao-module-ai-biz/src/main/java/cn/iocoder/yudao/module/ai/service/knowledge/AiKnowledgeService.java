package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * AI 知识库-基础信息 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeService {

    /**
     * 创建知识库
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createKnowledge(AiKnowledgeCreateReqVO createReqVO, Long userId);

    /**
     * 更新知识库
     *
     * @param updateReqVO 更新信息
     * @param userId      用户编号
     */
    void updateKnowledge(AiKnowledgeUpdateReqVO updateReqVO, Long userId);

    /**
     * 校验知识库是否存在
     *
     * @param id 记录编号
     */
    AiKnowledgeDO validateKnowledgeExists(Long id);

    /**
     * 获得知识库分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页查询
     * @return 知识库分页
     */
    PageResult<AiKnowledgeDO> getKnowledgePage(Long userId, AiKnowledgePageReqVO pageReqVO);

    /**
     * 根据知识库编号获取向量存储实例
     *
     * @param id 知识库编号
     * @return 向量存储实例
     */
    VectorStore getVectorStoreById(Long id);

}

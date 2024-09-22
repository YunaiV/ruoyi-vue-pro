package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeCreateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * AI 知识库-基础信息 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeService {

    /**
     * 创建【我的】知识库
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createKnowledgeMy(AiKnowledgeCreateMyReqVO createReqVO, Long userId);

    /**
     * 创建【我的】知识库
     *
     * @param updateReqVO 更新信息
     * @param userId      用户编号
     */
    void updateKnowledgeMy(AiKnowledgeUpdateMyReqVO updateReqVO, Long userId);

    /**
     * 校验知识库是否存在
     *
     * @param id 记录编号
     */
    AiKnowledgeDO validateKnowledgeExists(Long id);

    /**
     * 获得【我的】知识库分页
     *
     * @param userId 用户编号
     * @param pageReqVO   分页查询
     * @return 知识库分页
     */
    PageResult<AiKnowledgeDO> getKnowledgePageMy(Long userId, PageParam pageReqVO);

    /**
     * 根据知识库编号获取向量存储实例
     *
     * @param id 知识库编号
     * @return 向量存储实例
     */
    VectorStore getVectorStoreById(Long id);

}

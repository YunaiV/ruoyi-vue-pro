package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;

import java.util.List;

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
     * @return 编号
     */
    Long createKnowledge(AiKnowledgeSaveReqVO createReqVO);

    /**
     * 更新知识库
     *
     * @param updateReqVO 更新信息
     */
    void updateKnowledge(AiKnowledgeSaveReqVO updateReqVO);

    /**
     * 删除知识库
     *
     * @param id 知识库编号
     */
    void deleteKnowledge(Long id);

    /**
     * 获得知识库
     *
     * @param id 编号
     * @return 知识库
     */
    AiKnowledgeDO getKnowledge(Long id);

    /**
     * 校验知识库是否存在
     *
     * @param id 记录编号
     */
    AiKnowledgeDO validateKnowledgeExists(Long id);

    /**
     * 获得知识库分页
     *
     * @param pageReqVO 分页查询
     * @return 知识库分页
     */
    PageResult<AiKnowledgeDO> getKnowledgePage(AiKnowledgePageReqVO pageReqVO);

    /**
     * 获得指定状态的知识库列表
     *
     * @param status 状态
     * @return 知识库列表
     */
    List<AiKnowledgeDO> getKnowledgeSimpleListByStatus(Integer status);

}

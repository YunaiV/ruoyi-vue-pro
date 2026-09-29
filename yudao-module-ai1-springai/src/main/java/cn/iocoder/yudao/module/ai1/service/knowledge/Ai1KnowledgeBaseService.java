package cn.iocoder.yudao.module.ai1.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBasePageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBaseSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.tool.rag.Ai1RagTool;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 知识库 Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1KnowledgeBaseService {

    /**
     * 创建知识库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createKnowledgeBase(@Valid Ai1KnowledgeBaseSaveReqVO createReqVO);

    /**
     * 更新知识库
     *
     * @param updateReqVO 更新信息
     */
    void updateKnowledgeBase(@Valid Ai1KnowledgeBaseSaveReqVO updateReqVO);

    /**
     * 删除知识库，连带删除文档与 Milvus 向量集合
     *
     * @param id 编号
     */
    void deleteKnowledgeBase(Long id);

    /**
     * 批量删除知识库，连带删除文档与 Milvus 向量集合
     *
     * @param ids 编号列表
     */
    void deleteKnowledgeBaseListByIds(List<Long> ids);

    /**
     * 获得知识库
     *
     * @param id 编号
     * @return 知识库
     */
    Ai1KnowledgeBaseDO getKnowledgeBase(Long id);

    /**
     * 校验知识库是否存在
     *
     * @param id 编号
     * @return 知识库
     */
    Ai1KnowledgeBaseDO validateKnowledgeBaseExists(Long id);

    /**
     * 获得知识库分页
     *
     * @param pageReqVO 分页查询
     * @return 知识库分页
     */
    PageResult<Ai1KnowledgeBaseDO> getKnowledgeBasePage(Ai1KnowledgeBasePageReqVO pageReqVO);

    /**
     * 获得指定状态的知识库列表
     *
     * @param status 状态
     * @return 知识库列表
     */
    List<Ai1KnowledgeBaseDO> getKnowledgeBaseListByStatus(Integer status);

    /**
     * 获得知识库列表
     *
     * @param ids 编号集合
     * @return 知识库列表
     */
    List<Ai1KnowledgeBaseDO> getKnowledgeBaseList(Collection<Long> ids);

    /**
     * 向量检索：按查询文本召回知识库相关分片
     *
     * @param id    编号
     * @param query 查询文本
     * @param topK  检索数量，为空时使用知识库配置
     * @return 命中分片
     */
    List<Ai1RagTool.SearchHit> searchKnowledgeBase(Long id, String query, Integer topK);

}

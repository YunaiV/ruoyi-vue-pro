package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentProcessRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import org.springframework.scheduling.annotation.Async;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI 知识库段落 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeSegmentService {

    /**
     * 获取知识库段落详情
     *
     * @param id 段落编号
     * @return 段落详情
     */
    AiKnowledgeSegmentDO getKnowledgeSegment(Long id);

    /**
     * 获取知识库段落列表
     *
     * @param ids 段落编号列表
     * @return 段落列表
     */
    List<AiKnowledgeSegmentDO> getKnowledgeSegmentList(Collection<Long> ids);

    /**
     * 获取知识库段落 Map
     *
     * @param ids 段落编号列表
     * @return 段落 Map
     */
    default Map<Long, AiKnowledgeSegmentDO> getKnowledgeSegmentMap(Collection<Long> ids) {
        return convertMap(getKnowledgeSegmentList(ids), AiKnowledgeSegmentDO::getId);
    }

    /**
     * 获取段落分页
     *
     * @param pageReqVO 分页查询
     * @return 文档分页
     */
    PageResult<AiKnowledgeSegmentDO> getKnowledgeSegmentPage(AiKnowledgeSegmentPageReqVO pageReqVO);

    /**
     * 基于 content 内容，切片创建多个段落
     *
     * @param documentId 知识库文档编号
     * @param content    文档内容
     */
    void createKnowledgeSegmentBySplitContent(Long documentId, String content);

    /**
     * 【异步】基于 content 内容，切片创建多个段落
     *
     * @param documentId 知识库文档编号
     * @param content    文档内容
     */
    @Async
    default void createKnowledgeSegmentBySplitContentAsync(Long documentId, String content) {
        createKnowledgeSegmentBySplitContent(documentId, content);
    }

    /**
     * 创建知识库段落
     *
     * @param createReqVO 创建信息
     * @return 段落编号
     */
    Long createKnowledgeSegment(AiKnowledgeSegmentSaveReqVO createReqVO);

    /**
     * 更新段落的内容
     *
     * @param reqVO 更新内容
     */
    void updateKnowledgeSegment(AiKnowledgeSegmentSaveReqVO reqVO);

    /**
     * 更新段落的状态
     *
     * @param reqVO 更新内容
     */
    void updateKnowledgeSegmentStatus(AiKnowledgeSegmentUpdateStatusReqVO reqVO);

    /**
     * 重新索引知识库下的所有文档段落
     *
     * @param knowledgeId 知识库编号
     */
    void reindexKnowledgeSegmentByKnowledgeId(Long knowledgeId);

    /**
     * 【异步】重新索引知识库下的所有文档段落
     *
     * @param knowledgeId 知识库编号
     */
    @Async
    default void reindexByKnowledgeIdAsync(Long knowledgeId) {
        reindexKnowledgeSegmentByKnowledgeId(knowledgeId);
    }

    /**
     * 根据文档编号删除段落
     *
     * @param documentId 文档编号
     */
    void deleteKnowledgeSegmentByDocumentId(Long documentId);

    /**
     * 搜索知识库段落，并返回结果
     *
     * @param reqBO 搜索请求信息
     * @return 搜索结果段落列表
     */
    List<AiKnowledgeSegmentSearchRespBO> searchKnowledgeSegment(AiKnowledgeSegmentSearchReqBO reqBO);

    /**
     * 根据 URL 内容，切片创建多个段落
     *
     * @param url              URL 地址
     * @param segmentMaxTokens 段落最大 Token 数
     * @return 切片后的段落列表
     */
    List<AiKnowledgeSegmentDO> splitContent(String url, Integer segmentMaxTokens);

    /**
     * 获取文档处理进度（多个）
     *
     * @param documentIds 文档编号列表
     * @return 文档处理列表
     */
    List<AiKnowledgeSegmentProcessRespVO> getKnowledgeSegmentProcessList(List<Long> documentIds);

}

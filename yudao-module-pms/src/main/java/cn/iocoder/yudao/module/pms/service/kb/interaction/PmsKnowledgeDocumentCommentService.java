package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.comment.PmsKnowledgeDocumentCommentSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentCommentDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识库文档评论 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeDocumentCommentService {

    /**
     * 创建文档评论或回复
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 评论编号
     */
    Long createDocumentComment(PmsKnowledgeDocumentCommentSaveReqVO saveReqVO, Long userId);

    /**
     * 删除自己的评论，删除主评论时同步删除全部回复
     *
     * @param id 评论编号
     * @param userId 用户编号
     */
    void deleteDocumentComment(Long id, Long userId);

    /**
     * 获得文档的评论和回复列表
     *
     * @param documentId 文档编号
     * @param userId 用户编号
     * @return 评论列表
     */
    List<PmsKnowledgeDocumentCommentDO> getDocumentCommentList(Long documentId, Long userId);

    /**
     * 删除文档的全部评论
     *
     * @param documentIds 文档编号集合
     */
    void deleteCommentsByDocumentIds(Collection<Long> documentIds);

}

package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentLikeDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识文档点赞 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeDocumentLikeService {

    /**
     * 点赞文档
     *
     * @param documentId 文档编号
     * @param userId 用户编号
     */
    void createDocumentLike(Long documentId, Long userId);

    /**
     * 取消点赞文档
     *
     * @param documentId 文档编号
     * @param userId 用户编号
     */
    void deleteDocumentLike(Long documentId, Long userId);

    /**
     * 获得文档的点赞列表
     *
     * @param documentId 文档编号
     * @return 点赞列表
     */
    List<PmsKnowledgeDocumentLikeDO> getDocumentLikeList(Long documentId);

    /**
     * 删除文档的点赞关系
     *
     * @param documentIds 文档编号集合
     */
    void deleteLikesByDocumentIds(Collection<Long> documentIds);

}

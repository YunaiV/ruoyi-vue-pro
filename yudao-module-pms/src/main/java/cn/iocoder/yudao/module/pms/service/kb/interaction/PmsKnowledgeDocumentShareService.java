package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentShareDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 知识库文档分享 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsKnowledgeDocumentShareService {

    /**
     * 开启文档分享，重新开启时生成新的外部令牌，并通知内部分享成员
     *
     * @param documentId 文档编号
     * @param shareUserIds 内部分享用户编号列表
     * @param userId 用户编号
     * @return 分享信息
     */
    PmsKnowledgeDocumentShareDO openShare(Long documentId, List<Long> shareUserIds, Long userId);

    /**
     * 更新文档的内部分享成员，只通知本次新增的成员
     *
     * @param documentId 文档编号
     * @param shareUserIds 内部分享用户编号列表
     * @param userId 用户编号
     */
    void updateShareMemberList(Long documentId, List<Long> shareUserIds, Long userId);

    /**
     * 关闭文档分享
     *
     * @param documentId 文档编号
     * @param userId 用户编号
     */
    void closeShare(Long documentId, Long userId);

    /**
     * 获得文档处于开启状态的分享
     *
     * @param documentId 文档编号
     * @return 分享信息
     */
    PmsKnowledgeDocumentShareDO getActiveDocumentShare(Long documentId);

    /**
     * 获得文档处于开启状态的分享，校验当前用户有读取权限
     *
     * @param documentId 文档编号
     * @param userId 用户编号
     * @return 分享信息
     */
    PmsKnowledgeDocumentShareDO getActiveDocumentShare(Long documentId, Long userId);

    /**
     * 通过分享令牌获得正常状态的文档
     *
     * @param token 分享令牌
     * @return 文档
     */
    PmsKnowledgeDocumentDO getDocumentByShareToken(String token);

    /**
     * 删除文档的分享记录
     *
     * @param documentIds 文档编号集合
     */
    void deleteSharesByDocumentIds(Collection<Long> documentIds);

}

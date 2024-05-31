package cn.iocoder.yudao.module.promotion.service.kefu;

import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.conversation.KeFuConversationUpdatePinnedReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuConversationDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服会话 Service 接口
 *
 * @author HUIHUI
 */
public interface KeFuConversationService {

    /**
     * 删除客服会话
     *
     * @param id 编号
     */
    void deleteKefuConversation(Long id);

    /**
     * 客服会话置顶
     *
     * @param updateReqVO 请求
     */
    void updatePinned(KeFuConversationUpdatePinnedReqVO updateReqVO);

    /**
     * 更新会话客服消息冗余信息
     *
     * @param id                     编号
     * @param lastMessageTime        最后聊天时间
     * @param lastMessageContent     最后聊天内容
     * @param lastMessageContentType 最后聊天内容类型
     */
    void updateConversationMessage(Long id, LocalDateTime lastMessageTime, String lastMessageContent, Integer lastMessageContentType);

    /**
     * 更新管理员未读消息数
     *
     * @param id    编号
     * @param count 数量：0 则重置 1 则消息数加一
     */
    void updateAdminUnreadMessageCountByConversationId(Long id, Integer count);

    /**
     * 更新会话对于管理员是否可见
     *
     * @param adminDeleted 管理员是否可见
     */
    void updateConversationAdminDeleted(Long id, Boolean adminDeleted);

    /**
     * 获得客服会话列表
     *
     * @return 会话列表
     */
    List<KeFuConversationDO> getKefuConversationList();

    /**
     * 获得或创建会话
     *
     * @param userId 用户编号
     * @return 客服会话
     */
    KeFuConversationDO getOrCreateConversation(Long userId);

    /**
     * 校验客服会话是否存在
     *
     * @param id 编号
     * @return 客服会话
     */
    KeFuConversationDO validateKefuConversationExists(Long id);

}
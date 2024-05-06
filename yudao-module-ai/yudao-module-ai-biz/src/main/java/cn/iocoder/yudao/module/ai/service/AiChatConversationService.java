package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;

import java.util.List;

/**
 * chat 对话
 *
 * @fansili
 * @since v1.0
 */
public interface AiChatConversationService {

    /**
     * 对话 - 创建普通对话
     *
     * @param req
     * @return
     */
    AiChatConversationRespVO createConversation(AiChatConversationCreateUserReq req);

    /**
     * 对话 - 创建role对话
     *
     * @param req
     * @return
     */
    AiChatConversationRespVO createRoleConversation(AiChatConversationCreateReqVO req);


    /**
     * 获取 - 对话
     *
     * @param id
     * @return
     */
    AiChatConversationRespVO getConversation(Long id);

    /**
     * 获取 - 对话列表
     *
     * @param req
     * @return
     */
    List<AiChatConversationRespVO> listConversation(AiChatConversationListReq req);

    /**
     * 更新 - 更新模型
     *
     * @param id
     * @param modalId
     * @return
     */
    void updateModal(Long id, Long modalId);

    /**
     * 删除 - 根据id
     *
     * @param id
     */
    void delete(Long id);

}

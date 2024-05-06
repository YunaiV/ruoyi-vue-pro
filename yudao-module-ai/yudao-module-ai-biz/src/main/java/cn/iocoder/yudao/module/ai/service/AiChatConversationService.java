package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.vo.AiChatConversationCreateRoleReq;
import cn.iocoder.yudao.module.ai.vo.AiChatConversationCreateUserReq;
import cn.iocoder.yudao.module.ai.vo.AiChatConversationListReq;
import cn.iocoder.yudao.module.ai.vo.AiChatConversationRes;

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
    AiChatConversationRes createConversation(AiChatConversationCreateUserReq req);

    /**
     * 对话 - 创建role对话
     *
     * @param req
     * @return
     */
    AiChatConversationRes createRoleConversation(AiChatConversationCreateRoleReq req);


    /**
     * 获取 - 对话
     *
     * @param id
     * @return
     */
    AiChatConversationRes getConversation(Long id);

    /**
     * 获取 - 对话列表
     *
     * @param req
     * @return
     */
    List<AiChatConversationRes> listConversation(AiChatConversationListReq req);

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

package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateRes;
import cn.iocoder.yudao.module.ai.vo.ChatConversationListReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationRes;

import java.util.List;

/**
 * chat 对话
 *
 * @fansili
 * @since v1.0
 */
public interface ChatConversationService {

    /**
     * 对话 - 创建
     *
     * @param req
     * @return
     */
    ChatConversationRes create(ChatConversationCreateReq req);

    /**
     * 获取 - 对话
     *
     * @param id
     * @return
     */
    ChatConversationRes getConversation(Long id);

    /**
     * 获取 - 对话列表
     *
     * @param req
     * @return
     */
    List<ChatConversationRes> listConversation(ChatConversationListReq req);

    /**
     * 删除 - 根据id
     *
     * @param id
     */
    void delete(Long id);
}

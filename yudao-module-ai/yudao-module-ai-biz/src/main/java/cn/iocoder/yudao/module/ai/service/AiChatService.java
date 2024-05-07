package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;

import java.util.List;

/**
 * 聊天 chat
 *
 * @author fansili
 * @time 2024/4/14 15:55
 * @since 1.0
 */
public interface AiChatService {

    /**
     * chat
     *
     * @param sendReqVO
     * @return
     */
    AiChatMessageRespVO chat(AiChatMessageSendReqVO sendReqVO);

    /**
     * chat stream
     *
     * @param req
     * @param sseEmitter
     * @return
     */
    void chatStream(AiChatMessageSendReqVO req, Utf8SseEmitter sseEmitter);

    /**
     * 获取 - 获取对话 message list
     *
     * @param conversationId
     * @return
     */
    List<AiChatMessageRespVO> getMessageListByConversationId(Long conversationId);

    /**
     * 删除 - 删除message
     *
     * @param id
     * @return
     */
    Boolean deleteMessage(Long id);

}

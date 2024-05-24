package cn.iocoder.yudao.module.ai.service.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 聊天消息 Service 接口
 *
 * @author fansili
 */
public interface AiChatMessageService {

    /**
     * 发送消息
     *
     * @param sendReqVO 发送信息
     * @return 发送结果
     */
    AiChatMessageRespVO sendMessage(AiChatMessageSendReqVO sendReqVO);

    /**
     * 发送消息
     *
     * @param sendReqVO 发送信息
     * @param userId 用户编号
     * @return 发送结果
     */
    Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO, Long userId);

    /**
     * 获得指定会话的消息列表
     *
     * @param conversationId 会话编号
     * @return 消息列表
     */
    List<AiChatMessageDO> getChatMessageListByConversationId(Long conversationId);

    /**
     * 删除消息
     *
     * @param id 消息编号
     * @param userId 用户编号
     */
    void deleteMessage(Long id, Long userId);

    /**
     * 删除指定会话的消息
     *
     * @param conversationId 会话编号
     * @param userId 用户编号
     */
    void deleteChatMessageByConversationId(Long conversationId, Long userId);

}

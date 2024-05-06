package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;

/**
 * chat message
 *
 * @author fansili
 * @time 2024/4/24 17:25
 * @since 1.0
 */
public interface AiChatMessageService {

    /**
     * message - 列表
     *
     * @param req
     * @return
     */
    PageResult<AiChatMessageRespVO> list(AiChatMessageReq req);

    /**
     * message - 删除
     *
     * @param chatConversationId
     * @param id
     */
    void delete(Long chatConversationId, Long id);
}

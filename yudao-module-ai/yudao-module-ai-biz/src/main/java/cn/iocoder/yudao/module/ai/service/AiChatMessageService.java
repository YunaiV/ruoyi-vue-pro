package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.vo.AiChatMessageListRes;
import cn.iocoder.yudao.module.ai.vo.AiChatMessageReq;

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
    PageResult<AiChatMessageListRes> list(AiChatMessageReq req);

    /**
     * message - 删除
     *
     * @param chatConversationId
     * @param id
     */
    void delete(Long chatConversationId, Long id);
}

package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.ChatMessageService;
import cn.iocoder.yudao.module.ai.vo.ChatMessageListRes;
import cn.iocoder.yudao.module.ai.vo.ChatMessageReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * chat message
 *
 * @author fansili
 * @time 2024/4/24 17:25
 * @since 1.0
 */
@AllArgsConstructor
@Service
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatConversationMapper aiChatConversationMapper;

    @Override
    public PageResult<ChatMessageListRes> list(ChatMessageReq req) {
        return null;
    }

    @Override
    public void delete(Long chatConversationId, Long id) {
        // 获取登录用户
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 校验 ChatConversation
        validateChatConversation(chatConversationId, loginUserId);
        // 删除
        aiChatMessageMapper.deleteByConversationAndId(chatConversationId, id);
    }

    private AiChatConversationDO validateChatConversation(Long chatConversationId, Long loginUserId) {
        AiChatConversationDO aiChatConversationDO = aiChatConversationMapper.selectById(chatConversationId);
        if (aiChatConversationDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_CONTINUE_NOT_EXIST);
        }
        if (!aiChatConversationDO.getUserId().equals(loginUserId)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_CONVERSATION_NOT_YOURS);
        }
        return aiChatConversationDO;
    }
}

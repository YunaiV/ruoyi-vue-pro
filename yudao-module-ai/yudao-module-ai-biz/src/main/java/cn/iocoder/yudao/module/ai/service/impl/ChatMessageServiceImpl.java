package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.convert.ChatMessageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.ChatMessageService;
import cn.iocoder.yudao.module.ai.vo.ChatMessageListRes;
import cn.iocoder.yudao.module.ai.vo.ChatMessageReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // 查询
        LambdaQueryWrapperX<AiChatMessageDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(AiChatMessageDO::getChatConversationId, req.getChatConversationId());
        // 默认排序
        queryWrapperX.orderByDesc(AiChatMessageDO::getId);
        PageResult<AiChatMessageDO> pageResult = aiChatMessageMapper.selectPage(req, queryWrapperX);
        // 转换 res
        List<ChatMessageListRes> messageListResList = ChatMessageConvert.INSTANCE.convert(pageResult.getList());
        return new PageResult(messageListResList, pageResult.getTotal());
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

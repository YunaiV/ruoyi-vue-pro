package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.convert.AiChatMessageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.AiChatMessageService;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
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
public class AiChatMessageServiceImpl implements AiChatMessageService {

    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatConversationMapper aiChatConversationMapper;

    @Override
    public PageResult<AiChatMessageRespVO> list(AiChatMessageReq req) {
        // 查询
        LambdaQueryWrapperX<AiChatMessageDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(AiChatMessageDO::getConversationId, req.getChatConversationId());
        // 默认排序
        queryWrapperX.orderByDesc(AiChatMessageDO::getId);
        PageResult<AiChatMessageDO> pageResult = aiChatMessageMapper.selectPage(req, queryWrapperX);
        // 转换 res
        List<AiChatMessageRespVO> messageListResList = AiChatMessageConvert.INSTANCE.convert(pageResult.getList());
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

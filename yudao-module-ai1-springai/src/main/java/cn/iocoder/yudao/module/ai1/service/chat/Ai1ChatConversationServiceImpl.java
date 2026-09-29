package cn.iocoder.yudao.module.ai1.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation.Ai1ChatConversationUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatConversationMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * AI1 对话 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1ChatConversationServiceImpl implements Ai1ChatConversationService {

    @Resource
    private Ai1ChatConversationMapper chatConversationMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1AgentService agentService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private Ai1ChatMessageService chatMessageService;

    @Override
    public Long createChatConversationMy(Long userId, Ai1ChatConversationCreateMyReqVO createReqVO) {
        // 1. 校验 Agent 存在且已开启
        agentService.validateAgentEnabled(createReqVO.getAgentId());

        // 2. 插入：首条消息发送后，标题自动替换为提问内容
        Ai1ChatConversationDO conversation = new Ai1ChatConversationDO().setAgentId(createReqVO.getAgentId())
                .setUserId(userId).setTitle(Ai1ChatConversationDO.TITLE_DEFAULT);
        chatConversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public void updateChatConversationMy(Long userId, Ai1ChatConversationUpdateMyReqVO updateReqVO) {
        // 1. 校验归属
        validateChatConversationMy(userId, updateReqVO.getId());

        // 2. 更新标题
        updateChatConversationTitle(updateReqVO.getId(), updateReqVO.getTitle().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatConversationMy(Long userId, Long id) {
        // 1. 校验归属
        validateChatConversationMy(userId, id);

        // 2. 删除对话与消息
        chatConversationMapper.deleteById(id);
        chatMessageService.deleteChatMessageListByConversationIds(Collections.singletonList(id));
    }

    @Override
    public Ai1ChatConversationDO validateChatConversationMy(Long userId, Long id) {
        Ai1ChatConversationDO conversation = chatConversationMapper.selectById(id);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

    @Override
    public List<Ai1ChatConversationDO> getChatConversationListByAgentIdAndUserId(Long agentId, Long userId) {
        return chatConversationMapper.selectListByAgentIdAndUserId(agentId, userId);
    }

    @Override
    public PageResult<Ai1ChatConversationDO> getChatConversationPage(Ai1ChatConversationPageReqVO pageReqVO) {
        return chatConversationMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateChatConversationTitle(Long id, String title) {
        chatConversationMapper.updateById(new Ai1ChatConversationDO().setId(id).setTitle(title));
    }

    @Override
    public void touchChatConversation(Long id) {
        chatConversationMapper.updateById(new Ai1ChatConversationDO().setId(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatConversationListByAgentIds(Collection<Long> agentIds) {
        // 1. 查询 Agent 下的全部对话编号
        if (CollUtil.isEmpty(agentIds)) {
            return;
        }
        List<Long> conversationIds = convertList(chatConversationMapper.selectListByAgentIds(agentIds), Ai1ChatConversationDO::getId);
        if (CollUtil.isEmpty(conversationIds)) {
            return;
        }

        // 2. 删除对话与消息
        chatConversationMapper.deleteByIds(conversationIds);
        chatMessageService.deleteChatMessageListByConversationIds(conversationIds);
    }

}

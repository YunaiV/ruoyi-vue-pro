package cn.iocoder.yudao.module.ai1.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.message.Ai1ChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatConversationDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.chat.Ai1ChatMessageDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.chat.Ai1ChatMessageMapper;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageRoleEnum;
import cn.iocoder.yudao.module.ai1.enums.chat.Ai1ChatMessageStatusEnum;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.tool.chat.Ai1ChatStreamTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CHAT_MESSAGE_NOT_EXISTS;

/**
 * AI1 对话消息 Service 实现类
 *
 * 流式生成与下发委托 {@link Ai1ChatStreamTool}；本类只负责会话校验与消息落库
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class Ai1ChatMessageServiceImpl implements Ai1ChatMessageService {

    // TODO DONE @AI：这个放到 DO 里？conversationdo 里？
    // 对话标题最大长度定义在 Ai1ChatConversationDO#TITLE_MAX_LENGTH

    @Resource
    private Ai1ChatMessageMapper chatMessageMapper;

    @Resource
    private Ai1ChatConversationService chatConversationService;
    @Resource
    private Ai1AgentService agentService;

    @Resource
    private Ai1ChatStreamTool chatStreamTool;

    @Override
    public List<Ai1ChatMessageDO> getChatMessageListByConversationId(Long conversationId) {
        return chatMessageMapper.selectListByConversationId(conversationId);
    }

    @Override
    public List<Ai1ChatMessageDO> getChatMessageListByConversationIdAndIdLessThan(Long conversationId, Long id, Integer limit) {
        return chatMessageMapper.selectListByConversationIdAndIdLessThan(conversationId, id, limit);
    }

    @Override
    public SseEmitter sendChatMessageStream(Long userId, Ai1ChatMessageSendReqVO sendReqVO) {
        try {
            // 1. 校验对话归属、Agent 存在且已开启
            Ai1ChatConversationDO conversation = chatConversationService.validateChatConversationMy(userId, sendReqVO.getConversationId());
            agentService.validateAgentEnabled(conversation.getAgentId());

            // 2. 同一事务内落库用户消息与助手占位
            Long messageId = getSelf().createRoundMessages(conversation, sendReqVO.getContent());

            // 3. 投递生成任务，并打开 SSE 连接
            // TODO DONE @AI：是不是租户 id 在 submit 里处理哈？
            // TODO DONE @AI：然后里面 TenantContextHolder.getRequiredTenantId() 非绝对的，不然关闭租户不好兼容噢；
            // 租户由 submit 从当前上下文可选获取，关闭多租户时同样可用
            chatStreamTool.submit(messageId, conversation.getAgentId(), conversation.getId(), sendReqVO.getContent());
            return chatStreamTool.open(messageId, null);
        } catch (ServiceException e) {
            return chatStreamTool.error(e.getMessage());
        } catch (Exception e) {
            log.warn("[sendChatMessageStream][用户({}) 对话({}) 提交失败]", userId, sendReqVO.getConversationId(), e);
            return chatStreamTool.error("对话提交失败，请稍后重试");
        }
    }

    @Override
    public SseEmitter resumeChatMessageStream(Long userId, Long messageId, String lastEventId) {
        try {
            // 1. 校验消息存在、为助手消息，且所属对话归属当前用户
            Ai1ChatMessageDO message = chatMessageMapper.selectById(messageId);
            if (message == null || !Ai1ChatMessageRoleEnum.isAssistant(message.getRole())) {
                throw exception(CHAT_MESSAGE_NOT_EXISTS);
            }
            chatConversationService.validateChatConversationMy(userId, message.getConversationId());

            // TODO DONE @AI：这里写个方法注释；ps：不要“通过后才读取 Redis 结果流”里的 Redis，这样注释和实现太耦合了。。。后续不好替换 redis 呀；
            // 2. 校验通过后，从 lastEventId 之后续传结果流，不重新生成
            return chatStreamTool.open(messageId, lastEventId);
        } catch (ServiceException e) {
            return chatStreamTool.error(e.getMessage());
        }
    }

    /**
     * 开启一轮对话：首条消息自动生成标题 → 落库用户消息（完成）与助手占位（生成中）→ 刷新对话活跃时间
     *
     * @param conversation 已校验归属的对话
     * @param content      提问内容
     * @return 助手消息编号，即结果流标识
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRoundMessages(Ai1ChatConversationDO conversation, String content) {
        // 1. 首条消息时，使用提问内容作为标题（超长截断）
        if (Ai1ChatConversationDO.TITLE_DEFAULT.equals(conversation.getTitle())
                && chatMessageMapper.selectCountByConversationId(conversation.getId()) == 0) {
            chatConversationService.updateChatConversationTitle(conversation.getId(),
                    StrUtil.maxLength(content.trim(), Ai1ChatConversationDO.TITLE_MAX_LENGTH - 3));
        }

        // 2. 用户消息 + 助手占位；刷新页面后，前端按生成中的助手消息编号续传
        chatMessageMapper.insert(new Ai1ChatMessageDO().setConversationId(conversation.getId())
                .setRole(Ai1ChatMessageRoleEnum.USER.getRole()).setContent(content)
                .setStatus(Ai1ChatMessageStatusEnum.SUCCESS.getStatus()));
        Ai1ChatMessageDO assistantMessage = new Ai1ChatMessageDO().setConversationId(conversation.getId())
                .setRole(Ai1ChatMessageRoleEnum.ASSISTANT.getRole()).setContent("")
                .setStatus(Ai1ChatMessageStatusEnum.GENERATING.getStatus());
        chatMessageMapper.insert(assistantMessage);

        // 3. 刷新对话活跃时间
        chatConversationService.touchChatConversation(conversation.getId());
        return assistantMessage.getId();
    }

    @Override
    public void updateChatMessage(Ai1ChatMessageDO updateObj) {
        chatMessageMapper.updateById(updateObj);
    }

    @Override
    public void deleteChatMessageListByConversationIds(Collection<Long> conversationIds) {
        if (CollUtil.isEmpty(conversationIds)) {
            return;
        }
        chatMessageMapper.deleteByConversationIds(conversationIds);
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     */
    private Ai1ChatMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}

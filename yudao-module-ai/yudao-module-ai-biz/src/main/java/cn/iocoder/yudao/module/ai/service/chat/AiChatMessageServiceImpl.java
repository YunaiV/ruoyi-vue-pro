package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.AiKnowledgeSegmentSearchReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnum;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.CHAT_MESSAGE_NOT_EXIST;

/**
 * AI 聊天消息 Service 实现类
 *
 * @author fansili
 */
@Service
@Slf4j
public class AiChatMessageServiceImpl implements AiChatMessageService {

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatModelService chatModalService;
    @Resource
    private AiApiKeyService apiKeyService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageSendRespVO sendMessage(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService.validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiChatModelDO model = chatModalService.validateChatModel(conversation.getModelId());
        ChatModel chatModel = apiKeyService.getChatModel(model.getKeyId());

        // 2. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext());

        // 3.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext());

        // 3.2 召回段落
        List<AiKnowledgeSegmentDO> segmentList = recallSegment(sendReqVO.getContent(), conversation.getKnowledgeId());

        // 3.3 创建 chat 需要的 Prompt
        Prompt prompt = buildPrompt(conversation, historyMessages, segmentList, model, sendReqVO);
        ChatResponse chatResponse = chatModel.call(prompt);

        // 3.4 段式返回
        String newContent = chatResponse.getResult().getOutput().getContent();
        chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setSegmentIds(convertList(segmentList, AiKnowledgeSegmentDO::getId)).setContent(newContent));
        return new AiChatMessageSendRespVO().setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class).setContent(newContent));
    }

    @Override
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService.validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiChatModelDO model = chatModalService.validateChatModel(conversation.getModelId());
        StreamingChatModel chatModel = apiKeyService.getChatModel(model.getKeyId());

        // 2. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext());

        // 3.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext());


        // 3.2 召回段落
        List<AiKnowledgeSegmentDO> segmentList = recallSegment(sendReqVO.getContent(), conversation.getKnowledgeId());

        // 3.3 构建 Prompt，并进行调用
        Prompt prompt = buildPrompt(conversation, historyMessages, segmentList, model, sendReqVO);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 3.4 流式返回
        // TODO 注意：Schedulers.immediate() 目的是，避免默认 Schedulers.parallel() 并发消费 chunk 导致 SSE 响应前端会乱序问题
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return success(new AiChatMessageSendRespVO().setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                    .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class).setContent(newContent)));
        }).doOnComplete(() -> {
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setSegmentIds(convertList(segmentList, AiKnowledgeSegmentDO::getId))
                            .setContent(contentBuffer.toString())));
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(throwable.getMessage())));
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.CHAT_STREAM_ERROR)));
    }

    private List<AiKnowledgeSegmentDO> recallSegment(String content, Long knowledgeId) {
        if (Objects.isNull(knowledgeId)) {
            return Collections.emptyList();
        }
        return knowledgeSegmentService.similaritySearch(new AiKnowledgeSegmentSearchReqVO().setKnowledgeId(knowledgeId).setContent(content));
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages,List<AiKnowledgeSegmentDO> segmentList,
                               AiChatModelDO model, AiChatMessageSendReqVO sendReqVO) {
        // 1. 构建 Prompt Message 列表
        List<Message> chatMessages = new ArrayList<>();

        // 1.1 召回内容消息构建
        if (CollUtil.isNotEmpty(segmentList)) {
            PromptTemplate promptTemplate = new PromptTemplate(AiChatRoleEnum.AI_KNOWLEDGE_ROLE.getSystemMessage());
            StringBuilder infoBuilder = StrUtil.builder();
            segmentList.forEach(segment -> infoBuilder.append(System.lineSeparator()).append(segment.getContent()));
            Message message = promptTemplate.createMessage(Map.of("info", infoBuilder.toString()));
            chatMessages.add(message);
        }

        // 1.2 system context 角色设定
        if (StrUtil.isNotBlank(conversation.getSystemMessage())) {
            chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        }
        // 1.3 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages.forEach(message -> chatMessages.add(AiUtils.buildMessage(message.getType(), message.getContent())));
        // 1.4 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));

        // 2. 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(),
                conversation.getTemperature(), conversation.getMaxTokens());
        return new Prompt(chatMessages, chatOptions);
    }

    /**
     * 从历史消息中，获得倒序的 n 组消息作为消息上下文
     * <p>
     * n 组：指的是 user + assistant 形成一组
     *
     * @param messages     消息列表
     * @param conversation 对话
     * @param sendReqVO    发送请求
     * @return 消息上下文
     */
    private List<AiChatMessageDO> filterContextMessages(List<AiChatMessageDO> messages,
                                                        AiChatConversationDO conversation,
                                                        AiChatMessageSendReqVO sendReqVO) {
        if (conversation.getMaxContexts() == null || ObjUtil.notEqual(sendReqVO.getUseContext(), Boolean.TRUE)) {
            return Collections.emptyList();
        }
        List<AiChatMessageDO> contextMessages = new ArrayList<>(conversation.getMaxContexts() * 2);
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiChatMessageDO assistantMessage = CollUtil.get(messages, i);
            if (assistantMessage == null || assistantMessage.getReplyId() == null) {
                continue;
            }
            AiChatMessageDO userMessage = CollUtil.get(messages, i - 1);
            if (userMessage == null || ObjUtil.notEqual(assistantMessage.getReplyId(), userMessage.getId())
                    || StrUtil.isEmpty(assistantMessage.getContent())) {
                continue;
            }
            // 由于后续要 reverse 反转，所以先添加 assistantMessage
            contextMessages.add(assistantMessage);
            contextMessages.add(userMessage);
            // 超过最大上下文，结束
            if (contextMessages.size() >= conversation.getMaxContexts() * 2) {
                break;
            }
        }
        Collections.reverse(contextMessages);
        return contextMessages;
    }

    private AiChatMessageDO createChatMessage(Long conversationId, Long replyId,
                                              AiChatModelDO model, Long userId, Long roleId,
                                              MessageType messageType, String content, Boolean useContext) {
        AiChatMessageDO message = new AiChatMessageDO().setConversationId(conversationId).setReplyId(replyId)
                .setModel(model.getModel()).setModelId(model.getId()).setUserId(userId).setRoleId(roleId)
                .setType(messageType.getValue()).setContent(content).setUseContext(useContext);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<AiChatMessageDO> getChatMessageListByConversationId(Long conversationId) {
        return chatMessageMapper.selectListByConversationId(conversationId);
    }

    @Override
    public void deleteChatMessage(Long id, Long userId) {
        // 1. 校验消息存在
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null || ObjUtil.notEqual(message.getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteById(id);
    }

    @Override
    public void deleteChatMessageByConversationId(Long conversationId, Long userId) {
        // 1. 校验消息存在
        List<AiChatMessageDO> messages = chatMessageMapper.selectListByConversationId(conversationId);
        if (CollUtil.isEmpty(messages) || ObjUtil.notEqual(messages.get(0).getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteBatchIds(convertList(messages, AiChatMessageDO::getId));
    }

    @Override
    public void deleteChatMessageByAdmin(Long id) {
        // 1. 校验消息存在
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteById(id);
    }

    @Override
    public Map<Long, Integer> getChatMessageCountMap(Collection<Long> conversationIds) {
        return chatMessageMapper.selectCountMapByConversationId(conversationIds);
    }

    @Override
    public PageResult<AiChatMessageDO> getChatMessagePage(AiChatMessagePageReqVO pageReqVO) {
        return chatMessageMapper.selectPage(pageReqVO);
    }

}

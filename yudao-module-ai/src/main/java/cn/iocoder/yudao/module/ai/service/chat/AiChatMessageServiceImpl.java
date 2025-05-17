package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiToolDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.model.AiToolService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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

    /**
     * 知识库转 {@link UserMessage} 的内容模版
     */
    private static final String KNOWLEDGE_USER_MESSAGE_TEMPLATE = "使用 <Reference></Reference> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" + // 多个 <Reference></Reference> 的拼接
            "回答要求：\n- 避免提及你是从 <Reference></Reference> 获取的知识。";

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiModelService modalService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    private AiKnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private AiToolService toolService;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageSendRespVO sendMessage(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        ChatModel chatModel = modalService.getChatModel(model.getId());

        // 2. 知识库找回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(sendReqVO.getContent(), conversation);

        // 3. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                null);

        // 3.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                knowledgeSegments);

        // 3.2 创建 chat 需要的 Prompt
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, model, sendReqVO);
        ChatResponse chatResponse = chatModel.call(prompt);

        // 3.3 更新响应内容
        String newContent = chatResponse.getResult().getOutput().getText();
        chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(newContent));
        // 3.4 响应结果
        Map<Long, AiKnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId));
        List<AiChatMessageRespVO.KnowledgeSegment> segments = BeanUtils.toBean(knowledgeSegments,
                AiChatMessageRespVO.KnowledgeSegment.class, segment -> {
                    AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                    segment.setDocumentName(document != null ? document.getName() : null);
                });
        return new AiChatMessageSendRespVO()
                .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                        .setContent(newContent).setSegments(segments));
    }

    @Override
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO,
            Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        StreamingChatModel chatModel = modalService.getChatModel(model.getId());

        // 2. 知识库找回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(sendReqVO.getContent(),
                conversation);

        // 3. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                null);

        // 4.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                knowledgeSegments);

        // 4.2 构建 Prompt，并进行调用
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, model, sendReqVO);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 4.3 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            // 处理知识库的返回，只有首次才有
            List<AiChatMessageRespVO.KnowledgeSegment> segments = null;
            if (StrUtil.isEmpty(contentBuffer)) {
                Map<Long, AiKnowledgeDocumentDO> documentMap = TenantUtils.executeIgnore(() ->
                        knowledgeDocumentService.getKnowledgeDocumentMap(
                                convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId)));
                segments = BeanUtils.toBean(knowledgeSegments, AiChatMessageRespVO.KnowledgeSegment.class, segment ->  {
                    AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                    segment.setDocumentName(document != null ? document.getName() : null);
                });
            }
            // 响应结果
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getText() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            return success(new AiChatMessageSendRespVO()
                    .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                    .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                            .setContent(newContent).setSegments(segments)));
        }).doOnComplete(() -> {
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() -> chatMessageMapper.updateById(
                    new AiChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString())));
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() -> chatMessageMapper.updateById(
                    new AiChatMessageDO().setId(assistantMessage.getId()).setContent(throwable.getMessage())));
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.CHAT_STREAM_ERROR)));
    }

    private List<AiKnowledgeSegmentSearchRespBO> recallKnowledgeSegment(String content,
            AiChatConversationDO conversation) {
        // 1. 查询聊天角色
        if (conversation == null || conversation.getRoleId() == null) {
            return Collections.emptyList();
        }
        AiChatRoleDO role = chatRoleService.getChatRole(conversation.getRoleId());
        if (role == null || CollUtil.isEmpty(role.getKnowledgeIds())) {
            return Collections.emptyList();
        }

        // 2. 遍历找回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = new ArrayList<>();
        for (Long knowledgeId : role.getKnowledgeIds()) {
            knowledgeSegments.addAll(knowledgeSegmentService.searchKnowledgeSegment(new AiKnowledgeSegmentSearchReqBO()
                    .setKnowledgeId(knowledgeId).setContent(content)));
        }
        return knowledgeSegments;
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages,
                               List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments,
                               AiModelDO model, AiChatMessageSendReqVO sendReqVO) {
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 System Context 角色设定
        if (StrUtil.isNotBlank(conversation.getSystemMessage())) {
            chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        }

        // 1.2 历史 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages
                .forEach(message -> chatMessages.add(AiUtils.buildMessage(message.getType(), message.getContent())));

        // 1.3 当前 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));

        // 1.4 知识库，通过 UserMessage 实现
        if (CollUtil.isNotEmpty(knowledgeSegments)) {
            String reference = knowledgeSegments.stream()
                    .map(segment -> "<Reference>" + segment.getContent() + "</Reference>")
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(KNOWLEDGE_USER_MESSAGE_TEMPLATE, reference)));
        }

        // 2.1 查询 tool 工具
        Set<String> toolNames = null;
        Map<String,Object> toolContext = Map.of();
        if (conversation.getRoleId() != null) {
            AiChatRoleDO chatRole = chatRoleService.getChatRole(conversation.getRoleId());
            if (chatRole != null && CollUtil.isNotEmpty(chatRole.getToolIds())) {
                toolNames = convertSet(toolService.getToolList(chatRole.getToolIds()), AiToolDO::getName);
                toolContext = AiUtils.buildCommonToolContext();
            }
        }
        // 2.2 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(),
                conversation.getTemperature(), conversation.getMaxTokens(), toolNames, toolContext);
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
            if (userMessage == null
                    || ObjUtil.notEqual(assistantMessage.getReplyId(), userMessage.getId())
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
            AiModelDO model, Long userId, Long roleId,
            MessageType messageType, String content, Boolean useContext,
            List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments) {
        AiChatMessageDO message = new AiChatMessageDO().setConversationId(conversationId).setReplyId(replyId)
                .setModel(model.getModel()).setModelId(model.getId()).setUserId(userId).setRoleId(roleId)
                .setType(messageType.getValue()).setContent(content).setUseContext(useContext)
                .setSegmentIds(convertList(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getId));
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

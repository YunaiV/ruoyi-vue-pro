package cn.iocoder.yudao.module.ai.service.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactory;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.AI_CHAT_MESSAGE_NOT_EXIST;
import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

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
    private AiClientFactory clientFactory;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatModelService chatModalService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private AdminUserApi adminUserApi;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageRespVO sendMessage(AiChatMessageSendReqVO req) {
         return null; // TODO 芋艿：一起改
//        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
//        // 查询对话
//        AiChatConversationDO conversation = chatConversationService.validateExists(req.getConversationId());
//        // 获取对话模型
//        AiChatModelDO chatModel = chatModalService.validateChatModel(conversation.getModelId());
//        // 获取角色信息
//        AiChatRoleDO chatRoleDO = conversation.getRoleId() != null ? chatRoleService.validateChatRole(conversation.getRoleId()) : null;
//        // 获取 client 类型
//        AiPlatformEnum platformEnum = AiPlatformEnum.validatePlatform(chatModel.getPlatform());
//        // 保存 chat message
//        createChatMessage(conversation.getId(), MessageType.USER, loginUserId, conversation.getRoleId(),
//                chatModel.getModel(), chatModel.getId(), req.getContent());
//        String content = null;
//        int tokens = 0;
//        try {
//            // 创建 chat 需要的 Prompt
//            Prompt prompt = new Prompt(req.getContent());
//            // TODO @芋艿 @范 看要不要支持这些
////            req.setTopK(req.getTopK());
////            req.setTopP(req.getTopP());
////            req.setTemperature(req.getTemperature());
//            // 发送 call 调用
//            ChatClient chatClient = chatClientFactory.getChatClient(platformEnum);
//            ChatResponse call = chatClient.call(prompt);
//            content = call.getResult().getOutput().getContent();
//            // 更新 conversation
//        } catch (Exception e) {
//            content = ExceptionUtil.getMessage(e);
//        } finally {
//            // 保存 chat message
//            createChatMessage(conversation.getId(), MessageType.SYSTEM, loginUserId, conversation.getRoleId(),
//                    chatModel.getModel(), chatModel.getId(), content);
//        }
//        return new AiChatMessageRespVO().setContent(content);
    }

    @Override
    public Flux<AiChatMessageSendRespVO> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService.validateExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS); // TODO 芋艿：异常情况的对接；
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiChatModelDO model = chatModalService.validateChatModel(conversation.getModelId());
        StreamingChatClient chatClient = apiKeyService.getStreamingChatClient(model.getKeyId());
        // 1.3 获取用户头像、角色头像
        AdminUserRespDTO user = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId());
        AiChatRoleDO role = conversation.getRoleId() != null ? chatRoleService.getChatRole(conversation.getRoleId()) : null;

        // 2. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext());

        // 3.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext());

        // 3.2 创建 chat 需要的 Prompt
        Prompt prompt = buildPrompt(conversation, historyMessages, model, sendReqVO);
        Flux<ChatResponse> streamResponse = chatClient.stream(prompt);

        // 3.3 流式返回
        // 注意：Schedulers.immediate() 目的是，避免默认 Schedulers.parallel() 并发消费 chunk 导致 SSE 响应前端会乱序问题
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            AiChatMessageSendRespVO.Message send = BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class,
                    o -> o.setUserAvatar(user.getAvatar()));
            AiChatMessageSendRespVO.Message receive = BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class,
                            o -> o.setRoleAvatar(role != null ? role.getAvatar() : null)).setContent(newContent);
            return new AiChatMessageSendRespVO().setSend(send).setReceive(receive);
        }).doOnComplete(() -> {
            chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString()));
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
            chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(throwable.getMessage()));
        });
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages,
                               AiChatModelDO model, AiChatMessageSendReqVO sendReqVO) {
        // 1. 构建 Prompt Message 列表
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 system context 角色设定
        chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        // 1.2 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages.forEach(message -> chatMessages.add(new ChatMessage(message.getType().toUpperCase(), message.getContent())));
        // 1.3 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));

        // 2. 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = clientFactory.buildChatOptions(platform, model.getModel(),
                conversation.getTemperature(), conversation.getMaxTokens());
        return new Prompt(chatMessages, chatOptions);
    }

    /**
     * 从历史消息中，获得倒序的 n 组消息作为消息上下文
     *
     * n 组：指的是 user + assistant 形成一组
     *
     * @param messages 消息列表
     * @param conversation 会话
     * @param sendReqVO 发送请求
     * @return 消息上下文
     */
    private List<AiChatMessageDO> filterContextMessages(List<AiChatMessageDO> messages, AiChatConversationDO conversation, AiChatMessageSendReqVO sendReqVO) {
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
    public void deleteMessage(Long id, Long userId) {
        // 1. 校验消息存在
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null || ObjUtil.notEqual(message.getUserId(), userId)) {
            throw exception(AI_CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteById(id);
    }

    @Override
    public void deleteChatMessageByConversationId(Long conversationId, Long userId) {
        // 1. 校验消息存在
        List<AiChatMessageDO> messages = chatMessageMapper.selectListByConversationId(conversationId);
        if (CollUtil.isEmpty(messages) || ObjUtil.notEqual(messages.get(0).getUserId(), userId)) {
            throw exception(AI_CHAT_MESSAGE_NOT_EXIST);
        }
        // 2. 执行删除
        chatMessageMapper.deleteBatchIds(convertList(messages, AiChatMessageDO::getId));
    }

}

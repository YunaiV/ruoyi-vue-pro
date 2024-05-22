package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.config.AiChatClientFactory;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendRespVO;
import cn.iocoder.yudao.module.ai.convert.AiChatMessageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import cn.iocoder.yudao.module.ai.service.chat.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;

/**
 * 聊天 service
 *
 * @author fansili
 * @time 2024/4/14 15:55
 * @since 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiChatClientFactory chatClientFactory;

    private final AiChatMessageMapper chatMessageMapper;

    private final AiChatConversationService chatConversationService;
    private final AiChatModelService chatModalService;
    private final AiChatRoleService chatRoleService;
    private final AdminUserApi adminUserApi;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageRespVO chat(AiChatMessageSendReqVO req) {
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
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectByConversationId(conversation.getId());
        // 1.2 校验模型
        AiChatModelDO model = chatModalService.validateChatModel(conversation.getModelId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        StreamingChatClient chatClient = chatClientFactory.getStreamingChatClient(platform);

        // 2. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext());

        // 3.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext());

        // 3.2 创建 chat 需要的 Prompt
        Prompt prompt = buildPrompt(conversation, historyMessages, sendReqVO);
        Flux<ChatResponse> streamResponse = chatClient.stream(prompt);

        // 3.3 流式返回
        // 注意：Schedulers.immediate() 目的是，避免默认 Schedulers.parallel() 并发消费 chunk 导致 SSE 响应前端会乱序问题
        StringBuffer contentBuffer = new StringBuffer();

        // 3.4 获取用户头像、角色头像
        AdminUserRespDTO user = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId());
        AiChatRoleDO chatRole = chatRoleService.getChatRole(assistantMessage.getRoleId());
        return streamResponse.publishOn(Schedulers.immediate()).map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return new AiChatMessageSendRespVO().setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class).setUserAvatar(user.getAvatar()))
                    .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class).setContent(newContent).setRoleAvatar(chatRole == null ? null : chatRole.getAvatar()));
        }).doOnComplete(() -> {
            chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString()));
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
            chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId()).setContent(throwable.getMessage()));
        });
    }

    @Override
    public Boolean deleteByConversationId(Long conversationId) {
        return chatMessageMapper.deleteByConversationId(conversationId) > 0;
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages, AiChatMessageSendReqVO sendReqVO) {
        // 1. 构建 Prompt Message 列表
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 system context 角色设定
        chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        // 1.2 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages.forEach(message -> chatMessages.add(new ChatMessage(message.getType().toUpperCase(), message.getContent())));
        // 1.3 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));

        // 2. 构建 ChatOptions 对象 TODO 芋艿：临时注释掉；等文心一言兼容了；
        // TODO 每一轮 token 数量
//        ChatOptions chatOptions = ChatOptionsBuilder.builder().withTemperature(conversation.getTemperature().floatValue()).build();
//        return new Prompt(chatMessages, null);
        return new Prompt(chatMessages);
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
    public List<AiChatMessageRespVO> getMessageListByConversationId(Long conversationId) {
        // 校验对话是否存在
        chatConversationService.validateExists(conversationId);
        // 获取对话所有 message
        List<AiChatMessageDO> aiChatMessageDOList = chatMessageMapper.selectByConversationId(conversationId);
        // 获取模型信息
        Set<Long> roleIds = aiChatMessageDOList.stream().map(AiChatMessageDO::getRoleId).collect(Collectors.toSet());
        List<AiChatRoleDO> roleList;
        if (!CollUtil.isEmpty(roleIds)) {
            roleList = chatRoleService.getChatRoles(roleIds);
        } else {
            roleList = Collections.emptyList();
        }
        Map<Long, AiChatRoleDO> roleMap = roleList.stream().collect(Collectors.toMap(AiChatRoleDO::getId, o -> o));
        // 转换 AiChatMessageRespVO
        List<AiChatMessageRespVO> aiChatMessageRespList = AiChatMessageConvert.INSTANCE.convertAiChatMessageRespVOList(aiChatMessageDOList);
        // 获取用户信息
        AdminUserRespDTO user = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId());
        // 设置用户头像 和 模型头像
        return aiChatMessageRespList.stream().map(item -> {
            // 设置 role 头像
            if (roleMap.containsKey(item.getRoleId())) {
                AiChatRoleDO role = roleMap.get(item.getRoleId());
                item.setRoleAvatar(role.getAvatar());
            }
            // 设置 user 头像
            if (user != null) {
                item.setUserAvatar(user.getAvatar());
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteMessage(Long id) {
        return chatMessageMapper.deleteById(id) > 0;
    }

}

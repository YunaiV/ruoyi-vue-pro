package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.chat.ChatClient;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.StreamingChatClient;
import cn.iocoder.yudao.framework.ai.chat.messages.MessageType;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.config.AiChatClientFactory;
import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRes;
import cn.iocoder.yudao.module.ai.convert.AiChatMessageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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

    private final AiChatClientFactory aiChatClientFactory;
    private final AiChatRoleMapper aiChatRoleMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatConversationMapper aiChatConversationMapper;
    private final AiChatConversationService chatConversationService;
    private final AiChatModalService aiChatModalService;
    private final AiChatRoleService aiChatRoleService;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageRespVO chat(AiChatMessageSendReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询对话
        AiChatConversationRespVO conversation = chatConversationService.getConversationOfValidate(req.getConversationId());
        // 获取对话模型
        AiChatModalRes chatModal = aiChatModalService.getChatModalOfValidate(conversation.getModelId());
        // 对话模型是否可用
        aiChatModalService.validateAvailable(chatModal);
        // 获取角色信息
        AiChatRoleDO aiChatRoleDO = null;
        if (conversation.getRoleId() != null) {
            aiChatRoleDO = aiChatRoleService.validateExists(conversation.getRoleId());
        }
        // 校验角色是否公开
        aiChatRoleService.validateIsPublic(aiChatRoleDO);
        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(chatModal.getModel());
        // 保存 chat message
        insertChatMessage(conversation.getId(), MessageType.USER, loginUserId, conversation.getRoleId(),
                chatModal.getModel(), chatModal.getId(), req.getContent(),
                null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
        String content = null;
        try {
            // 创建 chat 需要的 Prompt
            Prompt prompt = new Prompt(req.getContent());
            // TODO @芋艿 @范 看要不要支持这些
//            req.setTopK(req.getTopK());
//            req.setTopP(req.getTopP());
//            req.setTemperature(req.getTemperature());
            // 发送 call 调用
            ChatClient chatClient = aiChatClientFactory.getChatClient(platformEnum);
            ChatResponse call = chatClient.call(prompt);
            content = call.getResult().getOutput().getContent();
            // 更新 conversation

        } catch (Exception e) {
            content = ExceptionUtil.getMessage(e);
        } finally {
            // 保存 chat message
            insertChatMessage(conversation.getId(), MessageType.SYSTEM, loginUserId, conversation.getRoleId(),
                    chatModal.getModel(), chatModal.getId(), req.getContent(),
                    null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
        }
        return new AiChatMessageRespVO().setContent(content);
    }

    private AiChatMessageDO insertChatMessage(Long conversationId, MessageType messageType, Long loginUserId, Long roleId,
                                              String model, Long modelId, String content, Integer tokens, Double temperature,
                                              Integer maxTokens, Integer maxContexts) {
        AiChatMessageDO insertChatMessageDO = new AiChatMessageDO()
                .setId(null)
                .setConversationId(conversationId)
                .setType(messageType.getValue())
                .setUserId(loginUserId)
                .setRoleId(roleId)
                .setModel(model)
                .setModelId(modelId)
                .setContent(content)
                .setTokens(tokens)

                .setTemperature(temperature)
                .setMaxTokens(maxTokens)
                .setMaxContexts(maxContexts);
        // 增加 chat message 记录
        aiChatMessageMapper.insert(insertChatMessageDO);
        // chat count 先+1
        aiChatConversationMapper.updateIncrChatCount(conversationId);
        return insertChatMessageDO;
    }

    @Override
    public void chatStream(AiChatMessageSendReqVO req, Utf8SseEmitter sseEmitter) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询对话
        AiChatConversationRespVO conversation = chatConversationService.getConversationOfValidate(req.getConversationId());
        // 获取对话模型
        AiChatModalRes chatModal = aiChatModalService.getChatModalOfValidate(conversation.getModelId());
        // 对话模型是否可用
        aiChatModalService.validateAvailable(chatModal);
        // 获取角色信息
        AiChatRoleDO aiChatRoleDO = null;
        if (conversation.getRoleId() != null) {
            aiChatRoleDO = aiChatRoleService.validateExists(conversation.getRoleId());
        }
        // 校验角色是否公开
        aiChatRoleService.validateIsPublic(aiChatRoleDO);
        // 创建 chat 需要的 Prompt
        Prompt prompt = new Prompt(req.getContent());
//        req.setTopK(req.getTopK());
//        req.setTopP(req.getTopP());
//        req.setTemperature(req.getTemperature());
        // 保存 chat message
        // 保存 chat message
        insertChatMessage(conversation.getId(), MessageType.USER, loginUserId, conversation.getRoleId(),
                chatModal.getModel(), chatModal.getId(), req.getContent(),
                null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());

        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(chatModal.getModel());
        StreamingChatClient streamingChatClient = aiChatClientFactory.getStreamingChatClient(platformEnum);
        Flux<ChatResponse> streamResponse = streamingChatClient.stream(prompt);

        StringBuffer contentBuffer = new StringBuffer();
        streamResponse.subscribe(
                new Consumer<ChatResponse>() {
                    @Override
                    public void accept(ChatResponse chatResponse) {
                        String content = chatResponse.getResults().get(0).getOutput().getContent();
                        try {
                            contentBuffer.append(content);
                            sseEmitter.send(content, MediaType.APPLICATION_JSON);
                        } catch (IOException e) {
                            log.error("发送异常{}", ExceptionUtil.getMessage(e));
                            // 如果不是因为关闭而抛出异常，则重新连接
                            sseEmitter.completeWithError(e);
                        }
                    }
                },
                error -> {
                    //
                    log.error("subscribe错误 {}", ExceptionUtil.getMessage(error));
                },
                () -> {
                    log.info("发送完成!");
                    sseEmitter.complete();
                    // 保存 chat message
                    insertChatMessage(conversation.getId(), MessageType.SYSTEM, loginUserId, conversation.getRoleId(),
                            chatModal.getModel(), chatModal.getId(), req.getContent(),
                            null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());

                }
        );
    }

    @Override
    public List<AiChatMessageRespVO> getMessageListByConversationId(Long conversationId) {
        // 校验对话是否存在
        chatConversationService.validateExists(conversationId);
        // 获取对话所有 message
        List<AiChatMessageDO> aiChatMessageDOList = aiChatMessageMapper.selectByConversationId(conversationId);
        // 转换 AiChatMessageRespVO
       return AiChatMessageConvert.INSTANCE.convertAiChatMessageRespVOList(aiChatMessageDOList);
    }

    @Override
    public Boolean deleteMessage(Long id) {
        return aiChatMessageMapper.deleteById(id) > 0;
    }
}

package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.config.AiChatClientFactory;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation.AiChatConversationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageAddReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendStreamReqVO;
import cn.iocoder.yudao.module.ai.convert.AiChatMessageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.service.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatConversationMapper aiChatConversationMapper;
    private final AiChatConversationService chatConversationService;
    private final AiChatModelService aiChatModalService;
    private final AiChatRoleService chatRoleService;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageRespVO chat(AiChatMessageSendReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询对话
        AiChatConversationRespVO conversation = chatConversationService.getConversationOfValidate(req.getConversationId());
        // 获取对话模型
        AiChatModelDO chatModel = aiChatModalService.validateChatModel(conversation.getModelId());
        // 获取角色信息
        AiChatRoleDO chatRoleDO = conversation.getRoleId() != null ? chatRoleService.validateChatRole(conversation.getRoleId()) : null;
        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.validatePlatform(chatModel.getPlatform());
        // 保存 chat message
        insertChatMessage(conversation.getId(), MessageType.USER, loginUserId, conversation.getRoleId(),
                chatModel.getModel(), chatModel.getId(), req.getContent(),
                null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
        String content = null;
        int tokens = 0;
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
            tokens = call.getResults().size();
            // 更新 conversation
        } catch (Exception e) {
            content = ExceptionUtil.getMessage(e);
        } finally {
            // 保存 chat message
            insertChatMessage(conversation.getId(), MessageType.SYSTEM, loginUserId, conversation.getRoleId(),
                    chatModel.getModel(), chatModel.getId(), content,
                    tokens, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
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

    public Flux<AiChatMessageRespVO> chatStream(AiChatMessageSendStreamReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询提问的 message
        AiChatMessageDO aiChatMessageDO = aiChatMessageMapper.selectById(req.getId());
        if (aiChatMessageDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_MESSAGE_NOT_EXIST);
        }
        // 查询对话
        AiChatConversationRespVO conversation = chatConversationService.getConversationOfValidate(aiChatMessageDO.getConversationId());
        // 获取对话模型
        AiChatModelDO chatModel = aiChatModalService.validateChatModel(conversation.getModelId());
        // 获取角色信息
        AiChatRoleDO chatRoleDO = conversation.getRoleId() != null ? chatRoleService.validateChatRole(conversation.getRoleId()) : null;
        // 创建 chat 需要的 Prompt
        Prompt prompt = new Prompt(aiChatMessageDO.getContent());
        // 提前创建一个 system message
        AiChatMessageDO systemMessage = insertChatMessage(conversation.getId(), MessageType.SYSTEM, loginUserId, conversation.getRoleId(),
                chatModel.getModel(), chatModel.getId(), "",
                0, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
//        req.setTopK(req.getTopK());
//        req.setTopP(req.getTopP());
//        req.setTemperature(req.getTemperature());
        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.validatePlatform(chatModel.getPlatform());
        StreamingChatClient streamingChatClient = aiChatClientFactory.getStreamingChatClient(platformEnum);
        Flux<ChatResponse> streamResponse = streamingChatClient.stream(prompt);
        // 转换 flex AiChatMessageRespVO
        StringBuffer contentBuffer = new StringBuffer();
        AtomicInteger tokens = new AtomicInteger(0);
        return streamResponse.map(res -> {
                    AiChatMessageRespVO aiChatMessageRespVO =
                            AiChatMessageConvert.INSTANCE.convertAiChatMessageRespVO(systemMessage);
                    aiChatMessageRespVO.setContent(res.getResult().getOutput().getContent());
                    contentBuffer.append(res.getResult().getOutput().getContent());
                    tokens.incrementAndGet();
                    return aiChatMessageRespVO;
                }
        ).doOnComplete(new Runnable() {
            @Override
            public void run() {
                log.info("发送完成!");
                // 保存 chat message
                aiChatMessageMapper.updateById(new AiChatMessageDO()
                        .setId(systemMessage.getId())
                        .setContent(contentBuffer.toString())
                        .setTokens(tokens.get())
                );
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                log.error("发送错误 {}!", throwable.getMessage());
                // 更新错误信息
                aiChatMessageMapper.updateById(new AiChatMessageDO()
                        .setId(systemMessage.getId())
                        .setContent(throwable.getMessage())
                        .setTokens(tokens.get())
                );
            }
        });
    }

    @Override
    public AiChatMessageRespVO add(AiChatMessageAddReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询对话
        AiChatConversationRespVO conversation = chatConversationService.getConversationOfValidate(req.getConversationId());
        // 获取对话模型
        AiChatModelDO chatModel = aiChatModalService.validateChatModel(conversation.getModelId());
        AiChatMessageDO userMessage = insertChatMessage(conversation.getId(), MessageType.USER, loginUserId, conversation.getRoleId(),
                chatModel.getModel(), chatModel.getId(), req.getContent(),
                null, conversation.getTemperature(), conversation.getMaxTokens(), conversation.getMaxContexts());
       return AiChatMessageConvert.INSTANCE.convertAiChatMessageRespVO(userMessage);
    }

    @Override
    public List<AiChatMessageRespVO> getMessageListByConversationId(Long conversationId) {
        // 校验对话是否存在
        chatConversationService.validateExists(conversationId);
        // 获取对话所有 message
        List<AiChatMessageDO> aiChatMessageDOList = aiChatMessageMapper.selectByConversationId(conversationId);
        // 获取模型信息
        Set<Long> modalIds = aiChatMessageDOList.stream().map(AiChatMessageDO::getModelId).collect(Collectors.toSet());
        List<AiChatModelDO> modalList = aiChatModalService.getModalByIds(modalIds);
        Map<Long, AiChatModelDO> modalIdMap = modalList.stream().collect(Collectors.toMap(AiChatModelDO::getId, o -> o));
        // 转换 AiChatMessageRespVO
        List<AiChatMessageRespVO> aiChatMessageRespList = AiChatMessageConvert.INSTANCE.convertAiChatMessageRespVOList(aiChatMessageDOList);
        // 设置用户头像 和 模型头像 todo @芋艿 这里需要转换 用户头像、模型头像
        return aiChatMessageRespList.stream().map(item -> {
            if (modalIdMap.containsKey(item.getModelId())) {
//                modalIdMap.get(item.getModelId());
//                item.setModelImage()
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteMessage(Long id) {
        return aiChatMessageMapper.deleteById(id) > 0;
    }

}

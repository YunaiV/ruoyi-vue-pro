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
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import cn.iocoder.yudao.module.ai.vo.AiChatConversationRes;
import cn.iocoder.yudao.module.ai.vo.AiChatReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.IOException;
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

    /**
     * chat
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String chat(AiChatReq req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(req.getModal());
        // 获取对话信息
        AiChatConversationRes conversationRes = chatConversationService.getConversation(req.getConversationId());
        // 保存 chat message
        saveChatMessage(req, conversationRes, loginUserId);
        String content = null;
        try {
            // 创建 chat 需要的 Prompt
            Prompt prompt = new Prompt(req.getPrompt());
            req.setTopK(req.getTopK());
            req.setTopP(req.getTopP());
            req.setTemperature(req.getTemperature());
            // 发送 call 调用
            ChatClient chatClient = aiChatClientFactory.getChatClient(platformEnum);
            ChatResponse call = chatClient.call(prompt);
            content = call.getResult().getOutput().getContent();
            // 更新 conversation

        } catch (Exception e) {
            content = ExceptionUtil.getMessage(e);
        } finally {
            // 保存 chat message
            saveSystemChatMessage(req, conversationRes, loginUserId, content);
        }
        return content;
    }

    private void saveChatMessage(AiChatReq req, AiChatConversationRes conversationRes, Long loginUserId) {
        Long chatConversationId = conversationRes.getId();
        // 增加 chat message 记录
        aiChatMessageMapper.insert(
                new AiChatMessageDO()
                        .setId(null)
                        .setConversationId(chatConversationId)
                        .setUserId(loginUserId)
                        .setMessage(req.getPrompt())
                        .setMessageType(MessageType.USER.getValue())
                        .setTopK(req.getTopK())
                        .setTopP(req.getTopP())
                        .setTemperature(req.getTemperature())
        );
        // chat count 先+1
        aiChatConversationMapper.updateIncrChatCount(req.getConversationId());
    }

    public void saveSystemChatMessage(AiChatReq req, AiChatConversationRes conversationRes, Long loginUserId, String systemPrompts) {
        Long chatConversationId = conversationRes.getId();
        // 增加 chat message 记录
        aiChatMessageMapper.insert(
                new AiChatMessageDO()
                        .setId(null)
                        .setConversationId(chatConversationId)
                        .setUserId(loginUserId)
                        .setMessage(systemPrompts)
                        .setMessageType(MessageType.SYSTEM.getValue())
                        .setTopK(req.getTopK())
                        .setTopP(req.getTopP())
                        .setTemperature(req.getTemperature())
        );

        // chat count 先+1
        aiChatConversationMapper.updateIncrChatCount(req.getConversationId());
    }

    /**
     * chat stream
     *
     * @param req
     * @param sseEmitter
     * @return
     */
    @Override
    public void chatStream(AiChatReq req, Utf8SseEmitter sseEmitter) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 获取 client 类型
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(req.getModal());
        // 获取对话信息
        AiChatConversationRes conversationRes = chatConversationService.getConversation(req.getConversationId());
        // 创建 chat 需要的 Prompt
        Prompt prompt = new Prompt(req.getPrompt());
        req.setTopK(req.getTopK());
        req.setTopP(req.getTopP());
        req.setTemperature(req.getTemperature());
        // 保存 chat message
        saveChatMessage(req, conversationRes, loginUserId);
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
                    saveSystemChatMessage(req, conversationRes, loginUserId, contentBuffer.toString());
                }
        );
    }
}

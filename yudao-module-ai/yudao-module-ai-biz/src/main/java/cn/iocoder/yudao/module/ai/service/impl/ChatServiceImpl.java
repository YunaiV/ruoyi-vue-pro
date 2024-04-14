package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.messages.MessageType;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.config.AiClient;
import cn.iocoder.yudao.framework.common.exception.ServerException;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.dataobject.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dataobject.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dataobject.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.enums.AiClientNameEnum;
import cn.iocoder.yudao.module.ai.enums.ChatConversationTypeEnum;
import cn.iocoder.yudao.module.ai.enums.ChatTypeEnum;
import cn.iocoder.yudao.module.ai.mapper.AiChatConversationMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatMessageMapper;
import cn.iocoder.yudao.module.ai.mapper.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.ChatService;
import cn.iocoder.yudao.module.ai.vo.ChatReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

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
public class ChatServiceImpl implements ChatService {

    private final AiClient aiClient;
    private final AiChatRoleMapper aiChatRoleMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatConversationMapper aiChatConversationMapper;


    /**
     * chat
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String chat(ChatReq req) {
        // 获取 client 类型
        AiClientNameEnum clientNameEnum = AiClientNameEnum.valueOfName(req.getModal());
        // 获取 对话类型(新建还是继续)
        ChatConversationTypeEnum chatConversationTypeEnum = ChatConversationTypeEnum.valueOfType(req.getConversationType());

        AiChatConversationDO aiChatConversationDO;
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (ChatConversationTypeEnum.NEW == chatConversationTypeEnum) {
            // 创建一个新的对话
            aiChatConversationDO = createNewChatConversation(req, loginUserId);
        } else {
            // 继续对话
            if (req.getConversationId() == null) {
                throw new ServerException(ErrorCodeConstants.AI_CHAT_CONTINUE_CONVERSATION_ID_NOT_NULL);
            }
            aiChatConversationDO = aiChatConversationMapper.selectById(req.getConversationId());
        }

        String content;
        try {
            // 创建 chat 需要的 Prompt
            Prompt prompt = new Prompt(req.getPrompt());
            req.setTopK(req.getTopK());
            req.setTopP(req.getTopP());
            req.setTemperature(req.getTemperature());
            // 发送 call 调用
            ChatResponse call = aiClient.call(prompt, clientNameEnum.getName());
            content = call.getResult().getOutput().getContent();
        } catch (Exception e) {
            content = ExceptionUtil.getMessage(e);
        }

        // 增加 chat message 记录
        aiChatMessageMapper.insert(
                new AiChatMessageDO()
                        .setId(null)
                        .setChatConversationId(aiChatConversationDO.getId())
                        .setUserId(loginUserId)
                        .setMessage(req.getPrompt())
                        .setMessageType(MessageType.USER.getValue())
                        .setTopK(req.getTopK())
                        .setTopP(req.getTopP())
                        .setTemperature(req.getTemperature())
        );

        // chat count 先+1
        aiChatConversationMapper.updateIncrChatCount(req.getConversationId());
        return content;
    }

    private AiChatConversationDO createNewChatConversation(ChatReq req, Long loginUserId) {
        // 获取 chat 角色
        String chatRoleName = null;
        ChatTypeEnum chatTypeEnum = null;
        Long chatRoleId = req.getChatRoleId();
        if (req.getChatRoleId() != null) {
            AiChatRoleDO aiChatRoleDO = aiChatRoleMapper.selectById(chatRoleId);
            if (aiChatRoleDO == null) {
                throw new ServerException(ErrorCodeConstants.AI_CHAT_ROLE_NOT_EXISTENT);
            }
            chatTypeEnum = ChatTypeEnum.ROLE_CHAT;
            chatRoleName = aiChatRoleDO.getRoleName();
        } else {
            chatTypeEnum = ChatTypeEnum.USER_CHAT;
        }
        //
        AiChatConversationDO insertChatConversation = new AiChatConversationDO()
                .setId(null)
                .setUserId(loginUserId)
                .setChatRoleId(req.getChatRoleId())
                .setChatRoleName(chatRoleName)
                .setChatType(chatTypeEnum.getType())
                .setChatCount(1)
                .setChatTitle(req.getPrompt().substring(0, 20) + "...");
        aiChatConversationMapper.insert(insertChatConversation);
        return insertChatConversation;
    }

    /**
     * chat stream
     *
     * @param req
     * @return
     */
    @Override
    public Flux<ChatResponse> chatStream(ChatReq req) {
        AiClientNameEnum clientNameEnum = AiClientNameEnum.valueOfName(req.getModal());
        // 创建 chat 需要的 Prompt
        Prompt prompt = new Prompt(req.getPrompt());
        req.setTopK(req.getTopK());
        req.setTopP(req.getTopP());
        req.setTemperature(req.getTemperature());
        return aiClient.stream(prompt, clientNameEnum.getName());
    }
}

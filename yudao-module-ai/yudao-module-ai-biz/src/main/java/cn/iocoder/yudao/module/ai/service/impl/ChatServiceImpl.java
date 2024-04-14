package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.config.AiClient;
import cn.iocoder.yudao.module.ai.enums.AiClientNameEnum;
import cn.iocoder.yudao.module.ai.service.ChatService;
import cn.iocoder.yudao.module.ai.vo.ChatReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    /**
     * chat
     *
     * @param req
     * @return
     */
    public String chat(ChatReq req) {
        AiClientNameEnum clientNameEnum = AiClientNameEnum.valueOfName(req.getModal());
        // 创建 chat 需要的 Prompt
        Prompt prompt = new Prompt(req.getPrompt());
        req.setTopK(req.getTopK());
        req.setTopP(req.getTopP());
        req.setTemperature(req.getTemperature());
        // 发送 call 调用
        ChatResponse call = aiClient.call(prompt, clientNameEnum.getName());
        return call.getResult().getOutput().getContent();
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

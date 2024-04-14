package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.module.ai.enums.AiClientNameEnum;
import cn.iocoder.yudao.module.ai.vo.ChatReq;
import reactor.core.publisher.Flux;

/**
 * 聊天 chat
 *
 * @author fansili
 * @time 2024/4/14 15:55
 * @since 1.0
 */
public interface ChatService {

    /**
     * chat
     *
     * @param req
     * @return
     */
    String chat(ChatReq req);

    /**
     * chat stream
     *
     * @param req
     * @return
     */
    Flux<ChatResponse> chatStream(ChatReq req);
}

package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.vo.ChatReq;

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
     * @param sseEmitter
     * @return
     */
    void chatStream(ChatReq req, Utf8SseEmitter sseEmitter);
}

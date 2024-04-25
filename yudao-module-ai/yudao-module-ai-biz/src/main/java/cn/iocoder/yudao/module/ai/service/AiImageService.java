package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.vo.AiImageDallDrawingReq;

/**
 * ai 作图
 *
 * @author fansili
 * @time 2024/4/25 15:50
 * @since 1.0
 */
public interface AiImageService {

    /**
     * ai绘画 - dall2/dall3 绘画
     *
     * @param req
     * @param sseEmitter
     */
    void dallDrawing(AiImageDallDrawingReq req, Utf8SseEmitter sseEmitter);
}

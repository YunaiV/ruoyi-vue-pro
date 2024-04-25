package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.service.AiImageService;
import cn.iocoder.yudao.module.ai.vo.AiImageDallDrawingReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ai 作图
 *
 * @author fansili
 * @time 2024/4/25 15:51
 * @since 1.0
 */
@AllArgsConstructor
@Service
@Slf4j
public class AiImageServiceImpl implements AiImageService {


    @Override
    public void dallDrawing(AiImageDallDrawingReq req, Utf8SseEmitter sseEmitter) {

    }
}

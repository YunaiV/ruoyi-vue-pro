package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.module.ai.service.AiImageService;
import cn.iocoder.yudao.module.ai.vo.AiImageDallDrawingReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ai作图
 *
 * @author fansili
 * @time 2024/4/25 15:49
 * @since 1.0
 */
@Tag(name = "A10-ai作图")
@RestController
@RequestMapping("/ai/image")
@Slf4j
@AllArgsConstructor
public class AiImageController {

    private final AiImageService aiImageService;

    @Operation(summary = "dall2/dall3绘画", description = "openAi dall3是付费的!")
    @GetMapping("/dallDrawing")
    public SseEmitter dallDrawing(@Validated @ModelAttribute AiImageDallDrawingReq req) {
        Utf8SseEmitter sseEmitter = new Utf8SseEmitter();
        aiImageService.dallDrawing(req, sseEmitter);
        return null;
    }
}

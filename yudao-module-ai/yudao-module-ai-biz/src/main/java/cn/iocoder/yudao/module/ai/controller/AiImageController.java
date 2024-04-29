package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.service.AiImageService;
import cn.iocoder.yudao.module.ai.vo.AiImageDallDrawingReq;
import cn.iocoder.yudao.module.ai.vo.AiImageMidjourneyReq;
import cn.iocoder.yudao.module.ai.vo.AiImageMidjourneyRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PostMapping("/dallDrawing")
    public SseEmitter dallDrawing(@Validated @RequestBody AiImageDallDrawingReq req) {
        Utf8SseEmitter sseEmitter = new Utf8SseEmitter();
        aiImageService.dallDrawing(req, sseEmitter);
        return sseEmitter;
    }

    @Operation(summary = "midjourney", description = "midjourney图片绘画流程：1、提交任务 2、获取完成的任务 3、选择对应功能 4、获取最终结果")
    @PostMapping("/midjourney")
    public CommonResult<AiImageMidjourneyRes> midjourney(@Validated @RequestBody AiImageMidjourneyReq req) {
        return CommonResult.success(aiImageService.midjourney(req));
    }
}

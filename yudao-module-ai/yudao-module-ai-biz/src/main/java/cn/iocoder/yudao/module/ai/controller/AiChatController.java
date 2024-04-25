package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import cn.iocoder.yudao.module.ai.vo.AiChatReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ia 模块
 *
 * @author fansili
 * @time 2024/4/13 17:44
 * @since 1.0
 */
@Tag(name = "A1-AI聊天")
@RestController
@RequestMapping("/ai")
@Slf4j
@AllArgsConstructor
public class AiChatController {

    @Autowired
    private final AiChatService chatService;

    @Operation(summary = "聊天-chat", description = "这个一般等待时间比较久，需要全部完成才会返回!")
    @GetMapping("/chat")
    public CommonResult<String> chat(@Validated @ModelAttribute AiChatReq req) {
        return CommonResult.success(chatService.chat(req));
    }

    // TODO @芋艿：调用这个方法异常，Unable to handle the Spring Security Exception because the response is already committed.
    @Operation(summary = "聊天-stream", description = "这里跟通义千问一样采用的是 Server-Sent Events (SSE) 通讯模式")
    @GetMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Validated @ModelAttribute AiChatReq req) {
        Utf8SseEmitter sseEmitter = new Utf8SseEmitter();
        chatService.chatStream(req, sseEmitter);
        return sseEmitter;
    }
}

package cn.iocoder.yudao.module.ai.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.config.AiClient;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author fansili
 * @time 2024/4/13 17:44
 * @since 1.0
 */
@Tag(name = "AI模块")
@RestController
@RequestMapping("/ai")
@Slf4j
@AllArgsConstructor
public class ChatController {

    @Autowired
    private AiClient aiClient;

    @Operation(summary = "聊天-chat", description = "这个一般等待时间比较久，需要全部完成才会返回!")
    @GetMapping("/chat")
    public CommonResult<String> chat(@RequestParam("prompt") String prompt) {
        ChatResponse callRes = aiClient.call(new Prompt(prompt), "qianWen");
        return CommonResult.success(callRes.getResult().getOutput().getContent());
    }

    // TODO @芋艿：调用这个方法异常，Unable to handle the Spring Security Exception because the response is already committed.
    @Operation(summary = "聊天-stream", description = "这里跟通义千问一样采用的是 Server-Sent Events (SSE) 通讯模式")
    @GetMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("prompt") String prompt) {
        Utf8SseEmitter sseEmitter = new Utf8SseEmitter();
        Flux<ChatResponse> streamResponse = aiClient.stream(new Prompt(prompt), "qianWen");
        streamResponse.subscribe(
                new Consumer<ChatResponse>() {
                    @Override
                    public void accept(ChatResponse chatResponse) {
                        String content = chatResponse.getResults().get(0).getOutput().getContent();
                        try {
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
                }
        );
        return sseEmitter;
    }
}

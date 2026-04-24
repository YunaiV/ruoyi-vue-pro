package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.service.AiChatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * AI 对话 SSE 流式接口 — 实现打字机效果。
 *
 * <pre>
 * GET /deepay/chat/stream
 *   ?module=selection
 *   &sessionId=abc123
 *   &customerId=1
 *   &userMessage=我想做外套
 * </pre>
 *
 * <h3>SSE 事件格式</h3>
 * <ul>
 *   <li>{@code event: token}   — 流式 token（逐字推送）</li>
 *   <li>{@code event: meta}    — 元数据（pendingField、quickReplies、images、done、sessionId）</li>
 *   <li>{@code event: error}   — 错误信息</li>
 *   <li>{@code event: done}    — 流结束信号</li>
 * </ul>
 *
 * <h3>前端使用示例</h3>
 * <pre>
 * const url = `/deepay/chat/stream?module=selection&userMessage=我想做外套`;
 * const es  = new EventSource(url);
 * es.addEventListener('token',  e => appendToken(e.data));
 * es.addEventListener('meta',   e => updateMeta(JSON.parse(e.data)));
 * es.addEventListener('done',   e => es.close());
 * </pre>
 */
@Tag(name = "Deepay - AI 对话 SSE 流式")
@RestController
@RequestMapping("/deepay/chat")
@Validated
public class AiChatStreamController {

    @Resource
    private AiChatStreamService aiChatStreamService;

    /**
     * SSE 流式对话 — 打字机效果。
     * 连接后立即开始推送 token，直到整条回复推完发出 done 事件。
     *
     * @param module      板块（selection/design/product/inventory/finance/trend/order）
     * @param sessionId   会话 ID（可选，首次不传）
     * @param customerId  用户 ID（可选）
     * @param userMessage 用户输入（必填）
     * @return SseEmitter（Spring 异步处理）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 流式对话（打字机效果）")
    public SseEmitter stream(
            @RequestParam(defaultValue = "selection") String module,
            @RequestParam(required = false)            String sessionId,
            @RequestParam(required = false)            Long   customerId,
            @RequestParam @NotBlank                    String userMessage) {

        // 超时 60 秒（单条回复不会超过此时间）
        SseEmitter emitter = new SseEmitter(60_000L);
        aiChatStreamService.streamChat(emitter, module, sessionId, customerId, userMessage);
        return emitter;
    }

}

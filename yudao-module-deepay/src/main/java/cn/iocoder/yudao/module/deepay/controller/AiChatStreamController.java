package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.service.AiChatStreamService;
import cn.iocoder.yudao.module.deepay.service.AiUsageLogService;
import cn.iocoder.yudao.module.deepay.service.DeepayRateLimitService;
import cn.iocoder.yudao.module.deepay.vo.AiChatContextVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
 *   &context={"route":"/order/detail","entityType":"order","entityId":"123"}
 * </pre>
 *
 * <h3>SSE 事件格式</h3>
 * <ul>
 *   <li>{@code event: token}   — 流式 token（逐字推送）</li>
 *   <li>{@code event: meta}    — 元数据（pendingField、quickReplies、images、done、sessionId）</li>
 *   <li>{@code event: error}   — 错误信息（含限流/配额提示）</li>
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
 * es.addEventListener('error',  e => showError(e.data));
 * </pre>
 */
@Tag(name = "Deepay - AI 对话 SSE 流式")
@RestController
@RequestMapping("/deepay/chat")
@Validated
@Slf4j
public class AiChatStreamController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource private AiChatStreamService  aiChatStreamService;
    @Resource private DeepayRateLimitService rateLimitService;
    @Resource private AiUsageLogService    usageLogService;

    /**
     * SSE 流式对话 — 打字机效果。
     * 连接后立即开始推送 token，直到整条回复推完发出 done 事件。
     *
     * @param module      板块（selection/design/product/inventory/finance/trend/order/global）
     * @param sessionId   会话 ID（可选，首次不传）
     * @param customerId  用户 ID（可选）
     * @param userMessage 用户输入（必填）
     * @param contextJson 页面上下文 JSON（可选，格式：{route,module,entityType,entityId,snapshot}）
     * @return SseEmitter（Spring 异步处理）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 流式对话（打字机效果）")
    public SseEmitter stream(
            @RequestParam(defaultValue = "selection") String module,
            @RequestParam(required = false)            String sessionId,
            @RequestParam(required = false)            Long   customerId,
            @RequestParam @NotBlank                    String userMessage,
            @Parameter(description = "页面上下文 JSON（可选）")
            @RequestParam(required = false)            String contextJson) {

        // 超时 60 秒（单条回复不会超过此时间）
        SseEmitter emitter = new SseEmitter(60_000L);

        String userId = customerId != null ? String.valueOf(customerId) : null;

        // ── 限流检查（每分钟 N 次）──
        if (!rateLimitService.allow(userId)) {
            usageLogService.log(userId, 0L, module, sessionId, null, null,
                    "RATE_LIMITED", "每分钟请求次数超限");
            sendErrorAndComplete(emitter, "请求过于频繁，请稍后再试（每分钟最多 " +
                    DeepayRateLimitService.MAX_PER_MINUTE + " 次）");
            return emitter;
        }

        // ── 解析上下文 JSON ──
        AiChatContextVO context = parseContext(contextJson);
        String entityType = context != null ? context.getEntityType() : null;
        String entityId   = context != null ? context.getEntityId()   : null;

        // ── 记录用量日志（异步，成功路径）──
        usageLogService.log(userId, 0L, module, sessionId, entityType, entityId, "OK", null);

        // ── 启动异步流推送 ──
        aiChatStreamService.streamChat(emitter, module, sessionId, customerId, userMessage, context);
        return emitter;
    }

    // ── 私有工具 ────────────────────────────────────────────────────────────

    private AiChatContextVO parseContext(String contextJson) {
        if (contextJson == null || contextJson.trim().isEmpty()) return null;
        try {
            return MAPPER.readValue(contextJson, AiChatContextVO.class);
        } catch (Exception e) {
            log.warn("[AiChatStreamController] context JSON 解析失败，忽略上下文: {}", contextJson, e);
            return null;
        }
    }

    private void sendErrorAndComplete(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }
}

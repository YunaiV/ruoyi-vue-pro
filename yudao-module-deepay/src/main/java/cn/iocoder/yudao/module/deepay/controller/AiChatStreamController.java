package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.controller.vo.ChatContextVO;
import cn.iocoder.yudao.module.deepay.service.AiChatStreamService;
import cn.iocoder.yudao.module.deepay.service.AiRateLimitService;
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
 *   &entityType=product          (可选，上下文注入)
 *   &entityId=CHAIN-ABC123       (可选)
 *   &snapshot={"price":99}       (可选，页面数据快照)
 *   &route=/deepay/product/list  (可选)
 *   &tenantId=0                  (可选，默认 0)
 * </pre>
 *
 * <h3>SSE 事件格式</h3>
 * <ul>
 *   <li>{@code event: token}   — 流式 token（逐字推送）</li>
 *   <li>{@code event: meta}    — 元数据（pendingField、quickReplies、images、done、sessionId）</li>
 *   <li>{@code event: error}   — 错误信息（含限流提示）</li>
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
public class AiChatStreamController {

    @Resource private AiChatStreamService aiChatStreamService;
    @Resource private AiRateLimitService  aiRateLimitService;

    /**
     * SSE 流式对话 — 打字机效果。
     * 连接后立即开始推送 token，直到整条回复推完发出 done 事件。
     *
     * @param module      板块（selection/design/product/inventory/finance/trend/order）
     * @param sessionId   会话 ID（可选，首次不传）
     * @param customerId  用户 ID（可选）
     * @param userMessage 用户输入（必填）
     * @param entityType  实体类型（可选，上下文注入）
     * @param entityId    实体 ID（可选）
     * @param snapshot    页面数据快照 JSON（可选）
     * @param route       当前路由（可选）
     * @param tenantId    租户 ID（可选，默认 0）
     * @return SseEmitter（Spring 异步处理）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 流式对话（打字机效果）")
    public SseEmitter stream(
            @RequestParam(defaultValue = "selection") String  module,
            @RequestParam(required = false)            String  sessionId,
            @RequestParam(required = false)            Long    customerId,
            @RequestParam @NotBlank                    String  userMessage,
            @RequestParam(required = false)            String  entityType,
            @RequestParam(required = false)            String  entityId,
            @RequestParam(required = false)            String  snapshot,
            @RequestParam(required = false)            String  route,
            @RequestParam(defaultValue = "0")          Long    tenantId) {

        // 限流检查
        String userId = customerId != null ? String.valueOf(customerId) : null;
        AiRateLimitService.RateLimitResult rlResult =
                aiRateLimitService.checkAndConsume(tenantId, userId, module);
        if (rlResult != null) {
            SseEmitter emitter = new SseEmitter(5_000L);
            aiChatStreamService.sendRateLimitError(emitter, rlResult.message);
            return emitter;
        }

        // 构建上下文 VO
        ChatContextVO chatCtx = null;
        if (entityType != null || entityId != null || snapshot != null || route != null) {
            chatCtx = new ChatContextVO();
            chatCtx.setRoute(route);
            chatCtx.setModule(module);
            chatCtx.setEntityType(entityType);
            chatCtx.setEntityId(entityId);
            chatCtx.setSnapshot(snapshot);
        }

        // 超时 60 秒（单条回复不会超过此时间）
        SseEmitter emitter = new SseEmitter(60_000L);
        aiChatStreamService.streamChat(emitter, module, sessionId, customerId, userMessage,
                chatCtx, tenantId);
        return emitter;
    }

}

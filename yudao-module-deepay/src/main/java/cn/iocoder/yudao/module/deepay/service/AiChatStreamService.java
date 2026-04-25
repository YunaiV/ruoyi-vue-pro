package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.controller.vo.ChatContextVO;
import cn.iocoder.yudao.module.deepay.service.memory.AiMemoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 对话 SSE 流式服务 — 将 {@link AiChatService} 的回复分词推送，实现打字机效果。
 *
 * <h3>推送事件</h3>
 * <ul>
 *   <li>{@code token}  — 每次推送 1~3 个字符，前端追加到气泡</li>
 *   <li>{@code meta}   — 推送完整元数据（pendingField、quickReplies、images、done、sessionId）</li>
 *   <li>{@code error}  — 异常时推送错误消息</li>
 *   <li>{@code done}   — 流结束，前端关闭 EventSource</li>
 * </ul>
 *
 * <p>每个 token 间延迟 30ms，模拟真实打字节奏（可配置）。</p>
 */
@Slf4j
@Service
public class AiChatStreamService {

    /** token 间隔（毫秒）— 越小越快，建议 20~50 */
    private static final int TOKEN_DELAY_MS = 30;
    /** 每次推送字符数（汉字场景建议 1~2，英文 3~5）*/
    private static final int CHARS_PER_TOKEN = 2;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource private AiChatService      aiChatService;
    @Resource private ChatSessionService chatSessionService;
    @Resource private AiMemoryService    aiMemoryService;

    /**
     * 异步流式推送对话回复（含上下文注入 + MongoDB 记忆管理）。
     *
     * @param emitter     SSE 发射器
     * @param module      板块
     * @param sessionId   会话 ID（null → 自动创建）
     * @param customerId  用户 ID
     * @param userMessage 用户输入
     * @param chatCtx     前端页面上下文（可为 null）
     * @param tenantId    租户 ID（null → 1）
     */
    @Async("deepayAsyncExecutor")
    public void streamChat(SseEmitter emitter, String module, String sessionId,
                           Long customerId, String userMessage,
                           ChatContextVO chatCtx, Long tenantId) {
        final Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        try {
            // 0. 确保会话存在（落库 / 续期）
            String effectiveSessionId = aiMemoryService.getOrCreateSession(
                    sessionId, effectiveTenantId, customerId, module);

            // 1. 立即落 user message
            aiMemoryService.saveUserMessage(effectiveSessionId, effectiveTenantId, customerId, module, userMessage);

            // 2. 调用 AiChatService 获取完整回复（支持上下文注入）
            AiChatService.ChatReply reply = aiChatService.chat(
                    module, effectiveSessionId, customerId, userMessage,
                    chatCtx, effectiveTenantId);

            // 3. 逐字推送 aiMessage
            String text = reply.getAiMessage();
            if (text != null && !text.isEmpty()) {
                int i = 0;
                while (i < text.length()) {
                    int end = Math.min(i + CHARS_PER_TOKEN, text.length());
                    String chunk = text.substring(i, end);
                    emitter.send(SseEmitter.event()
                            .name("token")
                            .data(chunk));
                    i = end;
                    Thread.sleep(TOKEN_DELAY_MS);
                }
            }

            // 4. 推送元数据（pendingField、quickReplies、images、done、sessionId）
            Map<String, Object> meta = buildMeta(reply, effectiveSessionId);
            emitter.send(SseEmitter.event()
                    .name("meta")
                    .data(MAPPER.writeValueAsString(meta)));

            // 5. 发送结束信号
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data(""));

            emitter.complete();

            // 6. AI 完成后落 assistant 最终消息（合并）
            if (StringUtils.hasText(text)) {
                aiMemoryService.saveAssistantMessage(effectiveSessionId, effectiveTenantId, customerId, module, text);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            sendError(emitter, "请求被中断");
        } catch (Exception e) {
            log.error("[AiChatStream] 流式推送异常", e);
            sendError(emitter, "服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 兼容旧调用（无上下文）。
     */
    @Async("deepayAsyncExecutor")
    public void streamChat(SseEmitter emitter, String module, String sessionId,
                           Long customerId, String userMessage) {
        streamChat(emitter, module, sessionId, customerId, userMessage, null, 1L);
    }

    /**
     * 发送限流错误并立即完成 SSE。
     */
    public void sendRateLimitError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private Map<String, Object> buildMeta(AiChatService.ChatReply reply, String effectiveSessionId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sessionId",    effectiveSessionId);
        m.put("pendingField", reply.getPendingField());
        m.put("quickReplies", reply.getQuickReplies());
        m.put("images",       reply.getImages());
        m.put("done",         reply.isDone());
        return m;
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(message));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

}

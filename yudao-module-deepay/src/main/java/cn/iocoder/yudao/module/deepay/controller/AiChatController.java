package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.controller.vo.ChatContextVO;
import cn.iocoder.yudao.module.deepay.service.AiChatService;
import cn.iocoder.yudao.module.deepay.service.AiRateLimitService;
import cn.iocoder.yudao.module.deepay.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;

/**
 * AI 对话统一接口 — 每个板块通过 {@code module} 参数路由到对应 Agent 流程。
 *
 * <pre>
 * POST /deepay/chat/message    — 发送一条消息，获取 AI 回复
 * GET  /deepay/chat/autocomplete?prefix=外套  — 输入框自动补全
 * DELETE /deepay/chat/session/{sessionId}     — 结束会话（清除 Context）
 * </pre>
 *
 * <h3>典型对话序列（选款板块）</h3>
 * <pre>
 * POST { module:"selection", customerId:1, userMessage:"我想做外套" }
 *   → { aiMessage:"你的客户是谁？", pendingField:"crowd",
 *        quickReplies:["男装","少女","中老年","运动"], sessionId:"abc123" }
 *
 * POST { module:"selection", sessionId:"abc123", customerId:1, userMessage:"少女" }
 *   → { aiMessage:"什么风格？", pendingField:"style", ... }
 *
 * ...（五问全部回答完）
 *
 * → { aiMessage:"选款完成！为你推荐了 12 款...", images:[...], done:true }
 * </pre>
 */
@Tag(name = "Deepay - AI 对话（各板块）")
@RestController
@RequestMapping("/deepay/chat")
@Validated
public class AiChatController {

    @Resource private AiChatService     aiChatService;
    @Resource private ChatSessionService chatSessionService;
    @Resource private AiRateLimitService aiRateLimitService;

    // ====================================================================
    // POST /deepay/chat/message — 核心接口
    // ====================================================================

    @PostMapping("/message")
    @Operation(summary = "发送消息（自然语言），获取 AI 回复")
    public CommonResult<AiChatService.ChatReply> message(
            @Valid @RequestBody ChatMessageReqVO req) {

        // 限流检查
        String userId = req.getCustomerId() != null ? String.valueOf(req.getCustomerId()) : null;
        Long tenantId = req.getTenantId() != null ? req.getTenantId() : 0L;
        AiRateLimitService.RateLimitResult rlResult =
                aiRateLimitService.checkAndConsume(tenantId, userId, req.getModule());
        if (rlResult != null) {
            return error(429, rlResult.message);
        }

        // 构建上下文 VO
        ChatContextVO chatCtx = req.toChatContext();

        AiChatService.ChatReply reply = aiChatService.chat(
                req.getModule(),
                req.getSessionId(),
                req.getCustomerId(),
                req.getUserMessage(),
                chatCtx,
                tenantId
        );
        return success(reply);
    }

    // ====================================================================
    // GET /deepay/chat/autocomplete — 输入框自动补全
    // ====================================================================

    @GetMapping("/autocomplete")
    @Operation(summary = "输入框自动补全（返回最多 8 个候选词）")
    public CommonResult<List<String>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String prefix) {
        return success(aiChatService.autocomplete(prefix));
    }

    // ====================================================================
    // DELETE /deepay/chat/session/{sessionId} — 结束会话
    // ====================================================================

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "结束对话会话（清除 Context）")
    public CommonResult<Map<String, Object>> endSession(@PathVariable String sessionId) {
        chatSessionService.delete(sessionId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("deleted",   true);
        resp.put("sessionId", sessionId);
        return success(resp);
    }

    // ====================================================================
    // Request VO
    // ====================================================================

    /**
     * 对话消息请求体（支持上下文注入 v1）。
     */
    public static class ChatMessageReqVO {

        /**
         * 板块标识：selection / design / product / inventory / finance / trend / order。
         * 不填默认 selection。
         */
        private String module;

        /**
         * 会话 ID（首次对话可不填，系统自动生成并在响应中返回）。
         * 后续消息须带上此 ID 以保持对话上下文。
         */
        private String sessionId;

        /** 用户 / 客户 ID（可为 null，匿名使用） */
        private Long customerId;

        /** 用户输入的自然语言消息，不能为空 */
        @NotBlank(message = "userMessage 不能为空")
        private String userMessage;

        /** 租户 ID（SaaS 多租户，默认 0）*/
        private Long tenantId;

        // ---- 上下文注入 v1 ----

        /** 当前路由路径（如 /deepay/order/detail） */
        private String route;

        /** 实体类型（order / product / customer / paymentLink / design 等）*/
        private String entityType;

        /** 实体 ID（如订单号、商品 chainCode 等）*/
        private String entityId;

        /**
         * 页面数据快照（JSON 字符串）。
         * 前端可附带当前页面的关键数据，如 {"orderId":"PAY-XXX","status":"PENDING"}。
         */
        private String snapshot;

        /** 构建 ChatContextVO（若有上下文字段则非 null）*/
        public ChatContextVO toChatContext() {
            if (route == null && entityType == null && entityId == null && snapshot == null) {
                return null;
            }
            ChatContextVO ctx = new ChatContextVO();
            ctx.setRoute(route);
            ctx.setModule(module);
            ctx.setEntityType(entityType);
            ctx.setEntityId(entityId);
            ctx.setSnapshot(snapshot);
            return ctx;
        }

        public String getModule()        { return module; }
        public void   setModule(String v)        { this.module = v; }
        public String getSessionId()     { return sessionId; }
        public void   setSessionId(String v)     { this.sessionId = v; }
        public Long   getCustomerId()    { return customerId; }
        public void   setCustomerId(Long v)      { this.customerId = v; }
        public String getUserMessage()   { return userMessage; }
        public void   setUserMessage(String v)   { this.userMessage = v; }
        public Long   getTenantId()      { return tenantId; }
        public void   setTenantId(Long v)        { this.tenantId = v; }
        public String getRoute()         { return route; }
        public void   setRoute(String v)         { this.route = v; }
        public String getEntityType()    { return entityType; }
        public void   setEntityType(String v)    { this.entityType = v; }
        public String getEntityId()      { return entityId; }
        public void   setEntityId(String v)      { this.entityId = v; }
        public String getSnapshot()      { return snapshot; }
        public void   setSnapshot(String v)      { this.snapshot = v; }
    }

}

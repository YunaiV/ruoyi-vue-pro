package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.service.AiChatService;
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

    // ====================================================================
    // POST /deepay/chat/message — 核心接口
    // ====================================================================

    @PostMapping("/message")
    @Operation(summary = "发送消息（自然语言），获取 AI 回复")
    public CommonResult<AiChatService.ChatReply> message(
            @Valid @RequestBody ChatMessageReqVO req) {

        AiChatService.ChatReply reply = aiChatService.chat(
                req.getModule(),
                req.getSessionId(),
                req.getCustomerId(),
                req.getUserMessage()
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
     * 对话消息请求体。
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

        public String getModule()       { return module; }
        public void   setModule(String v)       { this.module = v; }
        public String getSessionId()    { return sessionId; }
        public void   setSessionId(String v)    { this.sessionId = v; }
        public Long   getCustomerId()   { return customerId; }
        public void   setCustomerId(Long v)     { this.customerId = v; }
        public String getUserMessage()  { return userMessage; }
        public void   setUserMessage(String v)  { this.userMessage = v; }
    }

}

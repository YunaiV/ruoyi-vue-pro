package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiToolAuditLogDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiToolAuditLogRepository;
import cn.iocoder.yudao.module.deepay.service.tool.AiToolExecutor;
import cn.iocoder.yudao.module.deepay.service.tool.AiToolRegistry;
import cn.iocoder.yudao.module.deepay.service.tool.ToolCallContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * AI 工具调用接口。
 *
 * <p>提供：工具列表、调用工具、确认高风险动作、取消动作、查询审计日志。</p>
 */
@Tag(name = "Deepay - AI 工具调用")
@RestController
@RequestMapping("/deepay/tools")
@Validated
public class AiToolController {

    @Resource private AiToolRegistry         toolRegistry;
    @Resource private AiToolExecutor         toolExecutor;
    @Resource private AiToolAuditLogRepository auditRepo;

    // =========================================================================
    // 工具元信息
    // =========================================================================

    @GetMapping("/list")
    @Operation(summary = "列出所有工具定义（name/desc/schema/riskLevel）")
    public List<Map<String, Object>> listTools() {
        return toolRegistry.listToolSchemas();
    }

    // =========================================================================
    // 工具调用
    // =========================================================================

    @PostMapping("/call")
    @Operation(summary = "调用工具（HIGH 风险返回 pending，LOW/MEDIUM 直接执行）")
    public Map<String, Object> callTool(@RequestBody @Validated ToolCallReq req) {
        ToolCallContext ctx = ToolCallContext.builder()
                .tenantId(req.getTenantId())
                .customerId(req.getCustomerId())
                .sessionId(req.getSessionId())
                .module(req.getModule())
                .operator(req.getOperator())
                .build();
        return toolExecutor.call(ctx, req.getToolName(), req.getParams());
    }

    // =========================================================================
    // Human-in-the-loop
    // =========================================================================

    @PostMapping("/confirm/{actionId}")
    @Operation(summary = "确认高风险待执行动作（Human-in-the-loop）")
    public Map<String, Object> confirmAction(
            @PathVariable String actionId,
            @RequestParam @NotNull Long tenantId) {
        return toolExecutor.confirm(actionId, tenantId);
    }

    @PostMapping("/cancel/{actionId}")
    @Operation(summary = "取消待确认动作")
    public void cancelAction(
            @PathVariable String actionId,
            @RequestParam @NotNull Long tenantId) {
        toolExecutor.cancel(actionId, tenantId);
    }

    // =========================================================================
    // 审计日志
    // =========================================================================

    @GetMapping("/audit")
    @Operation(summary = "查询租户工具调用审计日志")
    public Page<AiToolAuditLogDocument> listAuditLogs(
            @RequestParam @NotNull Long tenantId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditRepo.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(page, size));
    }

    // =========================================================================
    // DTO
    // =========================================================================

    @Data
    public static class ToolCallReq {
        @NotNull  private Long              tenantId;
        private   Long                      customerId;
        private   String                    sessionId;
        private   String                    module;
        @NotBlank private String            toolName;
        private   String                    operator;
        private   Map<String, Object>       params;
    }

}

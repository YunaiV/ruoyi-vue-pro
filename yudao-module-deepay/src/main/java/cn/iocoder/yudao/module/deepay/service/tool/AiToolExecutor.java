package cn.iocoder.yudao.module.deepay.service.tool;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiPendingActionDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiPendingActionRepository;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiToolAuditLogDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiToolAuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AI 工具执行器。
 *
 * <h3>执行流程</h3>
 * <ol>
 *   <li>查找工具</li>
 *   <li>HIGH 风险：先创建 {@link AiPendingActionDocument}，返回 pendingActionId，等待前端确认</li>
 *   <li>LOW / MEDIUM：直接执行，记录审计日志</li>
 * </ol>
 */
@Slf4j
@Service
public class AiToolExecutor {

    /** 高风险 pending action 超时时间：30 分钟 */
    private static final long PENDING_TTL_SECONDS = 30 * 60;

    @Resource private AiToolRegistry          toolRegistry;
    @Resource private AiToolAuditLogRepository auditRepo;
    @Resource private AiPendingActionRepository pendingRepo;

    /**
     * 调用工具（主入口）。
     *
     * <p>HIGH 风险工具返回 {@code {pending: true, actionId: "..."}} 而不执行；
     * LOW / MEDIUM 风险工具直接执行并返回结果。</p>
     *
     * @param ctx      调用上下文
     * @param toolName 工具名称
     * @param params   工具参数
     * @return 执行结果（LOW/MEDIUM）或 pending 信息（HIGH）
     */
    public Map<String, Object> call(ToolCallContext ctx, String toolName, Map<String, Object> params) {
        AiTool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            throw new ToolExecutionException(toolName, "TOOL_NOT_FOUND", "工具 " + toolName + " 不存在");
        }

        if (tool.getRiskLevel() == AiTool.RiskLevel.HIGH) {
            return createPendingAction(ctx, tool, params);
        }
        return doExecute(ctx, tool, params);
    }

    /**
     * 确认待执行的高风险动作（Human-in-the-loop 确认后调用）。
     *
     * @param actionId 待确认动作 ID
     * @param tenantId 租户 ID（安全校验）
     * @return 执行结果
     */
    public Map<String, Object> confirm(String actionId, Long tenantId) {
        AiPendingActionDocument pending = pendingRepo.findById(actionId)
                .orElseThrow(() -> new ToolExecutionException("unknown", "ACTION_NOT_FOUND",
                        "待确认动作不存在或已过期: " + actionId));
        if (!pending.getTenantId().equals(tenantId)) {
            throw new ToolExecutionException(pending.getToolName(), "FORBIDDEN", "无权确认此动作");
        }
        if (!"WAITING".equals(pending.getStatus())) {
            throw new ToolExecutionException(pending.getToolName(), "ACTION_NOT_WAITING",
                    "动作状态已变更: " + pending.getStatus());
        }
        // 更新状态为已确认
        pending.setStatus("CONFIRMED");
        pendingRepo.save(pending);

        // 重建上下文并执行
        ToolCallContext ctx = ToolCallContext.builder()
                .tenantId(pending.getTenantId())
                .sessionId(pending.getSessionId())
                .build();
        AiTool tool = toolRegistry.getTool(pending.getToolName());
        if (tool == null) {
            throw new ToolExecutionException(pending.getToolName(), "TOOL_NOT_FOUND",
                    "工具 " + pending.getToolName() + " 已被移除");
        }
        return doExecute(ctx, tool, pending.getParams());
    }

    /**
     * 取消待确认动作。
     */
    public void cancel(String actionId, Long tenantId) {
        pendingRepo.findById(actionId).ifPresent(p -> {
            if (!p.getTenantId().equals(tenantId)) {
                throw new ToolExecutionException(p.getToolName(), "FORBIDDEN", "无权取消此动作");
            }
            p.setStatus("CANCELLED");
            pendingRepo.save(p);
            log.info("[AiToolExecutor] 取消动作 actionId={} tool={}", actionId, p.getToolName());
        });
    }

    // =========================================================================
    // 内部
    // =========================================================================

    private Map<String, Object> createPendingAction(ToolCallContext ctx, AiTool tool,
                                                    Map<String, Object> params) {
        AiPendingActionDocument pending = AiPendingActionDocument.builder()
                .tenantId(ctx.getTenantId())
                .sessionId(ctx.getSessionId())
                .toolName(tool.getName())
                .riskLevel(tool.getRiskLevel())
                .description(tool.getDescription())
                .params(params)
                .status("WAITING")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(PENDING_TTL_SECONDS))
                .build();
        AiPendingActionDocument saved = pendingRepo.save(pending);
        log.info("[AiToolExecutor] 高风险工具等待确认 tool={} actionId={}", tool.getName(), saved.getId());

        // 写入审计日志
        saveAuditLog(ctx, tool, params, null, "PENDING_CONFIRM", 0, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pending",     true);
        result.put("actionId",    saved.getId());
        result.put("toolName",    tool.getName());
        result.put("description", tool.getDescription());
        result.put("riskLevel",   tool.getRiskLevel().name());
        return result;
    }

    private Map<String, Object> doExecute(ToolCallContext ctx, AiTool tool,
                                          Map<String, Object> params) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = tool.execute(ctx, params);
            long latency = System.currentTimeMillis() - start;
            saveAuditLog(ctx, tool, params, result, "SUCCESS", latency, null);
            log.info("[AiToolExecutor] 工具执行成功 tool={} latency={}ms", tool.getName(), latency);
            return result;
        } catch (ToolExecutionException e) {
            long latency = System.currentTimeMillis() - start;
            saveAuditLog(ctx, tool, params, null, "FAILED", latency, e.getMessage());
            log.warn("[AiToolExecutor] 工具执行失败 tool={} error={}", tool.getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            saveAuditLog(ctx, tool, params, null, "FAILED", latency, e.getMessage());
            log.error("[AiToolExecutor] 工具执行异常 tool={}", tool.getName(), e);
            throw new ToolExecutionException(tool.getName(), "UNEXPECTED_ERROR",
                    "工具执行异常: " + e.getMessage(), e);
        }
    }

    private void saveAuditLog(ToolCallContext ctx, AiTool tool, Map<String, Object> params,
                              Map<String, Object> result, String status, long latencyMs, String error) {
        try {
            AiToolAuditLogDocument logDoc = AiToolAuditLogDocument.builder()
                    .tenantId(ctx.getTenantId())
                    .customerId(ctx.getCustomerId())
                    .module(ctx.getModule())
                    .sessionId(ctx.getSessionId())
                    .operator(ctx.getOperator())
                    .toolName(tool.getName())
                    .riskLevel(tool.getRiskLevel())
                    .params(params)
                    .result(result)
                    .status(status)
                    .latencyMs(latencyMs)
                    .error(error)
                    .createdAt(Instant.now())
                    .build();
            auditRepo.save(logDoc);
        } catch (Exception e) {
            log.warn("[AiToolExecutor] 审计日志写入失败 tool={}", tool.getName(), e);
        }
    }

}

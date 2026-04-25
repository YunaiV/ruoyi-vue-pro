package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiChatMessageDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiChatSessionDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiMemoryItemDocument;
import cn.iocoder.yudao.module.deepay.service.memory.AiMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * AI 记忆管理后台接口。
 *
 * <p>提供：记忆开关、删除记忆、清空会话、合规删除全部聊天与记忆。</p>
 */
@Tag(name = "Deepay - AI 记忆管理")
@RestController
@RequestMapping("/deepay/memory")
@Validated
public class AiMemoryController {

    @Resource
    private AiMemoryService aiMemoryService;

    // =========================================================================
    // Session
    // =========================================================================

    @GetMapping("/sessions")
    @Operation(summary = "查询某客户的会话列表")
    public Page<AiChatSessionDocument> listSessions(
            @RequestParam @NotNull Long tenantId,
            @RequestParam @NotNull Long customerId,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return aiMemoryService.listSessions(tenantId, customerId, page, size);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话及其消息")
    public void deleteSession(
            @PathVariable String sessionId,
            @RequestParam @NotNull Long tenantId) {
        aiMemoryService.deleteSession(sessionId, tenantId);
    }

    @PutMapping("/sessions/{sessionId}/memory-switch")
    @Operation(summary = "切换会话记忆开关")
    public void setMemoryEnabled(
            @PathVariable String sessionId,
            @RequestParam boolean enabled) {
        aiMemoryService.setMemoryEnabled(sessionId, enabled);
    }

    // =========================================================================
    // Message
    // =========================================================================

    @GetMapping("/messages")
    @Operation(summary = "查询会话消息列表")
    public List<AiChatMessageDocument> listMessages(@RequestParam String sessionId) {
        return aiMemoryService.listMessages(sessionId);
    }

    // =========================================================================
    // Memory
    // =========================================================================

    @GetMapping("/items")
    @Operation(summary = "查询某客户的所有记忆")
    public List<AiMemoryItemDocument> listMemory(
            @RequestParam @NotNull Long tenantId,
            @RequestParam @NotNull Long customerId) {
        return aiMemoryService.listMemory(tenantId, customerId);
    }

    @DeleteMapping("/items")
    @Operation(summary = "删除某客户某板块的记忆")
    public void deleteMemoryByModule(
            @RequestParam @NotNull Long tenantId,
            @RequestParam @NotNull Long customerId,
            @RequestParam @NotNull String module) {
        aiMemoryService.deleteMemoryByModule(tenantId, customerId, module);
    }

    @PostMapping("/items/upsert")
    @Operation(summary = "（测试/运营）更新某客户某板块的记忆")
    public void upsertMemory(@RequestBody @Validated UpsertMemoryReq req) {
        aiMemoryService.upsertMemory(req.getTenantId(), req.getCustomerId(), req.getModule(), req.getFacts());
    }

    // =========================================================================
    // 合规删除
    // =========================================================================

    @DeleteMapping("/compliance/delete-all")
    @Operation(summary = "合规删除：按 customerId 全量删除所有聊天与记忆")
    public void deleteAll(
            @RequestParam @NotNull Long tenantId,
            @RequestParam @NotNull Long customerId) {
        aiMemoryService.deleteAll(tenantId, customerId);
    }

    // =========================================================================
    // DTO
    // =========================================================================

    @Data
    public static class UpsertMemoryReq {
        @NotNull private Long tenantId;
        @NotNull private Long customerId;
        @NotNull private String module;
        private Map<String, Object> facts;
    }

}

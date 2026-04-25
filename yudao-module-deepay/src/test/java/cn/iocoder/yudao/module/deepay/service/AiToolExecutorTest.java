package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiPendingActionDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiPendingActionRepository;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiToolAuditLogRepository;
import cn.iocoder.yudao.module.deepay.service.tool.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AiToolExecutor 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AiToolExecutorTest {

    @Mock private AiToolRegistry          toolRegistry;
    @Mock private AiToolAuditLogRepository auditRepo;
    @Mock private AiPendingActionRepository pendingRepo;

    @InjectMocks
    private AiToolExecutor toolExecutor;

    private static final ToolCallContext CTX = ToolCallContext.builder()
            .tenantId(1L).customerId(100L).sessionId("s1").module("order").build();

    // =========================================================================
    // call - LOW risk
    // =========================================================================

    @Test
    void call_lowRiskTool_executesDirectly() {
        AiTool mockTool = mockTool("getProductDetail", AiTool.RiskLevel.LOW,
                Map.of("id", 1L, "title", "test"));
        when(toolRegistry.getTool("getProductDetail")).thenReturn(mockTool);

        Map<String, Object> result = toolExecutor.call(CTX, "getProductDetail", Map.of("productId", 1));

        assertThat(result).containsKey("id");
        verify(auditRepo).save(any());
        verify(pendingRepo, never()).save(any());
    }

    // =========================================================================
    // call - HIGH risk creates pending
    // =========================================================================

    @Test
    void call_highRiskTool_createsPendingAction() {
        AiTool mockTool = mockTool("updateOrderField", AiTool.RiskLevel.HIGH, Map.of());
        when(toolRegistry.getTool("updateOrderField")).thenReturn(mockTool);
        AiPendingActionDocument pending = AiPendingActionDocument.builder()
                .id("action-abc").tenantId(1L).toolName("updateOrderField")
                .status("WAITING").createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();
        when(pendingRepo.save(any())).thenReturn(pending);

        Map<String, Object> result = toolExecutor.call(CTX, "updateOrderField", Map.of());

        assertThat(result.get("pending")).isEqualTo(true);
        assertThat(result.get("actionId")).isEqualTo("action-abc");
        verify(pendingRepo).save(any());
    }

    // =========================================================================
    // call - unknown tool
    // =========================================================================

    @Test
    void call_unknownTool_throwsException() {
        when(toolRegistry.getTool("nonExistent")).thenReturn(null);

        assertThatThrownBy(() -> toolExecutor.call(CTX, "nonExistent", Map.of()))
                .isInstanceOf(ToolExecutionException.class)
                .hasMessageContaining("不存在");
    }

    // =========================================================================
    // confirm - happy path
    // =========================================================================

    @Test
    void confirm_waitingAction_executesAndReturnsResult() {
        AiPendingActionDocument pending = AiPendingActionDocument.builder()
                .id("action-xyz").tenantId(1L).toolName("createOrderDraft")
                .status("WAITING").params(Map.of("chainCode", "CC001", "amount", "100"))
                .createdAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1800))
                .build();
        when(pendingRepo.findById("action-xyz")).thenReturn(Optional.of(pending));
        when(pendingRepo.save(any())).thenReturn(pending);

        AiTool mockTool = mockTool("createOrderDraft", AiTool.RiskLevel.HIGH, Map.of("orderId", 99L));
        when(toolRegistry.getTool("createOrderDraft")).thenReturn(mockTool);

        Map<String, Object> result = toolExecutor.confirm("action-xyz", 1L);

        assertThat(result).containsKey("orderId");
        verify(pendingRepo, atLeastOnce()).save(any());
    }

    // =========================================================================
    // confirm - wrong tenant
    // =========================================================================

    @Test
    void confirm_wrongTenant_throwsForbidden() {
        AiPendingActionDocument pending = AiPendingActionDocument.builder()
                .id("action-xyz").tenantId(2L).toolName("xxx")
                .status("WAITING").createdAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1800))
                .build();
        when(pendingRepo.findById("action-xyz")).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> toolExecutor.confirm("action-xyz", 1L))
                .isInstanceOf(ToolExecutionException.class)
                .hasMessageContaining("无权");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private AiTool mockTool(String name, AiTool.RiskLevel risk, Map<String, Object> returnVal) {
        AiTool tool = mock(AiTool.class);
        when(tool.getName()).thenReturn(name);
        when(tool.getRiskLevel()).thenReturn(risk);
        when(tool.getDescription()).thenReturn(name + " description");
        if (risk != AiTool.RiskLevel.HIGH) {
            try {
                when(tool.execute(any(), any())).thenReturn(returnVal);
            } catch (ToolExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return tool;
    }
}

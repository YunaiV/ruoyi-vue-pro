package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.mongodb.*;
import cn.iocoder.yudao.module.deepay.service.memory.AiMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AiMemoryService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AiMemoryServiceTest {

    @Mock private AiChatSessionRepository sessionRepo;
    @Mock private AiChatMessageRepository messageRepo;
    @Mock private AiMemoryItemRepository  memoryRepo;

    @InjectMocks
    private AiMemoryService aiMemoryService;

    // =========================================================================
    // getOrCreateSession
    // =========================================================================

    @Test
    void getOrCreateSession_newSession_createsAndReturnsId() {
        AiChatSessionDocument saved = AiChatSessionDocument.builder()
                .id("session-123")
                .tenantId(1L)
                .customerId(100L)
                .module("design")
                .createdAt(Instant.now())
                .lastActiveAt(Instant.now())
                .build();
        when(sessionRepo.save(any())).thenReturn(saved);

        String id = aiMemoryService.getOrCreateSession(null, 1L, 100L, "design");

        assertThat(id).isEqualTo("session-123");
        verify(sessionRepo, times(1)).save(any());
    }

    @Test
    void getOrCreateSession_existingSession_updatesLastActive() {
        AiChatSessionDocument existing = AiChatSessionDocument.builder()
                .id("existing-456")
                .tenantId(1L)
                .build();
        when(sessionRepo.findById("existing-456")).thenReturn(Optional.of(existing));
        when(sessionRepo.save(any())).thenReturn(existing);

        String id = aiMemoryService.getOrCreateSession("existing-456", 1L, 100L, "design");

        assertThat(id).isEqualTo("existing-456");
        verify(sessionRepo, times(1)).save(any());
    }

    // =========================================================================
    // saveUserMessage
    // =========================================================================

    @Test
    void saveUserMessage_savesCorrectRole() {
        aiMemoryService.saveUserMessage("s1", 1L, 100L, "design", "我想做外套");

        ArgumentCaptor<AiChatMessageDocument> captor = ArgumentCaptor.forClass(AiChatMessageDocument.class);
        verify(messageRepo).save(captor.capture());
        AiChatMessageDocument msg = captor.getValue();
        assertThat(msg.getRole()).isEqualTo("user");
        assertThat(msg.getContent()).isEqualTo("我想做外套");
        assertThat(msg.getSessionId()).isEqualTo("s1");
    }

    // =========================================================================
    // upsertMemory - 只允许字段过滤
    // =========================================================================

    @Test
    void upsertMemory_design_filtersAllowedFields() {
        when(memoryRepo.findByTenantIdAndCustomerIdAndModule(1L, 100L, "design"))
                .thenReturn(Optional.empty());

        Map<String, Object> rawFacts = new LinkedHashMap<>();
        rawFacts.put("stylePreference", "工装");
        rawFacts.put("sizePreference",  "L");
        rawFacts.put("illegalField",    "should-be-filtered");

        aiMemoryService.upsertMemory(1L, 100L, "design", rawFacts);

        ArgumentCaptor<AiMemoryItemDocument> captor = ArgumentCaptor.forClass(AiMemoryItemDocument.class);
        verify(memoryRepo).save(captor.capture());
        AiMemoryItemDocument item = captor.getValue();
        assertThat(item.getFacts()).containsKey("stylePreference");
        assertThat(item.getFacts()).containsKey("sizePreference");
        assertThat(item.getFacts()).doesNotContainKey("illegalField");
    }

    @Test
    void upsertMemory_unknownModule_skips() {
        aiMemoryService.upsertMemory(1L, 100L, "unknown_module", Map.of("key", "val"));
        verify(memoryRepo, never()).save(any());
    }

    @Test
    void upsertMemory_emptyParams_skips() {
        aiMemoryService.upsertMemory(null, 100L, "design", Map.of("k", "v"));
        verify(memoryRepo, never()).save(any());
    }

    // =========================================================================
    // deleteAll
    // =========================================================================

    @Test
    void deleteAll_deletesAllThreeCollections() {
        aiMemoryService.deleteAll(1L, 100L);
        verify(messageRepo).deleteByTenantIdAndCustomerId(1L, 100L);
        verify(sessionRepo).deleteByTenantIdAndCustomerId(1L, 100L);
        verify(memoryRepo).deleteByTenantIdAndCustomerId(1L, 100L);
    }

    // =========================================================================
    // deleteMemoryByModule
    // =========================================================================

    @Test
    void deleteMemoryByModule_callsRepoWithCorrectArgs() {
        aiMemoryService.deleteMemoryByModule(1L, 100L, "sales");
        verify(memoryRepo).deleteByTenantIdAndCustomerIdAndModule(1L, 100L, "sales");
    }

}

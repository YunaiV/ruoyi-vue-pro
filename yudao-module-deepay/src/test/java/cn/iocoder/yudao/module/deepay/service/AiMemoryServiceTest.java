package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiMemoryItemDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiMemoryItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiMemoryService} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AiMemoryServiceTest {

    @Mock  private DeepayAiMemoryItemMapper memoryItemMapper;
    @InjectMocks private AiMemoryService aiMemoryService;

    // ====================================================================
    // buildMemoryPack
    // ====================================================================

    @Test
    void testBuildMemoryPack_hasMemory_shouldReturnFormattedPack() {
        // Given
        DeepayAiMemoryItemDO item1 = new DeepayAiMemoryItemDO();
        item1.setMemKey("preferredStyle");
        item1.setMemValue("极简");

        DeepayAiMemoryItemDO item2 = new DeepayAiMemoryItemDO();
        item2.setMemKey("budgetRange");
        item2.setMemValue("300-600元");

        when(memoryItemMapper.selectByCustomerAndModule(0L, "cust001", "design"))
                .thenReturn(Arrays.asList(item1, item2));

        // When
        String pack = aiMemoryService.buildMemoryPack(0L, "cust001", "design");

        // Then
        assertNotNull(pack);
        assertTrue(pack.contains("preferredStyle: 极简"));
        assertTrue(pack.contains("budgetRange: 300-600元"));
        assertTrue(pack.contains("用户记忆"));
    }

    @Test
    void testBuildMemoryPack_noMemory_shouldReturnEmpty() {
        when(memoryItemMapper.selectByCustomerAndModule(anyLong(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        String pack = aiMemoryService.buildMemoryPack(0L, "cust001", "design");

        assertEquals("", pack);
    }

    @Test
    void testBuildMemoryPack_nullCustomerId_shouldReturnEmpty() {
        String pack = aiMemoryService.buildMemoryPack(0L, null, "design");
        assertEquals("", pack);
        verify(memoryItemMapper, never()).selectByCustomerAndModule(any(), any(), any());
    }

    // ====================================================================
    // saveMemory — 白名单过滤
    // ====================================================================

    @Test
    void testSaveMemory_allowedKey_shouldCallMapper() {
        // Given: design module, preferredStyle 在白名单内
        // When
        aiMemoryService.saveMemory(0L, "cust001", "design", "profile",
                "preferredStyle", "极简", 0.9, "sess123");

        // Then: 应调用 mapper upsert
        ArgumentCaptor<DeepayAiMemoryItemDO> captor =
                ArgumentCaptor.forClass(DeepayAiMemoryItemDO.class);
        verify(memoryItemMapper).upsertMemory(captor.capture());
        DeepayAiMemoryItemDO saved = captor.getValue();
        assertEquals("preferredStyle", saved.getMemKey());
        assertEquals("极简", saved.getMemValue());
        assertEquals("design", saved.getModule());
        assertEquals("cust001", saved.getCustomerId());
        assertEquals(0, saved.getDeleted());
        assertNotNull(saved.getExpiresAt(), "应设置过期时间");
    }

    @Test
    void testSaveMemory_blockedKey_shouldNotCallMapper() {
        // Given: design module, "sensitiveKey" 不在白名单内
        aiMemoryService.saveMemory(0L, "cust001", "design", "profile",
                "sensitivePrivateKey", "secret", 0.9, "sess123");

        // Then: 不应写入
        verify(memoryItemMapper, never()).upsertMemory(any());
    }

    @Test
    void testSaveMemory_nullCustomerId_shouldNotCallMapper() {
        aiMemoryService.saveMemory(0L, null, "design", "fact",
                "preferredStyle", "极简", 0.9, "sess123");

        verify(memoryItemMapper, never()).upsertMemory(any());
    }

    @Test
    void testSaveMemory_ttl365Days_shouldBeSet() {
        aiMemoryService.saveMemory(0L, "cust001", "design", "profile",
                "preferredStyle", "极简", 0.9, "sess123");

        ArgumentCaptor<DeepayAiMemoryItemDO> captor =
                ArgumentCaptor.forClass(DeepayAiMemoryItemDO.class);
        verify(memoryItemMapper).upsertMemory(captor.capture());

        // 过期时间应大约在 365 天后
        DeepayAiMemoryItemDO saved = captor.getValue();
        long daysUntilExpiry = java.time.Duration.between(
                java.time.LocalDateTime.now(), saved.getExpiresAt()).toDays();
        assertTrue(daysUntilExpiry >= 364 && daysUntilExpiry <= 366,
                "过期时间应为 365 天");
    }

    // ====================================================================
    // deleteAllMemory
    // ====================================================================

    @Test
    void testDeleteAllMemory_shouldCallSoftDelete() {
        aiMemoryService.deleteAllMemory(0L, "cust001");
        verify(memoryItemMapper).softDeleteAllByCustomer(0L, "cust001");
    }

    @Test
    void testDeleteAllMemory_nullCustomerId_shouldNotCallMapper() {
        aiMemoryService.deleteAllMemory(0L, null);
        verify(memoryItemMapper, never()).softDeleteAllByCustomer(any(), any());
    }

    // ====================================================================
    // saveMemoriesFromJson
    // ====================================================================

    @Test
    void testSaveMemoriesFromJson_validJson_shouldSaveAllowed() {
        String json = "[{\"key\":\"preferredStyle\",\"value\":\"极简\",\"type\":\"profile\",\"confidence\":0.9}]";

        aiMemoryService.saveMemoriesFromJson(0L, "cust001", "design", json, "sess123");

        verify(memoryItemMapper, atLeastOnce()).upsertMemory(any());
    }

    @Test
    void testSaveMemoriesFromJson_invalidJson_shouldNotThrow() {
        assertDoesNotThrow(() ->
                aiMemoryService.saveMemoriesFromJson(0L, "cust001", "design",
                        "not-valid-json", "sess123"));
    }

    // ====================================================================
    // getAllowedKeys
    // ====================================================================

    @Test
    void testGetAllowedKeys_shouldReturnImmutableMap() {
        var keys = aiMemoryService.getAllowedKeys();
        assertNotNull(keys);
        assertTrue(keys.containsKey("design"));
        assertTrue(keys.get("design").contains("preferredStyle"));
        assertTrue(keys.containsKey("sales"));
        assertTrue(keys.containsKey("finance"));
    }

}

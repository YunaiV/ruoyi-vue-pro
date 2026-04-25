package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.AiPersonaDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.AiPersonaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiPersonaService} 单元测试。
 *
 * <p>验证三层降级策略：
 * <ol>
 *   <li>命中租户级 persona → 返回租户 prompt</li>
 *   <li>租户级未命中，命中全局默认 → 返回全局 prompt</li>
 *   <li>全局也未命中 → 返回硬编码 fallback</li>
 *   <li>DB 异常 → 降级到硬编码（不抛异常）</li>
 * </ol>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AiPersonaServiceTest {

    @Mock
    private AiPersonaMapper personaMapper;

    @InjectMocks
    private AiPersonaService aiPersonaService;

    // ── 1. 命中租户级 persona ─────────────────────────────────

    @Test
    void getSystemPrompt_tenantLevel_returnsTenantPrompt() {
        Long tenantId = 42L;
        AiPersonaDO tenantPersona = buildPersona(tenantId, "selection", "租户专属选款助手 prompt");
        when(personaMapper.selectByTenantAndModule(tenantId, "selection"))
                .thenReturn(List.of(tenantPersona));

        String result = aiPersonaService.getSystemPrompt(tenantId, "selection");

        assertEquals("租户专属选款助手 prompt", result);
    }

    // ── 2. 租户级未命中，命中全局默认 ─────────────────────────

    @Test
    void getSystemPrompt_fallsBackToGlobal_whenTenantMissing() {
        Long tenantId = 42L;
        AiPersonaDO globalPersona = buildPersona(0L, "selection", "全局默认选款 prompt");

        when(personaMapper.selectByTenantAndModule(tenantId, "selection"))
                .thenReturn(Collections.emptyList());
        when(personaMapper.selectByTenantAndModule(0L, "selection"))
                .thenReturn(List.of(globalPersona));

        String result = aiPersonaService.getSystemPrompt(tenantId, "selection");

        assertEquals("全局默认选款 prompt", result);
    }

    // ── 3. 两层 DB 均未命中 → 硬编码 fallback ────────────────

    @Test
    void getSystemPrompt_fallsBackToHardcoded_whenBothDbMiss() {
        Long tenantId = 99L;
        when(personaMapper.selectByTenantAndModule(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        String result = aiPersonaService.getSystemPrompt(tenantId, "selection");

        assertNotNull(result, "硬编码 fallback 不应为 null");
        assertFalse(result.isBlank(), "硬编码 fallback 不应为空字符串");
        // selection 模块的硬编码 prompt 包含"选款"关键字
        assertTrue(result.contains("选款") || result.contains("购物"),
                "selection 模块的 fallback prompt 应包含相关关键字");
    }

    // ── 4. module=null → 使用 "global" ──────────────────────

    @Test
    void getSystemPrompt_nullModule_usesGlobal() {
        when(personaMapper.selectByTenantAndModule(anyLong(), eq("global")))
                .thenReturn(Collections.emptyList());

        String result = aiPersonaService.getSystemPrompt(0L, null);

        assertNotNull(result, "null module 时应有默认 prompt");
        assertFalse(result.isBlank());
    }

    // ── 5. DB 异常 → 降级，不抛异常 ────────────────────────

    @Test
    void getSystemPrompt_dbException_degradesGracefully() {
        when(personaMapper.selectByTenantAndModule(anyLong(), anyString()))
                .thenThrow(new RuntimeException("DB 连接失败"));

        // 不应抛异常，应返回硬编码 fallback
        assertDoesNotThrow(() -> {
            String result = aiPersonaService.getSystemPrompt(1L, "order");
            assertNotNull(result);
            assertFalse(result.isBlank());
        });
    }

    // ── 6. 租户 ID 为 0 → 只查全局，不重复查 ────────────────

    @Test
    void getSystemPrompt_globalTenantId_queriesOnlyOnce() {
        AiPersonaDO globalPersona = buildPersona(0L, "design", "全局设计师 prompt");
        when(personaMapper.selectByTenantAndModule(0L, "design"))
                .thenReturn(List.of(globalPersona));

        String result = aiPersonaService.getSystemPrompt(AiPersonaService.GLOBAL_TENANT_ID, "design");

        assertEquals("全局设计师 prompt", result);
        // 传入 GLOBAL_TENANT_ID 时，只应执行一次 DB 查询（不需要先查租户再查全局）
        verify(personaMapper, times(1)).selectByTenantAndModule(anyLong(), anyString());
    }

    // ── 7. 未知 module → 返回通用 fallback ───────────────────

    @Test
    void getSystemPrompt_unknownModule_returnsGenericFallback() {
        when(personaMapper.selectByTenantAndModule(anyLong(), eq("unknown_module")))
                .thenReturn(Collections.emptyList());

        String result = aiPersonaService.getSystemPrompt(0L, "unknown_module");

        assertNotNull(result);
        assertFalse(result.isBlank(), "未知 module 应返回通用兜底 prompt");
    }

    // ── helpers ──────────────────────────────────────────────

    private AiPersonaDO buildPersona(Long tenantId, String module, String systemPrompt) {
        AiPersonaDO p = new AiPersonaDO();
        p.setId(1L);
        p.setTenantId(tenantId);
        p.setModule(module);
        p.setSystemPrompt(systemPrompt);
        p.setEnabled(1);
        p.setSortOrder(0);
        return p;
    }
}

package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiPersonaDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiPersonaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiPersonaService} 单元测试。
 *
 * <p>测试：DB 优先查询、租户回退、代码默认回退逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
class AiPersonaServiceTest {

    @Mock
    private DeepayAiPersonaMapper personaMapper;

    @InjectMocks
    private AiPersonaService aiPersonaService;

    // ====================================================================
    // 租户自定义优先
    // ====================================================================

    @Test
    void testGetSystemPrompt_tenantCustom_shouldReturnTenantPrompt() {
        // Given: 租户 123 有自定义 design persona
        DeepayAiPersonaDO custom = new DeepayAiPersonaDO();
        custom.setTenantId(123L);
        custom.setModule("design");
        custom.setSystemPrompt("租户123专属设计师prompt");
        custom.setEnabled(1);

        when(personaMapper.selectByTenantAndModule(123L, "design")).thenReturn(custom);

        // When
        String prompt = aiPersonaService.getSystemPrompt(123L, "design");

        // Then: 应使用租户自定义
        assertEquals("租户123专属设计师prompt", prompt);
        verify(personaMapper).selectByTenantAndModule(123L, "design");
        // 不应查全局默认
        verify(personaMapper, never()).selectByTenantAndModule(0L, "design");
    }

    // ====================================================================
    // 全局默认回退
    // ====================================================================

    @Test
    void testGetSystemPrompt_tenantNotFound_shouldFallbackToGlobal() {
        // Given: 租户 123 无自定义，全局有配置
        when(personaMapper.selectByTenantAndModule(123L, "finance")).thenReturn(null);

        DeepayAiPersonaDO global = new DeepayAiPersonaDO();
        global.setTenantId(0L);
        global.setModule("finance");
        global.setSystemPrompt("全局财务总监prompt");
        global.setEnabled(1);
        when(personaMapper.selectByTenantAndModule(0L, "finance")).thenReturn(global);

        // When
        String prompt = aiPersonaService.getSystemPrompt(123L, "finance");

        // Then: 应使用全局默认
        assertEquals("全局财务总监prompt", prompt);
    }

    // ====================================================================
    // 代码硬编码回退
    // ====================================================================

    @Test
    void testGetSystemPrompt_noDbConfig_shouldFallbackToHardcoded() {
        // Given: DB 无任何配置
        when(personaMapper.selectByTenantAndModule(anyLong(), anyString())).thenReturn(null);

        // When: 查询 selection
        String prompt = aiPersonaService.getSystemPrompt(0L, "selection");

        // Then: 应返回代码默认值
        assertNotNull(prompt);
        assertTrue(prompt.contains("购物顾问") || prompt.contains("选款"),
                "应包含 selection 角色描述");
    }

    @Test
    void testGetSystemPrompt_unknownModule_shouldReturnDefaultFallback() {
        // Given: DB 无配置，module 不在默认 map 中
        when(personaMapper.selectByTenantAndModule(anyLong(), anyString())).thenReturn(null);

        // When
        String prompt = aiPersonaService.getSystemPrompt(0L, "unknownmodule");

        // Then: 应返回最终兜底
        assertNotNull(prompt);
        assertTrue(prompt.contains("AI 助手") || prompt.contains("助手"),
                "未知模块应返回通用 AI 助手 prompt");
    }

    // ====================================================================
    // DB 异常时回退
    // ====================================================================

    @Test
    void testGetSystemPrompt_dbException_shouldFallbackGracefully() {
        // Given: DB 抛出异常
        when(personaMapper.selectByTenantAndModule(anyLong(), anyString()))
                .thenThrow(new RuntimeException("DB 连接失败"));

        // When: 不应抛出异常
        assertDoesNotThrow(() -> {
            String prompt = aiPersonaService.getSystemPrompt(0L, "selection");
            assertNotNull(prompt, "DB 异常时应回退到代码默认值");
        });
    }

    // ====================================================================
    // CRUD
    // ====================================================================

    @Test
    void testCreate_shouldSetDefaultValues() {
        // Given
        DeepayAiPersonaDO persona = new DeepayAiPersonaDO();
        persona.setModule("sales");
        persona.setSystemPrompt("测试销售prompt");

        // When
        aiPersonaService.create(persona);

        // Then: 应设置默认值
        assertEquals(1, persona.getEnabled(), "enabled 默认应为 1");
        assertEquals(0, persona.getDeleted(), "deleted 默认应为 0");
        assertNotNull(persona.getCreateTime());
        assertNotNull(persona.getUpdateTime());
        verify(personaMapper).insert(persona);
    }

    @Test
    void testDelete_existingPersona_shouldSoftDelete() {
        // Given
        DeepayAiPersonaDO existing = new DeepayAiPersonaDO();
        existing.setId(1L);
        existing.setDeleted(0);
        when(personaMapper.selectById(1L)).thenReturn(existing);
        when(personaMapper.updateById((DeepayAiPersonaDO) any())).thenReturn(1);

        // When
        boolean result = aiPersonaService.delete(1L);

        // Then: 软删除
        assertTrue(result);
        assertEquals(1, existing.getDeleted(), "删除后 deleted 应为 1");
        verify(personaMapper).updateById((DeepayAiPersonaDO) existing);
    }

    @Test
    void testDelete_notFound_shouldReturnFalse() {
        when(personaMapper.selectById(999L)).thenReturn(null);

        boolean result = aiPersonaService.delete(999L);

        assertFalse(result);
    }

}

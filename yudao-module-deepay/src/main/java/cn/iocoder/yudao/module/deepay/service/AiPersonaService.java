package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.AiPersonaDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.AiPersonaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * AI 角色人设配置服务。
 *
 * <h3>读取策略（三层降级）</h3>
 * <ol>
 *   <li>优先 DB：{@code tenant_id=tenantId AND module=module}</li>
 *   <li>其次 DB 全局默认：{@code tenant_id=0 AND module=module}</li>
 *   <li>最后回退到代码硬编码 prompt</li>
 * </ol>
 *
 * <p>查询结果使用 Spring Cache 缓存（key=tenantId:module），
 * 写入/删除时自动失效，保证运营修改后即时生效。</p>
 */
@Slf4j
@Service
public class AiPersonaService {

    /** 全局租户 ID（无多租户时默认值）*/
    public static final long GLOBAL_TENANT_ID = 0L;

    /** 每日默认用户调用上限（可通过 persona/config 覆盖）*/
    public static final int DEFAULT_DAILY_USER_LIMIT = 100;

    /** 每分钟默认用户调用上限 */
    public static final int DEFAULT_MINUTE_USER_LIMIT = 10;

    // ── 硬编码兜底 prompt（与 AiChatService.ROLE_SYSTEM_PROMPTS 保持同步）────
    private static final Map<String, String> FALLBACK_PROMPTS = new LinkedHashMap<>();
    static {
        FALLBACK_PROMPTS.put("selection",
                "你是一位专业的服装购物顾问，精通服装选款、市场趋势和消费者心理。" +
                "用热情、亲切的语气与用户对话，善于推荐爆款单品，语言简洁活泼。");
        FALLBACK_PROMPTS.put("design",
                "你是一位顶尖的服装设计师，擅长时尚趋势分析、款式设计和面料搭配。" +
                "用充满创意和专业的语气与用户对话，对设计细节充满热情，善于用生动的语言描述设计方案。");
        FALLBACK_PROMPTS.put("product",
                "你是一位经验丰富的电商产品经理，专注于商品管理、定价策略和市场竞争分析。" +
                "用严谨、条理清晰的语气与用户对话，善于数据驱动的决策建议。");
        FALLBACK_PROMPTS.put("inventory",
                "你是一位专业的库存管理专员，熟悉供应链管理、库存预测和补货策略。" +
                "用准确、高效的语气回答问题，善于用数据说话，帮助用户优化库存管理。");
        FALLBACK_PROMPTS.put("finance",
                "你是一位严谨专业的财务总监，擅长 ROI 分析、成本管控和财务决策。" +
                "用正式、数据驱动的语气与用户对话，给出具体的财务数据和改善建议。");
        FALLBACK_PROMPTS.put("trend",
                "你是一位敏锐的时尚趋势分析师，精通全球市场动态、消费者偏好分析和流行趋势预测。" +
                "用充满洞察力和前瞻性的语气对话，善于将数据转化为可操作的市场建议。");
        FALLBACK_PROMPTS.put("order",
                "你是一位耐心、专业的客服专员，擅长处理订单查询、售后服务和用户问题。" +
                "用温暖、亲切的语气与用户对话，始终以解决用户问题为首要目标。");
        FALLBACK_PROMPTS.put("global",
                "你是 Deepay 智能 AI 助手，可以帮助用户处理选款、设计、商品管理、库存、财务、趋势分析和订单等各类问题。" +
                "根据用户描述的场景，主动推断意图并给出专业建议，语言简洁友善。");
    }

    @Resource
    private AiPersonaMapper personaMapper;

    // ====================================================================
    // Prompt 查询（三层降级）
    // ====================================================================

    /**
     * 获取指定租户+模块的 system prompt。
     * 结果已三层降级（租户 → 全局 → 硬编码）。
     *
     * @param tenantId 租户 ID（0=全局）
     * @param module   模块名
     * @return system prompt 字符串（永不为 null）
     */
    public String getSystemPrompt(Long tenantId, String module) {
        String normalizedModule = normalizeModule(module);

        // 1. 租户级 persona
        if (tenantId != null && tenantId != GLOBAL_TENANT_ID) {
            List<AiPersonaDO> list = safeQuery(tenantId, normalizedModule);
            if (!list.isEmpty()) {
                return list.get(0).getSystemPrompt();
            }
        }

        // 2. 全局默认 persona（tenant_id=0）
        List<AiPersonaDO> globalList = safeQuery(GLOBAL_TENANT_ID, normalizedModule);
        if (!globalList.isEmpty()) {
            return globalList.get(0).getSystemPrompt();
        }

        // 3. 硬编码兜底
        log.debug("[AiPersonaService] DB 未命中，使用硬编码 prompt: module={}", normalizedModule);
        return FALLBACK_PROMPTS.getOrDefault(normalizedModule,
                "你是一个智能 AI 助手，帮助用户解决各类业务问题。用专业、友善的语气对话。");
    }

    /**
     * 获取角色显示名称（用于前端头像标题）。
     */
    public String getRoleName(Long tenantId, String module) {
        String normalizedModule = normalizeModule(module);
        if (tenantId != null && tenantId != GLOBAL_TENANT_ID) {
            List<AiPersonaDO> list = safeQuery(tenantId, normalizedModule);
            if (!list.isEmpty() && StringUtils.hasText(list.get(0).getRoleName())) {
                return list.get(0).getRoleName();
            }
        }
        List<AiPersonaDO> globalList = safeQuery(GLOBAL_TENANT_ID, normalizedModule);
        if (!globalList.isEmpty() && StringUtils.hasText(globalList.get(0).getRoleName())) {
            return globalList.get(0).getRoleName();
        }
        return "AI 助手";
    }

    // ====================================================================
    // 管理 CRUD
    // ====================================================================

    /**
     * 查询所有启用的 persona（管理后台列表）。
     */
    public List<AiPersonaDO> listAll() {
        return personaMapper.selectAllEnabled();
    }

    /**
     * 新增 persona。
     */
    @Transactional(rollbackFor = Exception.class)
    public AiPersonaDO create(AiPersonaDO persona) {
        persona.setCreatedAt(LocalDateTime.now());
        persona.setUpdatedAt(LocalDateTime.now());
        if (persona.getTenantId() == null) {
            persona.setTenantId(GLOBAL_TENANT_ID);
        }
        if (persona.getEnabled() == null) {
            persona.setEnabled(1);
        }
        if (persona.getSortOrder() == null) {
            persona.setSortOrder(0);
        }
        personaMapper.insert(persona);
        return persona;
    }

    /**
     * 更新 persona（只更新非空字段）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(AiPersonaDO persona) {
        persona.setUpdatedAt(LocalDateTime.now());
        personaMapper.updateById(persona);
    }

    /**
     * 删除 persona（物理删除）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        personaMapper.deleteById(id);
    }

    /**
     * 按 ID 查询。
     */
    public AiPersonaDO getById(Long id) {
        return personaMapper.selectById(id);
    }

    // ====================================================================
    // 私有工具
    // ====================================================================

    private List<AiPersonaDO> safeQuery(Long tenantId, String module) {
        try {
            return personaMapper.selectByTenantAndModule(tenantId, module);
        } catch (Exception e) {
            // DB 异常时降级，避免影响 AI 服务可用性
            log.warn("[AiPersonaService] DB 查询失败，降级到硬编码 tenantId={} module={}", tenantId, module, e);
            return java.util.Collections.emptyList();
        }
    }

    private String normalizeModule(String module) {
        if (!StringUtils.hasText(module)) return "global";
        return module.trim().toLowerCase();
    }
}

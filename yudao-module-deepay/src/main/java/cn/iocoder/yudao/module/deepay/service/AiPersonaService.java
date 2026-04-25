package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiPersonaDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiPersonaMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Persona 服务 — 从数据库读取 system prompt 配置，支持多租户差异化。
 *
 * <h3>查询优先级</h3>
 * <ol>
 *   <li>tenantId + module（租户自定义）</li>
 *   <li>tenantId=0 + module（全局默认）</li>
 *   <li>代码硬编码默认值（兜底）</li>
 * </ol>
 */
@Slf4j
@Service
public class AiPersonaService {

    // ====================================================================
    // 代码硬编码的默认 system prompt（Persona DB 回退）
    // ====================================================================
    private static final Map<String, String> DEFAULT_PROMPTS = new LinkedHashMap<>();
    static {
        DEFAULT_PROMPTS.put("selection",
                "你是一位专业的服装购物顾问，精通服装选款、市场趋势和消费者心理。" +
                "用热情、亲切的语气与用户对话，善于推荐爆款单品，语言简洁活泼。");
        DEFAULT_PROMPTS.put("design",
                "你是一位顶尖的服装设计师，擅长时尚趋势分析、款式设计和面料搭配。" +
                "用充满创意和专业的语气与用户对话，对设计细节充满热情，善于用生动的语言描述设计方案。");
        DEFAULT_PROMPTS.put("product",
                "你是一位经验丰富的电商产品经理，专注于商品管理、定价策略和市场竞争分析。" +
                "用严谨、条理清晰的语气与用户对话，善于数据驱动的决策建议。");
        DEFAULT_PROMPTS.put("inventory",
                "你是一位专业的库存管理专员，熟悉供应链管理、库存预测和补货策略。" +
                "用准确、高效的语气回答问题，善于用数据说话，帮助用户优化库存管理。");
        DEFAULT_PROMPTS.put("finance",
                "你是一位严谨专业的财务总监，擅长 ROI 分析、成本管控和财务决策。" +
                "用正式、数据驱动的语气与用户对话，给出具体的财务数据和改善建议。");
        DEFAULT_PROMPTS.put("trend",
                "你是一位敏锐的时尚趋势分析师，精通全球市场动态、消费者偏好分析和流行趋势预测。" +
                "用充满洞察力和前瞻性的语气对话，善于将数据转化为可操作的市场建议。");
        DEFAULT_PROMPTS.put("order",
                "你是一位耐心、专业的客服专员，擅长处理订单查询、售后服务和用户问题。" +
                "用温暖、亲切的语气与用户对话，始终以解决用户问题为首要目标。");
        DEFAULT_PROMPTS.put("sales",
                "你是一位顶级销售顾问，擅长分析客户购买动机、处理异议、推进成交。" +
                "你的话术有温度有力度：先共情，再塑造价值，最后顺水推舟推进下一步。" +
                "你会说动天懂地，但绝不花里胡哨——每一句话都让客户感受到你在帮他/她。");
    }
    private static final String DEFAULT_FALLBACK =
            "你是一个智能 AI 助手，帮助用户解决各类业务问题。用专业、友善的语气对话。";

    @Resource
    private DeepayAiPersonaMapper personaMapper;

    // ====================================================================
    // 查询 system prompt
    // ====================================================================

    /**
     * 获取指定租户 + 板块的 system prompt。
     *
     * @param tenantId 租户 ID（0=默认）
     * @param module   板块名
     * @return system prompt 文本
     */
    public String getSystemPrompt(Long tenantId, String module) {
        String mod = normalizeModule(module);

        // 1. 租户自定义配置
        if (tenantId != null && tenantId > 0) {
            DeepayAiPersonaDO persona = safeSelect(tenantId, mod);
            if (persona != null && StringUtils.hasText(persona.getSystemPrompt())) {
                log.debug("[AiPersona] 使用租户自定义 tenantId={} module={}", tenantId, mod);
                return persona.getSystemPrompt();
            }
        }

        // 2. 全局默认（tenantId=0）
        DeepayAiPersonaDO globalPersona = safeSelect(0L, mod);
        if (globalPersona != null && StringUtils.hasText(globalPersona.getSystemPrompt())) {
            log.debug("[AiPersona] 使用全局 DB 配置 module={}", mod);
            return globalPersona.getSystemPrompt();
        }

        // 3. 代码默认
        String hardcoded = DEFAULT_PROMPTS.get(mod);
        if (StringUtils.hasText(hardcoded)) {
            return hardcoded;
        }
        return DEFAULT_FALLBACK;
    }

    /**
     * 获取 persona 完整信息（含 examples / toolWhitelist）。
     * 若不存在则返回 null。
     */
    public DeepayAiPersonaDO getPersona(Long tenantId, String module) {
        String mod = normalizeModule(module);
        if (tenantId != null && tenantId > 0) {
            DeepayAiPersonaDO p = safeSelect(tenantId, mod);
            if (p != null) return p;
        }
        return safeSelect(0L, mod);
    }

    // ====================================================================
    // 管理 CRUD
    // ====================================================================

    public List<DeepayAiPersonaDO> listAll() {
        return personaMapper.selectAllEnabled();
    }

    public DeepayAiPersonaDO create(DeepayAiPersonaDO persona) {
        persona.setEnabled(persona.getEnabled() != null ? persona.getEnabled() : 1);
        persona.setDeleted(0);
        persona.setCreateTime(LocalDateTime.now());
        persona.setUpdateTime(LocalDateTime.now());
        personaMapper.insert(persona);
        return persona;
    }

    public boolean update(DeepayAiPersonaDO persona) {
        if (persona.getId() == null) return false;
        persona.setUpdateTime(LocalDateTime.now());
        return personaMapper.updateById(persona) > 0;
    }

    public boolean delete(Long id) {
        DeepayAiPersonaDO p = personaMapper.selectById(id);
        if (p == null) return false;
        p.setDeleted(1);
        p.setUpdateTime(LocalDateTime.now());
        return personaMapper.updateById(p) > 0;
    }

    // ====================================================================
    // Private helpers
    // ====================================================================

    private DeepayAiPersonaDO safeSelect(Long tenantId, String module) {
        try {
            return personaMapper.selectByTenantAndModule(tenantId, module);
        } catch (Exception e) {
            log.warn("[AiPersona] DB 查询异常，回退默认 tenantId={} module={}", tenantId, module, e);
            return null;
        }
    }

    private String normalizeModule(String module) {
        return StringUtils.hasText(module) ? module.toLowerCase().trim() : "selection";
    }

}

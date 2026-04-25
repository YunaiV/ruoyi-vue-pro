package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiMemoryItemDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayAiMemoryItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 长久记忆服务。
 *
 * <h3>按板块记忆策略</h3>
 * <ul>
 *   <li>design  — preferredStyle, favoriteColor, budgetRange, fabric, dislikeReasons, targetScene</li>
 *   <li>sales   — motivation, objections, decisionStyle, priceRange, communicationStyle</li>
 *   <li>finance — paymentMethod, preferredChain, invoicePreference, riskTolerance</li>
 *   <li>order   — deliveryPreference, aftersaleHistory, preferredCourier</li>
 *   <li>selection — category, crowd, style, market, priceLevel</li>
 * </ul>
 *
 * <h3>记忆保留期</h3>
 * 默认 365 天（{@value #MEMORY_TTL_DAYS} 天）。
 */
@Slf4j
@Service
public class AiMemoryService {

    /** 默认记忆保留天数（365天）*/
    public static final int MEMORY_TTL_DAYS = 365;

    /** 按板块定义允许的记忆字段白名单 */
    private static final Map<String, Set<String>> MODULE_ALLOWED_KEYS = new LinkedHashMap<>();
    static {
        MODULE_ALLOWED_KEYS.put("design",     new LinkedHashSet<>(Arrays.asList(
                "preferredStyle", "favoriteColor", "budgetRange",
                "fabric", "dislikeReasons", "targetScene", "sizePreference", "styleNotes"
        )));
        MODULE_ALLOWED_KEYS.put("sales",      new LinkedHashSet<>(Arrays.asList(
                "motivation", "objections", "decisionStyle",
                "priceRange", "communicationStyle", "lastConversionBlock"
        )));
        MODULE_ALLOWED_KEYS.put("finance",    new LinkedHashSet<>(Arrays.asList(
                "paymentMethod", "preferredChain", "invoicePreference",
                "riskTolerance", "preferredCurrency"
        )));
        MODULE_ALLOWED_KEYS.put("order",      new LinkedHashSet<>(Arrays.asList(
                "deliveryPreference", "aftersaleHistory",
                "preferredCourier", "returnTolerance"
        )));
        MODULE_ALLOWED_KEYS.put("selection",  new LinkedHashSet<>(Arrays.asList(
                "category", "crowd", "style", "market", "priceLevel", "purpose"
        )));
        MODULE_ALLOWED_KEYS.put("inventory",  new LinkedHashSet<>(Arrays.asList(
                "restockPreference", "safetyStockRatio"
        )));
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private DeepayAiMemoryItemMapper memoryItemMapper;

    // ====================================================================
    // 读取：组装 MemoryPack 注入到 prompt
    // ====================================================================

    /**
     * 为指定用户+板块组装 MemoryPack（注入到 AI prompt 前缀）。
     *
     * @param tenantId   租户 ID
     * @param customerId 用户/客户 ID
     * @param module     板块名
     * @return 格式化文本，若无记忆则返回 ""
     */
    public String buildMemoryPack(Long tenantId, String customerId, String module) {
        if (!StringUtils.hasText(customerId)) return "";

        try {
            List<DeepayAiMemoryItemDO> items =
                    memoryItemMapper.selectByCustomerAndModule(
                            tenantId != null ? tenantId : 0L, customerId,
                            module != null ? module.toLowerCase() : "selection");

            if (items.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            sb.append("【用户记忆（仅供参考，不要直接告知用户）】\n");
            for (DeepayAiMemoryItemDO item : items) {
                sb.append("- ").append(item.getMemKey()).append(": ").append(item.getMemValue()).append("\n");
            }
            sb.append("【记忆结束】\n");
            return sb.toString();

        } catch (Exception e) {
            log.warn("[AiMemory] 读取记忆失败 customerId={}", customerId, e);
            return "";
        }
    }

    /**
     * 查询用户在指定板块的所有记忆（管理端展示用）。
     */
    public List<DeepayAiMemoryItemDO> listMemory(Long tenantId, String customerId, String module) {
        if (!StringUtils.hasText(customerId)) return Collections.emptyList();
        return memoryItemMapper.selectByCustomerAndModule(
                tenantId != null ? tenantId : 0L, customerId,
                module != null ? module.toLowerCase() : "selection");
    }

    /**
     * 查询用户所有记忆（跨板块）。
     */
    public List<DeepayAiMemoryItemDO> listAllMemory(Long tenantId, String customerId) {
        if (!StringUtils.hasText(customerId)) return Collections.emptyList();
        return memoryItemMapper.selectAllByCustomer(
                tenantId != null ? tenantId : 0L, customerId);
    }

    // ====================================================================
    // 写入
    // ====================================================================

    /**
     * 保存/更新一条记忆条目（upsert 语义）。
     * 若 key 不在白名单中，忽略写入（防止随意写入敏感字段）。
     *
     * @param tenantId        租户 ID
     * @param customerId      用户/客户 ID
     * @param module          板块
     * @param memoryType      记忆类型（profile/fact/task）
     * @param key             记忆键
     * @param value           记忆值
     * @param confidence      置信度（0~1）
     * @param sourceSessionId 来源会话 ID
     */
    public void saveMemory(Long tenantId, String customerId, String module,
                           String memoryType, String key, String value,
                           double confidence, String sourceSessionId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(key)) return;

        String mod = module != null ? module.toLowerCase() : "selection";

        // 白名单检查
        Set<String> allowed = MODULE_ALLOWED_KEYS.get(mod);
        if (allowed != null && !allowed.contains(key)) {
            log.debug("[AiMemory] key={} 不在模块 {} 白名单中，跳过写入", key, mod);
            return;
        }

        DeepayAiMemoryItemDO item = new DeepayAiMemoryItemDO();
        item.setTenantId(tenantId != null ? tenantId : 0L);
        item.setCustomerId(customerId);
        item.setModule(mod);
        item.setMemoryType(StringUtils.hasText(memoryType) ? memoryType : "fact");
        item.setMemKey(key);
        item.setMemValue(value);
        item.setConfidence(BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, confidence))));
        item.setSourceSessionId(sourceSessionId);
        item.setExpiresAt(LocalDateTime.now().plusDays(MEMORY_TTL_DAYS));
        item.setDeleted(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        try {
            memoryItemMapper.upsertMemory(item);
            log.debug("[AiMemory] 已保存记忆 customerId={} module={} key={}", customerId, mod, key);
        } catch (Exception e) {
            log.warn("[AiMemory] 保存记忆失败 customerId={} key={}", customerId, key, e);
        }
    }

    /**
     * 批量保存记忆（从 AI 回复中提取的结构化记忆）。
     *
     * @param tenantId        租户 ID
     * @param customerId      用户/客户 ID
     * @param module          板块
     * @param memoriesJson    JSON 数组格式：[{"key":"preferredStyle","value":"极简","type":"profile","confidence":0.9}]
     * @param sourceSessionId 来源会话 ID
     */
    public void saveMemoriesFromJson(Long tenantId, String customerId, String module,
                                      String memoriesJson, String sourceSessionId) {
        if (!StringUtils.hasText(memoriesJson) || !StringUtils.hasText(customerId)) return;
        try {
            List<Map<String, Object>> list = MAPPER.readValue(memoriesJson,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            for (Map<String, Object> m : list) {
                String key        = String.valueOf(m.getOrDefault("key", ""));
                String value      = String.valueOf(m.getOrDefault("value", ""));
                String type       = String.valueOf(m.getOrDefault("type", "fact"));
                double confidence = Double.parseDouble(String.valueOf(m.getOrDefault("confidence", "0.8")));
                saveMemory(tenantId, customerId, module, type, key, value, confidence, sourceSessionId);
            }
        } catch (Exception e) {
            log.warn("[AiMemory] JSON 解析失败 customerId={}", customerId, e);
        }
    }

    // ====================================================================
    // 删除（合规）
    // ====================================================================

    /**
     * 软删除用户所有记忆（合规删除，GDPR 场景）。
     */
    public void deleteAllMemory(Long tenantId, String customerId) {
        if (!StringUtils.hasText(customerId)) return;
        memoryItemMapper.softDeleteAllByCustomer(
                tenantId != null ? tenantId : 0L, customerId);
        log.info("[AiMemory] 已删除用户全部记忆 customerId={}", customerId);
    }

    /**
     * 获取按板块允许的记忆字段白名单（前端展示用）。
     */
    public Map<String, Set<String>> getAllowedKeys() {
        return Collections.unmodifiableMap(MODULE_ALLOWED_KEYS);
    }

}

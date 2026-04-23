package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayCustomerProfileDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayCustomerProfileMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CustomerProfileAgent — 客户记忆读/写中枢（Phase 6 任务 2）。
 *
 * <p>两个核心方法：
 * <ul>
 *   <li>{@link #loadProfile(Long)} — 按 customerId 加载画像并填充 Context，首次无数据时返回空 ctx</li>
 *   <li>{@link #updateProfile(Long, Context)} — 将 ctx 中的偏好写回 deepay_customer_profile</li>
 * </ul>
 * </p>
 *
 * <p>作为 {@link Agent} 时，{@link #run} 等价于 loadProfile：Orchestrator 在流程入口调用。</p>
 */
@Component
public class CustomerProfileAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfileAgent.class);

    @Resource private DeepayCustomerProfileMapper profileMapper;
    @Resource private ObjectMapper                objectMapper;

    // ====================================================================
    // Agent.run — Orchestrator 入口调用，等价于 loadProfile
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        if (ctx.customerId == null) {
            log.debug("[CustomerProfileAgent] 无 customerId，跳过画像加载");
            return ctx;
        }
        return loadProfile(ctx.customerId, ctx);
    }

    // ====================================================================
    // 公共方法
    // ====================================================================

    /**
     * 加载客户画像并填充到 Context。
     *
     * <p>行为：
     * <ul>
     *   <li>第一次（无数据）— ctx 不变（返回空），Orchestrator 据此触发 SmartQuestionAgent</li>
     *   <li>有数据 — 将 category / style / market / priceLevel / targetAge / gender
     *               / confidenceScore 注入 ctx（已有值不覆盖，保留本次调用方设定的覆盖项）</li>
     * </ul>
     * </p>
     */
    public Context loadProfile(Long customerId, Context ctx) {
        DeepayCustomerProfileDO profile = profileMapper.selectByCustomerId(customerId);
        if (profile == null) {
            log.info("[CustomerProfileAgent] customerId={} 无历史画像，新客户", customerId);
            ctx.confidenceScore = BigDecimal.ZERO;
            return ctx;
        }

        // 只在 ctx 字段为空时填充（保留调用方显式传入的值）
        if (!StringUtils.hasText(ctx.category))   ctx.category   = profile.getCategoryLevel1();
        if (!StringUtils.hasText(ctx.style))       ctx.style      = extractPrimaryStyle(profile.getStyleWeights());
        if (!StringUtils.hasText(ctx.market))      ctx.market     = profile.getMarket();
        if (!StringUtils.hasText(ctx.priceLevel))  ctx.priceLevel = profile.getPriceLevel();
        if (!StringUtils.hasText(ctx.targetAge))   ctx.targetAge  = profile.getTargetAge();
        if (!StringUtils.hasText(ctx.gender))      ctx.gender     = profile.getGender();
        if (ctx.confidenceScore == null)            ctx.confidenceScore = profile.getConfidenceScore();

        // keyword 兜底：画像品类 → 更精准的 DesignAgent prompt
        if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(profile.getCategoryLevel1())) {
            ctx.keyword = profile.getCategoryLevel1();
        }

        log.info("[CustomerProfileAgent] 画像加载完成 customerId={} category={} style={} confidence={}",
                customerId, ctx.category, ctx.style, ctx.confidenceScore);
        return ctx;
    }

    /**
     * 便捷重载（Context 已有 customerId）。
     */
    public Context loadProfile(Long customerId) {
        return loadProfile(customerId, new Context());
    }

    /**
     * 将 Context 中的偏好回写到画像（SmartQuestionAgent 填充答案后调用）。
     */
    public void updateProfile(Long customerId, Context ctx) {
        if (customerId == null) {
            log.warn("[CustomerProfileAgent] updateProfile: customerId 为 null，跳过");
            return;
        }

        DeepayCustomerProfileDO profile = profileMapper.selectByCustomerId(customerId);
        if (profile == null) {
            profile = new DeepayCustomerProfileDO();
            profile.setCustomerId(customerId);
            profile.setConfidenceScore(BigDecimal.ZERO);
            profile.setCreatedAt(LocalDateTime.now());
        }

        // 覆盖所有非空字段
        if (StringUtils.hasText(ctx.category))   profile.setCategoryLevel1(ctx.category);
        if (StringUtils.hasText(ctx.style)) {
            profile.setStyleWeights(mergeStyleWeight(profile.getStyleWeights(), ctx.style, 0.1));
        }
        if (StringUtils.hasText(ctx.market))      profile.setMarket(ctx.market);
        if (StringUtils.hasText(ctx.priceLevel))  profile.setPriceLevel(ctx.priceLevel);
        if (StringUtils.hasText(ctx.targetAge))   profile.setTargetAge(ctx.targetAge);
        if (StringUtils.hasText(ctx.gender))      profile.setGender(ctx.gender);

        // SmartQuestion 完整回答后设置初始置信度 0.6（如尚未达到）
        boolean hasFullAnswers = StringUtils.hasText(ctx.category)
                && StringUtils.hasText(ctx.market)
                && StringUtils.hasText(ctx.style)
                && StringUtils.hasText(ctx.priceLevel);
        if (hasFullAnswers && (profile.getConfidenceScore() == null
                || profile.getConfidenceScore().compareTo(new BigDecimal("0.6")) < 0)) {
            profile.setConfidenceScore(new BigDecimal("0.6"));
        }

        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.upsert(profile);
        log.info("[CustomerProfileAgent] 画像更新完成 customerId={} category={} confidence={}",
                customerId, profile.getCategoryLevel1(), profile.getConfidenceScore());
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    /**
     * 从 styleWeights JSON 中提取权重最高的风格名称。
     * 格式：{"minimalist":0.8,"luxury":0.3}
     */
    private String extractPrimaryStyle(String styleWeightsJson) {
        if (!StringUtils.hasText(styleWeightsJson)) {
            return null;
        }
        try {
            Map<String, Double> weights = objectMapper.readValue(
                    styleWeightsJson, new TypeReference<Map<String, Double>>() {});
            return weights.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("[CustomerProfileAgent] 解析 styleWeights 失败，原始值={}", styleWeightsJson, e);
            return null;
        }
    }

    /**
     * 将指定风格的权重 +delta 后序列化回 JSON（其他风格保持不变）。
     * 若 JSON 解析失败则重建为仅含此风格的新 JSON。
     */
    private String mergeStyleWeight(String existingJson, String style, double delta) {
        Map<String, Double> weights = new LinkedHashMap<>();
        if (StringUtils.hasText(existingJson)) {
            try {
                weights = objectMapper.readValue(
                        existingJson, new TypeReference<Map<String, Double>>() {});
            } catch (Exception ignored) {
                // 旧 JSON 损坏，重建
            }
        }
        double current = weights.getOrDefault(style, 0.0);
        weights.put(style, Math.min(1.0, current + delta));
        try {
            return objectMapper.writeValueAsString(weights);
        } catch (Exception e) {
            log.warn("[CustomerProfileAgent] 序列化 styleWeights 失败", e);
            return existingJson;
        }
    }

}

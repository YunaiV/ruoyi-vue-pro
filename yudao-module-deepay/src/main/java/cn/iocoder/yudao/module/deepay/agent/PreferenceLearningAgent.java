package cn.iocoder.yudao.module.deepay.agent;

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
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayCustomerProfileDO;

/**
 * PreferenceLearningAgent — 偏好学习（Phase 6 任务 9）。
 *
 * <p>触发时机：用户点击 / 选择设计图时（非 Orchestrator 主流程，由图片选择接口单独调用）。</p>
 *
 * <p>更新规则：
 * <pre>
 *   style_weights[selectedStyle] += 0.1   （上限 1.0）
 *   category 更新为本次 ctx.category
 *   confidence += 0.05                     （上限 1.0）
 * </pre>
 * </p>
 *
 * <p>作为 {@link Agent} 时（在 Orchestrator 末尾调用），只在 ctx.selectedImage 非空时运行，
 * 相当于"系统自动替用户记录一次偏好"。</p>
 */
@Component
public class PreferenceLearningAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PreferenceLearningAgent.class);

    /** 每次学习后 confidence 增加量 */
    private static final BigDecimal CONFIDENCE_DELTA = new BigDecimal("0.05");

    /** style_weights 每次增加量 */
    private static final double STYLE_DELTA = 0.1;

    @Resource private DeepayCustomerProfileMapper profileMapper;
    @Resource private ObjectMapper                objectMapper;

    // ====================================================================
    // Agent.run — Orchestrator 末尾调用（系统自动学习）
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        if (ctx.customerId == null) {
            log.debug("[PreferenceLearningAgent] 无 customerId，跳过偏好学习");
            return ctx;
        }
        if (!StringUtils.hasText(ctx.selectedImage)) {
            log.debug("[PreferenceLearningAgent] 无 selectedImage，跳过偏好学习");
            return ctx;
        }
        learn(ctx.customerId, ctx);
        return ctx;
    }

    // ====================================================================
    // 公共方法（由图片选择接口主动调用）
    // ====================================================================

    /**
     * 用户选择图片时触发偏好学习。
     *
     * @param customerId 客户 ID
     * @param ctx        含 selectedImage / category / style / market / priceLevel 的 Context
     */
    public void learn(Long customerId, Context ctx) {
        if (customerId == null) {
            log.warn("[PreferenceLearningAgent] customerId 为 null，跳过");
            return;
        }

        DeepayCustomerProfileDO profile = profileMapper.selectByCustomerId(customerId);
        boolean isNew = (profile == null);
        if (isNew) {
            profile = new DeepayCustomerProfileDO();
            profile.setCustomerId(customerId);
            profile.setConfidenceScore(BigDecimal.ZERO);
            profile.setCreatedAt(LocalDateTime.now());
        }

        // 1. 更新 style_weights（+0.1，上限 1.0）
        if (StringUtils.hasText(ctx.style)) {
            String newWeights = mergeStyleWeight(profile.getStyleWeights(), ctx.style, STYLE_DELTA);
            profile.setStyleWeights(newWeights);
            log.info("[PreferenceLearningAgent] style_weights 更新 customerId={} style={} newWeights={}",
                    customerId, ctx.style, newWeights);
        }

        // 2. 更新品类（以本次实际 category 为准）
        if (StringUtils.hasText(ctx.category)) {
            profile.setCategoryLevel1(ctx.category);
        }

        // 3. 同步其他画像字段（非空时更新）
        if (StringUtils.hasText(ctx.market))      profile.setMarket(ctx.market);
        if (StringUtils.hasText(ctx.priceLevel))  profile.setPriceLevel(ctx.priceLevel);
        if (StringUtils.hasText(ctx.targetAge))   profile.setTargetAge(ctx.targetAge);
        if (StringUtils.hasText(ctx.gender))      profile.setGender(ctx.gender);

        // 4. confidence += 0.05（上限 1.0）
        BigDecimal oldConf = profile.getConfidenceScore() != null
                ? profile.getConfidenceScore() : BigDecimal.ZERO;
        BigDecimal newConf = oldConf.add(CONFIDENCE_DELTA).min(BigDecimal.ONE);
        profile.setConfidenceScore(newConf);

        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.upsert(profile);

        log.info("[PreferenceLearningAgent] 偏好学习完成 customerId={} category={} style={} confidence={}→{}",
                customerId, profile.getCategoryLevel1(), ctx.style, oldConf, newConf);
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

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
            log.warn("[PreferenceLearningAgent] 序列化 styleWeights 失败", e);
            return existingJson;
        }
    }

}

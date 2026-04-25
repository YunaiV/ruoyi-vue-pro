package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserProfileMapper;
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
 * MemoryAgent — 用户画像记忆（Phase 6 任务 1）。
 *
 * <p>功能：
 * <ol>
 *   <li><b>load</b> — 从 deepay_user_profile 加载用户画像，自动填充 ctx 各字段</li>
 *   <li><b>save</b> — 将 ctx 中的偏好回写画像，下次直接用无需再问</li>
 * </ol>
 * </p>
 *
 * <p>用户标识：{@link Context#userId}（Long）转字符串存储，兼容外部平台 UID。</p>
 *
 * <p>作为 {@link Agent}，{@link #run} 等价于 load，在 Orchestrator 最前面调用。</p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 能存 — upsert 幂等</li>
 *   <li>✔ 能查 — selectByUserId</li>
 *   <li>✔ 一个用户一条记录</li>
 *   <li>✔ 每次调用自动补充 ctx</li>
 * </ul>
 * </p>
 */
@Component
public class MemoryAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(MemoryAgent.class);

    @Resource private DeepayUserProfileMapper profileMapper;
    @Resource private ObjectMapper            objectMapper;

    // ====================================================================
    // Agent.run — Orchestrator 入口
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        String uid = resolveUserId(ctx);
        if (uid == null) {
            log.debug("[MemoryAgent] 无 userId，跳过记忆加载");
            return ctx;
        }

        DeepayUserProfileDO profile = profileMapper.selectByUserId(uid);
        if (profile == null) {
            log.info("[MemoryAgent] userId={} 首次使用，无历史画像", uid);
            ctx.confidenceScore = BigDecimal.ZERO;
            return ctx;
        }

        // 只在 ctx 字段为空时填充（保留调用方显式传入的值）
        if (!StringUtils.hasText(ctx.category))   ctx.category  = profile.getCategory();
        if (!StringUtils.hasText(ctx.style))       ctx.style     = profile.getStylePreference();
        if (!StringUtils.hasText(ctx.market))      ctx.market    = profile.getMarket();
        if (!StringUtils.hasText(ctx.priceLevel))  ctx.priceLevel = profile.getPriceRange();
        if (!StringUtils.hasText(ctx.targetAge))   ctx.targetAge  = profile.getAgeGroup();
        if (!StringUtils.hasText(ctx.gender))      ctx.gender     = profile.getGender();
        if (ctx.confidenceScore == null)            ctx.confidenceScore = profile.getConfidence();

        // 解析 styleWeights JSON → Map<String, Double>
        if (ctx.styleWeights == null && StringUtils.hasText(profile.getStyleWeightsJson())) {
            ctx.styleWeights = parseStyleWeights(profile.getStyleWeightsJson());
        }

        // keyword 兜底：无关键词时用品类填充
        if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(profile.getCategory())) {
            ctx.keyword = profile.getCategory();
        }

        log.info("[MemoryAgent] 记忆加载完成 userId={} category={} style={} market={} confidence={}",
                uid, ctx.category, ctx.style, ctx.market, ctx.confidenceScore);
        return ctx;
    }

    // ====================================================================
    // 公共方法：保存 / 更新画像
    // ====================================================================

    /**
     * 将 ctx 中的最新偏好写回 deepay_user_profile（幂等）。
     * 由 Orchestrator 在流程末尾调用，也可由 QuestionAgent 答完一题后立即调用。
     */
    public void save(Context ctx) {
        String uid = resolveUserId(ctx);
        if (uid == null) {
            log.warn("[MemoryAgent] save: 无 userId，跳过");
            return;
        }

        DeepayUserProfileDO profile = profileMapper.selectByUserId(uid);
        boolean isNew = (profile == null);
        if (isNew) {
            profile = new DeepayUserProfileDO();
            profile.setUserId(uid);
            profile.setConfidence(BigDecimal.ZERO);
            profile.setCreatedAt(LocalDateTime.now());
        }

        if (StringUtils.hasText(ctx.category))   profile.setCategory(ctx.category);
        if (StringUtils.hasText(ctx.style))       profile.setStylePreference(ctx.style);
        if (StringUtils.hasText(ctx.market))      profile.setMarket(ctx.market);
        if (StringUtils.hasText(ctx.priceLevel))  profile.setPriceRange(ctx.priceLevel);
        if (StringUtils.hasText(ctx.targetAge))   profile.setAgeGroup(ctx.targetAge);
        if (StringUtils.hasText(ctx.gender))      profile.setGender(ctx.gender);

        // 序列化 styleWeights
        if (ctx.styleWeights != null && !ctx.styleWeights.isEmpty()) {
            profile.setStyleWeightsJson(serializeWeights(ctx.styleWeights));
        } else if (StringUtils.hasText(ctx.style)) {
            // 无完整 weights 时，仅把主风格写入 weights（初始权重 0.5）
            Map<String, Double> w = new LinkedHashMap<>();
            w.put(ctx.style, 0.5);
            profile.setStyleWeightsJson(serializeWeights(w));
        }

        // 四个核心字段都有了 → 置信度升至 0.6
        boolean baseFilled = StringUtils.hasText(ctx.category)
                && StringUtils.hasText(ctx.market)
                && StringUtils.hasText(ctx.style)
                && StringUtils.hasText(ctx.priceLevel);
        if (baseFilled && (profile.getConfidence() == null
                || profile.getConfidence().compareTo(new BigDecimal("0.6")) < 0)) {
            profile.setConfidence(new BigDecimal("0.6"));
        }
        if (ctx.confidenceScore != null && (profile.getConfidence() == null
                || ctx.confidenceScore.compareTo(profile.getConfidence()) > 0)) {
            profile.setConfidence(ctx.confidenceScore);
        }

        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.upsert(profile);
        log.info("[MemoryAgent] 画像保存完成 userId={} category={} confidence={}",
                uid, profile.getCategory(), profile.getConfidence());
    }

    /**
     * 用户选图后更新风格权重 + 置信度（+0.05）。
     * 由 Orchestrator 末尾或图片选择接口调用。
     */
    public void learnFromSelection(Context ctx) {
        String uid = resolveUserId(ctx);
        if (uid == null || !StringUtils.hasText(ctx.style)) return;

        // 更新 styleWeights
        if (ctx.styleWeights == null) ctx.styleWeights = new LinkedHashMap<>();
        double cur = ctx.styleWeights.getOrDefault(ctx.style, 0.0);
        ctx.styleWeights.put(ctx.style, Math.min(1.0, cur + 0.1));

        // 置信度 +0.05
        if (ctx.confidenceScore == null) ctx.confidenceScore = BigDecimal.ZERO;
        ctx.confidenceScore = ctx.confidenceScore.add(new BigDecimal("0.05")).min(BigDecimal.ONE);

        save(ctx);
        log.info("[MemoryAgent] 偏好学习完成 userId={} style={} confidence={}",
                uid, ctx.style, ctx.confidenceScore);
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    /** 从 Context 中解析用户 ID 字符串（支持 userId Long 或 customerId Long）。 */
    private String resolveUserId(Context ctx) {
        if (ctx.userId != null)     return String.valueOf(ctx.userId);
        if (ctx.customerId != null) return String.valueOf(ctx.customerId);
        return null;
    }

    private Map<String, Double> parseStyleWeights(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            log.warn("[MemoryAgent] 解析 styleWeightsJson 失败: {}", json, e);
            return new LinkedHashMap<>();
        }
    }

    private String serializeWeights(Map<String, Double> weights) {
        try {
            return objectMapper.writeValueAsString(weights);
        } catch (Exception e) {
            log.warn("[MemoryAgent] 序列化 styleWeights 失败", e);
            return "{}";
        }
    }

}

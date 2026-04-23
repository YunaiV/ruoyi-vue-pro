package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayCustomerProfileDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepaySelectionLogDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendPoolDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayCustomerProfileMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepaySelectionLogMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTrendPoolMapper;
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
 * FeedbackAgent — 用户选款反馈学习（Phase 6-7 核心：越用越准）。
 *
 * <h3>三件事</h3>
 * <ol>
 *   <li>📝 <b>记录行为</b>：每次点选（选中/跳过）落库 deepay_selection_log</li>
 *   <li>⚡ <b>权重更新</b>：选中 +5 / 跳过 -3，更新 deepay_trend_pool.score（动态打分）</li>
 *   <li>🧠 <b>画像进化</b>：选中的风格权重 +0.1，更新 deepay_customer_profile.style_weights</li>
 * </ol>
 *
 * <h3>调用时机</h3>
 * <ul>
 *   <li>用户点击"选这款" → {@link #learn(Context, String, boolean)} isSelected=true</li>
 *   <li>用户点击"跳过" → {@link #learn(Context, String, boolean)} isSelected=false</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ selection_log 每次点选都落库</li>
 *   <li>✔ 选中多次的款式 score 上升，下次排名更靠前</li>
 *   <li>✔ 客户偏好风格的 style_weights 自动增长</li>
 * </ul>
 */
@Component
public class FeedbackAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAgent.class);

    /** 选中时趋势库评分增量 */
    private static final int SELECT_SCORE_DELTA = 5;
    /** 跳过时趋势库评分增量（负数，但不低于0） */
    private static final int SKIP_SCORE_DELTA   = -3;
    /** 选中时风格权重增量 */
    private static final double STYLE_WEIGHT_DELTA = 0.1;
    /** 画像置信度增量 */
    private static final BigDecimal CONFIDENCE_DELTA = new BigDecimal("0.05");

    @Resource private DeepaySelectionLogMapper   selectionLogMapper;
    @Resource private DeepayTrendPoolMapper      trendPoolMapper;
    @Resource private DeepayCustomerProfileMapper profileMapper;
    @Resource private ObjectMapper               objectMapper;

    // ====================================================================
    // Agent.run — Orchestrator 末尾被动调用（系统自动学习）
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        if (ctx.customerId == null || !StringUtils.hasText(ctx.selectedImage)) {
            log.debug("[FeedbackAgent] 无 customerId 或 selectedImage，跳过");
            return ctx;
        }
        learn(ctx, ctx.selectedImage, true);
        return ctx;
    }

    // ====================================================================
    // 主要公开接口（由 SelectionFlowService / Controller 主动调用）
    // ====================================================================

    /**
     * 记录用户选款行为并触发学习。
     *
     * @param ctx        包含 customerId / category / style / crowd 的 Context
     * @param imageUrl   用户操作的参考图 URL
     * @param isSelected true=选中（喜欢）/ false=跳过（不感兴趣）
     */
    public void learn(Context ctx, String imageUrl, boolean isSelected) {
        if (ctx.customerId == null) {
            log.warn("[FeedbackAgent] customerId 为 null，跳过学习");
            return;
        }

        // 1️⃣ 记录行为到 deepay_selection_log
        recordLog(ctx.customerId, imageUrl, ctx.category, ctx.style, ctx.crowd, isSelected);

        // 2️⃣ 更新趋势库评分（动态打分：选中 +5，跳过 -3）
        updateTrendScore(imageUrl, isSelected ? SELECT_SCORE_DELTA : SKIP_SCORE_DELTA);

        // 3️⃣ 选中时更新客户画像风格权重（画像进化）
        if (isSelected && StringUtils.hasText(ctx.style)) {
            evolveProfile(ctx);
        }

        log.info("[FeedbackAgent] 学习完成 customerId={} imageUrl={} isSelected={} style={}",
                ctx.customerId, shorten(imageUrl), isSelected, ctx.style);
    }

    // ====================================================================
    // 内部实现
    // ====================================================================

    private void recordLog(Long customerId, String imageUrl, String category,
                           String style, String crowd, boolean isSelected) {
        try {
            DeepaySelectionLogDO record = new DeepaySelectionLogDO();
            record.setCustomerId(customerId);
            record.setImageUrl(imageUrl);
            record.setCategory(category);
            record.setStyle(style);
            record.setCrowd(crowd);
            record.setIsSelected(isSelected ? 1 : 0);
            record.setCreatedAt(LocalDateTime.now());
            selectionLogMapper.insert(record);
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 记录 selection_log 失败", e);
        }
    }

    private void updateTrendScore(String imageUrl, int delta) {
        if (!StringUtils.hasText(imageUrl)) return;
        try {
            DeepayTrendPoolDO trend = trendPoolMapper.selectByImageUrl(imageUrl);
            if (trend != null) {
                trendPoolMapper.updateScore(trend.getId(), delta);
                log.debug("[FeedbackAgent] trend_pool score delta={} imageUrl={}",
                        delta, shorten(imageUrl));
            }
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 更新 trend_pool score 失败 imageUrl={}", shorten(imageUrl), e);
        }
    }

    private void evolveProfile(Context ctx) {
        try {
            DeepayCustomerProfileDO profile = profileMapper.selectByCustomerId(ctx.customerId);
            boolean isNew = (profile == null);
            if (isNew) {
                profile = new DeepayCustomerProfileDO();
                profile.setCustomerId(ctx.customerId);
                profile.setConfidenceScore(BigDecimal.ZERO);
                profile.setCreatedAt(LocalDateTime.now());
            }

            // 风格权重 +0.1（上限 1.0）
            String newWeights = mergeStyleWeight(profile.getStyleWeights(), ctx.style, STYLE_WEIGHT_DELTA);
            profile.setStyleWeights(newWeights);

            // 同步品类/客群/市场
            if (StringUtils.hasText(ctx.category)) profile.setCategoryLevel1(ctx.category);
            if (StringUtils.hasText(ctx.crowd))    profile.setCrowd(ctx.crowd);
            if (StringUtils.hasText(ctx.market))   profile.setMarket(ctx.market);

            // confidence += 0.05（上限 1.0）
            BigDecimal old = profile.getConfidenceScore() != null
                    ? profile.getConfidenceScore() : BigDecimal.ZERO;
            profile.setConfidenceScore(old.add(CONFIDENCE_DELTA).min(BigDecimal.ONE));

            profile.setUpdatedAt(LocalDateTime.now());
            profileMapper.upsert(profile);

            log.info("[FeedbackAgent] 画像进化 customerId={} style={} newWeights={} confidence={}",
                    ctx.customerId, ctx.style, newWeights, profile.getConfidenceScore());
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 画像进化失败 customerId={}", ctx.customerId, e);
        }
    }

    private String mergeStyleWeight(String existingJson, String style, double delta) {
        Map<String, Double> weights = new LinkedHashMap<>();
        if (StringUtils.hasText(existingJson)) {
            try {
                weights = objectMapper.readValue(
                        existingJson, new TypeReference<Map<String, Double>>() {});
            } catch (Exception ignored) { /* 旧 JSON 损坏，重建 */ }
        }
        double current = weights.getOrDefault(style, 0.0);
        weights.put(style, Math.min(1.0, current + delta));
        try {
            return objectMapper.writeValueAsString(weights);
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 序列化 styleWeights 失败", e);
            return existingJson;
        }
    }

    private static String shorten(String url) {
        if (url == null) return "null";
        return url.length() > 60 ? url.substring(0, 60) + "..." : url;
    }

}

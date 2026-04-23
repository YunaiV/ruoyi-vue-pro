package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * DesignConfirmAgent — Phase 8 设计图最终确认 + 评分门控。
 *
 * <h3>评分公式（DesignConfirm）</h3>
 * <pre>
 * confirmScore = trendScore × 0.4 + brandScore × 0.3 + matchScore × 0.3
 *
 * trendScore  — trendImages 匹配度（有趋势图=80；无=50）
 * brandScore  — 品牌合规度（logoDetected=false→90；true→0）
 * matchScore  — 来自 StyleConsistencyAgent.styleConsistencyScore（已计算）
 * </pre>
 *
 * <h3>绝对过滤（不经过评分直接拒绝）</h3>
 * <ul>
 *   <li>检测到品牌 Logo（logoDetected=true）</li>
 *   <li>结构复杂度 &gt; 80</li>
 *   <li>品类与客户主营不符（由 StyleConsistencyAgent 已设 needRedesign）</li>
 *   <li>无设计图</li>
 * </ul>
 *
 * <h3>模式</h3>
 * <ul>
 *   <li>AUTO（designSelectMode=AI 或 selectedImage 已有）→ 自动选第一张，不打断流程</li>
 *   <li>HUMAN → 暂停，等设计师 / 客户确认</li>
 * </ul>
 */
@Component
public class DesignConfirmAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignConfirmAgent.class);

    private static final int CONFIRM_PASS_THRESHOLD = 60;

    @Override
    public Context run(Context ctx) {
        // 已确认过（重入保护）
        if (Boolean.TRUE.equals(ctx.designConfirmed)) {
            log.debug("[DesignConfirmAgent] 已确认，跳过");
            return ctx;
        }
        // RiskControl 已拦截
        if (Boolean.FALSE.equals(ctx.shouldProduce)) {
            log.debug("[DesignConfirmAgent] shouldProduce=false，跳过");
            return ctx;
        }

        // ── 绝对过滤 ──────────────────────────────────────────────
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            return reject(ctx, "[DesignConfirm] 无设计图，拒绝进入生产");
        }
        if (Boolean.TRUE.equals(ctx.logoDetected)) {
            return reject(ctx, "[DesignConfirm] 检测到品牌Logo，拒绝生产");
        }
        if (ctx.complexity != null && ctx.complexity > 80) {
            return reject(ctx, "[DesignConfirm] 结构复杂度=" + ctx.complexity + ">80，拒绝量产");
        }

        // ── 评分模型 ──────────────────────────────────────────────
        int trendScore = hasTrendImages(ctx) ? 80 : 50;
        int brandScore = Boolean.TRUE.equals(ctx.logoDetected) ? 0 : 90;
        int matchScore = ctx.styleConsistencyScore != null ? ctx.styleConsistencyScore : 70;

        int confirmScore = ScoreUtil.computeConfirmScore(trendScore, brandScore, matchScore);
        ctx.designScore  = confirmScore;

        log.info("[DesignConfirmAgent] score={} (trend={} brand={} match={}) chainCode={}",
                confirmScore, trendScore, brandScore, matchScore, ctx.chainCode);

        if (confirmScore < CONFIRM_PASS_THRESHOLD) {
            ctx.needRedesign   = true;
            ctx.decisionReason = "[DesignConfirm] score=" + confirmScore + "<" + CONFIRM_PASS_THRESHOLD + "，触发重设计";
            log.info("[DesignConfirmAgent] ❌ 评分不足，触发重设计 chainCode={}", ctx.chainCode);
            return ctx;
        }

        // ── 模式判断 ─────────────────────────────────────────────
        return "AI".equalsIgnoreCase(ctx.designSelectMode) || StringUtils.hasText(ctx.selectedImage)
                ? doAutoConfirm(ctx)
                : doHumanConfirm(ctx);
    }

    // ----------------------------------------------------------------

    private Context doAutoConfirm(Context ctx) {
        if (!StringUtils.hasText(ctx.selectedImage) && ctx.designImages != null && !ctx.designImages.isEmpty()) {
            ctx.selectedImage = ctx.designImages.get(0);
        }
        ctx.designConfirmed = true;
        ctx.shouldProduce   = true;
        log.info("[DesignConfirmAgent] ✅ AUTO确认 score={} image={}", ctx.designScore, shorten(ctx.selectedImage));
        return ctx;
    }

    private Context doHumanConfirm(Context ctx) {
        if (StringUtils.hasText(ctx.selectedImage)) {
            ctx.designConfirmed = true;
            ctx.shouldProduce   = true;
            log.info("[DesignConfirmAgent] ✅ HUMAN已有selectedImage，确认通过 score={}", ctx.designScore);
        } else {
            ctx.pendingQuestion = "请确认满意的设计图（回传 selectedImage URL 继续生产）";
            ctx.pendingField    = "confirmDesign";
            log.info("[DesignConfirmAgent] ⏸ 等待人工确认 designImages={} chainCode={}",
                    ctx.designImages != null ? ctx.designImages.size() : 0, ctx.chainCode);
        }
        return ctx;
    }

    private Context reject(Context ctx, String reason) {
        ctx.shouldProduce  = false;
        ctx.riskLevel      = "HIGH";
        ctx.decisionReason = reason;
        log.warn("[DesignConfirmAgent] ❌ {} chainCode={}", reason, ctx.chainCode);
        return ctx;
    }

    private boolean hasTrendImages(Context ctx) {
        return ctx.trendImages != null && !ctx.trendImages.isEmpty();
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 60 ? s.substring(0, 60) + "…" : s;
    }
}
